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

import java.util.List;

public class ExpressionDTO {
    private Object calculatedValue;
    private String text;
    private ExpressionType type;
    private List<ExpressionDTO> children;

    public ExpressionDTO() {
    }

    public ExpressionDTO(String text, ExpressionType type) {
        this(text, type, null);
    }

    public ExpressionDTO(String text, ExpressionType type, List<ExpressionDTO> children) {
        this(null, text, type, children);
    }

    public ExpressionDTO(Object calculatedValue, String text, ExpressionType type, List<ExpressionDTO> children) {
        this.calculatedValue = calculatedValue;
        this.text = text;
        this.type = type;
        this.children = children;
    }

    public Object getCalculatedValue() {
        return calculatedValue;
    }

    public void setCalculatedValue(Object calculatedValue) {
        this.calculatedValue = calculatedValue;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ExpressionType getType() {
        return type;
    }

    public void setType(ExpressionType type) {
        this.type = type;
    }

    public List<ExpressionDTO> getChildren() {
        return children;
    }

    public void setChildren(List<ExpressionDTO> children) {
        this.children = children;
    }
}
