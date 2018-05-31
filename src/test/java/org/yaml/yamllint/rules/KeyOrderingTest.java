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

public class KeyOrderingTest extends RuleTester {
    public void testDisabled() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("key-ordering: disable");
        check("---\n" +
                "block mapping:\n" +
                "  secondkey: a\n" +
                "  firstkey: b\n", conf);
        check("---\n" +
                "flow mapping:\n" +
                "  {secondkey: a, firstkey: b}\n", conf);
        check("---\n" +
                "second: before_first\n" +
                "at: root\n", conf);
        check("---\n" +
                "nested but OK:\n" +
                "  second: {first: 1}\n" +
                "  third:\n" +
                "    second: 2\n", conf);
    }

    public void testEnabled() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("key-ordering: enable");
        check("---\n" +
                "block mapping:\n" +
                "  secondkey: a\n" +
                "  firstkey: b\n", conf,
                getLintProblem(4, 3));
        check("---\n" +
                "flow mapping:\n" +
                "  {secondkey: a, firstkey: b}\n", conf,
                getLintProblem(3, 18));
        check("---\n" +
                "second: before_first\n" +
                "at: root\n", conf,
                getLintProblem(3, 1));
        check("---\n" +
                "nested but OK:\n" +
                "  second: {first: 1}\n" +
                "  third:\n" +
                "    second: 2\n", conf);
    }

    public void testWordLength() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("key-ordering: enable");
        check("---\n" +
                "a: 1\n" +
                "ab: 1\n" +
                "abc: 1\n", conf);
        check("---\n" +
                "a: 1\n" +
                "abc: 1\n" +
                "ab: 1\n", conf,
                getLintProblem(4, 1));
    }

    public void testKeyDuplicates() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("key-duplicates: disable",
                "key-ordering: enable");
        check("---\n" +
                "key: 1\n" +
                "key: 2\n", conf);
    }

    public void testCase() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("key-ordering: enable");
        check("---\n" +
                "T-shirt: 1\n" +
                "T-shirts: 2\n" +
                "t-shirt: 3\n" +
                "t-shirts: 4\n", conf);
        check("---\n" +
                "T-shirt: 1\n" +
                "t-shirt: 2\n" +
                "T-shirts: 3\n" +
                "t-shirts: 4\n", conf,
                getLintProblem(4, 1));
    }

    public void testAccents() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("key-ordering: enable");
        check("---\n" +
                "hair: true\n" +
                "hais: true\n" +
                "haïr: true\n" +
                "haïssable: true\n", conf);
        check("---\n" +
                "haïr: true\n" +
                "hais: true\n", conf,
                getLintProblem(3, 1));
        check("---\n" +
                "haïr: true\n" +
                "hais: true\n", conf,
                getLintProblem(3, 1));
    }

    public void testKeyTokensInFlowSequences() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("key-ordering: enable");
        check("---\n" +
                "[\n" +
                "  key: value, mappings, in, flow: sequence\n" +
                "]\n", conf);
    }
}