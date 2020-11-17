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

import org.yaml.snakeyaml.scanner.ScannerException;
import org.yaml.snakeyaml.tokens.StreamEndToken;
import org.yaml.snakeyaml.tokens.StreamStartToken;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Main YAMLLint parser utility class. Use the methods of this class to tokenize a YAML string.
 */
public class Parser {
    /**
     * All classes that implement this interface can expose a line number
     */
    public interface Lined {
        int getLineNo();
    }

    /**
     * Simple wrapper class for lines: a line is contained within a given string (the boundaries are given
     * by <var>start</var> and <var>end</var>) and has a line number
     */
    public static class Line implements Lined {
        private int lineNo;
        private int start;
        private int end;
        private String buffer;


        /**
         * Constructor
         *
         * @param lineNo the line number
         * @param buffer the string in which the line can be found
         * @param start the start index of the line in <var>buffer</var>
         * @param end the end index of the line in <var>buffer</var>
         */
        public Line(int lineNo, String buffer, int start, int end) {
            this.lineNo = lineNo;
            this.start  = start;
            this.end    = end;
            this.buffer = buffer;
        }

        /**
         * Returns the actual line content
         *
         * @return the line content taken from the buffer between <var>start</var> and <var>end</var>
         */
        public String getContent() {
            return buffer.substring(start, end);
        }

        @Override
        public int getLineNo() {
            return lineNo;
        }

        /**
         * Returns the end index
         *
         * @return the end index
         */
        public int getEnd() {
            return end;
        }

        /**
         * Returns the start index
         *
         * @return the start index
         */
        public int getStart() {
            return start;
        }

        /**
         * Returns the buffer in which we can find the line
         *
         * @return the buffer in which we can find the line
         */
        public String getBuffer() {
            return buffer;
        }
    }

    /**
     * Simple wrapper class for token: a token is found on a line and has neighbour tokens
     */
    public static class Token implements Lined {
        private int lineNo;
        private org.yaml.snakeyaml.tokens.Token curr;
        private org.yaml.snakeyaml.tokens.Token prev;
        private org.yaml.snakeyaml.tokens.Token next;
        private org.yaml.snakeyaml.tokens.Token nextnext;


        /**
         * Constructor
         *
         * @param lineNo the line number at which we can find the token
         * @param curr the token itself
         * @param prev the previous token
         * @param next the next token
         * @param nextnext the next next token
         */
        public Token(int lineNo, org.yaml.snakeyaml.tokens.Token curr, org.yaml.snakeyaml.tokens.Token prev, org.yaml.snakeyaml.tokens.Token next, org.yaml.snakeyaml.tokens.Token nextnext) {
            this.lineNo   = lineNo;
            this.curr     = curr;
            this.prev     = prev;
            this.next     = next;
            this.nextnext = nextnext;
        }

        @Override
        public int getLineNo() {
            return lineNo;
        }

        /**
         * Returns the token wrapped by this class
         *
         * @return the token wrapped by this class
         */
        public org.yaml.snakeyaml.tokens.Token getCurr() {
            return curr;
        }

        /**
         * Returns the token located right before the token wrapped by this class
         *
         * @return the token located right before the token wrapped by this class
         */
        public org.yaml.snakeyaml.tokens.Token getPrev() {
            return prev;
        }

        /**
         * Returns the token located right after the token wrapped by this class
         *
         * @return the token located right after the token wrapped by this class
         */
        public org.yaml.snakeyaml.tokens.Token getNext() {
            return next;
        }

        /**
         * Returns the token located right after the next token
         *
         * @return the token located right after the next token
         */
        public org.yaml.snakeyaml.tokens.Token getNextNext() {
            return nextnext;
        }
    }

    /**
     * Simple wrapper class for comments: a YAML comment start with a {@code #} character, is located on a line
     * and starts at a given column number on this line. It also has tokens located before and after it.
     */
    public static class Comment implements Lined {
        private int lineNo;
        private int columnNo;
        private int pointer;
        private String buffer;
        private org.yaml.snakeyaml.tokens.Token tokenBefore;
        private org.yaml.snakeyaml.tokens.Token tokenAfter;
        private Comment commentBefore;


