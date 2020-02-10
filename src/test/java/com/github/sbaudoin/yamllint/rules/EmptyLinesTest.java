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

public class EmptyLinesTest extends RuleTester {
    public void testDisabled() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-lines: disable",
                "new-line-at-end-of-file: disable",
                "document-start: disable");
        check("", conf);
        // Unix
        check("\n", conf);
        check("\n\n", conf);
        check("\n\n\n\n\n\n\n\n\n", conf);
        check("some text\n\n\n\n\n\n\n\n\n", conf);
        check("\n\n\n\n\n\n\n\n\nsome text", conf);
        check("\n\n\nsome text\n\n\n", conf);
        // Windows
        check("\r\n", conf);
        check("\r\n\r\n", conf);
        check("\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n", conf);
        check("some text\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n", conf);
        check("\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\nsome text", conf);
        check("\r\n\r\n\r\nsome text\r\n\r\n\r\n", conf);
    }

    public void testEmptyDocument() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-lines: {max: 0, max-start: 0, max-end: 0}",
                "new-line-at-end-of-file: disable",
                "document-start: disable");
        check("", conf);
        // Unix
        check("\n", conf);
        // Windows
        check("\r\n", conf);
    }

    public void test0EmptyLines() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-lines: {max: 0, max-start: 0, max-end: 0}",
                "new-line-at-end-of-file: disable");
        // Unix
        check("---\n", conf);
        check("---\ntext\n\ntext", conf, getLintProblem(3, 1));
        check("---\ntext\n\ntext\n", conf, getLintProblem(3, 1));
        // Windows
        check("---\r\n", conf);
        check("---\r\ntext\r\n\r\ntext", conf, getLintProblem(3, 1));
        check("---\r\ntext\r\n\r\ntext\r\n", conf, getLintProblem(3, 1));
    }

    public void test10EmptyLines() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-lines: {max: 10, max-start: 0, max-end: 0}");
        // Unix
        check("---\nintro\n\n\n\n\n\n\n\n\n\n\nconclusion\n", conf);
        check("---\nintro\n\n\n\n\n\n\n\n\n\n\n\nconclusion\n", conf,
                getLintProblem(13, 1));
        // Windows
        check("---\r\nintro\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\nconclusion\r\n", conf);
        check("---\r\nintro\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\nconclusion\r\n", conf,
                getLintProblem(13, 1));

    }

    public void testSpaces() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-lines: {max: 1, max-start: 0, max-end: 0}",
                "trailing-spaces: disable");
        // Unix
        check("---\nintro\n\n \n\nconclusion\n", conf);
        check("---\nintro\n\n \n\n\nconclusion\n", conf, getLintProblem(6, 1));
        // Windows
        check("---\r\nintro\r\n\r\n \r\n\r\nconclusion\r\n", conf);
        check("---\r\nintro\r\n\r\n \r\n\r\n\r\nconclusion\r\n", conf, getLintProblem(6, 1));
    }

    public void testEmptyLinesAtStart() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-lines: {max: 2, max-start: 4, max-end: 0}",
                "document-start: disable");
        // Unix
        check("\n\n\n\nnon empty\n", conf);
        check("\n\n\n\n\nnon empty\n", conf, getLintProblem(5, 1));
        // Windows
        check("\r\n\r\n\r\n\r\nnon empty\r\n", conf);
        check("\r\n\r\n\r\n\r\n\r\nnon empty\r\n", conf, getLintProblem(5, 1));

        conf = getConfig("empty-lines: {max: 2, max-start: 0, max-end: 0}",
                "document-start: disable");
        // Unix
        check("non empty\n", conf);
        check("\nnon empty\n", conf, getLintProblem(1, 1));
        // Windows
        check("non empty\r\n", conf);
        check("\r\nnon empty\r\n", conf, getLintProblem(1, 1));
    }

    public void testEmptyLinesAtEnd() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-lines: {max: 2, max-start: 0, max-end: 4}",
                "document-start: disable");
        // Unix
        check("non empty\n\n\n\n\n", conf);
        check("non empty\n\n\n\n\n\n", conf, getLintProblem(6, 1));
        // Windows
        check("non empty\r\n\r\n\r\n\r\n\r\n", conf);
        check("non empty\r\n\r\n\r\n\r\n\r\n\r\n", conf, getLintProblem(6, 1));

        conf = getConfig("empty-lines: {max: 2, max-start: 0, max-end: 0}",
                "document-start: disable");
        // Unix
        check("non empty\n", conf);
        check("non empty\n\n", conf, getLintProblem(2, 1));
        // Windows
        check("non empty\r\n", conf);
        check("non empty\r\n\r\n", conf, getLintProblem(2, 1));
    }
}