/**
 * Copyright (c) 2018-2020, Sylvain Baudoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.sbaudoin.yamllint.rules;

import com.github.sbaudoin.yamllint.LintProblem;
import org.yaml.snakeyaml.tokens.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Use this rule to enforce alphabetical ordering of keys in mappings. The sorting
 * order uses the Unicode code point number. As a result, the ordering is
 * case-sensitive and not accent-friendly (see examples below).
 *
 * <p>Examples:</p>
 * <p>With <code>key-ordering: {}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     - key 1: v
 *       key 2: val
 *       key 3: value
 *     - {a: 1, b: 2, c: 3}
 *     - T-shirt: 1
 *       T-shirts: 2
 *       t-shirt: 3
 *       t-shirts: 4
 *     - hair: true
 *       hais: true
 *       haïr: true
 *       haïssable: true
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     - key 2: v
 *       key 1: val
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>- {b: 1, a: 2}</pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     - T-shirt: 1
 *       t-shirt: 2
 *       T-shirts: 3
 *       t-shirts: 4
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     - haïr: true
 *       hais: true
 * </pre>
 */
public class KeyOrdering extends TokenRule {
    private static final String STACK_KEY = "stack";


    @Override
    public List<LintProblem> check(Map<Object, Object> conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        context.putIfAbsent(STACK_KEY, new ArrayList<Parent>());

        List<Parent> stack = (List<Parent>)context.get(STACK_KEY);

        if (token instanceof BlockMappingStartToken || token instanceof FlowMappingStartToken) {
            stack.add(new Parent(TYPE.MAP));
        } else if (token instanceof BlockSequenceStartToken || token instanceof FlowSequenceStartToken) {
            stack.add(new Parent(TYPE.SEQ));
        } else if (token instanceof BlockEndToken || token instanceof FlowMappingEndToken || token instanceof FlowSequenceEndToken) {
            stack.remove(stack.size() - 1);
        } else if (token instanceof KeyToken && next instanceof ScalarToken) {
            // This check is done because KeyTokens can be found inside flow
            // sequences... strange, but allowed.
            if (!stack.isEmpty() && stack.get(stack.size() - 1).type == TYPE.MAP) {
                if (stack.get(stack.size() - 1).keys.stream().anyMatch(key -> key.compareTo(((ScalarToken)next).getValue()) > 0)) {
                    problems.add(new LintProblem(next.getStartMark().getLine() + 1, next.getStartMark().getColumn() + 1,
                            "wrong ordering of key " + ((ScalarToken)next).getValue() + " in mapping"));
                } else {
                    stack.get(stack.size() - 1).keys.add(((ScalarToken)next).getValue());
                }
            }
        }

        return problems;
    }


    private enum TYPE { MAP, SEQ }

    private class Parent {
        TYPE type;
        List<String> keys;

        public Parent(TYPE type) {
            this.type = type;
            this.keys = new ArrayList<>();
        }
    }
}
