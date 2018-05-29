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

public class NewLinesTest extends RuleTester {
    public void testDisabled() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("new-line-at-end-of-file: disable",
                "new-lines: disable");
        check("", conf);
        check("\n", conf);
        check("\r", conf);
        check("\r\n", conf);
        check("---\ntext\n", conf);
        check("---\r\ntext\r\n", conf);
    }

    public void testUnixType() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("new-lines: {type: unix}");
        check("", conf);
        check("\n", conf);
        check("\r\n", conf, getLintProblem(1, 1));
        check("---\ntext\n", conf);
        check("---\r\ntext\r\n", conf, getLintProblem(1, 4));
        check("\n---\ntext\n", conf);
        check("\r\n---\r\ntext\r\n", conf, getLintProblem(1, 1));
    }

    public void testDosType() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("new-lines: {type: dos}");
        check("", conf);
        check("\n", conf, getLintProblem(1, 1));
        check("\r\n", conf);
        check("---\ntext\n", conf, getLintProblem(1, 4));
        check("---\r\ntext\r\n", conf);
        check("\n---\ntext\n", conf, getLintProblem(1, 1));
        check("\r\n---\r\ntext\r\n", conf);
    }
}