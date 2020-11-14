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

public class TruthyTest extends RuleTester {
    public void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("truthy: disable");
        check("---\n" +
                "1: True\n", conf);
        check("---\n" +
                "True: 1\n", conf);
    }

    public void testEnabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("truthy: enable");
        check("---\n" +
                "1: True\n" +
                "True: 1\n",
                conf, getLintProblem(2, 4), getLintProblem(3, 1));
        check("---\n" +
                "1: \"True\"\n" +
                "\"True\": 1\n", conf);
        check("---\n" +
                "[\n" +
                "  true, false,\n" +
                "  \"false\", \"FALSE\",\n" +
                "  \"true\", \"True\",\n" +
                "  True, FALSE,\n" +
                "  on, OFF,\n" +
                "  NO, Yes\n" +
                "]\n", conf,
                getLintProblem(6, 3), getLintProblem(6, 9),
                getLintProblem(7, 3), getLintProblem(7, 7),
                getLintProblem(8, 3), getLintProblem(8, 7));
    }

    public void testDifferentAllowedValues() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("truthy:",
                "  allowed-values: [\"yes\", \"no\"]");
        check("---\n" +
                "key1: foo\n" +
                "key2: yes\n" +
                "key3: bar\n" +
                "key4: no\n", conf);
        check("---\n" +
                "key1: true\n" +
                "key2: Yes\n" +
                "key3: false\n" +
                "key4: no\n" +
                "key5: yes\n",
                conf,
                getLintProblem(2, 7), getLintProblem(3, 7),
                getLintProblem(4, 7));
    }

    public void testCombinedAllowedValues() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("truthy:",
                "  allowed-values: [\"yes\", \"no\", \"true\", \"false\"]");
        check("---\n" +
                "key1: foo\n" +
                "key2: yes\n" +
                "key3: bar\n" +
                "key4: no\n", conf);
        check("---\n" +
                "key1: true\n" +
                "key2: Yes\n" +
                "key3: false\n" +
                "key4: no\n" +
                "key5: yes\n",
                conf, getLintProblem(3, 7));
    }

    public void testNoAllowedValues() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("truthy:",
                "  allowed-values: []");
        check("---\n" +
                "key1: foo\n" +
                "key2: bar\n", conf);
        check("---\n" +
                "key1: true\n" +
                "key2: yes\n" +
                "key3: false\n" +
                "key4: no\n", conf,
                getLintProblem(2, 7), getLintProblem(3, 7),
                getLintProblem(4, 7), getLintProblem(5, 7));
    }

    public void testExplicitTypes() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("truthy: enable");
        check("---\n" +
                "string1: !!str True\n" +
                "string2: !!str yes\n" +
                "string3: !!str off\n" +
                "encoded: !!binary |\n" +
                "           True\n" +
                "           OFF\n" +
                "           pad==\n" +  // this decodes as "N\xbb\x9e8Qii"
                "boolean1: !!bool true\n" +
                "boolean2: !!bool \"false\"\n" +
                "boolean3: !!bool FALSE\n" +
                "boolean4: !!bool True\n" +
                "boolean5: !!bool off\n" +
                "boolean6: !!bool NO\n",
                conf);
    }

    public void testCheckKeysDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("truthy:",
                "  allowed-values: []",
                "  check-keys: false",
                "key-duplicates: disable");
        check("---\n" +
                "YES: 0\n" +
                "Yes: 0\n" +
                "yes: 0\n" +
                "No: 0\n" +
                "No: 0\n" +
                "no: 0\n" +
                "TRUE: 0\n" +
                "True: 0\n" +
                "true: 0\n" +
                "FALSE: 0\n" +
                "False: 0\n" +
                "false: 0\n" +
                "ON: 0\n" +
                "On: 0\n" +
                "on: 0\n" +
                "OFF: 0\n" +
                "Off: 0\n" +
                "off: 0\n" +
                "YES:\n" +
                "  Yes:\n" +
                "    yes:\n" +
                "      on: 0\n",
                conf);
    }
}