/**
 * Copyright (c) 2018, Sylvain Baudoin
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

import com.github.sbaudoin.yamllint.rules.RuleTester;

import java.io.IOException;

public class YamlLintDirectivesTest extends RuleTester {
    public void testDisableDirective() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getDefaultConf();

        check("---\n" +
                "- [valid , YAML]\n" +
                "- trailing spaces    \n" +
                "- bad   : colon\n" +
                "- [valid , YAML]\n" +
                "- bad  : colon and spaces   \n" +
                "- [valid , YAML]\n",
            conf,
            getLintProblem(3, 18, "trailing-spaces"),
            getLintProblem(4, 8, "colons"),
            getLintProblem(6, 7, "colons"),
            getLintProblem(6, 26, "trailing-spaces"));
        check("---\n" +
                "- [valid , YAML]\n" +
                "- trailing spaces    \n" +
                "# yamllint disable\n" +
                "- bad   : colon\n" +
                "- [valid , YAML]\n" +
                "- bad  : colon and spaces   \n" +
                "- [valid , YAML]\n",
            conf,
            getLintProblem(3, 18, "trailing-spaces"));
        check("---\n" +
                "- [valid , YAML]\n" +
                "# yamllint disable\n" +
                "- trailing spaces    \n" +
                "- bad   : colon\n" +
                "- [valid , YAML]\n" +
                "# yamllint enable\n" +
                "- bad  : colon and spaces   \n" +
                "- [valid , YAML]\n",
            conf,
            getLintProblem(8, 7, "colons"),
            getLintProblem(8, 26, "trailing-spaces"));
    }

    public void testDisableDirectiveWithRules() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getDefaultConf();

        check("---\n" +
                "- [valid , YAML]\n" +
                "- trailing spaces    \n" +
                "# yamllint disable rule:trailing-spaces\n" +
                "- bad   : colon\n" +
                "- [valid , YAML]\n" +
                "- bad  : colon and spaces   \n" +
                "- [valid , YAML]\n",
            conf,
            getLintProblem(3, 18, "trailing-spaces"),
            getLintProblem(5, 8, "colons"),
            getLintProblem(7, 7, "colons"));
        check("---\n" +
                "- [valid , YAML]\n" +
                "# yamllint disable rule:trailing-spaces\n" +
                "- trailing spaces    \n" +
                "- bad   : colon\n" +
                "- [valid , YAML]\n" +
                "# yamllint enable rule:trailing-spaces\n" +
                "- bad  : colon and spaces   \n" +
                "- [valid , YAML]\n",
            conf,
            getLintProblem(5, 8, "colons"),
            getLintProblem(8, 7, "colons"),
            getLintProblem(8, 26, "trailing-spaces"));
        check("---\n" +
                "- [valid , YAML]\n" +
                "# yamllint disable rule:trailing-spaces\n" +
                "- trailing spaces    \n" +
                "- bad   : colon\n" +
                "- [valid , YAML]\n" +
                "# yamllint enable\n" +
                "- bad  : colon and spaces   \n" +
                "- [valid , YAML]\n",
            conf,
            getLintProblem(5, 8, "colons"),
            getLintProblem(8, 7, "colons"),
            getLintProblem(8, 26, "trailing-spaces"));
        check("---\n" +
                "- [valid , YAML]\n" +
                "# yamllint disable\n" +
                "- trailing spaces    \n" +
                "- bad   : colon\n" +
                "- [valid , YAML]\n" +
                "# yamllint enable rule:trailing-spaces\n" +
                "- bad  : colon and spaces   \n" +
                "- [valid , YAML]\n",
            conf,
            getLintProblem(8, 26, "trailing-spaces"));
        check("---\n" +
                "- [valid , YAML]\n" +
                "# yamllint disable rule:colons\n" +
                "- trailing spaces    \n" +
                "# yamllint disable rule:trailing-spaces\n" +
                "- bad   : colon\n" +
                "- [valid , YAML]\n" +
                "# yamllint enable rule:colons\n" +
                "- bad  : colon and spaces   \n" +
                "- [valid , YAML]\n",
            conf,
            getLintProblem(4, 18, "trailing-spaces"),
            getLintProblem(9, 7, "colons"));
    }

    public void testDisableLineDirective() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getDefaultConf();

        check("---\n" +
                "- [valid , YAML]\n" +
                "- trailing spaces    \n" +
                "# yamllint disable-line\n" +
                "- bad   : colon\n" +
                "- [valid , YAML]\n" +
                "- bad  : colon and spaces   \n" +
                "- [valid , YAML]\n",
            conf,
            getLintProblem(3, 18, "trailing-spaces"),
            getLintProblem(7, 7, "colons"),
            getLintProblem(7, 26, "trailing-spaces"));
        check("---\n" +
                "- [valid , YAML]\n" +
                "- trailing spaces    \n" +
                "- bad   : colon  # yamllint disable-line\n" +
                "- [valid , YAML]\n" +
                "- bad  : colon and spaces   \n" +
                "- [valid , YAML]\n",
            conf,
            getLintProblem(3, 18, "trailing-spaces"),
            getLintProblem(6, 7, "colons"),
            getLintProblem(6, 26, "trailing-spaces"));
        check("---\n" +
                "- [valid , YAML]\n" +
                "- trailing spaces    \n" +
                "- bad   : colon\n" +
                "- [valid , YAML]  # yamllint disable-line\n" +
                "- bad  : colon and spaces   \n" +
                "- [valid , YAML]\n",
            conf,
            getLintProblem(3, 18, "trailing-spaces"),
            getLintProblem(4, 8, "colons"),
            getLintProblem(6, 7, "colons"),
            getLintProblem(6, 26, "trailing-spaces"));
    }

    public void testDisableLineDirectiveWithRules() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getDefaultConf();

        check("---\n" +
                "- [valid , YAML]\n" +
                "# yamllint disable-line rule:colons\n" +
                "- trailing spaces    \n" +
                "- bad   : colon\n" +
                "- [valid , YAML]\n" +
                "- bad  : colon and spaces   \n" +
                "- [valid , YAML]\n",
            conf,
            getLintProblem(4, 18, "trailing-spaces"),
            getLintProblem(5, 8, "colons"),
            getLintProblem(7, 7, "colons"),
            getLintProblem(7, 26, "trailing-spaces"));
        check("---\n" +
                "- [valid , YAML]\n" +
                "- trailing spaces  # yamllint disable-line rule:colons  \n" +
                "- bad   : colon\n" +
                "- [valid , YAML]\n" +
                "- bad  : colon and spaces   \n" +
                "- [valid , YAML]\n",
            conf,
            getLintProblem(3, 55, "trailing-spaces"),
            getLintProblem(4, 8, "colons"),
            getLintProblem(6, 7, "colons"),
            getLintProblem(6, 26, "trailing-spaces"));
        check("---\n" +
                "- [valid , YAML]\n" +
                "- trailing spaces    \n" +
                "# yamllint disable-line rule:colons\n" +
                "- bad   : colon\n" +
                "- [valid , YAML]\n" +
                "- bad  : colon and spaces   \n" +
                "- [valid , YAML]\n",
            conf,
            getLintProblem(3, 18, "trailing-spaces"),
            getLintProblem(7, 7, "colons"),
            getLintProblem(7, 26, "trailing-spaces"));
        check("---\n" +
                "- [valid , YAML]\n" +
                "- trailing spaces    \n" +
                "- bad   : colon  # yamllint disable-line rule:colons\n" +
                "- [valid , YAML]\n" +
                "- bad  : colon and spaces   \n" +
                "- [valid , YAML]\n",
            conf,
            getLintProblem(3, 18, "trailing-spaces"),
            getLintProblem(6, 7, "colons"),
            getLintProblem(6, 26, "trailing-spaces"));
        check("---\n" +
                "- [valid , YAML]\n" +
                "- trailing spaces    \n" +
                "- bad   : colon\n" +
                "- [valid , YAML]\n" +
                "# yamllint disable-line rule:colons\n" +
                "- bad  : colon and spaces   \n" +
                "- [valid , YAML]\n",
            conf,
            getLintProblem(3, 18, "trailing-spaces"),
            getLintProblem(4, 8, "colons"),
            getLintProblem(7, 26, "trailing-spaces"));
        check("---\n" +
                "- [valid , YAML]\n" +
                "- trailing spaces    \n" +
                "- bad   : colon\n" +
                "- [valid , YAML]\n" +
                "# yamllint disable-line rule:colons rule:trailing-spaces\n" +
                "- bad  : colon and spaces   \n" +
                "- [valid , YAML]\n",
            conf,
            getLintProblem(3, 18, "trailing-spaces"),
            getLintProblem(4, 8, "colons"));
    }

    public void testDirectiveOnLastLine() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConf("new-line-at-end-of-file: {}");

        check("---\n" +
                "no new line",
            conf,
            getLintProblem(2, 12, "new-line-at-end-of-file"));
        check("---\n" +
                "# yamllint disable\n" +
                "no new line",
            conf);
        check("---\n" +
                "no new line  # yamllint disable",
            conf);
    }

    public void testIndentedDirective() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConf("brackets: {min-spaces-inside: 0, max-spaces-inside: 0}");

        check("---\n" +
                "- a: 1\n" +
                "  b:\n" +
                "    c: [    x]\n",
            conf,
            getLintProblem(4, 12, "brackets"));
        check("---\n" +
                "- a: 1\n" +
                "  b:\n" +
                "    # yamllint disable-line rule:brackets\n" +
                "    c: [    x]\n",
            conf);
    }

    public void testDirectiveOnItself() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConf("comments: {min-spaces-from-content: 2}\n",
                "comments-indentation: {}\n");

        check("---\n" +
                "- a: 1 # comment too close\n" +
                "  b:\n" +
                " # wrong indentation\n" +
                "    c: [x]\n",
            conf,
            getLintProblem(2, 8, "comments"),
            getLintProblem(4, 2, "comments-indentation"));
        check("---\n" +
                "# yamllint disable\n" +
                "- a: 1 # comment too close\n" +
                "  b:\n" +
                " # wrong indentation\n" +
                "    c: [x]\n",
            conf);
        check("---\n" +
                "- a: 1 # yamllint disable-line\n" +
                "  b:\n" +
                "    # yamllint disable-line\n" +
                " # wrong indentation\n" +
                "    c: [x]\n",
            conf);
        check("---\n" +
                "- a: 1 # yamllint disable-line rule:comments\n" +
                "  b:\n" +
                "    # yamllint disable-line rule:comments-indentation\n" +
                " # wrong indentation\n" +
                "    c: [x]\n",
            conf);
        check("---\n" +
                "# yamllint disable\n" +
                "- a: 1 # comment too close\n" +
                "  # yamllint enable rule:comments-indentation\n" +
                "  b:\n" +
                " # wrong indentation\n" +
                "    c: [x]\n",
            conf,
            getLintProblem(6, 2, "comments-indentation"));
    }


    private YamlLintConfig getConf(String... rules) throws IOException, YamlLintConfigException {
        StringBuilder sb = new StringBuilder("---\nextends: default\nrules:\n");

        if (rules != null) {
            for (String rule : rules) {
                sb.append("  ").append(rule);
            }
        }

        return new YamlLintConfig(sb.toString());
    }

    private YamlLintConfig getDefaultConf() throws IOException, YamlLintConfigException {
        return getConf("commas: disable\n",
                "trailing-spaces: {}\n",
                "colons: {max-spaces-before: 1}\n");
    }
}
