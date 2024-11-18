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

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AbacusUtil {

    private AbacusUtil() {
    }

    public static ExpressionResultDTO parse(String inputExpression) {
        AbacusLexer lexer = new AbacusLexer(CharStreams.fromString(inputExpression));
        AbacusParser parser = new AbacusParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(new SyntaxErrorListener());
        AbacusDefaultVisitor visitor = new AbacusDefaultVisitor();
        return visitor.visit(parser.additiveExpression());
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

    private static void calculate(List<ExpressionDTO> exps, Map<String, BigDecimal> context,
            int defaultScale, RoundingMode defaultRoundingMode) {
        for (ExpressionDTO exp : exps) {
            if (!exp.isCalculated()) {
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