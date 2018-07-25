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
import com.github.sbaudoin.yamllint.LintProblem;
import com.github.sbaudoin.yamllint.Linter;
import com.github.sbaudoin.yamllint.YamlLintConfig;
import com.github.sbaudoin.yamllint.YamlLintConfigException;

import java.io.IOException;
import java.util.List;

public abstract class RuleTester extends TestCase {
    protected YamlLintConfig getConfig(String... rules) throws IOException, YamlLintConfigException {
        StringBuilder sb = new StringBuilder("---\n").append("rules:\n");
        for (String rule: rules) {
            sb.append("  ").append(rule).append("\n");
        }
        return new YamlLintConfig(sb.toString());
    }

    /**
     * Checks the passed YAML string again the given configuration. It is expected that the YAML contains as many errors
     * as the number of rows of <var>linesAndColumns</var> and ony for the rule identified by <code>getRuleId()</code>.
     *
     * @param source a YAML string
     * @param conf a yamllint configuration. If <code>null</code>, the default configuration is used.
     * @param expectedProblems list of pairs "lines" and "columns" of expected errors. For example if the passed source
     *                         is supposed to contain 2 (and only 2) errors fpr the rule <code>getRuleId()</code>, one at
     *                         line 2 and column 7 and the other one at line 10 and column 2, the parameter must be
     *                         <code>new int[][] { { 2, 7 }, { 10, 2} }</code>
     * @throws IOException should never happen. This exception would come from the initialisation of the default <code>YamlLintconfig</code> instance
     *                     created when <var>conf</var> is <code>null</code>.
     * @throws YamlLintConfigException should never happen. This exception would come from the initialisation of the default <code>YamlLintconfig</code> instance
     *                     created when <var>conf</var> is <code>null</code>.
     */
    protected void check(String source, YamlLintConfig conf, LintProblem... expectedProblems) throws IOException, YamlLintConfigException {
        List<LintProblem> problems = Linter.run(source, (conf == null)?getFakeConfig():conf);
        assertTrue("Expected " + expectedProblems.length + " error(s), got " + problems.size(), problems.size() == expectedProblems.length);
        for (int i = 0; i < expectedProblems.length; i++) {
            assertTrue("Source '" + source + "' expected to contain a problem for '" +
                            ((expectedProblems[i].getRuleId() == null)?"syntax error":expectedProblems[i].getRuleId()) +
                            "' at line " + expectedProblems[i].getLine() + " and column " + expectedProblems[i].getColumn() + ", found '" + problems.get(i) + "'",
                    problems.get(i).equals(expectedProblems[i]));
        }
    }

    protected LintProblem getLintProblem(int line, int column) {
        return new LintProblem(line, column, null, getRuleId());
    }

    protected LintProblem getLintProblem(int line, int column, String ruleId) {
        return new LintProblem(line, column, null, ruleId);
    }

    protected LintProblem getSyntaxError(int line, int column) {
        return new LintProblem(line, column, null, null);
    }

    /**
     * Returns the ID of the rule to be checked by the tester. The default is calculated from the class name: "Test" is removed
     * from the class name and the capital letters are replaced by "-" followed by the lower case letter. E.g. for the
     * class "FooBarTest" this method will return "foo-bar".
     *
     * @return the ID of the rule checked by this test class
     */
    public String getRuleId() {
        return this.getClass().getSimpleName().replaceAll("Test", "").replaceAll("([A-Z])", "-$1").substring(1).toLowerCase();
    }


    public static YamlLintConfig getFakeConfig() throws IOException, YamlLintConfigException {
        return new YamlLintConfig("extends: default");
    }
}
