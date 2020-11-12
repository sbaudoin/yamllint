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
 * Use this rule to force the type of new line characters.
 * <p>Options:</p>
 * <ul>
 *     <li>Set {@code type} to {@code unix} to use UNIX-typed new line characters ({@code \n}), or
 *     {@code dos} to use DOS-typed new line characters ({@code \r\n}).</li>
 * </ul>
 */
public class NewLines extends LineRule {
    public static final String OPTION_TYPE = "type";


    public NewLines() {
        registerOption(OPTION_TYPE, Arrays.asList("unix", "dos"));
    }

    @Override
    public List<LintProblem> check(Map<Object, Object> conf, Parser.Line line) {
        List<LintProblem> problems = new ArrayList<>();

        // Check only first line
        if (line.getStart() == 0 && line.getBuffer().length() > line.getEnd()) {
            if ("dos".equals(conf.get(OPTION_TYPE))) {
                if ((line.getEnd() == 0 && line.getBuffer().charAt(0) == '\n') ||
                        !"\r\n".equals(line.getBuffer().substring(line.getEnd() - 1, line.getEnd() + 1))) {
                    problems.add(new LintProblem(1, line.getEnd() - line.getStart() + 1,
                            "wrong new line character: expected \\r\\n"));
                }
            } else {
                if (line.getEnd() > 0 && line.getBuffer().charAt(line.getEnd() - 1) == '\r') {
                    problems.add(new LintProblem(1, line.getEnd() - line.getStart(),
                            "wrong new line character: expected \\n"));
                }
            }
        }

        return problems;
    }
}
