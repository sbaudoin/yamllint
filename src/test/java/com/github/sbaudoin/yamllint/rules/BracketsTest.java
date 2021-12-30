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

public class BracketsTest extends RuleTester {
    public void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("brackets: disable");
        check("---\n" +
                "array1: []\n" +
                "array2: [ ]\n" +
                "array3: [   a, b]\n" +
                "array4: [a, b, c ]\n" +
                "array5: [a, b, c ]\n" +
                "array6: [  a, b, c ]\n" +
                "array7: [   a, b, c ]\n", conf);
    }

    public void testForbid() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("brackets:", "  forbid: false");
        check("---\n" +
                "array: []\n", conf);
        check("---\n" +
                "array: [a, b]\n", conf);
        check("---\n" +
                "array: [\n" +
                "  a,\n" +
                "  b\n" +
                "]\n", conf);

        conf = getConfig("brackets:", "  forbid: true");
        check("---\n" +
                "array:\n" +
                "  - a\n" +
                "  - b\n", conf);
        check("---\n" +
                "array: []\n", conf, getLintProblem(2, 9));
        check("---\n" +
                "array: [a, b]\n", conf, getLintProblem(2, 9));
        check("---\n" +
                "array: [\n" +
                "  a,\n" +
                "  b\n" +
                "]\n", conf, getLintProblem(2, 9));

        conf = getConfig("brackets:", "  forbid: non-empty");
        check("---\n" +
                "array:\n" +
                "  - a\n" +
                "  - b\n", conf);
        check("---\n" +
                "array: []\n", conf);
        check("---\n" +
                "array: [\n\n" +
                "]\n", conf);
        check("---\n" +
                "array: [\n" +
                "# a comment\n" +
                "]\n", conf);
        check("---\n" +
                "array: [a, b]\n", conf, getLintProblem(2, 9));
        check("---\n" +
                "array: [\n" +
                "  a,\n" +
                "  b\n" +
                "]\n", conf, getLintProblem(2, 9));
    }

    public void testMinSpaces() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: 0",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "array: []\n", conf);

        conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "array: []\n", conf, getLintProblem(2, 9));
        check("---\n" +
                "array: [ ]\n", conf);
        check("---\n" +
                "array: [a, b]\n", conf, getLintProblem(2, 9), getLintProblem(2, 13));
        check("---\n" +
                "array: [ a, b ]\n", conf);
        check("---\n" +
                "array: [\n" +
                "  a,\n" +
                "  b\n" +
                "]\n", conf);

        conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: 3",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "array: [ a, b ]\n", conf,
                getLintProblem(2, 10), getLintProblem(2, 15));
        check("---\n" +
                "array: [   a, b   ]\n", conf);
    }

    public void testMaxSpaces() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("brackets:",
                "  max-spaces-inside: 0",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "array: []\n", conf);
        check("---\n" +
                "array: [ ]\n", conf, getLintProblem(2, 9));
        check("---\n" +
                "array: [a, b]\n", conf);
        check("---\n" +
                "array: [ a, b ]\n", conf,
                getLintProblem(2, 9), getLintProblem(2, 14));
        check("---\n" +
                "array: [   a, b   ]\n", conf,
                getLintProblem(2, 11), getLintProblem(2, 18));
        check("---\n" +
                "array: [\n" +
                "  a,\n" +
                "  b\n" +
                "]\n", conf);

        conf = getConfig("brackets:",
                "  max-spaces-inside: 3",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "array: [   a, b   ]\n", conf);
        check("---\n" +
                "array: [    a, b     ]\n", conf,
                getLintProblem(2, 12), getLintProblem(2, 21));
    }

    public void testMinAndMaxSpaces() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("brackets:",
                "  max-spaces-inside: 0",
                "  min-spaces-inside: 0",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "array: []\n", conf);
        check("---\n" +
                "array: [ ]\n", conf, getLintProblem(2, 9));
        check("---\n" +
                "array: [   a, b]\n", conf, getLintProblem(2, 11));

        conf = getConfig("brackets:",
                "  max-spaces-inside: 1",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "array: [a, b, c ]\n", conf, getLintProblem(2, 9));

        conf = getConfig("brackets:",
                "  max-spaces-inside: 2",
                "  min-spaces-inside: 0",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "array: [a, b, c ]\n", conf);
        check("---\n" +
                "array: [  a, b, c ]\n", conf);
        check("---\n" +
                "array: [   a, b, c ]\n", conf, getLintProblem(2, 11));
    }

    public void testMinSpacesEmpty() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 0",
                "  min-spaces-inside-empty: 0");
        check("---\n" +
                "array: []\n", conf);

        conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: 1");
        check("---\n" +
                "array: []\n", conf, getLintProblem(2, 9));
        check("---\n" +
                "array: [ ]\n", conf);

        conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: 3");
        check("---\n" +
                "array: []\n", conf, getLintProblem(2, 9));
        check("---\n" +
                "array: [   ]\n", conf);
    }

    public void testMaxSpacesEmpty() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 0",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "array: []\n", conf);
        check("---\n" +
                "array: [ ]\n", conf, getLintProblem(2, 9));

        conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 1",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "array: []\n", conf);
        check("---\n" +
                "array: [ ]\n", conf);
        check("---\n" +
                "array: [  ]\n", conf, getLintProblem(2, 10));

        conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 3",
                "  min-spaces-inside-empty: -1");
        check("---\n" +
                "array: []\n", conf);
        check("---\n" +
                "array: [   ]\n", conf);
        check("---\n" +
                "array: [    ]\n", conf, getLintProblem(2, 12));
    }

    public void testMinAndMaxSpacesEmpty() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 2",
                "  min-spaces-inside-empty: 1");
        check("---\n" +
                "array: []\n", conf, getLintProblem(2, 9));
        check("---\n" +
                "array: [ ]\n", conf);
        check("---\n" +
                "array: [  ]\n", conf);
        check("---\n" +
                "array: [   ]\n", conf, getLintProblem(2, 11));
    }

    public void testMixedEmptyNonempty() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: 0",
                "  min-spaces-inside-empty: 0");
        check("---\n" +
                "array: [ a, b ]\n", conf);
        check("---\n" +
                "array: [a, b]\n", conf,
                getLintProblem(2, 9), getLintProblem(2, 13));
        check("---\n" +
                "array: []\n", conf);
        check("---\n" +
                "array: [ ]\n", conf,
                getLintProblem(2, 9));

        conf = getConfig("brackets:",
                "  max-spaces-inside: 0",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 1",
                "  min-spaces-inside-empty: 1");
        check("---\n" +
                        "array: [ a, b ]\n", conf,
                getLintProblem(2, 9), getLintProblem(2, 14));
        check("---\n" +
                "array: [a, b]\n", conf);
        check("---\n" +
                "array: []\n", conf,
                getLintProblem(2, 9));
        check("---\n" +
                "array: [ ]\n", conf);

        conf = getConfig("brackets:",
                "  max-spaces-inside: 2",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: 1",
                "  min-spaces-inside-empty: 1");
        check("---\n" +
                "array: [ a, b  ]\n", conf);
        check("---\n" +
                "array: [a, b   ]\n", conf,
                getLintProblem(2, 9), getLintProblem(2, 15));
        check("---\n" +
                "array: []\n", conf,
                getLintProblem(2, 9));
        check("---\n" +
                "array: [ ]\n", conf);
        check("---\n" +
                "array: [   ]\n", conf,
                getLintProblem(2, 11));

        conf = getConfig("brackets:",
                "  max-spaces-inside: 1",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: 1",
                "  min-spaces-inside-empty: 1");
        check("---\n" +
                "array: [ a, b ]\n", conf);
        check("---\n" +
                "array: [a, b]\n", conf,
                getLintProblem(2, 9), getLintProblem(2, 13));
        check("---\n" +
                "array: []", conf,
                getLintProblem(2, 9));
        check("---\n" +
                "array: [ ]\n", conf);
    }
}