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

import me.ningpp.abacus.AbacusParser.ConditionalExpressionContext;
import me.ningpp.abacus.translator.TranslatorUtil;

import java.util.List;

public class AbacusDefaultVisitor extends AbacusParserBaseVisitor<ExpressionResultDTO> {

    @Override
    public ExpressionResultDTO visitConditionalExpression(ConditionalExpressionContext ctx) {
        if (ctx == null || ctx.children == null) {
            return null;
        }
        ExpressionResultDTO dto = new ExpressionResultDTO();
        dto.setExpressions(List.of(TranslatorUtil.translate(ctx)));
        return dto;
    }

}
