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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Use this rule to report duplicated anchors and aliases referencing undeclared anchors.
 * <p>Options:</p>
 * <ul>
 *     <li>Set {@code forbid-undeclared-aliases} to {@code true} to avoid aliases that reference
 *         an anchor that hasn't been declared (either not declared at all, or declared
 *         later in the document).</li>
 *     <li>Set {@code forbid-duplicated-anchors} to {@code true} to avoid duplications of a same
 *         anchor.</li>
 *     <li>Set {@code forbid-unused-anchors} to {@code true} to avoid anchors being declared but
 *         not used anywhere in the YAML document via alias.</li>
 * </ul>
 *
 * <p>Default values (when enabled):</p>
 * <pre>
 *     rules:
 *       anchors:
 *         forbid-undeclared-aliases: true
 *         forbid-duplicated-anchors: false
 *         forbid-unused-anchors: false
 * </pre>
 *
 * <p>Examples:</p>
 * <p>With <code>anchors: {forbid-undeclared-aliases: true}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     ---
 *     - &amp;anchor
 *       foo: bar
 *     - *anchor
 * </pre>
 * the following code snippets would **FAIL**:
 * <pre>
 *     ---
 *     - &amp;anchor
 *       foo: bar
 *     - *unknown
 * </pre>
 * <pre>
 *     ---
 *     - &amp;anchor
 *       foo: bar
 *     - &lt;&lt;: *unknown
 *       extra: value
 * </pre>
 *
 * <p>With <code>anchors: {forbid-duplicated-anchors: true}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     ---
 *     - &amp;anchor1 Foo Bar
 *     - &amp;anchor2 [item 1, item 2]
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     ---
 *     - &amp;anchor Foo Bar
 *     - &amp;anchor [item 1, item 2]
 * </pre>
 *
 * <p>With <code>anchors: {forbid-unused-anchors: true}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     ---
 *     - &amp;anchor
 *       foo: bar
 *     - *anchor
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     ---
 *     - &amp;anchor
 *       foo: bar
 *     - items:
 *       - item1
 *       - item2
 * </pre>
 */
public class Anchors extends TokenRule {
    private static final String ANCHORS_KEY = "anchors";


    public static final String OPTION_FORBID_UNDECLARED_ALIASES = "forbid-undeclared-aliases";
    public static final String OPTION_FORBID_DUPLICATED_ANCHORS = "forbid-duplicated-anchors";
    public static final String OPTION_FORBID_UNUSED_ANCHORS     = "forbid-unused-anchors";


    public Anchors() {
        registerOption(OPTION_FORBID_UNDECLARED_ALIASES, true);
        registerOption(OPTION_FORBID_DUPLICATED_ANCHORS, false);
        registerOption(OPTION_FORBID_UNUSED_ANCHORS, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<LintProblem> check(Map<Object, Object> conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        if (Boolean.TRUE.equals(conf.get(OPTION_FORBID_UNDECLARED_ALIASES)) ||
                Boolean.TRUE.equals(conf.get(OPTION_FORBID_DUPLICATED_ANCHORS)) ||
                Boolean.TRUE.equals(conf.get(OPTION_FORBID_UNUSED_ANCHORS))) {
            if (token instanceof StreamStartToken ||
                    token instanceof DocumentStartToken ||
                    token instanceof DocumentEndToken) {
                context.put(ANCHORS_KEY, new HashMap<String, Object>());
            }
        }

        if (Boolean.TRUE.equals(conf.get(OPTION_FORBID_UNDECLARED_ALIASES)) && token instanceof AliasToken &&
                !getContextMap(context).containsKey(((AliasToken)token).getValue())) {
            problems.add(new LintProblem(token.getStartMark().getLine() + 1, token.getStartMark().getColumn() + 1,
                    String.format("found undeclared alias \"%s\"", ((AliasToken)token).getValue())));
        }

        if (Boolean.TRUE.equals(conf.get(OPTION_FORBID_DUPLICATED_ANCHORS)) && token instanceof AnchorToken &&
                getContextMap(context).containsKey(((AnchorToken)token).getValue())) {
            problems.add(new LintProblem(token.getStartMark().getLine() + 1, token.getStartMark().getColumn() + 1,
                    String.format("found duplicated anchor \"%s\"", ((AnchorToken) token).getValue())));
        }

        if (Boolean.TRUE.equals(conf.get(OPTION_FORBID_UNUSED_ANCHORS))) {
            // Unused anchors can only be detected at the end of Document.
            // End of document can be either
            //   - end of stream
            //   - end of document sign '...'
            //   - start of a new document sign '---'
            // If next token indicates end of document,
            // check if the anchors have been used or not.
            // If they haven't been used, report problem on those anchors.
            if (next instanceof StreamEndToken ||
                    next instanceof DocumentStartToken ||
                    next instanceof DocumentEndToken) {
                for (String k : getContextMap(context).keySet()) {
                    Map<String, Object> info = (Map<String, Object>) getContextMap(context).get(k);
                    if (Boolean.FALSE.equals(info.get("used"))) {
                        problems.add(new LintProblem((Integer)info.get("line") + 1,
                                (Integer)info.get("column") + 1,
                            String.format("found unused anchor \"%s\"", k)));
                    }
                }
            } else if (token instanceof AliasToken) {
                ((HashMap<String, Object>) (getContextMap(context).getOrDefault(
                        ((AliasToken)token).getValue(), new HashMap<String, Object>()))).put("used", true);
            }
        }

        if (Boolean.TRUE.equals(conf.get(OPTION_FORBID_UNDECLARED_ALIASES)) ||
                Boolean.TRUE.equals(conf.get(OPTION_FORBID_DUPLICATED_ANCHORS)) ||
                Boolean.TRUE.equals(conf.get(OPTION_FORBID_UNUSED_ANCHORS))) {
            if (token instanceof AnchorToken) {
                getContextMap(context).put(((AnchorToken) token).getValue(),
                        Stream.of(
                                new AbstractMap.SimpleEntry<>("line", token.getStartMark().getLine()),
                                new AbstractMap.SimpleEntry<>("column", token.getStartMark().getColumn()),
                                new AbstractMap.SimpleEntry<>("used", false))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            }
        }

        return problems;
    }


    /**
     * Method used to simplify the syntax of the {@code check} method
     *
     * @param context a context map used to propagate info between rules
     * @return a map used to store information about the tokens being checked
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getContextMap(Map<String, Object> context) {
        return (Map<String, Object>) context.get(ANCHORS_KEY);
    }
}
