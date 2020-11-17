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
package com.github.sbaudoin.yamllint;

import junit.framework.TestCase;
import com.github.sbaudoin.yamllint.rules.Rule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
        try {
            new YamlLintConfig((InputStream) null);
            fail("null argument should not be accepted");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    public void testConstructorInputStream() {
        File confFile = Paths.get("src", "test", "resources", "config", "local", ".yamllint").toFile();
        try (InputStream in = new FileInputStream(confFile)) {
            new YamlLintConfig(in);
        } catch (Exception e) {
            fail("Should not fail: valid conf file passed");
        }
    }

    @SuppressWarnings("unchecked")
    public void testParseConfig() throws YamlLintConfigException {
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

    public void testInvalidConf() {
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

        try {
            new YamlLintConfig("ignore: 3");
            fail("Invalid config not identified");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }
    }

    public void testUnknownRule() {
        try {
            new YamlLintConfig("rules:\n" +
                    "  this-one-does-not-exist: enable\n");
            fail("Unknown rule not identified");
        } catch (YamlLintConfigException e) {
            assertEquals("invalid config: no such rule: \"this-one-does-not-exist\"", e.getMessage());
        }
    }

    public void testUnknownOption() {
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

    public void testYesNoForBooleans() throws YamlLintConfigException {
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
    public void testValidateRuleConf() throws YamlLintConfigException {
        Rule rule = getDummyRule();

        assertNull(YamlLintConfig.validateRuleConf(rule, null));
        assertNull(YamlLintConfig.validateRuleConf(rule, "disable"));

        assertEquals(toMap(new Object[][] { {"level", "error"} }), YamlLintConfig.validateRuleConf(rule, new HashMap()));
        assertEquals(toMap(new Object[][] { {"level", "error"} }), YamlLintConfig.validateRuleConf(rule, "enable"));

        try {
            YamlLintConfig.validateRuleConf(rule, "invalid conf");
            fail("Invalid configuration accepted");
        } catch (YamlLintConfigException e) {
            assertEquals("invalid config: rule \"dummy-rule\": should be either \"enable\", \"disable\" or a dictionary", e.getMessage());
        }

        // Ignore
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { {"ignore", 3} }));
            fail("Invalid configuration accepted");
        } catch (YamlLintConfigException e) {
            assertEquals("invalid config: ignore should contain regexp patterns", e.getMessage());
        }
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { {"ignore", Arrays.asList("foo", "bar")} }));
            assertTrue(rule.ignores(new File("foo")));
            assertTrue(rule.ignores(new File("bar")));
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { {"ignore", "foo\nbar"} }));
            assertTrue(rule.ignores(new File("foo")));
            assertTrue(rule.ignores(new File("bar")));
        } catch (YamlLintConfigException e) {
            fail("Error level not recognized");
        }

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
            assertEquals("invalid config: level should be \"error\", \"warning\" or \"info\"", e.getMessage());
        }

        rule = getDummyRule(toMap(new Object[][] { { "length", 0 } }));
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "length", 8 } }));
        } catch (YamlLintConfigException e) {
            fail("Supported option value not accepted: " + e.getMessage());
        }
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "height", 8 } }));
            fail("Unknown option accepted");
        } catch (YamlLintConfigException e) {
            assertEquals("invalid config: unknown option \"height\" for rule \"dummy-rule\"", e.getMessage());
        }

        rule = getDummyRule(toMap(new Object[][] { { "a", false }, { "b", 44 } }));
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "a", true }, { "b", 0 } }));
        } catch (YamlLintConfigException e) {
            fail("Supported option value not accepted");
        }
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "a", 1 }, { "b", 0 } }));
            fail("Unsupported option value accepted");
        } catch (YamlLintConfigException e) {
            assertEquals("invalid config: option \"a\" of \"dummy-rule\" should be of type boolean", e.getMessage());
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
            assertEquals("invalid config: option \"choice\" of \"dummy-rule\" should be in [true, 88, 'str']", e.getMessage());
        }
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "choice", 99 } }));
            fail("Unsupported option value accepted");
        } catch (YamlLintConfigException e) {
            assertEquals("invalid config: option \"choice\" of \"dummy-rule\" should be in [true, 88, 'str']", e.getMessage());
        }
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "choice", "abc" } }));
            fail("Unsupported option value accepted");
        } catch (YamlLintConfigException e) {
            assertEquals("invalid config: option \"choice\" of \"dummy-rule\" should be in [true, 88, 'str']", e.getMessage());
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
            assertEquals("invalid config: option \"choice\" of \"dummy-rule\" should be in [integer, 'hardcoded']", e.getMessage());
        }
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "choice", "abc" } }));
            fail("Unsupported option value accepted");
        } catch (YamlLintConfigException e) {
            assertEquals("invalid config: option \"choice\" of \"dummy-rule\" should be in [integer, 'hardcoded']", e.getMessage());
        }

        rule = getDummyRule(toMap(new Object[][] { { "errored", true } }));
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "errored", true } }));
            fail("Invalid conf accepted");
        } catch (YamlLintConfigException e) {
            assertEquals("invalid config: dummy-rule: the conf says to return an error message", e.getMessage());
        }

        // Test list options
        rule = getDummyRule(toMap(new Object[][] { { "alist", Collections.<String>emptyList() } }), true);
        try {
            YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "alist", "not a list" } }));
            fail("Invalid conf accepted");
        } catch (YamlLintConfigException e) {
            assertEquals("invalid config: option \"alist\" of \"dummy-rule\" should be a list", e.getMessage());
        }
        try {
            Map<String, Object> conf = YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] { { "alist", Arrays.asList("value1", "value2") } }));
            assertTrue(true);
        } catch (YamlLintConfigException e) {
            fail("Valid list conf failed: " + e.getMessage());
        }
    }

    public void testIgnore() throws YamlLintConfigException {
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

    public void testIsYamlFile() throws YamlLintConfigException {
        try {
            new YamlLintConfig("yaml-files:\n" +
                    "  indentation:\n" +
                    "    spaces: 2\n" +
                    "    indent-sequences: true\n" +
                    "    check-multi-line-strings: false\n");
            fail("Invalid yaml-files syntax accepted");
        } catch (YamlLintConfigException e) {
            assertTrue(true);
        }

        YamlLintConfig conf = new YamlLintConfig("extends: default\n");
        assertTrue(conf.isYamlFile("/my/file.yaml"));
        assertTrue(conf.isYamlFile("foo.yml"));
        assertFalse(conf.isYamlFile("/anything/that/a.yaml/donot.match"));
        assertFalse(conf.isYamlFile("/foo.Yaml"));

        conf = new YamlLintConfig("rules:\n" +
                "  colons:\n" +
                "    max-spaces-before: 0\n" +
                "    max-spaces-after: 1\n");
        assertTrue(conf.isYamlFile("/my/file.yaml"));
        assertTrue(conf.isYamlFile("foo.yml"));
        assertFalse(conf.isYamlFile("/anything/that/a.yaml/donot.match"));
        assertFalse(conf.isYamlFile("/foo.Yaml"));

        conf = new YamlLintConfig("yaml-files:\n" +
                "  - .*\\.match$\n");
        assertFalse(conf.isYamlFile("/my/file.yaml"));
        assertFalse(conf.isYamlFile("foo.yml"));
        assertTrue(conf.isYamlFile("/anything/that/a.yaml/donot.match"));
        assertFalse(conf.isYamlFile("/foo.Yaml"));
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
        return getDummyRule(o, false);
    }

    private Rule getDummyRule(final Map<String, Object> o, final boolean isList) {
        return new Rule() {
            {
                for (Map.Entry<String, Object> e : o.entrySet()) {
                    if (isList) {
                        registerListOption(e.getKey(), (List<?>)e.getValue());
                    } else {
                        registerOption(e.getKey(), e.getValue());
                    }
                }
            }

            @Override
            public TYPE getType() {
                return null;
            }

            @Override
            public String getId() {
                return "dummy-rule";
            }

            @Override
            public String validate(Map<String, Object> conf) {
                if (conf.containsKey("errored")) {
                    return "the conf says to return an error message";
                }
                return null;
            }
        };
    }
}
