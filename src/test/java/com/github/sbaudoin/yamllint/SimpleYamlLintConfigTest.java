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

import junit.framework.TestCase;
import com.github.sbaudoin.yamllint.rules.Rule;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SimpleYamlLintConfigTest extends TestCase {
    public void testConstructorWithNull() throws IOException, YamlLintConfigException {
        try {
            new YamlLintConfig((String)null);
            fail("null argument should not be accepted");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            new YamlLintConfig((URL)null);
            fail("null argument should not be accepted");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @SuppressWarnings("unchecked")
    public void testParseConfig() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = new YamlLintConfig("rules:\n" +
                "  colons:\n" +
                "    max-spaces-before: 0\n" +
                "    max-spaces-after: 1\n");

        assertEquals(new HashSet(Arrays.asList("colons")), conf.ruleConf.keySet());
        assertTrue(conf.getRuleConf("colons") instanceof Map);
        assertEquals(0, ((Map)conf.getRuleConf("colons")).get("max-spaces-before"));
        assertEquals(1, ((Map)conf.getRuleConf("colons")).get("max-spaces-after"));

        assertEquals(1, conf.getEnabledRules(null).size());
    }

    public void testInvalidConf() throws IOException {
        try {
            new YamlLintConfig("");
            fail("Empty conf should be rejected");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }

        try {
            new YamlLintConfig("not: valid: yaml");
            fail("Invalid config not identified");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }
    }

    public void testUnknownRule() throws IOException {
        try {
            new YamlLintConfig("rules:\n" +
                    "  this-one-does-not-exist: enable\n");
            fail("Unknown rule not identified");
        } catch (YamlLintConfigException e) {
            assertEquals("invalid config: no such rule: \"this-one-does-not-exist\"", e.getMessage());
        }
    }

    public void testMissingOption() throws IOException {
        try {
            new YamlLintConfig("rules:\n" +
                "  colons:\n" +
                "    max-spaces-after: 1\n");
            fail("Missing rule option not identified");
        } catch (YamlLintConfigException e) {
            assertEquals("invalid config: missing option \"max-spaces-before\" for rule \"colons\"", e.getMessage());
        }
    }

    public void testUnknownOption() throws IOException {
        try {
            new YamlLintConfig("rules:\n" +
                    "  colons:\n" +
                    "    max-spaces-before: 0\n" +
                    "    max-spaces-after: 1\n" +
                    "    abcdef: yes\n");
            fail("Unknown option not identified");
        } catch (YamlLintConfigException e) {
            assertEquals("invalid config: unknown option \"abcdef\" for rule \"colons\"", e.getMessage());
        }
    }

    public void testYesNoForBooleans() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = new YamlLintConfig("rules:\n" +
                "  indentation:\n" +
                "    spaces: 2\n" +
                "    indent-sequences: true\n" +
                "    check-multi-line-strings: false\n");
        assertEquals(true, ((Map)conf.getRuleConf("indentation")).get("indent-sequences"));
        assertEquals(false, ((Map)conf.getRuleConf("indentation")).get("check-multi-line-strings"));

        conf = new YamlLintConfig("rules:\n" +
                "  indentation:\n" +
                "    spaces: 2\n" +
                "    indent-sequences: yes\n" +
                "    check-multi-line-strings: false\n");
        assertEquals(true, ((Map)conf.getRuleConf("indentation")).get("indent-sequences"));
        assertEquals(false, ((Map)conf.getRuleConf("indentation")).get("check-multi-line-strings"));

        conf = new YamlLintConfig("rules:\n" +
                "  indentation:\n" +
                "    spaces: 2\n" +
                "    indent-sequences: whatever\n" +
                "    check-multi-line-strings: false\n");
        assertEquals("whatever", ((Map)conf.getRuleConf("indentation")).get("indent-sequences"));
        assertEquals(false, ((Map)conf.getRuleConf("indentation")).get("check-multi-line-strings"));

        try {
            new YamlLintConfig("rules:\n" +
                    "  indentation:\n" +
                    "    spaces: 2\n" +
                    "    indent-sequences: YES!\n" +
                    "    check-multi-line-strings: false\n");
            fail("Invalid option value accepted");
        } catch (YamlLintConfigException e) {
            assertTrue(e.getMessage().startsWith("invalid config: option \"indent-sequences\" of \"indentation\" should be in "));
        }
    }

    @SuppressWarnings("unchecked")
    public void testValidateRuleConf() throws YamlLintConfigException, NoSuchMethodException, IllegalAccessException {
        Rule rule = getDummyRule();

        assertNull(YamlLintConfig.validateRuleConf(rule, null));
        assertNull(YamlLintConfig.validateRuleConf(rule, "disable"));

        assertEquals(toMap(new Object[][] { {"level", "error"} }), YamlLintConfig.validateRuleConf(rule, new HashMap()));
        assertEquals(toMap(new Object[][] { {"level", "error"} }), YamlLintConfig.validateRuleConf(rule, "enable"));

        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { {"level", "error"} }));
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { {"level", "warning"} }));
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { {"level", "info"} }));
        } catch (YamlLintConfigException e) {
            fail("Error level not recognized");
        }
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { {"level", "warn"} }));
            fail("Unsupported error level accepted");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }

        rule = getDummyRule(toMap(new Object[][] { { "length", Integer.class } }));
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "length", 8 } }));
        } catch (YamlLintConfigException e) {
            fail("Supported option value not accepted");
        }
        try {
            YamlLintConfig.validateRuleConf(rule, new HashMap<>());
            fail("Required options not checked");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "height", 8 } }));
            fail("Unknown option accepted");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }

        rule = getDummyRule(toMap(new Object[][] { { "a", Boolean.class }, { "b", Integer.class } }));
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "a", true }, { "b", 0 } }));
        } catch (YamlLintConfigException e) {
            fail("Supported option value not accepted");
        }
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "a", true } }));
            fail("Required options not checked");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "b", 0 } }));
            fail("Required options not checked");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "a", 1 }, { "b", 0 } }));
            fail("Unsupported option value accepted");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }

        rule = getDummyRule(toMap(new Object[][] { { "choice", Arrays.asList(true, 88, "str") } }));
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "choice", true } }));
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "choice", 88 } }));
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "choice", "str" } }));
        } catch (YamlLintConfigException e) {
            fail("Supported option value not accepted");
        }
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "choice", false } }));
            fail("Unsupported option value accepted");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "choice", 99 } }));
            fail("Unsupported option value accepted");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "choice", "abc" } }));
            fail("Unsupported option value accepted");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }

        rule = getDummyRule(toMap(new Object[][] { { "choice", Arrays.asList(Integer.class, "hardcoded") } }));
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "choice", 42 } }));
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "choice", "hardcoded" } }));
        } catch (YamlLintConfigException e) {
            fail("Supported option value not accepted");
        }
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "choice", false } }));
            fail("Unsupported option value accepted");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "choice", "abc" } }));
            fail("Unsupported option value accepted");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }
    }

    public void testIgnore() throws IOException, YamlLintConfigException {
        YamlLintConfig conf = new YamlLintConfig("rules:\n" +
                "  indentation:\n" +
                "    spaces: 2\n" +
                "    indent-sequences: true\n" +
                "    check-multi-line-strings: false\n" +
                "ignore: |\n" +
                "  .*\\.txt$\n" +
                "  foo.bar\n");
        assertTrue(conf.isFileIgnored("/my/file.txt"));
        assertTrue(conf.isFileIgnored("foo.bar"));
        assertFalse(conf.isFileIgnored("/anything/that/matches/nothing.doc"));
        assertFalse(conf.isFileIgnored("/foo.bar"));

        try {
            new YamlLintConfig("rules:\n" +
                    "  indentation:\n" +
                    "    spaces: 2\n" +
                    "    indent-sequences: true\n" +
                    "    check-multi-line-strings: false\n" +
                    "ignore:\n" +
                    "  - \".*\\.txt$\"\n" +
                    "  - foo.bar\n");
            fail("Invalid ignore syntax accepted");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }

        try {
            new YamlLintConfig("rules:\n" +
                    "  indentation:\n" +
                    "    spaces: 2\n" +
                    "    indent-sequences: true\n" +
                    "    check-multi-line-strings: false\n" +
                    "ignore: 3\n");
            fail("Invalid ignore syntax accepted");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }
    }


    private Map toMap(Object[][] o) {
        Map map = new HashMap();
        for (int i = 0; i < o.length; i++) {
            map.put(o[i][0], o[i][1]);
        }
        return map;
    }

    private Rule getDummyRule() {
        return getDummyRule(new HashMap<>());
    }

    private Rule getDummyRule(final Map<String, Object> o) {
        return new Rule() {
            {
                this.options = o;
            }

            @Override
            public TYPE getType() {
                return null;
            }
        };
    }
}
