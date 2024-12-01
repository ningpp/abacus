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

public class ConditionalAndCalculator implements Calculator {

    @Override
    public Object calculate(ExpressionDTO expr, Map<String, Object> context, int defaultScale, RoundingMode defaultRoundingMode, MathContext mathContext) {
        if (expr.getChildren().size() == 1) {
            return calculateOne(expr.getChildren().get(0), context, defaultScale, defaultRoundingMode, mathContext);
        }
        Object left = expr.getChildren().get(0).getCalculatedValue();
        if (left instanceof Boolean leftBoolean) {
            if (Boolean.FALSE.equals(leftBoolean)) {
                return false;
            }

            Object right = expr.getChildren().get(2).getCalculatedValue();
            if (right instanceof Boolean rightBoolean) {
                return rightBoolean;
            }
            throw new IllegalArgumentException("right value must be Boolean value, but right = " + right);
        }
        throw new IllegalArgumentException("left value must be Boolean value, but left = " + left);
    }

}
