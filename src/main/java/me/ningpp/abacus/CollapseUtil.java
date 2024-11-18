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

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public final class CollapseUtil {

    private CollapseUtil() {
    }

    public static List<ExpressionDTO> collapse(List<ExpressionDTO> expressions) {
        if (expressions== null) {
            return null;
        }
        List<ExpressionDTO> results = new ArrayList<>();
        for (ExpressionDTO exp : expressions) {
            results.add(collapse(exp, null));
        }
        return results;
    }

    public static ExpressionDTO collapse(ExpressionDTO exp, ExpressionDTO parentExp) {
        if (CollectionUtils.isEmpty(exp.getChildren())) {
            return exp;
        }

        int childCount = exp.getChildren().size();
        if (childCount == 1) {
            if (parentExp != null) {
                if (exp.getType() != ExpressionType.PARENTHESIS) {
                    return collapse(exp.getChildren().get(0), parentExp);
                }
            } else {
                return collapse(exp.getChildren().get(0), exp);
            }
        }

        List<ExpressionDTO> collapsedChildren = new ArrayList<>();
        for (ExpressionDTO child : exp.getChildren()) {
            collapsedChildren.add(collapse(child, exp));
        }

        if (exp.getType() == ExpressionType.PARENTHESIS
                && collapsedChildren.size() == 1) {
            if (collapsedChildren.get(0).getType() == ExpressionType.PARENTHESIS) {
                return new ExpressionDTO(collapsedChildren.get(0).getText(), exp.getType(), collapsedChildren.get(0).getChildren());
            } else if (collapsedChildren.get(0).getType() == ExpressionType.VARIABLE
                        || collapsedChildren.get(0).getType() == ExpressionType.NUMBER) {
                return new ExpressionDTO(collapsedChildren.get(0).getText(), collapsedChildren.get(0).getType(), collapsedChildren.get(0).getChildren());
            }
        }
        return new ExpressionDTO(exp.getText(), exp.getType(), collapsedChildren);
    }
}
