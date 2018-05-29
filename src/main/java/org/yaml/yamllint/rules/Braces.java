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
import org.yaml.snakeyaml.tokens.FlowMappingEndToken;
import org.yaml.snakeyaml.tokens.FlowMappingStartToken;
import org.yaml.snakeyaml.tokens.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Use this rule to control the number of spaces inside braces (<code>{</code> and <code>}</code>).
 * <p>Options:</p>
 * <ul>
 *     <li>{@code min-spaces-inside} defines the minimal number of spaces required inside braces.</li>
 *     <li>{@code max-spaces-inside} defines the maximal number of spaces allowed inside braces.</li>
 *     <li>{@code min-spaces-inside-empty} defines the minimal number of spaces required inside empty braces.</li>
 *     <li>{@code max-spaces-inside-empty} defines the maximal number of spaces allowed inside empty braces.</li>
 * </ul>
 *
 * <p>Examples:</p>
 * <p>With <code>braces: {min-spaces-inside: 0, max-spaces-inside: 0}</code>
 * the following code snippet would **PASS**:
 * <pre>object: {key1: 4, key2: 8}</pre>
 * the following code snippet would **FAIL**:
 * <pre>object: { key1: 4, key2: 8 }</pre>
 *
 * <p>With <code>braces: {min-spaces-inside: 1, max-spaces-inside: 3}</code>
 * the following code snippet would **PASS**:
 * <pre>object: { key1: 4, key2: 8 }</pre>
 * the following code snippet would **PASS**:
 * <pre>object: { key1: 4, key2: 8   }</pre>
 * the following code snippet would **FAIL**:
 * <pre>object: {    key1: 4, key2: 8   }</pre>
 * the following code snippet would **FAIL**:
 * <pre>object: {key1: 4, key2: 8 }</pre>
 *
 * <p>With <code>braces: {min-spaces-inside-empty: 0, max-spaces-inside-empty: 0}</code>
 * the following code snippet would **PASS**:
 * <pre>object: {}</pre>
 * the following code snippet would **FAIL**:
 * <pre>object: { }</pre>
 *
 * <p>With <code>braces: {min-spaces-inside-empty: 1, max-spaces-inside-empty: -1}</code>
 * the following code snippet would **PASS**:
 * <pre>object: {         }</pre>
 * the following code snippet would **FAIL**:
 * <pre>object: {}</pre>
 */
public class Braces extends TokenRule {
    public static final String OPTION_MIN_SPACES_INSIDE       = "min-spaces-inside";
    public static final String OPTION_MAX_SPACES_INSIDE       = "max-spaces-inside";
    public static final String OPTION_MIN_SPACES_INSIDE_EMPTY = "min-spaces-inside-empty";
    public static final String OPTION_MAX_SPACES_INSIDE_EMPTY = "max-spaces-inside-empty";


    public Braces() {
        options.put(OPTION_MIN_SPACES_INSIDE, Integer.class);
        options.put(OPTION_MAX_SPACES_INSIDE, Integer.class);
        options.put(OPTION_MIN_SPACES_INSIDE_EMPTY, Integer.class);
        options.put(OPTION_MAX_SPACES_INSIDE_EMPTY, Integer.class);
    }

    @Override
    public List<LintProblem> check(Map conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        if (token instanceof FlowMappingStartToken && next instanceof FlowMappingEndToken) {
            LintProblem problem = spacesAfter(token, next,
                    (int)((((int)conf.get(OPTION_MIN_SPACES_INSIDE_EMPTY)) != -1)?conf.get(OPTION_MIN_SPACES_INSIDE_EMPTY):conf.get(OPTION_MIN_SPACES_INSIDE)),
                    (int)((((int)conf.get(OPTION_MAX_SPACES_INSIDE_EMPTY)) != -1)?conf.get(OPTION_MAX_SPACES_INSIDE_EMPTY):conf.get(OPTION_MAX_SPACES_INSIDE)),
                    "too few spaces inside empty braces",
                    "too many spaces inside empty braces");
            if (problem != null) {
                problems.add(problem);
            }
        } else if (token instanceof FlowMappingStartToken) {
            LintProblem problem = spacesAfter(token, next,
                    (int)conf.get(OPTION_MIN_SPACES_INSIDE),
                    (int)conf.get(OPTION_MAX_SPACES_INSIDE),
                    "too few spaces inside braces",
                    "too many spaces inside braces");
            if (problem != null) {
                problems.add(problem);
            }
        } else if (token instanceof FlowMappingEndToken && (prev == null || !(prev instanceof FlowMappingStartToken))) {
            LintProblem problem = spacesBefore(token, prev,
                    (int)conf.get(OPTION_MIN_SPACES_INSIDE),
                    (int)conf.get(OPTION_MAX_SPACES_INSIDE),
                    "too few spaces inside braces",
                    "too many spaces inside braces");
            if (problem != null) {
                problems.add(problem);
            }
        }

        return problems;
    }
}
