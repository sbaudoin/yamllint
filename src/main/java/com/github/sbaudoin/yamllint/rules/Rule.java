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
package com.github.sbaudoin.yamllint.rules;

import org.yaml.snakeyaml.tokens.ScalarToken;
import org.yaml.snakeyaml.tokens.Token;
import com.github.sbaudoin.yamllint.LintProblem;
import com.github.sbaudoin.yamllint.Linter;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

/**
 * The class represents a coding rule. It is not intended to be extended directly; instead, you are encouraged to extend
 * the derivated classes {@link CommentRule}, {@link LineRule} and {@link TokenRule}.
 *
 * @see CommentRule
 * @see LineRule
 * @see TokenRule
 */
public abstract class Rule {
    private List<String> ignore = new ArrayList<>();
    private Map<String, Object> parameters = new HashMap<>();
    private String level = Linter.ERROR_LEVEL;
    private Map<String, Object> options = new HashMap<>();
    private Map<String, Object> defaults = new HashMap<>();


    /**
     * The different supported rule types
     */
    public enum TYPE {
        TOKEN,
        COMMENT,
        LINE
    }

    /**
     * Returns the ID of this rule.
     * The default implementation returns the simple class' name (name without package) in lower case and the "words"
     * (identified by the capital letters) separated with an hyphen.
     *
     * @return the rule ID
     */
    public String getId() {
        return this.getClass().getName().replaceAll(".*\\.", "").replaceAll("([A-Z])", "-$1").substring(1).toLowerCase();
    }

    /**
     * Sets the regex patterns to tell is files are ignored
     *
     * @param ignore list of regex patterns representing files to be ignored by this rule
     */
    public void setIgnore(List<String> ignore) {
        this.ignore = ignore;
    }

