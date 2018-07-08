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

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)][license]
[![Maven Central](https://img.shields.io/maven-central/v/com.github.sbaudoin/yamllint.svg?label=Maven%20Central)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.sbaudoin%22%20AND%20a%3A%22yamlllint%22)
[![Build Status](https://travis-ci.org/sbaudoin/yamllint.svg?branch=master)](https://travis-ci.org/sbaudoin/yamllint)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.sbaudoin:yamllint&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.github.sbaudoin:yamllint)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.sbaudoin:yamllint&metric=coverage)](https://sonarcloud.io/dashboard?id=com.github.sbaudoin:yamllint)

YAML lint written in Java.
Its main purpose is to provide an API and scripts to analyze YAML documents.
The YAML documents are syntactically checked as well as against rules. To get the list of rules, please refer to the classes
of the [`com.github.sbaudoin.yamllint.rules`](src/main/java/com/github/sbaudoin/yamllint/rules) package. Among
other we have a rule to check the presence of the start and end YAML document marker, the correct and consistent indentation, etc.

## API usage
Please refer to the JavaDoc.

The class that will mostly interest you is `com.github.sbaudoin.yamllint.Linter`: it contains static methods
that can be used to analyze a YAML string or a file.

3 errors levels have been defined: info, warning and error.

The linter can return only one syntax error per file (once a syntax error has been met we cannot expect a lot from the rest
of the file with respect to the syntax). It is returned apart, not as part of the so called "cosmetic errors", that represent
all other errors checked with specific rules.

## Batch usage
Get the `.zip` or `.tar.gz` distribution archive, unpack and execute the script corresponding to your platform in the `bin` directory:

    bin/yamllint [options] files/directories

or

    bin\yamllint [options] files/directories

Use the `--help` (or `-h`) option to get help. By default, if the terminal supports it, the output is colorized and has
the following output format:

    file.yml
      line:column       level  message  (ruleId)
      ...

You can specify the `-f parsable` on the command line to get a parsable output as follows:

    file.yml:line:column:ruleId:level:message
