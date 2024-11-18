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
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbacusUtilTest {

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

}
