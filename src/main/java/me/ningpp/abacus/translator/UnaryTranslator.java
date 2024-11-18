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

import me.ningpp.abacus.AbacusParser.UnaryExpressionContext;
import me.ningpp.abacus.ExpressionDTO;
import me.ningpp.abacus.ExpressionType;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

public class UnaryTranslator implements Translator {

    @Override
    public ExpressionDTO translate(ParseTree node) {
        if (! (node instanceof UnaryExpressionContext)) {
            return null;
        }
        UnaryExpressionContext ueCtx = (UnaryExpressionContext) node;
        int childCount = node.getChildCount();
        ExpressionDTO dto = new ExpressionDTO();
        dto.setType(ExpressionType.UNARY);
        dto.setText(node.getText());
        List<ExpressionDTO> children = new ArrayList<>(childCount);
        dto.setChildren(children);
        for (int i = 0; i < childCount; i++) {
            children.add(TranslatorUtil.translate(ueCtx.children.get(i)));
        }
        return dto;
    }
}
