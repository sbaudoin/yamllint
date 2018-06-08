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

public class TrailingSpacesTest extends RuleTester {
    public void testDisabled() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("trailing-spaces: disable");
        check("", conf);
        check("\n", conf);
        check("    \n", conf);
        check("---\n" +
                "some: text \n", conf);
    }

    public void testEnabled() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("trailing-spaces: enable");
        check("", conf);
        check("\n", conf);
        check("    \n", conf, getLintProblem(1, 1));
        check("\t\t\t\n", conf, getSyntaxError(1, 1));
        check("---\n"+
                "some: text \n", conf, getLintProblem(2, 11));
        // Bug in snakeyaml: the syntax error is not raised.
        // See https://bitbucket.org/asomov/snakeyaml/issues/404/incorrect-handling-of-tab-character.
//        check("---\n" +
//                "some: text\t\n", conf, getSyntaxError(2, 11));
    }

    public void testWithDosNewLines() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("trailing-spaces: enable",
                "new-lines: {type: dos}");
        check("---\r\n" +
                "some: text\r\n", conf);
        check("---\r\n" +
                "some: text \r\n", conf, getLintProblem(2, 11));
    }
}