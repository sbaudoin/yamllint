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
package org.yaml.yamllint.rules;

import org.yaml.yamllint.YamlLintConfig;
import org.yaml.yamllint.YamlLintConfigException;

import java.io.IOException;

public class CommasTest extends RuleTester {
    public void testDisabled() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("commas: disable");
        check("---\n" +
                "dict: {a: b ,   c: \"1 2 3\",    d: e , f: [g,      h]}\n" +
                "array: [\n" +
                "  elem  ,\n" +
                "  key: val ,\n" +
                "]\n" +
                "map: {\n" +
                "  key1: val1 ,\n" +
                "  key2: val2,\n" +
                "}\n" +
                "...\n", conf);
        check("---\n" +
                "- [one, two , three,four]\n" +
                "- {five,six , seven, eight}\n" +
                "- [\n" +
                "  nine,  ten\n" +
                "  , eleven\n" +
                "  ,twelve\n" +
                "]\n" +
                "- {\n" +
                "  thirteen: 13,  fourteen\n" +
                "  , fifteen: 15\n" +
                "  ,sixteen: 16\n" +
                "}\n", conf);
    }

    public void testBeforeMax() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("commas:",
                "  max-spaces-before: 0",
                "  min-spaces-after: 0",
                "  max-spaces-after: -1");
        check("---\n" +
                "array: [1, 2,  3, 4]\n" +
                "...\n", conf);
        check("---\n" +
                "array: [1, 2 ,  3, 4]\n" +
                "...\n", conf, getLintProblem(2, 13));
        check("---\n" +
                "array: [1 , 2,  3      , 4]\n" +
                "...\n", conf, getLintProblem(2, 10), getLintProblem(2, 23));
        check("---\n" +
                "dict: {a: b, c: \"1 2 3\", d: e,  f: [g, h]}\n" +
                "...\n", conf);
        check("---\n" +
                "dict: {a: b, c: \"1 2 3\" , d: e,  f: [g, h]}\n" +
                "...\n", conf, getLintProblem(2, 24));
        check("---\n" +
                "dict: {a: b , c: \"1 2 3\", d: e,  f: [g    , h]}\n" +
                "...\n", conf, getLintProblem(2, 12), getLintProblem(2, 42));
        check("---\n" +
                "array: [\n" +
                "  elem,\n" +
                "  key: val,\n" +
                "]\n", conf);
        check("---\n" +
                "array: [\n" +
                "  elem ,\n" +
                "  key: val,\n" +
                "]\n", conf, getLintProblem(3, 7));
        check("---\n" +
                "map: {\n" +
                "  key1: val1,\n" +
                "  key2: val2,\n" +
                "}\n", conf);
        check("---\n" +
                "map: {\n" +
                "  key1: val1,\n" +
                "  key2: val2 ,\n" +
                "}\n", conf, getLintProblem(4, 13));
    }

    public void testBeforeMaxWithCommaOnNewLine() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("commas:",
                "  max-spaces-before: 0",
                "  min-spaces-after: 0",
                "  max-spaces-after: -1");
        check("---\n" +
                "flow-seq: [1, 2, 3\n" +
                "           , 4, 5, 6]\n" +
                "...\n", conf, getLintProblem(3, 11));
        check("---\n" +
                "flow-map: {a: 1, b: 2\n" +
                "           , c: 3}\n" +
                "...\n", conf, getLintProblem(3, 11));

        conf = getConfig("commas:",
                "  max-spaces-before: 0",
                "  min-spaces-after: 0",
                "  max-spaces-after: -1",
                "indentation: disable");
        check("---\n" +
                "flow-seq: [1, 2, 3\n" +
                "         , 4, 5, 6]\n" +
                "...\n", conf, getLintProblem(3, 9));
        check("---\n" +
                "flow-map: {a: 1, b: 2\n" +
                "         , c: 3}\n" +
                "...\n", conf, getLintProblem(3, 9));
        check("---\n" +
                "[\n" +
                "1,\n" +
                "2\n" +
                ", 3\n" +
                "]\n", conf, getLintProblem(5, 1));
        check("---\n" +
                "{\n" +
                "a: 1,\n" +
                "b: 2\n" +
                ", c: 3\n" +
                "}\n", conf, getLintProblem(5, 1));
    }

    public void testBeforeMax3() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("commas:",
                "  max-spaces-before: 3",
                "  min-spaces-after: 0",
                "  max-spaces-after: -1");
        check("---\n" +
                "array: [1 , 2, 3   , 4]\n" +
                "...\n", conf);
        check("---\n" +
                "array: [1 , 2, 3    , 4]\n" +
                "...\n", conf, getLintProblem(2, 20));
        check("---\n" +
                "array: [\n" +
                "  elem1   ,\n" +
                "  elem2    ,\n" +
                "  key: val,\n" +
                "]\n", conf, getLintProblem(4, 11));
    }

    public void testAfterMin() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("commas:",
                "  max-spaces-before: -1",
                "  min-spaces-after: 1",
                "  max-spaces-after: -1");
        check("---\n" +
                "- [one, two , three,four]\n" +
                "- {five,six , seven, eight}\n" +
                "- [\n" +
                "  nine,  ten\n" +
                "  , eleven\n" +
                "  ,twelve\n" +
                "]\n" +
                "- {\n" +
                "  thirteen: 13,  fourteen\n" +
                "  , fifteen: 15\n" +
                "  ,sixteen: 16\n" +
                "}\n", conf,
                getLintProblem(2, 21), getLintProblem(3, 9),
                getLintProblem(7, 4), getLintProblem(12, 4));
    }

    public void testAfterMax() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("commas:",
                "  max-spaces-before: -1",
                "  min-spaces-after: 0",
                "  max-spaces-after: 1");
        check("---\n" +
                "array: [1, 2, 3, 4]\n" +
                "...\n", conf);
        check("---\n" +
                "array: [1, 2,  3, 4]\n" +
                "...\n", conf, getLintProblem(2, 15));
        check("---\n" +
                "array: [1,  2, 3,     4]\n" +
                "...\n", conf, getLintProblem(2, 12), getLintProblem(2, 22));
        check("---\n" +
                "dict: {a: b , c: \"1 2 3\", d: e, f: [g, h]}\n" +
                "...\n", conf);
        check("---\n" +
                "dict: {a: b , c: \"1 2 3\",  d: e, f: [g, h]}\n" +
                "...\n", conf, getLintProblem(2, 27));
        check("---\n" +
                "dict: {a: b ,  c: \"1 2 3\", d: e, f: [g,     h]}\n" +
                "...\n", conf, getLintProblem(2, 15), getLintProblem(2, 44));
        check("---\n" +
                "array: [\n" +
                "  elem,\n" +
                "  key: val,\n" +
                "]\n", conf);
        check("---\n" +
                "array: [\n" +
                "  elem,  key: val,\n" +
                "]\n", conf, getLintProblem(3, 9));
        check("---\n" +
                "map: {\n" +
                "  key1: val1,   key2: [val2,  val3]\n" +
                "}\n", conf, getLintProblem(3, 16), getLintProblem(3, 30));
    }

    public void testAfterMax3() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("commas:",
                "  max-spaces-before: -1",
                "  min-spaces-after: 1",
                "  max-spaces-after: 3");
        check("---\n" +
                "array: [1,  2, 3,   4]\n" +
                "...\n", conf);
        check("---\n" +
                "array: [1,  2, 3,    4]\n" +
                "...\n", conf, getLintProblem(2, 21));
        check("---\n" +
                "dict: {a: b ,   c: \"1 2 3\",    d: e, f: [g,      h]}\n" +
                "...\n", conf, getLintProblem(2, 31), getLintProblem(2, 49));
    }

    public void testBothBeforeAndAfter() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("commas:",
                "  max-spaces-before: 0",
                "  min-spaces-after: 1",
                "  max-spaces-after: 1");
        check("---\n" +
                        "dict: {a: b ,   c: \"1 2 3\",    d: e , f: [g,      h]}\n" +
                        "array: [\n" +
                        "  elem  ,\n" +
                        "  key: val ,\n" +
                        "]\n" +
                        "map: {\n" +
                        "  key1: val1 ,\n" +
                        "  key2: val2,\n" +
                        "}\n" +
                        "...\n", conf,
                getLintProblem(2, 12), getLintProblem(2, 16), getLintProblem(2, 31),
                getLintProblem(2, 36), getLintProblem(2, 50), getLintProblem(4, 8),
                getLintProblem(5, 11), getLintProblem(8, 13));
        conf = getConfig("commas:",
                "  max-spaces-before: 0",
                "  min-spaces-after: 1",
                "  max-spaces-after: 1",
                "indentation: disable");
        check("---\n" +
                "- [one, two , three,four]\n" +
                "- {five,six , seven, eight}\n" +
                "- [\n" +
                "  nine,  ten\n" +
                "  , eleven\n" +
                "  ,twelve\n" +
                "]\n" +
                "- {\n" +
                "  thirteen: 13,  fourteen\n" +
                "  , fifteen: 15\n" +
                "  ,sixteen: 16\n" +
                "}\n", conf,
                getLintProblem(2, 12), getLintProblem(2, 21), getLintProblem(3, 9),
                getLintProblem(3, 12), getLintProblem(5, 9), getLintProblem(6, 2),
                getLintProblem(7, 2), getLintProblem(7, 4), getLintProblem(10, 17),
                getLintProblem(11, 2), getLintProblem(12, 2), getLintProblem(12, 4));
    }
}