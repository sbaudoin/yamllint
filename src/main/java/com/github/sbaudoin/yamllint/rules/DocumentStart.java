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
package com.github.sbaudoin.yamllint.rules;

import com.github.sbaudoin.yamllint.LintProblem;
import org.yaml.snakeyaml.tokens.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Use this rule to require or forbid the use of document start marker ({@code ---}).
 * <p>Options:</p>
 * <ul>
 *     <li>Set {@code present} to {@code true} when the document start marker is required, or to {@code false}
 *     when it is forbidden.</li>
 * </ul>
 *
 * <p>Examples:</p>
 * <p>With <code>document-start: {present: true}</code>
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
 *     this:
 *       is: [a, document]
 *     ---
 *     - this
 *     - is: another one
 * </pre>
 *
 * <p>With <code>document-start: {present: false}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     this:
 *       is: [a, document]
 *     ...
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     ---
 *     this:
 *       is: [a, document]
 *     ...
 * </pre>
 */
public class DocumentStart extends TokenRule {
    public static final String OPTION_PRESENT = "present";


    public DocumentStart() {
        options.put(OPTION_PRESENT, Boolean.class);
    }

    @Override
    public List<LintProblem> check(Map conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        if ((boolean)conf.get(OPTION_PRESENT)) {
            if ((prev instanceof StreamStartToken || prev instanceof DocumentEndToken || prev instanceof  DirectiveToken) &&
                    !(token instanceof DocumentStartToken || token instanceof DirectiveToken || token instanceof StreamEndToken)) {
                problems.add(new LintProblem(token.getStartMark().getLine() + 1, 1,
                        "missing document start \"---\""));
            }
        } else {
            if (token instanceof DocumentStartToken) {
                problems.add(new LintProblem(token.getStartMark().getLine() + 1,
                        token.getStartMark().getColumn() + 1,
                        "found forbidden document start \"---\""));
            }
        }

        return problems;
    }
}
