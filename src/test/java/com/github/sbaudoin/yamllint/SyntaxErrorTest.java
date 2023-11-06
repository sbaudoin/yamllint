/**
 * Copyright (c) 2018-2023, Sylvain Baudoin
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
package com.github.sbaudoin.yamllint;

import com.github.sbaudoin.yamllint.rules.RuleTester;
import org.junit.jupiter.api.Test;

class SyntaxErrorTest extends RuleTester {
    @Override
    public String getRuleId() {
        // By convention syntax errors have the 'null' id
        return null;
    }

    @Test
    void testSyntaxErrors() throws YamlLintConfigException {
        check("---\n" +
                "this is not: valid: YAML\n", null, getLintProblem(2, 19));
        check("---\n" +
                "this is: valid YAML\n" +
                "\n" +
                "this is an error: [\n" +
                "\n" +
                "...\n", null, getLintProblem(6, 1));
        check("%YAML 1.2\n" +
                "%TAG ! tag:clarkevans.com,2002:\n" +
                "doc: ument\n" +
                "...\n", null, getLintProblem(3, 1));
    }

    @Test
    void testEmptyFlows() throws YamlLintConfigException {
        check("---\n" +
                "- []\n" +
                "- {}\n" +
                "- [\n" +
                "]\n" +
                "- {\n" +
                "}\n" +
                "...\n", null);
    }

    @Test
    void testExplicitMapping() throws YamlLintConfigException {
        check("---\n" +
                "? key\n" +
                ": - value 1\n" +
                "  - value 2\n" +
                "...\n", null);
        check("---\n" +
                 "?\n" +
                "  key\n" +
                ": {a: 1}\n" +
                "...\n", null);
        check("---\n" +
                "?\n" +
                "  key\n" +
                ":\n" +
                "  val\n" +
                "...\n", null);
    }

    @Test
    void testMappingBetweenSequences() throws YamlLintConfigException {
        // This is valid YAML.See http://www.yaml.org/spec/1.2/spec.html,
        // example 2.11
        check("---\n" +
                "? - Detroit Tigers\n" +
                "  - Chicago cubs\n" +
                ":\n" +
                "  - 2001-07-23\n" +
                "\n" +
                "? [New York Yankees,\n" +
                "   Atlanta Braves]\n" +
                ": [2001-07-02, 2001-08-12,\n" +
                "   2001-08-14]\n", null);
    }

    @Test
    void testSets() throws YamlLintConfigException {
        check("---\n" +
                "? key one\n" +
                "? key two\n" +
                "? [non, scalar, key]\n" +
                "? key with value\n" +
                ": value\n" +
                "...\n", null);
        check("---\n" +
                "? - multi\n" +
                "  - line\n" +
                "  - keys\n" +
                "? in:\n" +
                "    a:\n" +
                "      set\n" +
                "...\n", null);
    }

    @Test
    void testMultipleDocs() throws YamlLintConfigException {
        check("---\n" +
                "a: b\n" +
                "...\n" +
                "---\n" +
                ",\n" +
                "...\n", null, getLintProblem(5, 1));
    }

    @Test
    void testCustomTag() throws YamlLintConfigException {
        // See https://github.com/sbaudoin/sonar-yaml/issues/15
        check("---\n" +
                "appli_password: !vault |\n" +
                "          $ANSIBLE_VAULT;1.1;AES256\n" +
                "          42424242424242424242424242424242424242424242424242424242424242424242424242424242\n" +
                "          42424242424242424242424242424242424242424242424242424242424242424242424242424242\n" +
                "          42424242424242424242424242424242424242424242424242424242424242424242424242424242\n" +
                "          42424242424242424242424242424242424242424242424242424242424242424242424242424242\n" +
                "          4242\n", null);
    }
}
