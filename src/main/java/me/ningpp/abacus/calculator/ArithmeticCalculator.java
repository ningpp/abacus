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
package me.ningpp.abacus.calculator;

import me.ningpp.abacus.ExpressionDTO;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;

public class ArithmeticCalculator implements Calculator {

    @Override
    public Object calculate(ExpressionDTO expr, Map<String, Object> context, int defaultScale, RoundingMode defaultRoundingMode, MathContext mathContext) {
        String symbol = expr.getChildren().get(0).getText();
        boolean additiveSymbol = "+".equals(symbol) || "-".equals(symbol);
        return calculateNumber(additiveSymbol ? BigDecimal.ZERO : BigDecimal.ONE,
                symbol,
                expr.getChildren().get(1).getCalculatedValue(),
                defaultScale, defaultRoundingMode, mathContext);
    }

    public static BigDecimal calculateNumber(Object left, String preSymbol, Object right,
            int defaultScale, RoundingMode defaultRoundingMode, MathContext mathContext) {
        if (left == null) {
            throw new IllegalArgumentException("'left' value can't be null!");
        }
        if (right == null) {
            throw new IllegalArgumentException("'right' value can't be null!");
        }

        BigDecimal leftDecimal = toDecimal(left);
        BigDecimal rightDecimal = toDecimal(right);

        BigDecimal result;
        if ("*".equals(preSymbol)) {
            if (mathContext != null) {
                result = leftDecimal.multiply(rightDecimal, mathContext);
            } else {
                result = leftDecimal.multiply(rightDecimal);
            }
        } else if ("/".equals(preSymbol)) {
            if (mathContext != null) {
                result = leftDecimal.divide(rightDecimal, mathContext);
            } else {
                result = leftDecimal.divide(rightDecimal, defaultScale, defaultRoundingMode);
            }
        } else if ("+".equals(preSymbol)) {
            if (mathContext != null) {
                result = leftDecimal.add(rightDecimal, mathContext);
            } else {
                result = leftDecimal.add(rightDecimal);
            }
        } else if ("-".equals(preSymbol)) {
            if (mathContext != null) {
                result = leftDecimal.subtract(rightDecimal, mathContext);
            } else {
                result = leftDecimal.subtract(rightDecimal);
            }
        } else {
            throw new IllegalStateException("unsupport Symbol " + preSymbol);
        }
        return result;
    }

    public static BigDecimal toDecimal(Object left) {
        if (left instanceof BigDecimal r) {
            return r;
        } else if (left instanceof Number n) {
            return new BigDecimal(n.toString());
        } else if (left instanceof CharSequence c) {
            return new BigDecimal(c.toString());
        } else {
            throw new IllegalArgumentException("illegal value, expect BigDecimal but " + left.getClass().getName());
        }
    }

}
