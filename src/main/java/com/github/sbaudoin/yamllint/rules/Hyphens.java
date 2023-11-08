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
import org.yaml.snakeyaml.tokens.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Use this rule to control the number of spaces after hyphens ({@code -}).
 * <p>Options:</p>
 * <ul>
 *     <li>{@code max-spaces-after} defines the maximal number of spaces allowed after hyphens.
 *         Set to a negative integer if you want to allow any number of spaces.</li>
 *     <li>{@code min-spaces-after} defines the minimal number of spaces expected after hyphens.
 *         Set to a negative integer if you want to allow any number of spaces. When set to a
 *         positive value, cannot be greater than {@code max-spaces-after}.</li>
 *     <li>YAMLLint will consider "-xx" as a scalar. However you may consider that, in your context,
 *         such a syntax is a typo and is actually a sequence and as a consequence there should be
 *         a space after the hyphen. As this is not a standard behaviour, you explicitly need to
 *         enable this control by setting the option {@code check-scalars} to {@code true}.
 *         <strong>Use with caution</strong> as all scalars will be checked and non-solvable false
 *         positive might be identified. Has no effect when set to {@code true} but
 *         {@code min-spaces-after} is disabled (< 0).</li>
 * </ul>
 *
 * <p>Default values (when enabled):</p>
 * <pre>
 *     rules:
 *       hyphens:
 *         max-spaces-after: 1
 *         min-spaces-after: -1  # Disabled
 *         check-scalars: false
 * </pre>
 *
 * <p>Examples:</p>
 * <p>With <code>hyphens: {max-spaces-after: 1}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     - first list:
 *         - a
 *         - b
 *     - - 1
 *       - 2
 *       - 3
 * </pre>
 * the following code snippets would **FAIL**:
 * <pre>
 *     -  first list:
 *          - a
 *          - b
 * </pre>
 *
 * <p>With <code>hyphens: {max-spaces-after: 3}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     -   key
 *     -  key2
 *     - key42
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     -    key
 *     -   key2
 *     -  key42
 * </pre>
 *
 * <p>With <code>hyphens: {min-spaces-after: 3}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     list:
 *     -   key
 *     -    key2
 *     -     key42
 *     -foo:  # starter of a new sequence named "-foo"; without the colon, a syntax error will be raised.
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     -  key
 *     -   key2
 *     -  key42
 * </pre>
 *
 * <p>With <code>hyphens: {min-spaces-after: 3, check-scalars: true}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     list:
 *     -   key
 *     -    key2
 *     -     key42
 *     key: -value
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     ---
 *     -item
 * </pre>
 * or
 * <pre>
 *     sequence:
 *       -key  # Mind the spaces before the hyphen to enforce the sequence and avoid a syntax error
 * </pre>
 */
public class Hyphens extends TokenRule {
    public static final String OPTION_MAX_SPACES_AFTER = "max-spaces-after";
    public static final String OPTION_MIN_SPACES_AFTER = "min-spaces-after";
    public static final String OPTION_CHECK_SCALARS    = "check-scalars";


    public Hyphens() {
        registerOption(OPTION_MAX_SPACES_AFTER, 1);
        registerOption(OPTION_MIN_SPACES_AFTER, -1);
        registerOption(OPTION_CHECK_SCALARS, false);
    }

    @Override
    public String validate(Map<String, Object> conf) {
        int maxSpaces = (int) conf.get(OPTION_MAX_SPACES_AFTER);
        int minSpaces = (int) conf.get(OPTION_MIN_SPACES_AFTER);

        if (maxSpaces == 0) {
            return String.format("\"%s\" cannot be set to 0", OPTION_MAX_SPACES_AFTER);
        }

        if (minSpaces > 0 && minSpaces > maxSpaces) {
            return String.format("\"%s\" cannot be greater than \"%s\"", OPTION_MIN_SPACES_AFTER, OPTION_MAX_SPACES_AFTER);
        }
        return null;
    }

    @Override
    public List<LintProblem> check(Map<Object, Object> conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        if (token instanceof BlockEntryToken) {
            if ((Integer)conf.get(OPTION_MAX_SPACES_AFTER) > 0) {
                LintProblem problem = spacesAfter(token, next,
                        -1,
                        (int) conf.get(OPTION_MAX_SPACES_AFTER),
                        null,
                        "too many spaces after hyphen");
                if (problem != null) {
                    problems.add(problem);
                }
            }

            if ((Integer)conf.get(OPTION_MIN_SPACES_AFTER) > 0) {
                LintProblem problem = spacesAfter(token, next,
                        (int) conf.get(OPTION_MIN_SPACES_AFTER),
                        -1,
                        "too few spaces after hyphen",
                        null);
                if (problem != null) {
                    problems.add(problem);
                }
            }
        }

        if (Boolean.TRUE.equals(conf.get(OPTION_CHECK_SCALARS)) && (Integer)conf.get(OPTION_MIN_SPACES_AFTER) > 0 && token instanceof ScalarToken) {
            // Token identified as a scalar so there is no space after the hyphen: no need to count
            if (((ScalarToken) token).getValue().startsWith("-")) {
                problems.add(new LintProblem(token.getStartMark().getLine() + 1,
                        token.getStartMark().getColumn() + 1, "too few spaces after hyphen"));
            }
        }

        return problems;
    }
}
