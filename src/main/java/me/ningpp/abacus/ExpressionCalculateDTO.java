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

import java.math.BigDecimal;
import java.util.List;

public class ExpressionCalculateDTO {
    private BigDecimal result;
    private boolean calculated;
    private ExpressionDTO expression;
    private List<ExpressionCalculateDTO> children;

    public boolean isCalculated() {
        return calculated;
    }

    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
    }

    public ExpressionDTO getExpression() {
        return expression;
    }

    public void setExpression(ExpressionDTO expression) {
        this.expression = expression;
    }

    public BigDecimal getResult() {
        return result;
    }

    public void setResult(BigDecimal result) {
        this.result = result;
    }

    public List<ExpressionCalculateDTO> getChildren() {
        return children;
    }

    public void setChildren(List<ExpressionCalculateDTO> children) {
        this.children = children;
    }
}
