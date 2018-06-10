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
package com.github.sbaudoin.yamllint.rules;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.List;
import java.util.logging.*;
import java.util.stream.Collectors;

public class RuleFactoryTest extends TestCase {
    public void testGetRule() {
        // Temporarily remove console handler
        Logger logger = Logger.getLogger(RuleFactory.class.getName()).getParent();
        ConsoleHandler ch = (ConsoleHandler)logger.getHandlers()[0];
        logger.removeHandler(ch);

        // Add memory handler to check logs
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamHandler sh = new StreamHandler(bos, new Formatter() {
            @Override
            public String format(LogRecord logRecord) {
                return logRecord.getMessage() + System.lineSeparator();
            }
        });
        sh.setLevel(Level.WARNING);
        logger.addHandler(sh);

        // All known rules
        assertNotNull(RuleFactory.instance.getRule("braces"));
        assertNotNull(RuleFactory.instance.getRule("brackets"));
        assertNotNull(RuleFactory.instance.getRule("colons"));
        assertNotNull(RuleFactory.instance.getRule("commas"));
        assertNotNull(RuleFactory.instance.getRule("comments"));
        assertNotNull(RuleFactory.instance.getRule("comments-indentation"));
        assertNotNull(RuleFactory.instance.getRule("document-end"));
        assertNotNull(RuleFactory.instance.getRule("document-start"));
        assertNotNull(RuleFactory.instance.getRule("empty-lines"));
        assertNotNull(RuleFactory.instance.getRule("empty-values"));
        assertNotNull(RuleFactory.instance.getRule("hyphens"));
        assertNotNull(RuleFactory.instance.getRule("indentation"));
        assertNotNull(RuleFactory.instance.getRule("key-duplicates"));
        assertNotNull(RuleFactory.instance.getRule("key-ordering"));
        assertNotNull(RuleFactory.instance.getRule("line-length"));
        assertNotNull(RuleFactory.instance.getRule("new-line-at-end-of-file"));
        assertNotNull(RuleFactory.instance.getRule("new-lines"));
        assertNotNull(RuleFactory.instance.getRule("octal-values"));
        assertNotNull(RuleFactory.instance.getRule("trailing-spaces"));
        assertNotNull(RuleFactory.instance.getRule("truthy"));

        // Unknown rule
        assertNull(RuleFactory.instance.getRule("this-rule-does-not-exist"));

        // Check broken rule warnings
        sh.flush();
        List<String> lines = new BufferedReader(new StringReader(bos.toString())).lines().collect(Collectors.toList());
        // The search to the unknown rule should have at least raised the exception in the BrokenRule constructor
        assertNotSame(0, lines.size());
        lines.forEach(line -> assertEquals("Cannot instantiate rule class com.github.sbaudoin.yamllint.rules.BrokenRule, will ignore it", line));

        // Set back console handler
        logger.removeHandler(sh);
        sh.close();
        logger.addHandler(ch);
    }
}
