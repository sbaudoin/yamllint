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
 *     <li>Use {@code forbid-in-block-sequences} to prevent empty values in block sequences.</li>
 * </ul>
 *
 * <p>Default values (when enabled):</p>
 * <pre>
 *     rules:
 *       empty-values:
 *         forbid-in-block-mappings: true
 *         forbid-in-flow-mappings: true
 *         forbid-in-block-sequences: true
 * </pre>
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
 *
 * <p>With <code>empty-values: {forbid-in-block-sequences: true}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     some-sequence:
 *       - string item
 * </pre>
 * <pre>
 *     some-sequence:
 *       - null
 * </pre>
 * the following code snippets would **FAIL**:
 * <pre>
 *     some-sequence:
 *       -
 * </pre>
 * <pre>
 *     some-sequence:
 *       - string item
 *       -
 * </pre>
 */
public class EmptyValues extends TokenRule {
    /**
     * Name of the "forbid-in-block-mappings" option
     */
    public static final String OPTION_FORBID_IN_BLOCK_MAPPINGS  = "forbid-in-block-mappings";
    /**
     * Name of the "forbid-in-flow-mappings" option
     */
    public static final String OPTION_FORBID_IN_FLOW_MAPPINGS   = "forbid-in-flow-mappings";
    /**
     * Name of the "forbid-in-block-sequences" option
     */
    public static final String OPTION_FORBID_IN_BLOCK_SEQUENCES = "forbid-in-block-sequences";


    /**
     * Constructor. Sets default values to rule options.
     */
    public EmptyValues() {
        registerOption(OPTION_FORBID_IN_BLOCK_MAPPINGS, true);
        registerOption(OPTION_FORBID_IN_FLOW_MAPPINGS, true);
        registerOption(OPTION_FORBID_IN_BLOCK_SEQUENCES, true);
    }

    @Override
    public List<LintProblem> check(Map<Object, Object> conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
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

        if ((boolean)conf.get(OPTION_FORBID_IN_BLOCK_SEQUENCES)) {
            if (token instanceof BlockEntryToken && (next instanceof KeyToken || next instanceof BlockEndToken || next instanceof BlockEntryToken)) {
                problems.add(new LintProblem(token.getStartMark().getLine() + 1,
                        token.getEndMark().getColumn() + 1,
                        "empty value in block sequence"));
            }
        }

        return problems;
    }
}