        /**
         * Constructor
         *
         * @param lineNo the number of the line where the comment is located
         * @param columnNo the column number at which the comment starts
         * @param buffer the string where we can find the comment
         * @param pointer the start index of the comment in <var>buffer</var>
         * @param tokenBefore the token located right before this comment
         * @param tokenAfter the token located right after this comment
         * @param commentBefore the possible comment located before this comment
         */
        public Comment(int lineNo, int columnNo, String buffer, int pointer,
                       org.yaml.snakeyaml.tokens.Token tokenBefore,
                       org.yaml.snakeyaml.tokens.Token tokenAfter, Comment commentBefore) {
            this.lineNo        = lineNo;
            this.columnNo      = columnNo;
            this.buffer        = buffer;
            this.pointer       = pointer;
            this.tokenBefore   = tokenBefore;
            this.tokenAfter    = tokenAfter;
            this.commentBefore = commentBefore;
        }

        @Override
        public String toString() {
            int end = buffer.indexOf('\n', pointer);
            if (end == -1) {
                end = buffer.indexOf('\0', pointer);
            }
            if (end != -1) {
                return buffer.substring(pointer, end);
            }
            return buffer.substring(pointer);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Comment &&
                    lineNo == ((Comment)other).lineNo &&
                    columnNo == ((Comment)other).columnNo &&
                    toString().equals(other.toString());
        }

        @Override
        public int hashCode() {
            return (lineNo + ":" + columnNo + ":" + toString()).hashCode();
        }

        /**
         * Returns {@code true} if the comment is inline. Inline comments appear on a line after some text:
         * <pre>
         *     foo: bar # This is inline comment
         * </pre>
         *
         * @return {@code true} if the comment is inline, {@code false} if not
         */
        public boolean isInline() {
            return !(tokenBefore instanceof StreamStartToken) &&
                    lineNo == tokenBefore.getEndMark().getLine() + 1 &&
                    // sometimes token end marks are on the next line
                    buffer.charAt(tokenBefore.getEndMark().getPointer() - 1) != '\n';
        }

        @Override
        public int getLineNo() {
            return lineNo;
        }

        /**
         * Returns the column number at which the comment starts
         *
         * @return the column number at which the comment starts
         */
        public int getColumnNo() {
            return columnNo;
        }

        /**
         * Returns the end index of the comment in <var>buffer</var>
         *
         * @return the end index of the comment in <var>buffer</var>
         */
        public int getPointer() {
            return pointer;
        }

        /**
         * Returns the token located right before this comment
         *
         * @return the token located right before this comment
         */
        public org.yaml.snakeyaml.tokens.Token getTokenBefore() {
            return tokenBefore;
        }

        /**
         * Returns the token located right after this comment
         *
         * @return the token located right after this comment
         */
        public org.yaml.snakeyaml.tokens.Token getTokenAfter() {
            return tokenAfter;
        }

        /**
         * Returns the possible comment located before this comment
         *
         * @return the possible comment located before this comment
         */
        public Comment getCommentBefore() {
            return commentBefore;
        }

        public String getBuffer() {
            return buffer;
        }
    }


    /**
     * Hide constructor, only static methods
     */
    private Parser() {
    }


    /**
     * Parses the passed string and returns the lines found in this text
     *
     * @param buffer a string to be parsed
     * @return the list of lines found in the string
     */
    public static List<Line> getLines(final String buffer) {
        List<Line> lines = new ArrayList<>();
        int lineNo = 1;
        int cur = 0;
        int next = buffer.indexOf('\n');
        while (next != -1) {
            lines.add(new Line(lineNo, buffer, cur, next));
            cur = next + 1;
            next = buffer.indexOf('\n', cur);
            lineNo += 1;
        }

        lines.add(new Line(lineNo, buffer, cur, buffer.length()));

        return lines;
    }

