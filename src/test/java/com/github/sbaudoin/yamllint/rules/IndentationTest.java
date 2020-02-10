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

public class IndentationTest extends RuleTester {
    public void testDisabled() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: disable");
        check("---\n" +
                "object:\n" +
                "   k1: v1\n" +
                "obj2:\n" +
                " k2:\n" +
                "     - 8\n" +
                " k3:\n" +
                "           val\n" +
                "...\n", conf);
        check("---\n" +
                "  o:\n" +
                "    k1: v1\n" +
                "  p:\n" +
                "   k3:\n" +
                "       val\n" +
                "...\n", conf);
        check("---\n" +
                "     - o:\n" +
                "         k1: v1\n" +
                "     - p: kdjf\n" +
                "     - q:\n" +
                "        k3:\n" +
                "              - val\n" +
                "...\n", conf);
    }

    public void testOneSpace() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: 1, indent-sequences: false, check-multi-line-strings: false}");
        check("---\n" +
                "object:\n" +
                " k1:\n" +
                " - a\n" +
                " - b\n" +
                " k2: v2\n" +
                " k3:\n" +
                " - name: Unix\n" +
                "   date: 1969\n" +
                " - name: Linux\n" +
                "   date: 1991\n" +
                "...\n", conf);

        conf = getConfig("indentation: {spaces: 1, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "object:\n" +
                " k1:\n" +
                "  - a\n" +
                "  - b\n" +
                " k2: v2\n" +
                " k3:\n" +
                "  - name: Unix\n" +
                "    date: 1969\n" +
                "  - name: Linux\n" +
                "    date: 1991\n" +
                "...\n", conf);
    }

    public void testTwoSpaces() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: 2, indent-sequences: false, check-multi-line-strings: false}");
        check("---\n" +
                "object:\n" +
                "  k1:\n" +
                "  - a\n" +
                "  - b\n" +
                "  k2: v2\n" +
                "  k3:\n" +
                "  - name: Unix\n" +
                "    date: 1969\n" +
                "  - name: Linux\n" +
                "    date: 1991\n" +
                "  k4:\n" +
                "  -\n" +
                "  k5: v3\n" +
                "...\n", conf);

        conf = getConfig("indentation: {spaces: 2, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "object:\n" +
                "  k1:\n" +
                "    - a\n" +
                "    - b\n" +
                "  k2: v2\n" +
                "  k3:\n" +
                "    - name: Unix\n" +
                "      date: 1969\n" +
                "    - name: Linux\n" +
                "      date: 1991\n" +
                "...\n", conf);
    }

    public void testThreeSpaces() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: 3, indent-sequences: false, check-multi-line-strings: false}");
        check("---\n" +
                "object:\n" +
                "   k1:\n" +
                "   - a\n" +
                "   - b\n" +
                "   k2: v2\n" +
                "   k3:\n" +
                "   - name: Unix\n" +
                "     date: 1969\n" +
                "   - name: Linux\n" +
                "     date: 1991\n" +
                "...\n", conf);

        conf = getConfig("indentation: {spaces: 3, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "object:\n" +
                "   k1:\n" +
                "      - a\n" +
                "      - b\n" +
                "   k2: v2\n" +
                "   k3:\n" +
                "      - name: Unix\n" +
                "        date: 1969\n" +
                "      - name: Linux\n" +
                "        date: 1991\n" +
                "...\n", conf);
    }

    public void testConsistentSpaces() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: whatever,",
                "              check-multi-line-strings: false}",
                "document-start: disable");
        check("---\n" +
                "object:\n" +
                " k1:\n" +
                "  - a\n" +
                "  - b\n" +
                " k2: v2\n" +
                " k3:\n" +
                "  - name: Unix\n" +
                "    date: 1969\n" +
                "  - name: Linux\n" +
                "    date: 1991\n" +
                "...\n", conf);
        check("---\n" +
                "object:\n" +
                "  k1:\n" +
                "  - a\n" +
                "  - b\n" +
                "  k2: v2\n" +
                "  k3:\n" +
                "  - name: Unix\n" +
                "    date: 1969\n" +
                "  - name: Linux\n" +
                "    date: 1991\n" +
                "...\n", conf);
        check("---\n" +
                "object:\n" +
                "   k1:\n" +
                "      - a\n" +
                "      - b\n" +
                "   k2: v2\n" +
                "   k3:\n" +
                "      - name: Unix\n" +
                "        date: 1969\n" +
                "      - name: Linux\n" +
                "        date: 1991\n" +
                "...\n", conf);
        check("first is not indented:\n" +
                "  value is indented\n", conf);
        check("first is not indented:\n" +
                "     value:\n" +
                "          is indented\n", conf);
        check("- first is already indented:\n" +
                "    value is indented too\n", conf);
        check("- first is already indented:\n" +
                "       value:\n" +
                "            is indented too\n", conf);
        check("- first is already indented:\n" +
                "       value:\n" +
                "             is indented too\n", conf, getLintProblem(3, 14));
        check("---\n" +
                "list one:\n" +
                "  - 1\n" +
                "  - 2\n" +
                "  - 3\n" +
                "list two:\n" +
                "    - a\n" +
                "    - b\n" +
                "    - c\n", conf, getLintProblem(7, 5));
        check("---\n" +
                "list one:\n" +
                "- 1\n" +
                "- 2\n" +
                "- 3\n" +
                "list two:\n" +
                "  - a\n" +
                "  - b\n" +
                "  - c\n", conf);
        check("---\n" +
                "list one:\n" +
                " - 1\n" +
                " - 2\n" +
                " - 3\n" +
                "list two:\n" +
                "- a\n" +
                "- b\n" +
                "- c\n", conf);
    }

    public void testConsistentSpacesAndIndentSequences() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "list one:\n" +
                "- 1\n" +
                "- 2\n" +
                "- 3\n" +
                "list two:\n" +
                "    - a\n" +
                "    - b\n" +
                "    - c\n", conf, getLintProblem(3, 1));
        check("---\n" +
                "list one:\n" +
                "  - 1\n" +
                "  - 2\n" +
                "  - 3\n" +
                "list two:\n" +
                "    - a\n" +
                "    - b\n" +
                "    - c\n", conf, getLintProblem(7, 5));
        check("---\n" +
                "list one:\n" +
                "  - 1\n" +
                "  - 2\n" +
                "  - 3\n" +
                "list two:\n" +
                "- a\n" +
                "- b\n" +
                "- c\n", conf, getLintProblem(7, 1));

        conf = getConfig("indentation: {spaces: consistent, indent-sequences: false, check-multi-line-strings: false}");
        check("---\n" +
                "list one:\n" +
                "- 1\n" +
                "- 2\n" +
                "- 3\n" +
                "list two:\n" +
                "    - a\n" +
                "    - b\n" +
                "    - c\n", conf, getLintProblem(7, 5));
        check("---\n" +
                "list one:\n" +
                "- 1\n" +
                "- 2\n" +
                "- 3\n" +
                "list two:\n" +
                "  - a\n" +
                "  - b\n" +
                "  - c\n", conf, getLintProblem(7, 3));
        check("---\n" +
                "list one:\n" +
                "  - 1\n" +
                "  - 2\n" +
                "  - 3\n" +
                "list two:\n" +
                "- a\n" +
                "- b\n" +
                "- c\n", conf, getLintProblem(3, 3));

        conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: consistent,",
                "              check-multi-line-strings: false}");
        check("---\n" +
                "list one:\n" +
                "- 1\n" +
                "- 2\n" +
                "- 3\n" +
                "list two:\n" +
                "    - a\n" +
                "    - b\n" +
                "    - c\n", conf, getLintProblem(7, 5));
        check("---\n" +
                "list one:\n" +
                "    - 1\n" +
                "    - 2\n" +
                "    - 3\n" +
                "list two:\n" +
                "- a\n" +
                "- b\n" +
                "- c\n", conf, getLintProblem(7, 1));
        check("---\n" +
                "list one:\n" +
                "- 1\n" +
                "- 2\n" +
                "- 3\n" +
                "list two:\n" +
                "- a\n" +
                "- b\n" +
                "- c\n", conf);
        check("---\n" +
                "list one:\n" +
                "  - 1\n" +
                "  - 2\n" +
                "  - 3\n" +
                "list two:\n" +
                "    - a\n" +
                "    - b\n" +
                "    - c\n", conf, getLintProblem(7, 5));

        conf = getConfig("indentation: {spaces: consistent, indent-sequences: whatever, check-multi-line-strings: false}");
        check("---\n" +
                "list one:\n" +
                "- 1\n" +
                "- 2\n" +
                "- 3\n" +
                "list two:\n" +
                "    - a\n" +
                "    - b\n" +
                "    - c\n", conf);
        check("---\n" +
                "list one:\n" +
                "    - 1\n" +
                "    - 2\n" +
                "    - 3\n" +
                "list two:\n" +
                "- a\n" +
                "- b\n" +
                "- c\n", conf);
        check("---\n" +
                "list one:\n" +
                "- 1\n" +
                "- 2\n" +
                "- 3\n" +
                "list two:\n" +
                "- a\n" +
                "- b\n" +
                "- c\n", conf);
        check("---\n" +
                "list one:\n" +
                "  - 1\n" +
                "  - 2\n" +
                "  - 3\n" +
                "list two:\n" +
                "    - a\n" +
                "    - b\n" +
                "    - c\n", conf, getLintProblem(7, 5));
    }

    public void testIndentSequencesWhatever() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: 4, indent-sequences: whatever, check-multi-line-strings: false}");
        check("---\n" +
                "list one:\n" +
                "- 1\n" +
                "- 2\n" +
                "- 3\n" +
                "list two:\n" +
                "    - a\n" +
                "    - b\n" +
                "    - c\n", conf);
        check("---\n" +
                "list one:\n" +
                "  - 1\n" +
                "  - 2\n" +
                "  - 3\n" +
                "list two:\n" +
                "    - a\n" +
                "    - b\n" +
                "    - c\n", conf, getLintProblem(3, 3));
        check("---\n" +
                "list one:\n" +
                "- 1\n" +
                "- 2\n" +
                "- 3\n" +
                "list two:\n" +
                "  - a\n" +
                "  - b\n" +
                "  - c\n", conf, getLintProblem(7, 3));
        check("---\n" +
                "list:\n" +
                "    - 1\n" +
                "    - 2\n" +
                "    - 3\n" +
                "- a\n" +
                "- b\n" +
                "- c\n", conf, getSyntaxError(6, 1));
    }

    public void testIndentSequencesConsistent() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: 4, indent-sequences: consistent, check-multi-line-strings: false}");
        check("---\n" +
                "list one:\n" +
                "- 1\n" +
                "- 2\n" +
                "- 3\n" +
                "list:\n" +
                "    two:\n" +
                "    - a\n" +
                "    - b\n" +
                "    - c\n", conf);
        check("---\n" +
                "list one:\n" +
                "    - 1\n" +
                "    - 2\n" +
                "    - 3\n" +
                "list:\n" +
                "    two:\n" +
                "        - a\n" +
                "        - b\n" +
                "        - c\n", conf);
        check("---\n" +
                "list one:\n" +
                "- 1\n" +
                "- 2\n" +
                "- 3\n" +
                "list two:\n" +
                "    - a\n" +
                "    - b\n" +
                "    - c\n", conf, getLintProblem(7, 5));
        check("---\n" +
                "list one:\n" +
                "    - 1\n" +
                "    - 2\n" +
                "    - 3\n" +
                "list two:\n" +
                "- a\n" +
                "- b\n" +
                "- c\n", conf, getLintProblem(7, 1));
        check("---\n" +
                "list one:\n" +
                " - 1\n" +
                " - 2\n" +
                " - 3\n" +
                "list two:\n" +
                "- a\n" +
                "- b\n" +
                "- c\n", conf, getLintProblem(3, 2), getLintProblem(7, 1));
    }

    public void testDirectFlows() throws IOException, YamlLintConfigException {
        // flow: [ ...
        // ]
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "a: {x: 1,\n" +
                "    y,\n" +
                "    z: 1}\n", conf);
        check("---\n" +
                "a: {x: 1,\n" +
                "   y,\n" +
                "    z: 1}\n", conf, getLintProblem(3, 4));
        check("---\n" +
                "a: {x: 1,\n" +
                "     y,\n" +
                "    z: 1}\n", conf, getLintProblem(3, 6));
        check("---\n" +
                "a: {x: 1,\n" +
                "  y, z: 1}\n", conf, getLintProblem(3, 3));
        check("---\n" +
                "a: {x: 1,\n" +
                "    y, z: 1\n" +
                "}\n", conf);
        check("---\n" +
                "a: {x: 1,\n" +
                "  y, z: 1\n" +
                "}\n", conf, getLintProblem(3, 3));
        check("---\n" +
                "a: [x,\n" +
                "    y,\n" +
                "    z]\n", conf);
        check("---\n" +
                "a: [x,\n" +
                "   y,\n" +
                "    z]\n", conf, getLintProblem(3, 4));
        check("---\n" +
                "a: [x,\n" +
                "     y,\n" +
                "    z]\n", conf, getLintProblem(3, 6));
        check("---\n" +
                "a: [x,\n" +
                "  y, z]\n", conf, getLintProblem(3, 3));
        check("---\n" +
                "a: [x,\n" +
                "    y, z\n" +
                "]\n", conf);
        check("---\n" +
                "a: [x,\n" +
                "  y, z\n" +
                "]\n", conf, getLintProblem(3, 3));
    }

    public void testBrokenFlows() throws IOException, YamlLintConfigException {
        // flow: [
        //   ...
        // ]
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "a: {\n" +
                "  x: 1,\n" +
                "  y, z: 1\n" +
                "}\n", conf);
        check("---\n" +
                "a: {\n" +
                "  x: 1,\n" +
                "  y, z: 1}\n", conf);
        check("---\n" +
                "a: {\n" +
                "   x: 1,\n" +
                "  y, z: 1\n" +
                "}\n", conf, getLintProblem(4, 3));
        check("---\n" +
                "a: {\n" +
                "  x: 1,\n" +
                "  y, z: 1\n" +
                "  }\n", conf, getLintProblem(5, 3));
        check("---\n" +
                "a: [\n" +
                "  x,\n" +
                "  y, z\n" +
                "]\n", conf);
        check("---\n" +
                "a: [\n" +
                "  x,\n" +
                "  y, z]\n", conf);
        check("---\n" +
                "a: [\n" +
                "   x,\n" +
                "  y, z\n" +
                "]\n", conf, getLintProblem(4, 3));
        check("---\n" +
                "a: [\n" +
                "  x,\n" +
                "  y, z\n" +
                "  ]\n", conf, getLintProblem(5, 3));
        check("---\n" +
                "obj: {\n" +
                "  a: 1,\n" +
                "   b: 2,\n" +
                " c: 3\n" +
                "}\n", conf, getLintProblem(4, 4), getLintProblem(5, 2));
        check("---\n" +
                "list: [\n" +
                "  1,\n" +
                "   2,\n" +
                " 3\n" +
                "]\n", conf, getLintProblem(4, 4), getLintProblem(5, 2));
        check("---\n" +
                "top:\n" +
                "  rules: [\n" +
                "    1, 2,\n" +
                "  ]\n", conf);
        check("---\n" +
                "top:\n" +
                "  rules: [\n" +
                "    1, 2,\n" +
                "]\n" +
                "  rulez: [\n" +
                "    1, 2,\n" +
                "    ]\n", conf, getLintProblem(5, 1), getLintProblem(8, 5));
        check("---\n" +
                "top:\n" +
                "  rules:\n" +
                "    here: {\n" +
                "      foo: 1,\n" +
                "      bar: 2\n" +
                "    }\n", conf);
        check("---\n" +
                "top:\n" +
                "  rules:\n" +
                "    here: {\n" +
                "      foo: 1,\n" +
                "      bar: 2\n" +
                "      }\n" +
                "    there: {\n" +
                "      foo: 1,\n" +
                "      bar: 2\n" +
                "  }\n", conf, getLintProblem(7, 7), getLintProblem(11, 3));

        conf = getConfig("indentation: {spaces: 2, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "a: {\n" +
                "   x: 1,\n" +
                "  y, z: 1\n" +
                "}\n", conf, getLintProblem(3, 4));
        check("---\n" +
                "a: [\n" +
                "   x,\n" +
                "  y, z\n" +
                "]\n", conf, getLintProblem(3, 4));
    }

    public void testClearedFlows() throws IOException, YamlLintConfigException {
        // flow:
        //   [
        //     ...
        //   ]
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "top:\n" +
                "  rules:\n" +
                "    {\n" +
                "      foo: 1,\n" +
                "      bar: 2\n" +
                "    }\n", conf);
        check("---\n" +
                "top:\n" +
                "  rules:\n" +
                "    {\n" +
                "       foo: 1,\n" +
                "      bar: 2\n" +
                "    }\n", conf, getLintProblem(5, 8));
        check("---\n" +
                "top:\n" +
                "  rules:\n" +
                "   {\n" +
                "     foo: 1,\n" +
                "     bar: 2\n" +
                "   }\n", conf, getLintProblem(4, 4));
        check("---\n" +
                "top:\n" +
                "  rules:\n" +
                "    {\n" +
                "      foo: 1,\n" +
                "      bar: 2\n" +
                "   }\n", conf, getLintProblem(7, 4));
        check("---\n" +
                "top:\n" +
                "  rules:\n" +
                "    {\n" +
                "      foo: 1,\n" +
                "      bar: 2\n" +
                "     }\n", conf, getLintProblem(7, 6));
        check("---\n" +
                "top:\n" +
                "  [\n" +
                "    a, b, c\n" +
                "  ]\n", conf);
        check("---\n" +
                "top:\n" +
                "  [\n" +
                "     a, b, c\n" +
                "  ]\n", conf, getLintProblem(4, 6));
        check("---\n" +
                "top:\n" +
                "   [\n" +
                "     a, b, c\n" +
                "   ]\n", conf, getLintProblem(4, 6));
        check("---\n" +
                "top:\n" +
                "  [\n" +
                "    a, b, c\n" +
                "   ]\n", conf, getLintProblem(5, 4));
        check("---\n" +
                "top:\n" +
                "  rules: [\n" +
                "    {\n" +
                "      foo: 1\n" +
                "    },\n" +
                "    {\n" +
                "      foo: 2,\n" +
                "      bar: [\n" +
                "        a, b, c\n" +
                "      ],\n" +
                "    },\n" +
                "  ]\n", conf);
        check("---\n" +
                "top:\n" +
                "  rules: [\n" +
                "    {\n" +
                "     foo: 1\n" +
                "     },\n" +
                "    {\n" +
                "      foo: 2,\n" +
                "        bar: [\n" +
                "          a, b, c\n" +
                "      ],\n" +
                "    },\n" +
                "]\n", conf, getLintProblem(5, 6), getLintProblem(6, 6),
                getLintProblem(9, 9), getLintProblem(11, 7), getLintProblem(13, 1));
    }

    public void testUnderIndented() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: 2, indent-sequences: consistent, check-multi-line-strings: false}");
        check("---\n" +
                "object:\n" +
                " val: 1\n" +
                "...\n", conf, getLintProblem(3, 2));
        check("---\n" +
                "object:\n" +
                "  k1:\n" +
                "   - a\n" +
                "...\n", conf, getLintProblem(4, 4));
        check("---\n" +
                "object:\n" +
                "  k3:\n" +
                "    - name: Unix\n" +
                "     date: 1969\n" +
                "...\n", conf, getSyntaxError(5, 6));

        conf = getConfig("indentation: {spaces: 4, indent-sequences: consistent, check-multi-line-strings: false}");
        check("---\n" +
                "object:\n" +
                "   val: 1\n" +
                "...\n", conf, getLintProblem(3, 4));
        check("---\n" +
                "- el1\n" +
                "- el2:\n" +
                "   - subel\n" +
                "...\n", conf, getLintProblem(4, 4));
        check("---\n" +
                "object:\n" +
                "    k3:\n" +
                "        - name: Linux\n" +
                "         date: 1991\n" +
                "...\n", conf, getSyntaxError(5, 10));

        conf = getConfig("indentation: {spaces: 2, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "a:\n" +
                "-\n" +  // empty list
               "b: c\n" +
               "...\n", conf, getLintProblem(3, 1));

        conf = getConfig("indentation: {spaces: 2, indent-sequences: consistent, check-multi-line-strings: false}");
        check("---\n" +
                "a:\n" +
                "  -\n" +  // empty list
               "b:\n" +
               "-\n" +
               "c: d\n" +
               "...\n", conf, getLintProblem(5, 1));
    }

    public void testOverIndented() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: 2, indent-sequences: consistent, check-multi-line-strings: false}");
        check("---\n" +
                "object:\n" +
                "   val: 1\n" +
                "...\n", conf, getLintProblem(3, 4));
        check("---\n" +
                "object:\n" +
                "  k1:\n" +
                "     - a\n" +
                "...\n", conf, getLintProblem(4, 6));
        check("---\n" +
                "object:\n" +
                "  k3:\n" +
                "    - name: Unix\n" +
                "       date: 1969\n" +
                "...\n", conf, getSyntaxError(5, 12));

        conf = getConfig("indentation: {spaces: 4, indent-sequences: consistent, check-multi-line-strings: false}");
        check("---\n" +
                "object:\n" +
                "     val: 1\n" +
                "...\n", conf, getLintProblem(3, 6));
        check("---\n" +
                " object:\n" +
                "     val: 1\n" +
                "...\n", conf, getLintProblem(2, 2));
        check("---\n" +
                "- el1\n" +
                "- el2:\n" +
                "     - subel\n" +
                "...\n", conf, getLintProblem(4, 6));
        check("---\n" +
                "- el1\n" +
                "- el2:\n" +
                "              - subel\n" +
                "...\n", conf, getLintProblem(4, 15));
        check("---\n" +
                "  - el1\n" +
                "  - el2:\n" +
                "        - subel\n" +
                "...\n", conf,
                getLintProblem(2, 3));
        check("---\n" +
                "object:\n" +
                "    k3:\n" +
                "        - name: Linux\n" +
                "           date: 1991\n" +
                "...\n", conf, getSyntaxError(5, 16));

        conf = getConfig("indentation: {spaces: 4, indent-sequences: whatever, check-multi-line-strings: false}");
        check("---\n" +
                "  - el1\n" +
                "  - el2:\n" +
                "    - subel\n" +
                "...\n", conf,
                getLintProblem(2, 3));

        conf = getConfig("indentation: {spaces: 2, indent-sequences: false, check-multi-line-strings: false}");
        check("---\n" +
                "a:\n" +
                "  -\n" +  // empty list
               "b: c\n" +
               "...\n", conf, getLintProblem(3, 3));

        conf = getConfig("indentation: {spaces: 2, indent-sequences: consistent, check-multi-line-strings: false}");
        check("---\n" +
                "a:\n" +
                "-\n" +  // empty list
               "b:\n" +
               "  -\n" +
               "c: d\n" +
               "...\n", conf, getLintProblem(5, 3));
    }

    public void testMultiLines() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "long_string: >\n" +
                "  bla bla blah\n" +
                "  blah bla bla\n" +
                "...\n", conf);
        check("---\n" +
                "- long_string: >\n" +
                "    bla bla blah\n" +
                "    blah bla bla\n" +
                "...\n", conf);
        check("---\n" +
                "obj:\n" +
                "  - long_string: >\n" +
                "      bla bla blah\n" +
                "      blah bla bla\n" +
                "...\n", conf);
    }

    public void testEmptyValue() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "key1:\n" +
                "key2: not empty\n" +
                "key3:\n" +
                "...\n", conf);
        check("---\n" +
                "-\n" +
                "- item 2\n" +
                "-\n" +
                "...\n", conf);
    }

    public void testNestedCollections() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: 2, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "- o:\n" +
                "  k1: v1\n" +
                "...\n", conf);
        check("---\n" +
                "- o:\n" +
                " k1: v1\n" +
                "...\n", conf, getSyntaxError(3, 2));
        check("---\n" +
                "- o:\n" +
                "   k1: v1\n" +
                "...\n", conf, getLintProblem(3, 4));

        conf = getConfig("indentation: {spaces: 4, indent-sequences: true, check-multi-line-strings: false}");
                check("---\n" +
                "- o:\n" +
                "      k1: v1\n" +
                "...\n", conf);
        check("---\n" +
                "- o:\n" +
                "     k1: v1\n" +
                "...\n", conf, getLintProblem(3, 6));
        check("---\n" +
                "- o:\n" +
                "       k1: v1\n" +
                "...\n", conf, getLintProblem(3, 8));
        check("---\n" +
                "- - - - item\n" +
                "    - elem 1\n" +
                "    - elem 2\n" +
                "    - - - - - very nested: a\n" +
                "              key: value\n" +
                "...\n", conf);
        check("---\n" +
                " - - - - item\n" +
                "     - elem 1\n" +
                "     - elem 2\n" +
                "     - - - - - very nested: a\n" +
                "               key: value\n" +
                "...\n", conf, getLintProblem(2, 2));
    }

    public void testReturn() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "a:\n" +
                "  b:\n" +
                "    c:\n" +
                "  d:\n" +
                "    e:\n" +
                "      f:\n" +
                "g:\n" +
                "...\n", conf);
        check("---\n" +
                "a:\n" +
                "  b:\n" +
                "    c:\n" +
                "   d:\n" +
                "...\n", conf, getSyntaxError(5, 4));
        check("---\n" +
                "a:\n" +
                "  b:\n" +
                "    c:\n" +
                " d:\n" +
                "...\n", conf, getSyntaxError(5, 2));
    }

    public void testFirstLine() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}",
                "document-start: disable");
        check("  a: 1\n", conf, getLintProblem(1, 3));
    }

    public void testExplicitBlockMappings() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "object:\n" +
                "    ? key\n" +
                "    : value\n", conf);
        check("---\n" +
                "object:\n" +
                "    ? key\n" +
                "    :\n" +
                "        value\n" +
                "...\n", conf);
        check("---\n" +
                "object:\n" +
                "    ?\n" +
                "        key\n" +
                "    : value\n", conf);
        check("---\n" +
                "object:\n" +
                "    ?\n" +
                "        key\n" +
                "    :\n" +
                "        value\n" +
                "...\n", conf);
        check("---\n" +
                "- ? key\n" +
                "  : value\n", conf);
        check("---\n" +
                "- ? key\n" +
                "  :\n" +
                "      value\n" +
                "...\n", conf);
        check("---\n" +
                "- ?\n" +
                "      key\n" +
                "  : value\n", conf);
        check("---\n" +
                "- ?\n" +
                "      key\n" +
                "  :\n" +
                "      value\n" +
                "...\n", conf);
        check("---\n" +
                "object:\n" +
                "    ? key\n" +
                "    :\n" +
                "       value\n" +
                "...\n", conf, getLintProblem(5, 8));
        check("---\n" +
                "- - ?\n" +
                "       key\n" +
                "    :\n" +
                "      value\n" +
                "...\n", conf, getLintProblem(5, 7));
        check("---\n" +
                "object:\n" +
                "    ?\n" +
                "       key\n" +
                "    :\n" +
                "         value\n" +
                "...\n", conf, getLintProblem(4, 8), getLintProblem(6, 10));
        check("---\n" +
                "object:\n" +
                "    ?\n" +
                "         key\n" +
                "    :\n" +
                "       value\n" +
                "...\n", conf, getLintProblem(4, 10), getLintProblem(6, 8));
    }

    public void testClearSequenceItem() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "-\n" +
                "  string\n" +
                "-\n" +
                "  map: ping\n" +
                "-\n" +
                "  - sequence\n" +
                "  -\n" +
                "    nested\n" +
                "  -\n" +
                "    >\n" +
                "      multi\n" +
                "      line\n" +
                "...\n", conf);
        check("---\n" +
                "-\n" +
                " string\n" +
                "-\n" +
                "   string\n", conf, getLintProblem(5, 4));
        check("---\n" +
                "-\n" +
                " map: ping\n" +
                "-\n" +
                "   map: ping\n", conf, getLintProblem(5, 4));
        check("---\n" +
                "-\n" +
                " - sequence\n" +
                "-\n" +
                "   - sequence\n", conf, getLintProblem(5, 4));
        check("---\n" +
                "-\n" +
                "  -\n" +
                "   nested\n" +
                "  -\n" +
                "     nested\n", conf, getLintProblem(4, 4), getLintProblem(6, 6));
        check("---\n" +
                "-\n" +
                "  -\n" +
                "     >\n" +
                "      multi\n" +
                "      line\n" +
                "...\n", conf, getLintProblem(4, 6));

        conf = getConfig("indentation: {spaces: 2, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "-\n" +
                " string\n" +
                "-\n" +
                "   string\n", conf, getLintProblem(3, 2), getLintProblem(5, 4));
        check("---\n" +
                "-\n" +
                " map: ping\n" +
                "-\n" +
                "   map: ping\n", conf, getLintProblem(3, 2), getLintProblem(5, 4));
        check("---\n" +
                "-\n" +
                " - sequence\n" +
                "-\n" +
                "   - sequence\n", conf, getLintProblem(3, 2), getLintProblem(5, 4));
        check("---\n" +
                "-\n" +
                "  -\n" +
                "   nested\n" +
                "  -\n" +
                "     nested\n", conf, getLintProblem(4, 4), getLintProblem(6, 6));
    }

    public void testAnchors() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "key: &anchor value\n", conf);
        check("---\n" +
                "key: &anchor\n" +
                "  value\n", conf);
        check("---\n" +
                "- &anchor value\n", conf);
        check("---\n" +
                "- &anchor\n" +
                "  value\n", conf);
        check("---\n" +
                "key: &anchor [1,\n" +
                "              2]\n", conf);
        check("---\n" +
                "key: &anchor\n" +
                "  [1,\n" +
                "   2]\n", conf);
        check("---\n" +
                "key: &anchor\n" +
                "  - 1\n" +
                "  - 2\n", conf);
        check("---\n" +
                "- &anchor [1,\n" +
                "           2]\n", conf);
        check("---\n" +
                "- &anchor\n" +
                "  [1,\n" +
                "   2]\n", conf);
        check("---\n" +
                "- &anchor\n" +
                "  - 1\n" +
                "  - 2\n", conf);
        check("---\n" +
                "key:\n" +
                "  &anchor1\n" +
                "  value\n", conf);
        check("---\n" +
                "pre:\n" +
                "  &anchor1 0\n" +
                "&anchor2 key:\n" +
                "  value\n", conf);
        check("---\n" +
                "machine0:\n" +
                "  /etc/hosts: &ref-etc-hosts\n" +
                "    content:\n" +
                "      - 127.0.0.1: localhost\n" +
                "      - ::1: localhost\n" +
                "    mode: 0644\n" +
                "machine1:\n" +
                "  /etc/hosts: *ref-etc-hosts\n", conf);
        check("---\n" +
                "list:\n" +
                "  - k: v\n" +
                "  - &a truc\n" +
                "  - &b\n" +
                "    truc\n" +
                "  - k: *a\n", conf);
    }

    public void testTags() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "-\n" +
                "  \"flow in block\"\n" +
                "- >\n" +
                "    Block scalar\n" +
                "- !!map  # Block collection\n" +
                "  foo: bar\n", conf);

        conf = getConfig("indentation: {spaces: consistent, indent-sequences: false, check-multi-line-strings: false}");
        check("---\n" +
                "sequence: !!seq\n" +
                "- entry\n" +
                "- !!seq\n" +
                "  - nested\n", conf);
        check("---\n" +
                "mapping: !!map\n" +
                "  foo: bar\n" +
                "Block style: !!map\n" +
                "  Clark: Evans\n" +
                "  Ingy: döt Net\n" +
                "  Oren: Ben-Kiki\n", conf);
        check("---\n" +
                "Flow style: !!map {Clark: Evans, Ingy: döt Net}\n" +
                "Block style: !!seq\n" +
                "- Clark Evans\n" +
                "- Ingy döt Net\n", conf);
    }

    public void testFlowsImbrication() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: false, check-multi-line-strings: false}");
        check("---\n" +
                "[val]: value\n", conf);
        check("---\n" +
                "{key}: value\n", conf);
        check("---\n" +
                "{key: val}: value\n", conf);
        check("---\n" +
                "[[val]]: value\n", conf);
        check("---\n" +
                "{{key}}: value\n", conf);
        check("---\n" +
                "{{key: val1}: val2}: value\n", conf);
        check("---\n" +
                "- [val, {{key: val}: val}]: value\n" +
                "- {[val,\n" +
                "    {{key: val}: val}]}\n" +
                "- {[val,\n" +
                "    {{key: val,\n" +
                "      key2}}]}\n" +
                "- {{{{{moustaches}}}}}\n" +
                "- {{{{{moustache,\n" +
                "       moustache},\n" +
                "      moustache}},\n" +
                "    moustache}}\n", conf);
        check("---\n" +
                "- {[val,\n" +
                "     {{key: val}: val}]}\n",
                conf, getLintProblem(3, 6));
        check("---\n" +
                "- {[val,\n" +
                "    {{key: val,\n" +
                "     key2}}]}\n",
                conf, getLintProblem(4, 6));
        check("---\n" +
                "- {{{{{moustache,\n" +
                "       moustache},\n" +
                "       moustache}},\n" +
                "   moustache}}\n",
                conf, getLintProblem(4, 8), getLintProblem(5, 4));
    }
}