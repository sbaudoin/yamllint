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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Use this rule to require a new line character ({@code \n}) at the end of files.
 * <p>The POSIX standard 'requires the last line to end with a new line character
 * <a href="http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap03.html#tag_03_206">http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap03.html#tag_03_206</a>'.
 * All UNIX tools expect a new line at the end of files. Most text editors use
 * this convention too.</p>
 */
public class NewLineAtEndOfFile extends LineRule {
    @Override
    public List<LintProblem> check(Map conf, Parser.Line line) {
        List<LintProblem> problems = new ArrayList<>();

        if (line.getEnd() == line.getBuffer().length() && line.getEnd() > line.getStart()) {
            problems.add(new LintProblem(line.getLineNo(), line.getEnd() - line.getStart() + 1,
                    "no new line character at the end of file"));
        }

        return problems;
    }
}
