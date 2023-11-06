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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Use this rule to force the type of new line characters.
 * <p>Options:</p>
 * <ul>
 *     <li>Set {@code type} to {@code unix} to enforce UNIX-typed new line characters ({@code \n}),
 *     set {@code type} to {@code dos} to enforce DOS-typed new line characters ({@code \r\n}),
 *     or set {@code type} to {@code platform} to infer the type from the system running yamllint
 *     ({@code \n} on POSIX / UNIX / Linux / Mac OS systems or {@code \r\n`} on DOS / Windows systems).</li>
 * </ul>
 */
public class NewLines extends LineRule {
    public static final String OPTION_TYPE = "type";


    public NewLines() {
        registerOption(OPTION_TYPE, Arrays.asList("unix", "dos", "platform"));
    }

    @Override
    public List<LintProblem> check(Map<Object, Object> conf, Parser.Line line) {
        List<LintProblem> problems = new ArrayList<>();

        String newLineChar = null;
        switch ((String)conf.get(OPTION_TYPE)) {
            case "dos":
                newLineChar = "\r\n";
                break;
            case "unix":
                newLineChar = "\n";
                break;
            default:  // Is "platform"
                newLineChar = System.getProperty("line.separator");  // Should be System.lineSeparator() but needed for unit tests to work...
                break;
        }

        // Check only first line
        if (line.getStart() == 0 && line.getBuffer().length() > line.getEnd()) {
            assert newLineChar != null;  // Valid since the option values have been checked and 'type' should be of a supported value checked above
            int endIndex = (line.getEnd() + newLineChar.length() <= line.getBuffer().length())?(line.getEnd() + newLineChar.length()):(line.getEnd() + 1);
            if (!newLineChar.equals(line.getBuffer().substring(line.getEnd(), endIndex))) {
                problems.add(new LintProblem(1, line.getEnd() - line.getStart() + 1,
                        "wrong new line character: expected " +
                                newLineChar.replace("\n", "\\n").replace("\r", "\\r")));
            }
        }

        return problems;
    }
}
