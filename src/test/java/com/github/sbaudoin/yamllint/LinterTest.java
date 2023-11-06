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

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static com.github.sbaudoin.yamllint.rules.RuleTester.getFakeConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LinterTest {
    @Test
    void testRunOnString() throws YamlLintConfigException {
        assertEquals(2, Linter.run("test: document", getFakeConfig()).size());
        assertEquals(2, Linter.run("test: document", getFakeConfig(), new File("file.yml")).size());
    }

    @Test
    void testReader() throws YamlLintConfigException, IOException {
        assertEquals(0, Linter.run(new StringReader("---\n"), getFakeConfig()).size());
        assertEquals(2, Linter.run(new StringReader("test: document"), getFakeConfig(), new File("file.yml")).size());
    }

    @Test
    void testEmpty() throws YamlLintConfigException {
        assertEquals(0, Linter.run("---\n", getFakeConfig()).size());
    }

    @Test
    void testRunOnNonAsciiChars() throws IOException, YamlLintConfigException {
        String s = "---\n" +
                "- hétérogénéité\n" +
                "# 19.99\n";
        assertEquals(0, Linter.run(s, getFakeConfig()).size());
        assertEquals(0, Linter.run(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)), getFakeConfig()).size());
        assertEquals(0, Linter.run(new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1), getFakeConfig()).size());

        s = "---\n" +
                "- お早う御座います。\n" +
                "# الأَبْجَدِيَّة العَرَبِيَّة\n";
        assertEquals(0, Linter.run(s, getFakeConfig()).size());
        assertEquals(0, Linter.run(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)), getFakeConfig()).size());
    }

    @Test
    void testRunWithIgnore() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = new YamlLintConfig("rules:\n" +
                "  indentation:\n" +
                "    spaces: 2\n" +
                "    indent-sequences: true\n" +
                "    check-multi-line-strings: false\n" +
                "ignore: |\n" +
                "  .*\\.txt$\n" +
                "  foo.bar\n");
        assertEquals(0, Linter.run(conf, new File("/my/file.txt")).size());
        assertEquals(0, Linter.run(conf, new File("foo.bar")).size());
    }

    @Test
    void testGetProblemLevel() {
        assertEquals(Linter.NONE_LEVEL, Linter.getProblemLevel(0));
        assertEquals(Linter.INFO_LEVEL, Linter.getProblemLevel(1));
        assertEquals(Linter.WARNING_LEVEL, Linter.getProblemLevel(2));
        assertEquals(Linter.ERROR_LEVEL, Linter.getProblemLevel(3));
        assertEquals(0, Linter.getProblemLevel(Linter.NONE_LEVEL));
        assertEquals(1, Linter.getProblemLevel(Linter.INFO_LEVEL));
        assertEquals(2, Linter.getProblemLevel(Linter.WARNING_LEVEL));
        assertEquals(3, Linter.getProblemLevel(Linter.ERROR_LEVEL));
    }
}
