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

import me.ningpp.abacus.AbacusParser.ConditionalAndExpressionContext;
import me.ningpp.abacus.ExpressionDTO;
import me.ningpp.abacus.ExpressionType;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConditionalAndExpressionTranslator implements Translator {

    @Override
    public ExpressionDTO translate(ParseTree node) {
        if (!(node instanceof ConditionalAndExpressionContext caeCtx)) {
            return null;
        }

        ExpressionDTO dto = new ExpressionDTO();
        dto.setType(ExpressionType.CONDITIONAL_AND);
        dto.setText(node.getText());

        List<ExpressionDTO> children = new ArrayList<>();
        children.add(TranslatorUtil.translate(caeCtx.equalityExpression()));
        children.add(TranslatorUtil.translate(caeCtx.OP_AND()));
        children.add(TranslatorUtil.translate(caeCtx.conditionalAndExpression()));

        dto.setChildren(children.stream().filter(Objects::nonNull).toList());
        return dto;
    }

}
