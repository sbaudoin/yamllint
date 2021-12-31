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

public class QuotedStringsTest extends RuleTester {
    public void testDisabled() throws YamlLintConfigException {
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

    public void testQuoteTypeAny() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("quoted-strings: {quote-type: any}");

        check("---\n" +
                "boolean1: true\n" +
                "number1: 123\n" +
                "string1: foo\n" +                          // fails
                "string2: \"foo\"\n" +
                "string3: \"true\"\n" +
                "string4: \"123\"\n" +
                "string5: 'bar'\n" +
                "string4: !!str genericstring\n" +
                "string5: !!str 456\n" +
                "string6: !!str \"quotedgenericstring\"\n" +
                "binary: !!binary Ymluc3RyaW5n\n" +
                "integer: !!int intstring\n" +
                "boolean2: !!bool boolstring\n" +
                "boolean3: !!bool \"quotedboolstring\"\n" +
                "block-seq:\n" +
                "  - foo\n" +                               // fails
                "  - \"foo\"\n" +
                "flow-seq: [foo, \"foo\"]\n" +              // fails
                "flow-map: {a: foo, b: \"foo\"}\n",         // fails
                conf,
                getLintProblem(4, 10), getLintProblem(17, 5),
                getLintProblem(19, 12), getLintProblem(20, 15));
        check("---\n" +
                "multiline string 1: |\n" +
                "  line 1\n" +
                "  line 2\n" +
                "multiline string 2: >\n" +
                "  word 1\n" +
                "  word 2\n" +
                "multiline string 3:\n" +
                "  word 1\n" +               // fails
                "  word 2\n" +
                "multiline string 4:\n" +
                "  \"word 1\\\n" +
                "   word 2\"\n", conf, getLintProblem(9, 3));
    }

    public void testQuoteTypeSingle() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("quoted-strings: {quote-type: single}");

