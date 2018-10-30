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
