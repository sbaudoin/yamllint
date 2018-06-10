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
import org.yaml.snakeyaml.tokens.ScalarToken;
import org.yaml.snakeyaml.tokens.TagToken;
import org.yaml.snakeyaml.tokens.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Use this rule to prevent values with octal numbers. In YAML, numbers that
 * start with {@code 0} are interpreted as octal, but this is not always wanted.
 * For instance {@code 010} is the city code of Beijing, and should not be
 * converted to {@code 8}.
 *
 * <p>Examples:</p>
 * <p>With <code>octal-values: {forbid-implicit-octal: true}</code>
 * the following code snippets would **PASS**:
 * <pre>
 *     user:
 *       city-code: '010'
 * </pre>
 * <pre>
 *     user:
 *       city-code: 010,021
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     user:
 *       city-code: 010
 * </pre>
 *
 * <p>With <code>octal-values: {forbid-explicit-octal: true}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     user:
 *       city-code: '0o10'
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     user:
 *       city-code: 0o10
 * </pre>
 */
public class OctalValues extends TokenRule {
    public static final String OPTION_FORBID_IMPLICIT_OCTAL = "forbid-implicit-octal";
    public static final String OPTION_FORBID_EXPLICIT_OCTAL = "forbid-explicit-octal";


    public OctalValues() {
        options.put(OPTION_FORBID_IMPLICIT_OCTAL, Boolean.class);
        options.put(OPTION_FORBID_EXPLICIT_OCTAL, Boolean.class);
    }

    @Override
    public List<LintProblem> check(Map conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        if (prev instanceof TagToken) {
            return problems;
        }

        if ((boolean)conf.get(OPTION_FORBID_IMPLICIT_OCTAL)) {
            if (token instanceof ScalarToken) {
                if (((ScalarToken)token).getStyle().getChar() == null) {
                    String val = ((ScalarToken)token).getValue();
                    if (isDigit(val) && val.length() > 1 && val.charAt(0) =='0') {
                        problems.add(new LintProblem(
                                token.getStartMark().getLine() + 1, token.getEndMark().getColumn() + 1,
                                "forbidden implicit octal value \"" + ((ScalarToken)token).getValue() + "\""));
                    }
                }
            }
        }

        if ((boolean)conf.get(OPTION_FORBID_EXPLICIT_OCTAL)) {
            if (token instanceof ScalarToken) {
                if (((ScalarToken)token).getStyle().getChar() == null) {
                    String val = ((ScalarToken)token).getValue();
                    if (val.length() > 2 && "0o".equals(val.substring(0, 2)) && isDigit(val.substring(2))) {
                        problems.add(new LintProblem(
                                token.getStartMark().getLine() + 1, token.getEndMark().getColumn() + 1,
                                "forbidden explicit octal value \"" + ((ScalarToken)token).getValue() + "\""));
                    }
                }
            }
        }

        return problems;
    }
}
