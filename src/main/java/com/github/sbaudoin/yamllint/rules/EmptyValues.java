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
 * Use this rule to prevent nodes with empty content, that implicitly result in {@code null} values.
 * <p>Options:</p>
 * <ul>
 *     <li>Use {@code forbid-in-block-mappings} to prevent empty values in block mappings.</li>
 *     <li>Use {@code forbid-in-flow-mappings} to prevent empty values in flow mappings.</li>
 * </ul>
 *
 * <p>Examples:</p>
 * <p>With <code>empty-values: {forbid-in-block-mappings: true}</code>
 * the following code snippets would **PASS**:
 * <pre>
 *     some-mapping:
 *       sub-element: correctly indented
 * </pre>
 * <pre>
 *     explicitly-null: null
 * </pre>
 * the following code snippets would **FAIL**:
 * <pre>
 *     some-mapping:
 *     sub-element: incorrectly indented
 * </pre>
 * <pre>
 *     implicitly-null:
 * </pre>
 *
 * <p>With <code>empty-values: {forbid-in-flow-mappings: true}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     {prop: null}
 *     {a: 1, b: 2, c: 3}
 * </pre>
 * the following code snippets would **FAIL**:
 * <pre>{prop: }</pre>
 * <pre> {a: 1, b:, c: 3}</pre>
 */
public class EmptyValues extends TokenRule {
    public static final String OPTION_FORBID_IN_BLOCK_MAPPINGS = "forbid-in-block-mappings";
    public static final String OPTION_FORBID_IN_FLOW_MAPPINGS  = "forbid-in-flow-mappings";


    public EmptyValues() {
        options.put(OPTION_FORBID_IN_BLOCK_MAPPINGS, Boolean.class);
        options.put(OPTION_FORBID_IN_FLOW_MAPPINGS, Boolean.class);
    }

    @Override
    public List<LintProblem> check(Map conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        if ((boolean)conf.get(OPTION_FORBID_IN_BLOCK_MAPPINGS)) {
            if (token instanceof ValueToken && (next instanceof KeyToken || next instanceof BlockEndToken)) {
                problems.add(new LintProblem(token.getStartMark().getLine() + 1,
                        token.getEndMark().getColumn() + 1,
                        "empty value in block mapping"));
            }
        }

        if ((boolean)conf.get(OPTION_FORBID_IN_FLOW_MAPPINGS)) {
            if (token instanceof ValueToken && (next instanceof FlowEntryToken || next instanceof FlowMappingEndToken)) {
                problems.add(new LintProblem(token.getStartMark().getLine() + 1,
                        token.getEndMark().getColumn() + 1,
                        "empty value in flow mapping"));
            }
        }

        return problems;
    }
}
