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
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;

public class CommentsTest extends RuleTester {
    public void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments: disable",
                "comments-indentation: disable");
        check("---\n" +
                "#comment\n" +
                "\n" +
                "test: #    description\n" +
                "  - foo  # bar\n" +
                "  - hello #world\n" +
                "\n" +
                "# comment 2\n" +
                "#comment 3\n" +
                "  #comment 3 bis\n" +
                "  #  comment 3 ter\n" +
                "\n" +
                "################################\n" +
                "## comment 4\n" +
                "##comment 5\n" +
                "\n" +
                "string: \"Une longue phrase.\" # this is French\n", conf);
    }

    public void testStartingSpace() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments:",
                "  require-starting-space: true",
                "  min-spaces-from-content: -1",
                "comments-indentation: disable");
        check("---\n" +
                "# comment\n" +
                "\n" +
                "test:  #     description\n" +
                "  - foo  #   bar\n" +
                "  - hello  # world\n" +
                "\n" +
                "# comment 2\n" +
                "# comment 3\n" +
                "  #  comment 3 bis\n" +
                "  #  comment 3 ter\n" +
                "\n" +
                "################################\n" +
                "## comment 4\n" +
                "##  comment 5\n", conf);
        check("---\n" +
                "#comment\n" +
                "\n" +
                "test:  #    description\n" +
                "  - foo  #  bar\n" +
                "  - hello  #world\n" +
                "\n" +
                "# comment 2\n" +
                "#comment 3\n" +
                "  #comment 3 bis\n" +
                "  #  comment 3 ter\n" +
                "\n" +
                "################################\n" +
                "## comment 4\n" +
                "##comment 5\n", conf,
                getLintProblem(2, 2), getLintProblem(6, 13),
                getLintProblem(9, 2), getLintProblem(10, 4),
                getLintProblem(15, 3));
    }

    public void testShebang() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments:",
                "  require-starting-space: true",
                "  ignore-shebangs: false",
                "comments-indentation: disable",
                "document-start: disable");
        check("#!/bin/env my-interpreter\n", conf,
                getLintProblem(1, 2));
        check("# comment\n" +
                "#!/bin/env my-interpreter\n", conf,
                getLintProblem(2, 2));
        check("#!/bin/env my-interpreter\n" +
                "---\n" +
                "#comment\n" +
                "#!/bin/env my-interpreter\n" +
                "", conf,
                getLintProblem(1, 2),
                getLintProblem(3, 2),
                getLintProblem(4, 2));
        check("#! not a shebang\n", conf, getLintProblem(1, 2));
        check("key:  #!/not/a/shebang\n", conf, getLintProblem(1, 8));
    }

    public void testIgnoreShebang() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments:",
                "  require-starting-space: true",
                "  ignore-shebangs: true",
                "comments-indentation: disable",
                "document-start: disable");
        check("#!/bin/env my-interpreter\n", conf);
        check("# comment\n" +
                "#!/bin/env my-interpreter\n", conf,
                getLintProblem(2, 2));
        check("#!/bin/env my-interpreter\n" +
                "---\n" +
                "#comment\n" +
                "#!/bin/env my-interpreter\n", conf,
                getLintProblem(3, 2), getLintProblem(4, 2));
        check("#! not a shebang\n", conf, getLintProblem(1, 2));
        check("key:  #!/not/a/shebang\n", conf, getLintProblem(1, 8));
    }

    public void testSpacesFromContent() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments:",
                "  require-starting-space: false",
                "  min-spaces-from-content: 2");
        check("---\n" +
                "# comment\n" +
                "\n" +
                "test:  #    description\n" +
                "  - foo  #  bar\n" +
                "  - hello  #world\n" +
                "\n" +
                "string: \"Une longue phrase.\"  # this is French\n", conf);
        check("---\n" +
                "# comment\n" +
                "\n" +
                "test: #    description\n" +
                "  - foo  # bar\n" +
                "  - hello #world\n" +
                "\n" +
                "string: \"Une longue phrase.\" # this is French\n", conf,
                getLintProblem(4, 7), getLintProblem(6, 11), getLintProblem(8, 30));
    }

    public void testBoth() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments:",
                "  require-starting-space: true",
                "  min-spaces-from-content: 2",
                "comments-indentation: disable");
        check("---\n" +
                "#comment\n" +
                "\n" +
                "test: #    description\n" +
                "  - foo  # bar\n" +
                "  - hello #world\n" +
                "\n" +
                "# comment 2\n" +
                "#comment 3\n" +
                "  #comment 3 bis\n" +
                "  #  comment 3 ter\n" +
                "\n" +
                "################################\n" +
                "## comment 4\n" +
                "##comment 5\n" +
                "\n" +
                "string: \"Une longue phrase.\" # this is French\n", conf,
                getLintProblem(2, 2),
                getLintProblem(4, 7),
                getLintProblem(6, 11),
                getLintProblem(6, 12),
                getLintProblem(9, 2),
                getLintProblem(10, 4),
                getLintProblem(15, 3),
                getLintProblem(17, 30));
    }

    public void testEmptyComment() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments:",
                "  require-starting-space: true",
                "  min-spaces-from-content: 2");
        check("---\n" +
                "# This is paragraph 1.\n" +
                "#\n" +
                "# This is paragraph 2.\n", conf);
        check("---\n" +
                "inline: comment  #\n" +
                "foo: bar\n", conf);
    }

    public void testFirstLine() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments:",
                    "  require-starting-space: true",
                    "  min-spaces-from-content: 2");
        check("# comment\n", conf);
    }

    public void testLastLine() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments:",
                "  require-starting-space: true",
                "  min-spaces-from-content: 2",
                "new-line-at-end-of-file: disable");
        check("# comment with no newline char:\n" +
                "#", conf);
    }

    public void testMultiLineScalar() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments:",
                "  require-starting-space: true",
                "  min-spaces-from-content: 2",
                "trailing-spaces: disable");
        check("---\n" +
                "string: >\n" +
                "  this is plain text\n" +
                "\n" +
                "# comment\n", conf);
        check("---\n" +
                "- string: >\n" +
                "    this is plain text\n" +
                "  \n" +
                "  # comment\n", conf);
    }
}