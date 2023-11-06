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

class KeyDuplicatesTest extends RuleTester {
    @Test
    void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("key-duplicates: disable");
        check("---\n" +
                "block mapping:\n" +
                "  key: a\n" +
                "  otherkey: b\n" +
                "  key: c\n", conf);
        check("---\n" +
                "flow mapping:\n" +
                "  {key: a, otherkey: b, key: c}\n", conf);
        check("---\n" +
                "duplicated twice:\n" +
                "  - k: a\n" +
                "    ok: b\n" +
                "    k: c\n" +
                "    k: d\n", conf);
        check("---\n" +
                "duplicated twice:\n" +
                "  - {k: a, ok: b, k: c, k: d}\n", conf);
        check("---\n" +
                "multiple duplicates:\n" +
                "  a: 1\n" +
                "  b: 2\n" +
                "  c: 3\n" +
                "  d: 4\n" +
                "  d: 5\n" +
                "  b: 6\n", conf);
        check("---\n" +
                "multiple duplicates:\n" +
                "  {a: 1, b: 2, c: 3, d: 4, d: 5, b: 6}\n", conf);
        check("---\n" +
                "at: root\n" +
                "multiple: times\n" +
                "at: root\n", conf);
        check("---\n" +
                "nested but OK:\n" +
                "  a: {a: {a: 1}}\n" +
                "  b:\n" +
                "    b: 2\n" +
                "    c: 3\n", conf);
        check("---\n" +
                "nested duplicates:\n" +
                "  a: {a: 1, a: 1}\n" +
                "  b:\n" +
                "    c: 3\n" +
                "    d: 4\n" +
                "    d: 4\n" +
                "  b: 2\n", conf);
        check("---\n" +
                "duplicates with many styles: 1\n" +
                "\"duplicates with many styles\": 1\n" +
                "'duplicates with many styles': 1\n" +
                "? duplicates with many styles\n" +
                ": 1\n" +
                "? >-\n" +
                "    duplicates with\n" +
                "    many styles\n" +
                ": 1\n", conf);
            check("---\n" +
                "Merge Keys are OK:\n" +
                "anchor_one: &anchor_one\n" +
                "  one: one\n" +
                "anchor_two: &anchor_two\n" +
                "  two: two\n" +
                "anchor_reference:\n" +
                "  <<: *anchor_one\n" +
                "  <<: *anchor_two\n", conf);
        check("---\n" +
                "{a: 1, b: 2}}\n", conf, getSyntaxError(2, 13));
        check("---\n" +
                "[a, b, c]]\n", conf, getSyntaxError(2, 10));
    }

    @Test
    void testEnabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("key-duplicates: enable");
        check("---\n" +
                "block mapping:\n" +
                "  key: a\n" +
                "  otherkey: b\n" +
                "  key: c\n", conf,
                getLintProblem(5, 3));
        check("---\n" +
                "flow mapping:\n" +
                "  {key: a, otherkey: b, key: c}\n", conf,
                getLintProblem(3, 25));
        check("---\n" +
                "duplicated twice:\n" +
                "  - k: a\n" +
                "    ok: b\n" +
                "    k: c\n" +
                "    k: d\n", conf,
                getLintProblem(5, 5), getLintProblem(6, 5));
        check("---\n" +
                "duplicated twice:\n" +
                "  - {k: a, ok: b, k: c, k: d}\n", conf,
                getLintProblem(3, 19), getLintProblem(3, 25));
        check("---\n" +
                "multiple duplicates:\n" +
                "  a: 1\n" +
                "  b: 2\n" +
                "  c: 3\n" +
                "  d: 4\n" +
                "  d: 5\n" +
                "  b: 6\n", conf,
                getLintProblem(7, 3), getLintProblem(8, 3));
        check("---\n" +
                "multiple duplicates:\n" +
                "  {a: 1, b: 2, c: 3, d: 4, d: 5, b: 6}\n", conf,
                getLintProblem(3, 28), getLintProblem(3, 34));
        check("---\n" +
                "at: root\n" +
                "multiple: times\n" +
                "at: root\n", conf,
                getLintProblem(4, 1));
        check("---\n" +
                "nested but OK:\n" +
                "  a: {a: {a: 1}}\n" +
                "  b:\n" +
                "    b: 2\n" +
                "    c: 3\n", conf);
        check("---\n" +
                "nested duplicates:\n" +
                "  a: {a: 1, a: 1}\n" +
                "  b:\n" +
                "    c: 3\n" +
                "    d: 4\n" +
                "    d: 4\n" +
                "  b: 2\n", conf,
                getLintProblem(3, 13), getLintProblem(7, 5), getLintProblem(8, 3));
        check("---\n" +
                "duplicates with many styles: 1\n" +
                "\"duplicates with many styles\": 1\n" +
                "'duplicates with many styles': 1\n" +
                "? duplicates with many styles\n" +
                ": 1\n" +
                "? >-\n" +
                "    duplicates with\n" +
                "    many styles\n" +
                ": 1\n", conf,
                getLintProblem(3, 1), getLintProblem(4, 1), getLintProblem(5, 3),
                getLintProblem(7, 3));
        check("---\n" +
                "Merge Keys are OK:\n" +
                "anchor_one: &anchor_one\n" +
                "  one: one\n" +
                "anchor_two: &anchor_two\n" +
                "  two: two\n" +
                "anchor_reference:\n" +
                "  <<: *anchor_one\n" +
                "  <<: *anchor_two\n", conf);
        check("---\n" +
                "{a: 1, b: 2}}\n", conf, getSyntaxError(2, 13));
        check("---\n" +
                "[a, b, c]]\n", conf, getSyntaxError(2, 10));
    }

    @Test
    void testKeyTokensInFlowSequences() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("key-duplicates: enable");
        check("---\n" +
                "[\n" +
                "  flow: sequence, with, key: value, mappings\n" +
                "]\n", conf);
    }
}