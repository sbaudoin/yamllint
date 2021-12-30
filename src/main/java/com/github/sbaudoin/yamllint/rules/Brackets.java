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

import java.util.*;

/**
 * Use this rule to control the number of spaces inside brackets ({@code [} and {@code ]}).
 * <p>Options:</p>
 * <ul>
 *     <li>{@code forbid} is used to forbid the use of flow sequences which are denoted by
 *         surrounding brackets (<code>[</code> and <code>]</code>). Use {@code true} to forbid the use of
 *         flow sequences completely. Use {@code non-empty} to forbid the use of all flow
 *         sequences except for empty ones.</li>
 *     <li>{@code min-spaces-inside} defines the minimal number of spaces required inside brackets.</li>
 *     <li>{@code max-spaces-inside} defines the maximal number of spaces allowed inside brackets.</li>
 *     <li>{@code min-spaces-inside-empty} defines the minimal number of spaces required inside empty brackets.</li>
 *     <li>{@code max-spaces-inside-empty} defines the maximal number of spaces allowed inside empty brackets.</li>
 * </ul>
 *
 * <p>Examples:</p>
 * <p>With <code>brackets: {forbid: true}</code> the following code snippet would **PASS**:
 * <pre>
 *     object:
 *       - 1
 *       - 2
 *       - abc
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>object: [ 1, 2, abc ]</pre>
 *
 * <p>With <code>brackets: {forbid: non-empty}</code> the following code snippet would **PASS**:
 * <pre>
 *     object: []
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     object: [ 1, 2, abc ]
 * </pre>
 *
 * <p>With <code>brackets: {min-spaces-inside: 0, max-spaces-inside: 0}</code>
 * the following code snippet would **PASS**:
 * <pre>object: [1, 2, abc]</pre>
 * the following code snippet would **FAIL**:
 * <pre>object: [ 1, 2, abc ]</pre>
 *
 * <p>With <code>brackets: {min-spaces-inside: 1, max-spaces-inside: 3}</code>
 * the following code snippet would **PASS**:
 * <pre>object: [ 1, 2, abc ]</pre>
 * the following code snippet would **PASS**:
 * <pre>object: [ 1, 2, abc   ]</pre>
 * the following code snippet would **FAIL**:
 * <pre>object: [    1, 2, abc   ]</pre>
 * the following code snippet would **FAIL**:
 * <pre>object: [1, 2, abc ]</pre>
 *
 * <p>With <code>brackets: {min-spaces-inside-empty: 0, max-spaces-inside-empty: 0}</code>
 * the following code snippet would **PASS**:
 * <pre>object: []</pre>
 * the following code snippet would **FAIL**:
 * <pre>object: [ ]</pre>
 *
 * <p>With <code>brackets: {min-spaces-inside-empty: 1, max-spaces-inside-empty: -1}</code>
 * the following code snippet would **PASS**:
 * <pre>object: [         ]</pre>
 * the following code snippet would **FAIL**:
 * <pre>object: []</pre>
 */
public class Brackets extends TokenRule {
    public static final String OPTION_FORBID                  = "forbid";
    public static final String OPTION_MIN_SPACES_INSIDE       = "min-spaces-inside";
    public static final String OPTION_MAX_SPACES_INSIDE       = "max-spaces-inside";
    public static final String OPTION_MIN_SPACES_INSIDE_EMPTY = "min-spaces-inside-empty";
    public static final String OPTION_MAX_SPACES_INSIDE_EMPTY = "max-spaces-inside-empty";


    public Brackets() {
        registerOption(OPTION_FORBID, Arrays.asList(Boolean.class, "non-empty"), false);
        registerOption(OPTION_MIN_SPACES_INSIDE, 0);
        registerOption(OPTION_MAX_SPACES_INSIDE, 0);
        registerOption(OPTION_MIN_SPACES_INSIDE_EMPTY, -1);
        registerOption(OPTION_MAX_SPACES_INSIDE_EMPTY, -1);
    }

    @Override
    public List<LintProblem> check(Map<Object, Object> conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        if ((Boolean.TRUE.equals(conf.get(OPTION_FORBID)) && token instanceof FlowSequenceStartToken) ||
                ("non-empty".equals(conf.get(OPTION_FORBID)) && token instanceof FlowSequenceStartToken && !(next instanceof FlowSequenceEndToken))) {
            return Collections.singletonList(
                    new LintProblem(
                            token.getStartMark().getLine() + 1,
                            token.getEndMark().getColumn() + 1,
                            "forbidden flow sequence"
                    ));
        }

        LintProblem problem = null;

        if (token instanceof FlowSequenceStartToken && next instanceof FlowSequenceEndToken) {
            problem = spacesAfter(token, next,
                    (int)((conf.get(OPTION_MIN_SPACES_INSIDE_EMPTY).equals(-1))?conf.get(OPTION_MIN_SPACES_INSIDE):conf.get(OPTION_MIN_SPACES_INSIDE_EMPTY)),
                    (int)((conf.get(OPTION_MAX_SPACES_INSIDE_EMPTY).equals(-1))?conf.get(OPTION_MAX_SPACES_INSIDE):conf.get(OPTION_MAX_SPACES_INSIDE_EMPTY)),
                    "too few spaces inside empty brackets",
                    "too many spaces inside empty brackets");
        } else if (token instanceof FlowSequenceStartToken) {
            problem = spacesAfter(token, next,
                    (int)conf.get(OPTION_MIN_SPACES_INSIDE),
                    (int)conf.get(OPTION_MAX_SPACES_INSIDE),
                    "too few spaces inside brackets",
                    "too many spaces inside brackets");
        } else if (token instanceof FlowSequenceEndToken && !(prev instanceof FlowSequenceStartToken)) {
            problem = spacesBefore(token, prev,
                    (int)conf.get(OPTION_MIN_SPACES_INSIDE),
                    (int)conf.get(OPTION_MAX_SPACES_INSIDE),
                    "too few spaces inside brackets",
                    "too many spaces inside brackets");
        }

        List<LintProblem> problems = new ArrayList<>();
        if (problem != null) {
            problems.add(problem);
        }
        return problems;
    }
}
