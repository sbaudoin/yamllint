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

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Format.class)
public class CliTest {
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void testDummy() {
        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        cli.setErrOutputStream(err);

        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> {
            assertEquals("", std.toString());
            assertTrue(err.toString().startsWith("Error: FILE_OR_DIR is required"));
        });
        cli.run(new String[] {});
    }

    @Test
    public void testSetStdOutputStream() {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli1.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> assertEquals(
                path + System.lineSeparator() +
                        "  2:8       warning  too few spaces before comment  (comments)" + System.lineSeparator() +
                        "  3:16      error    syntax error: mapping values are not allowed here" + System.lineSeparator() +
                        "                     mapping values are not allowed here" + System.lineSeparator() +
                        "                      in 'reader', line 3, column 16:" + System.lineSeparator() +
                        "                         - invalid: yaml:" + System.lineSeparator() +
                        "                                        ^" + System.lineSeparator() + System.lineSeparator(),
                std.toString()));
        cli.run(new String[] { path });
    }

    @Test
    public void testSetErrOutputStream() {
        Cli cli = new Cli();

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        cli.setErrOutputStream(err);

        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> assertTrue(err.toString().contains("Error: FILE_OR_DIR is required")));
        cli.run(new String[] { "-s" });
    }

    @Test
    public void testWrongOutputFormat() {
        Cli cli = new Cli();

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        cli.setErrOutputStream(err);

        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> assertTrue(err.toString().contains("Error: invalid output format")));
        cli.run(new String[] { "-f", "foo" });
    }

    @Test
    public void testRecursive() {
        String dirPath = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "recursive";
        String path1 = dirPath + File.separator + "cli2.yml";
        String path2 = dirPath + File.separator + "sub" + File.separator + "cli3.yaml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(() -> assertEquals(
                new HashSet<>(Arrays.asList(path1 + ":2:8:comments:warning:too few spaces before comment",
                        path2 + ":1:1:document-start:warning:missing document start \"---\"")),
                new HashSet<>(Arrays.asList(std.toString().trim().split(System.lineSeparator())))));
        cli.run(new String[] { "-f", "parsable", dirPath });
    }

    @Test
    public void testStrict() {
        final String dirPath = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "recursive" + File.separator + "sub";
        final String path = dirPath + File.separator + "cli3.yaml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        exit.expectSystemExitWithStatus(2);
        exit.checkAssertionAfterwards(() -> assertEquals(
                path + ":1:1:document-start:warning:missing document start \"---\"" + System.lineSeparator(),
                std.toString()));
        cli.run(new String[] { "-s", "-f", "parsable", dirPath });
    }

    @Test
    public void testNoWarnings() {
        final String dirPath = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "recursive" + File.separator + "sub";
        final String path = dirPath + File.separator + "cli3.yaml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(() -> assertEquals(
                "",
                std.toString()));
        cli.run(new String[] { "-s", "-f", "parsable", "--no-warnings", dirPath });
    }

    @Test
    public void testWrongConfiguration() {
        Cli cli = new Cli();

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        cli.setErrOutputStream(err);

        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> assertTrue(err.toString().contains("Error: cannot get or process configuration")));
        cli.run(new String[] { "-d", "\"foo: bar: error\"", "foo.yml" });
    }

    @Test
    public void testShowHelpShort() {
        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        cli.setErrOutputStream(err);

        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(() -> {
            assertTrue(std.toString().contains("A linter for YAML files"));
            assertEquals("", err.toString());
        });
        cli.run(new String[] { "-h" });
    }

    @Test
    public void testShowVersion1() throws IOException {
        testShowVersion("--version");
    }

    @Test
    public void testShowVersion2() throws IOException {
        testShowVersion("-v");
    }

    private void testShowVersion(String option) throws IOException {
        Cli cli = new Cli();

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        cli.setErrOutputStream(err);

        // Get version
        Properties props = new Properties();
        props.load(cli.getClass().getClassLoader().getResourceAsStream("yaml.properties"));

        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(() -> assertEquals(Cli.APP_NAME + " " + props.getProperty("version") + System.lineSeparator(), err.toString()));
        cli.run(new String[] { option });
    }

    @Test
    public void testMutuallyExcludedOptions() {
        Cli cli = new Cli();

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        cli.setErrOutputStream(err);

        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> assertTrue(err.toString().contains("Error: options `c' and `d' are mutually exclusive.")));
        cli.run(new String[] { "-c", "conf.yaml", "-d", "\"---\"" });
    }

    @Test
    public void testConfData1() {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli5.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(() -> assertEquals(
                path + ":3:3:hyphens:warning:too many spaces after hyphen" + System.lineSeparator(),
                std.toString()));
        cli.run(new String[] { "-d", "relaxed", "-f", "parsable", path });
    }

    @Test
    public void testConfData2() {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli5.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> assertEquals(
                path + ":3:3:hyphens:error:too many spaces after hyphen" + System.lineSeparator(),
                std.toString()));
        cli.run(new String[] { "-d", "\"rules:\n  hyphens:\n    max-spaces-after: 1\"", "-f", "parsable", path });
    }

    @Test
    public void testConfFile() {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli5.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(() -> assertEquals(
                path + ":2:8:comments:warning:too few spaces before comment" + System.lineSeparator(),
                std.toString()));
        cli.run(new String[] { "-f", "parsable", "-c", "src" + File.separator + "test" + File.separator + "resources" + File.separator + "config" + File.separator + "XDG" + File.separator + "yamllint" + File.separator + "config", path });
    }

    @Test
    public void testParsableFormat() {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli1.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> assertEquals(
                new HashSet<>(Arrays.asList(path + ":2:8:comments:warning:too few spaces before comment",
                        path + ":3:16::error:syntax error: mapping values are not allowed here")),
                new HashSet<>(Arrays.asList(std.toString().trim().split(System.lineSeparator())))));
        cli.run(new String[] { "-f", "parsable", path });
    }

    @Test
    public void testGitHubFormat() {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli1.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> assertEquals(
                new HashSet<>(Arrays.asList("::warning file=" + path + ",line=2,col=8::[comments] too few spaces before comment",
                        "::error file=" + path + ",line=3,col=16::syntax error: mapping values are not allowed here")),
                new HashSet<>(Arrays.asList(std.toString().trim().split(System.lineSeparator())))));
        cli.run(new String[] { "-f", "github", path });
    }

    @Test
    public void testColoredOutput() {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli5.yml";

        PowerMockito.spy(Format.class);
        when(Format.supportsColor()).thenReturn(true);

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(() -> assertEquals(
                Format.ANSI_UNDERLINED + path + Format.ANSI_RESET + System.lineSeparator() +
                "  " + Format.ANSI_FAINT + "3:3" + Format.ANSI_RESET + "       " + Format.ANSI_YELLOW + "warning" + Format.ANSI_RESET +
                        "  too many spaces after hyphen  " + Format.ANSI_FAINT + "(hyphens)" + Format.ANSI_RESET + System.lineSeparator() + System.lineSeparator(),
                std.toString()));
        cli.run(new String[] { "-d", "relaxed", path });
    }

    @Test
    public void testGlobalConfig1() {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli5.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        environmentVariables.set("XDG_CONFIG_HOME", "src" + File.separator + "test" + File.separator + "resources" + File.separator + "config" + File.separator + "XDG");
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(() -> assertEquals(
                        path + ":2:8:comments:warning:too few spaces before comment" + System.lineSeparator(),
                std.toString()));
        cli.run(new String[] { "-f", "parsable", path });
    }

    @Test
    public void testGlobalConfig2() {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli5.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        String userHome = System.getProperty("user.home");
        System.setProperty("user.home", System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "config" + File.separator + "home");
        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> {
            assertEquals(
                    path + ":3:3:hyphens:error:too many spaces after hyphen" + System.lineSeparator(), std.toString());
            // Need to restore user.home for the other tests
            System.setProperty("user.home", userHome);
        });
        cli.run(new String[] { "-f", "parsable", path });
    }

    @Test
    public void testLocalConfig() throws IOException {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli5.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        Files.copy(Paths.get("src", "test", "resources", "config", "local", Cli.USER_CONF_FILENAME), Paths.get(Cli.USER_CONF_FILENAME), StandardCopyOption.REPLACE_EXISTING);
        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> {
            assertEquals(
                    path + ":3:3:hyphens:error:too many spaces after hyphen" + System.lineSeparator(), std.toString());
            // Need to restore user.home for the other tests
            Files.delete(Paths.get(Cli.USER_CONF_FILENAME));
        });
        cli.run(new String[] { "-f", "parsable", path });
    }
}
