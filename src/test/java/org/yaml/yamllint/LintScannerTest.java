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
import org.yaml.snakeyaml.reader.StreamReader;

public class LintScannerTest extends TestCase {
    public void testGetToken() {
        LintScanner scanner = new LintScanner(new StreamReader("key1: value\nkey2: value"));
        try {
            while (scanner.getToken() != null) {
                // Do nothing: we just want to go through all token
            }
            assertTrue(true);
        } catch (IndexOutOfBoundsException e) {
            fail("IndexOutOfBoundsException raised while reading tokens");
        }
    }

    public void testPeekToken() {
        LintScanner scanner = new LintScanner(new StreamReader("key1: value\nkey2: value"));
        try {
            while (scanner.peekToken() != null) {
                // Looks like this is redundant compared to testGetToken()...
                scanner.getToken();
            }
            assertTrue(true);
        } catch (IndexOutOfBoundsException e) {
            fail("IndexOutOfBoundsException raised while reading tokens");
        }
    }

    public void testHasMoreTokens() {
        LintScanner scanner = new LintScanner(new StreamReader("key1: value\nkey2: value"));
        assertTrue(scanner.hasMoreTokens()); // StreamStartToken
        for (int i = 0; i < 11; i++) {
            scanner.getToken();
        }
        assertTrue(scanner.hasMoreTokens());
        scanner.getToken(); // StreamEndToken
        assertFalse(scanner.hasMoreTokens());
    }
}