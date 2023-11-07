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
package com.github.sbaudoin.yamllint.rules;

import com.github.sbaudoin.yamllint.YamlLintConfig;
import com.github.sbaudoin.yamllint.YamlLintConfigException;
import org.junit.jupiter.api.Test;

class DocumentStartTest extends RuleTester {
    @Test
    void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("document-start: disable");
        check("", conf);
        check("key: val", conf);
        check("---\n" +
                "key: val", conf);
    }

    @Test
    void testRequired() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("document-start: {present: true}", "empty-lines: disable");
        check("", conf);
        check("\n", conf);
        check("key: val", conf, getLintProblem(1, 1));
        check("\n" +
                "\n" +
                "key: val\n", conf, getLintProblem(3, 1));
        check("---\n" +
                "key: val\n", conf);
        check("\n" +
                "\n" +
                "---\n" +
                "key: val\n", conf);
    }

    @Test
    void testForbidden() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("document-start: {present: false}", "empty-lines: disable");
        check("", conf);
        check("key: val\n", conf);
        check("\n" +
                "\n" +
                "key: val\n", conf);
        check("---\n" +
                "key: val\n", conf, getLintProblem(1, 1));
        check("\n" +
                "\n" +
                "---\n" +
                "key: val\n", conf, getLintProblem(3, 1));
        check("first: document\n" +
                "---\n" +
                "key: val\n", conf, getLintProblem(2, 1));
    }

    @Test
    void testMultipleDocuments() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("document-start: {present: true}");
        check("---\n" +
                "first: document\n" +
                "...\n" +
                "---\n" +
                "second: document\n" +
                "...\n" +
                "---\n" +
                "third: document\n", conf);
        check("---\n" +
                "first: document\n" +
                "---\n" +
                "second: document\n" +
                "---\n" +
                "third: document\n", conf);
        check("---\n" +
                "first: document\n" +
                "...\n" +
                "second: document\n" +
                "---\n" +
                "third: document\n", conf, getSyntaxError(4, 1));
    }

    @Test
    void testDirectives() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("document-start: {present: true}");
        check("%YAML 1.2\n" +
                "---\n" +
                "doc: ument\n" +
                "...\n", conf);
        check("%YAML 1.2\n" +
                "%TAG ! tag:clarkevans.com,2002:\n" +
                "---\n" +
                "doc: ument\n" +
                "...\n", conf);
        check("---\n" +
                "doc: 1\n" +
                "...\n" +
                "%YAML 1.2\n" +
                "---\n" +
                "doc: 2\n" +
                "...\n", conf);
    }
}