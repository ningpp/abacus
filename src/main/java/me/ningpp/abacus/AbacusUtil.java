/*
 *    Copyright 2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package me.ningpp.abacus;

import me.ningpp.abacus.exception.MethodNotFoundException;
import me.ningpp.abacus.exception.SyntaxException;
import me.ningpp.abacus.methods.AbacusMethod;
import me.ningpp.abacus.methods.MaxMethod;
import me.ningpp.abacus.methods.MinMethod;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AbacusUtil {

    private AbacusUtil() {
    }

    private static final Map<String, AbacusMethod> METHODS = new HashMap<>();

    static {
        METHODS.put("max", new MaxMethod());
        METHODS.put("min", new MinMethod());
    }

    public static ExpressionResultDTO parse(String inputExpression) {
        AbacusLexer lexer = new AbacusLexer(CharStreams.fromString(inputExpression));
        AbacusParser parser = new AbacusParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(new SyntaxErrorListener());
        AbacusDefaultVisitor visitor = new AbacusDefaultVisitor();
        return visitor.visit(parser.expression());
    }

    public static BigDecimal calculate(ExpressionResultDTO resultDto, Map<String, BigDecimal> context,
            int defaultScale, RoundingMode defaultRoundingMode) {
        if (resultDto == null) {
            return null;
        }
        List<ExpressionDTO> exps = CollapseUtil.collapse(resultDto.getExpressions());
        return calculateCollapse(exps, context, defaultScale, defaultRoundingMode);
    }

    public static BigDecimal calculateCollapse(List<ExpressionDTO> exps, Map<String, BigDecimal> context,
            int defaultScale, RoundingMode defaultRoundingMode) {
        if (CollectionUtils.isEmpty(exps)) {
            return null;
        }

        List<ExpressionDTO> horizontals = treeToHorizontal(exps);

        while (hasNotCalculated(horizontals)) {
            calculate(horizontals, context, defaultScale, defaultRoundingMode);
        }

        ExpressionDTO root = new ExpressionDTO();
        root.setType(ExpressionType.ADDITIVE);
        root.setChildren(exps);
        calculate(List.of(root), context, defaultScale, defaultRoundingMode);
        return root.getCalculatedValue();
    }

    private static boolean calculateCondition(ExpressionDTO conditionExpr, Map<String, BigDecimal> context,
            int defaultScale, RoundingMode defaultRoundingMode) {
        if (conditionExpr.getType() == ExpressionType.RELATIONAL) {
            return calculateRelational(conditionExpr, context, defaultScale, defaultRoundingMode);
        } else if (conditionExpr.getType() == ExpressionType.EQUALITY) {
            return calculateEquality(conditionExpr, context, defaultScale, defaultRoundingMode);
        } else if (conditionExpr.getType() == ExpressionType.PARENTHESIS) {
            if (CollectionUtils.isEmpty(conditionExpr.getChildren())) {
                throw new SyntaxException("syntax error : " + conditionExpr.getText());
            }
            return calculateCondition(conditionExpr.getChildren().get(0), context, defaultScale, defaultRoundingMode);
        }
        return calculateConditionalOr(conditionExpr, context, defaultScale, defaultRoundingMode);
    }

    private static boolean calculateRelational(ExpressionDTO relationalExpr, Map<String, BigDecimal> context,
            int defaultScale, RoundingMode defaultRoundingMode) {
        if (relationalExpr.getChildren().size() == 1) {
            throw new IllegalStateException(relationalExpr.getText());
        } else {
            String symbol = relationalExpr.getChildren().get(1).getText();
            ExpressionDTO leftExpr = relationalExpr.getChildren().get(0);
            ExpressionDTO rightExpr = relationalExpr.getChildren().get(2);
            calculate(List.of(leftExpr), context, defaultScale, defaultRoundingMode);
            calculate(List.of(rightExpr), context, defaultScale, defaultRoundingMode);
            BigDecimal left = leftExpr.getCalculatedValue();
            BigDecimal right = rightExpr.getCalculatedValue();
            if ("<".equals(symbol)) {
                return left.compareTo(right) < 0;
            } else if (">".equals(symbol)) {
                return left.compareTo(right) > 0;
            } else if ("<=".equals(symbol)) {
                return left.compareTo(right) <= 0;
            } else if (">=".equals(symbol)) {
                return left.compareTo(right) >= 0;
            } else {
                throw new IllegalStateException(relationalExpr.getText());
            }
        }
    }

    private static boolean calculateEquality(ExpressionDTO equalityAndExpr, Map<String, BigDecimal> context,
            int defaultScale, RoundingMode defaultRoundingMode) {
        if (equalityAndExpr.getChildren().size() == 1) {
            return calculateRelational(equalityAndExpr.getChildren().get(0), context, defaultScale, defaultRoundingMode);
        } else {
            ExpressionDTO leftExpr = equalityAndExpr.getChildren().get(0);
            ExpressionDTO rightExpr = equalityAndExpr.getChildren().get(2);
            calculate(List.of(leftExpr), context, defaultScale, defaultRoundingMode);
            calculate(List.of(rightExpr), context, defaultScale, defaultRoundingMode);
            BigDecimal left = leftExpr.getCalculatedValue();
            BigDecimal right = rightExpr.getCalculatedValue();
            if ("==".equals(equalityAndExpr.getChildren().get(1).getText())) {
                return left.compareTo(right) == 0;
            } else {
                return left.compareTo(right) != 0;
            }
        }
    }

    private static boolean calculateConditionalAnd(ExpressionDTO conditionalAndExpr, Map<String, BigDecimal> context,
            int defaultScale, RoundingMode defaultRoundingMode) {

        if (conditionalAndExpr.getType() == ExpressionType.RELATIONAL) {
            return calculateRelational(conditionalAndExpr, context, defaultScale, defaultRoundingMode);
        } else if (conditionalAndExpr.getType() == ExpressionType.EQUALITY) {
            return calculateEquality(conditionalAndExpr, context, defaultScale, defaultRoundingMode);
        } else if (conditionalAndExpr.getType() == ExpressionType.PARENTHESIS) {
            if (CollectionUtils.isEmpty(conditionalAndExpr.getChildren())) {
                throw new SyntaxException("syntax error : " + conditionalAndExpr.getText());
            }
            return calculateCondition(conditionalAndExpr.getChildren().get(0), context, defaultScale, defaultRoundingMode);
        }

        if (conditionalAndExpr.getChildren().size() == 1) {
            return calculateEquality(conditionalAndExpr.getChildren().get(0), context, defaultScale, defaultRoundingMode);
        } else {
            return calculateConditionalAnd(conditionalAndExpr.getChildren().get(0), context, defaultScale, defaultRoundingMode)
                    && calculateConditionalAnd(conditionalAndExpr.getChildren().get(2), context, defaultScale, defaultRoundingMode);
        }
    }

    private static boolean calculateConditionalOr(ExpressionDTO conditionalOrExpr, Map<String, BigDecimal> context,
            int defaultScale, RoundingMode defaultRoundingMode) {
        if (conditionalOrExpr.getType() == ExpressionType.EQUALITY) {
            return calculateEquality(conditionalOrExpr, context, defaultScale, defaultRoundingMode);
        } else if (conditionalOrExpr.getType() == ExpressionType.CONDITIONAL_AND) {
            return calculateConditionalAnd(conditionalOrExpr, context, defaultScale, defaultRoundingMode);
        } else if (conditionalOrExpr.getType() == ExpressionType.RELATIONAL) {
            return calculateRelational(conditionalOrExpr, context, defaultScale, defaultRoundingMode);
        }

        if (conditionalOrExpr.getChildren().size() == 1) {
            return calculateConditionalAnd(conditionalOrExpr.getChildren().get(0), context, defaultScale, defaultRoundingMode);
        } else {
            return calculateConditionalAnd(conditionalOrExpr.getChildren().get(0), context, defaultScale, defaultRoundingMode)
                    || calculateConditionalOr(conditionalOrExpr.getChildren().get(2), context, defaultScale, defaultRoundingMode);
        }
    }

    private static void calculate(List<ExpressionDTO> exps, Map<String, BigDecimal> context,
            int defaultScale, RoundingMode defaultRoundingMode) {
        for (ExpressionDTO exp : exps) {
            if (ExpressionType.METHOD_INVOCATION == exp.getType()) {
                int a = 1;
            }
            if (exp.isCalculated()) {
                continue;
            }

            if (exp.getType() == ExpressionType.CONDITIONAL) {
                boolean condition = calculateCondition(exp.getChildren().get(0), context, defaultScale, defaultRoundingMode);
                if (condition) {
                    // then expr
                    ExpressionDTO thenExpr = exp.getChildren().get(1);
                    calculate(List.of(thenExpr), context, defaultScale, defaultRoundingMode);
                    exp.setCalculatedValue(thenExpr.getCalculatedValue());
                } else {
                    // else expr
                    ExpressionDTO elseExpr = exp.getChildren().get(2);
                    calculate(List.of(elseExpr), context, defaultScale, defaultRoundingMode);
                    exp.setCalculatedValue(elseExpr.getCalculatedValue());
                }
                exp.setCalculated(true);
                continue;
            }

            if (CollectionUtils.isNotEmpty(exp.getChildren())) {
                calculate(exp.getChildren(), context, defaultScale, defaultRoundingMode);

                if (exp.getType() == ExpressionType.MULTIPLICATIVE || exp.getType() == ExpressionType.ADDITIVE) {
                    BigDecimal result = exp.getChildren().get(0).getCalculatedValue();
                    if (exp.getChildren().size() > 1) {
                        String preSymbol = exp.getChildren().get(1).getText();
                        for (int i = 2; i < exp.getChildren().size(); i++) {
                            BigDecimal v = exp.getChildren().get(i).getCalculatedValue();
                            if (v != null) {
                                if ("*".equals(preSymbol)) {
                                    result = result.multiply(v);
                                } else if ("/".equals(preSymbol)) {
                                    result = result.divide(v, defaultScale, defaultRoundingMode);
                                } else if ("+".equals(preSymbol)) {
                                    result = result.add(v);
                                } else if ("-".equals(preSymbol)) {
                                    result = result.subtract(v);
                                } else {
                                    throw new IllegalStateException();
                                }
                            }
                            if (exp.getChildren().get(i).getType() == ExpressionType.SYMBOL) {
                                preSymbol = exp.getChildren().get(i).getText();
                            }
                        }
                    }

                    exp.setCalculatedValue(result);
                } else if (exp.getType() == ExpressionType.PARENTHESIS) {
                    exp.setCalculatedValue(exp.getChildren().get(0).getCalculatedValue());
                } else if (exp.getType() == ExpressionType.ARITHMETIC) {
                    String preSymbol = exp.getChildren().get(0).getText();
                    BigDecimal result;
                    if ("*".equals(preSymbol)) {
                        result = exp.getChildren().get(1).getCalculatedValue();
                    } else if ("/".equals(preSymbol)) {
                        result = BigDecimal.ONE.divide(exp.getChildren().get(1).getCalculatedValue(), defaultScale, defaultRoundingMode);
                    } else if ("+".equals(preSymbol)) {
                        result = exp.getChildren().get(1).getCalculatedValue();
                    } else if ("-".equals(preSymbol)) {
                        result = BigDecimal.ZERO.subtract(exp.getChildren().get(1).getCalculatedValue());
                    } else {
                        throw new IllegalStateException();
                    }
                    exp.setCalculatedValue(result);
                } else if (exp.getType() == ExpressionType.METHOD_INVOCATION) {
                    String methodName = exp.getChildren().get(0).getText();
                    AbacusMethod abacusMethod = METHODS.get(methodName);
                    if (abacusMethod == null) {
                        throw new MethodNotFoundException("should register method before use it, method name is " + methodName);
                    }
                    Object[] args = null;
                    int childCount = exp.getChildren().size();
                    int argCount = exp.getChildren().size() - 1;
                    if (argCount > 0) {
                        args = new Object[argCount];
                        for (int i = 1; i < childCount; i++) {
                            args[i-1] = exp.getChildren().get(i).getCalculatedValue();
                        }
                    }
                    exp.setCalculatedValue((BigDecimal) abacusMethod.execute(args));
                }

                exp.setCalculated(true);
            } else {
                if (exp.getType() == ExpressionType.NUMBER) {
                    exp.setCalculatedValue(new BigDecimal(exp.getText()));
                } else if (exp.getType() == ExpressionType.VARIABLE) {
                    exp.setCalculatedValue(context.get(exp.getText()));
                }
                exp.setCalculated(true);
            }
        }
    }

    private static boolean hasNotCalculated(List<ExpressionDTO> horizontals) {
        for (ExpressionDTO exp : horizontals) {
            if (! exp.isCalculated()) {
                return true;
            }
        }
        return false;
    }

    public static List<ExpressionDTO> treeToHorizontal(List<ExpressionDTO> list) {
        if (list == null) {
            return new ArrayList<>(0);
        }
        List<ExpressionDTO> temps = new ArrayList<>(64);
        for (ExpressionDTO n : list) {
            treeToList(temps, n);
        }
        return temps;
    }

    private static void treeToList(List<ExpressionDTO> list, ExpressionDTO n) {
        list.add(n);
        if (CollectionUtils.isNotEmpty(n.getChildren())) {
            for (ExpressionDTO child : n.getChildren()) {
                treeToList(list, child);
            }
        }
    }

}
