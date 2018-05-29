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
import org.yaml.snakeyaml.tokens.ScalarToken;
import org.yaml.snakeyaml.tokens.TagToken;
import org.yaml.snakeyaml.tokens.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Use this rule to forbid non-explictly typed truthy values other than {@code true}
 * and {@code false}, for example {@code YES}, {@code False} and {@code off}.
 * <p>This can be useful to prevent surprises from YAML parsers transforming
 * {@code [yes, FALSE, Off]} into {@code [true, false, false]} or
 * {@code {y: 1, yes: 2, on: 3, true: 4, True: 5}} into {@code {y: 1, true: 5}}.</p>
 *
 * <p>Examples:</p>
 * <p>With <code>truthy: {}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     boolean: true
 *
 *     object: {"True": 1, 1: "True"}
 *
 *     "yes":  1
 *     "on":   2
 *     "True": 3
 *
 *     explicit:
 *       string1: !!str True
 *       string2: !!str yes
 *       string3: !!str off
 *       encoded: !!binary |
 *                  True
 *                  OFF
 *                  pad==  # this decodes as 'N\xbb\x9e8Qii'
 *     boolean1: !!bool true
 *     boolean2: !!bool "false"
 *     boolean3: !!bool FALSE
 *     boolean4: !!bool True
 *     boolean5: !!bool off
 *     boolean6: !!bool NO
 * </pre>
 * the following code snippets would **FAIL**:
 * <pre>
 *     object: {True: 1, 1: True}
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     yes:  1
 *     on:   2
 *     True: 3
 * </pre>
 */
public class Truthy extends TokenRule {
    private static final List<String> TRUTHY_TOKENS = Arrays.asList((String[])new String[] {
            "YES", "Yes", "yes",
            "NO", "No", "no",
            "TRUE", "True",  // "true" is a boolean
            "FALSE", "False",  // "false" is a boolean
            "ON", "On", "on",
            "OFF", "Off", "off"
    });


    @Override
    public List<LintProblem> check(Map conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        if (prev != null && prev instanceof TagToken) {
            return problems;
        }

        if (token instanceof ScalarToken) {
            if (TRUTHY_TOKENS.stream().anyMatch(truthy -> truthy.equals(((ScalarToken)token).getValue())) &&
                    ((ScalarToken)token).getStyle().getChar() == null){
                problems.add(new LintProblem(token.getStartMark().getLine() + 1,
                        token.getStartMark().getColumn() + 1,
                        "truthy value should be true or false"));
            }
        }

        return problems;
    }
}
