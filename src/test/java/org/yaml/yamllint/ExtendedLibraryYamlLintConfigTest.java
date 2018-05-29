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
package org.yaml.yamllint;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Map;

import static org.yaml.yamllint.rules.RuleTester.getFakeConfig;

public class ExtendedLibraryYamlLintConfigTest extends TestCase {
    public void testExtendConfigDisableRule() throws IOException, YamlLintConfigException {
        YamlLintConfig oldConf = getFakeConfig();
        YamlLintConfig newConf = new YamlLintConfig("extends: default\n" +
                "rules:\n" +
                "  trailing-spaces: disable\n");

        oldConf.ruleConf.put("trailing-spaces", null);

        assertEquals(newConf.ruleConf.keySet(), oldConf.ruleConf.keySet());
        for (String ruleId : newConf.ruleConf.keySet()) {
            assertEquals(newConf.ruleConf.get(ruleId), oldConf.ruleConf.get(ruleId));
        }
    }

    @SuppressWarnings("unchecked")
    public void testExtendConfigOverrideWholeRule() throws IOException, YamlLintConfigException {
        YamlLintConfig oldConf = getFakeConfig();
        YamlLintConfig newConf = new YamlLintConfig("extends: default\n" +
                "rules:\n" +
                "  empty-lines:\n" +
                "    max: 42\n" +
                "    max-start: 43\n" +
                "    max-end: 44\n");

        ((Map)oldConf.ruleConf.get("empty-lines")).put("max", 42);
        ((Map)oldConf.ruleConf.get("empty-lines")).put("max-start", 43);
        ((Map)oldConf.ruleConf.get("empty-lines")).put("max-end", 44);

        assertEquals(newConf.ruleConf.keySet(), oldConf.ruleConf.keySet());
        for (String ruleId : newConf.ruleConf.keySet()) {
            assertEquals(newConf.ruleConf.get(ruleId), oldConf.ruleConf.get(ruleId));
        }
    }

    public void testExtendConfigOverrideRulePartly() throws IOException, YamlLintConfigException {
        YamlLintConfig oldConf = getFakeConfig();
        YamlLintConfig newConf = new YamlLintConfig("extends: default\n" +
                "rules:\n" +
                "  empty-lines:\n" +
                "    max-start: 42\n");

        ((Map)oldConf.ruleConf.get("empty-lines")).put("max-start", 42);

        assertEquals(newConf.ruleConf.keySet(), oldConf.ruleConf.keySet());
        for (String ruleId : newConf.ruleConf.keySet()) {
            assertEquals(newConf.ruleConf.get(ruleId), oldConf.ruleConf.get(ruleId));
        }
    }
}
