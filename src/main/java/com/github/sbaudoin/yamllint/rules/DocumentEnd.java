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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Use this rule to require or forbid the use of document end marker ({@code ...}).
 * <p>Options:</p>
 * <ul>
 *     <li>Set {@code present} to {@code true} when the document end marker is required, or to {@code false} when it is forbidden.</li>
 * </ul>
 *
 * <p>Examples:</p>
 * <p>With <code>document-end: {present: true}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     ---
 *     this:
 *       is: [a, document]
 *     ...
 *     ---
 *     - this
 *     - is: another one
 *     ...
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     ---
 *     this:
 *       is: [a, document]
 *     ---
 *     - this
 *     - is: another one
 *     ...
 * </pre>
 *
 * <p>With <code>document-end: {present: false}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     ---
 *     this:
 *       is: [a, document]
 *     ---
 *     - this
 *     - is: another one
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     ---
 *     this:
 *       is: [a, document]
 *     ...
 *     ---
 *     - this
 *     - is: another one
 * </pre>
 */
public class DocumentEnd extends TokenRule {
    public static final String OPTION_PRESENT = "present";


    public DocumentEnd() {
        registerOption(OPTION_PRESENT, true);
    }

    @Override
    public List<LintProblem> check(Map<Object, Object> conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        if ((boolean)conf.get(OPTION_PRESENT)) {
            if (token instanceof StreamEndToken && !(prev instanceof DocumentEndToken || prev instanceof StreamStartToken)) {
                problems.add(new LintProblem(token.getStartMark().getLine(), 1,
                        "missing document end \"...\""));
            } else if (token instanceof DocumentStartToken && !(prev instanceof DocumentEndToken || prev instanceof StreamStartToken || prev instanceof DirectiveToken)) {
                problems.add(new LintProblem(token.getStartMark().getLine() + 1, 1,
                        "missing document end \"...\""));
            }
        } else {
            if (token instanceof DocumentEndToken) {
                problems.add(new LintProblem(token.getStartMark().getLine() + 1,
                        token.getStartMark().getColumn() + 1,
                        "found forbidden document end \"...\""));
            }
        }

        return problems;
    }
}
