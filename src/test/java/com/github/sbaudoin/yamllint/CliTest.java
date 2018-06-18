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

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class CliTest {
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void testSetStdOutputStream() {
        Cli cli = new Cli();

        ByteArrayOutputStream std = new ByteArrayOutputStream();
        cli.setStdOutputStream(std);

        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> assertTrue(std.toString().contains("2:16      error    syntax error: mapping values are not allowed here")));
        cli.run(new String[] { "src" + File.separator + "test" + File.separator + "resources" + File.separator + "cli1.yml" });
    }

    @Test
    public void testSetErrOutputStream() {
        Cli cli = new Cli();

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        cli.setErrOutputStream(err);

        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> assertTrue(err.toString().contains("A linter for YAML files")));
        cli.run(new String[] { "--help" });
    }

    @Test
    public void testEndOnError() {
        Cli cli = new Cli();

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        cli.setErrOutputStream(err);

        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> assertTrue(err.toString().contains("Error: invalid output format")));
        cli.run(new String[] { "-f foo" });
    }

    @Test
    public void testShowHelpShort() {
        Cli cli = new Cli();

        ByteArrayOutputStream err = new ByteArrayOutputStream();
        cli.setErrOutputStream(err);

        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(() -> assertTrue(err.toString().contains("A linter for YAML files")));
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

        exit.expectSystemExitWithStatus(1);
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


    public void testRunWithIgnoredPath() {
    }

    /*
    class IgnorePathConfigTestCase(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        super(IgnorePathConfigTestCase, cls).setUpClass()

        bad_yaml = ('---\n'
                    '- key: val1\n'
                    '  key: val2\n'
                    '- trailing space \n'
                    '-    lonely hyphen\n')

        cls.wd = build_temp_workspace({
            'bin/file.lint-me-anyway.yaml': bad_yaml,
            'bin/file.yaml': bad_yaml,
            'file-at-root.yaml': bad_yaml,
            'file.dont-lint-me.yaml': bad_yaml,
            'ign-dup/file.yaml': bad_yaml,
            'ign-dup/sub/dir/file.yaml': bad_yaml,
            'ign-trail/file.yaml': bad_yaml,
            'include/ign-dup/sub/dir/file.yaml': bad_yaml,
            's/s/ign-trail/file.yaml': bad_yaml,
            's/s/ign-trail/s/s/file.yaml': bad_yaml,
            's/s/ign-trail/s/s/file2.lint-me-anyway.yaml': bad_yaml,

            '.yamllint': 'ignore: |\n'
                         '  *.dont-lint-me.yaml\n'
                         '  /bin/\n'
                         '  !/bin/*.lint-me-anyway.yaml\n'
                         '\n'
                         'extends: default\n'
                         '\n'
                         'rules:\n'
                         '  key-duplicates:\n'
                         '    ignore: |\n'
                         '      /ign-dup\n'
                         '  trailing-spaces:\n'
                         '    ignore: |\n'
                         '      ign-trail\n'
                         '      !*.lint-me-anyway.yaml\n',
        })

        cls.backup_wd = os.getcwd()
        os.chdir(cls.wd)

    @classmethod
    def tearDownClass(cls):
        super(IgnorePathConfigTestCase, cls).tearDownClass()

        os.chdir(cls.backup_wd)

        shutil.rmtree(cls.wd)

    @unittest.skipIf(sys.version_info < (2, 7), 'Python 2.6 not supported')
    def test_run_with_ignored_path(self):
        sys.stdout = StringIO()
        with self.assertRaises(SystemExit):
            cli.run(('-f', 'parsable', '.'))

        out = sys.stdout.getvalue()
        out = '\n'.join(sorted(out.splitlines()))

        keydup = '[error] duplication of key "key" in mapping (key-duplicates)'
        trailing = '[error] trailing spaces (trailing-spaces)'
        hyphen = '[error] too many spaces after hyphen (hyphens)'

        self.assertEqual(out, '\n'.join((
            './bin/file.lint-me-anyway.yaml:3:3: ' + keydup,
            './bin/file.lint-me-anyway.yaml:4:17: ' + trailing,
            './bin/file.lint-me-anyway.yaml:5:5: ' + hyphen,
            './file-at-root.yaml:3:3: ' + keydup,
            './file-at-root.yaml:4:17: ' + trailing,
            './file-at-root.yaml:5:5: ' + hyphen,
            './ign-dup/file.yaml:4:17: ' + trailing,
            './ign-dup/file.yaml:5:5: ' + hyphen,
            './ign-dup/sub/dir/file.yaml:4:17: ' + trailing,
            './ign-dup/sub/dir/file.yaml:5:5: ' + hyphen,
            './ign-trail/file.yaml:3:3: ' + keydup,
            './ign-trail/file.yaml:5:5: ' + hyphen,
            './include/ign-dup/sub/dir/file.yaml:3:3: ' + keydup,
            './include/ign-dup/sub/dir/file.yaml:4:17: ' + trailing,
            './include/ign-dup/sub/dir/file.yaml:5:5: ' + hyphen,
            './s/s/ign-trail/file.yaml:3:3: ' + keydup,
            './s/s/ign-trail/file.yaml:5:5: ' + hyphen,
            './s/s/ign-trail/s/s/file.yaml:3:3: ' + keydup,
            './s/s/ign-trail/s/s/file.yaml:5:5: ' + hyphen,
            './s/s/ign-trail/s/s/file2.lint-me-anyway.yaml:3:3: ' + keydup,
            './s/s/ign-trail/s/s/file2.lint-me-anyway.yaml:4:17: ' + trailing,
            './s/s/ign-trail/s/s/file2.lint-me-anyway.yaml:5:5: ' + hyphen,
)))
     */
}
