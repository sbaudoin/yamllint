<!---
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->
YAML Lint
=========

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.sbaudoin/yamllint.svg?label=Maven%20Central)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.sbaudoin%22%20AND%20a%3A%22yamllint%22)
[![Build Status](https://travis-ci.org/sbaudoin/yamllint.svg?branch=master)](https://travis-ci.org/sbaudoin/yamllint)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.sbaudoin:yamllint&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.github.sbaudoin:yamllint)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.sbaudoin:yamllint&metric=coverage)](https://sonarcloud.io/dashboard?id=com.github.sbaudoin:yamllint)
[![javadoc](https://javadoc.io/badge2/com.github.sbaudoin/yamllint/javadoc.svg)](https://javadoc.io/doc/com.github.sbaudoin/yamllint) 

YAML lint written in Java.
Its main purpose is to provide an API and scripts to analyze YAML documents.
The YAML documents are syntactically checked as well as against rules. To get the list of rules, please refer to the classes
of the [`com.github.sbaudoin.yamllint.rules`](src/main/java/com/github/sbaudoin/yamllint/rules) package. Among
other, we have a rule to check the presence of the start and end YAML document marker, the correct and consistent indentation, etc.

## API usage

Maven dependency:

    <dependency>
        <groupId>com.github.sbaudoin</groupId>
        <artifactId>yamllint</artifactId>
        <version>1.4.0</version>
    </dependency>

For use, please refer to the [JavaDoc](https://javadoc.io/doc/com.github.sbaudoin/yamllint/latest/index.html).

The class that will mostly interest you is `com.github.sbaudoin.yamllint.Linter`: it contains static methods
that can be used to analyze a YAML string or a file.

3 errors levels have been defined: info, warning and error.

The linter can return only one syntax error per file (once a syntax error has been met we cannot expect a lot from the rest
of the file with respect to the syntax). It is returned apart, not as part of the so called "cosmetic errors", that represent
all other errors checked with specific rules.

## Configuration

YAML lint uses [rules](src/main/java/com/github/sbaudoin/yamllint/rules) to check the YAML files. These rules have default
settings that can changed or overridden with, whether an instance of `com.github.sbaudoin.yamllint.YamlLintConfig` or a YAML
configuration file if using the batch tool (see below). The expected format for this YAML configuration file is as follows:

    ---
    yaml-files:
      - '.*\.yaml'
      - '.*\.yml'
    
    ignore: pathspecs
    
    rules:
      <rule name>: enable|disable
        level: info|warning|error
        ignore: pathspecs
        rule_conf_param1: value
        rule_conf_param2: value
        ...
      ...

Some details:

- The `yaml-files` block is **optional** and allows you to specify a list of regexp file name patterns used to identify YAML files.
  The default configuration is to take only the `.yml` and `.yaml` files into account.
- The `ignore` parameter is also **optional** and allows you do the contrary, i.e. specify regexp patterns to be used to
  ignore files. It can be defined globally or per rule. Be aware that it is a string, not a list, that contains an expression
  per line:
  
      ignore: |
        .*\.txt$
        foo.bar

- The rule `level` is also **optional** (the default rule level is "error").
- See [the default configuration](src/main/java/resources/conf/default.yaml) to get the default rules' parameter values.

## Batch usage

Get the `.zip` or `.tar.gz` distribution archive, unpack and execute the script corresponding to your platform in the `bin` directory:

    bin/yamllint [options] files/directories

or for Windows:

    bin\yamllint [options] files/directories

or even like this to lint from the standard input:

    a command | bin/yamllint [options]

**Warning!** The Unix version requires Bash. There is no guaranty that the script will work with other Shell interpreters.

Use the `--help` (or `-h`) option to get help with the complete list of options and values.

By default, if the terminal supports it, the output is colorized and has the following output format:

    file.yml
      line:column       level  message  (ruleId)
      ...

You can force color with the `-f colored` format; you can force the non-colorized output with `-f standard`.

You can specify the `-f parsable` on the command line to get a parsable output as follows:

    <file path>:<line>:<column>:<ruleId>:<level>:<message>

The `-f github` format also provides a parsable output as follows:

    ::<level> file=<file path>,line=<line>,col=<col>::<ruleId><message>

The YAML lint configuration file can be passed in different ways:

- Use the `-d` option to specify a YAML configuration directly on the command line or specify the "relaxed" configuration;
- Use the `-c` option to specify a path to a configuration file;
- If `-c` is not provided, yamllint will look for a configuration file in the following locations (by order of preference):
  - `.yamllint`, `.yamllint.yaml` or `.yamllint.yml` in the current working directory
  - the file referenced by `YAMLLINT_CONFIG_FILE` environment variable if set
  - `$XDG_CONFIG_HOME/yamllint/config` if the `XDG_CONFIG_HOME` environment variable is set
  - `~/.config/yamllint/config` if this file exists
- Finally if no config file is found, the default configuration is applied.

As a reminder, the default configuration can be found [here](src/main/java/resources/conf/default.yaml) and the relaxed
version [here](src/main/java/resources/conf/relaxed.yaml).
