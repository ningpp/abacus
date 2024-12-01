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
package me.ningpp.abacus.aviator;

import com.googlecode.aviator.lexer.token.OperatorType;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorDecimal;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class AviatorDivFunction extends AbstractFunction {

    private final int scale;
    private final RoundingMode roundingMode;

    public AviatorDivFunction(int scale, RoundingMode roundingMode) {
        this.scale = scale;
        this.roundingMode = roundingMode;
    }

    @Override
    public String getName() {
        return OperatorType.DIV.getToken();
    }

    @Override
    public AviatorObject call(final Map<String, Object> env, final AviatorObject arg1,
            final AviatorObject arg2) {
        BigDecimal left;
        BigDecimal right;
        if (arg1.getAviatorType() == AviatorType.Decimal
                && arg2.getAviatorType() == AviatorType.Decimal) {
            left  = (BigDecimal) arg1.getValue(env);
            right = (BigDecimal) arg2.getValue(env);
        } else {
            left  = new BigDecimal(arg1.getValue(env).toString());
            right = new BigDecimal(arg2.getValue(env).toString());
        }
        return AviatorDecimal.valueOf(left.divide(right, scale, roundingMode));
    }

}
