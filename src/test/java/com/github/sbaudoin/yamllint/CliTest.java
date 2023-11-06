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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

import static com.github.stefanbirkner.systemlambda.SystemLambda.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliTest {
    @Test
    void testDummy() throws Exception {
        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        cli.setErrOutputStream(err);

        int statusCode = catchSystemExit(() -> cli.run(new String[] {}));
        assertEquals(1, statusCode);
        assertEquals("", std.toString());
        assertTrue(err.toString().startsWith("Error: FILE_OR_DIR is required"));
    }

    @Test
    void testSetStdOutputStream() throws Exception {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli1.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        int statusCode = catchSystemExit(() -> cli.run(new String[] { path }));
        assertEquals(1, statusCode);
        assertEquals(path + System.lineSeparator() +
                        "  2:8       warning  too few spaces before comment  (comments)" + System.lineSeparator() +
                        "  3:16      error    syntax error: mapping values are not allowed here" + System.lineSeparator() +
                        "                     mapping values are not allowed here" + System.lineSeparator() +
                        "                      in 'reader', line 3, column 16:" + System.lineSeparator() +
                        "                         - invalid: yaml:" + System.lineSeparator() +
                        "                                        ^" + System.lineSeparator() + System.lineSeparator(),
                std.toString());
    }

    @Test
    void testSetErrOutputStream() throws Exception {
        Cli cli = new Cli();

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        cli.setErrOutputStream(err);

        int statusCode = catchSystemExit(() -> cli.run(new String[] { "-s" }));
        assertEquals(1, statusCode);
        assertTrue(err.toString().contains("Error: FILE_OR_DIR is required"));
    }

    @Test
    void testWrongOutputFormat() throws Exception {
        Cli cli = new Cli();

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        cli.setErrOutputStream(err);

        int statusCode = catchSystemExit(() -> cli.run(new String[] { "-f", "foo" }));
        assertEquals(1, statusCode);
        assertTrue(err.toString().contains("Error: invalid output format"));
    }

    @Test
    void testRecursive() throws Exception {
        String dirPath = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "recursive";
        String path1 = dirPath + File.separator + "cli2.yml";
        String path2 = dirPath + File.separator + "sub" + File.separator + "cli3.yaml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        int statusCode = catchSystemExit(() -> cli.run(new String[] { "-f", "parsable", dirPath }));
        assertEquals(0, statusCode);
        assertEquals(
                new HashSet<>(Arrays.asList(path1 + ":2:8:comments:warning:too few spaces before comment",
                        path2 + ":1:1:document-start:warning:missing document start \"---\"")),
                new HashSet<>(Arrays.asList(std.toString().trim().split(System.lineSeparator()))));
    }

    @Test
    void testStrict() throws Exception {
        final String dirPath = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "recursive" + File.separator + "sub";
        final String path = dirPath + File.separator + "cli3.yaml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        int statusCode = catchSystemExit(() -> cli.run(new String[] { "-s", "-f", "parsable", dirPath }));
        assertEquals(2, statusCode);
        assertEquals(
                path + ":1:1:document-start:warning:missing document start \"---\"" + System.lineSeparator(),
                std.toString());
    }

    @Test
    void testNoWarnings() throws Exception {
        final String dirPath = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "recursive" + File.separator + "sub";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        int statusCode = catchSystemExit(() -> cli.run(new String[] { "-s", "-f", "parsable", "--no-warnings", dirPath }));
        assertEquals(0, statusCode);
        assertEquals("", std.toString());
    }

    @Test
    void testWrongConfiguration() throws Exception {
        Cli cli = new Cli();

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        cli.setErrOutputStream(err);

        int statusCode = catchSystemExit(() -> cli.run(new String[] { "-d", "\"foo: bar: error\"", "foo.yml" }));
        assertEquals(1, statusCode);
        assertTrue(err.toString().contains("Error: cannot get or process configuration"));
    }

    @Test
    void testShowHelpShort() throws Exception {
        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        cli.setErrOutputStream(err);

        int statusCode = catchSystemExit(() -> cli.run(new String[] { "-h" }));
        assertEquals(0, statusCode);
        assertTrue(std.toString().contains("A linter for YAML files"));
        assertEquals("", err.toString());
    }

    @Test
    void testShowVersion1() throws Exception {
        testShowVersion("--version");
    }

    @Test
    void testShowVersion2() throws Exception {
        testShowVersion("-v");
    }

    private void testShowVersion(String option) throws Exception {
        Cli cli = new Cli();

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        cli.setErrOutputStream(err);

        // Get version
        Properties props = new Properties();
        props.load(cli.getClass().getClassLoader().getResourceAsStream("yaml.properties"));

        int statusCode = catchSystemExit(() -> cli.run(new String[] { option }));
        assertEquals(0, statusCode);
        assertEquals(Cli.APP_NAME + " " + props.getProperty("version") + System.lineSeparator(), err.toString());
    }

    @Test
    void testMutuallyExcludedOptions() throws Exception {
        Cli cli = new Cli();

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        cli.setErrOutputStream(err);

        int statusCode = catchSystemExit(() -> cli.run(new String[] { "-c", "conf.yaml", "-d", "\"---\"" }));
        assertEquals(1, statusCode);
        assertTrue(err.toString().contains("Error: options `c' and `d' are mutually exclusive."));
    }

    @Test
    void testConfData1() throws Exception {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli5.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        int statusCode = catchSystemExit(() -> cli.run(new String[] { "-d", "relaxed", "-f", "parsable", path }));
        assertEquals(0, statusCode);
        assertEquals(
                path + ":3:3:hyphens:warning:too many spaces after hyphen" + System.lineSeparator(),
                std.toString());
    }

    @Test
    void testConfData2() throws Exception {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli5.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        int statusCode = catchSystemExit(() ->
                cli.run(new String[] { "-d", "\"rules:\n  hyphens:\n    max-spaces-after: 1\"", "-f", "parsable", path }));
        assertEquals(1, statusCode);
        assertEquals(
                path + ":3:3:hyphens:error:too many spaces after hyphen" + System.lineSeparator(),
                std.toString());
    }

    @Test
    void testConfFile() throws Exception {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli5.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        int statusCode = catchSystemExit(() ->
                cli.run(new String[] { "-f", "parsable", "-c", "src" + File.separator + "test" + File.separator + "resources" + File.separator + "config" + File.separator + "XDG" + File.separator + "yamllint" + File.separator + "config", path }));
        assertEquals(0, statusCode);
        assertEquals(
                path + ":2:8:comments:warning:too few spaces before comment" + System.lineSeparator(),
                std.toString());
    }

    @Test
    void testParsableFormat() throws Exception {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli1.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        int statusCode = catchSystemExit(() -> cli.run(new String[] { "-f", "parsable", path }));
        assertEquals(1, statusCode);
        assertEquals(
                new HashSet<>(Arrays.asList(path + ":2:8:comments:warning:too few spaces before comment",
                        path + ":3:16::error:syntax error: mapping values are not allowed here")),
                new HashSet<>(Arrays.asList(std.toString().trim().split(System.lineSeparator()))));
    }

    @Test
    void testGitHubFormat() throws Exception {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli1.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        int statusCode = catchSystemExit(() -> cli.run(new String[] { "-f", "github", path }));
        assertEquals(1, statusCode);
        assertEquals(
                new HashSet<>(Arrays.asList("::warning file=" + path + ",line=2,col=8::[comments] too few spaces before comment",
                        "::error file=" + path + ",line=3,col=16::syntax error: mapping values are not allowed here")),
                new HashSet<>(Arrays.asList(std.toString().trim().split(System.lineSeparator()))));
    }

    @Test
    void testColoredOutput() throws Exception {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli5.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        int statusCode = catchSystemExit(() -> cli.run(new String[]{"-f", "colored", "-d", "relaxed", path}));
        assertEquals(0, statusCode);
        assertEquals(
                Format.ANSI_UNDERLINED + path + Format.ANSI_RESET + System.lineSeparator() +
                        "  " + Format.ANSI_FAINT + "3:3" + Format.ANSI_RESET + "       " + Format.ANSI_YELLOW + "warning" + Format.ANSI_RESET +
                        "  too many spaces after hyphen  " + Format.ANSI_FAINT + "(hyphens)" + Format.ANSI_RESET + System.lineSeparator() + System.lineSeparator(),
                std.toString());
    }

    @Test
    void testGlobalConfig1() throws Exception {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli5.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        int statusCode = catchSystemExit(() ->
                withEnvironmentVariable("XDG_CONFIG_HOME", "src" + File.separator + "test" + File.separator + "resources" + File.separator + "config" + File.separator + "XDG").
                        execute(() -> cli.run(new String[] { "-f", "parsable", path }))
        );
        assertEquals(0, statusCode);
        assertEquals(path + ":2:8:comments:warning:too few spaces before comment" + System.lineSeparator(), std.toString());
    }

    @Test
    void testGlobalConfig2() throws Exception {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli5.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        int statusCode = catchSystemExit(() ->
                withEnvironmentVariable(Cli.XDG_CONFIG_HOME_ENV_VAR, null).and(Cli.YAMLLINT_CONFIG_FILE_ENV_VAR, null).
                        execute(() -> restoreSystemProperties(() -> {
                            System.setProperty("user.home", System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "config" + File.separator + "home");
                            cli.run(new String[]{"-f", "parsable", path});
                        }))
        );
        assertEquals(1, statusCode);
        assertEquals(path + ":3:3:hyphens:error:too many spaces after hyphen" + System.lineSeparator(), std.toString());
    }

    @Test
    void testGlobalConfig3() throws Exception {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli5.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        int statusCode = catchSystemExit(() ->
                withEnvironmentVariable("YAMLLINT_CONFIG_FILE", "src" + File.separator + "test" + File.separator + "resources" + File.separator + "config" + File.separator + "XDG" + File.separator + "yamllint" + File.separator + "config").
                        execute(() -> cli.run(new String[] { "-f", "parsable", path }))
        );
        assertEquals(0, statusCode);
        assertEquals(
                path + ":2:8:comments:warning:too few spaces before comment" + System.lineSeparator(),
                std.toString());
    }

    @Test
    void testLocalConfig1() throws Exception {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli5.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        Files.copy(Paths.get("src", "test", "resources", "config", "local", Cli.USER_CONF_FILENAME), Paths.get(Cli.USER_CONF_FILENAME), StandardCopyOption.REPLACE_EXISTING);
        int statusCode = catchSystemExit(() -> cli.run(new String[] { "-f", "parsable", path }));
        assertEquals(1, statusCode);
        assertEquals(
                path + ":3:3:hyphens:error:too many spaces after hyphen" + System.lineSeparator(), std.toString());
        // Need to restore user.home for the other tests
        Files.delete(Paths.get(Cli.USER_CONF_FILENAME));
    }

    @Test
    void testLocalConfig2() throws Exception {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli5.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        Files.copy(Paths.get("src", "test", "resources", "config", "local", Cli.USER_CONF_FILENAME), Paths.get(Cli.USER_CONF_FILENAME + ".yaml"), StandardCopyOption.REPLACE_EXISTING);
        int statusCode = catchSystemExit(() -> cli.run(new String[] { "-f", "parsable", path }));
        assertEquals(1, statusCode);
        assertEquals(
                path + ":3:3:hyphens:error:too many spaces after hyphen" + System.lineSeparator(), std.toString());
        // Need to restore user.home for the other tests
        Files.delete(Paths.get(Cli.USER_CONF_FILENAME + ".yaml"));
    }

    @Test
    void testLocalConfig3() throws Exception {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli5.yml";

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        Files.copy(Paths.get("src", "test", "resources", "config", "local", Cli.USER_CONF_FILENAME), Paths.get(Cli.USER_CONF_FILENAME + ".yml"), StandardCopyOption.REPLACE_EXISTING);
        int statusCode = catchSystemExit(() -> cli.run(new String[] { "-f", "parsable", path }));
        assertEquals(1, statusCode);
        assertEquals(
                path + ":3:3:hyphens:error:too many spaces after hyphen" + System.lineSeparator(), std.toString());
        // Need to restore user.home for the other tests
        Files.delete(Paths.get(Cli.USER_CONF_FILENAME + ".yml"));
    }

    @Test
    void testListFiles() throws Exception {
        String path = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "recursive" + File.separator;

        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        int statusCode = catchSystemExit(() ->
                cli.run(new String[] { "--list-files", "-d", "{ignore: \".*" + File.separator + "cli4.yml\"}", path }));
        assertEquals(0, statusCode);
        assertEquals(
                new HashSet<>(Arrays.asList(path + "cli2.yml",
                        path + "sub" + File.separator + "cli3.yaml")),
                new HashSet<>(Arrays.asList(std.toString().trim().split(System.lineSeparator()))));
    }
}
