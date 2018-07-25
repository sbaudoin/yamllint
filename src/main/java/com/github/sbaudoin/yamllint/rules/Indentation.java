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

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.tokens.*;
import com.github.sbaudoin.yamllint.LintProblem;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Use this rule to control the indentation.
 * <p>Options:</p>
 * <ul>
 *     <li>{@code spaces} defines the indentation width, in spaces. Set either to an integer
 *     (e.g. {@code 2} or {@code 4}, representing the number of spaces in an indentation
 *     level) or to {@code consistent} to allow any number, as long as it remains the
 *     same within the file.</li>
 *     <li>{@code indent-sequences} defines whether block sequences should be indented or
 *     not (when in a mapping, this indentation is not mandatory -- some people
 *     perceive the {@code -} as part of the indentation). Possible values: {@code true},
 *     {@code false}, {@code whatever} and {@code consistent}. {@code consistent}
 *     requires either all block sequences to be indented, or none to be. {@code whatever} means
 *     either indenting or not indenting individual block sequences is OK.</li>
 *     <li>{@code check-multi-line-strings} defines whether to lint indentation in
 *     multi-line strings. Set to {@code true} to enable, {@code false} to disable.</li>
 * </ul>
 *
 * <p>Examples:</p>
 * <p>With <code>indentation: {spaces: 1}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     history:
 *      - name: Unix
 *        date: 1969
 *      - name: Linux
 *        date: 1991
 *     nest:
 *      recurse:
 *       - haystack:
 *          needle
 * </pre>
 *
 * <p>With <code>indentation: {spaces: 4}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     history:
 *         - name: Unix
 *           date: 1969
 *         - name: Linux
 *           date: 1991
 *     nest:
 *         recurse:
 *             - haystack:
 *                   needle
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     history:
 *       - name: Unix
 *         date: 1969
 *       - name: Linux
 *         date: 1991
 *     nest:
 *       recurse:
 *         - haystack:
 *             needle
 * </pre>
 *
 * <p>With <code>indentation: {spaces: consistent}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     history:
 *        - name: Unix
 *          date: 1969
 *        - name: Linux
 *          date: 1991
 *     nest:
 *        recurse:
 *           - haystack:
 *                needle
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     some:
 *       Russian:
 *           dolls
 * </pre>
 *
 * <p>With <code>indentation: {spaces: 2, indent-sequences: false}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     list:
 *     - flying
 *     - spaghetti
 *     - monster
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     list:
 *       - flying
 *       - spaghetti
 *       - monster
 * </pre>
 *
 * <p>With <code>indentation: {spaces: 2, indent-sequences: whatever}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     list:
 *     - flying:
 *       - spaghetti
 *       - monster
 *     - not flying:
 *         - spaghetti
 *         - sauce
 * </pre>
 *
 * <p>With <code>indentation: {spaces: 2, indent-sequences: consistent}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     - flying:
 *         - spaghetti
 *         - monster
 *     - not flying:
 *         - spaghetti
 *         - sauce
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     - flying:
 *         - spaghetti
 *         - monster
 *     - not flying:
 *       - spaghetti
 *       - sauce
 * </pre>
 *
 * <p>With <code>indentation: {spaces: 4, check-multi-line-strings: true}</code>
 * the following code snippet would **PASS**:
 * <pre>
 *     Blaise Pascal:
 *         Je vous écris une longue lettre parce que
 *         je n'ai pas le temps d'en écrire une courte.
 * </pre>
 * the following code snippet would **PASS**:
 * <pre>
 *     Blaise Pascal: Je vous écris une longue lettre parce que
 *                    je n'ai pas le temps d'en écrire une courte.
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     Blaise Pascal: Je vous écris une longue lettre parce que
 *       je n'ai pas le temps d'en écrire une courte.
 * </pre>
 * the following code snippet would **FAIL**:
 * <pre>
 *     C code:
 *         void main() {
 *             printf("foo");
 *         }
 * </pre>
 * the following code snippet would **PASS**:
 * <pre>
 *     C code:
 *         void main() {
 *         printf("bar");
 *         }
 * </pre>
 */
public class Indentation extends TokenRule {
    public enum LABEL { ROOT, B_MAP, F_MAP, B_SEQ, F_SEQ, B_ENT, KEY, VAL }