        check("---\n" +
                "boolean1: true\n" +
                "number1: 123\n" +
                "string1: foo\n" +                          // fails
                "string2: \"foo\"\n" +                      // fails
                "string3: \"true\"\n" +                     // fails
                "string4: \"123\"\n" +                      // fails
                "string5: 'bar'\n" +
                "string4: !!str genericstring\n" +
                "string5: !!str 456\n" +
                "string6: !!str \"quotedgenericstring\"\n" +
                "binary: !!binary Ymluc3RyaW5n\n" +
                "integer: !!int intstring\n" +
                "boolean2: !!bool boolstring\n" +
                "boolean3: !!bool \"quotedboolstring\"\n" +
                "block-seq:\n" +
                "  - foo\n" +                               // fails
                "  - \"foo\"\n" +                           // fails
                "flow-seq: [foo, \"foo\"]\n" +              // fails
                "flow-map: {a: foo, b: \"foo\"}\n",         // fails
                conf,
                getLintProblem(4, 10), getLintProblem(5, 10),
                getLintProblem(6, 10), getLintProblem(7, 10),
                getLintProblem(17, 5), getLintProblem(18, 5),
                getLintProblem(19, 12), getLintProblem(19, 17),
                getLintProblem(20, 15), getLintProblem(20, 23));
        check("---\n" +
                "multiline string 1: |\n" +
                "  line 1\n" +
                "  line 2\n" +
                "multiline string 2: >\n" +
                "  word 1\n" +
                "  word 2\n" +
                "multiline string 3:\n" +
                "  word 1\n" +               // fails
                "  word 2\n" +
                "multiline string 4:\n" +
                "  \"word 1\\\n" +           // fails
                "   word 2\"\n", conf,
                getLintProblem(9, 3), getLintProblem(12, 3));
    }

    public void testQuoteTypeDouble() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("quoted-strings: {quote-type: double}");

        check("---\n" +
                        "boolean1: true\n" +
                        "number1: 123\n" +
                        "string1: foo\n" +                          // fails
                        "string2: \"foo\"\n" +
                        "string3: \"true\"\n" +
                        "string4: \"123\"\n" +
                        "string5: 'bar'\n" +                        // fails
                        "string4: !!str genericstring\n" +
                        "string5: !!str 456\n" +
                        "string6: !!str \"quotedgenericstring\"\n" +
                        "binary: !!binary Ymluc3RyaW5n\n" +
                        "integer: !!int intstring\n" +
                        "boolean2: !!bool boolstring\n" +
                        "boolean3: !!bool \"quotedboolstring\"\n" +
                        "block-seq:\n" +
                        "  - foo\n" +                               // fails
                        "  - \"foo\"\n" +
                        "flow-seq: [foo, \"foo\"]\n" +              // fails
                        "flow-map: {a: foo, b: \"foo\"}\n",         // fails
                conf,
                getLintProblem(4, 10), getLintProblem(8, 10),
                getLintProblem(17, 5), getLintProblem(19, 12),
                getLintProblem(20, 15));
        check("---\n" +
                        "multiline string 1: |\n" +
                        "  line 1\n" +
                        "  line 2\n" +
                        "multiline string 2: >\n" +
                        "  word 1\n" +
                        "  word 2\n" +
                        "multiline string 3:\n" +
                        "  word 1\n" +               // fails
                        "  word 2\n" +
                        "multiline string 4:\n" +
                        "  \"word 1\\\n" +
                        "   word 2\"\n", conf,
                getLintProblem(9, 3));
    }

    public void testAnyQuotesNotRequired() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("quoted-strings: {quote-type: any, required: false}");

        check("---\n" +
               "boolean1: true\n" +
               "number1: 123\n" +
               "string1: foo\n" +
               "string2: \"foo\"\n" +
               "string3: \"true\"\n" +
               "string4: \"123\"\n" +
               "string5: 'bar'\n" +
               "string6: !!str genericstring\n" +
               "string7: !!str 456\n" +
               "string8: !!str \"quotedgenericstring\"\n" +
               "binary: !!binary binstring\n" +
               "integer: !!int intstring\n" +
               "boolean2: !!bool boolstring\n" +
               "boolean3: !!bool \"quotedboolstring\"\n" +
               "block-seq:\n" +
               "  - foo\n" +
               "  - \"foo\"\n" +
               "flow-seq: [foo, \"foo\"]\n" +
               "flow-map: {a: foo, b: \"foo\"}\n",
               conf);
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
               "   word 2\"\n",
               conf);
    }

    public void testSingleQuotesNotRequired() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("quoted-strings: {quote-type: single, required: false}");

        check("---\n" +
                "boolean1: true\n" +
                "number1: 123\n" +
                "string1: foo\n" +
                "string2: \"foo\"\n" +                        // fails
                "string3: \"true\"\n" +                       // fails
                "string4: \"123\"\n" +                        // fails
                "string5: 'bar'\n" +
                "string6: !!str genericstring\n" +
                "string7: !!str 456\n" +
                "string8: !!str \"quotedgenericstring\"\n" +
                "binary: !!binary binstring\n" +
                "integer: !!int intstring\n" +
                "boolean2: !!bool boolstring\n" +
                "boolean3: !!bool \"quotedboolstring\"\n" +
                "block-seq:\n" +
                "  - foo\n" +                                 // fails
                "  - \"foo\"\n" +
                "flow-seq: [foo, \"foo\"]\n" +                // fails
                "flow-map: {a: foo, b: \"foo\"}\n",           // fails
                conf,
                getLintProblem(5, 10), getLintProblem(6, 10),
                getLintProblem(7, 10), getLintProblem(18, 5),
                getLintProblem(19, 17), getLintProblem(20, 23));
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
                "  \"word 1\\\n" +            // fails
                "   word 2\"\n",
                conf, getLintProblem(12, 3));
    }

    public void testOnlyWhenNeeded() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("quoted-strings: {required: only-when-needed}");

        check("---\n" +
                "boolean1: true\n" +
                "number1: 123\n" +
                "string1: foo\n" +
                "string2: \"foo\"\n" +                        // fails
                "string3: \"true\"\n" +
                "string4: \"123\"\n" +
                "string5: 'bar'\n" +                          // fails
                "string6: !!str genericstring\n" +
                "string7: !!str 456\n" +
                "string8: !!str \"quotedgenericstring\"\n" +
                "binary: !!binary binstring\n" +
                "integer: !!int intstring\n" +
                "boolean2: !!bool boolstring\n" +
                "boolean3: !!bool \"quotedboolstring\"\n" +
                "block-seq:\n" +
                "  - foo\n" +
                "  - \"foo\"\n" +                             // fails
                "flow-seq: [foo, \"foo\"]\n" +                // fails
                "flow-map: {a: foo, b: \"foo\"}\n",           // fails
                conf,
                getLintProblem(5, 10), getLintProblem(8, 10),
                getLintProblem(18, 5), getLintProblem(19, 17),
                getLintProblem(20, 23));
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
                "  \"word 1\\\n" +            // fails
                "   word 2\"\n",
                conf, getLintProblem(12, 3));
    }

    public void testOnlyWhenNeededSingleQuotes() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("quoted-strings: {quote-type: single,",
                        "                 required: only-when-needed}");

        check("---\n" +
                "boolean1: true\n" +
                "number1: 123\n" +
                "string1: foo\n" +
                "string2: \"foo\"\n" +                        // fails
                "string3: \"true\"\n" +                       // fails
                "string4: \"123\"\n" +                        // fails
                "string5: 'bar'\n" +                          // fails
                "string6: !!str genericstring\n" +
                "string7: !!str 456\n" +
                "string8: !!str \"quotedgenericstring\"\n" +
                "binary: !!binary binstring\n" +
                "integer: !!int intstring\n" +
                "boolean2: !!bool boolstring\n" +
                "boolean3: !!bool \"quotedboolstring\"\n" +
                "block-seq:\n" +
                "  - foo\n" +
                "  - \"foo\"\n" +                             // fails
                "flow-seq: [foo, \"foo\"]\n" +                // fails
                "flow-map: {a: foo, b: \"foo\"}\n",           // fails
                conf,
               getLintProblem(5, 10), getLintProblem(6, 10),
               getLintProblem(7, 10), getLintProblem(8, 10),
               getLintProblem(18, 5), getLintProblem(19, 17),
               getLintProblem(20, 23));
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
                "  \"word 1\\\n" +            // fails
                "   word 2\"\n",
                conf, getLintProblem(12, 3));
    }

    public void testOnlyWwhenNeededCornerCases() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("quoted-strings: {required: only-when-needed}");

        check("---\n" +
                "- \"\"\n" +
                "- \"- item\"\n" +
                "- \"key: value\"\n" +
                "- \"%H:%M:%S\"\n" +
                "- \"%wheel ALL=(ALL) NOPASSWD: ALL\"\n" +
                "- '\"quoted\"'\n" +
                "- \"'foo' == 'bar'\"\n" +
                "- \"'Mac' in ansible_facts.product_name\"\n" +
                "- 'foo # bar'\n",
                conf);
        check("---\n" +
                "k1: \"\"\n" +
                "k2: \"- item\"\n" +
                "k3: \"key: value\"\n" +
                "k4: \"%H:%M:%S\"\n" +
                "k5: \"%wheel ALL=(ALL) NOPASSWD: ALL\"\n" +
                "k6: '\"quoted\"'\n" +
                "k7: \"'foo' == 'bar'\"\n" +
                "k8: \"'Mac' in ansible_facts.product_name\"\n",
                conf);

        check("---\n" +
                "- ---\n" +
                "- \"---\"\n" +                     // fails
                "- ----------\n" +
                "- \"----------\"\n" +              // fails
                "- :wq\n" +
                "- \":wq\"\n",                      // fails
                conf,
                getLintProblem(3, 3), getLintProblem(5, 3), getLintProblem(7, 3));
        check("---\n" +
                "k1: ---\n" +
                "k2: \"---\"\n" +                   // fails
                "k3: ----------\n" +
                "k4: \"----------\"\n" +            // fails
                "k5: :wq\n" +
                "k6: \":wq\"\n",                    // fails
                conf,
                getLintProblem(3, 5), getLintProblem(5, 5), getLintProblem(7, 5));
    }

    public void testOnlyWhenNeededExtras() throws YamlLintConfigException {
        YamlLintConfig conf;
        try {
            getConfig("quoted-strings:",
                    "  required: true", "  extra-allowed: [^http://]");
            fail("Invalid configuration accepted");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }

        try {
            getConfig("quoted-strings:",
                    "  required: true",
                    "  extra-required: [^http://]");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }

        try {
            getConfig("quoted-strings:",
                    "  required: false",
                    "  extra-allowed: [^http://]");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }

        conf = getConfig("quoted-strings:",
                "  required: true");
        check("---\n" +
                "- 123\n" +
                "- \"123\"\n" +
                "- localhost\n" +                  // fails
                "- \"localhost\"\n" +
                "- http://localhost\n" +           // fails
                "- \"http://localhost\"\n" +
                "- ftp://localhost\n" +            // fails
                "- \"ftp://localhost\"\n",
                conf,
                getLintProblem(4, 3), getLintProblem(6, 3), getLintProblem(8, 3));

        conf = getConfig("quoted-strings:",
                "  required: only-when-needed",
                "  extra-allowed: [^ftp://]",
                "  extra-required: [^http://]");
        check("---\n" +
                "- 123\n" +
                "- \"123\"\n" +
                "- localhost\n" +
                "- \"localhost\"\n" +              // fails
                "- http://localhost\n" +           // fails
                "- \"http://localhost\"\n" +
                "- ftp://localhost\n" +
                "- \"ftp://localhost\"\n",
                conf,
                getLintProblem(5, 3), getLintProblem(6, 3));

        conf = getConfig("quoted-strings:",
                "  required: false",
                "  extra-required: [^http://, ^ftp://]");
        check("---\n" +
                "- 123\n" +
                "- \"123\"\n" +
                "- localhost\n" +
                "- \"localhost\"\n" +
                "- http://localhost\n" +           // fails
                "- \"http://localhost\"\n" +
                "- ftp://localhost\n" +            // fails
                "- \"ftp://localhost\"\n",
                conf,
                getLintProblem(6, 3), getLintProblem(8, 3));

        conf = getConfig("quoted-strings:",
                "  required: only-when-needed",
                "  extra-allowed: [^ftp://, \";$\", \" \"]");
        check("---\n" +
                "- localhost\n" +
                "- \"localhost\"\n" +            // fails
                "- ftp://localhost\n" +
                "- \"ftp://localhost\"\n" +
                "- i=i+1\n" +
                "- \"i=i+1\"\n" +                // fails
                "- i=i+2;\n" +
                "- \"i=i+2;\"\n" +
                "- foo\n" +
                "- \"foo\"\n" +                  // fails
                "- foo bar\n" +
                "- \"foo bar\"\n",
                conf,
                getLintProblem(3, 3), getLintProblem(7, 3), getLintProblem(11, 3));
    }

    public void testOctalValues() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("quoted-strings: {required: true}");
        check("---\n" +
                "- 100\n" +
                "- 0100\n" +
                "- 0o100\n" +
                "- 777\n" +
                "- 0777\n" +
                "- 0o777\n" +
                "- 800\n" +
                "- 0800\n" +                     // fails
                "- 0o800\n" +                    // fails
                "- \"0800\"\n" +
                "- \"0o800\"\n",
                conf,
                getLintProblem(9, 3), getLintProblem(10, 3));
    }
}