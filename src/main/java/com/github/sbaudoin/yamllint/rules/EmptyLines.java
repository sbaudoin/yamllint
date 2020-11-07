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
import java.util.List;
import java.util.Map;

/**
 * Use this rule to set a maximal number of allowed consecutive blank lines.
 * <p>Options:</p>
 * <ul>
 *     <li>{@code max} defines the maximal number of consecutive empty lines allowed in the document.</li>
 *     <li>{@code max-start} defines the maximal number of empty lines allowed at the
 *     beginning of the file. This option takes precedence over {@code max}.</li>
 *     <li>{@code max-end} defines the maximal number of empty lines allowed at the end of the file.
 *     This option takes precedence over {@code max}.</li>
 * </ul>
 *
 * <p>Examples:</p>
 * <p>With <code>empty-lines: {max: 1}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     - foo:
 *         - 1
 *         - 2
 *
 *     - bar1: [3, 4]
 *
 *     - bar2: [3, 4]
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     - foo:
 *         - 1
 *         - 2
 *
 *
 *     - bar1: [3, 4]
 *
 *     - bar2: [3, 4]
 * </pre>
 */
public class EmptyLines extends LineRule {
    public static final String OPTION_MAX       = "max";
    public static final String OPTION_MAX_START = "max-start";
    public static final String OPTION_MAX_END   = "max-end";


    public EmptyLines() {
        options.put(OPTION_MAX, Integer.class);
        options.put(OPTION_MAX_START, Integer.class);
        options.put(OPTION_MAX_END, Integer.class);
    }

    @Override
    public List<LintProblem> check(Map conf, Parser.Line line) {
        List<LintProblem> problems = new ArrayList<>();

        // We need to replace the Windows line breaks to properly identify the empty lines
        Parser.Line newLine = Parser.getLines(line.getBuffer().replace("\r\n", "\n")).get(line.getLineNo() - 1);

        if (newLine.getStart() == newLine.getEnd() && newLine.getEnd() < newLine.getBuffer().length()) {
            // Only alert on the last blank line of a series
            if (newLine.getEnd() < newLine.getBuffer().length() - 1 && newLine.getBuffer().charAt(newLine.getEnd() + 1) == '\n') {
                return problems;
            }

            int blankLines = 0;

            while (newLine.getStart() > blankLines && newLine.getBuffer().charAt(newLine.getStart() - blankLines - 1) == '\n') {
                blankLines += 1;
            }

            int max = (int)conf.get(OPTION_MAX);

            // Special case:start of document
            if (newLine.getStart() - blankLines == 0) {
                blankLines += 1;  // first line doesn't have a preceding \n
                max = (int)conf.get(OPTION_MAX_START);
            }

            // Special case: end of document
            // NOTE: The last line of a file is always supposed to end with a new
            // line. See POSIX definition of a line at:
            if (newLine.getEnd() == newLine.getBuffer().length() - 1 && (newLine.getBuffer().charAt(newLine.getEnd()) == '\n' || newLine.getBuffer().charAt(newLine.getEnd()) == '\r')) {
                // Allow the exception of the one - byte file containing '\n'
                if (newLine.getEnd() == 0) {
                    return problems;
                }

                max = (int)conf.get(OPTION_MAX_END);
            }

            if (blankLines > max) {
                problems.add(new LintProblem(newLine.getLineNo(), 1, "too many blank lines (" + blankLines + " > " + max + ")"));
            }
        }

        return problems;
    }
}
