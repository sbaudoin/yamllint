/**
 * Copyright (c) 2018-2023, Sylvain Baudoin
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
import org.yaml.snakeyaml.tokens.AliasToken;
import org.yaml.snakeyaml.tokens.KeyToken;
import org.yaml.snakeyaml.tokens.Token;
import org.yaml.snakeyaml.tokens.ValueToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Use this rule to control the number of spaces before and after colons ({@code :}).
 * <p>Options:</p>
 * <ul>
 *     <li>{@code max-spaces-before} defines the maximal number of spaces allowed before colons (use {@code -1} to disable).</li>
 *     <li>{@code max-spaces-after} defines the maximal number of spaces allowed after colons (use {@code -1} to disable).</li>
 * </ul>
 *
 * <p>Examples:</p>
 * <p>With <code>colons: {max-spaces-before: 0, max-spaces-after: 1}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     object:
 *       - a
 *       - b
 *     key: value
 * </pre>
 *
 * <p>With <code>colons: {max-spaces-before: 1}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     object :
 *       - a
 *       - b
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     object  :
 *       - a
 *       - b
 * </pre>
 *
 * <p>With <code>brackets: {max-spaces-after: 2}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     first:  1
 *     second: 2
 *     third:  3
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     first: 1
 *     2nd:   2
 *     third: 3
 * </pre>
 */
public class Colons extends TokenRule {
    /**
     * Name of the "max-spaces-before" option
     */
    public static final String OPTION_MAX_SPACES_BEFORE = "max-spaces-before";
    /**
     * Name of the "max-spaces-after" option
     */
    public static final String OPTION_MAX_SPACES_AFTER  = "max-spaces-after";


    /**
     * Constructor. Sets default values to rule options.
     */
    public Colons() {
        registerOption(OPTION_MAX_SPACES_BEFORE, Integer.valueOf(0));
        registerOption(OPTION_MAX_SPACES_AFTER, Integer.valueOf(1));
    }

    @Override
    public List<LintProblem> check(Map<Object, Object> conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        if (token instanceof ValueToken && !(
                prev instanceof AliasToken &&
                token.getStartMark().getPointer() - prev.getEndMark().getPointer() == 1)) {
            LintProblem problem = spacesBefore(token, prev,
                    -1,
                    (int)conf.get(OPTION_MAX_SPACES_BEFORE),
                    null,
                    "too many spaces before colon");
            if (problem != null) {
                problems.add(problem);
            }

            problem = spacesAfter(token, next,
                    -1,
                    (int)conf.get(OPTION_MAX_SPACES_AFTER),
                    null,
                    "too many spaces after colon");
            if (problem != null) {
                problems.add(problem);
            }
        }

        if (token instanceof KeyToken && isExplicitKey(token)) {
            LintProblem problem = spacesAfter(token, next,
                    -1,
                    (int)conf.get(OPTION_MAX_SPACES_AFTER),
                    null,
                    "too many spaces after question mark");
            if (problem != null) {
                problems.add(problem);
            }
        }

        return problems;
    }
}
