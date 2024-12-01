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

import me.ningpp.abacus.calculator.AdditiveMultiplicativeCalculator;
import me.ningpp.abacus.calculator.ArithmeticCalculator;
import me.ningpp.abacus.calculator.Calculator;
import me.ningpp.abacus.calculator.ConditionalAndCalculator;
import me.ningpp.abacus.calculator.ConditionalCalculator;
import me.ningpp.abacus.calculator.ConditionalConditionCalculator;
import me.ningpp.abacus.calculator.ConditionalElseCalculator;
import me.ningpp.abacus.calculator.ConditionalOrCalculator;
import me.ningpp.abacus.calculator.ConditionalThenCalculator;
import me.ningpp.abacus.calculator.EqualityCalculator;
import me.ningpp.abacus.calculator.ExpressionCalculator;
import me.ningpp.abacus.calculator.MethodInvocationCalculator;
import me.ningpp.abacus.calculator.NumberCalculator;
import me.ningpp.abacus.calculator.ParenthesisCalculator;
import me.ningpp.abacus.calculator.PrimaryCalculator;
import me.ningpp.abacus.calculator.RelationalCalculator;
import me.ningpp.abacus.calculator.StringLiteralCalculator;
import me.ningpp.abacus.calculator.SymbolCalculator;
import me.ningpp.abacus.calculator.UnaryCalculator;
import me.ningpp.abacus.calculator.VariableCalculator;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.collections4.CollectionUtils;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

public final class AbacusUtil {

    private AbacusUtil() {
    }

    private static final Calculator[] CALCULATORS = new Calculator[ExpressionType.values().length];

    static {
        CALCULATORS[ExpressionType.ADDITIVE.ordinal()] = new AdditiveMultiplicativeCalculator();
        CALCULATORS[ExpressionType.MULTIPLICATIVE.ordinal()] = new AdditiveMultiplicativeCalculator();
        CALCULATORS[ExpressionType.ARITHMETIC.ordinal()] = new ArithmeticCalculator();
        CALCULATORS[ExpressionType.CONDITIONAL_AND.ordinal()] = new ConditionalAndCalculator();
        CALCULATORS[ExpressionType.CONDITIONAL.ordinal()] = new ConditionalCalculator();
        CALCULATORS[ExpressionType.CONDITIONAL_CONDITION.ordinal()] = new ConditionalConditionCalculator();
        CALCULATORS[ExpressionType.CONDITIONAL_ELSE.ordinal()] = new ConditionalElseCalculator();
        CALCULATORS[ExpressionType.CONDITIONAL_OR.ordinal()] = new ConditionalOrCalculator();
        CALCULATORS[ExpressionType.CONDITIONAL_THEN.ordinal()] = new ConditionalThenCalculator();
        CALCULATORS[ExpressionType.EQUALITY.ordinal()] = new EqualityCalculator();
        CALCULATORS[ExpressionType.EXPRESSION.ordinal()] = new ExpressionCalculator();
        CALCULATORS[ExpressionType.METHOD_INVOCATION.ordinal()] = new MethodInvocationCalculator();
        CALCULATORS[ExpressionType.NUMBER.ordinal()] = new NumberCalculator();
        CALCULATORS[ExpressionType.PARENTHESIS.ordinal()] = new ParenthesisCalculator();
        CALCULATORS[ExpressionType.PRIMARY.ordinal()] = new PrimaryCalculator();
        CALCULATORS[ExpressionType.RELATIONAL.ordinal()] = new RelationalCalculator();
        CALCULATORS[ExpressionType.STRING_LITERAL.ordinal()] = new StringLiteralCalculator();
        CALCULATORS[ExpressionType.SYMBOL.ordinal()] = new SymbolCalculator();
        CALCULATORS[ExpressionType.UNARY.ordinal()] = new UnaryCalculator();
        CALCULATORS[ExpressionType.VARIABLE.ordinal()] = new VariableCalculator();
    }

    public static ExpressionResultDTO parse(String inputExpression) {
        AbacusLexer lexer = new AbacusLexer(CharStreams.fromString(inputExpression));
        AbacusParser parser = new AbacusParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(new SyntaxErrorListener());
        AbacusDefaultVisitor visitor = new AbacusDefaultVisitor();
        return visitor.visit(parser.expression());
    }

    public static Object calculate(ExpressionResultDTO resultDto, Map<String, Object> context,
            int defaultScale, RoundingMode defaultRoundingMode, MathContext mathContext) {
        if (resultDto == null) {
            return null;
        }
        List<ExpressionDTO> exps = CollapseUtil.collapse(resultDto.getExpressions());
        return calculateCollapse(exps, context, defaultScale, defaultRoundingMode, mathContext);
    }

    public static Object calculate(ExpressionResultDTO resultDto, Map<String, Object> context,
            int defaultScale, RoundingMode defaultRoundingMode) {
        return calculate(resultDto, context, defaultScale, defaultRoundingMode, null);
    }

    public static Object calculateCollapse(List<ExpressionDTO> exps, Map<String, Object> context,
            int defaultScale, RoundingMode defaultRoundingMode) {
        return calculateCollapse(exps, context, defaultScale, defaultRoundingMode, null);
    }

    public static Object calculateCollapse(List<ExpressionDTO> exps, Map<String, Object> context,
            int defaultScale, RoundingMode defaultRoundingMode, MathContext mathContext) {
        if (CollectionUtils.isEmpty(exps)) {
            return null;
        }

        for (ExpressionDTO exp : exps) {
            calculateOne(exp, context, defaultScale, defaultRoundingMode, mathContext);
        }

        return exps.get(0).getCalculatedValue();
    }

    public static Object calculateOne(ExpressionDTO exp, Map<String, Object> context,
            int defaultScale, RoundingMode defaultRoundingMode, MathContext mathContext) {
        Calculator calculator = CALCULATORS[exp.getType().ordinal()];
        if (calculator == null) {
            throw new IllegalStateException("no calulator for " + exp.getType());
        }

        if (CollectionUtils.isNotEmpty(exp.getChildren())) {
            for (ExpressionDTO child : exp.getChildren()) {
                Object childCalculatedValue = calculateOne(child, context, defaultScale, defaultRoundingMode, mathContext);
                child.setCalculatedValue(childCalculatedValue);
            }
        }

        Object calculatedValue = calculator.calculate(exp, context, defaultScale, defaultRoundingMode, mathContext);
        exp.setCalculatedValue(calculatedValue);
        return calculatedValue;
    }

}
