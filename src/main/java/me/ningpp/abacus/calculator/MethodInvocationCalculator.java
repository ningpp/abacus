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

import me.ningpp.abacus.AbacusUtil;
import me.ningpp.abacus.ExpressionDTO;
import me.ningpp.abacus.exception.MethodNotFoundException;
import me.ningpp.abacus.methods.AbacusMethod;
import me.ningpp.abacus.methods.MaxMethod;
import me.ningpp.abacus.methods.MinMethod;
import me.ningpp.abacus.methods.StringContainsAnyMethod;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class MethodInvocationCalculator implements Calculator {

    private static final Map<String, AbacusMethod> METHODS = new HashMap<>();

    static {
        METHODS.put("max", new MaxMethod());
        METHODS.put("min", new MinMethod());

        METHODS.put("stringContainsAny", new StringContainsAnyMethod());
    }

    @Override
    public Object calculate(ExpressionDTO expr, Map<String, Object> context, int defaultScale, RoundingMode defaultRoundingMode, MathContext mathContext) {
        String methodName = expr.getChildren().get(0).getText();
        AbacusMethod abacusMethod = METHODS.get(methodName);
        if (abacusMethod == null) {
            throw new MethodNotFoundException("should register method before use it, method name is " + methodName);
        }
        Object[] args = null;
        int childCount = expr.getChildren().size();
        int argCount = expr.getChildren().size() - 1;
        if (argCount > 0) {
            args = new Object[argCount];
            for (int i = 1; i < childCount; i++) {
                args[i-1] = expr.getChildren().get(i).getCalculatedValue();
            }
        }
        return abacusMethod.execute(args);
    }

}
