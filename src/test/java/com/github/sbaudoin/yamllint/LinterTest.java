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
package com.github.sbaudoin.yamllint;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.github.sbaudoin.yamllint.rules.RuleTester.getFakeConfig;

public class LinterTest extends TestCase {
    public void testRunOnString() throws YamlLintConfigException {
        Linter.run("test: document", getFakeConfig());
    }

    public void testEmpty() throws YamlLintConfigException {
        assertEquals(0, Linter.run("---\n", getFakeConfig()).size());
    }

    public void testRunOnNonAsciiChars() throws IOException, YamlLintConfigException {
        String s = "- hétérogénéité\n" +
               "# 19.99\n";
        Linter.run(s, getFakeConfig());
        Linter.run(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)), getFakeConfig());
        Linter.run(new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1), getFakeConfig());

        s = "- お早う御座います。\n" +
                "# الأَبْجَدِيَّة العَرَبِيَّة\n";
        Linter.run(s, getFakeConfig());
        Linter.run(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)), getFakeConfig());
    }

    public void testRunWithIgnore() throws IOException, YamlLintConfigException {
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

    public void testGetProblemLevel() {
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
