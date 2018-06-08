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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;

@RunWith(Parameterized.class)
public class SpecExamplesTest {
    private static final List<String> CONF_GENERAL = Arrays.asList(
            "document-start: disable\n",
            "comments: {min-spaces-from-content: 1}\n",
            "braces: {min-spaces-inside: 1, max-spaces-inside: 1}\n",
            "brackets: {min-spaces-inside: 1, max-spaces-inside: 1}\n");

    private static final Map<String, List<String>> CONF_OVERRIDES = new HashMap<String, List<String>>() {
        {
            put("example-2.2", Arrays.asList("colons: {max-spaces-after: 2}\n"));
            put("example-2.4", Arrays.asList("colons: {max-spaces-after: 3}\n"));
            put("example-2.5", Arrays.asList(
                    "empty-lines: {max-end: 2}\n",
                    "brackets: {min-spaces-inside: 0, max-spaces-inside: 2}\n",
                    "commas: {max-spaces-before: -1}\n"));
            put("example-2.6", Arrays.asList(
                    "braces: {min-spaces-inside: 0, max-spaces-inside: 0}\n",
                    "indentation: disable\n"));
            put("example-2.12", Arrays.asList(
                    "empty-lines: {max-end: 1}\n",
                    "colons: {max-spaces-before: -1}\n"));
            put("example-2.16", Arrays.asList("empty-lines: {max-end: 1}\n"));
            put("example-2.18", Arrays.asList("empty-lines: {max-end: 1}\n"));
            put("example-2.19", Arrays.asList("empty-lines: {max-end: 1}\n"));
            put("example-2.28", Arrays.asList("empty-lines: {max-end: 3}\n"));
            put("example-5.3", Arrays.asList(
                    "indentation: {indent-sequences: false}\n",
                    "colons: {max-spaces-before: 1}\n"));
            put("example-6.4", Arrays.asList("trailing-spaces: disable\n"));
            put("example-6.5", Arrays.asList("trailing-spaces: disable\n"));
            put("example-6.6", Arrays.asList("trailing-spaces: disable\n"));
            put("example-6.7", Arrays.asList("trailing-spaces: disable\n"));
            put("example-6.8", Arrays.asList("trailing-spaces: disable\n"));
            put("example-6.10", Arrays.asList(
                    "empty-lines: {max-end: 2}\n",
                    "trailing-spaces: disable\n",
                    "comments-indentation: disable\n"));
            put("example-6.11", Arrays.asList(
                    "empty-lines: {max-end: 1}\n",
                    "comments-indentation: disable\n"));
            put("example-6.13", Arrays.asList("comments-indentation: disable\n"));
            put("example-6.14", Arrays.asList("comments-indentation: disable\n"));
            put("example-6.23", Arrays.asList("colons: {max-spaces-before: 1}\n"));
            put("example-7.4", Arrays.asList(
                    "colons: {max-spaces-before: 1}\n",
                    "indentation: disable\n"));
            put("example-7.5", Arrays.asList("trailing-spaces: disable\n"));
            put("example-7.6", Arrays.asList("trailing-spaces: disable\n"));
            put("example-7.7", Arrays.asList("indentation: disable\n"));
            put("example-7.8", Arrays.asList(
                    "colons: {max-spaces-before: 1}\n",
                    "indentation: disable\n"));
            put("example-7.9", Arrays.asList("trailing-spaces: disable\n"));
            put("example-7.11", Arrays.asList(
                    "colons: {max-spaces-before: 1}\n",
                    "indentation: disable\n"));
            put("example-7.13", Arrays.asList(
                    "brackets: {min-spaces-inside: 0, max-spaces-inside: 1}\n",
                   "commas: {max-spaces-before: 1, min-spaces-after: 0}\n"));
            put("example-7.14", Arrays.asList("indentation: disable\n"));
            put("example-7.15", Arrays.asList(
                    "braces: {min-spaces-inside: 0, max-spaces-inside: 1}\n",
                    "commas: {max-spaces-before: 1, min-spaces-after: 0}\n",
                    "colons: {max-spaces-before: 1}\n"));
            put("example-7.16", Arrays.asList("indentation: disable\n"));
            put("example-7.17", Arrays.asList("indentation: disable\n"));
            put("example-7.18", Arrays.asList("indentation: disable\n"));
            put("example-7.19", Arrays.asList("indentation: disable\n"));
            put("example-7.20", Arrays.asList(
                    "colons: {max-spaces-before: 1}\n",
                    "indentation: disable\n"));
            put("example-8.1", Arrays.asList("empty-lines: {max-end: 1}\n"));
            put("example-8.2", Arrays.asList("trailing-spaces: disable\n"));
            put("example-8.5", Arrays.asList(
                    "comments-indentation: disable\n",
                    "trailing-spaces: disable\n"));
            put("example-8.6", Arrays.asList("empty-lines: {max-end: 1}\n"));
            put("example-8.7", Arrays.asList("empty-lines: {max-end: 1}\n"));
            put("example-8.8", Arrays.asList("trailing-spaces: disable\n"));
            put("example-8.9", Arrays.asList("empty-lines: {max-end: 1}\n"));
            put("example-8.14", Arrays.asList("colons: {max-spaces-before: 1}\n"));
            put("example-8.16", Arrays.asList("indentation: {spaces: 1}\n"));
            put("example-8.17", Arrays.asList("indentation: disable\n"));
            put("example-8.20", Arrays.asList(
                    "indentation: {indent-sequences: false}\n",
                    "colons: {max-spaces-before: 1}\n"));
            put("example-8.22", Arrays.asList("indentation: disable\n"));
            put("example-10.1", Arrays.asList("colons: {max-spaces-before: 2}\n"));
            put("example-10.2", Arrays.asList("indentation: {indent-sequences: false}\n"));
            put("example-10.8", Arrays.asList("truthy: disable\n"));
            put("example-10.9", Arrays.asList("truthy: disable\n"));
        }
    };