    public static final String STACK_KEY = "stack";
    private static final String CURRENT_LINE_KEY = "cur_line";
    private static final String CURRENT_LINE_INDENT_KEY = "cur_line_indent";

    public static final String OPTION_SPACES = "spaces";
    public static final String OPTION_CONSISTENT = "consistent";
    public static final String OPTION_INDENT_SEQUENCES = "indent-sequences";
    public static final String OPTION_CHECK_MULTI_LINE_STRINGS = "check-multi-line-strings";


    /**
     * Constructor
     */
    public Indentation() {
        options.put(OPTION_SPACES, Arrays.asList(Integer.class, OPTION_CONSISTENT));
        options.put(OPTION_INDENT_SEQUENCES, Arrays.asList(Boolean.class, "whatever", OPTION_CONSISTENT));
        options.put(OPTION_CHECK_MULTI_LINE_STRINGS, Boolean.class);
    }

    @Override
    public List<LintProblem> check(Map conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        try {
            problems.addAll(checkToken(conf, token, prev, next, nextnext, context));
        } catch (AssertionError e) {
            problems.add(new LintProblem(token.getStartMark().getLine() + 1,
                    token.getStartMark().getColumn() + 1,
                    "cannot infer indentation: unexpected token"));
        }

        return problems;
    }


    private int detectIndent(int baseIndent, int foundIndent, Map context) {
        if (!(context.get(OPTION_SPACES) instanceof Integer)) {
            context.put(OPTION_SPACES, foundIndent - baseIndent);
        }
        return baseIndent + (int)context.get(OPTION_SPACES);
    }

    private int computeExpectedIndent(int foundIndent, Token token, Map context) {
        Boolean plain = (Boolean)invokeSimpleMethod(token, "getPlain");
        DumperOptions.ScalarStyle style = (DumperOptions.ScalarStyle)invokeSimpleMethod(token, "getStyle");

        if (plain != null && plain) {
            return token.getStartMark().getColumn();
        } else if (style != null && (style.getChar() == '"' || style.getChar() == '\'')) {
            return token.getStartMark().getColumn() + 1;
        } else if (style != null && (style.getChar() == '>' || style.getChar() == '|')) {
            List<Parent> stack = (List<Parent>)context.get(STACK_KEY);
            if (stack.get(stack.size() - 1).type == LABEL.B_ENT) {
                // - >
                // multi
                // line
                return detectIndent(token.getStartMark().getColumn(), foundIndent, context);
            } else if (stack.get(stack.size() - 1).type == LABEL.KEY) {
                assert stack.get(stack.size() - 1).explicitKey;
                // - ? >
                //       multi - line
                //       key
                //   : >
                //       multi - line
                //       value
                return detectIndent(token.getStartMark().getColumn(), foundIndent, context);
            } else if (stack.get(stack.size() - 1).type == LABEL.VAL) {
                if (token.getStartMark().getLine() + 1 > (int)context.get(CURRENT_LINE_KEY)) {
                    // - key:
                    //     >
                    //       multi
                    //       line
                    return detectIndent(stack.get(stack.size() - 1).indent, foundIndent, context);
                } else if(stack.get(stack.size() - 2).explicitKey) {
                    // - ? key
                    //   : >
                    //       multi - line
                    //       value
                    return detectIndent(token.getStartMark().getColumn(), foundIndent, context);
                } else {
                    // - key: >
                    //     multi
                    //     line
                    return detectIndent(stack.get(stack.size() - 2).indent, foundIndent, context);
                }
            } else {
                return detectIndent(stack.get(stack.size() - 1).indent, foundIndent, context);
            }
        }

        return 0;
    }

    private Object invokeSimpleMethod(Object o, String methodName) {
        try {
            return o.getClass().getMethod(methodName, null).invoke(o);
        } catch (NoSuchMethodException|IllegalAccessException|InvocationTargetException e) {
            // Error: we assume this may be expected and we return null
            return null;
        }
    }

