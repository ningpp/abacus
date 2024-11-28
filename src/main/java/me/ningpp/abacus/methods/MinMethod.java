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
package me.ningpp.abacus.methods;

import java.math.BigDecimal;

public class MinMethod implements AbacusMethod {

    @Override
    public Object execute(Object[] args) {
        if (args == null || args.length < 2) {
            throw new IllegalArgumentException("illegal argument size, min method args must >= 2");
        }
        BigDecimal[] decimals = new BigDecimal[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            decimals[i] = arg instanceof BigDecimal darg ? darg : arg == null ? null : new BigDecimal(arg.toString());
        }
        return min(decimals);
    }

    public static BigDecimal min(BigDecimal[] decimals) {
        BigDecimal min = decimals[0];
        for (int i = 1; i < decimals.length; i++) {
            BigDecimal current = decimals[i];
            if (min == null) {
                min = current;
            } else if (current != null && current.compareTo(min) < 0) {
                min = current;
            }
        }
        return min;
    }

}
