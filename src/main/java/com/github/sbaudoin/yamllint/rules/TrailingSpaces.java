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
 * Use this rule to forbid trailing spaces at the end of lines.
 *
 * <p>Examples:</p>
 * <p>With <code>trailing-spaces: {}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     this document doesn't contain
 *     any trailing
 *     spaces
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     this document contains     " "
 *     trailing spaces
 *     on lines 1 and 3         " "
 * </pre>
 */
public class TrailingSpaces extends LineRule {
    @Override
    public List<LintProblem> check(Map<Object, Object> conf, Parser.Line line) {
        List<LintProblem> problems = new ArrayList<>();

        if (line.getEnd() == 0) {
            return problems;
        }

        // YAML recognizes two white space characters: space and tab.
        // http://yaml.org/spec/1.2/spec.html#id2775170

        int pos = line.getEnd();
        while (pos > line.getStart() && isWhitespace(line.getBuffer().charAt(pos - 1))) {
            pos -= 1;
        }

        if (pos != line.getEnd() && (line.getBuffer().charAt(pos) == ' ' || line.getBuffer().charAt(pos) == '\t')) {
            problems.add(new LintProblem(line.getLineNo(), pos - line.getStart() + 1,
                    "trailing spaces"));
        }

        return problems;
    }
}
