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

import java.util.List;
import java.util.Map;

/**
 * Class that represents rules related to comments ({@code # ...})
 */
public abstract class CommentRule extends Rule {
    /**
     * Checks the passed comment token and returns a list (possibly empty, but never {@code null})
     *
     * @param conf the rule configuration
     * @param comment the comment token to check
     * @return the problems found on this comment. If no problem is found, the list is empty, never {@code null}
     */
    public abstract List<LintProblem> check(Map<Object, Object> conf, Parser.Comment comment);

    @Override
    public TYPE getType() {
        return TYPE.COMMENT;
    }
}
