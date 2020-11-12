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
import org.yaml.snakeyaml.tokens.FlowEntryToken;
import org.yaml.snakeyaml.tokens.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Use this rule to control the number of spaces before and after commas ({@code ,}).
 * <p>Options:</p>
 * <ul>
 *     <li>{@code max-spaces-before} defines the maximal number of spaces allowed before commas (use -1 to disable).</li>
 *     <li>{@code min-spaces-after} defines the minimal number of spaces after commas.</li>
 *     <li>{@code max-spaces-after} defines the maximal number of spaces allowed after commas (use -1 to disable).</li>
 * </ul>
 *
 * <p>Examples:</p>
 * <p>With <code>commas: {max-spaces-before: 0}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     strange var:
 *       [10, 20, 30, {x: 1, y: 2}]
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     strange var:
 *       [10, 20 , 30, {x: 1, y: 2}]
 * </pre>
 *
 * <p>With <code>commas: {max-spaces-before: 2}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     strange var:
 *       [10  , 20 , 30,  {x: 1  , y: 2}]
 * </pre>
 *
 * <p>With <code>commas: {max-spaces-before: -1}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     strange var:
 *       [10,
 *        20   , 30
 *        ,   {x: 1, y: 2}]
 * </pre>
 *
 * <p>With <code>commas: {min-spaces-after: 1, max-spaces-after: 1}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     strange var:
 *       [10, 20,30, {x: 1, y: 2}]
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     strange var:
 *       [10, 20,30,   {x: 1,   y: 2}]
 * </pre>
 *
 * <p>With <code>commas: {min-spaces-after: 1, max-spaces-after: 3}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     strange var:
 *       [10, 20,  30,  {x: 1,   y: 2}]
 * </pre>
 *
 * <p>With <code>commas: {min-spaces-after: 0, max-spaces-after: 1}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     strange var:
 *       [10, 20,30, {x: 1, y: 2}]
 * </pre>
 */
public class Commas extends TokenRule {
    public static final String OPTION_MAX_SPACES_BEFORE = "max-spaces-before";
    public static final String OPTION_MIN_SPACES_AFTER  = "min-spaces-after";
    public static final String OPTION_MAX_SPACES_AFTER  = "max-spaces-after";


    public Commas() {
        registerOption(OPTION_MAX_SPACES_BEFORE, 0);
        registerOption(OPTION_MIN_SPACES_AFTER, 1);
        registerOption(OPTION_MAX_SPACES_AFTER, 1);
    }

    @Override
    public List<LintProblem> check(Map<Object, Object> conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        if (token instanceof FlowEntryToken) {
            if (prev != null && (int)conf.get(OPTION_MAX_SPACES_BEFORE) != -1 && prev.getEndMark().getLine() < token.getStartMark().getLine()) {
                problems.add(new LintProblem(token.getStartMark().getLine() + 1,
                        Math.max(1, token.getStartMark().getColumn()),
                        "too many spaces before comma"));
            } else {
                LintProblem problem = spacesBefore(token, prev,
                        -1,
                        (int)conf.get(OPTION_MAX_SPACES_BEFORE),
                        null,
                        "too many spaces before comma");
                if (problem != null) {
                    problems.add(problem);
                }
            }

            LintProblem problem = spacesAfter(token, next,
                    (int)conf.get(OPTION_MIN_SPACES_AFTER),
                    (int)conf.get(OPTION_MAX_SPACES_AFTER),
                    "too few spaces after comma",
                    "too many spaces after comma");
            if (problem != null) {
                problems.add(problem);
            }
        }

        return problems;
    }
}
