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
import com.github.sbaudoin.yamllint.Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Use this rule to control the position and formatting of comments.
 * <p>Options:</p>
 * <ul>
 *     <li>Use {@code require-starting-space} to require a space character right after the {@code #}. Set to {@code true}
 *     to enable, {@code false} to disable.</li>
 *     <li>{@code min-spaces-from-content} is used to visually separate inline comments from content. It defines the
 *     minimal required number of spaces between a comment and its preceding content.</li>
 * </ul>
 *
 * <p>Examples:</p>
 * <p>With <code>comments: {require-starting-space: true}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     # This sentence
 *     # is a block comment
 * </pre>
 * the following code snippet would **PASS**:
 * <pre>
 *     ##############################
 *     ## This is some documentation
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     #This sentence
 *     #is a block comment
 * </pre>
 *
 * <p>With <code>comments: {min-spaces-from-content: 2}</code>
 * the following code snippet would **PASS**:
 * <pre>x = 2 ^ 127 - 1  # Mersenne prime number</pre>
 * the following code snippet would **FAIL**:
 * <pre>x = 2 ^ 127 - 1 # Mersenne prime number</pre>
 */
public class Comments extends CommentRule {
    public static final String OPTION_REQUIRE_STARTING_SPACE  = "require-starting-space";
    public static final String OPTION_MIN_SPACES_FROM_CONTENT = "min-spaces-from-content";


    public Comments() {
        options.put(OPTION_REQUIRE_STARTING_SPACE, Boolean.class);
        options.put(OPTION_MIN_SPACES_FROM_CONTENT, Integer.class);
    }

    @Override
    public List<LintProblem> check(Map conf, Parser.Comment comment) {
        List<LintProblem> problems = new ArrayList<>();

        if (((int)conf.get(OPTION_MIN_SPACES_FROM_CONTENT)) != -1 && comment.isInline() &&
                comment.getPointer() - comment.getTokenBefore().getEndMark().getPointer() < (int)conf.get(OPTION_MIN_SPACES_FROM_CONTENT)) {
            problems.add(new LintProblem(comment.getLineNo(), comment.getColumnNo(), "too few spaces before comment"));
        }

        if ((boolean)conf.get(OPTION_REQUIRE_STARTING_SPACE)) {
            int textStart = comment.getPointer() + 1;
            while (textStart < comment.getBuffer().length() && comment.getBuffer().charAt(textStart) == '#') {
                textStart += 1;
            }
            if (textStart < comment.getBuffer().length() && Arrays.binarySearch(new char[] { '\0', '\n', ' ' }, comment.getBuffer().charAt(textStart)) < 0) {
                problems.add(new LintProblem(comment.getLineNo(),
                        comment.getColumnNo() + textStart - comment.getPointer(),
                        "missing starting space in comment"));
            }
        }

        return problems;
    }
}
