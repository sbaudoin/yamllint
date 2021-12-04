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
package com.github.sbaudoin.yamllint.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

/**
 * Factory that will instantiate the rules of this package. Rules are cached in order to save memory.
 */
public class RuleFactory {
    private static final Logger LOGGER = Logger.getLogger(RuleFactory.class.getName());

    /**
     * The instance that holds this singleton
     */
    public static final RuleFactory instance = new RuleFactory();


    private final Map<String, Rule> rules = new HashMap<>();


    /**
     * Returns the list of rules registered in this package
     *
     * @param id the ID of the rule to be returned
     * @return the rule corresponding to the passed ID or <code>null</code> if not found
     */
    public Rule getRule(String id) {
        if (rules.containsKey(id)) {
            return rules.get(id);
        }

        try {
            ServiceLoader<Rule> loader = ServiceLoader.load(Rule.class, getClass().getClassLoader());
            Optional<Rule> found = StreamSupport.stream(loader.spliterator(), false)
                .filter(rule -> id.equals(rule.getId()))
                .findFirst();
            if (found.isPresent()) {
                Rule rule = found.get();
                rules.put(rule.getId(), rule);
                return rule;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unexpected error loading Rule instances", e);
        }

        return null;
    }


    /**
     * Hide default constructor
     */
    private RuleFactory() {
    }
}
