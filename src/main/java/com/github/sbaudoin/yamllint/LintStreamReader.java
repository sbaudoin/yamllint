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
package com.github.sbaudoin.yamllint;

import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.scanner.Constant;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>StreamReader</code> intended to replace SnakeYAML's standard class for loading the whole content in memory.
 * This is not optimized but this is a requirement to permanently be able to have a complete access to the complete file
 * content without windowing.
 */
public class LintStreamReader extends StreamReader {
    private String name;

    /**
     * Real length of the data in dataWindow
     */
    private int dataLength;

    /**
     * The variable points to the current position in the data array
     */
    private int pointer = 0;
    /**
     * index is only required to implement 1024 key length restriction
     * http://yaml.org/spec/1.1/#simple key/
     * It must count code points, but it counts characters (to be fixed)
     */
    private int index = 0; // in code points
    private int line = 0;
    private int column = 0; //in code points
    private char[] buffer;


    /**
     * Constructor
     *
     * @param stream a string representing a YAML content
     */
    public LintStreamReader(String stream) {
        this(new StringReader(stream));
        this.name = "'string'";
    }

    /**
     * Constructor
     *
     * @param reader a reader
     */
    public LintStreamReader(Reader reader) {
        super(reader);

        this.name = "'reader'";
        this.dataLength = 0;
        try {
            this.buffer = read(reader);
        } catch (IOException e) {
            this.buffer = new char[0];
            throw new IllegalArgumentException("cannot read data from reader", e);
        }
    }


    @Override
    public Mark getMark() {
        return new Mark(name, this.index, this.line, this.column, this.buffer, this.pointer);
    }

    /**
     * read the next length characters and move the pointer.
     * if the last character is high surrogate one more character will be read
     *
     * @param length amount of characters to move forward
     */
    @Override
    public void forward(int length) {
        for (int i = 0; i < length && hasEnoughData(); i++) {
            int c = buffer[pointer++];
            this.index++;
            if (Constant.LINEBR.has(c)
                    || (c == '\r' && (hasEnoughData() && buffer[pointer] != '\n'))) {
                this.line++;
                this.column = 0;
            } else if (c != 0xFEFF) {
                this.column++;
            }
        }
    }

    /**
     * Peeks the current code point
     *
     * @return the current code point or <code>\0</code> if no more code points exist
     */
    @Override
    public int peek() {
        return (hasEnoughData()) ? buffer[pointer] : '\0';
    }

    /**
     * Peek the next index-th code point
     *
     * @param index to peek
     * @return the next index-th code point
     */
    @Override
    public int peek(int index) {
        return (hasEnoughData(index)) ? buffer[pointer + index] : '\0';
    }

    /**
     * peek the next length code points
     *
     * @param length amount of the characters to peek
     * @return the next length code points
     */
    @Override
    public String prefix(int length) {
        if (length == 0) {
            return "";
        } else if (hasEnoughData(length)) {
            return new String(this.buffer, pointer, length);
        } else {
            return new String(this.buffer, pointer,
                    Math.min(length, dataLength - pointer));
        }
    }

    /**
     * prefix(length) immediately followed by forward(length)
     * @param length amount of characters to get
     * @return the next length code points
     */
    @Override
    public String prefixForward(int length) {
        final String prefix = prefix(length);
        this.pointer += length;
        this.index += length;
        // prefix never contains new line characters
        this.column += length;
        return prefix;
    }


    private boolean hasEnoughData() {
        return hasEnoughData(0);
    }

    private boolean hasEnoughData(int size) {
        return (this.pointer + size) < buffer.length;
    }

    private char[] read(Reader reader) throws IOException {
        List<Character> data = new ArrayList<>();
        int c;
        while ((c = reader.read()) >= 0) {
            data.add((char)c);
        }
        // Convert list to array
        dataLength = data.size();
        char[] b = new char[dataLength];
        for(int i = 0; i < dataLength; i++) {
            b[i] = data.get(i);
        }
        return b;
    }


    @Override
    public int getColumn() {
        return column;
    }

    /**
     * @return current position as number (in characters) from the beginning of the stream
     */
    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getLine() {
        return line;
    }
}
