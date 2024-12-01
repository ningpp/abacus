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

public class ConditionalCalculator implements Calculator {

    @Override
    public Object calculate(ExpressionDTO expr, Map<String, Object> context, int defaultScale, RoundingMode defaultRoundingMode, MathContext mathContext) {
        Object conditionObject = expr.getChildren().get(0).getCalculatedValue();
        if (conditionObject instanceof Boolean condition) {
            if (condition) {
                // then expr
                ExpressionDTO thenExpr = expr.getChildren().get(1);
                return thenExpr.getCalculatedValue();
            } else {
                // else expr
                ExpressionDTO elseExpr = expr.getChildren().get(2);
                return elseExpr.getCalculatedValue();
            }
        } else {
            throw new IllegalArgumentException("value must be Boolean value, but " + conditionObject + ", expression = " + expr.getText());
        }
    }

}
