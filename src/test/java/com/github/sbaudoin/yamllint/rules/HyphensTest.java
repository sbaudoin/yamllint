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

import static org.junit.jupiter.api.Assertions.assertThrows;

class HyphensTest extends RuleTester {
    @Test
    void testDisabled() throws YamlLintConfigException {
        testDisabled(getConfig("hyphens: disable"));
        testDisabled(getConfig("hyphens: {max-spaces-after: 5, min-spaces-after: -1}"));
        testDisabled(getConfig("hyphens: {max-spaces-after: -1, min-spaces-after: -1}"));
        testDisabled(getConfig("hyphens: {max-spaces-after: -1, min-spaces-after: 0}"));
    }

    private void testDisabled(YamlLintConfig conf) throws YamlLintConfigException {
        check("---\n" +
                "- elem1\n" +
                "- elem2\n", conf);
        check("---\n" +
                "- elem1\n" +
                "-  elem2\n", conf);
        check("---\n" +
                "-  elem1\n" +
                "-  elem2\n", conf);
        check("---\n" +
                "-  elem1\n" +
                "- elem2\n", conf);
        check("---\n" +
                "object:\n" +
                "  - elem1\n" +
                "  -  elem2\n", conf);
        check("---\n" +
                "object:\n" +
                "  -  elem1\n" +
                "  -  elem2\n", conf);
        check("---\n" +
                "object:\n" +
                "  subobject:\n" +
                "    - elem1\n" +
                "    -  elem2\n", conf);
        check("---\n" +
                "object:\n" +
                "  subobject:\n" +
                "    -  elem1\n" +
                "    -  elem2\n", conf);
        check("---\n" +
                "object:\n" +
                "  -elem2\n", conf);
    }

    @Test
    void testEnabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("hyphens: {max-spaces-after: 1}");
        check("---\n" +
                "- elem1\n" +
                "- elem2\n", conf);
        check("---\n" +
                "- elem1\n" +
                "-  elem2\n", conf, getLintProblem(3, 3));
        check("---\n" +
                "-  elem1\n" +
                "-  elem2\n", conf, getLintProblem(2, 3), getLintProblem(3, 3));
        check("---\n" +
                "-  elem1\n" +
                "- elem2\n", conf, getLintProblem(2, 3));
        check("---\n" +
                "object:\n" +
                "  - elem1\n" +
                "  -  elem2\n", conf, getLintProblem(4, 5));
        check("---\n" +
                "object:\n" +
                "  -  elem1\n" +
                "  -  elem2\n", conf, getLintProblem(3, 5), getLintProblem(4, 5));
        check("---\n" +
                "object:\n" +
                "  subobject:\n" +
                "    - elem1\n" +
                "    -  elem2\n", conf, getLintProblem(5, 7));
        check("---\n" +
                "object:\n" +
                "  subobject:\n" +
                "    -  elem1\n" +
                "    -  elem2\n", conf, getLintProblem(4, 7), getLintProblem(5, 7));
    }

    @Test
    void testMax3() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("hyphens: {max-spaces-after: 3}");
        check("---\n" +
                "-   elem1\n" +
                "-   elem2\n", conf);
        check("---\n" +
                "-    elem1\n" +
                "-   elem2\n", conf, getLintProblem(2, 5));
        check("---\n" +
                "a:\n" +
                "  b:\n" +
                "    -   elem1\n" +
                "    -   elem2\n", conf);
        check("---\n" +
                "a:\n" +
                "  b:\n" +
                "    -    elem1\n" +
                "    -    elem2\n", conf, getLintProblem(4, 9), getLintProblem(5, 9));
    }

    @Test
    void testInvalidSpaces() {
        assertThrows(YamlLintConfigException.class, () -> getConfig("hyphens: {max-spaces-after: 0}"));
        assertThrows(YamlLintConfigException.class, () -> getConfig("hyphens: {min-spaces-after: 3}"));
    }

    @Test
    void testMinSpace() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("hyphens: {max-spaces-after: 4, min-spaces-after: 3}");
        check("---\n" +
                "object:\n" +
                "  -   elem1\n" +
                "  -   elem2\n", conf);
        check("---\n" +
                "object:\n" +
                "  -    elem1\n" +
                "  -    elem2: -foo\n" +
                "-bar:\n", conf);
        check("---\n" +
                "object:\n" +
                "  -  elem1\n" +
                "  -  elem2\n", conf, getLintProblem(3, 6), getLintProblem(4, 6));

        conf = getConfig("hyphens: {max-spaces-after: 4, min-spaces-after: 3, check-scalars: true}");
        check("---\n" +
                "foo\n" +
                "-bar\n", conf);
        check("---\n" +
                "object:\n" +
                "  -    elem1\n" +
                "  -    elem2\n" +
                "key: -value\n", conf, getLintProblem(5, 6));
        check("---\n" +
                "list:\n" +
                "  -value\n", conf, getLintProblem(3, 3));
    }
}