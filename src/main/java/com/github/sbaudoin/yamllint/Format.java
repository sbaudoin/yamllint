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

import java.util.Arrays;
import java.util.Collections;

/**
 * Helper class to format the output of this linter
 */
public class Format {
    /**
     * ANSI code used to reset text decoration. Any other {@code ANSI_} code must be followed by this code once the text
     * has been decorated in order to come back to the standard text output format.
     */
    public static final String ANSI_RESET      = "\u001B[0m";

    /**
     * ANSI for faint decoration
     */
    public static final String ANSI_FAINT      = "\u001B[2m";

    /**
     * ANSI code for underlined text
     */
    public static final String ANSI_UNDERLINED = "\u001B[4m";

    /**
     * ANSI code for black text color
     */
    public static final String ANSI_BLACK      = "\u001B[30m";

    /**
     * ANSI code for red text color
     */
    public static final String ANSI_RED        = "\u001B[31m";

    /**
     * ANSI code for green text color
     */
    public static final String ANSI_GREEN      = "\u001B[32m";

    /**
     * ANSI code for yellow text color
     */
    public static final String ANSI_YELLOW     = "\u001B[33m";

    /**
     * ANSI code for blue text color
     */
    public static final String ANSI_BLUE       = "\u001B[34m";

    /**
     * ANSI code for purple text color
     */
    public static final String ANSI_PURPLE     = "\u001B[35m";

    /**
     * ANSI code for cyan text color
     */
    public static final String ANSI_CYAN       = "\u001B[36m";

    /**
     * ANSI code for white text color
     */
    public static final String ANSI_WHITE      = "\u001B[37m";


    /**
     * Hide default constructor
     */
    private Format() {
    }


    /**
     * Returns the parsable output of a problem
     *
     * @param problem the problem
     * @param filename the name of the file where the problem was found
     * @return the parsable representation of the problem
     */
    public static String parsable(LintProblem problem, String filename) {
        return String.format("%1$s:%2$d:%3$d:%4$s:%5$s:%6$s",
                filename,
                problem.getLine(),
                problem.getColumn(),
                (problem.getRuleId() == null)?"":problem.getRuleId(),
                (problem.getLevel() == null)?"":problem.getLevel(),
                problem.getDesc());
    }

    /**
     * Returns the standard output of a problem
     *
     * @param problem the problem
     * @return the standard representation of the problem
     */
    public static String standard(LintProblem problem) {
        StringBuilder line = new StringBuilder();

        line.append("  ").append(problem.getLine()).append(":").append(problem.getColumn());
        line.append(getFiller(Math.max(12 - line.length(), 0)));
        if (problem.getLevel() != null) {
            line.append(problem.getLevel());
        }
        line.append(getFiller(Math.max(21 - line.length(), 0)));
        line.append(problem.getDesc());
        if (problem.getRuleId() != null) {
            line.append("  (").append(problem.getRuleId()).append(")");
        }
        if (problem.getExtraDesc() != null) {
            Arrays.stream(problem.getExtraDesc().split("\n")).forEach(l -> line.append(System.lineSeparator()).append(getFiller(21)).append(l));
        }

        return line.toString();
    }

    /**
     * Returns the colorized standard output of a problem
     *
     * @param problem the problem
     * @return the colorized standard representation of the problem
     */
    public static String standardColor(LintProblem problem) {
        StringBuilder line = new StringBuilder();

        line.append("  ").append(ANSI_FAINT).append(problem.getLine()).append(":").append(problem.getColumn()).append(ANSI_RESET);
        line.append(getFiller(Math.max(20 - line.length(), 0)));
        if (problem.getLevel() != null) {
            if (Linter.WARNING_LEVEL.equals(problem.getLevel())) {
                line.append(ANSI_YELLOW).append(problem.getLevel()).append(ANSI_RESET);
            } else if (Linter.ERROR_LEVEL.equals(problem.getLevel())) {
                line.append(ANSI_RED).append(problem.getLevel()).append(ANSI_RESET);
            } else {
                line.append(problem.getLevel());
            }
        }
        line.append(getFiller(Math.max(38 - line.length(), 0)));
        line.append(problem.getDesc());
        if (problem.getRuleId() != null) {
            line.append("  ").append(ANSI_FAINT).append("(").append(problem.getRuleId()).append(")").append(ANSI_RESET);
        }
        if (problem.getExtraDesc() != null) {
            Arrays.stream(problem.getExtraDesc().split("\n")).forEach(l -> line.append(System.lineSeparator()).append(getFiller(21)).append(l));
        }

        return line.toString();
    }

    /**
     * Tells if the current system and execution channel can support output colorization
     *
     * @return <code>true</code> if it is possible to colorized the output, <code>false</code> if not
     */
    public static boolean supportsColor() {
        boolean supportedPlatform = !(System.getProperty("os.name").toLowerCase().contains("windows") &&
                !(System.getenv("ANSICON") != null ||
                        (System.getenv("TERM") != null &&
                                "ANSI".equals(System.getenv("TERM")))));
        return (supportedPlatform && System.console() != null);
    }

    /**
     * Returns a string containing the indicated number of spaces
     *
     * @param length the number of spaces to be returned
     * @return a string containing the indicated number of spaces
     */
    public static String getFiller(int length) {
        return repeat(length, " ");
    }

    /**
     * Returns a string repeated the indicated number of times
     *
     * @param n the number of time to repeat <var>s</var>
     * @param s the string to repeat
     * @return <var>s</var> repeated <var>n</var> times
     */
    public static String repeat(int n, String s) {
        return String.join("", Collections.nCopies(n, s));
    }
}
