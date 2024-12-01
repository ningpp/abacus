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

import com.alibaba.qlexpress4.Express4Runner;
import com.alibaba.qlexpress4.InitOptions;
import com.alibaba.qlexpress4.QLOptions;
import com.alibaba.qlexpress4.api.QLFunctionalVarargs;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.lexer.token.OperatorType;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import me.ningpp.abacus.aviator.AviatorDivFunction;
import me.ningpp.abacus.methods.MaxMethod;
import me.ningpp.abacus.methods.MinMethod;
import me.ningpp.abacus.methods.StringContainsAnyMethod;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbacusUtilTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbacusUtilTest.class);

    private static final List<String> OPERATORS = List.of("+", "-", "*", "/");

    private static final List<String> CONDITIONS = List.of("<", "<=", ">", ">=", "==", "!=");

    @RepeatedTest(199)
    void randomCalculateTest() {
        Random random = new Random();
        int terms = random.nextInt(1, 32);
        int variableCount = random.nextInt(0, terms + 1);
        int depth = random.nextInt(0, 11);

        Map<String, Object> context = new LinkedHashMap<>();
        for (int i = 0; i < variableCount; i++) {
            context.put("$" + (i+1), randomValue(random));
        }

        List<String> expressionParts = new ArrayList<>(context.keySet());
        for (int i = variableCount; i < terms; i++) {
            expressionParts.add(randomValue(random).toPlainString());
        }

        String expression = generateRandomExpression(false, depth, 1, expressionParts, random);
        LOGGER.info("generateRandomExpression length:\t\t{}\t\t{}", expression.length(), expression);
        boolean resultEqual = false;
        Object abacusResult = null;
        Object aviatorResult = null;
        Object qlResult = null;
        try {
            Triple<Object, Object, Object> result = calculate(expression, context);
            qlResult = result.getRight() instanceof BigDecimal rr ? rr : new BigDecimal(result.getRight().toString());
            abacusResult = result.getLeft();
            aviatorResult = result.getMiddle() instanceof BigDecimal rr ? rr : new BigDecimal(result.getMiddle().toString());
            resultEqual = aviatorResult.equals(abacusResult) || abacusResult.equals(qlResult);
        } catch (ArithmeticException e) {
            // ignore
            resultEqual = true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            if ("Method code too large!".equals(e.getMessage())) {
                // ignore for aviator
                resultEqual = true;
            }
        }
        if (!resultEqual) {
            LOGGER.info("expressionParts:\t\t{}", String.join(", ", expressionParts));
            LOGGER.info("context:\t\t{}", context);
            LOGGER.info("expression:\t\t{}", expression);
            LOGGER.info("abacusResult:\t\t{}", abacusResult);
            LOGGER.info("aviatorResult:\t\t{}", aviatorResult);
            LOGGER.info("qlResult:\t\t{}", qlResult);
        }
        assertTrue(resultEqual);
    }

    private Triple<Object, Object, Object> calculate(String expression, Map<String, Object> context) {
        return calculate(expression, false, true, context);
    }

    private Triple<Object, Object, Object> calculate(String expression, boolean useQl4, boolean useQl3, Map<String, Object> context) {
        int qlScale = 10;
        RoundingMode qlRoundingMode = RoundingMode.HALF_UP;

        LocalDateTime start = LocalDateTime.now();
        ExpressionResultDTO prseResult = AbacusUtil.parse(expression);
        LocalDateTime parseEnd = LocalDateTime.now();
        List<ExpressionDTO> exps = CollapseUtil.collapse(prseResult.getExpressions());
        Object abacusResult = AbacusUtil.calculateCollapse(exps, context, qlScale, qlRoundingMode);
        LOGGER.info("Abacus parse expression cost {}", Duration.between(start, parseEnd).toMillis());
        LOGGER.info("Abacus calculate cost {}", Duration.between(parseEnd, LocalDateTime.now()).toMillis());

        AviatorEvaluator.getInstance().addOpFunction(OperatorType.DIV, new AviatorDivFunction(qlScale, qlRoundingMode));
        start = LocalDateTime.now();
        Expression aviatorCompiledExpr = AviatorEvaluator.compile(expression, false);
        LOGGER.info("Aviator parse expression cost {}", Duration.between(start, LocalDateTime.now()).toMillis());
        start = LocalDateTime.now();
        Object aviatorResult = aviatorCompiledExpr.execute(context);
        LOGGER.info("Aviator calculate cost {}", Duration.between(start, LocalDateTime.now()).toMillis());

        Object qlResult = null;
        try {
            if (useQl4) {
                Map<String, Object> qlContext = new LinkedHashMap<>(context);
                Express4Runner runner = new Express4Runner(InitOptions.builder().build());
                runner.addFunction("min", new QLMinMethod());
                runner.addFunction("max", new QLMaxMethod());
                runner.addFunction("stringContainsAny", new QLStringContainsAnyMethod());
                start = LocalDateTime.now();
                qlResult = runner.execute(expression, qlContext, QLOptions.builder().precise(true).build());
                LOGGER.info("QLExpress4 parse and calculate cost {}", Duration.between(start, LocalDateTime.now()).toMillis());
            }
            if (useQl3) {
                DefaultContext<String, Object> qlContext = new DefaultContext<>();
                qlContext.putAll(context);
                ExpressRunner runner = new ExpressRunner(true, false);
                start = LocalDateTime.now();
                qlResult = runner.execute(expression, qlContext, null, true, false);
                LOGGER.info("QLExpress3 parse and calculate cost {}", Duration.between(start, LocalDateTime.now()).toMillis());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return Triple.of(abacusResult, aviatorResult, qlResult);
    }

    private static class QLMinMethod implements QLFunctionalVarargs {
        private static final MinMethod METHOD = new MinMethod();

        @Override
        public Object call(Object[] args) {
            return METHOD.execute(args);
        }
    }

    private static class QLMaxMethod implements QLFunctionalVarargs {
        private static final MaxMethod METHOD = new MaxMethod();

        @Override
        public Object call(Object[] args) {
            return METHOD.execute(args);
        }
    }

    private static class QLStringContainsAnyMethod implements QLFunctionalVarargs {
        private static final StringContainsAnyMethod METHOD = new StringContainsAnyMethod();

        @Override
        public Object call(Object[] args) {
            return METHOD.execute(args);
        }
    }

    private String generateRandomExpression(boolean genStringContainsAny, int depth, int currentDepth, List<String> parts, Random random) {
        if (depth == 0) {
            List<String> expressionParts = new ArrayList<>(parts);
            Collections.shuffle(expressionParts);
            StringBuilder builder = new StringBuilder();
            int size = expressionParts.size();
            int last = size - 1;
            for (int i = 0; i < size; i++) {
                builder.append(expressionParts.get(i));
                if (i != last) {
                    builder.append(OPERATORS.get(random.nextInt(OPERATORS.size())));
                }
            }
            return builder.toString();
        }

        boolean useMethodInvocation = random.nextInt(101) < 8;
        if (useMethodInvocation) {
            int paramCount = 2 + random.nextInt(0, 3);
            String methodName;
            if (random.nextBoolean()) {
                methodName = "max";
            } else {
                methodName = "min";
            }
            List<String> params = new ArrayList<>(paramCount);
            for (int i = 0; i < paramCount; i++) {
                params.add(generateRandomExpression(genStringContainsAny, depth - 1, currentDepth + 1, parts, random));
            }
            return String.format(Locale.ROOT,
                    " %s( %s ) ", methodName, String.join(", ", params));
        }

        boolean useConditional = random.nextInt(101) < 8;
        if (useConditional) {
            String conditionExpr;
            if (random.nextBoolean()) {
                String format = random.nextBoolean() ? " %s && %s " : " %s || %s ";
                conditionExpr = String.format(format, randomSimpleCondition(random),
                        randomSimpleCondition(random));
            } else {
                conditionExpr = randomSimpleCondition(random);
            }
            if (genStringContainsAny && random.nextBoolean()) {
                conditionExpr += (random.nextBoolean() ? " && " : " || ") + randomStringContainsAnyCondition(random);
            }
            String thenExpr = generateRandomExpression(genStringContainsAny, depth - 1, currentDepth + 1, parts, random);
            String elseExpr = generateRandomExpression(genStringContainsAny, depth - 1, currentDepth + 1, parts, random);
            return String.format(Locale.ROOT,
                    " ( (%s) ? (%s) : ((%s)) ) ", conditionExpr, thenExpr, elseExpr);
        }

        boolean wrapInParentheses = random.nextInt(11) > 2;
        if (wrapInParentheses) {
            boolean oneExp = random.nextInt(11) > 6;
            if (oneExp) {
                return "(" + generateRandomExpression(genStringContainsAny, depth - 1, currentDepth + 1, parts, random) + ")" +
                        OPERATORS.get(random.nextInt(OPERATORS.size())) +
                        generateRandomExpression(genStringContainsAny, depth - 1, currentDepth + 1, parts, random);
            } else {
                return "(" +
                        parts.get(random.nextInt(parts.size())) +
                        OPERATORS.get(random.nextInt(OPERATORS.size())) +
                        generateRandomExpression(genStringContainsAny, depth - 1, currentDepth + 1, parts, random) +
                        ")";
            }
        } else {
            return parts.get(random.nextInt(parts.size())) +
                    OPERATORS.get(random.nextInt(OPERATORS.size())) +
                    generateRandomExpression(genStringContainsAny, depth, currentDepth + 1, parts, random);
        }
    }

    private String randomSimpleCondition(Random random) {
        return String.format(" %d %s %d ", random.nextInt(11),
                CONDITIONS.get(random.nextInt(CONDITIONS.size())),
                random.nextInt(11));
    }

    private String randomStringContainsAnyCondition(Random random) {
        int size = random.nextInt(1, 6);
        List<String> params = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            params.add(String.format(Locale.ROOT, "\"%s\"", RandomStringUtils.randomAlphanumeric(random.nextInt(1, 3))));
        }
        return String.format(" stringContainsAny(\"%s\", %s) ", RandomStringUtils.randomAlphanumeric(32),
                String.join(", ", params));
    }

    private BigDecimal randomValue(Random random) {
        String val = random.nextBoolean()
                ? String.valueOf(random.nextInt(1, 11))
                : random.nextInt(0, 11) + "." + String.format(Locale.ROOT, "%03d", random.nextInt(1000));
        return new BigDecimal(val);
    }

    @Test
    void calculateTest() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("$1", BigDecimal.valueOf(1));
        context.put("$2", BigDecimal.valueOf(2));
        context.put("$3", BigDecimal.valueOf(3));
        context.put("$4", BigDecimal.valueOf(4));
        context.put("$5", BigDecimal.valueOf(17));

        String expression = "(-1 * ($1 + $2 - $3 * $4)) / $5";

        Triple<Object, Object, Object> pair = calculate(expression, context);
        Object abacusResult = pair.getLeft();
        Object qlResult = pair.getRight();
        assertEquals(qlResult, abacusResult);
    }

    @Test
    void conditionalCalculateTest() {
        List<String> expressions = List.of(
                " $1 < $2 ? $3 / $4 : $4 / $3 ",
                " ($1 < $2) ? $3 / $4 : $4 / $3 ",
                " (((((((($1 < $2)))))))) ? $3 / $4 : $4 / $3 ",
                " $1 + $2 < $1 * $2 ? $3 / $4 : $4 / $3 ",
                //QL不支持这个语法 $1 + $2 < $1 * $2 ? $3 / $4 :   $1 + $2 == $1 * $2 ? $4 / $3 : $4 * $3
                " $1 + $2 < $1 * $2 ? $3 / $4 : ( $1 + $2 == $1 * $2 ? $4 / $3 : $4 * $3 ) ",
                " $1 + $2 < $1 * $2 ? $3 / $4 : ( $1 + $2 != $1 * $2 && $3 - $1 == $2 ? $4 / $3 : $4 * $3 ) ",
                " $1 + $2 < $1 * $2 ? $3 / $4 : ( $1 + $2 != $1 * $2 && ($3 - $1 == $2) || (((((($4 - $2 == $1)))))) ? $4 / $3 : $4 * $3 ) "
        );
        for (String expression : expressions) {
            for (int i = 1; i < 5; i++) {
                for (int j = 1; j < 5; j++) {
                    Map<String, Object> context = new LinkedHashMap<>();
                    context.put("$1", BigDecimal.valueOf(i));
                    context.put("$2", BigDecimal.valueOf(j));
                    context.put("$3", BigDecimal.valueOf(3));
                    context.put("$4", BigDecimal.valueOf(7));

                    Triple<Object, Object, Object> pair = calculate(expression, context);
                    BigDecimal right = pair.getRight() instanceof BigDecimal rr ? rr : new BigDecimal(pair.getRight().toString());
                    boolean resultEqual = right.equals(pair.getLeft());
                    if (!resultEqual) {
                        LOGGER.info("context:\t{}", context);
                        LOGGER.info("expression:\t{}", expression);
                    }
                    assertTrue(resultEqual);
                }
            }
        }
    }

    @Test
    void methodComplexTest() {
        String expression = " ( 1 + min(a, 1) ) < (b - 5)  ?  c * d :  ( f > g ? e * min(f, g) + max(f, g) : e / max(f, g) - min(f, g) ) ";
        for (int i = -1; i < 9; i++) {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("a", BigDecimal.valueOf( i ));
            context.put("b", BigDecimal.valueOf(7));
            context.put("c", BigDecimal.valueOf(11));
            context.put("d", BigDecimal.valueOf(13));
            context.put("e", BigDecimal.valueOf(17));
            context.put("f", BigDecimal.valueOf(new Random().nextDouble()));
            context.put("g", BigDecimal.valueOf(new Random().nextDouble()));

            Triple<Object, Object, Object> pair = calculate(expression, context);
            BigDecimal right = pair.getRight() instanceof BigDecimal rr ? rr : new BigDecimal(pair.getRight().toString());
            boolean resultEqual = right.equals(pair.getLeft());
            if (!resultEqual) {
                LOGGER.info("expression:\t{}", expression);
            }
            assertTrue(resultEqual);
        }
    }

    @Test
    void methodTest() {
        List<String> expressions = List.of(
                "max(1, 2)",
                "max(1, max(2, 3))",
                "max(max(1, 2), 3)",
                "max(max(1, 2), max(3, 4))",
                "max(1, max(2, max(3, 4)))",
                "max(max(1, 2), max(3, max(4, max(5, 6))))"
        );
        for (String expression : expressions) {
            Triple<Object, Object, Object> pair = calculate(expression, Map.of());
            BigDecimal right = pair.getRight() instanceof BigDecimal rr ? rr : new BigDecimal(pair.getRight().toString());
            boolean resultEqual = right.equals(pair.getLeft());
            if (!resultEqual) {
                LOGGER.info("expression:\t{}", expression);
            }
            assertTrue(resultEqual);
        }
    }

    @Test
    void stringLiteralTest() {
        List<ExpressionDTO> piExprs = CollapseUtil.collapse(AbacusUtil.parse(" \"3.141592653\" ").getExpressions());
        Object piResult = AbacusUtil.calculateCollapse(piExprs, Map.of(), 10, RoundingMode.HALF_UP);
        assertEquals("3.141592653", piResult);

        List<ExpressionDTO> abc26Exprs = CollapseUtil.collapse(AbacusUtil.parse(" \"abc26\" ").getExpressions());
        Object abc26Result = AbacusUtil.calculateCollapse(abc26Exprs, Map.of(), 10, RoundingMode.HALF_UP);
        assertEquals("abc26", abc26Result);

        List<ExpressionDTO> exprs = CollapseUtil.collapse(AbacusUtil.parse(" \"中文汉字\" ").getExpressions());
        Object result = AbacusUtil.calculateCollapse(exprs, Map.of(), 10, RoundingMode.HALF_UP);
        assertEquals("中文汉字", result);
    }

    @Test
    void stringContainsAnyTest() {
        String expression = " stringContainsAny(\"abcdef123456\", \"xyz\", \"xyzxyz\", \"xyzxyzxyz\", \"def123\") ? 3.14 : 2.718 ";
        assertEquals(BigDecimal.valueOf(3.14), AbacusUtil.calculate(AbacusUtil.parse(expression), Map.of(), 10, RoundingMode.HALF_UP));

        expression = " stringContainsAny(\"abcdef123456\", \"xyz\", \"xyzxyz\", \"xyzxyzxyz\") ? 3.14 : 2.718 ";
        assertEquals(BigDecimal.valueOf(2.718), AbacusUtil.calculate(AbacusUtil.parse(expression), Map.of(), 10, RoundingMode.HALF_UP));

        expression = " stringContainsAny(\"abcdef123456\", \"xyz\", \"def123\") ? 3.14 : 2.718 ";
        assertEquals(BigDecimal.valueOf(3.14), AbacusUtil.calculate(AbacusUtil.parse(expression), Map.of(), 10, RoundingMode.HALF_UP));

        expression = " stringContainsAny(\"abcdef123456\", \"xyz\", \"xyzxyz\") ? 3.14 : 2.718 ";
        assertEquals(BigDecimal.valueOf(2.718), AbacusUtil.calculate(AbacusUtil.parse(expression), Map.of(), 10, RoundingMode.HALF_UP));

        expression = " stringContainsAny(\"abcdef123456\", \"def123\") ? 3.14 : 2.718 ";
        assertEquals(BigDecimal.valueOf(3.14), AbacusUtil.calculate(AbacusUtil.parse(expression), Map.of(), 10, RoundingMode.HALF_UP));

        expression = " ( (stringContainsAny(\"abcdef123456\", \"xyzxyz\")) ? 3.14 : 2.718 )";
        assertEquals(BigDecimal.valueOf(2.718), AbacusUtil.calculate(AbacusUtil.parse(expression), Map.of(), 10, RoundingMode.HALF_UP));

    }

}
