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

public class CommentsIndentationTest extends RuleTester {
    public void testDisable() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments-indentation: disable");
        check("---\n" +
                " # line 1\n" +
                "# line 2\n" +
                "  # line 3\n" +
                "  # line 4\n" +
                "\n" +
                "obj:\n" +
                " # these\n" +
                "   # are\n" +
                "  # [good]\n" +
                "# bad\n" +
                "      # comments\n" +
                "  a: b\n" +
                "\n" +
                "obj1:\n" +
                "  a: 1\n" +
                "  # comments\n" +
                "\n" +
                "obj2:\n" +
                "  b: 2\n" +
                "\n" +
                "# empty\n" +
                "#\n" +
                "# comment\n" +
                "...\n", conf);
    }

    public void testEnabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments-indentation: enable");
        check("---\n" +
                "# line 1\n" +
                "# line 2\n", conf);
        check("---\n" +
                " # line 1\n" +
                "# line 2\n", conf, getLintProblem(2, 2));
        check("---\n" +
                "  # line 1\n" +
                "  # line 2\n", conf, getLintProblem(2, 3));
        check("---\n" +
                "obj:\n" +
                "  # normal\n" +
                "  a: b\n", conf);
        check("---\n" +
                "obj:\n" +
                " # bad\n" +
                "  a: b\n", conf, getLintProblem(3, 2));
        check("---\n" +
                "obj:\n" +
                "# bad\n" +
                "  a: b\n", conf, getLintProblem(3, 1));
        check("---\n" +
                "obj:\n" +
                "   # bad\n" +
                "  a: b\n", conf, getLintProblem(3, 4));
        check("---\n" +
                "obj:\n" +
                " # these\n" +
                "   # are\n" +
                "  # [good]\n" +
                "# bad\n" +
                "      # comments\n" +
                "  a: b\n", conf,
                getLintProblem(3, 2), getLintProblem(4, 4),
                getLintProblem(6, 1), getLintProblem(7, 7));
        check("---\n" +
                "obj1:\n" +
                "  a: 1\n" +
                "  # the following line is disabled\n" +
                "  # b: 2\n", conf);
        check("---\n" +
                "obj1:\n" +
                "  a: 1\n" +
                "  # b: 2\n" +
                "\n" +
                "obj2:\n" +
                "  b: 2\n", conf);
        check("---\n" +
                "obj1:\n" +
                "  a: 1\n" +
                "  # b: 2\n" +
                "# this object is useless\n" +
                "obj2: \"no\"\n", conf);
        check("---\n" +
                "obj1:\n" +
                "  a: 1\n" +
                "# this object is useless\n" +
                "  # b: 2\n" +
                "obj2: \"no\"\n", conf, getLintProblem(5, 3));
        check("---\n" +
                "obj1:\n" +
                "  a: 1\n" +
                "  # comments\n" +
                "  b: 2\n", conf);
        check("---\n" +
                "my list for today:\n" +
                "  - todo 1\n" +
                "  - todo 2\n" +
                "  # commented for now\n" +
                "  # - todo 3\n" +
                "...\n", conf);
    }

    public void testFirstLine() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments-indentation: enable");
        check("# comment\n", conf);
        check("  # comment\n", conf, getLintProblem(1, 3));
    }

    public void testNoNewlineAtEnd() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments-indentation: enable",
                "new-line-at-end-of-file: disable");
        check("# comment", conf);
        check("  # comment", conf, getLintProblem(1, 3));
    }

    public void testEmptyComment() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments-indentation: enable");
        check("---\n" +
                "# hey\n" +
                "# normal\n" +
                "#\n", conf);
        check("---\n" +
                "# hey\n" +
                "# normal\n" +
                " #\n", conf, getLintProblem(4, 2));
    }

    public void testInlineComment() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments-indentation: enable");
        check("---\n" +
                "- a  # inline\n" +
                "# ok\n", conf);
        check("---\n" +
                "- a  # inline\n" +
                " # not ok\n", conf, getLintProblem(3, 2));
        check("---\n" +
                " # not ok\n" +
                "- a  # inline\n", conf, getLintProblem(2, 2));
    }
}