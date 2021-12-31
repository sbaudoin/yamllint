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
import com.github.sbaudoin.yamllint.Parser;
import org.yaml.snakeyaml.scanner.ScannerException;
import org.yaml.snakeyaml.tokens.BlockMappingStartToken;
import org.yaml.snakeyaml.tokens.ScalarToken;
import org.yaml.snakeyaml.tokens.Token;
import org.yaml.snakeyaml.tokens.ValueToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Use this rule to set a limit to lines length.
 * <p>Options:</p>
 * <ul>
 *     <li>{@code max} defines the maximal (inclusive) length of lines.</li>
 *     <li>{@code allow-non-breakable-words} is used to allow non breakable words (without
 *     spaces inside) to overflow the limit. This is useful for long URLs, for instance.
 *     Use {@code true} to allow, {@code false} to forbid.</li>
 *     <li>{@code allow-non-breakable-inline-mappings} implies {@code allow-non-breakable-words}
 *     and extends it to also allow non-breakable words in inline mappings.</li>
 * </ul>
 *
 * <p>Examples:</p>
 * <p>With <code>line-length: {max: 70}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     long sentence:
 *       Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do
 *       eiusmod tempor incididunt ut labore et dolore magna aliqua.
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     long sentence:
 *       Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod
 *       tempor incididunt ut labore et dolore magna aliqua.
 * </pre>
 *
 * <p>With <code>line-length: {max: 60, allow-non-breakable-words: true}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     this:
 *       is:
 *         - a:
 *             http://localhost/very/very/very/very/very/very/very/very/long/url
 *
 *     # this comment is too long,
 *     # but hard to split:
 *     # http://localhost/another/very/very/very/very/very/very/very/very/long/url
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     - this line is waaaaaaaaaaaaaay too long but could be easily split...
 * </pre>
 * and the following code snippet would also **FAIL**:
 * <pre>
 *     - foobar: http://localhost/very/very/very/very/very/very/very/very/long/url
 * </pre>
 *
 * <p>With <code>line-length: {max: 60, allow-non-breakable-words: true, allow-non-breakable-inline-mappings: true}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     - foobar: http://localhost/very/very/very/very/very/very/very/very/long/url
 * </pre>
 *
 * <p>With <code>line-length: {max: 60, allow-non-breakable-words: false}</code>
 * the following code snippet would **FAIL**:
 * <pre>
 *     this:
 *       is:
 *         - a:
 *             http://localhost/very/very/very/very/very/very/very/very/long/url
 * </pre>
 */
public class LineLength extends LineRule {
    public static final String OPTION_MAX                                 = "max";
    public static final String OPTION_ALLOW_NON_BREAKABLE_WORDS           = "allow-non-breakable-words";
    public static final String OPTION_ALLOW_NON_BREAKABLE_INLINE_MAPPINGS = "allow-non-breakable-inline-mappings";


    public LineLength() {
        registerOption(OPTION_MAX, 80);
        registerOption(OPTION_ALLOW_NON_BREAKABLE_WORDS, true);
        registerOption(OPTION_ALLOW_NON_BREAKABLE_INLINE_MAPPINGS, false);
    }

    @Override
    public List<LintProblem> check(Map<Object, Object> conf, Parser.Line line) {
        List<LintProblem> problems = new ArrayList<>();

        if (line.getEnd() - line.getStart() > (int)conf.get("max")) {
            boolean anbw = (boolean)conf.get(OPTION_ALLOW_NON_BREAKABLE_WORDS);
            conf.put(OPTION_ALLOW_NON_BREAKABLE_WORDS, anbw || (boolean)conf.get(OPTION_ALLOW_NON_BREAKABLE_INLINE_MAPPINGS));
            if ((boolean)conf.get(OPTION_ALLOW_NON_BREAKABLE_WORDS)) {
                int start = line.getStart();
                while (start < line.getEnd() && line.getBuffer().charAt(start) == ' ') {
                    start += 1;
                }

                if (start != line.getEnd()) {
                    if (line.getBuffer().charAt(start) == '#') {
                        while (line.getBuffer().charAt(start) == '#') {
                            start++;
                        }
                        start++;
                    } else if (line.getBuffer().charAt(start) == '-') {
                        start += 2;
                    }

                    if (line.getBuffer().substring(start, line.getEnd()).indexOf(' ') == -1) {
                        return problems;
                    }

                    if ((boolean)conf.get(OPTION_ALLOW_NON_BREAKABLE_INLINE_MAPPINGS) && checkInlineMapping(line)) {
                        return problems;
                    }
                }
            }

            problems.add(new LintProblem(line.getLineNo(), (int)conf.get(OPTION_MAX) + 1,
                    "line too long (" + (line.getEnd() - line.getStart()) + " > " + conf.get(OPTION_MAX) + " characters)"));
        }

        return problems;
    }

    private boolean checkInlineMapping(Parser.Line line) {
        LintScanner loader = new LintScanner(new LintStreamReader(line.getContent()));
        try {
            while (loader.peekToken() != null) {
                if (loader.getToken() instanceof BlockMappingStartToken) {
                    while (loader.peekToken() != null) {
                        if (loader.getToken() instanceof ValueToken) {
                            Token t = loader.getToken();
                            if (t instanceof ScalarToken) {
                                return (line.getContent().indexOf(' ', t.getStartMark().getColumn()) < 0);
                            }
                        }
                    }
                }
            }
        } catch (ScannerException e) {
            // Do nothing
        }

        return false;
    }
}
