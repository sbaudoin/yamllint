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
 * Use this rule to report duplicated anchors and aliases referencing undeclared anchors.
 * <p>Options:</p>
 * <ul>
 *     <li>Set {@code forbid-undeclared-aliases} to {@code true} to avoid aliases that reference
 *         an anchor that hasn't been declared (either not declared at all, or declared
 *         later in the document).</li>
 *     <li>Set {@code forbid-duplicated-anchors} to {@code true} to avoid duplications of a same
 *         anchor.</li>
 * </ul>
 *
 * <p>Default values (when enabled):</p>
 * <pre>
 *     rules:
 *       anchors:
 *         forbid-undeclared-aliases: true
 *         forbid-duplicated-anchors: false
 * </pre>
 *
 * <p>Examples:</p>
 * <p>With <code>anchors: {forbid-undeclared-aliases: true}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     ---
 *     - &anchor
 *       foo: bar
 *     - *anchor
 * </pre>
 * the following code snippets would **FAIL**:
 * <pre>
 *     ---
 *     - &anchor
 *       foo: bar
 *     - *unknown
 * </pre>
 * <pre>
 *     ---
 *     - &anchor
 *       foo: bar
 *     - <<: *unknown
 *       extra: value
 * </pre>
 *
 * <p>With <code>anchors: {forbid-duplicated-anchors: true}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     ---
 *     - &anchor1 Foo Bar
 *     - &anchor2 [item 1, item 2]
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     ---
 *     - &anchor Foo Bar
 *     - &anchor [item 1, item 2]
 * </pre>
 */
public class Anchors extends TokenRule {
    private static final String ANCHORS_KEY = "anchors";


    public static final String OPTION_FORBID_UNDECLARED_ALIASES = "forbid-undeclared-aliases";
    public static final String OPTION_FORBID_DUPLICATED_ANCHORS = "forbid-duplicated-anchors";


    public Anchors() {
        registerOption(OPTION_FORBID_UNDECLARED_ALIASES, true);
        registerOption(OPTION_FORBID_DUPLICATED_ANCHORS, false);
    }

    @Override
    public List<LintProblem> check(Map<Object, Object> conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        if (Boolean.TRUE.equals(conf.get(OPTION_FORBID_UNDECLARED_ALIASES)) || Boolean.TRUE.equals(conf.get(OPTION_FORBID_DUPLICATED_ANCHORS))) {
            if (token instanceof StreamStartToken || token instanceof DocumentStartToken) {
                context.put(ANCHORS_KEY, new HashSet<String>());
            }
        }

        if (Boolean.TRUE.equals(conf.get(OPTION_FORBID_UNDECLARED_ALIASES)) && token instanceof AliasToken &&
                !((Set<?>)context.get(ANCHORS_KEY)).contains(((AliasToken)token).getValue())) {
            problems.add(new LintProblem(token.getStartMark().getLine() + 1, token.getStartMark().getColumn() + 1,
                    String.format("found undeclared alias \"%s\"", ((AliasToken)token).getValue())));
        }

        if (Boolean.TRUE.equals(conf.get(OPTION_FORBID_DUPLICATED_ANCHORS)) && token instanceof AnchorToken &&
                ((Set<?>)context.get(ANCHORS_KEY)).contains(((AnchorToken)token).getValue())) {
            problems.add(new LintProblem(token.getStartMark().getLine() + 1, token.getStartMark().getColumn() + 1,
                    String.format("found duplicated anchor \"%s\"", ((AnchorToken) token).getValue())));
        }

        if (Boolean.TRUE.equals(conf.get(OPTION_FORBID_UNDECLARED_ALIASES)) || Boolean.TRUE.equals(conf.get(OPTION_FORBID_DUPLICATED_ANCHORS))) {
            if (token instanceof AnchorToken) {
                ((Set<String>)context.get(ANCHORS_KEY)).add(((AnchorToken)token).getValue());
            }
        }

        return problems;
    }
}
