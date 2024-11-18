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

import me.ningpp.abacus.AbacusParser.MultiplicativeExpressionContext;
import me.ningpp.abacus.AbacusParser.PrimaryExpressionContext;
import me.ningpp.abacus.AbacusParser.ScientificContext;
import me.ningpp.abacus.AbacusParser.UnaryExpressionContext;
import me.ningpp.abacus.AbacusParser.VariableContext;
import me.ningpp.abacus.ExpressionDTO;
import me.ningpp.abacus.ExpressionType;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TranslatorUtil {

    private TranslatorUtil() {
    }

    private static final List<Translator> TRANSLATORS = new ArrayList<>();

    static {
        TRANSLATORS.add(new ArithmeticOperatorTranslator());
        TRANSLATORS.add(new TerminalNodeImplTranslator());
        TRANSLATORS.add(new ScientificTranslator());
        TRANSLATORS.add(new VariableTranslator());
        TRANSLATORS.add(new MultiplicativeTranslator());
        TRANSLATORS.add(new AdditiveTranslator());
        TRANSLATORS.add(new UnaryTranslator());
        TRANSLATORS.add(new PrimaryTranslator());
        TRANSLATORS.add(new ArithmeticTranslator());
        TRANSLATORS.add(new ParenthesisTranslator());
    }

    public static ExpressionDTO translate(ParseTree node) {
        if (node == null) {
            return null;
        }
        ExpressionDTO result = null;
        for (Translator translator : TRANSLATORS) {
            result = translator.translate(node);
            if (result != null) {
                break;
            }
        }

        if (result == null) {
            throw new IllegalStateException(node.getText() + "\t\t\t" + node.getClass().getSimpleName());
        }

        if (CollectionUtils.isEmpty(result.getChildren())
                && !ROOT_EXPRESSION_TYPES.contains(result.getType())) {
            int childCount = node.getChildCount();
            List<ExpressionDTO> children = new ArrayList<>(childCount);
            result.setChildren(children);
            for (int i = 0; i < childCount; i++) {
                ParseTree child = node.getChild(i);
                children.add(translate(child));
            }
        }
        return result;
    }

    private static final Set<ExpressionType> ROOT_EXPRESSION_TYPES = new HashSet<>(Arrays.asList(
            ExpressionType.VARIABLE,
            ExpressionType.NUMBER,
            ExpressionType.SYMBOL
    ));

    public static Pair<ScientificContext, VariableContext> onlyContainScientificAndVariable(ParseTree parseTree) {
        if (parseTree instanceof MultiplicativeExpressionContext) {
            return onlyContainScientificAndVariable((MultiplicativeExpressionContext) parseTree);
        } else if (parseTree instanceof UnaryExpressionContext) {
            return onlyContainScientificAndVariable((UnaryExpressionContext) parseTree);
        } else if (parseTree instanceof PrimaryExpressionContext) {
            return onlyContainScientificAndVariable((PrimaryExpressionContext) parseTree);
        } else {
            return null;
        }
    }

    private static Pair<ScientificContext, VariableContext> onlyContainScientificAndVariable(MultiplicativeExpressionContext meCtx) {
        if (meCtx == null || meCtx.children == null || meCtx.getChildCount() > 1) {
            return null;
        } else {
            return onlyContainScientificAndVariable((UnaryExpressionContext) meCtx.children.get(0));
        }
    }

    private static Pair<ScientificContext, VariableContext> onlyContainScientificAndVariable(UnaryExpressionContext ueCtx) {
        if (ueCtx == null) {
            return null;
        } else {
            return onlyContainScientificAndVariable(ueCtx.primaryExpression());
        }
    }

    private static Pair<ScientificContext, VariableContext> onlyContainScientificAndVariable(PrimaryExpressionContext peCtx) {
        if (peCtx != null && peCtx.parenthesisExpression() == null) {
            if (peCtx.scientific() != null || peCtx.variable() != null) {
                return Pair.of(peCtx.scientific(), peCtx.variable());
            }
        }
        return null;
    }

}