    /**
     * Finds all comments between two tokens
     *
     * @param token1 a first token
     * @param token2 a second token
     * @return the comments found between the two tokens
     */
    public static List<Comment> commentsBetweenTokens(final org.yaml.snakeyaml.tokens.Token token1, final @Nullable org.yaml.snakeyaml.tokens.Token token2) {
        List<Comment> comments = new ArrayList<>();
        String buf;
        if (token2 == null) {
            buf = toString(
                    Arrays.copyOfRange(token1.getEndMark().getBuffer(), token1.getEndMark().getPointer(), token1.getEndMark().getBuffer().length)
            );
        } else if (token1.getEndMark().getLine() == token2.getStartMark().getLine() &&
                !(token1 instanceof StreamStartToken) &&
                !(token2 instanceof StreamEndToken)) {
            return comments;
        } else {
            buf = toString(
                    Arrays.copyOfRange(token1.getEndMark().getBuffer(), token1.getEndMark().getPointer(), token2.getStartMark().getPointer())
            );
        }

        int lineNo = token1.getEndMark().getLine() + 1;
        int columnNo = token1.getEndMark().getColumn() + 1;
        int pointer = token1.getEndMark().getPointer();

        Comment commentBefore = null;
        int pos;
        for (String line : buf.split("\n")) {
            pos = line.indexOf('#');
            if (pos != -1) {
                Comment comment = new Comment(lineNo, columnNo + pos,
                        toString(token1.getEndMark().getBuffer()), pointer + pos,
                        token1, token2, commentBefore);
                comments.add(comment);

                commentBefore = comment;
            }

            pointer += line.length() + 1;
            lineNo += 1;
            columnNo = 1;
        }

        return comments;
    }

    /**
     * Identifies and returns the tokens and comments contained in the passed string
     *
     * @param buffer a string to be parsed
     * @return the list of tokens and comments found in the string
     */
    public static List<Lined> getTokensOrComments(final String buffer) {
        LintScanner yamlLoader = new LintScanner(new LintStreamReader(buffer));

        List<Lined> tokensOrComments = new ArrayList<>();
        try {
            org.yaml.snakeyaml.tokens.Token prev = null;
            org.yaml.snakeyaml.tokens.Token curr = yamlLoader.getToken();
            while (curr != null) {
                org.yaml.snakeyaml.tokens.Token next = yamlLoader.getToken();
                org.yaml.snakeyaml.tokens.Token nextnext = yamlLoader.peekToken();

                tokensOrComments.add(new Token(curr.getStartMark().getLine() + 1, curr, prev, next, nextnext));

                tokensOrComments.addAll(commentsBetweenTokens(curr, next));

                prev = curr;
                curr = next;
            }
        } catch (ScannerException e) {
            // Do nothing, just skip the token
        }
        return tokensOrComments;
    }

    /**
     * Generator that mixes tokens and lines, ordering them by line number
     *
     * @param buffer a string to be parsed
     * @return all tokens, comments and lines found in the passed string
     */
    public static List<Lined> getTokensOrCommentsOrLines(final String buffer) {
        List<Lined> objects = new ArrayList<>();
        Iterator<Lined> tokensOrComments = getTokensOrComments(buffer).iterator();
        Iterator<Line> lines = getLines(buffer).iterator();

        Lined tokenOrComment = (tokensOrComments.hasNext())?tokensOrComments.next():null;
        Line line = (lines.hasNext())?lines.next():null;
        while (tokenOrComment != null || line != null) {
            if (tokenOrComment == null || (line != null && tokenOrComment.getLineNo() > line.lineNo)) {
                objects.add(line);
                line = (lines.hasNext())?lines.next():null;
            } else {
                objects.add(tokenOrComment);
                tokenOrComment = (tokensOrComments.hasNext())?tokensOrComments.next():null;
            }
        }

        return objects;
    }


    /**
     * Converts the passed array of bytes represented by ints into a string
     *
     * @param array an array of bytes
     * @return a string
     */
    private static String toString(int[] array) {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(array).forEach(i -> sb.append((char)i));
        return sb.toString();
    }
}
