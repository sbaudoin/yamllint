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

public class NewLinesTest extends RuleTester {
    public void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("new-line-at-end-of-file: disable",
                "new-lines: disable");
        check("", conf);
        check("\n", conf);
        check("\r", conf);
        check("\r\n", conf);
        check("---\ntext\n", conf);
        check("---\r\ntext\r\n", conf);
    }

    public void testUnixType() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("new-line-at-end-of-file: disable",
                "new-lines: {type: unix}");
        check("", conf);
        check("\r", conf);
        check("\n", conf);
        check("\r\n", conf, getLintProblem(1, 1));
        check("---\ntext\n", conf);
        check("---\r\ntext\r\n", conf, getLintProblem(1, 4));
        check("\n---\ntext\n", conf);
        check("\r\n---\r\ntext\r\n", conf, getLintProblem(1, 1));
    }

    public void testDosType() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("new-line-at-end-of-file: disable",
                "new-lines: {type: dos}");
        check("", conf);
        check("\r", conf);
        check("\n", conf, getLintProblem(1, 1));
        check("\r\n", conf);
        check("---\ntext\n", conf, getLintProblem(1, 4));
        check("---\r\ntext\r\n", conf);
        check("\n---\ntext\n", conf, getLintProblem(1, 1));
        check("\r\n---\r\ntext\r\n", conf);
    }

    public void testPlatformType() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("new-line-at-end-of-file: disable",
                "new-lines: {type: platform}");

        check("", conf);

        try(LineSeparatorModifier lsm = new LineSeparatorModifier("\n")) {
            check("\n", conf);
            check("\r\n", conf, getLintProblem(1, 1));
            check("---\ntext\n", conf);
            check("---\r\ntext\r\n", conf, getLintProblem(1, 4));
            check("---\r\ntext\n", conf, getLintProblem(1, 4));
            // FIXME: the following tests currently don't work
            // because only the first line is checked for line - endings
            // ---
            //check("---\ntext\r\nfoo\n", conf, getLintProblem(2, 4));
            //check("---\ntext\r\n", conf, getLintProblem(2, 4));
        }

        try(LineSeparatorModifier lsm = new LineSeparatorModifier("\r\n")) {
            check("\r\n", conf);
            check("\n", conf, getLintProblem(1, 1));
            check("---\r\ntext\r\n", conf);
            check("---\ntext\n", conf, getLintProblem(1, 4));
            check("---\ntext\r\n", conf, getLintProblem(1, 4));
            // FIXME: the following tests currently don't work
            // because only the first line is checked for line - endings
            // ---
            //check("---\r\ntext\nfoo\r\n", conf, getLintProblem(2, 4));
            //check("---\r\ntext\n", conf, getLintProblem(2, 4));
        }
    }


    private class LineSeparatorModifier implements AutoCloseable {
        private final String originalLS;

        public LineSeparatorModifier(String separator) {
            originalLS = System.getProperty("line.separator");
            System.setProperty("line.separator", separator);
        }

        @Override
        public void close() {
            System.setProperty("line.separator", originalLS);
        }
    }
}