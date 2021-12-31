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

import com.github.sbaudoin.yamllint.Format;
import com.github.sbaudoin.yamllint.YamlLintConfig;
import com.github.sbaudoin.yamllint.YamlLintConfigException;

public class LineLengthTest extends RuleTester {
    public void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("line-length: disable",
                "empty-lines: disable",
                "new-line-at-end-of-file: disable",
                "document-start: disable");
        check("", conf);
        check("\n", conf);
        check("---\n", conf);
        check(Format.repeat(81, "a"), conf);
        check("---\n" + Format.repeat(81, "a") + "\n", conf);
        check(Format.repeat(1000, "b"), conf);
        check("---\n" + Format.repeat(1000, "b") + "\n", conf);
        check("content: |\n" +
                "  {% this line is" + Format.repeat(99, " really") + " long %}\n",
                conf);
    }

    public void testDefault() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("line-length:",
                "  max: 80",
                "  allow-non-breakable-words: true",
                "  allow-non-breakable-inline-mappings: false",
                "empty-lines: disable",
                "new-line-at-end-of-file: disable",
                "document-start: disable");
        check("", conf);
        check("\n", conf);
        check("---\n", conf);
        check(Format.repeat(80, "a"), conf);
        check("---\n" + Format.repeat(80, "a") + "\n", conf);
        check(Format.repeat(16, "aaaa ") + "z", conf, getLintProblem(1, 81));
        check("---\n" + Format.repeat(16, "aaaa ") + "z" + "\n", conf, getLintProblem(2, 81));
        check(Format.repeat(1000, "word ") + "end", conf, getLintProblem(1, 81));
        check("---\n" + Format.repeat(1000, "word ") + "end\n", conf, getLintProblem(2, 81));
    }

    public void testMaxLength10() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("line-length:",
                "  max: 10",
                "  allow-non-breakable-words: true",
                "  allow-non-breakable-inline-mappings: false",
                "new-line-at-end-of-file: disable");
        check("---\nABCD EFGHI", conf);
        check("---\nABCD EFGHIJ", conf, getLintProblem(2, 11));
        check("---\nABCD EFGHIJ\n", conf, getLintProblem(2, 11));
    }

    public void testSpaces() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("line-length:",
                "  max: 80",
                "  allow-non-breakable-words: true",
                "  allow-non-breakable-inline-mappings: false",
                "new-line-at-end-of-file: disable",
                "trailing-spaces: disable");
        check("---\n" + Format.repeat(81, " "), conf, getLintProblem(2, 81));
        check("---\n" + Format.repeat(81, " ") + "\n", conf, getLintProblem(2, 81));
    }

    public void testNonBreakableWord() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("line-length: {max: 20, allow-non-breakable-words: true, allow-non-breakable-inline-mappings: false}");
        check("---\n" + Format.repeat(30, "A") + "\n", conf);
        check("---\n" +
                "this:\n" +
                "  is:\n" +
                "    - a:\n" +
                "        http://localhost/very/long/url\n" +
                "...\n", conf);
        check("---\n" +
                "this:\n" +
                "  is:\n" +
                "    - a:\n" +
                "        # http://localhost/very/long/url\n" +
                "        comment\n" +
                "...\n", conf);
        check("---\n" +
                "this:\n" +
                "is:\n" +
                "another:\n" +
                "  - https://localhost/very/very/long/url\n" +
                "...\n", conf);
        check("---\n" +
                "long_line: http://localhost/very/very/long/url\n", conf,
                getLintProblem(2, 21));

        conf = getConfig("line-length: {max: 20, allow-non-breakable-words: true}", "comments: enable");
        check("---\n" +
                "# http://www.verylongurlurlurlurlurlurlurlurl.com\n" +
                "key:\n" +
                "  subkey: value\n", conf);
        check("---\n" +
                "## http://www.verylongurlurlurlurlurlurlurlurl.com\n" +
                "key:\n" +
                "  subkey: value\n", conf);
        check("---\n" +
                "# # http://www.verylongurlurlurlurlurlurlurlurl.com\n" +
                "key:\n" +
                "  subkey: value\n", conf,
                getLintProblem(2, 21));
        check("---\n" +
                "#A http://www.verylongurlurlurlurlurlurlurlurl.com\n" +
                "key:\n" +
                "  subkey: value\n", conf,
                getLintProblem(2, 2, "comments"), getLintProblem(2, 21, "line-length"));

        conf = getConfig("line-length: {max: 20, allow-non-breakable-words: false, allow-non-breakable-inline-mappings: false}");
        check("---\n" + Format.repeat(30, "A") + "\n", conf, getLintProblem(2, 21));
        check("---\n" +
                "this:\n" +
                "  is:\n" +
                "    - a:\n" +
                "        http://localhost/very/long/url\n" +
                "...\n", conf, getLintProblem(5, 21));
        check("---\n" +
                "this:\n" +
                "  is:\n" +
                "    - a:\n" +
                "        # http://localhost/very/long/url\n" +
                "        comment\n" +
                "...\n", conf, getLintProblem(5, 21));
        check("---\n" +
                "this:\n" +
                "is:\n" +
                "another:\n" +
                "  - https://localhost/very/very/long/url\n" +
                "...\n", conf, getLintProblem(5, 21));
        check("---\n" +
                "long_line: http://localhost/very/very/long/url\n" +
                "...\n", conf, getLintProblem(2, 21));

        conf = getConfig("line-length: {max: 20, allow-non-breakable-words: true, allow-non-breakable-inline-mappings: false}",
                "trailing-spaces: disable");
        check("---\n" +
                "loooooooooong+word+and+some+space+at+the+end       \n",
                conf, getLintProblem(2, 21));
    }

    public void testNonBreakableInlineMappings() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("line-length: {max: 20," +
                "              allow-non-breakable-inline-mappings: true," +
                "              allow-non-breakable-words: true}");
        check("---\n" +
                "long_line: http://localhost/very/very/long/url\n" +
                "long line: http://localhost/very/very/long/url\n", conf);
        check("---\n" +
                "- long line: http://localhost/very/very/long/url\n", conf);
        check("---\n" +
                "long_line: http://localhost/short/url + word\n" +
                "long line: http://localhost/short/url + word\n",
                conf, getLintProblem(2, 21), getLintProblem(3, 21));

        conf = getConfig("line-length: {max: 20," +
                "              allow-non-breakable-inline-mappings: true," +
                "              allow-non-breakable-words: true}",
                "trailing-spaces: disable");
        check("---\n" +
                "long_line: and+some+space+at+the+end       \n",
                conf, getLintProblem(2, 21));
        check("---\n" +
                "long line: and+some+space+at+the+end       \n",
                conf, getLintProblem(2, 21));
        check("---\n" +
                "- long line: and+some+space+at+the+end       \n",
                conf, getLintProblem(2, 21));

        // See https://github.com/adrienverge/yamllint/issues/21
        conf = getConfig("line-length: {allow-non-breakable-inline-mappings: true," +
                "              max: 80," +
                "allow-non-breakable-words: true}");
        check("---\n" +
                "content: |\n" +
                "  {% this line is" + Format.repeat(99, " really") + " long %}\n",
                conf, getLintProblem(3, 81));
    }
}