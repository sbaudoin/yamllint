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
package com.github.sbaudoin.yamllint.rules;

import com.github.sbaudoin.yamllint.YamlLintConfig;
import com.github.sbaudoin.yamllint.YamlLintConfigException;

import java.io.IOException;

public class ScalarIndentationTest extends RuleTester {
    @Override
    public String getRuleId() {
        return "indentation";
    }

    public void testBasicsPlain() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: false}",
                "document-start: disable");
        check("multi\n" +
                "line\n", conf);
        check("multi\n" +
                " line\n", conf);
        check("- multi\n" +
                "  line\n", conf);
        check("- multi\n" +
                "   line\n", conf);
        check("a key: multi\n" +
                "       line\n", conf);
        check("a key: multi\n" +
                "  line\n", conf);
        check("a key: multi\n" +
                "        line\n", conf);
        check("a key:\n" +
                "  multi\n" +
                "  line\n", conf);
        check("- C code: void main() {\n" +
                "              printf(\"foo\");\n" +
                "          }\n", conf);
        check("- C code:\n" +
                "    void main() {\n" +
                "        printf(\"foo\");\n" +
                "    }\n", conf);
    }

    public void testCheckMultiLinePlain() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("multi\n" +
                " line\n", conf, getLintProblem(2, 2));
        check("- multi\n" +
                "   line\n", conf, getLintProblem(2, 4));
        check("a key: multi\n" +
                "  line\n", conf, getLintProblem(2, 3));
        check("a key: multi\n" +
                "        line\n", conf, getLintProblem(2, 9));
        check("a key:\n" +
                "  multi\n" +
                "   line\n", conf, getLintProblem(3, 4));
        check("- C code: void main() {\n" +
                "              printf(\"foo\");\n" +
                "          }\n", conf, getLintProblem(2, 15));
        check("- C code:\n" +
                "    void main() {\n" +
                "        printf(\"foo\");\n" +
                "    }\n", conf, getLintProblem(3, 9));
    }

    public void testBasicsQuoted() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: false}",
                "document-start: disable");
        check("\"multi\n" +
                " line\"\n", conf);
        check("- \"multi\n" +
                "   line\"\n", conf);
        check("a key: \"multi\n" +
                "        line\"\n", conf);
        check("a key:\n" +
                "  \"multi\n" +
                "   line\"\n", conf);
        check("- jinja2: \"{% if ansible is defined %}\n" +
                "             {{ ansible }}\n" +
                "           {% else %}\n" +
                "             {{ chef }}\n" +
                "           {% endif %}\"\n", conf);
        check("- jinja2:\n" +
                "    \"{% if ansible is defined %}\n" +
                "       {{ ansible }}\n" +
                "     {% else %}\n" +
                "       {{ chef }}\n" +
                "     {% endif %}\"\n", conf);
        check("[\"this is a very long line\n" +
                "  that needs to be split\",\n" +
                " \"other line\"]\n", conf);
        check("[\"multi\n" +
                "  line 1\", \"multi\n" +
                "            line 2\"]\n", conf);
    }

    public void testCheckMultiLineQuoted() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("\"multi\n" +
                "line\"\n", conf, getLintProblem(2, 1));
        check("\"multi\n" +
                "  line\"\n", conf, getLintProblem(2, 3));
        check("- \"multi\n" +
                "  line\"\n", conf, getLintProblem(2, 3));
        check("- \"multi\n" +
                "    line\"\n", conf, getLintProblem(2, 5));
        check("a key: \"multi\n" +
                "  line\"\n", conf, getLintProblem(2, 3));
        check("a key: \"multi\n" +
                "       line\"\n", conf, getLintProblem(2, 8));
        check("a key: \"multi\n" +
                "         line\"\n", conf, getLintProblem(2, 10));
        check("a key:\n" +
                "  \"multi\n" +
                "  line\"\n", conf, getLintProblem(3, 3));
        check("a key:\n" +
                "  \"multi\n" +
                "    line\"\n", conf, getLintProblem(3, 5));
        check("- jinja2: \"{% if ansible is defined %}\n" +
                "             {{ ansible }}\n" +
                "           {% else %}\n" +
                "             {{ chef }}\n" +
                "           {% endif %}\"\n", conf,
                getLintProblem(2, 14), getLintProblem(4, 14));
        check("- jinja2:\n" +
                "    \"{% if ansible is defined %}\n" +
                "       {{ ansible }}\n" +
                "     {% else %}\n" +
                "       {{ chef }}\n" +
                "     {% endif %}\"\n", conf,
                getLintProblem(3, 8), getLintProblem(5, 8));
        check("[\"this is a very long line\n" +
                "  that needs to be split\",\n" +
                " \"other line\"]\n", conf);
        check("[\"this is a very long line\n" +
                " that needs to be split\",\n" +
                " \"other line\"]\n", conf, getLintProblem(2, 2));
        check("[\"this is a very long line\n" +
                "   that needs to be split\",\n" +
                " \"other line\"]\n", conf, getLintProblem(2, 4));
        check("[\"multi\n" +
                "  line 1\", \"multi\n" +
                "            line 2\"]\n", conf);
        check("[\"multi\n" +
                "  line 1\", \"multi\n" +
                "           line 2\"]\n", conf, getLintProblem(3, 12));
        check("[\"multi\n" +
                "  line 1\", \"multi\n" +
                "             line 2\"]\n", conf, getLintProblem(3, 14));
    }

    public void testBasicsFoldedStyle() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: false}",
                "document-start: disable");
        check(">\n" +
                "  multi\n" +
                "  line\n", conf);
        check("- >\n" +
                "    multi\n" +
                "    line\n", conf);
        check("- key: >\n" +
                "    multi\n" +
                "    line\n", conf);
        check("- key:\n" +
                "    >\n" +
                "      multi\n" +
                "      line\n", conf);
        check("- ? >\n" +
                "      multi-line\n" +
                "      key\n" +
                "  : >\n" +
                "      multi-line\n" +
                "      value\n", conf);
        check("- ?\n" +
                "    >\n" +
                "      multi-line\n" +
                "      key\n" +
                "  :\n" +
                "    >\n" +
                "      multi-line\n" +
                "      value\n", conf);
        check("- jinja2: >\n" +
                "    {% if ansible is defined %}\n" +
                "      {{ ansible }}\n" +
                "    {% else %}\n" +
                "      {{ chef }}\n" +
                "    {% endif %}\n", conf);
    }

    public void testCheckMultiLineFoldedStyle() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check(">\n" +
                "  multi\n" +
                "   line\n", conf, getLintProblem(3, 4));
        check("- >\n" +
                "    multi\n" +
                "     line\n", conf, getLintProblem(3, 6));
        check("- key: >\n" +
                "    multi\n" +
                "     line\n", conf, getLintProblem(3, 6));
        check("- key:\n" +
                "    >\n" +
                "      multi\n" +
                "       line\n", conf, getLintProblem(4, 8));
        check("- ? >\n" +
                "      multi-line\n" +
                "       key\n" +
                "  : >\n" +
                "      multi-line\n" +
                "       value\n", conf,
                getLintProblem(3, 8), getLintProblem(6, 8));
        check("- ?\n" +
                "    >\n" +
                "      multi-line\n" +
                "       key\n" +
                "  :\n" +
                "    >\n" +
                "      multi-line\n" +
                "       value\n", conf,
                getLintProblem(4, 8), getLintProblem(8, 8));
        check("- jinja2: >\n" +
                "    {% if ansible is defined %}\n" +
                "      {{ ansible }}\n" +
                "    {% else %}\n" +
                "      {{ chef }}\n" +
                "    {% endif %}\n", conf,
                getLintProblem(3, 7), getLintProblem(5, 7));
    }

    public void testBasicsLiteralStyle() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: false}",
                "document-start: disable");
        check("|\n" +
                "  multi\n" +
                "  line\n", conf);
        check("- |\n" +
                "    multi\n" +
                "    line\n", conf);
        check("- key: |\n" +
                "    multi\n" +
                "    line\n", conf);
        check("- key:\n" +
                "    |\n" +
                "      multi\n" +
                "      line\n", conf);
        check("- ? |\n" +
                "      multi-line\n" +
                "      key\n" +
                "  : |\n" +
                "      multi-line\n" +
                "      value\n", conf);
        check("- ?\n" +
                "    |\n" +
                "      multi-line\n" +
                "      key\n" +
                "  :\n" +
                "    |\n" +
                "      multi-line\n" +
                "      value\n", conf);
        check("- jinja2: |\n" +
                "    {% if ansible is defined %}\n" +
                "     {{ ansible }}\n" +
                "    {% else %}\n" +
                "      {{ chef }}\n" +
                "    {% endif %}\n", conf);
    }

    public void testCheckMultiLineLiteralStyle() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("|\n" +
                "  multi\n" +
                "   line\n", conf, getLintProblem(3, 4));
        check("- |\n" +
                "    multi\n" +
                "     line\n", conf, getLintProblem(3, 6));
        check("- key: |\n" +
                "    multi\n" +
                "     line\n", conf, getLintProblem(3, 6));
        check("- key:\n" +
                "    |\n" +
                "      multi\n" +
                "       line\n", conf, getLintProblem(4, 8));
        check("- ? |\n" +
                "      multi-line\n" +
                "       key\n" +
                "  : |\n" +
                "      multi-line\n" +
                "       value\n", conf,
                getLintProblem(3, 8), getLintProblem(6, 8));
        check("- ?\n" +
                "    |\n" +
                "      multi-line\n" +
                "       key\n" +
                "  :\n" +
                "    |\n" +
                "      multi-line\n" +
                "       value\n", conf,
                getLintProblem(4, 8), getLintProblem(8, 8));
        check("- jinja2: |\n" +
                "    {% if ansible is defined %}\n" +
                "      {{ ansible }}\n" +
                "    {% else %}\n" +
                "      {{ chef }}\n" +
                "    {% endif %}\n", conf,
                getLintProblem(3, 7), getLintProblem(5, 7));
    }

    // The following "paragraph" examples are inspired from
    // http://stackoverflow.com/questions/3790454/in-yaml-how-do-i-break-a-string-over-multiple-lines

    public void testParagraphPlain() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("- long text: very \"long\"\n" +
                "             'string' with\n" +
                "\n" +
                "             paragraph gap, \\n and\n" +
                "             spaces.\n", conf);
        check("- long text: very \"long\"\n" +
                "    'string' with\n" +
                "\n" +
                "    paragraph gap, \\n and\n" +
                "    spaces.\n", conf,
                getLintProblem(2, 5), getLintProblem(4, 5), getLintProblem(5, 5));
        check("- long text:\n" +
                "    very \"long\"\n" +
                "    'string' with\n" +
                "\n" +
                "    paragraph gap, \\n and\n" +
                "    spaces.\n", conf);
    }

    public void testParagraphDoubleQuoted() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("- long text: \"very \\\"long\\\"\n" +
                "              'string' with\n" +
                "\n" +
                "              paragraph gap, \\n and\n" +
                "              spaces.\"\n", conf);
        check("- long text: \"very \\\"long\\\"\n" +
                "    'string' with\n" +
                "\n" +
                "    paragraph gap, \\n and\n" +
                "    spaces.\"\n", conf,
                getLintProblem(2, 5), getLintProblem(4, 5), getLintProblem(5, 5));
        check("- long text: \"very \\\"long\\\"\n" +
                "'string' with\n" +
                "\n" +
                "paragraph gap, \\n and\n" +
                "spaces.\"\n", conf,
                getLintProblem(2, 1), getLintProblem(4, 1), getLintProblem(5, 1));
        check("- long text:\n" +
                "    \"very \\\"long\\\"\n" +
                "     \'string\' with\n" +
                "\n" +
                "     paragraph gap, \\n and\n" +
                "     spaces.\"\n", conf);
    }

    public void testParagraphSingleQuoted() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("- long text: 'very \"long\"\n" +
                "              ''string'' with\n" +
                "\n" +
                "              paragraph gap, \\n and\n" +
                "              spaces.'\n", conf);
        check("- long text: 'very \"long\"\n" +
                "    ''string'' with\n" +
                "\n" +
                "    paragraph gap, \\n and\n" +
                "    spaces.'\n", conf,
                getLintProblem(2, 5), getLintProblem(4, 5), getLintProblem(5, 5));
        check("- long text: 'very \"long\"\n" +
                "''string'' with\n" +
                "\n" +
                "paragraph gap, \\n and\n" +
                "spaces.'\n", conf,
                getLintProblem(2, 1), getLintProblem(4, 1), getLintProblem(5, 1));
        check("- long text:\n" +
                "    'very \"long\"\n" +
                "     ''string'' with\n" +
                "\n" +
                "     paragraph gap, \\n and\n" +
                "     spaces.'\n", conf);
    }

    public void testParagraphFolded() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("- long text: >\n" +
                "    very \"long\"\n" +
                "    'string' with\n" +
                "\n" +
                "    paragraph gap, \\n and\n" +
                "    spaces.\n", conf);
        check("- long text: >\n" +
                "    very \"long\"\n" +
                "     'string' with\n" +
                "\n" +
                "      paragraph gap, \\n and\n" +
                "       spaces.\n", conf,
                getLintProblem(3, 6), getLintProblem(5, 7), getLintProblem(6, 8));
    }

    public void testParagraphLiteral() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("- long text: |\n" +
                "    very \"long\"\n" +
                "    'string' with\n" +
                "\n" +
                "    paragraph gap, \\n and\n" +
                "    spaces.\n", conf);
        check("- long text: |\n" +
                "    very \"long\"\n" +
                "     'string' with\n" +
                "\n" +
                "      paragraph gap, \\n and\n" +
                "       spaces.\n", conf,
                getLintProblem(3, 6), getLintProblem(5, 7), getLintProblem(6, 8));
    }

    public void testConsistent() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("multi\n" +
                "line\n", conf);
        check("multi\n" +
                " line\n", conf, getLintProblem(2, 2));
        check("- multi\n" +
                "  line\n", conf);
        check("- multi\n" +
                "   line\n", conf, getLintProblem(2, 4));
        check("a key: multi\n" +
                "  line\n", conf, getLintProblem(2, 3));
        check("a key: multi\n" +
                "        line\n", conf, getLintProblem(2, 9));
        check("a key:\n" +
                "  multi\n" +
                "   line\n", conf, getLintProblem(3, 4));
        check("- C code: void main() {\n" +
                "              printf(\"foo\");\n" +
                "          }\n", conf, getLintProblem(2, 15));
        check("- C code:\n" +
                "    void main() {\n" +
                "        printf(\"foo\");\n" +
                "    }\n", conf, getLintProblem(3, 9));
        check(">\n" +
                "  multi\n" +
                "  line\n", conf);
        check(">\n" +
                "     multi\n" +
                "     line\n", conf);
        check(">\n" +
                "     multi\n" +
                "      line\n", conf, getLintProblem(3, 7));
    }
}