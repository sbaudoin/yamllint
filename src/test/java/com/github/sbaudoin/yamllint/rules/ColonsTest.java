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
import org.yaml.snakeyaml.Yaml;

public class ColonsTest extends RuleTester {
    public void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: disable");
        check("---\n" +
                "object:\n" +
                "  k1 : v1\n" +
                "obj2:\n" +
                "  k2     :\n" +
                "    - 8\n" +
                "  k3:\n" +
                "    val\n" +
                "  property   : value\n" +
                "  prop2      : val2\n" +
                "  propriété  : [valeur]\n" +
                "  o:\n" +
                "    k1: [v1, v2]\n" +
                "  p:\n" +
                "    - k3: >\n" +
                "        val\n" +
                "    - o: {k1: v1}\n" +
                "    - p: kdjf\n" +
                "    - q: val0\n" +
                "    - q2:\n" +
                "        - val1\n" +
                "...\n", conf);
        check("---\n" +
                "object:\n" +
                "  k1:   v1\n" +
                "obj2:\n" +
                "  k2:\n" +
                "    - 8\n" +
                "  k3:\n" +
                "    val\n" +
                "  property:     value\n" +
                "  prop2:        val2\n" +
                "  propriété:    [valeur]\n" +
                "  o:\n" +
                "    k1:  [v1, v2]\n", conf);
        check("---\n" +
                "obj:\n" +
                "  p:\n" +
                "    - k1: >\n" +
                "        val\n" +
                "    - k3:  >\n" +
                "        val\n" +
                "    - o: {k1: v1}\n" +
                "    - o:  {k1: v1}\n" +
                "    - q2:\n" +
                "        - val1\n" +
                "...\n", conf);
        check("---\n" +
                "a: {b: {c:  d, e : f}}\n", conf);
    }

    public void testBeforeEnabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: 0, max-spaces-after: -1}");
        check("---\n" +
                "object:\n" +
                "  k1:\n" +
                "    - a\n" +
                "    - b\n" +
                "  k2: v2\n" +
                "...\n", conf);
        check("---\n" +
                "object:\n" +
                "  k1 :\n" +
                "    - a\n" +
                "    - b\n" +
                "  k2: v2\n" +
                "...\n", conf, getLintProblem(3, 5));
        check("---\n" +
                "lib :\n" +
                "  - var\n" +
                "...\n", conf, getLintProblem(2, 4));
        check("---\n" +
                "- lib :\n" +
                "    - var\n" +
                "...\n", conf, getLintProblem(2, 6));
        check("---\n" +
                "a: {b: {c : d, e : f}}\n", conf,
                getLintProblem(2, 10), getLintProblem(2, 17));
    }

    public void testBeforeMax() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: 3, max-spaces-after: -1}");
        check("---\n" +
                "object :\n" +
                "  k1   :\n" +
                "    - a\n" +
                "    - b\n" +
                "  k2  : v2\n" +
                "...\n", conf);
        check("---\n" +
                "object :\n" +
                "  k1    :\n" +
                "    - a\n" +
                "    - b\n" +
                "  k2  : v2\n" +
                "...\n", conf, getLintProblem(3, 8));
    }

    public void testBeforeWithExplicitBlockMappings() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: 0, max-spaces-after: 1}");
        check("---\n" +
                "object:\n" +
                "  ? key\n" +
                "  : value\n" +
                "...\n", conf);
        check("---\n" +
                "object :\n" +
                "  ? key\n" +
                "  : value\n" +
                "...\n", conf, getLintProblem(2, 7));
        check("---\n" +
                "? >\n" +
                "    multi-line\n" +
                "    key\n" +
                ": >\n" +
                "    multi-line\n" +
                "    value\n" +
                "...\n", conf);
        check("---\n" +
                "- ? >\n" +
                "      multi-line\n" +
                "      key\n" +
                "  : >\n" +
                "      multi-line\n" +
                "      value\n" +
                "...\n", conf);
        check("---\n" +
                "- ? >\n" +
                "      multi-line\n" +
                "      key\n" +
                "  :  >\n" +
                "       multi-line\n" +
                "       value\n" +
                "...\n", conf, getLintProblem(5, 5));
    }

    public void testAfterEnabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: -1, max-spaces-after: 1}");
        check("---\n" +
                "key: value\n", conf);
        check("---\n" +
                "key:  value\n", conf, getLintProblem(2, 6));
        check("---\n" +
                "object:\n" +
                "  k1:  [a, b]\n" +
                "  k2: string\n", conf, getLintProblem(3, 7));
        check("---\n" +
                "object:\n" +
                "  k1: [a, b]\n" +
                "  k2:  string\n", conf, getLintProblem(4, 7));
        check("---\n" +
                "object:\n" +
                "  other: {key:  value}\n" +
                "...\n", conf, getLintProblem(3, 16));
        check("---\n" +
                "a: {b: {c:  d, e :  f}}\n", conf,
                getLintProblem(2, 12), getLintProblem(2, 20));
    }

    public void testAfterEnabledQuestionMark() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: -1, max-spaces-after: 1}");
        check("---\n" +
                "? key\n" +
                ": value\n", conf);
        check("---\n" +
                "?  key\n" +
                ": value\n", conf, getLintProblem(2, 3));
        check("---\n" +
                "?  key\n" +
                ":  value\n", conf, getLintProblem(2, 3), getLintProblem(3, 3));
        check("---\n" +
                "- ?  key\n" +
                "  :  value\n", conf, getLintProblem(2, 5), getLintProblem(3, 5));
    }

    public void testAfterMax() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: -1, max-spaces-after: 3}");
        check("---\n" +
                "object:\n" +
                "  k1:  [a, b]\n", conf);
        check("---\n" +
                "object:\n" +
                "  k1:    [a, b]\n", conf, getLintProblem(3, 9));
        check("---\n" +
                "object:\n" +
                "  k2:  string\n", conf);
        check("---\n" +
                "object:\n" +
                "  k2:    string\n", conf, getLintProblem(3, 9));
        check("---\n" +
                "object:\n" +
                "  other: {key:  value}\n" +
                "...\n", conf);
        check("---\n" +
                "object:\n" +
                "  other: {key:    value}\n" +
                "...\n", conf, getLintProblem(3, 18));
    }

    public void testAfterWithExplicitBlockMappings() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: -1, max-spaces-after: 1}");
        check("---\n" +
                "object:\n" +
                "  ? key\n" +
                "  : value\n" +
                "...\n", conf);
        check("---\n" +
                "object:\n" +
                "  ? key\n" +
                "  :  value\n" +
                "...\n", conf, getLintProblem(4, 5));
    }

    public void testAfterDoNotConfoundWithTrailingSpace() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: 1, max-spaces-after: 1}",
                "trailing-spaces: disable");
        check("---\n" +
                "trailing:     \n" +
                "  - spaces\n", conf);
    }

    public void testBothBeforeAndAfter() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: 0, max-spaces-after: 1}");
        check("---\n" +
                "obj:\n" +
                "  string: text\n" +
                "  k:\n" +
                "    - 8\n" +
                "  k3:\n" +
                "    val\n" +
                "  property: [value]\n", conf);
        check("---\n" +
                "object:\n" +
                "  k1 :  v1\n", conf, getLintProblem(3, 5), getLintProblem(3, 8));
        check("---\n" +
                "obj:\n" +
                "  string:  text\n" +
                "  k :\n" +
                "    - 8\n" +
                "  k3:\n" +
                "    val\n" +
                "  property: {a: 1, b:  2, c : 3}\n", conf,
                getLintProblem(3, 11), getLintProblem(4, 4),
                getLintProblem(8, 23), getLintProblem(8, 28));
    }

    /**
     * Although accepted by PyYAML, `{*x: 4}` is not valid YAML: it should be
     * noted `{*x : 4}`. The reason is that a colon can be part of an anchor
     * name. See commit message for more details.
     */
    public void testWithAliasAsKey() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: 0, max-spaces-after: 1}");
        check("---\n" +
                "- anchor: &a key\n" +
                "- *a: 42\n" +
                "- {*a: 42}\n" +
                "- *a : 42\n" +
                "- {*a : 42}\n" +
                "- *a  : 42\n" +
                "- {*a  : 42}\n",
                conf,
                getLintProblem(7, 6), getLintProblem(8, 7));
    }
}