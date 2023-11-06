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

class OctalValuesTest extends RuleTester {
    @Test
    void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("octal-values: disable",
                "new-line-at-end-of-file: disable",
                "document-start: disable");
        check("user-city: 010", conf);
        check("user-city: 0o10", conf);
    }

    @Test
    void testImplicitOctalValues() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("octal-values: {forbid-implicit-octal: true, forbid-explicit-octal: false}",
                "new-line-at-end-of-file: disable",
                "document-start: disable");
        check("user-city: 010", conf, getLintProblem(1, 15));
        check("user-city: abc", conf);
        check("user-city: 010,0571", conf);
        check("user-city: \"010\"", conf);
        check("user-city: \"010\"", conf);
        check("user-city:\n" +
                "  - 010", conf, getLintProblem(2, 8));
        check("user-city: [010]", conf, getLintProblem(1, 16));
        check("user-city: {beijing: 010}", conf, getLintProblem(1, 25));
        check("explicit-octal: 0o10", conf);
        check("not-number: 0abc", conf);
        check("zero: 0", conf);
        check("hex-value: 0x10", conf);
        check("number-values:\n" +
                "  - 0.10\n" +
                "  - .01\n" +
                "  - 0e3\n", conf);
        check("with-decimal-digits: 012345678", conf);
        check("with-decimal-digits: 012345679", conf);
    }

    @Test
    void testExplicitOctalValues() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("octal-values: {forbid-implicit-octal: false, forbid-explicit-octal: true}",
                "new-line-at-end-of-file: disable",
                "document-start: disable");
        check("user-city: 0o10", conf, getLintProblem(1, 16));
        check("user-city: abc", conf);
        check("user-city: 0o10,0571", conf);
        check("user-city: \"0o10\"", conf);
        check("user-city:\n" +
                "  - 0o10", conf, getLintProblem(2, 9));
        check("user-city: [0o10]", conf, getLintProblem(1, 17));
        check("user-city: {beijing: 0o10}", conf, getLintProblem(1, 26));
        check("implicit-octal: 010", conf);
        check("not-number: 0oabc", conf);
        check("zero: 0", conf);
        check("hex-value: 0x10", conf);
        check("number-values:\n" +
                "  - 0.10\n" +
                "  - .01\n" +
                "  - 0e3\n", conf);
        check("user-city: \"010\"", conf);
        check("with-decimal-digits: 0o012345678", conf);
        check("with-decimal-digits: 0o012345679", conf);
    }
}