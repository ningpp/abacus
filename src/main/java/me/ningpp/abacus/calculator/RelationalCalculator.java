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

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;

import static me.ningpp.abacus.AbacusUtil.calculateOne;

public class RelationalCalculator implements Calculator {

    @Override
    public Object calculate(ExpressionDTO expr, Map<String, Object> context, int defaultScale, RoundingMode defaultRoundingMode, MathContext mathContext) {
        if (expr.getChildren().size() == 1) {
            return calculateOne(expr.getChildren().get(0), context, defaultScale, defaultRoundingMode, mathContext);
        }
        String symbol = expr.getChildren().get(1).getText();
        ExpressionDTO leftExpr = expr.getChildren().get(0);
        ExpressionDTO rightExpr = expr.getChildren().get(2);
        Object left = leftExpr.getCalculatedValue();
        Object right = rightExpr.getCalculatedValue();
        if (left instanceof Comparable leftNumber && right instanceof Comparable rightNumber) {
            int r = leftNumber.compareTo(rightNumber);
            if ("<".equals(symbol)) {
                return r < 0;
            } else if (">".equals(symbol)) {
                return r > 0;
            } else if ("<=".equals(symbol)) {
                return r <= 0;
            } else if (">=".equals(symbol)) {
                return r >= 0;
            } else {
                throw new IllegalStateException(expr.getText());
            }
        } else {
            throw new IllegalArgumentException("can't compare these two value, left " + left + ", right = " + right);
        }
    }
}
