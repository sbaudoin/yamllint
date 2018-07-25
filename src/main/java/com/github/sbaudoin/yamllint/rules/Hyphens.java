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
import org.yaml.snakeyaml.tokens.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Use this rule to control the number of spaces after hyphens ({@code -}).
 * <p>Options:</p>
 * <ul>
 *     <li>{@code max-spaces-after} defines the maximal number of spaces allowed after hyphens.</li>
 * </ul>
 *
 * <p>Examples:</p>
 * <p>With <code>hyphens: {max-spaces-after: 1}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     - first list:
 *         - a
 *         - b
 *     - - 1
 *       - 2
 *       - 3
 * </pre>
 * the following code snippets would **FAIL**:
 * <pre>
 *     -  first list:
 *          - a
 *          - b
 * </pre>
 *
 * <p>With <code>hyphens: {max-spaces-after: 3}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     -   key
 *     -  key2
 *     - key42
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     -    key
 *     -   key2
 *     -  key42
 * </pre>
 */
public class Hyphens extends TokenRule {
    public static final String OPTION_MAX_SPACES_AFTER = "max-spaces-after";


    public Hyphens() {
        options.put(OPTION_MAX_SPACES_AFTER, Integer.class);
    }

    @Override
    public List<LintProblem> check(Map conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        if (token instanceof BlockEntryToken) {
            LintProblem problem = spacesAfter(token, next,
                    -1,
                    (int)conf.get(OPTION_MAX_SPACES_AFTER),
                    null,
                    "too many spaces after hyphen");
            if (problem != null) {
                problems.add(problem);
            }
        }

        return problems;
    }
}
