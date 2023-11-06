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

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class LintStreamReaderTest {
    @Test
    void testConstructors() {
        assertEquals("'string'", new LintStreamReader("").getMark().getName());
        assertEquals("'reader'", new LintStreamReader(new StringReader("")).getMark().getName());

        try {
            new LintStreamReader(new PipedReader());
            fail("Unreadable readers should not be accepted");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    void testForward() {
        LintStreamReader reader = new LintStreamReader("test");
        while (reader.peek() != '\u0000') {
            reader.forward(1);
        }
        reader = new LintStreamReader("test");
        assertEquals('t', reader.peek());
        reader.forward(1);
        assertEquals('e', reader.peek());
        reader.forward(1);
        assertEquals('s', reader.peek());
        reader.forward(1);
        assertEquals('t', reader.peek());
        reader.forward(1);
        assertEquals('\u0000', reader.peek());
    }

    @Test
    void testPeekInt() {
        LintStreamReader reader = new LintStreamReader("test");
        assertEquals('t', reader.peek(0));
        assertEquals('e', reader.peek(1));
        assertEquals('s', reader.peek(2));
        assertEquals('t', reader.peek(3));
        reader.forward(1);
        assertEquals('e', reader.peek(0));
        assertEquals('s', reader.peek(1));
        assertEquals('t', reader.peek(2));
    }
}