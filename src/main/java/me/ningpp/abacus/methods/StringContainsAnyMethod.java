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
package me.ningpp.abacus.methods;

public class StringContainsAnyMethod implements AbacusMethod {

    @Override
    public Object execute(Object[] args) {
        if (args == null || args.length < 2) {
            throw new IllegalArgumentException("illegal argument size, StringContainsAny method args must >= 2");
        }
        if (args[0] == null) {
            return Boolean.FALSE;
        }
        String container = args[0].toString();
        int size = args.length;
        for (int i = 1; i < size; i++) {
            Object value2Search = args[i];
            if (value2Search != null && container.contains(value2Search.toString())) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

}
