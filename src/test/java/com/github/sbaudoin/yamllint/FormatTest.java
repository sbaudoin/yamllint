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
package com.github.sbaudoin.yamllint;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

public class FormatTest extends TestCase {
    public void testFormat() {
        List<LintProblem> problems = Arrays.asList(new LintProblem(1, 2, null));
        String file = "/my/filename.yaml";

        assertEquals("/my/filename.yaml:1:2:::<no description>",
                Format.format(file, problems, Format.OutputFormat.PARSABLE));

        assertEquals(":: file=/my/filename.yaml,line=1,col=2::<no description>",
                Format.format(file, problems, Format.OutputFormat.GITHUB));

        assertEquals(file + System.lineSeparator() + "  1:2                <no description>" + System.lineSeparator(),
                Format.format(file, problems, Format.OutputFormat.STANDARD));

        assertEquals("\u001B[4m" + file + "\u001B[0m" + System.lineSeparator() + "  \u001B[2m1:2\u001B[0m                         <no description>" + System.lineSeparator(),
                Format.format(file, problems, Format.OutputFormat.COLORED));

        assertEquals(file + System.lineSeparator() + "  1:2                <no description>" + System.lineSeparator(),
                Format.format(file, problems, Format.OutputFormat.AUTO));
    }

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

        problem = new LintProblem(1, 2, null, "rule-id", "extra desc");
        assertEquals("/my/filename.yaml:1:2:rule-id::<no description>",
                Format.parsable(problem, "/my/filename.yaml"));
    }

    public void testGithub() {
        LintProblem problem = new LintProblem(1, 2, null);
        assertEquals(":: file=/my/filename.yaml,line=1,col=2::<no description>",
                Format.github(problem, "/my/filename.yaml"));

        problem = new LintProblem(1, 2, "desc");
        problem.setLevel(Linter.INFO_LEVEL);
        assertEquals("::info file=/my/filename.yaml,line=1,col=2::desc",
                Format.github(problem, "/my/filename.yaml"));

        problem = new LintProblem(1, 2, null, "rule-id");
        assertEquals(":: file=/my/filename.yaml,line=1,col=2::[rule-id] <no description>",
                Format.github(problem, "/my/filename.yaml"));

        problem = new LintProblem(1, 2, null, "rule-id", "extra desc");
        assertEquals(":: file=/my/filename.yaml,line=1,col=2::[rule-id] <no description>",
                Format.github(problem, "/my/filename.yaml"));
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

        problem = new LintProblem(1, 2, null, "rule-id", "extra desc\nwith lines");
        assertEquals("  1:2                <no description>  (rule-id)" + System.lineSeparator() +
                        "                     extra desc" + System.lineSeparator() + "                     with lines",
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

        problem = new LintProblem(1, 2, null, "rule-id", "extra desc\nwith lines");
        assertEquals("  \u001B[2m1:2\u001B[0m                         <no description>  \u001B[2m(rule-id)\u001B[0m" +
                        System.lineSeparator() + "                     extra desc" + System.lineSeparator() + "                     with lines",
                Format.standardColor(problem));
    }

    // Hard to test in non-interactive, multi-platform build...
    public void testSupportsColor() {
        // Save platform name for future restoration
        String pf = System.getProperty("os.name");

        // Colors not supported on Windows platform
        System.setProperty("os.name", "Windows");
        assertFalse(Format.supportsColor());

        // On other platform, it depends
        System.setProperty("os.name", "foo");
        if (System.console() == null) {
            assertFalse(Format.supportsColor());
        } else if (System.getenv("ANSICON") != null || (System.getenv("TERM") != null && "ANSI".equals(System.getenv("TERM")))) {
                assertTrue(Format.supportsColor());
        } else {
            assertFalse(Format.supportsColor());
        }

        // Restore platform
        System.setProperty("os.name", pf);
    }

    public void testGetFiller() {
        assertEquals("    ", Format.getFiller(4));
    }

    public void testRepeat() {
        assertEquals("abababababababababab", Format.repeat(10, "ab"));
    }
}