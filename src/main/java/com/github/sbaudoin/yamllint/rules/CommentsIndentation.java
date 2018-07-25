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
import com.github.sbaudoin.yamllint.Parser;
import org.yaml.snakeyaml.tokens.StreamEndToken;
import org.yaml.snakeyaml.tokens.StreamStartToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Use this rule to force comments to be indented like content.
 *
 * <p>Examples:</p>
 * <p>With <code>comments-indentation: {}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     # Fibonacci
 *     [0, 1, 1, 2, 3, 5]
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *       # Fibonacci
 *     [0, 1, 1, 2, 3, 5]
 * </pre>
 * the following code snippet would **PASS**:
 * <pre>
 *     list:
 *         - 2
 *         - 3
 *         # - 4
 *         - 5
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     list:
 *         - 2
 *         - 3
 *     #    - 4
 *         - 5
 * </pre>
 * the following code snippet would **PASS**:
 * <pre>
 *     # This is the first object
 *     obj1:
 *       - item A
 *       # - item B
 *     # This is the second object
 *     obj2: []
 * </pre>
 * the following code snippet would **PASS**:
 * <pre>
 *     # This sentence
 *     # is a block comment
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     # This sentence
 *      # is a block comment
 * </pre>
 */
public class CommentsIndentation extends CommentRule {
    @Override
    public List<LintProblem> check(Map conf, Parser.Comment comment) {
        List<LintProblem> problems = new ArrayList<>();

        // Only check block comments
        if (!(comment.getTokenBefore() instanceof StreamStartToken) && comment.getTokenBefore().getEndMark().getLine() + 1 == comment.getLineNo()) {
            return problems;
        }

        int nextLineIndent = comment.getTokenAfter().getStartMark().getColumn();
        if (comment.getTokenAfter() instanceof StreamEndToken) {
            nextLineIndent = 0;
        }

        int prevLineIndent;
        if (comment.getTokenBefore() instanceof StreamStartToken) {
            prevLineIndent = 0;
        } else {
            prevLineIndent = getLineIndent(comment.getTokenBefore());
        }

        // In the following case only the next line indent is valid:
        //     list:
        //         # comment
        //         - 1
        //         - 2
        if (prevLineIndent <= nextLineIndent) {
            prevLineIndent = nextLineIndent;
        }

        // If two indents are valid but a previous comment went back to normal
        // indent, for the next ones to do the same. In other words, avoid this:
        //     list:
        //         - 1
        //     # comment on valid indent (0)
        //         # comment on valid indent (4)
        //     other-list:
        //         - 2
        if (comment.getCommentBefore() != null && !comment.getCommentBefore().isInline()) {
            prevLineIndent = comment.getCommentBefore().getColumnNo() - 1;
        }

        if (comment.getColumnNo() - 1 != prevLineIndent && comment.getColumnNo() - 1 != nextLineIndent) {
            problems.add(new LintProblem(comment.getLineNo(), comment.getColumnNo(),
                    "comment not indented like content"));
        }

        return problems;
    }
}
