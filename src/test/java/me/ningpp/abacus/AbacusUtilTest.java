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

import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @RepeatedTest(99)
    void randomCalculateTest() {
        Random random = new Random();
        int terms = random.nextInt(1, 32);
        int variableCount = random.nextInt(0, terms + 1);
        int depth = random.nextInt(0, 11);

        Map<String, BigDecimal> context = new LinkedHashMap<>();
        for (int i = 0; i < variableCount; i++) {
            context.put("$" + (i+1), randomValue(random));
        }

        List<String> expressionParts = new ArrayList<>(context.keySet());
        for (int i = variableCount; i < terms; i++) {
            expressionParts.add(randomValue(random).toPlainString());
        }

        String expression = generateRandomExpression(depth, 1, expressionParts, random);
        boolean resultEqual = false;
        BigDecimal abacusResult = null;
        BigDecimal qlResult = null;
        try {
            Pair<BigDecimal, Object> result = calculate(expression, context);
            qlResult = result.getRight() instanceof BigDecimal rr ? rr : new BigDecimal(result.getRight().toString());
            abacusResult = result.getLeft();
            resultEqual = qlResult.equals(abacusResult);
        } catch (ArithmeticException e) {
            // ignore
            resultEqual = true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        if (!resultEqual) {
            LOGGER.info("expressionParts:\t\t{}", String.join(", ", expressionParts));
            LOGGER.info("context:\t\t{}", context);
            LOGGER.info("expression:\t\t{}", expression);
            LOGGER.info("abacusResult:\t\t{}", abacusResult);
            LOGGER.info("qlResult:\t\t{}", qlResult);
        }
        assertTrue(resultEqual);
    }

    private Pair<BigDecimal, Object> calculate(String expression, Map<String, BigDecimal> context) throws Exception {
        int qlScale = 10;
        RoundingMode qlRoundingMode = RoundingMode.HALF_UP;

        BigDecimal abacusResult = AbacusUtil.calculate(AbacusUtil.parse(expression), context, qlScale, qlRoundingMode);

        DefaultContext<String, Object> qlContext = new DefaultContext<>();
        qlContext.putAll(context);
        ExpressRunner runner = new ExpressRunner(true, false);
        Object qlResult = runner.execute(expression, qlContext, null, true, false);

        return Pair.of(abacusResult, qlResult);
    }

    private String generateRandomExpression(int depth, int currentDepth, List<String> parts, Random random) {
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
            String thenExpr = generateRandomExpression(depth - 1, currentDepth + 1, parts, random);
            String elseExpr = generateRandomExpression(depth - 1, currentDepth + 1, parts, random);
            return String.format(Locale.ROOT,
                    " ( (%s) ? (%s) : ((%s)) ) ", conditionExpr, thenExpr, elseExpr);
        }

        boolean wrapInParentheses = random.nextInt(11) > 2;
        if (wrapInParentheses) {
            boolean oneExp = random.nextInt(11) > 6;
            if (oneExp) {
                return "(" + generateRandomExpression(depth - 1, currentDepth + 1, parts, random) + ")" +
                        OPERATORS.get(random.nextInt(OPERATORS.size())) +
                        generateRandomExpression(depth - 1, currentDepth + 1, parts, random);
            } else {
                return "(" +
                        parts.get(random.nextInt(parts.size())) +
                        OPERATORS.get(random.nextInt(OPERATORS.size())) +
                        generateRandomExpression(depth - 1, currentDepth + 1, parts, random) +
                        ")";
            }
        } else {
            return parts.get(random.nextInt(parts.size())) +
                    OPERATORS.get(random.nextInt(OPERATORS.size())) +
                    generateRandomExpression(depth, currentDepth + 1, parts, random);
        }
    }

    private String randomSimpleCondition(Random random) {
        return String.format(" %d %s %d ", random.nextInt(11),
                CONDITIONS.get(random.nextInt(CONDITIONS.size())),
                random.nextInt(11));
    }

    private BigDecimal randomValue(Random random) {
        String val = random.nextBoolean()
                ? String.valueOf(random.nextInt(1, 11))
                : random.nextInt(0, 11) + "." + String.format(Locale.ROOT, "%03d", random.nextInt(1000));
        return new BigDecimal(val);
    }

    @Test
    void calculateTest() throws Exception {
        Map<String, BigDecimal> context = new LinkedHashMap<>();
        context.put("$1", BigDecimal.valueOf(1));
        context.put("$2", BigDecimal.valueOf(2));
        context.put("$3", BigDecimal.valueOf(3));
        context.put("$4", BigDecimal.valueOf(4));
        context.put("$5", BigDecimal.valueOf(17));

        String expression = "(-1 * ($1 + $2 - $3 * $4)) / $5";

        int qlScale = 10;
        RoundingMode qlRoundingMode = RoundingMode.HALF_UP;

        BigDecimal abacusResult = AbacusUtil.calculate(AbacusUtil.parse(expression), context, qlScale, qlRoundingMode);

        DefaultContext<String, Object> qlContext = new DefaultContext<>();
        qlContext.putAll(context);
        ExpressRunner runner = new ExpressRunner(true, false);
        Object qlResult = runner.execute(expression, qlContext, null, true, false);
        assertEquals(qlResult, abacusResult);
    }

    @Test
    void conditionalCalculateTest() throws Exception {
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
                    Map<String, BigDecimal> context = new LinkedHashMap<>();
                    context.put("$1", BigDecimal.valueOf(i));
                    context.put("$2", BigDecimal.valueOf(j));
                    context.put("$3", BigDecimal.valueOf(3));
                    context.put("$4", BigDecimal.valueOf(7));

                    Pair<BigDecimal, Object> pair = calculate(expression, context);
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

}
