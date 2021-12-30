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

public class BracesTest extends RuleTester {
    public void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("braces: disable");
        check("---\n" +
                "dict1: {}\n" +
                "dict2: { }\n" +
                "dict3: {   a: 1, b}\n" +
                "dict4: {a: 1, b, c: 3 }\n" +
                "dict5: {a: 1, b, c: 3 }\n" +
                "dict6: {  a: 1, b, c: 3 }\n" +
                "dict7: {   a: 1, b, c: 3 }\n", conf);
    }

    public void testForbid() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("braces:", "  forbid: false");
        check("---\n" +
                "dict: {}\n", conf);
        check("---\n" +
                "dict: {a}\n", conf);
        check("---\n" +
                "dict: {a: 1}\n", conf);
        check("---\n" +
                "dict: {\n" +
                "  a: 1\n" +
                "}\n", conf);

        conf = getConfig("braces:", "  forbid: true");
        check("---\n" +
                "dict:\n" +
                "  a: 1\n", conf);
        check("---\n" +
                "dict: {}\n", conf, getLintProblem(2, 8));
        check("---\n" +
                "dict: {a}\n", conf, getLintProblem(2, 8));
        check("---\n" +
                "dict: {a: 1}\n", conf, getLintProblem(2, 8));
        check("---\n" +
                "dict: {\n" +
                "  a: 1\n" +
                "}\n", conf, getLintProblem(2, 8));

        conf = getConfig("braces:", "  forbid: non-empty");
        check("---\n" +
                "dict:\n" +
                "  a: 1\n", conf);
        check("---\n" +
                "dict: {}\n", conf);
        check("---\n" +
                "dict: {\n" +
                "}\n", conf);
        check("---\n" +
                "dict: {\n" +
                "# commented: value\n" +
                "# another: value2\n" +
                "}\n", conf);
        check("---\n" +
                "dict: {a}\n", conf, getLintProblem(2, 8));
        check("---\n" +
                "dict: {a: 1}\n", conf, getLintProblem(2, 8));
        check("---\n" +
                "dict: {\n" +
                "  a: 1\n" +
                "}\n", conf, getLintProblem(2, 8));
    }

    public void testMinSpaces() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: 0",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "dict: {}\n", conf);

        conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "dict: {}\n", conf, getLintProblem(2, 8));
        check("---\n" +
                "dict: { }\n", conf);
        check("---\n" +
                "dict: {a: 1, b}\n", conf,
                getLintProblem(2, 8), getLintProblem(2, 15));
        check("---\n" +
                "dict: { a: 1, b }\n", conf);
        check("---\n" +
                "dict: {\n" +
                "  a: 1,\n" +
                "  b\n" +
                "}\n", conf);

        conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: 3",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "dict: { a: 1, b }\n", conf,
                getLintProblem(2, 9), getLintProblem(2, 17));
        check("---\n" +
                "dict: {   a: 1, b   }\n", conf);
    }

    public void testMaxSpaces() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("braces:",
                "  max-spaces-inside: 0",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "dict: {}\n", conf);
        check("---\n" +
                "dict: { }\n", conf, getLintProblem(2, 8));
        check("---\n" +
                "dict: {a: 1, b}\n", conf);
        check("---\n" +
                "dict: { a: 1, b }\n", conf,
                getLintProblem(2, 8), getLintProblem(2, 16));
        check("---\n" +
                "dict: {   a: 1, b   }\n", conf,
                getLintProblem(2, 10), getLintProblem(2, 20));
        check("---\n" +
                "dict: {\n" +
                "  a: 1,\n" +
                "  b\n" +
                "}\n", conf);

        conf = getConfig("braces:",
                "  max-spaces-inside: 3",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "dict: {   a: 1, b   }\n", conf);
        check("---\n" +
                "dict: {    a: 1, b     }\n", conf,
                getLintProblem(2, 11), getLintProblem(2, 23));
    }

    public void testMinAndMaxSpaces() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("braces:",
                "  max-spaces-inside: 0",
                "  min-spaces-inside: 0",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "dict: {}\n", conf);
        check("---\n" +
                "dict: { }\n", conf, getLintProblem(2, 8));
        check("---\n" +
                "dict: {   a: 1, b}\n", conf, getLintProblem(2, 10));

        conf = getConfig("braces:",
                "  max-spaces-inside: 1",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "dict: {a: 1, b, c: 3 }\n", conf, getLintProblem(2, 8));

        conf = getConfig("braces:",
                "  max-spaces-inside: 2",
                "  min-spaces-inside: 0",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "dict: {a: 1, b, c: 3 }\n", conf);
        check("---\n" +
                "dict: {  a: 1, b, c: 3 }\n", conf);
        check("---\n" +
                "dict: {   a: 1, b, c: 3 }\n", conf, getLintProblem(2, 10));
    }

    public void testMinSpacesEmpty() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 0",
                "  min-spaces-inside-empty: 0");
        check("---\n" +
                "array: {}\n", conf);

        conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: 1");
        check("---\n" +
                "array: {}\n", conf, getLintProblem(2, 9));
        check("---\n" +
                "array: { }\n", conf);

        conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: 3");
        check("---\n" +
                "array: {}\n", conf, getLintProblem(2, 9));
        check("---\n" +
                "array: {   }\n", conf);
    }

    public void testMaxSpacesEmpty() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 0",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "array: {}\n", conf);
        check("---\n" +
                "array: { }\n", conf, getLintProblem(2, 9));

        conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 1",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "array: {}\n", conf);
        check("---\n" +
                "array: { }\n", conf);
        check("---\n" +
                "array: {  }\n", conf, getLintProblem(2, 10));

        conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 3",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "array: {}\n", conf);
        check("---\n" +
                "array: {   }\n", conf);
        check("---\n" +
                "array: {    }\n", conf, getLintProblem(2, 12));
    }

    public void testMinAndMaxSpacesEmpty() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 2",
                "  min-spaces-inside-empty: 1");
        check("---\n" +
                "array: {}\n", conf, getLintProblem(2, 9));
        check("---\n" +
                "array: { }\n", conf);
        check("---\n" +
                "array: {  }\n", conf);
        check("---\n" +
                "array: {   }\n", conf, getLintProblem(2, 11));
    }

    public void testMixedEmptyNonempty() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: 0",
                "  min-spaces-inside-empty: 0");
        check("---\n" +
                "array: { a: 1, b }\n", conf);
        check("---\n" +
                        "array: {a: 1, b}\n", conf,
                getLintProblem(2, 9), getLintProblem(2, 16));
        check("---\n" +
                "array: {}\n", conf);
        check("---\n" +
                        "array: { }\n", conf,
                getLintProblem(2, 9));

        conf = getConfig("braces:",
                "  max-spaces-inside: 0",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 1",
                "  min-spaces-inside-empty: 1");
        check("---\n" +
                        "array: { a: 1, b }\n", conf,
                getLintProblem(2, 9), getLintProblem(2, 17));
        check("---\n" +
                "array: {a: 1, b}\n", conf);
        check("---\n" +
                        "array: {}\n", conf,
                getLintProblem(2, 9));
        check("---\n" +
                "array: { }\n", conf);

        conf = getConfig("braces:",
                "  max-spaces-inside: 2",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: 1",
                "  min-spaces-inside-empty: 1");
        check("---\n" +
                "array: { a: 1, b  }\n", conf);
        check("---\n" +
                        "array: {a: 1, b   }\n", conf,
                getLintProblem(2, 9), getLintProblem(2, 18));
        check("---\n" +
                        "array: {}\n", conf,
                getLintProblem(2, 9));
        check("---\n" +
                "array: { }\n", conf);
        check("---\n" +
                        "array: {   }\n", conf,
                getLintProblem(2, 11));

        conf = getConfig("braces:",
                "  max-spaces-inside: 1",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: 1",
                "  min-spaces-inside-empty: 1");
        check("---\n" +
                "array: { a: 1, b }\n", conf);
        check("---\n" +
                "array: {a: 1, b}\n", conf,
                getLintProblem(2, 9), getLintProblem(2, 16));
        check("---\n" +
                "array: {}\n", conf,
                getLintProblem(2, 9));
        check("---\n" +
                "array: { }\n", conf);
    }
}