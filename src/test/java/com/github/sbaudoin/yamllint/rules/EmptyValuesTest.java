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

class EmptyValuesTest extends RuleTester {
    @Test
    void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: disable",
                "braces: disable",
                "commas: disable");
        check("---\n" +
                "foo:\n", conf);
        check("---\n" +
                "foo:\n" +
                " bar:\n", conf);
        check("---\n" +
                "{a:}\n", conf);
        check("---\n" +
                "foo: {a:}\n", conf);
        check("---\n" +
                "- {a:}\n" +
                "- {a:, b: 2}\n" +
                "- {a: 1, b:}\n" +
                "- {a: 1, b: , }\n", conf);
        check("---\n" +
                "{a: {b: , c: {d: 4, e:}}, f:}\n", conf);
    }

    @Test
    void testInBlockMappingsDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: false,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}");
        check("---\n" +
                "foo:\n", conf);
        check("---\n" +
                "foo:\n" +
                "bar: aaa\n", conf);
    }

    @Test
    void testInBlockMappingsSingleLine() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}");
        check("---\n" +
                "implicitly-null:\n", conf, getLintProblem(2, 17));
        check("---\n" +
                        "implicitly-null:with-colons:in-key:\n", conf,
                getLintProblem(2, 36));
        check("---\n" +
                        "implicitly-null:with-colons:in-key2:\n", conf,
                getLintProblem(2, 37));
    }

    @Test
    void testInBlockMappingsAllLines() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}");
        check("---\n" +
                        "foo:\n" +
                        "bar:\n" +
                        "foobar:\n", conf,
                getLintProblem(2, 5),
                getLintProblem(3, 5),
                getLintProblem(4, 8));
    }

    @Test
    void testInBlockMappingsExplicitEndOfDocument() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}");
        check("---\n" +
                "foo:\n" +
                "...\n", conf, getLintProblem(2, 5));
    }

    @Test
    void testInBlockMappingsNotEndOfDocument() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}");
        check("---\n" +
                "foo:\n" +
                "bar:\n" +
                " aaa\n", conf, getLintProblem(2, 5));
    }

    @Test
    void testInBlockMappingsDifferentLevel() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}");
        check("---\n" +
                "foo:\n" +
                " bar:\n" +
                "aaa: bbb\n", conf, getLintProblem(3, 6));
    }

    @Test
    void testInBlockMappingsEmptyFlowMapping() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}",
                "braces: disable",
                "commas: disable");
        check("---\n" +
                "foo: {a:}\n", conf);
        check("---\n" +
                "- {a:, b: 2}\n" +
                "- {a: 1, b:}\n" +
                "- {a: 1, b: , }\n", conf);
    }

    @Test
    void testInBlockMappingsEmptyBlockSequence() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}");
        check("---\n" +
                "foo:\n" +
                "  -\n", conf);
    }

    @Test
    void testInBlockMappingsNotEmptyOrExplicitNull() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}");
        check("---\n" +
                "foo:\n" +
                " bar:\n" +
                "  aaa\n", conf);
        check("---\n" +
                "explicitly-null: null\n", conf);
        check("---\n" +
                "explicitly-null:with-colons:in-key: null\n", conf);
        check("---\n" +
                "false-null: nulL\n", conf);
        check("---\n" +
                "empty-string: \"\"\n", conf);
        check("---\n" +
                "nullable-boolean: false\n", conf);
        check("---\n" +
                "nullable-int: 0\n", conf);
        check("---\n" +
                "First occurrence: &anchor Foo\n" +
                "Second occurrence: *anchor\n", conf);
    }

    @Test
    void testInBlockMappingsVariousExplicitNull() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}");
        check("---\n" +
                "null-alias: ~\n", conf);
        check("---\n" +
                "null-key1: {?: val}\n", conf);
        check("---\n" +
                "null-key2: {? !!null \"\": val}\n", conf);
    }

    @Test
    void testInBlockMappingsComments() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}",
                "comments: disable");
        check("---\n" +
                        "empty:  # comment\n" +
                        "foo:\n" +
                        "  bar: # comment\n", conf,
                getLintProblem(2, 7),
                getLintProblem(4, 7));
    }

    @Test
    void testInFlowMappingsDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: false,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}",
                "braces: disable",
                "commas: disable");
        check("---\n" +
                "{a:}\n", conf);
        check("---\n" +
                "foo: {a:}\n", conf);
        check("---\n" +
                "- {a:}\n" +
                "- {a:, b: 2}\n" +
                "- {a: 1, b:}\n" +
                "- {a: 1, b: , }\n", conf);
        check("---\n" +
                "{a: {b: , c: {d: 4, e:}}, f:}\n", conf);
    }

    @Test
    void testInFlowMappingsSingleLine() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: false,",
                "               forbid-in-flow-mappings: true," +
                "               forbid-in-block-sequences: false}",
                "braces: disable",
                "commas: disable");
        check("---\n" +
                        "{a:}\n", conf,
                getLintProblem(2, 4));
        check("---\n" +
                        "foo: {a:}\n", conf,
                getLintProblem(2, 9));
        check("---\n" +
                        "- {a:}\n" +
                        "- {a:, b: 2}\n" +
                        "- {a: 1, b:}\n" +
                        "- {a: 1, b: , }\n", conf,
                getLintProblem(2, 6),
                getLintProblem(3, 6),
                getLintProblem(4, 12),
                getLintProblem(5, 12));
        check("---\n" +
                        "{a: {b: , c: {d: 4, e:}}, f:}\n", conf,
                getLintProblem(2, 8),
                getLintProblem(2, 23),
                getLintProblem(2, 29));
    }

    @Test
    void testInFlowMappingsMultiLine() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: false,",
                "               forbid-in-flow-mappings: true," +
                "               forbid-in-block-sequences: false}",
                "braces: disable",
                "commas: disable");
        check("---\n" +
                        "foo: {\n" +
                        "  a:\n" +
                        "}\n", conf,
                getLintProblem(3, 5));
        check("---\n" +
                        "{\n" +
                        "  a: {\n" +
                        "    b: ,\n" +
                        "    c: {\n" +
                        "      d: 4,\n" +
                        "      e:\n" +
                        "    }\n" +
                        "  },\n" +
                        "  f:\n" +
                        "}\n", conf,
                getLintProblem(4, 7),
                getLintProblem(7, 9),
                getLintProblem(10, 5));
    }

    @Test
    void testInFlowMappingsVariousExplicitNull() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: false,",
                "               forbid-in-flow-mappings: true," +
                "               forbid-in-block-sequences: false}",
                "braces: disable",
                "commas: disable");
        check("---\n" +
                "{explicit-null: null}\n", conf);
        check("---\n" +
                "{null-alias: ~}\n", conf);
        check("---\n" +
                "null-key1: {?: val}\n", conf);
        check("---\n" +
                "null-key2: {? !!null \"\": val}\n", conf);
    }

    @Test
    void testInFlowMappingsComments() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: false,",
                "               forbid-in-flow-mappings: true," +
                "               forbid-in-block-sequences: false}",
                "braces: disable",
                "commas: disable",
                "comments: disable");
        check("---\n" +
                        "{\n" +
                        "  a: {\n" +
                        "    b: ,  # comment\n" +
                        "    c: {\n" +
                        "      d: 4,  # comment\n" +
                        "      e:  # comment\n" +
                        "    }\n" +
                        "  },\n" +
                        "  f:  # comment\n" +
                        "}\n", conf,
                getLintProblem(4, 7),
                getLintProblem(7, 9),
                getLintProblem(10, 5));
    }

    @Test
    void testInBlockSequencesDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: false," +
                "               forbid-in-flow-mappings: false,\n" +
                "               forbid-in-block-sequences: false}");
        check("---\n" +
                "foo:\n" +
                "  - bar\n" +
                "  -\n", conf);
        check("---\n" +
                "foo:\n" +
                "  -\n", conf);
    }

    @Test
    void testInBlockSequencesPrimativeItem() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: false,\n" +
                "               forbid-in-flow-mappings: false,\n" +
                "               forbid-in-block-sequences: true}");
        check("---\n" +
                "foo:\n" +
                "  -\n", conf,
                getLintProblem(3, 4));
        check("---\n" +
                "foo:\n" +
                "  - bar\n" +
                "  -\n", conf,
                getLintProblem(4, 4));
        check("---\n" +
                "foo:\n" +
                "  - 1\n" +
                "  - 2\n" +
                "  -\n", conf,
                getLintProblem(5, 4));
        check("---\n" +
                "foo:\n" +
                "  - true\n", conf);
    }

    @Test
    void test_in_block_sequences_complex_objects() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: false,\n" +
                "               forbid-in-flow-mappings: false,\n" +
                "               forbid-in-block-sequences: true}");
        check("---\n" +
                "foo:\n" +
                "  - a: 1\n", conf);
        check("---\n" +
                "foo:\n" +
                "  - a: 1\n" +
                "  -\n", conf,
                getLintProblem(4, 4));
        check("---\n" +
                "foo:\n" +
                "  - a: 1\n" +
                "    b: 2\n" +
                "  -\n", conf,
                getLintProblem(5, 4));
        check("---\n" +
                "foo:\n" +
                "  - a: 1\n" +
                "  - b: 2\n" +
                "  -\n", conf,
                getLintProblem(5, 4));
        check("---\n" +
                "foo:\n" +
                "  - - a\n" +
                "    - b: 2\n" +
                "    -\n", conf,
                getLintProblem(5, 6));
        check("---\n" +
                "foo:\n" +
                "  - - a\n" +
                "    - b: 2\n" +
                "  -\n", conf,
                getLintProblem(5, 4));
    }

    @Test
    void testInBlockSequencesVariousExplicitNull() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: false,\n" +
                "               forbid-in-flow-mappings: false,\n" +
                "               forbid-in-block-sequences: true}");
        check("---\n" +
                "foo:\n" +
                "  - null\n", conf);
        check("---\n" +
                "- null\n", conf);
        check("---\n" +
                "foo:\n" +
                "  - bar: null\n" +
                "  - null\n", conf);
        check("---\n" +
                "- null\n" +
                "- null\n", conf);
        check("---\n" +
                "- - null\n" +
                "  - null\n", conf);
    }
}