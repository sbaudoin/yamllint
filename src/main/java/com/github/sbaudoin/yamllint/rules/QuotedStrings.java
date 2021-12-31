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
import com.github.sbaudoin.yamllint.LintScanner;
import com.github.sbaudoin.yamllint.LintStreamReader;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.resolver.Resolver;
import org.yaml.snakeyaml.scanner.ScannerException;
import org.yaml.snakeyaml.tokens.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Use this rule to forbid any string values that are not quoted. You can also enforce the type of the quote used.
 * <p>Options:</p>
 * <ul>
 *     <li>{@code quote-type} defines allowed quotes: {@code single}, {@code double} or {@code any} (default).</li>
 *     <li>{@code required} defines whether using quotes in string values is required ({@code true}, default) or not
 *         ({@code false}), or only allowed when really needed ({@code "only-when-needed"}).</li>
 *     <li>{@code extra-required} is a list of regexps to force string values to be quoted, if they match any regex.
 *         This option can only be used with {@code required: false} and {@code required: only-when-needed}.</li>
 *     <li>{@code extra-allowed} is a list of regexps to allow quoted string values, even if {@code required: only-when-needed}
 *         is set.</li>
 * </ul>
 *
 * <p><strong>Note</strong>: Multi-line strings (with {@code |} or {@code >}) will not be checked.</p>
 *
 * <p>Examples:</p>
 * <p>With <code>quoted-strings: {quote-type: any, required: true}</code>
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
 *
 * <p>With <code>quoted-strings: {quote-type: single, required: only-when-needed}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     foo: bar
 *     bar: foo
 *     not_number: '123'
 *     not_boolean: 'true'
 *     not_comment: '# comment'
 *     not_list: '[1, 2, 3]'
 *     not_map: '{a: 1, b: 2}'
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     foo: 'bar'
 * </pre>
 *
 * <p>With <code>quoted-strings: {required: false, extra-required: [^http://, ^ftp://]}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     - localhost
 *     - "localhost"
 *     - "http://localhost"
 *     - "ftp://localhost"
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     - http://localhost
 *     - ftp://localhost
 * </pre>
 *
 * <p>With <code>quoted-strings: {required: only-when-needed, extra-allowed: [^http://, ^ftp://], extra-required: [QUOTED]}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     - localhost
 *     - "http://localhost"
 *     - "ftp://localhost"
 *     - "this is a string that needs to be QUOTED"
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     - "localhost"
 *     - this is a string that needs to be QUOTED
 * </pre>
 */
public class QuotedStrings extends TokenRule {
    private static final String MSG_NOT_QUOTED             = "string value is not quoted";
    private static final String MSG_NOT_QUOTED_WITH_QUOTES = "string value is not quoted with %s quotes";

    private static final String OCTAL_INT_RE = "^([-+]?0b[0-1_]+|[-+]?0o?[0-7_]+|[-+]?0[0-7_]+|[-+]?(?:0|[1-9][0-9_]*)|[-+]?0x[0-9a-fA-F_]+|[-+]?[1-9][0-9_]*(?::[0-5]?[0-9])+)$";


    public static final String OPTION_QUOTE_TYPE     = "quote-type";
    public static final String OPTION_REQUIRED       = "required";
    public static final String OPTION_EXTRA_REQUIRED = "extra-required";
    public static final String OPTION_EXTRA_ALLOWED  = "extra-allowed";
    public static final String QUOTE_STYLE_SINGLE    = "single";
    public static final String QUOTE_STYLE_DOUBLE    = "double";
    public static final String QUOTE_STYLE_ANY       = "any";
    public static final String ONLY_WHEN_NEEDED      = "only-when-needed";


    public QuotedStrings() {
        registerOption(OPTION_QUOTE_TYPE, Arrays.asList(QUOTE_STYLE_ANY, QUOTE_STYLE_SINGLE, QUOTE_STYLE_DOUBLE));
        registerOption(OPTION_REQUIRED, Arrays.asList(Boolean.class, String.class, ONLY_WHEN_NEEDED), true);
        registerListOption(OPTION_EXTRA_REQUIRED, Collections.<String>emptyList());
        registerListOption(OPTION_EXTRA_ALLOWED, Collections.<String>emptyList());
    }

    @Override
    public String validate(Map<String, Object> conf) {
        if (conf.get(OPTION_REQUIRED) instanceof Boolean) {
            boolean req = (boolean) conf.get(OPTION_REQUIRED);
            if (req && !((List<?>) conf.get(OPTION_EXTRA_ALLOWED)).isEmpty()) {
                return "cannot use both \"required: true\" and \"extra-allowed\"";
            }
            if (req && !((List<?>) conf.get(OPTION_EXTRA_REQUIRED)).isEmpty()) {
                return "cannot use both \"required: true\" and \"extra-required\"";
            }
            if (!req && !((List<?>) conf.get(OPTION_EXTRA_ALLOWED)).isEmpty()) {
                return "cannot use both \"required: false\" and \"extra-allowed\"";
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<LintProblem> check(Map<Object, Object> conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        if (!(token instanceof ScalarToken &&
                (prev instanceof BlockEntryToken || prev instanceof FlowEntryToken ||
                        prev instanceof FlowSequenceStartToken || prev instanceof TagToken ||
                        prev instanceof ValueToken))) {
            return problems;
        }

        // Ignore explicit types, e.g. !!str testtest or !!int 42
        if (prev instanceof TagToken && "!!".equals(((TagToken) prev).getValue().getHandle())) {
            return problems;
        }

        // Ignore numbers, booleans, etc.
        Resolver resolver = new Resolver();
        // https://stackoverflow.com/a/36514274
        resolver.addImplicitResolver(Tag.INT,
                Pattern.compile(OCTAL_INT_RE),
                "-+0123456789");
        Tag tag = resolver.resolve(NodeId.scalar, ((ScalarToken) token).getValue(), true);
        if (((ScalarToken) token).getPlain() && tag != Tag.STR) {
            return problems;
        }

        // Ignore multi-line strings
        if (!((ScalarToken) token).getPlain() && (
                ((ScalarToken) token).getStyle() == DumperOptions.ScalarStyle.LITERAL ||
                        ((ScalarToken) token).getStyle() == DumperOptions.ScalarStyle.FOLDED)) {
            return problems;
        }

        String quoteType = (String) conf.get(OPTION_QUOTE_TYPE);
        String msg = null;

        if (conf.get(OPTION_REQUIRED) instanceof Boolean && Boolean.TRUE.equals(conf.get(OPTION_REQUIRED))) {
            // Quotes are mandatory and need to match config
            if (((ScalarToken) token).getStyle() == DumperOptions.ScalarStyle.PLAIN || !quoteMatch(quoteType, ((ScalarToken) token).getStyle())) {
                msg = String.format(MSG_NOT_QUOTED_WITH_QUOTES, quoteType);
            }
        } else if (conf.get(OPTION_REQUIRED) instanceof Boolean && Boolean.FALSE.equals(conf.get(OPTION_REQUIRED))) {
            // Quotes are not mandatory but when used need to match config
            if (((ScalarToken) token).getStyle() != DumperOptions.ScalarStyle.PLAIN && !quoteMatch(quoteType, ((ScalarToken) token).getStyle())) {
                msg = String.format(MSG_NOT_QUOTED_WITH_QUOTES, quoteType);
            } else if (((ScalarToken) token).getStyle() == DumperOptions.ScalarStyle.PLAIN) {
                boolean isExtraRequired = ((List<String>)conf.get(OPTION_EXTRA_REQUIRED)).stream().anyMatch(
                        r -> Pattern.compile(r).matcher(((ScalarToken) token).getValue()).find());
                if (isExtraRequired) {
                    msg = MSG_NOT_QUOTED;
                }
            }
        } else if (ONLY_WHEN_NEEDED.equals(conf.get(OPTION_REQUIRED))) {
            // Quotes are not strictly needed here
            if (((ScalarToken) token).getStyle() != DumperOptions.ScalarStyle.PLAIN && tag == Tag.STR &&
                    ((ScalarToken) token).getValue() != null && !quotesAreNeeded(((ScalarToken) token).getValue())) {
                boolean isExtraRequired = ((List<String>)conf.get(OPTION_EXTRA_REQUIRED)).stream().anyMatch(
                        r -> Pattern.compile(r).matcher(((ScalarToken) token).getValue()).find());
                boolean isExtraAllowed = ((List<String>)conf.get(OPTION_EXTRA_ALLOWED)).stream().anyMatch(
                        r -> Pattern.compile(r).matcher(((ScalarToken) token).getValue()).find());
                if (!(isExtraRequired || isExtraAllowed)) {
                    msg = String.format("string value is redundantly quoted with %s quotes", quoteType);
                }
            }

            // But when used need to match config
            else if (((ScalarToken) token).getStyle() != DumperOptions.ScalarStyle.PLAIN &&
                    !quoteMatch(quoteType, ((ScalarToken) token).getStyle())) {
                msg = String.format(MSG_NOT_QUOTED_WITH_QUOTES, quoteType);
            }

            else if (((ScalarToken) token).getStyle() == DumperOptions.ScalarStyle.PLAIN) {
                boolean isExtraRequired = !((List<?>)conf.get(OPTION_EXTRA_REQUIRED)).isEmpty() &&
                        ((List<String>)conf.get(OPTION_EXTRA_REQUIRED)).stream().anyMatch(
                                r -> Pattern.compile(r).matcher(((ScalarToken) token).getValue()).find());
                if (isExtraRequired) {
                    msg = MSG_NOT_QUOTED;
                }
            }
        }

        if (msg != null) {
            problems.add(new LintProblem(token.getStartMark().getLine() + 1,
                    token.getStartMark().getColumn() + 1,
                    msg));
        }

        return problems;
    }


    private boolean quoteMatch(String quoteType, DumperOptions.ScalarStyle tokenStyle) {
        return (QUOTE_STYLE_ANY.equals(quoteType) ||
                (QUOTE_STYLE_SINGLE.equals(quoteType) && tokenStyle == DumperOptions.ScalarStyle.SINGLE_QUOTED) ||
                (QUOTE_STYLE_DOUBLE.equals(quoteType) && tokenStyle == DumperOptions.ScalarStyle.DOUBLE_QUOTED));
    }

    private boolean quotesAreNeeded(String string) {
        LintScanner loader = new LintScanner(new LintStreamReader("key: " + string));
        // Remove the 5 first tokens corresponding to 'key: ' (StreamStartToken, BlockMappingStartToken, KeyToken, ScalarToken(value = key), ValueToken)
        for (int i = 0; i < 5; i++) {
            loader.getToken();
        }
        try {
            Token a = loader.getToken();
            Token b = loader.getToken();
            return !(a instanceof ScalarToken && ((ScalarToken) a).getStyle() == DumperOptions.ScalarStyle.PLAIN &&
                    b instanceof BlockEndToken && string.equals(((ScalarToken) a).getValue()));
        } catch (ScannerException e) {
            return true;
        }
    }
}
