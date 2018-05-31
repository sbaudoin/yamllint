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

import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.scanner.ScannerImpl;
import org.yaml.snakeyaml.tokens.Token;

/**
 * Is a wrapper to <code>org.yaml.snakeyaml.scanner.ScannerImpl</code> for easing the
 * handling of some <code>java.lang.IndexOutOfBoundsException</code> that can occur
 * with <code>peekToken()</code> and <code>getToken()</code> when there is no more
 * tokens to get.
 *
 * @see org.yaml.snakeyaml.scanner.ScannerImpl
 */
public class LintScanner {
    private ScannerImpl scanner;

    /**
     * Constructor
     *
     * @param reader a reader to the YAML content
     * @see org.yaml.snakeyaml.reader.StreamReader
     * @see LintStreamReader
     */
    public LintScanner(StreamReader reader) {
        scanner = new ScannerImpl(reader);
    }


    /**
     * Returns the current token or <code>null</code> if there is no more
     * tokens to be read in the input stream. The token remains in the stack.
     *
     * @return a <code>Token</code> or <code>null</code> if none found
     */
    public Token peekToken() {
        try {
            return scanner.peekToken();
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Unstacks and returns the current token and moves or <code>null</code> if
     * there is no more tokens to be read in the input stream
     *
     * @return a <code>Token</code> or <code>null</code> if none found
     */
    public Token getToken() {
        try {
            scanner.checkToken();
            return scanner.getToken();
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Tells if there are still some tokens to be scanned and returned by {@link #getToken()} or {@link #peekToken()}
     *
     * @return {@code true} if there are still token to come, {@code false} if not
     */
    public boolean hasMoreTokens() {
        return scanner.checkToken();
    }
}
