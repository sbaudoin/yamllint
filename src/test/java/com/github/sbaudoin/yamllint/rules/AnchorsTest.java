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

public class AnchorsTest extends RuleTester {
    public void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("anchors: disable");
        check("---\n" +
                    "- &b true\n" +
                    "- &i 42\n" +
                    "- &s hello\n" +
                    "- &f_m {k: v}\n" +
                    "- &f_s [1, 2]\n" +
                    "- *b\n" +
                    "- *i\n" +
                    "- *s\n" +
                    "- *f_m\n" +
                    "- *f_s\n" +
                    "---\n" +                    // redeclare anchors in a new document
                    "- &b true\n" +
                    "- &i 42\n" +
                    "- &s hello\n" +
                    "- *b\n" +
                    "- *i\n" +
                    "- *s\n" +
                    "---\n" +
                    "block mapping: &b_m\n" +
                    "  key: value\n" +
                    "extended:\n" +
                    "  <<: *b_m\n" +
                    "  foo: bar\n" +
                    "---\n" +
                    "{a: 1, &x b: 2, c: &y 3, *x : 4, e: *y}\n" +
                    "...\n", conf);
        check("---\n" +
                "- &i 42\n" +
                "---\n" +
                "- &b true\n" +
                "- &b true\n" +
                "- &b true\n" +
                "- &s hello\n" +
                "- *b\n" +
                "- *i\n" +                       // declared in a previous document
                "- *f_m\n" +                     // never declared
                "- *f_m\n" +
                "- *f_m\n" +
                "- *f_s\n" +                     // declared after
                "- &f_s [1, 2]\n" +
                "---\n" +
                "block mapping: &b_m\n" +
                "  key: value\n" +
                "---\n" +
                "block mapping 1: &b_m_bis\n" +
                "  key: value\n" +
                "block mapping 2: &b_m_bis\n" +
                "  key: value\n" +
                "extended:\n" +
                "  <<: *b_m\n" +
                "  foo: bar\n" +
                "---\n" +
                "{a: 1, &x b: 2, c: &x 3, *x : 4, e: *y}\n" +
                "...\n", conf);
    }

    public void testForbidUndeclaredAliases() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("anchors:",
                "  forbid-undeclared-aliases: true",
                "  forbid-duplicated-anchors: false",
                "  forbid-unused-anchors: false");
        check("---\n" +
                "- &b true\n" +
                "- &i 42\n" +
                "- &s hello\n" +
                "- &f_m {k: v}\n" +
                "- &f_s [1, 2]\n" +
                "- *b\n" +
                "- *i\n" +
                "- *s\n" +
                "- *f_m\n" +
                "- *f_s\n" +
                "---\n" +                        // redeclare anchors in a new document
                "- &b true\n" +
                "- &i 42\n" +
                "- &s hello\n" +
                "- *b\n" +
                "- *i\n" +
                "- *s\n" +
                "---\n" +
                "block mapping: &b_m\n" +
                "  key: value\n" +
                "extended:\n" +
                "  <<: *b_m\n" +
                "  foo: bar\n" +
                "---\n" +
                "{a: 1, &x b: 2, c: &y 3, *x : 4, e: *y}\n" +
                "...\n", conf);
        check("---\n" +
                "- &i 42\n" +
                "---\n" +
                "- &b true\n" +
                "- &b true\n" +
                "- &b true\n" +
                "- &s hello\n" +
                "- *b\n" +
                "- *i\n" +                       // declared in a previous document
                "- *f_m\n" +                     // never declared
                "- *f_m\n" +
                "- *f_m\n" +
                "- *f_s\n" +                     // declared after
                "- &f_s [1, 2]\n" +
                "...\n" +
                "---\n" +
                "block mapping: &b_m\n" +
                "  key: value\n" +
                "---\n" +
                "block mapping 1: &b_m_bis\n" +
                "  key: value\n" +
                "block mapping 2: &b_m_bis\n" +
                "  key: value\n" +
                "extended:\n" +
                "  <<: *b_m\n" +
                "  foo: bar\n" +
                "---\n" +
                "{a: 1, &x b: 2, c: &x 3, *x : 4, e: *y}\n" +
                "...\n", conf,
                        getLintProblem(9, 3),
                getLintProblem(10, 3),
                getLintProblem(11, 3),
                getLintProblem(12, 3),
                getLintProblem (13, 3),
                getLintProblem(25, 7),
                getLintProblem(28, 37));
    }

    public void testForbidDuplicatedAnchors() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("anchors:",
               "  forbid-undeclared-aliases: false",
                "  forbid-duplicated-anchors: true",
                "  forbid-unused-anchors: false");
        check("---\n" +
                "- &b true\n" +
                "- &i 42\n" +
                "- &s hello\n" +
                "- &f_m {k: v}\n" +
                "- &f_s [1, 2]\n" +
                "- *b\n" +
                "- *i\n" +
                "- *s\n" +
                "- *f_m\n" +
                "- *f_s\n" +
                "---\n" +                        // redeclare anchors in a new document
                "- &b true\n" +
                "- &i 42\n" +
                "- &s hello\n" +
                "- *b\n" +
                "- *i\n" +
                "- *s\n" +
                "---\n" +
                "block mapping: &b_m\n" +
                "  key: value\n" +
                "extended:\n" +
                "  <<: *b_m\n" +
                "  foo: bar\n" +
                "---\n" +
                "{a: 1, &x b: 2, c: &y 3, *x : 4, e: *y}\n" +
                "...\n", conf);
        check("---\n" +
                "- &i 42\n" +
                "---\n" +
                "- &b true\n" +
                "- &b true\n" +
                "- &b true\n" +
                "- &s hello\n" +
                "- *b\n" +
                "- *i\n" +                       // declared in a previous document
                "- *f_m\n" +                     // never declared
                "- *f_m\n" +
                "- *f_m\n" +
                "- *f_s\n" +                     // declared after
                "- &f_s [1, 2]\n" +
                "...\n" +
                "---\n" +
                "block mapping: &b_m\n" +
                "  key: value\n" +
                "---\n" +
                "block mapping 1: &b_m_bis\n" +
                "  key: value\n" +
                "block mapping 2: &b_m_bis\n" +
                "  key: value\n" +
                "extended:\n" +
                "  <<: *b_m\n" +
                "  foo: bar\n" +
                "---\n" +
                "{a: 1, &x b: 2, c: &x 3, *x : 4, e: *y}\n" +
                "...\n", conf,
                getLintProblem(5, 3),
                getLintProblem(6, 3),
                getLintProblem(22, 18),
                getLintProblem(28, 20));
    }

    public void testForbidUnusedAnchors() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("anchors:",
                "  forbid-undeclared-aliases: false",
                "  forbid-duplicated-anchors: false",
                "  forbid-unused-anchors: true");

        check("---\n" +
                "- &b true\n" +
                "- &i 42\n" +
                "- &s hello\n" +
                "- &f_m {k: v}\n" +
                "- &f_s [1, 2]\n" +
                "- *b\n" +
                "- *i\n" +
                "- *s\n" +
                "- *f_m\n" +
                "- *f_s\n" +
                "---\n" +                        // redeclare anchors in a new document
                "- &b true\n" +
                "- &i 42\n" +
                "- &s hello\n" +
                "- *b\n" +
                "- *i\n" +
                "- *s\n" +
                "---\n" +
                "block mapping: &b_m\n" +
                "  key: value\n" +
                "extended:\n" +
                "  <<: *b_m\n" +
                "  foo: bar\n" +
                "---\n" +
                "{a: 1, &x b: 2, c: &y 3, *x : 4, e: *y}\n" +
                "...\n", conf);
        check("---\n" +
                "- &i 42\n" +
                "---\n" +
                "- &b true\n" +
                "- &b true\n" +
                "- &b true\n" +
                "- &s hello\n" +
                "- *b\n" +
                "- *i\n" +                       // declared in a previous document
                "- *f_m\n" +                     // never declared
                "- *f_m\n" +
                "- *f_m\n" +
                "- *f_s\n" +                     // declared after
                "- &f_s [1, 2]\n" +
                "...\n" +
                "---\n" +
                "block mapping: &b_m\n" +
                "  key: value\n" +
                "---\n" +
                "block mapping 1: &b_m_bis\n" +
                "  key: value\n" +
                "block mapping 2: &b_m_bis\n" +
                "  key: value\n" +
                "extended:\n" +
                "  <<: *b_m\n" +
                "  foo: bar\n" +
                "---\n" +
                "{a: 1, &x b: 2, c: &x 3, *x : 4, e: *y}\n" +
                "...\n", conf,
                getLintProblem(2, 3),
                getLintProblem(7, 3),
                getLintProblem(14, 3),
                getLintProblem(17, 16),
                getLintProblem(22, 18));
    }
}