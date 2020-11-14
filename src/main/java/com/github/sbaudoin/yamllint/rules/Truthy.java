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
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.tokens.KeyToken;
import org.yaml.snakeyaml.tokens.ScalarToken;
import org.yaml.snakeyaml.tokens.TagToken;
import org.yaml.snakeyaml.tokens.Token;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Use this rule to forbid non-explicitly typed truthy values other than allowed ones
 * (by default: {@code true} and {@code false}), for example {@code YES} or {@code off}.
 * <p>This can be useful to prevent surprises from YAML parsers transforming
 * {@code [yes, FALSE, Off]} into {@code [true, false, false]} or
 * {@code {y: 1, yes: 2, on: 3, true: 4, True: 5}} into {@code {y: 1, true: 5}}.</p>
 *
 * <p>Options:</p>
 * <ul>
 *     <li>{@code allowed-values} defines the list of truthy values which will be ignored
 *         during linting. The default is {@code ['true', 'false']}, but can be changed to
 *         any list containing: {@code 'TRUE'}, {@code 'True'},  {@code 'true'}, {@code 'FALSE'},
 *         {@code 'False'}, {@code 'false'}, {@code 'YES'}, {@code 'Yes'}, {@code 'yes'}, {@code 'NO'},
 *         {@code 'No'}, {@code 'no'}, {@code 'ON'}, {@code 'On'}, {@code 'on'}, {@code 'OFF'}, {@code 'Off'},
 *         {@code 'off'}.</li>
 *     <li>{@code check-keys} disables verification for keys in mappings. By default,
 *         {@code truthy} rule applies to both keys and values. Set this option to {@code false}
 *         to prevent this.</li>
 * </ul>
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
 *
 * <p>With <code>truthy: {allowed-values: ["yes", "no"]}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     - yes
 *     - no
 *     - "true"
 *     - 'false'
 *     - foo
 *     - bar
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     - true
 *     - false
 *     - on
 *     - off
 * </pre>
 *
 * <p>With <code>truthy: {check-keys: false}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     yes:  1
 *     on:   2
 *     true: 3
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     yes:  Yes
 *     on:   On
 *     true: True
 * </pre>
 */
public class Truthy extends TokenRule {
    private static final List<String> TRUTHY_TOKENS = Arrays.asList("YES", "Yes", "yes",
            "NO", "No", "no",
            "TRUE", "True", "true",
            "FALSE", "False", "false",
            "ON", "On", "on",
            "OFF", "Off", "off");


    public static final String OPTION_ALLOWED_VALUES = "allowed-values";
    public static final String OPTION_CHECK_KEYS     = "check-keys";


    public Truthy() {
        registerListOption(OPTION_ALLOWED_VALUES, TRUTHY_TOKENS, Arrays.asList("true", "false"));
        registerOption(OPTION_CHECK_KEYS, true);
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<LintProblem> check(Map<Object, Object> conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        if (prev instanceof TagToken) {
            return problems;
        }

        if (!(boolean)conf.get(OPTION_CHECK_KEYS) & prev instanceof KeyToken && token instanceof ScalarToken) {
            return problems;
        }

        if (token instanceof ScalarToken) {
            List<?> forbiddenTokens = TRUTHY_TOKENS.stream().filter(i -> !((List<?>)conf.get(OPTION_ALLOWED_VALUES)).contains(i)).collect(Collectors.toList());
            if (forbiddenTokens.stream().anyMatch(truthy -> truthy.equals(((ScalarToken)token).getValue())) &&
                    ((ScalarToken)token).getStyle() == DumperOptions.ScalarStyle.PLAIN) {
                problems.add(new LintProblem(token.getStartMark().getLine() + 1,
                        token.getStartMark().getColumn() + 1,
                        String.format("truthy value should be one of [%s]", String.join(", ", (List<String>)conf.get(OPTION_ALLOWED_VALUES)))));
            }
        }

        return problems;
    }
}