    // The following tests are blacklisted (i.e. will not be checked against
    // yamllint), because pyyaml is currently not able to parse the contents
    // (using yaml.parse()).
    private static final List<String> SNAKEYAML_BLACKLIST = Arrays.asList(
//            "example-2.11",
            "example-2.23",
            "example-2.24",
            "example-2.27",
            "example-5.10",
            "example-5.12",
            "example-5.13",
            "example-5.14",
            "example-5.6",
            "example-6.1",
            "example-6.12",
            "example-6.15",
            "example-6.17",
            "example-6.18",
            "example-6.19",
            "example-6.2",
            "example-6.20",
            "example-6.21",
            "example-6.22",
            "example-6.24",
            "example-6.25",
            "example-6.26",
            "example-6.27",
            "example-6.3",
            "example-7.1",
            "example-7.10",
            "example-7.12",
            "example-7.17",
            "example-7.2",
            "example-7.21",
            "example-7.22",
            "example-7.3",
            "example-8.18",
            "example-8.19",
            "example-8.21",
            "example-8.3",
            "example-9.3",
            "example-9.4",
            "example-9.5"
    );


    private File file;
    private String filename;


    public SpecExamplesTest(File file) {
        this.file = file;
        this.filename = file.toPath().getFileName().toString();
    }

    @Parameterized.Parameters
    public static Collection<File> getFilenames() {
        return Arrays.asList(Paths.get("src", "test", "resources", "yaml-1.2-spec-examples").toFile().listFiles())
                .stream().filter(f -> !SNAKEYAML_BLACKLIST.contains(f.toPath().getFileName().toString()))
                .collect(Collectors.toList());
    }

    @Test
    public void testSpecExample() throws IOException, YamlLintConfigException {
        assertEquals(0, Linter.run(getConf(), file).size());
    }


    private YamlLintConfig getConf() throws IOException, YamlLintConfigException {
        StringBuilder sb = new StringBuilder("---\nextends: default\nrules:\n");

        List<String> conf = new ArrayList<>(CONF_GENERAL);
        if (CONF_OVERRIDES.containsKey(filename)) {
            conf.addAll(CONF_OVERRIDES.get(filename));
        }
        conf.forEach(l -> sb.append("  ").append(l));

        return new YamlLintConfig(sb.toString());
    }
}
