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

public class DocumentEndTest extends RuleTester {
    public void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("document-end: disable");
        check("---\n" +
                "with:\n" +
                "  document: end\n" +
                "...\n", conf);
        check("---\n" +
                "without:\n" +
                "  document: end\n", conf);
    }

    public void testRequired() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("document-end: {present: true}");
        check("", conf);
        check("\n", conf);
        check("---\n" +
                "with:\n" +
                "  document: end\n" +
                "...\n", conf);
        check("---\n" +
                "without:\n" +
                "  document: end\n", conf, getLintProblem(3, 1));
    }

    public void testForbidden() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("document-end: {present: false}");
        check("---\n" +
                "with:\n" +
                "  document: end\n" +
                "...\n", conf, getLintProblem(4, 1));
        check("---\n" +
                "without:\n" +
                "  document: end\n", conf);
    }

    public void testMultipleDocuments() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("document-end: {present: true}", "document-start: disable");
        check("---\n" +
                "first: document\n" +
                "...\n" +
                "---\n" +
                "second: document\n" +
                "...\n" +
                "---\n" +
                "third: document\n" +
                "...\n", conf);
        check("---\n" +
                "first: document\n" +
                "...\n" +
                "---\n" +
                "second: document\n" +
                "---\n" +
                "third: document\n" +
                "...\n", conf, getLintProblem(6, 1));
    }

    public void testDirectives() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("document-end: {present: true}");
        check("%YAML 1.2\n" +
                "---\n" +
                "document: end\n" +
                "...\n", conf);
        check("%YAML 1.2\n" +
                "%TAG ! tag:clarkevans.com,2002:\n" +
                "---\n" +
                "document: end\n" +
                "...\n", conf);
        check("---\n" +
                "first: document\n" +
                "...\n" +
                "%YAML 1.2\n" +
                "---\n" +
                "second: document\n" +
                "...\n", conf);
    }
}