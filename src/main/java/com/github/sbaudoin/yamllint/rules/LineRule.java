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

import java.util.List;
import java.util.Map;

/**
 * Class that represents a rule that checks a complete (non-tokenized) line
 */
public abstract class LineRule extends Rule {
    /**
     * Checks the line and returns the problems found on it
     *
     * @param conf the rule configuration
     * @param line the line to be checked
     * @return the problems found on the line or an empty list if none found (the method never returns {@code null}
     */
    public abstract List<LintProblem> check(Map conf, Parser.Line line);

    @Override
    public TYPE getType() {
        return TYPE.LINE;
    }
}
