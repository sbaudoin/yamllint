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
package com.github.sbaudoin.yamllint.rules;

import com.github.sbaudoin.yamllint.YamlLintConfig;
import com.github.sbaudoin.yamllint.YamlLintConfigException;

import java.io.IOException;

public class TruthyTest extends RuleTester {
    public void testDisabled() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("truthy: disable");
        check("---\n" +
                "1: True\n", conf);
        check("---\n" +
                "True: 1\n", conf);
    }

    public void testEnabled() throws IOException, YamlLintConfigException {
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

    public void testExplicitTypes() throws IOException, YamlLintConfigException {
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
}