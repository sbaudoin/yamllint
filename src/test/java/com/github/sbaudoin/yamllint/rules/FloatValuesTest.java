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

public class FloatValuesTest extends RuleTester {
    public void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("float-values: disable");
        check("---\n" +
                "- 0.0\n" +
                "- .NaN\n" +
                "- .INF\n" +
                "- .1\n" +
                "- 10e-6\n",
                conf);
    }

    public void testNumeralBeforeDecimal() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("float-values:",
                "  require-numeral-before-decimal: true",
                "  forbid-scientific-notation: false",
                "  forbid-nan: false",
                "  forbid-inf: false");
        check("---\n" +
                "- 0.0\n" +
                "- .1\n" +
                "- '.1'\n" +
                "- string.1\n" +
                "- .1string\n" +
                "- !custom_tag .2\n" +
                "- &angle1 0.0\n" +
                "- *angle1\n" +
                "- &angle2 .3\n" +
                "- *angle2\n",
                conf,
                getLintProblem(3, 3), getLintProblem(10, 11));
    }

    public void testScientificNotation() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("float-values:",
                "  require-numeral-before-decimal: false",
                "  forbid-scientific-notation: true",
                "  forbid-nan: false",
                "  forbid-inf: false");
        check("---\n" +
                "- 10e6\n" +
                "- 10e-6\n" +
                "- 0.00001\n" +
                "- '10e-6'\n" +
                "- string10e-6\n" +
                "- 10e-6string\n" +
                "- !custom_tag 10e-6\n" +
                "- &angle1 0.000001\n" +
                "- *angle1\n" +
                "- &angle2 10e-6\n" +
                "- *angle2\n" +
                "- &angle3 10e6\n" +
                "- *angle3\n",
                conf,
                getLintProblem(2, 3),
                getLintProblem(3, 3),
                getLintProblem(11, 11),
                getLintProblem(13, 11));
    }

    public void testNan() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("float-values:",
                "  require-numeral-before-decimal: false",
                "  forbid-scientific-notation: false",
                "  forbid-nan: true",
                "  forbid-inf: false");
        check("---\n" +
                "- .NaN\n" +
                "- .NAN\n" +
                "- '.NaN'\n" +
                "- a.NaN\n" +
                "- .NaNa\n" +
                "- !custom_tag .NaN\n" +
                "- &angle .nan\n" +
                "- *angle\n",
                conf,
                getLintProblem(2, 3),
                getLintProblem(3, 3),
                getLintProblem(8, 10));
    }

    public void testInf() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("float-values:",
                "  require-numeral-before-decimal: false",
                "  forbid-scientific-notation: false",
                "  forbid-nan: false",
                "  forbid-inf: true");
        check("---\n" +
                "- .inf\n" +
                "- .INF\n" +
                "- -.inf\n" +
                "- -.INF\n" +
                "- '.inf'\n" +
                "- ∞.infinity\n" +
                "- .infinity∞\n" +
                "- !custom_tag .inf\n" +
                "- &angle .inf\n" +
                "- *angle\n" +
                "- &angle -.inf\n" +
                "- *angle\n",
                conf,
                getLintProblem(2, 3),
                getLintProblem(3, 3),
                getLintProblem(4, 3),
                getLintProblem(5, 3),
                getLintProblem(10, 10),
                getLintProblem(12, 10));
    }
}