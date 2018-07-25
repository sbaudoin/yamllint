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

/**
 * Exception thrown when a configuration error is met
 */
public class YamlLintConfigException extends Exception {
    /**
     * Default constructor
     */
    public YamlLintConfigException() {
        super();
    }

    /**
     * Constructor
     *
     * @param message a message
     */
    public YamlLintConfigException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param message a message
     * @param throwable a throwable that caused this exception to be thrown
     */
    public YamlLintConfigException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
