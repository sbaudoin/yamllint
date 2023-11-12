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
    /**
     * Name of the "max" option
     */
    public static final String OPTION_MAX       = "max";
    /**
     * Name of the "max-start" option
     */
    public static final String OPTION_MAX_START = "max-start";
    /**
     * Name of the "max-end" option
     */
    public static final String OPTION_MAX_END   = "max-end";


    /**
     * Constructor. Sets default values to rule options.
     */
    public EmptyLines() {
        registerOption(OPTION_MAX, 2);
        registerOption(OPTION_MAX_START, 0);
        registerOption(OPTION_MAX_END, 0);
    }

    @Override
    public List<LintProblem> check(Map<Object, Object> conf, Parser.Line line) {
        List<LintProblem> problems = new ArrayList<>();

        if (line.getStart() == line.getEnd() && line.getEnd() < line.getBuffer().length()) {
            // Only alert on the last blank line of a series
            if ((line.getEnd() + 2 <= line.getBuffer().length() &&
                            "\n\n".equals(line.getBuffer().substring(line.getEnd(), line.getEnd() + 2))) ||
                    (line.getEnd() + 4 <= line.getBuffer().length() &&
                            "\r\n\r\n".equals(line.getBuffer().substring(line.getEnd(), line.getEnd() + 4)))) {
                return problems;
            }

            int blankLines = 0;

            int start = line.getStart();
            while (start >= 2 && "\r\n".equals(line.getBuffer().substring(start - 2, start))) {
                blankLines += 1;
                start -= 2;
            }
            while (start >= 1 && line.getBuffer().charAt(start - 1) == '\n') {
                blankLines += 1;
                start -= 1;
            }

            int max = (int)conf.get(OPTION_MAX);

            // Special case:start of document
            if (start == 0) {
                blankLines += 1;  // first line doesn't have a preceding \n
                max = (int)conf.get(OPTION_MAX_START);
            }

            // Special case: end of document
            // NOTE: The last line of a file is always supposed to end with a new
            // line. See POSIX definition of a line at:
            if ((line.getEnd() == line.getBuffer().length() - 1 &&
                    line.getBuffer().charAt(line.getEnd()) == '\n') ||
                    (line.getEnd() == line.getBuffer().length() - 2 &&
                            "\r\n".equals(line.getBuffer().substring(line.getEnd(), line.getEnd() + 2)))) {
                // Allow the exception of the one - byte file containing '\n'
                if (line.getEnd() == 0) {
                    return problems;
                }

                max = (int)conf.get(OPTION_MAX_END);
            }

            if (blankLines > max) {
                problems.add(new LintProblem(line.getLineNo(), 1, "too many blank lines (" + blankLines + " > " + max + ")"));
            }
        }

        return problems;
    }
}
