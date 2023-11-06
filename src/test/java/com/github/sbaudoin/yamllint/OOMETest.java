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

import junit.framework.TestCase;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

public class OOMETest extends TestCase {
    public void test() throws IOException {
        LintScanner scanner = new LintScanner(new LintStreamReader(new FileReader(Paths.get("src", "test", "resources", "oome.yml").toFile())));
        int i = 0;
        while (scanner.hasMoreTokens()) {
            scanner.getToken();
            i++;
        }
        assertEquals(58457, i);
    }
}
