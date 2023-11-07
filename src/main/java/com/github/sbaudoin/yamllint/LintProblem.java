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

import java.util.Objects;

/**
 * Represents a linting problem found by yamllint.
 */
public class LintProblem {
    private int line;
    private int column;
    private String desc = "<no description>";
    private String extraDesc;
    private String ruleId;
    private String level;


    /**
     * Constructor
     *
     * @param line line on which the problem was found (starting at 1)
     * @param column column on which the problem was found (starting at 1)
     * @param desc human-readable description of the problem (defaulted to {@code "<no description>"} if {@code null})
     */
    public LintProblem(int line, int column, String desc) {
        this(line, column, desc, null, null);
    }

    /**
     * Constructor
     *
     * @param line line on which the problem was found (starting at 1)
     * @param column column on which the problem was found (starting at 1)
     * @param desc human-readable description of the problem (defaulted to {@code "<no description>"} if {@code null})
     * @param ruleId identifier of the rule that detected the problem. May be {@code null}.
     */
    public LintProblem(int line, int column, String desc, String ruleId) {
        this(line, column, desc, ruleId, null);
    }

    /**
     * Constructor
     *
     * @param line line on which the problem was found (starting at 1)
     * @param column column on which the problem was found (starting at 1)
     * @param desc hman-readable description of the problem (defaulted to {@code "<no description>"} if {@code null})
     * @param ruleId identifier of the rule that detected the problem. May be {@code null}.
     * @param extraDesc extra, additional description (i.e. may be {@code null}, in which case it is ignored)
     */
    public LintProblem(int line, int column, String desc, String ruleId, String extraDesc) {
        this.line = line;
        this.column = column;
        if (desc != null) {
            this.desc = desc;
        }
        this.ruleId = ruleId;
        this.extraDesc = extraDesc;
    }


    /**
     * Returns the line number where the problem is
     *
     * @return a line number
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the column number where the problem is
     *
     * @return a column number
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the description of this problem
     *
     * @return the description of this problem
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Returns the extra description of this problem
     *
     * @return the extra description of this problem
     */
    public String getExtraDesc() {
        return extraDesc;
    }

    /**
     * Returns the Id of the rule that raised this problem
     *
     * @return a rule Id
     */
    public String getRuleId() {
        return ruleId;
    }

    /**
     * Returns a message representing this problem: the description followed by the rule Id
     * between parenthesis
     *
     * @return a message representing this problem
     */
    public String getMessage() {
        if (ruleId != null) {
            return String.format("%1$2s (%2$2s)", desc, ruleId);
        }
        return desc;
    }

    /**
     * Returns a long message representing this problem: the description followed by the rule Id
     * between parenthesis and the extra description on a new line
     *
     * @return a message representing this problem
     */
    public String getLongMessage() {
        if (ruleId != null) {
            return String.format("%1$2s (%2$2s)%3$s%4$s", desc, ruleId, (extraDesc == null)?"":System.lineSeparator(), (extraDesc == null)?"":extraDesc);
        }
        return desc + ((extraDesc == null)?"":(System.lineSeparator() + extraDesc));
    }

    /**
     * Returns the level of the problem, one of levels defined in {@link Linter} (or <code>null</code> by default)
     *
     * @return the level of the problem (<code>null</code> by default)
     */
    public String getLevel() {
        return level;
    }

    /**
     * Sets the error level of this problem
     *
     * @param level the error level
     * @see Linter
     */
    public void setLevel(String level) {
        // Normalize level, in case it was not set from the Linter statics
        Object intLevel = Linter.getProblemLevel(level);
        if (intLevel != null) {
            this.level = (String)Linter.getProblemLevel(intLevel);
        }
    }

    /**
     * Sets the Id of the rule that raised this problem
     *
     * @param ruleId a rule Id
     */
    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    /**
     * Sets an additional description to this problem
     *
     * @param extraDesc an extra description of the problem
     */
    public void setExtraDesc(String extraDesc) {
        this.extraDesc = extraDesc;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LintProblem) {
            return (((LintProblem) o).line == line &&
                    ((LintProblem) o).column == column &&
                    Objects.equals(ruleId, ((LintProblem) o).ruleId));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (line + ":" + column + ":" + ruleId).hashCode();
    }

    @Override
    public String toString() {
        return String.format("%1$d:%2$d:%3$2s", line, column, getMessage());
    }
}
