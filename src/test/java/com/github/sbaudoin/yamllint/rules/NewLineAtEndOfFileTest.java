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

public class NewLineAtEndOfFileTest extends RuleTester {
    public void testDisabled() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("new-line-at-end-of-file: disable",
                "empty-lines: disable",
                "document-start: disable");
        check("", conf);
        check("\n", conf);
        check("word", conf);
        check("Sentence.\n", conf);
    }

    public void testEnabled() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("new-line-at-end-of-file: enable",
                "empty-lines: disable",
                "document-start: disable");
        check("", conf);
        check("\n", conf);
        check("word", conf, getLintProblem(1, 5));
        check("Sentence.\n", conf);
        check("---\n" +
        "yaml: document\n" +
        "...", conf, getLintProblem(3, 4));
    }
}