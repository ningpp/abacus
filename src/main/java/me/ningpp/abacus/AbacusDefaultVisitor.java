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

import me.ningpp.abacus.AbacusParser.AdditiveExpressionContext;
import me.ningpp.abacus.translator.TranslatorUtil;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

public class AbacusDefaultVisitor extends AbacusParserBaseVisitor<ExpressionResultDTO> {

    @Override
    public ExpressionResultDTO visitAdditiveExpression(AdditiveExpressionContext ctx) {
        return parseAdditiveExpression(ctx);
    }

    private ExpressionResultDTO parseAdditiveExpression(AdditiveExpressionContext ctx) {
        if (ctx == null || ctx.children == null) {
            return null;
        }
        ExpressionResultDTO dto = new ExpressionResultDTO();
        List<ParseTree> children = ctx.children;
        List<ExpressionDTO> expressions = new ArrayList<>();
        dto.setExpressions(expressions);
        for (ParseTree child : children) {
            expressions.add(TranslatorUtil.translate(child));
        }
        return dto;
    }

}
