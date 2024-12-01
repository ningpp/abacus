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

import me.ningpp.abacus.ExpressionDTO;
import me.ningpp.abacus.ExpressionType;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.collections4.CollectionUtils;

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
        TRANSLATORS.add(new StringLiteralTranslator());
        TRANSLATORS.add(new MultiplicativeTranslator());
        TRANSLATORS.add(new AdditiveTranslator());
        TRANSLATORS.add(new UnaryTranslator());
        TRANSLATORS.add(new PrimaryTranslator());
        TRANSLATORS.add(new ArithmeticTranslator());
        TRANSLATORS.add(new ParenthesisTranslator());
        TRANSLATORS.add(new ConditionalExpressionTranslator());
        TRANSLATORS.add(new ConditionalConditionTranslator());
        TRANSLATORS.add(new ConditionalThenTranslator());
        TRANSLATORS.add(new ConditionalElseTranslator());
        TRANSLATORS.add(new ConditionalOrExpressionTranslator());
        TRANSLATORS.add(new ConditionalAndExpressionTranslator());
        TRANSLATORS.add(new EqualityExpressionTranslator());
        TRANSLATORS.add(new RelationalExpressionTranslator());
        TRANSLATORS.add(new ExpressionTranslator());
        TRANSLATORS.add(new InvocationExpressionTranslator());
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

        if (!ROOT_EXPRESSION_TYPES.contains(result.getType())
                && CollectionUtils.isEmpty(result.getChildren())) {
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
            ExpressionType.STRING_LITERAL,
            ExpressionType.METHOD_INVOCATION,
            ExpressionType.CONDITIONAL,
            ExpressionType.CONDITIONAL_CONDITION,
            ExpressionType.CONDITIONAL_THEN,
            ExpressionType.CONDITIONAL_ELSE,
            ExpressionType.CONDITIONAL_OR,
            ExpressionType.CONDITIONAL_AND,
            ExpressionType.RELATIONAL,

            ExpressionType.VARIABLE,
            ExpressionType.NUMBER,
            ExpressionType.SYMBOL
    ));

}
