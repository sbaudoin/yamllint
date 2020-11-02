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

public class LintProblemTest extends TestCase {
    public void testSimpleProblem() {
        LintProblem problem = new LintProblem(1, 2, "desc");
        problem.setLevel(Linter.ERROR_LEVEL);
        assertEquals(null, problem.getRuleId());
        assertEquals(1, problem.getLine());
        assertEquals(2, problem.getColumn());
        assertEquals(Linter.ERROR_LEVEL, problem.getLevel());
        assertEquals("desc", problem.getDesc());
        assertNull(problem.getExtraDesc());
        assertEquals("desc", problem.getMessage());
        assertEquals("desc", problem.getLongMessage());
        assertEquals("1:2:desc", problem.toString());
        assertTrue(problem.equals(new LintProblem(1, 2, null)));
        assertEquals(922381112, problem.hashCode());
    }

    public void testCompleteProblem() {
        LintProblem problem = new LintProblem(1, 2, "desc", "rule-id");
        problem.setLevel(Linter.ERROR_LEVEL);
        assertEquals("rule-id", problem.getRuleId());
        assertEquals(1, problem.getLine());
        assertEquals(2, problem.getColumn());
        assertEquals(Linter.ERROR_LEVEL, problem.getLevel());
        assertEquals("desc", problem.getDesc());
        assertNull(problem.getExtraDesc());
        assertEquals("desc (rule-id)", problem.getMessage());
        assertEquals("desc (rule-id)", problem.getLongMessage());
        assertEquals("1:2:desc (rule-id)", problem.toString());
        assertTrue(problem.equals(new LintProblem(1, 2, null, "rule-id")));
        assertEquals(-1290166725, problem.hashCode());
    }

    public void testExtraProblem() {
        LintProblem problem = new LintProblem(1, 2, "desc", "rule-id", "an extra desc");
        problem.setLevel(Linter.ERROR_LEVEL);
        assertEquals("rule-id", problem.getRuleId());
        assertEquals(1, problem.getLine());
        assertEquals(2, problem.getColumn());
        assertEquals(Linter.ERROR_LEVEL, problem.getLevel());
        assertEquals("desc", problem.getDesc());
        assertEquals("an extra desc", problem.getExtraDesc());
        assertEquals("desc (rule-id)", problem.getMessage());
        assertEquals("desc (rule-id)" + System.lineSeparator() + "an extra desc", problem.getLongMessage());
        assertEquals("1:2:desc (rule-id)", problem.toString());
        assertTrue(problem.equals(new LintProblem(1, 2, null, "rule-id")));
        assertEquals(-1290166725, problem.hashCode());

        problem = new LintProblem(1, 2, "desc");
        problem.setExtraDesc("an extra desc");
        assertEquals("desc" + System.lineSeparator() + "an extra desc", problem.getLongMessage());
    }

    public void testProblemNullDesc() {
        LintProblem problem = new LintProblem(1, 2, null);
        assertEquals("<no description>", problem.getDesc());
        assertEquals("<no description>", problem.getMessage());
        assertEquals("1:2:<no description>", problem.toString());
        assertTrue(problem.equals(new LintProblem(1, 2, null, null)));
        assertEquals(922381112, problem.hashCode());
    }

    public void testProblemNullDescNullRuleId() {
        LintProblem problem = new LintProblem(1, 2, null, null);
        assertEquals("<no description>", problem.getDesc());
        assertEquals("<no description>", problem.getMessage());
        assertEquals("1:2:<no description>", problem.toString());
        assertTrue(problem.equals(new LintProblem(1, 2, null, null)));
        assertEquals(922381112, problem.hashCode());
    }

    public void testProblemNullDescWithRuleId() {
        LintProblem problem = new LintProblem(1, 2, null, "rule-id");
        assertEquals("<no description>", problem.getDesc());
        assertEquals("<no description> (rule-id)", problem.getMessage());
        assertEquals("1:2:<no description> (rule-id)", problem.toString());
        assertTrue(problem.equals(new LintProblem(1, 2, null, "rule-id")));
        assertEquals(-1290166725, problem.hashCode());
    }

    public void testNotEquals() {
        LintProblem problem = new LintProblem(1, 2, "desc", "rule-id");
        assertFalse(problem.equals("some text"));
    }
}