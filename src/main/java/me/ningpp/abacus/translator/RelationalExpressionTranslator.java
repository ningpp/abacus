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
package me.ningpp.abacus.translator;

import me.ningpp.abacus.AbacusParser.RelationalExpressionContext;
import me.ningpp.abacus.ExpressionDTO;
import me.ningpp.abacus.ExpressionType;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RelationalExpressionTranslator implements Translator {

    @Override
    public ExpressionDTO translate(ParseTree node) {
        if (!(node instanceof RelationalExpressionContext reCtx)) {
            return null;
        }

        ExpressionDTO dto = new ExpressionDTO();
        dto.setType(ExpressionType.RELATIONAL);
        dto.setText(node.getText());
        TerminalNode symbol = reCtx.LT();
        if (symbol == null) {
            symbol = reCtx.GT();
        }
        if (symbol == null) {
            symbol = reCtx.LE();
        }
        if (symbol == null) {
            symbol = reCtx.GE();
        }

        List<ExpressionDTO> children = new ArrayList<>();
        children.add(TranslatorUtil.translate(reCtx.relationalExpression()));
        children.add(TranslatorUtil.translate(symbol));
        children.add(TranslatorUtil.translate(reCtx.additiveExpression()));

        dto.setChildren(children.stream().filter(Objects::nonNull).toList());
        return dto;
    }

}
