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
 * Use this rule to limit the permitted values for floating-point numbers.
 * YAML permits three classes of float expressions: approximation to real numbers,
 * positive and negative infinity and "not a number".
 *
 * <p>Options:</p>
 * <ul>
 *     <li>Use {@code require-numeral-before-decimal} to require floats to start
 *         with a numeral (eg. {@code 0.0} instead of {@code .0}).</li>
 *     <li>Use {@code forbid-scientific-notation} to forbid scientific notation.</li>
 *     <li>Use {@code forbid-nan} to forbid NaN (not a number) values.</li>
 *     <li>Use {@code forbid-inf} to forbid infinite values.</li>
 * </ul>
 *
 * <p>Default values (when enabled):</p>
 * <pre>
 *     rules:
 *       float-values:
 *         forbid-inf: false
 *         forbid-nan: false
 *         forbid-scientific-notation: false
 *         require-numeral-before-decimal: false
 * </pre>
 *
 * <p>Examples:</p>
 * <p>With <code>float-values: {require-numeral-before-decimal: true}</code>
 * the following code snippets would **PASS**:
 * <pre>
 *     anemometer:
 *       angle: 0.0
 * </pre>
 * the following code snippets would **FAIL**:
 * <pre>
 *     anemometer:
 *       angle: .0
 * </pre>
 *
 * <p>With <code>float-values: {forbid-scientific-notation: true}</code>
 * the following code snippets would **PASS**:
 * <pre>
 *     anemometer:
 *       angle: 0.00001
 * </pre>
 * the following code snippets would **FAIL**:
 * <pre>
 *     anemometer:
 *       angle: 10e-6
 * </pre>
 *
 * <p>With <code>float-values: {forbid-nan: true}</code>
 * the following code snippets would **FAIL**:
 * <pre>
 *     anemometer:
 *       angle: .NaN
 * </pre>
 *
 * <p>With <code>float-values: {forbid-inf: true}</code>
 * the following code snippets would **FAIL**:
 * <pre>
 *     anemometer:
 *       angle: .inf
 * </pre>
 */
public class FloatValues extends TokenRule {
    /**
     * Name of the "require-numeral-before-decimal" option
     */
    public static final String OPTION_REQUIRE_NUMERAL_BEFORE_DECIMAL = "require-numeral-before-decimal";
    /**
     * Name of the "forbid-scientific-notation" option
     */
    public static final String OPTION_FORBID_SCIENTIFIC_NOTATION     = "forbid-scientific-notation";
    /**
     * Name of the "forbid-nan" option
     */
    public static final String OPTION_FORBID_NAN                     = "forbid-nan";
    /**
     * Name of the "forbid-inf" option
     */
    public static final String OPTION_FORBID_INF                     = "forbid-inf";


    /**
     * Constructor. Sets default values to rule options.
     */
    public FloatValues() {
        registerOption(OPTION_REQUIRE_NUMERAL_BEFORE_DECIMAL, false);
        registerOption(OPTION_FORBID_SCIENTIFIC_NOTATION, false);
        registerOption(OPTION_FORBID_NAN, false);
        registerOption(OPTION_FORBID_INF, false);
    }

    @Override
    public List<LintProblem> check(Map<Object, Object> conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        if (prev instanceof TagToken || !(token instanceof ScalarToken) || ((ScalarToken) token).getStyle().getChar() != null) {
            return problems;
        }

        String val = ((ScalarToken) token).getValue();

        if ((boolean)conf.get(OPTION_FORBID_NAN) && val.matches("(\\.nan|\\.NaN|\\.NAN)$")) {
            problems.add(new LintProblem(token.getStartMark().getLine() + 1,
                    token.getStartMark().getColumn() + 1,
                    String.format("forbidden not a number value \"%s\"", val)));
        }

        if ((boolean)conf.get(OPTION_FORBID_INF) && val.matches("[-+]?(\\.inf|\\.Inf|\\.INF)$")) {
            problems.add(new LintProblem(token.getStartMark().getLine() + 1,
                    token.getStartMark().getColumn() + 1,
                    String.format("forbidden infinite value \"%s\"", val)));
        }

        if ((boolean)conf.get(OPTION_FORBID_SCIENTIFIC_NOTATION) && val.matches("[-+]?(\\.\\d+|\\d+(\\.\\d*)?)([eE][-+]?\\d+)$")) {
            problems.add(new LintProblem(token.getStartMark().getLine() + 1,
                    token.getStartMark().getColumn() + 1,
                    String.format("forbidden scientific notation \"%s\"", val)));
        }

        if ((boolean)conf.get(OPTION_REQUIRE_NUMERAL_BEFORE_DECIMAL) && val.matches("[-+]?(\\.\\d+)([eE][-+]?\\d+)?$")) {
            problems.add(new LintProblem(token.getStartMark().getLine() + 1,
                    token.getStartMark().getColumn() + 1,
                    String.format("forbidden decimal missing 0 prefix \"%s\"", val)));
        }

        return problems;
    }
}
