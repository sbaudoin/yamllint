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

public class FormatTest extends TestCase {
    public void testParsable() {
        LintProblem problem = new LintProblem(1, 2, null);
        assertEquals("/my/filename.yaml:1:2:::<no description>",
                Format.parsable(problem, "/my/filename.yaml"));

        problem = new LintProblem(1, 2, "desc");
        problem.setLevel(Linter.INFO_LEVEL);
        assertEquals("/my/filename.yaml:1:2::info:desc",
                Format.parsable(problem, "/my/filename.yaml"));

        problem = new LintProblem(1, 2, null, "rule-id");
        assertEquals("/my/filename.yaml:1:2:rule-id::<no description>",
                Format.parsable(problem, "/my/filename.yaml"));
    }

    public void testStandard() {
        LintProblem problem = new LintProblem(1, 2, null);
        assertEquals("  1:2                <no description>",
                Format.standard(problem));

        problem = new LintProblem(1, 2, "desc");
        problem.setLevel(Linter.INFO_LEVEL);
        assertEquals("  1:2       info     desc",
                Format.standard(problem));

        problem = new LintProblem(1, 2, null, "rule-id");
        assertEquals("  1:2                <no description>  (rule-id)",
                Format.standard(problem));
    }

    public void testStandardColor() {
        LintProblem problem = new LintProblem(1, 2, null);
        assertEquals("  \u001B[2m1:2\u001B[0m                         <no description>",
                Format.standardColor(problem));

        problem = new LintProblem(1, 2, "desc");
        problem.setLevel(Linter.INFO_LEVEL);
        assertEquals("  \u001B[2m1:2\u001B[0m       info              desc",
                Format.standardColor(problem));
        problem = new LintProblem(1, 2, "desc");
        problem.setLevel(Linter.WARNING_LEVEL);
        assertEquals("  \u001B[2m1:2\u001B[0m       \u001B[33mwarning\u001B[0m  desc",
                Format.standardColor(problem));

        problem = new LintProblem(1, 2, "desc");
        problem.setLevel(Linter.ERROR_LEVEL);
        assertEquals("  \u001B[2m1:2\u001B[0m       \u001B[31merror\u001B[0m    desc",
                Format.standardColor(problem));

        problem = new LintProblem(1, 2, null, "rule-id");
        assertEquals("  \u001B[2m1:2\u001B[0m                         <no description>  \u001B[2m(rule-id)\u001B[0m",
                Format.standardColor(problem));
    }

    // May not work in some conditions (e.g. compilation done on the command line in interactive mode on Unix)
    public void testSupportsColor() {
        assertFalse(Format.supportsColor());
    }

    public void testGetFiller() {
        assertEquals("    ", Format.getFiller(4));
    }

    public void testRepeat() {
        assertEquals("abababababababababab", Format.repeat(10, "ab"));
    }
}