    private List<LintProblem> checkScalarIndentation(Token token, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        if (token.getStartMark().getLine() == token.getEndMark().getLine()) {
            return problems;
        }

        int expectedIndent = -1;

        int lineNo = token.getStartMark().getLine() + 1;

        int lineStart = token.getStartMark().getPointer();
        while (true) {
            lineStart = find(token.getStartMark().getBuffer(), '\n', lineStart, token.getEndMark().getPointer() - 1) + 1;
            if (lineStart == 0) {
                break;
            }
            lineNo += 1;

            int indent = 0;
            while (token.getStartMark().getBuffer()[lineStart + indent] == ' ') {
                indent += 1;
            }
            if (token.getStartMark().getBuffer()[lineStart + indent] == '\n') {
                continue;
            }

            if (expectedIndent == -1) {
                expectedIndent = computeExpectedIndent(indent, token, context);
            }

            if (indent != expectedIndent) {
                problems.add(new LintProblem(lineNo, indent + 1,
                        "wrong indentation: expected " + expectedIndent + " but found " + indent));
            }
        }

        return problems;
    }

    private List<LintProblem> checkToken(Map conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
        List<LintProblem> problems = new ArrayList<>();

        if (!context.containsKey(STACK_KEY)) {
            context.put(STACK_KEY, new ArrayList<>(Arrays.asList(new Parent(LABEL.ROOT, 0))));
            context.put(CURRENT_LINE_KEY, -1);
            context.put(OPTION_SPACES, conf.get(OPTION_SPACES));
            context.put(OPTION_INDENT_SEQUENCES, conf.get(OPTION_INDENT_SEQUENCES));
        }

        List<Parent> stack = (List<Parent>)context.get(STACK_KEY);

        // Step 1: Lint

        boolean isVisible = (!(token instanceof StreamStartToken || token instanceof StreamEndToken)) &&
                !(token instanceof BlockEndToken) &&
                !(token instanceof ScalarToken &&
                "".equals(invokeSimpleMethod(token, "getValue")));
        boolean firstInLine = (isVisible &&
                token.getStartMark().getLine() + 1 > (int)context.get(CURRENT_LINE_KEY));

        Integer foundIndentation = null;
        if (firstInLine) {
            foundIndentation = token.getStartMark().getColumn();
            int expected = stack.get(stack.size() - 1).indent;

            if (token instanceof FlowMappingEndToken || token instanceof FlowSequenceEndToken) {
                expected = stack.get(stack.size() - 1).lineIndent;
            } else if (stack.get(stack.size() - 1).type == LABEL.KEY &&
                    stack.get(stack.size() - 1).explicitKey &&
                    !(token instanceof org.yaml.snakeyaml.tokens.ValueToken)) {
                expected = detectIndent(expected, token, context);
            }

            if (foundIndentation != expected) {
                problems.add(new LintProblem(token.getStartMark().getLine() + 1, foundIndentation + 1,
                        "wrong indentation: expected " + expected + " but found " + foundIndentation));
            }
        }

        if (token instanceof org.yaml.snakeyaml.tokens.ScalarToken && (boolean)conf.get(OPTION_CHECK_MULTI_LINE_STRINGS)) {
            problems.addAll(checkScalarIndentation(token, context));
        }

        // Step 2. a:

        if (isVisible) {
            context.put(CURRENT_LINE_KEY, getRealEndLine(token));
            if (firstInLine) {
                context.put(CURRENT_LINE_INDENT_KEY, foundIndentation);
            }
        }

        // Step 2. b: Update state

        Integer indent = null;
        if (token instanceof BlockMappingStartToken) {
            //   - a: 1
            // or
            //   - ? a
            //     : 1
            // or
            //   - ?
            //       a
            //     : 1
            assert next instanceof KeyToken;
            assert next.getStartMark().getLine() == token.getStartMark().getLine();

            indent = token.getStartMark().getColumn();

            stack.add(new Parent(LABEL.B_MAP, indent));

        } else if (token instanceof FlowMappingStartToken) {
            if (next.getStartMark().getLine() == token.getStartMark().getLine()) {
                //   - {a: 1, b: 2}
                indent = next.getStartMark().getColumn();
            } else {
                //   - {
                //     a: 1, b: 2
                //   }
                indent = detectIndent((int)context.get(CURRENT_LINE_INDENT_KEY), next, context);
            }

            stack.add(new Parent(LABEL.F_MAP, indent, (int)context.get(CURRENT_LINE_INDENT_KEY)));

        } else if (token instanceof BlockSequenceStartToken) {
            //   - - a
            //     - b
            assert next instanceof BlockEntryToken;
            assert next.getStartMark().getLine() == token.getStartMark().getLine();

            indent = token.getStartMark().getColumn();

            stack.add(new Parent(LABEL.B_SEQ, indent));

        } else if (token instanceof BlockEntryToken &&
                // in case of an empty entry
                !(next instanceof BlockEntryToken || next instanceof BlockEndToken)) {
            // It looks like pyyaml doesn 't issue BlockSequenceStartTokens when the
            // list is not indented.We need to compensate that.
            if (stack.get(stack.size() - 1).type != LABEL.B_SEQ) {
                stack.add(new Parent(LABEL.B_SEQ, token.getStartMark().getColumn()));
                stack.get(stack.size() - 1).implicitBlockSeq = true;
            }

            if (next.getStartMark().getLine() == token.getEndMark().getLine()) {
                //   - item 1
                //   - item 2
                indent = next.getStartMark().getColumn();
            } else if (next.getStartMark().getColumn() == token.getStartMark().getColumn()) {
                //   -
                //   key: value
                indent = next.getStartMark().getColumn();
            } else {
                //   -
                //     item 1
                //   -
                //     key:
                //       value
                indent = detectIndent(token.getStartMark().getColumn(), next, context);
            }

            stack.add(new Parent(LABEL.B_ENT, indent));

        } else if (token instanceof FlowSequenceStartToken) {
            if (next.getStartMark().getLine() == token.getStartMark().getLine()) {
                //   - [a, b]
                indent = next.getStartMark().getColumn();
            } else {
                //   - [
                //   a, b
                // ]
                indent = detectIndent((int)context.get(CURRENT_LINE_INDENT_KEY), next, context);
            }

            stack.add(new Parent(LABEL.F_SEQ, indent,(int)context.get(CURRENT_LINE_INDENT_KEY)));

        } else if (token instanceof KeyToken) {
            indent = stack.get(stack.size() - 1).indent;

            stack.add(new Parent(LABEL.KEY, indent));

            stack.get(stack.size() - 1).explicitKey = isExplicitKey(token);

        } else if (token instanceof ValueToken) {
            assert stack.get(stack.size() - 1).type == LABEL.KEY;

            // Special cases:
            //     key: &anchor
            //       value
            // and:
            //     key: !!tag
            //       value
            if (next instanceof AnchorToken || next instanceof TagToken) {
                if (next.getStartMark().getLine() == prev.getStartMark().getLine() &&
                        next.getStartMark().getLine() < nextnext.getStartMark().getLine()) {
                    // May not be a good Java practice but it is the easiest way to proceed
                    next = nextnext;
                }
            }

            // Only if value is not empty
            if (!(next instanceof BlockEndToken || next instanceof FlowMappingEndToken || next instanceof FlowSequenceEndToken || next instanceof KeyToken)) {
                if (stack.get(stack.size() - 1).explicitKey) {
                    //   ? k
                    //   : value
                    // or
                    //   ? k
                    //   :
                    //     value
                    indent = detectIndent(stack.get(stack.size() - 1).indent, next, context);
                } else if (next.getStartMark().getLine() == prev.getStartMark().getLine()) {
                    //   k: value
                    indent = next.getStartMark().getColumn();
                } else if (next instanceof BlockSequenceStartToken || next instanceof BlockEntryToken) {
                    //  NOTE: We add BlockEntryToken in the test above because
                    //  sometimes BlockSequenceStartToken are not issued. Try
                    //  yaml.scan()ning this:
                    //      '- lib:\n'
                    //      '  - var\n'
                    if (context.get(OPTION_INDENT_SEQUENCES) instanceof Boolean && !(boolean)context.get(OPTION_INDENT_SEQUENCES)) {
                        indent = stack.get(stack.size() - 1).indent;
                    } else if (context.get(OPTION_INDENT_SEQUENCES) instanceof Boolean && (boolean)context.get(OPTION_INDENT_SEQUENCES)) {
                        if (OPTION_CONSISTENT.equals(context.get(OPTION_SPACES)) && next.getStartMark().getColumn() - stack.get(stack.size() - 1).indent == 0) {
                            //In this case, the block sequence item is not indented
                            // (while it should be), but we don't know yet the
                            // indentation it should have (because `spaces` is
                            // `consistent` and its value has not been computed yet
                            // -- this is probably the beginning of the document).
                            // So we choose an arbitrary value (2).
                            indent = 2;
                        } else {
                            indent = detectIndent(stack.get(stack.size() - 1).indent, next, context);
                        }
                    } else {  // 'whatever' or 'consistent'
                        if (next.getStartMark().getColumn() == stack.get(stack.size() - 1).indent) {
                            //   key:
                            //   - e1
                            //   - e2
                            if (OPTION_CONSISTENT.equals(context.get(OPTION_INDENT_SEQUENCES))) {
                                context.put(OPTION_INDENT_SEQUENCES, false);
                            }
                            indent = stack.get(stack.size() - 1).indent;
                        } else {
                            if (OPTION_CONSISTENT.equals(context.get(OPTION_INDENT_SEQUENCES))) {
                                context.put(OPTION_INDENT_SEQUENCES, true);
                            }
                            //   key:
                            //     - e1
                            //     - e2
                            indent = detectIndent(stack.get(stack.size() - 1).indent, next, context);
                        }
                    }
                } else {
                    //   k:
                    //     value
                    indent = detectIndent(stack.get(stack.size() - 1).indent, next, context);
                }

                stack.add(new Parent(LABEL.VAL, indent));
            }
        }

        boolean consumedCurrentToken = false;
        while (true) {
            if (
                    (stack.get(stack.size() - 1).type == LABEL.F_SEQ &&
                    token instanceof FlowSequenceEndToken &&
                    !consumedCurrentToken)
                            ||
                    (stack.get(stack.size() - 1).type == LABEL.F_MAP &&
                            token instanceof FlowMappingEndToken &&
                            !consumedCurrentToken)
                            ||
                    ((stack.get(stack.size() - 1).type  == LABEL.B_MAP || stack.get(stack.size() - 1).type  == LABEL.B_SEQ) &&
                            token instanceof BlockEndToken &&
                            !stack.get(stack.size() - 1).implicitBlockSeq &&
                            !consumedCurrentToken)
                    ) {
                stack.remove(stack.size() - 1);
                consumedCurrentToken = true;

            } else if (stack.get(stack.size() - 1).type == LABEL.B_ENT &&
                    !(token instanceof BlockEntryToken) &&
                    stack.get(stack.size() - 2).implicitBlockSeq &&
                    !(token instanceof AnchorToken || token instanceof TagToken) &&
                    !(next instanceof BlockEntryToken)) {
                stack.remove(stack.size() - 1);
                stack.remove(stack.size() - 1);

            } else if (stack.get(stack.size() - 1).type == LABEL.B_ENT &&
                    (next instanceof BlockEntryToken || next instanceof BlockEndToken)) {
                stack.remove(stack.size() - 1);

            } else if (stack.get(stack.size() - 1).type == LABEL.VAL &&
                    !(token instanceof ValueToken) &&
                    !(token instanceof AnchorToken || token instanceof TagToken)) {
                assert stack.get(stack.size() - 2).type == LABEL.KEY;
                stack.remove(stack.size() - 1);
                stack.remove(stack.size() - 1);

            } else if (stack.get(stack.size() - 1).type == LABEL.KEY &&
                    (next instanceof BlockEndToken || next instanceof FlowMappingEndToken || next instanceof FlowSequenceEndToken || next instanceof KeyToken)) {
                // A key without a value: it's part of a set. Let's drop this key
                // and leave room for the next one.
                stack.remove(stack.size() - 1);

            } else {
                break;
            }
        }

        return problems;
    }

    private int detectIndent(int baseIndent, Token next, Map context) {
        if (!(context.get(OPTION_SPACES) instanceof Integer)) {
            context.put(OPTION_SPACES, next.getStartMark().getColumn() - baseIndent);
        }

        return baseIndent + (int)context.get(OPTION_SPACES);
    }


    public class Parent {
        private LABEL type;
        private Integer indent;
        private Integer lineIndent;
        private boolean explicitKey;
        private boolean implicitBlockSeq;

        public Parent(LABEL type, int indent) {
            this(type, indent, null);
        }

        public Parent(LABEL type, int indent, Integer lineIndent) {
            this.type = type;
            this.indent = indent;
            this.lineIndent = lineIndent;
            explicitKey = false;
            implicitBlockSeq = false;
        }

        @Override
        public String toString() {
            return String.format("%1$s:%2$d", type, indent);
        }
    }
}
