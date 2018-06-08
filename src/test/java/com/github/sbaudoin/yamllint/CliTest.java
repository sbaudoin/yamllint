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

import junit.framework.TestCase;

public class CliTest extends TestCase {
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
