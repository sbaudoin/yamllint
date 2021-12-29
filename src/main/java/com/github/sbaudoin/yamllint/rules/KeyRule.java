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
import java.util.Optional;

/**
 * Abstract rule used to factorize code for rules processing keys
 * ({@link KeyDuplicates} and {@link KeyOrdering})
 */
public abstract class KeyRule extends TokenRule {
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
                checkKey(conf, token, prev, next, nextnext, context, stack).ifPresent(problems::add);
            }
        }

        return problems;
    }


    /**
     * Method that does the actual check
     *
     * @param conf the rule configuration
     * @param token the token to be checked
     * @param prev the previous token
     * @param next the next token
     * @param nextnext the next next token
     * @param context a context map used to propagate info between rules
     * @param stack a list of all keys found in the YAML document
     */
    protected abstract Optional<LintProblem> checkKey(Map<Object, Object> conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context, List<Parent> stack);


    private enum TYPE { MAP, SEQ }

    protected class Parent {
        TYPE type;
        List<String> keys;

        public Parent(TYPE type) {
            this.type = type;
            this.keys = new ArrayList<>();
        }
    }
}
