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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public class ExtendedYamlLintConfigTest extends TestCase {
    public void testWrongExtend() {
        try {
            new YamlLintConfig("extends: null");
            fail("Invalid config not identified");
        } catch (YamlLintConfigException e) {
            assertEquals("invalid extends config: need to extend something", e.getMessage());
        }

        try {
            new YamlLintConfig("extends:");
            fail("Invalid config not identified");
        } catch (YamlLintConfigException e) {
            assertEquals("invalid extends config: need to extend something", e.getMessage());
        }

        try {
            new YamlLintConfig("extends:\n  - foo");
            fail("Invalid config not identified");
        } catch (YamlLintConfigException e) {
            assertTrue(e.getMessage().startsWith("invalid extends config: unknown error: "));
        }

        try {
            new YamlLintConfig("extends: dummy");
            fail("Unknown ruleset should not be extended");
        } catch (YamlLintConfigException e) {
            assertEquals("invalid extends config: Bundled configuration file \"dummy\" not found", e.getMessage());
        }

        try {
            new YamlLintConfig("extends: foo" + File.separator + "bar");
            fail("Unknown ruleset should not be extended");
        } catch (YamlLintConfigException e) {
            assertTrue(e.getCause() instanceof FileNotFoundException);
        }
    }

    @SuppressWarnings("unchecked")
    public void testExtendAddRule() throws YamlLintConfigException {
        YamlLintConfig oldConf = new YamlLintConfig("rules:\n" +
                "  colons:\n" +
                "    max-spaces-before: 0\n" +
                "    max-spaces-after: 1\n");
        YamlLintConfig newConf = new YamlLintConfig("rules:\n" +
                "  hyphens:\n" +
                "    max-spaces-after: 2\n");
        newConf.extend(oldConf);

        assertEquals(new HashSet(Arrays.asList("colons", "hyphens")), newConf.ruleConf.keySet());
        assertTrue(newConf.getRuleConf("colons") instanceof Map);
        assertEquals(0, ((Map)newConf.getRuleConf("colons")).get("max-spaces-before"));
        assertEquals(1, ((Map)newConf.getRuleConf("colons")).get("max-spaces-after"));
        assertTrue(newConf.getRuleConf("hyphens") instanceof Map);
        assertEquals(2, ((Map)newConf.getRuleConf("hyphens")).get("max-spaces-after"));

        assertEquals(2, newConf.getEnabledRules(null).size());
    }

    @SuppressWarnings("unchecked")
    public void testExtendRemoveRule() throws YamlLintConfigException {
        YamlLintConfig oldConf = new YamlLintConfig("rules:\n" +
                "  colons:\n" +
                "    max-spaces-before: 0\n" +
                "    max-spaces-after: 1\n" +
                "  hyphens:\n" +
                "    max-spaces-after: 2\n");
        YamlLintConfig newConf = new YamlLintConfig("rules:\n" +
                "  colons: disable\n");
        newConf.extend(oldConf);

        assertEquals(new HashSet(Arrays.asList("colons", "hyphens")), newConf.ruleConf.keySet());
        assertNull(newConf.getRuleConf("colons"));
        assertTrue(newConf.getRuleConf("hyphens") instanceof Map);
        assertEquals(2, ((Map)newConf.getRuleConf("hyphens")).get("max-spaces-after"));

        assertEquals(1, newConf.getEnabledRules(null).size());
    }

    @SuppressWarnings("unchecked")
    public void testExtendEditRule() throws YamlLintConfigException {
        YamlLintConfig oldConf = new YamlLintConfig("rules:\n" +
                "  colons:\n" +
                "    max-spaces-before: 0\n" +
                "    max-spaces-after: 1\n" +
                "  hyphens:\n" +
                "    max-spaces-after: 2\n");
        YamlLintConfig newConf = new YamlLintConfig("rules:\n" +
                "  colons:\n" +
                "    max-spaces-before: 3\n" +
                "    max-spaces-after: 4\n");
        newConf.extend(oldConf);

        assertEquals(new HashSet(Arrays.asList("colons", "hyphens")), newConf.ruleConf.keySet());
        assertTrue(newConf.getRuleConf("colons") instanceof Map);
        assertTrue(newConf.getRuleConf("hyphens") instanceof Map);
        assertEquals(3, ((Map)newConf.getRuleConf("colons")).get("max-spaces-before"));
        assertEquals(4, ((Map)newConf.getRuleConf("colons")).get("max-spaces-after"));
        assertEquals(2, ((Map)newConf.getRuleConf("hyphens")).get("max-spaces-after"));

        assertEquals(2, newConf.getEnabledRules(null).size());
    }

    @SuppressWarnings("unchecked")
    public void testExtendReenableRule() throws YamlLintConfigException {
        YamlLintConfig oldConf = new YamlLintConfig("rules:\n" +
                "  colons:\n" +
                "    max-spaces-before: 0\n" +
                "    max-spaces-after: 1\n" +
                "  hyphens: disable\n");
        YamlLintConfig newConf = new YamlLintConfig("rules:\n" +
                "  hyphens:\n" +
                "    max-spaces-after: 2\n");
        newConf.extend(oldConf);

        assertEquals(new HashSet(Arrays.asList("colons", "hyphens")), newConf.ruleConf.keySet());
        assertTrue(newConf.getRuleConf("colons") instanceof Map);
        assertTrue(newConf.getRuleConf("hyphens") instanceof Map);
        assertEquals(0, ((Map)newConf.getRuleConf("colons")).get("max-spaces-before"));
        assertEquals(1, ((Map)newConf.getRuleConf("colons")).get("max-spaces-after"));
        assertEquals(2, ((Map)newConf.getRuleConf("hyphens")).get("max-spaces-after"));

        assertEquals(2, newConf.getEnabledRules(null).size());
    }

    public void testExtendWithIgnore() throws YamlLintConfigException {
        YamlLintConfig oldConf = new YamlLintConfig("rules:\n" +
                "  colons:\n" +
                "    max-spaces-before: 0\n" +
                "    max-spaces-after: 1\n" +
                "ignore: foo.bar\n");
        YamlLintConfig newConf = new YamlLintConfig("rules:\n" +
                "  hyphens:\n" +
                "    max-spaces-after: 2\n");

        assertFalse(newConf.isFileIgnored("foo.bar"));
        newConf.extend(oldConf);
        assertTrue(newConf.isFileIgnored("foo.bar"));
    }
}
