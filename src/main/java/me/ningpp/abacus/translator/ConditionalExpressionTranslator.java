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

import me.ningpp.abacus.AbacusParser.ConditionalConditionContext;
import me.ningpp.abacus.AbacusParser.ConditionalElseContext;
import me.ningpp.abacus.AbacusParser.ConditionalExpressionContext;
import me.ningpp.abacus.AbacusParser.ConditionalThenContext;
import me.ningpp.abacus.ExpressionDTO;
import me.ningpp.abacus.ExpressionType;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

public class ConditionalExpressionTranslator implements Translator {

    @Override
    public ExpressionDTO translate(ParseTree node) {
        if (! (node instanceof ConditionalExpressionContext ceCtx)) {
            return null;
        }

        ExpressionDTO dto = new ExpressionDTO();
        dto.setType(ExpressionType.CONDITIONAL);
        dto.setText(node.getText());

        ConditionalConditionContext conditionCtx = ceCtx.conditionalCondition();
        ConditionalThenContext thenCtx = ceCtx.conditionalThen();
        ConditionalElseContext elseCtx = ceCtx.conditionalElse();

        if (conditionCtx != null && thenCtx != null && elseCtx != null) {
            dto.setChildren(List.of(
                    // first  child is condition
                    TranslatorUtil.translate(ceCtx.conditionalCondition()),
                    // second child is thenExpr
                    TranslatorUtil.translate(ceCtx.conditionalThen()),
                    // third  child is elseExpr
                    TranslatorUtil.translate(ceCtx.conditionalElse())
            ));
        } else {
            return TranslatorUtil.translate(ceCtx.conditionalOrExpression());
        }
        return dto;
    }

}
