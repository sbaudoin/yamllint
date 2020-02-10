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
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.resolver.Resolver;
import org.yaml.snakeyaml.tokens.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Use this rule to forbid any string values that are not quoted. You can also enforce the type of the quote used using
 * the {@code quote-type} option ({@code single}, {@code double} or {@code any}).
 *
 * <p><strong>Note</strong>: Multi-line strings (with {@code |} or {@code >}) will not be checked.</p>
 *
 * <p>Examples:</p>
 * <p>With <code>quoted-strings: {quote-type: any}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     foo: "bar"
 *     bar: 'foo'
 *     number: 123
 *     boolean: true
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     foo: bar
 * </pre>
 */
public class QuotedStrings extends TokenRule {
    public static final String OPTION_QUOTE_TYPE = "quote-type";
    public static final String QUOTE_STYLE_SINGLE = "single";
    public static final String QUOTE_STYLE_DOUBLE = "double";
    public static final String QUOTE_STYLE_ANY = "any";


    public QuotedStrings() {
        options.put(OPTION_QUOTE_TYPE, Arrays.asList(QUOTE_STYLE_SINGLE, QUOTE_STYLE_DOUBLE, QUOTE_STYLE_ANY));
    }

    @Override
    public List<LintProblem> check(Map conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        String quoteType = (String) conf.getOrDefault(OPTION_QUOTE_TYPE, QUOTE_STYLE_ANY);

        List<LintProblem> problems = new ArrayList<>();

        if (token instanceof ScalarToken && (prev instanceof ValueToken || prev instanceof TagToken)) {
            // Ignore explicit types, e.g. !!str testtest or !!int 42
            if (prev instanceof TagToken && "!!".equals(((TagToken) prev).getValue().getHandle())) {
                return problems;
            }

            // Ignore numbers, booleans, etc.
            Resolver resolver = new Resolver();
            if (resolver.resolve(NodeId.scalar, ((ScalarToken) token).getValue(), true) != Tag.STR) {
                return problems;
            }

            // Ignore multi-line strings
            if (!((ScalarToken) token).getPlain() && (
                    ((ScalarToken) token).getStyle() == DumperOptions.ScalarStyle.LITERAL ||
                            ((ScalarToken) token).getStyle() == DumperOptions.ScalarStyle.FOLDED)) {
                return problems;
            }

            if ((quoteType.equals(QUOTE_STYLE_SINGLE) && ((ScalarToken) token).getStyle() != DumperOptions.ScalarStyle.SINGLE_QUOTED) ||
                    (quoteType.equals(QUOTE_STYLE_DOUBLE) && ((ScalarToken) token).getStyle() != DumperOptions.ScalarStyle.DOUBLE_QUOTED) ||
                    (quoteType.equals(QUOTE_STYLE_ANY) && ((ScalarToken) token).getStyle() == DumperOptions.ScalarStyle.PLAIN)) {
                problems.add(new LintProblem(token.getStartMark().getLine() + 1,
                        token.getStartMark().getColumn() + 1,
                        "string value is not quoted with " + quoteType + " quotes"));
            }
        }

        return problems;
    }
}
