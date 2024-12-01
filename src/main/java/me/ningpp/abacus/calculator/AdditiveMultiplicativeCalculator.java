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
import me.ningpp.abacus.ExpressionType;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;

import static me.ningpp.abacus.calculator.ArithmeticCalculator.calculateNumber;

public class AdditiveMultiplicativeCalculator implements Calculator {

    @Override
    public Object calculate(ExpressionDTO expr, Map<String, Object> context, int defaultScale, RoundingMode defaultRoundingMode, MathContext mathContext) {
        Object result = expr.getChildren().get(0).getCalculatedValue();
        if (expr.getChildren().size() > 1) {
            String preSymbol = expr.getChildren().get(1).getText();
            for (int i = 2; i < expr.getChildren().size(); i++) {
                Object v = expr.getChildren().get(i).getCalculatedValue();
                if (expr.getChildren().get(i).getType() != ExpressionType.SYMBOL) {
                    result = calculateNumber(result, preSymbol, v,
                            defaultScale, defaultRoundingMode, mathContext);
                } else {
                    preSymbol = expr.getChildren().get(i).getText();
                }
            }
        }
        return result;
    }

}
