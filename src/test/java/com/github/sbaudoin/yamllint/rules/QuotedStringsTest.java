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

import com.github.sbaudoin.yamllint.YamlLintConfig;
import com.github.sbaudoin.yamllint.YamlLintConfigException;

import java.io.IOException;

public class QuotedStringsTest extends RuleTester {
    public void testDisabled() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("quoted-strings: disable");
        check("---\n" +
                "foo: bar\n", conf);
        check("---\n" +
                "foo: \"bar\"\n", conf);
        check("---\n" +
                "foo: 'bar'\n", conf);
        check("---\n" +
                "bar: 123\n", conf);
    }

    public void testQuoteTypeAny() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("quoted-strings: {quote-type: any}");
        check("---\n" +
                "boolean1: true\n" +
                "number1: 123\n" +
                "string1: foo\n" +                          // fails
                "string2: \"foo\"\n" +
                "string3: 'bar'\n" +
                "string4: !!str genericstring\n" +
                "string5: !!str 456\n" +
                "string6: !!str \"quotedgenericstring\"\n" +
                "binary: !!binary Ymluc3RyaW5n\n" +
                "integer: !!int \"12\"\n" +
                "boolean2: !!bool boolstring\n" +
                "boolean3: !!bool \"quotedboolstring\"\n", conf, getLintProblem(4, 10));
        check("---\n" +
                "multiline string 1: |\n" +
                "  line 1\n" +
                "  line 2\n" +
                "multiline string 2: >\n" +
                "  word 1\n" +
                "  word 2\n" +
                "multiline string 3:\n" +
                "  word 1\n" +
                "  word 2\n" +
                "multiline string 4:\n" +
                "  \"word 1\\\n" +
                "   word 2\"\n", conf, getLintProblem(9, 3));
    }

    public void testQuoteTypeSingle() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("quoted-strings: {quote-type: single}");
        check("---\n" +
                "boolean1: true\n" +
                "number1: 123\n" +
                "string1: foo\n" +                          // fails
                "string2: \"foo\"\n" +                        // fails
                "string3: 'bar'\n" +
                "string4: !!str genericstring\n" +
                "string5: !!str 456\n" +
                "string6: !!str \"quotedgenericstring\"\n" +
                "binary: !!binary Ymluc3RyaW5n\n" +
                "integer: !!int \"12\"\n" +
                "boolean2: !!bool boolstring\n" +
                "boolean3: !!bool \"quotedboolstring\"\n", conf,
                getLintProblem(4, 10), getLintProblem(5, 10));
        check("---\n" +
                "multiline string 1: |\n" +
                "  line 1\n" +
                "  line 2\n" +
                "multiline string 2: >\n" +
                "  word 1\n" +
                "  word 2\n" +
                "multiline string 3:\n" +
                "  word 1\n" +
                "  word 2\n" +
                "multiline string 4:\n" +
                "  \"word 1\\\n" +
                "   word 2\"\n", conf,
                getLintProblem(9, 3), getLintProblem(12, 3));
    }

    public void testQuoteTypeDouble() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("quoted-strings: {quote-type: double}");
        check("---\n" +
                "boolean1: true\n" +
                "number1: 123\n" +
                "string1: foo\n" +                          // fails
                "string2: \"foo\"\n" +
                "string3: 'bar'\n" +                      // fails
                "string4: !!str genericstring\n" +
                "string5: !!str 456\n" +
                "string6: !!str \"quotedgenericstring\"\n" +
                "binary: !!binary Ymluc3RyaW5n\n" +
                "integer: !!int \"12\"\n" +
                "boolean2: !!bool boolstring\n" +
                "boolean3: !!bool \"quotedboolstring\"\n", conf,
                getLintProblem(4, 10), getLintProblem(6, 10));
        check("---\n" +
                "multiline string 1: |\n" +
                "  line 1\n" +
                "  line 2\n" +
                "multiline string 2: >\n" +
                "  word 1\n" +
                "  word 2\n" +
                "multiline string 3:\n" +
                "  word 1\n" +
                "  word 2\n" +
                "multiline string 4:\n" +
                "  \"word 1\\\n" +
                "   word 2\"\n", conf,
                getLintProblem(9, 3));
    }
}