    /**
     * Tells if the passed file is to be ignored by this rule
     *
     * @param file a file
     * @return <code>true</code> if the passed file is to be ignored, <code>false</code> if not
     */
    public boolean ignores(File file) {
        if (file == null) {
            return false;
        }
        for (String pattern : ignore) {
            if (file.getPath().matches(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the level or the problems returned by this rule
     *
     * @return an error level
     * @see Linter
     */
    public String getLevel() {
        return level;
    }

    /**
     * Sets the error level of the problems returned by this rule
     *
     * @param level the error level for this rule
     * @see Linter
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * Adds a parameter to this rule
     *
     * @param name the parameter's name
     * @param value the parameter's value
     */
    public void addParameter(String name, Object value) {
        parameters.put(name, value);
    }

    /**
     * Returns the value of the passed parameter
     *
     * @param name a parameter name
     * @return the value of the passed parameter or <code>null</code> if not found
     */
    public Object getParameter(String name) {
        return parameters.get(name);
    }

    /**
     * Returns the rule's options. This is a very generic container: see the rule details for the exact list
     * and possible values for the options.
     *
     * @return the rules options
     */
    public Map<String, Object> getOptions() {
        return options;
    }

    /**
     * Returns the default value for the passed option
     *
     * @param option an option name
     * @return
     */
    public Object getDefaultOptionValue(String option) {
        if (defaults.containsKey(option)) {
            return defaults.get(option);
        }

        throw new IllegalArgumentException("Unknown option: " + option);
    }

    /**
     * Returns the type of this rule
     *
     * @return the rule type
     * @see TYPE
     */
    public abstract TYPE getType();


    /**
     * Determines the number of spaces between the passed token and the next one and compares this number
     * with the min and max accepted values. In the event the determined number of spaces is less than or greater
     * than the min and max value, a problem is returned with the corresponding error message.
     *
     * @param token the token to analyse
     * @param next the next token with which the spaces are counted
     * @param min the minimum number or expected spaces. May be <code>null</code>, in which case -1 is taken (no minimum required).
     * @param max the maximum number or expected spaces. May be <code>null</code>, in which case -1 is taken (no maximum required).
     * @param minDesc the problem description in case the number of spaces is less than <var>min</var>
     * @param maxDesc the problem description in case the number of spaces is greater than <var>max</var>
     * @return a problem or <code>null</code> if no problem found
     */
    protected LintProblem spacesAfter(Token token, Token next, @Nullable Integer min, @Nullable Integer max,
                                      String minDesc, String maxDesc) {
        int myMin =  (min == null)?-1:min;
        int myMax =  (max == null)?-1:max;

        if (next != null && token.getEndMark().getLine() == next.getStartMark().getLine()) {
            int spaces = next.getStartMark().getIndex() - token.getEndMark().getIndex();
            if (myMax != -1 && spaces > myMax) {
                return new LintProblem(token.getStartMark().getLine() + 1,
                        next.getStartMark().getColumn(), maxDesc);
            } else if (myMin != -1 && spaces < myMin) {
                return new LintProblem(token.getStartMark().getLine() + 1,
                        next.getStartMark().getColumn() + 1, minDesc);
            }
        }
        return null;
    }

    /**
     * Determines the number of spaces between the passed token and the previous one and compares this number
     * with the min and max accepted values. In the event the determined number of spaces is less than or greater
     * than the min and max value, a problem is returned with the corresponding error message.
     *
     * @param token the token to analyse
     * @param prev the previous token with which the spaces are counted
     * @param min the minimum number or expected spaces. May be <code>null</code>, in which case -1 is taken (no minimum required).
     * @param max the maximum number or expected spaces. May be <code>null</code>, in which case -1 is taken (no maximum required).
     * @param minDesc the problem description in case the number of spaces is less than <var>min</var>
     * @param maxDesc the problem description in case the number of spaces is greater than <var>max</var>
     * @return a problem or <code>null</code> if no problem found
     */
    protected LintProblem spacesBefore(Token token, Token prev, @Nullable Integer min, @Nullable Integer max,
                                           String minDesc, String maxDesc) {
        int myMin = (min == null)?-1:min;
        int myMax = (max == null)?-1:max;

        if (prev != null && prev.getEndMark().getLine() == token.getStartMark().getLine() &&
                // Discard tokens (only scalars ?) that end at the start of next line
                (prev.getEndMark().getPointer() == 0 ||
                        prev.getEndMark().getBuffer()[prev.getEndMark().getPointer() - 1] != '\n')) {
            int spaces = token.getStartMark().getPointer() - prev.getEndMark().getPointer();
            if (myMax != -1 && spaces > myMax) {
                return new LintProblem(token.getStartMark().getLine() + 1, token.getStartMark().getColumn(), maxDesc);
            } else if (myMin !=-1 && spaces < myMin) {
                return new LintProblem(token.getStartMark().getLine() + 1, token.getStartMark().getColumn() + 1, minDesc);
            }
        }
        return null;
    }

    /**
     * Tells if the passed token is an explicit key or not. Explicit key:
     * <pre>
     *   ? key
     *   : v
     * </pre>
     * or
     * <pre>
     * ?
     *     key
     *   : v
     * </pre>
     * A key is guessed to be explicit if it starts with a question mark. It is the responsibility of the caller to
     * check that the token corresponds to a key (the instance is a {@link org.yaml.snakeyaml.tokens.KeyToken}).
     *
     * @param token a token
     * @return {@code true} if the token represents an explicit key, {@code false} otherwise
     */
    protected boolean isExplicitKey(Token token) {
        return (token.getStartMark().getPointer() < token.getEndMark().getPointer() && token.getStartMark().getBuffer()[token.getStartMark().getPointer()] == '?');
    }

    /**
     * Finds the indent of the line the token starts in
     *
     * @param token a token
     * @return the indent of the line the token starts in
     */
    protected int getLineIndent(Token token) {
        int start = rfind(token.getStartMark().getBuffer(), '\n', 0, token.getStartMark().getPointer()) + 1;
        int content = start;
        while (token.getStartMark().getBuffer()[content] == ' ') {
            content += 1;
        }
        return content - start;
    }

    /**
     * The whitespace characters are: tab (ASCII 9), line feed (ASCII 10), vertical tab (ASCII 11), form feed (ASCII 12),
     * carriage return (ASCII 13), space (ASCII 32)
     *
     * @param i an integer representing a character
     * @return <code>true</code> if <var>i</var> is one of the characters listed above, <code>false</code> otherwise
     */
    protected boolean isWhitespace(int i) {
        return Arrays.binarySearch(new int[] { 9, 10, 11, 12, 13, 32 }, i) >= 0;
    }

    /**
     * Returns the first index of an int in an array of int's. The int is searched for between the start and end-1
     * indexes. If the int is not found, the method returns -1.
     *
     * @param haystack the array of int's in which to search
     * @param needle the int to look for
     * @param start the index from which to search
     * @param end the search is done until the index <var>end</var>-1
     * @return the index (between start and end) if the int is found, -1 otherwise
     * @throws ArrayIndexOutOfBoundsException if <var>start</var> or <var>end</var> are beyond the boundaries of <var>haystack</var>
     */
    protected int find(int[] haystack, int needle, int start, int end) {
        if (start > haystack.length - 1  || end > haystack.length || start < 0 || end < 0) {
            throw new ArrayIndexOutOfBoundsException("start or end index beyond the array boundaries");
        }
        for (int i = start; i < end; i++) {
            if (haystack[i] == needle) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the right-most index of an int in an array of int's. The int is searched for between the start and end-1
     * indexes. If the int is not found, the method returns -1.
     *
     * @param haystack the array of int's in which to search
     * @param needle the int to look for
     * @param start the index from which to search
     * @param end the search is done until the index <var>end</var>-1
     * @return the index (between start and end) if the int is found, -1 otherwise
     * @throws ArrayIndexOutOfBoundsException if <var>start</var> or <var>end</var> are beyond the boundaries of <var>haystack</var>
     */
    protected int rfind(int[] haystack, int needle, int start, int end) {
        if (start > haystack.length - 1  || end > haystack.length || start < 0 || end < 0) {
            throw new ArrayIndexOutOfBoundsException("start or end index beyond the array boundaries");
        }
        for (int i = end - 1; i >= start; i--) {
            if (haystack[i] == needle) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the line on which the token really ends.
     * With SnakeYAML, scalar tokens often end on a next line.
     *
     * @param token a token
     * @return the line on which the token really ends
     */
    protected int getRealEndLine(Token token) {
        int endLine = token.getEndMark().getLine() + 1;

        if (!(token instanceof ScalarToken)) {
            return endLine;
        }

        int pos = token.getEndMark().getPointer() - 1;
        while (pos >= token.getStartMark().getPointer() - 1 && isWhitespace(token.getEndMark().getBuffer()[pos])) {
            if (token.getEndMark().getBuffer()[pos] == '\n') {
                endLine -= 1;
            }
            pos -= 1;
        }
        return endLine;
    }

    /**
     * Tells if the passed string represents an intege
     *
     * @param str a string
     * @return {@code true} if the string is an integer, {@code false} if not
     */
    protected boolean isDigit(String str) {
        if (str == null || "".equals(str)) {
            return false;
        }
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * Declares an option with the given name and default value. The type of the (default) value will be used to
     * check the rule configuration. If you pass a list, the first item will be used as the default value. If this
     * does not suits your need, use {@link #registerOption(String, Object, Object)} instead.
     *
     * @param name the option name
     * @param value the default value
     * @see #registerOption(String, Object, Object)
     */
    protected void registerOption(String name, Object value) {
        if (value instanceof List) {
            if (((List<?>) value).size() > 0) {
                registerOption(name, value, ((List<?>) value).get(0));
            } else {
                throw new IllegalArgumentException("Empty list passed, you must explicitly specify a default value");
            }
        } else {
            options.put(name, value);
            defaults.put(name, value);
        }
    }

    /**
     * Declares an option with the given name, value and default value. This method is mainly used when the other method
     * {@link #registerOption(String, Object)} is not sufficient, i.e. when the option type cannot be the same as the
     * default value (e.g. the values of the option are taken from a list).
     *
     * @param name the option name
     * @param type the option's type
     * @param defaultValue the default value of the option
     * @see #registerOption(String, Object)
     */
    protected void registerOption(String name, Object type, Object defaultValue) {
        options.put(name, type);
        defaults.put(name, defaultValue);
    }
}
