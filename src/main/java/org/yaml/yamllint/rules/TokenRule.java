/**
 * Copyright (c) 2018, Sylvain Baudoin
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
package org.yaml.yamllint.rules;

import org.yaml.yamllint.LintProblem;
import org.yaml.snakeyaml.tokens.Token;

import java.util.List;
import java.util.Map;

/**
 * Class that represents a token-oriented rule
 */
public abstract class TokenRule extends Rule {
    /**
     * Checks the passed token and returns some problems if any
     *
     * @param conf the rule configuration
     * @param token the token to be checked
     * @param prev the previous token
     * @param next the next token
     * @param nextnext the next next token
     * @param context a context map used to propagate info between rules
     * @return a list of problems. The list is empty if no problem found (it never returns {@code null})
     */
    public abstract List<LintProblem> check(Map conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context);

    @Override
    public TYPE getType() {
        return TYPE.TOKEN;
    }
}
