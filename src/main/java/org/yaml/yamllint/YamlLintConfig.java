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
package org.yaml.yamllint;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.yamllint.rules.Rule;
import org.yaml.yamllint.rules.RuleFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class YamlLintConfig {
    public static final String IGNORE_KEY = "ignore";


    // Compared to Python yamllint, for better semantic we store the rules' configurations in ruleConf
    // instead of an attribute named 'rule'
    protected Map<String, Object> ruleConf;
    // List of regexp patterns used to tell if a file is to be ignored by the rule or not
    protected List<String> ignore = null;


    /**
     * Constructs a <code>YamlLintConfig</code> from a YAML string
     *
     * @param content the configuration as a YAML string
     * @throws IOException if an error occurs handling the passed file
     * @throws YamlLintConfigException if the configuration contains an error so that its content cannot be successfully parsed
     * @throws IllegalArgumentException if <var>content</var> is <code>null</code>
     */
    public YamlLintConfig(String content) throws IOException, YamlLintConfigException {
        if (content != null) {
            parse(content);
            validate();
        } else {
            throw new IllegalArgumentException("content cannot be null");
        }
    }

    /**
     * Constructs a <code>YamlLintConfig</code> from a YAML file
     *
     * @param file a YAML configuration file identified by a URL
     * @throws IOException if an error occurs handling the passed file
     * @throws YamlLintConfigException if the configuration contains an error so that its content cannot be successfully parsed
     * @throws IllegalArgumentException if <var>file</var> is <code>null</code>
     */
    public YamlLintConfig(URL file) throws IOException, YamlLintConfigException {
        if (file != null) {
            try (Scanner scanner = new Scanner(file.openStream()).useDelimiter("\\A")) {
                parse(scanner.next());
            }
            validate();
        } else {
            throw new IllegalArgumentException("content cannot be null");
        }
    }


    /**
     * Tells if a file identified by its path is to be ignored by this tool
     *
     * @param filepath the path of the file to be checked by this tool
     * @return <code>true</code> if the file must be ignored, <code>false</code> otherwise
     */
    public boolean isFileIgnored(String filepath) {
        return ignore != null && ignore.stream().anyMatch(filepath::matches);
    }

    /**
     * Returns the rules to be checked for the passed file
     *
     * @param file the file to be checked
     * @return the list of rules to be checked for the file. All rules are returned if <var>file</var> is <code>null</code>.
     */
    public List<Rule> getEnabledRules(File file) {
        List<Rule> rules = new ArrayList<>();
        for (Map.Entry<String, Object> entry : ruleConf.entrySet()) {
            Rule rule = RuleFactory.instance.getRule(entry.getKey());
            if (rule != null && entry.getValue() != null && (file == null || !rule.ignores(file))) {
                rules.add(rule);
            }
        }
        return rules;
    }

    /**
     * Returns the configuration for the rule identified by its ID
     *
     * @param id a rule ID
     * @return a configuration map or <code>null</code> if not found
     */
    public Object getRuleConf(String id) {
        return ruleConf.get(id);
    }

    /**
     * Updates the <var>ruleConf</var> attribute of this configuration instance with the one of the passed configuration. Existing entries are replaced (overridden).
     *
     * @param baseConfig a configuration that will extend this instance's rule configuration
     */
    public void extend(YamlLintConfig baseConfig) {
        assert ruleConf != null;

        Map<String, Object> newConf = new HashMap<>(baseConfig.ruleConf);

        for (Map.Entry<String, Object> entry : ruleConf.entrySet()) {
            String ruleId = entry.getKey();
            Object conf = entry.getValue();
            if (conf instanceof Map && newConf.get(ruleId) != null) {
                deepMerge((Map)newConf.get(ruleId), (Map)conf);
            } else {
                newConf.put(ruleId, conf);
            }
        }

        ruleConf = newConf;

        if (baseConfig.ignore != null) {
            ignore = baseConfig.ignore;
        }
    }


    /**
     * Parses a passed YAML configuration for this tool and updates <var>ruleConf</var>
     *
     * @param rawContent a YAML linter configuration
     * @throws YamlLintConfigException if a parse error occurs
     * @throws IOException if the passed configuration extends a standard configuration and that there is a problem reading
     *                     this standard configuration file
     */
    protected void parse(String rawContent) throws IOException, YamlLintConfigException {
        Map conf;

        try {
            conf = new Yaml().load(rawContent);
        } catch (YAMLException|ClassCastException e) {
            throw new YamlLintConfigException("invalid YAML config: " + e.getMessage());
        }
        if (conf == null) {
            throw new YamlLintConfigException("invalid config: not a dictionary");
        }

        // ruleConf stores YAML conf; rules stores actual rules
        ruleConf = (Map)conf.getOrDefault("rules", new HashMap());

        // Does this conf override another conf that we need to load?
        if (conf.containsKey("extends")) {
            YamlLintConfig base = new YamlLintConfig(getExtendedConfigFile((String)conf.get("extends")));
            try {
                extend(base);
            } catch (Exception e) {
                throw new YamlLintConfigException("invalid config: " + e.getMessage());
            }
        }

        if (conf.containsKey(IGNORE_KEY)) {
            if (!(conf.get(IGNORE_KEY) instanceof String)) {
                throw new YamlLintConfigException("invalid config: 'ignore' should contain file patterns");
            }
            ignore = Arrays.asList(((String)conf.get(IGNORE_KEY)).split("\\r?\\n"));
        }
    }

    /**
     * Validates the rule configuration and instantiates the associated executable rules in <var>rules</var>
     *
     * @throws YamlLintConfigException if a mismatch exists between the configured rules and the rules contained in this package
     */
    protected void validate() throws YamlLintConfigException {
        for (Map.Entry<String, Object> entry : ruleConf.entrySet()) {
            String id = entry.getKey();
            Rule rule = RuleFactory.instance.getRule(id);
            if (rule == null) {
                throw new YamlLintConfigException("invalid config: no such rule: \"" + id + "\"");
            }

            Map<String, Object> newConf = validateRuleConf(rule, entry.getValue());
            ruleConf.put(id, newConf);
        }
    }

    /**
     * Validates a rule against a given configuration. The rule might be updated by this method.
     *
     * @param rule the rule to be validated against the passed configuration. Must not be <code>null</code>.
     * @param conf the YAML configuration of the rule
     * @return the possibly updated YAML configuration if the rule has been validated or <code>null</code> if the rule is disabled by configuration
     * @throws YamlLintConfigException if <var>conf</var> contains invalid configuration
     */
    @SuppressWarnings("unchecked")
    protected static Map<String, Object> validateRuleConf(Rule rule, Object conf) throws YamlLintConfigException {
        if (conf == null || "disable".equals(conf)) {
            return null;
        } else if ("enable".equals(conf)) {
            // Ugly but this is a shorthand for the next test
            conf = new HashMap<>();
        }

        if (conf instanceof Map) {
            Map<String, Object> mapConf = (Map)conf;

            // Deal with 'ignore' conf
            if (mapConf.containsKey(IGNORE_KEY)) {
                if (mapConf.get(IGNORE_KEY) instanceof List) {
                    rule.setIgnore((List)mapConf.get(IGNORE_KEY));
                } else if (!(mapConf.get(IGNORE_KEY) instanceof String)) {
                    throw new YamlLintConfigException("invalid config: ignore should contain regexp patterns");
                } else {
                    rule.setIgnore(Arrays.asList(((String) mapConf.get(IGNORE_KEY)).split("\\r?\\n")));
                }
            }

            // Deal with 'level' conf
            if (!mapConf.containsKey(Linter.LEVEL_KEY)) {
                rule.setLevel(Linter.ERROR_LEVEL);
                mapConf.put(Linter.LEVEL_KEY, Linter.ERROR_LEVEL);
            } else if (mapConf.containsKey(Linter.LEVEL_KEY) &&
                    (Linter.ERROR_LEVEL.equals(mapConf.get(Linter.LEVEL_KEY)) || Linter.WARNING_LEVEL.equals(mapConf.get(Linter.LEVEL_KEY)) || Linter.INFO_LEVEL.equals(mapConf.get(Linter.LEVEL_KEY)))) {
                rule.setLevel((String)mapConf.get(Linter.LEVEL_KEY));
            } else {
                throw new YamlLintConfigException("invalid config: level should be \"" + Linter.ERROR_LEVEL + "\", \"" + Linter.WARNING_LEVEL + "\" or \"" + Linter.INFO_LEVEL + "\"");
            }

            Map<String, Object> options = rule.getOptions();
            for (Map.Entry<String, Object> entry : mapConf.entrySet()) {
                String optkey = entry.getKey();

                if (IGNORE_KEY.equals(optkey) || Linter.LEVEL_KEY.equals(optkey)) {
                    continue;
                }
                if (!options.keySet().contains(optkey)) {
                    throw new YamlLintConfigException("invalid config: unknown option \"" + optkey + "\" for rule \"" + rule.getId() + "\"");
                }
                if (options.get(optkey) instanceof List) {
                    if (!((List)options.get(optkey)).contains(mapConf.get(optkey)) && ((List)options.get(optkey)).stream().noneMatch(object -> entry.getValue().getClass().equals(object))) {
                        throw new YamlLintConfigException("invalid config: option \"" + optkey + "\" of \"" + rule.getId() + "\" should be in " + getListRepresentation((List)options.get(optkey)));
                    }
                } else {
                    if (!mapConf.get(optkey).getClass().equals(options.get(optkey))) {
                        throw new YamlLintConfigException("invalid config: option \"" + optkey + "\" of \"" + rule.getId() + "\" should be of type " + options.get(optkey).getClass().getSimpleName().toLowerCase());
                    }
                }
                rule.addParameter(optkey, mapConf.get(optkey));
            }
            for (String optkey : options.keySet()) {
                if (!mapConf.containsKey(optkey)) {
                    throw new YamlLintConfigException("invalid config: missing option \"" + optkey + "\" for rule \"" + rule.getId() + "\"");
                }
            }

            return mapConf;
        } else {
            throw new YamlLintConfigException("invalid config: rule \"" + rule.getId() + "\": should be either \"enable\", \"disable\" or a dictionary");
        }
    }

    /**
     * Returns a <code>URL</code> pointing to the passed configuration file: local file system file or bundled configuration
     * file identified by its name (without the ".yaml" extension)
     *
     * @param name the file name
     * @return a <code>URL</code> to this file
     * @throws IllegalArgumentException if an error occurs handling the passed file name
     */
    protected URL getExtendedConfigFile(String name) {
        // Is it a standard conf shipped with yamllint...
        if (!name.contains(File.pathSeparator)) {
            URL url = getClass().getClassLoader().getResource("conf/" + name + ".yaml");

            if (url == null) {
                throw new IllegalArgumentException("Bundled configuration file \"" + name + "\" not found");
            }
            return url;
        }

        // or a custom conf on filesystem?
        try {
            return new File(name).toURI().toURL();
        } catch (MalformedURLException e) {
            // Should never happen...
            throw new IllegalArgumentException("Cannot create URL for the configuration file \"" + name + "\"", e);
        }
    }

    /**
     * Returns a representation of the passed list of objects (some object types have a specific representation, the default
     * being <code>Object.toString()</code> or <code>null</code> if applicable.
     *
     * @param list a list of object
     * @return a representation of the list and its objects
     */
    protected static String getListRepresentation(List list) {
        assert list != null;

        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object o : list) {
            if (!first) {
                sb.append(", ");
            } else {
                first = false;
            }

            if (o instanceof String) {
                sb.append("'").append(o).append("'");
            } else if (o instanceof Class) {
                sb.append(((Class)o).getSimpleName().toLowerCase());
            } else {
                sb.append(o);
            }
        }
        return sb.append("]").toString();
    }

    /**
     * Deeply merges 2 maps together (<var>newMap</var> is merged into <var>original</var>)
     *
     * @param original a map to be extended by <var>newMap</var>
     * @param newMap a map to be merged into <var>original</var>
     * @return the <var>original</var> map
     */
    protected static Map<Object, Object> deepMerge(Map<Object, Object> original, Map<Object, Object> newMap) {
        for (Map.Entry entry : newMap.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key instanceof Map && original.get(key) instanceof Map) {
                original.put(key, deepMerge((Map) original.get(key), (Map) value));
            } else if (key instanceof List && original.get(key) instanceof List) {
                List originalChild = (List) original.get(key);
                for (Object each : (List) value) {
                    if (!originalChild.contains(each)) {
                        originalChild.add(each);
                    }
                }
            } else {
                original.put(key, value);
            }
        }
        return original;
    }
}
