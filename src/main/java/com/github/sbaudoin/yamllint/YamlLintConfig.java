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

import org.apache.commons.io.input.CharSequenceReader;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import com.github.sbaudoin.yamllint.rules.Rule;
import com.github.sbaudoin.yamllint.rules.RuleFactory;
import org.yaml.snakeyaml.reader.UnicodeReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Class that represents the configuration for the YAML linter
 */
public class YamlLintConfig {
    /**
     * Configuration parameter that base extension point
     */
    public static final String EXTENDS_KEY = "extends";

    /**
     * Configuration parameter that lists patterns used by the linter to be identify YAML files
     */
    public static final String YAML_FILES_KEY = "yaml-files";

    /**
     * Configuration parameter that lists file patterns to be ignored by the linter
     */
    public static final String IGNORE_KEY = "ignore";

    /**
     * Configuration parameter that lists the rules checked by the linter
     */
    public static final String RULES_KEY = "rules";


    // Compared to Python yamllint, for better semantic we store the rules' configurations in ruleConf
    // instead of an attribute named 'rule', which can be misleading
    /**
     * Holder for the rules' configurations. Key: ruleId; value: rule configuration as a map
     */
    protected Map<String, Object> ruleConf;

    /**
     * List of regexp patterns used to tell if a file is to be ignored or not
     */
    protected List<String> ignore = null;

    /**
     * List of regexp patterns used to identify YAML files, defaulted to .yaml and .yml
     */
    protected List<String> yamlFiles = Arrays.asList(".*\\.yaml$", ".*\\.yml$");


    /**
     * Constructs a <code>YamlLintConfig</code> from a YAML string
     *
     * @param content the configuration as a YAML string
     * @throws YamlLintConfigException if the configuration contains an error so that its content cannot be successfully parsed
     * @throws IllegalArgumentException if <var>content</var> is <code>null</code>
     */
    public YamlLintConfig(CharSequence content) throws YamlLintConfigException {
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
            throw new IllegalArgumentException("file cannot be null");
        }
    }

    /**
     * Constructs a <code>YamlLintConfig</code> from an input stream
     *
     * @param in an {@code InputStream} that will supply YAML content. Be aware that this {@code InputStream} is not
     *           closed by this method, you will have to do it yourself later.
     * @throws IOException if an error occurs reading the input stream
     * @throws YamlLintConfigException if the configuration contains an error so that its content cannot be successfully parsed
     * @throws IllegalArgumentException if <var>in</var> is <code>null</code>
     */
    public YamlLintConfig(InputStream in) throws YamlLintConfigException {
        if (in == null) {
            throw new IllegalArgumentException("in cannot be null");
        }

        parse(in);
    }

    /**
     * Tells if a file identified by its path a to be considered as a YAML file
     *
     * @param filepath the path of the file to be checked by this tool
     * @return <code>true</code> if a YAML file, <code>false</code> otherwise
     */
    public boolean isYamlFile(String filepath) {
        return yamlFiles.stream().anyMatch(filepath::matches);
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
     * Updates the attributes of this configuration instance with the one of the passed configuration. Existing entries are replaced (overridden).
     *
     * @param baseConfig a configuration that will extend this instance's rule configuration
     */
    @SuppressWarnings("unchecked")
    public void extend(YamlLintConfig baseConfig) {
        assert ruleConf != null;

        Map<String, Object> newConf = new HashMap<>(baseConfig.ruleConf);

        for (Map.Entry<String, Object> entry : ruleConf.entrySet()) {
            String ruleId = entry.getKey();
            Object conf = entry.getValue();
            if (conf instanceof Map && newConf.get(ruleId) != null) {
                deepMerge((Map<Object, Object>)newConf.get(ruleId), (Map<Object, Object>)conf);
            } else {
                newConf.put(ruleId, conf);
            }
        }

        ruleConf = newConf;

        if (baseConfig.yamlFiles != null) {
            yamlFiles = baseConfig.yamlFiles;
        }

        if (baseConfig.ignore != null) {
            ignore = baseConfig.ignore;
        }
    }


    /**
     * Parses a passed YAML configuration for this tool and updates <var>ruleConf</var>.
     * This method does not handle the BOM: the passed {@code CharSequence} is expected not to contain BOM.
     *
     * @param rawContent a YAML linter configuration
     * @throws YamlLintConfigException if a parse error occurs
     */
    protected void parse(final CharSequence rawContent) throws YamlLintConfigException {
        try (CharSequenceReader r = new CharSequenceReader(rawContent)) {
            parse(r);
        }
    }

    /**
     * Parses a passed YAML configuration for this tool and updates <var>ruleConf</var>, taking into account the possible
     * BOM
     *
     * @param in an input stream to a YAML linter configuration
     * @throws YamlLintConfigException if a parse error occurs
     */
    protected void parse(final InputStream in) throws YamlLintConfigException {
        parse(new UnicodeReader(in));
    }

    /**
     * Parses a passed YAML configuration for this tool and updates <var>ruleConf</var>
     *
     * @param r a reader to a YAML linter configuration
     * @throws YamlLintConfigException if a parse error occurs
     */
    @SuppressWarnings("unchecked")
    protected void parse(final Reader r) throws YamlLintConfigException {
        Map<Object, Object> conf;

        try {
            conf = new Yaml().load(r);
        } catch (YAMLException | ClassCastException e) {
            throw getInvalidConfigException("YAML", e.getMessage(), e);
        }
        if (conf == null) {
            throw getInvalidConfigException("not a dictionary");
        }

        // ruleConf stores YAML conf; rules stores actual rules
        ruleConf = (Map<String, Object>)conf.getOrDefault(RULES_KEY, new HashMap<String, Object>());

        // Does this conf override another conf that we need to load?
        if (conf.containsKey(EXTENDS_KEY)) {
            try {
                YamlLintConfig base = new YamlLintConfig(getExtendedConfigFile((String) conf.get(EXTENDS_KEY)));
                extend(base);
            } catch (IllegalArgumentException e) {
                throw getInvalidConfigException(EXTENDS_KEY, e.getMessage(), e);
            } catch (Exception e) {
                throw getInvalidConfigException(EXTENDS_KEY, "unknown error: " + e.getMessage(), e);
            }
        }

        // List of patterns used to identify YAML files
        if (conf.containsKey(YAML_FILES_KEY)) {
            if (!(conf.get(YAML_FILES_KEY) instanceof List)) {
                throw getInvalidConfigException(String.format("'%s' must be a list (of regexp patterns)", YAML_FILES_KEY));
            }
            yamlFiles = (List<String>)conf.get(YAML_FILES_KEY);
        }

        // List of patterns used to ignore files
        if (conf.containsKey(IGNORE_KEY)) {
            if (!(conf.get(IGNORE_KEY) instanceof String)) {
                throw getInvalidConfigException(String.format("'%s' should contain file patterns", IGNORE_KEY));
            }
            ignore = Arrays.asList(((String)conf.get(IGNORE_KEY)).split("\\r?\\n"));
        }
    }

    /**
     * Validates the rule configuration and instantiates the associated executable rules in <var>rules</var>
     *
     * @throws YamlLintConfigException if a mismatch exists between the configured rules and the rules contained in this package,
     * or if the rule configuration is invalid
     */
    protected void validate() throws YamlLintConfigException {
        for (Map.Entry<String, Object> entry : ruleConf.entrySet()) {
            String id = entry.getKey();
            Rule rule = RuleFactory.instance.getRule(id);
            if (rule == null) {
                throw getInvalidConfigException(String.format("no such rule: \"%s\"", id));
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
            Map<String, Object> mapConf = (Map<String, Object>)conf;

            // Deal with 'ignore' conf
            if (mapConf.containsKey(IGNORE_KEY)) {
                if (mapConf.get(IGNORE_KEY) instanceof List) {
                    rule.setIgnore((List<String>)mapConf.get(IGNORE_KEY));
                } else if (!(mapConf.get(IGNORE_KEY) instanceof String)) {
                    throw getInvalidConfigException("ignore should contain regexp patterns");
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
                throw getInvalidConfigException(String.format("level should be \"%s\", \"%s\" or \"%s\"", Linter.ERROR_LEVEL, Linter.WARNING_LEVEL, Linter.INFO_LEVEL));
            }

            Map<String, Object> options = rule.getOptions();
            for (Map.Entry<String, Object> entry : mapConf.entrySet()) {
                String optkey = entry.getKey();
                Object optvalue = entry.getValue();

                if (IGNORE_KEY.equals(optkey) || Linter.LEVEL_KEY.equals(optkey)) {
                    continue;
                }
                if (!options.containsKey(optkey)) {
                    throw getInvalidConfigException(String.format("unknown option \"%s\" for rule \"%s\"", optkey, rule.getId()));
                }
                if (options.get(optkey) instanceof List && !rule.isListOption(optkey)) {
                    if (!((List<?>)options.get(optkey)).contains(optvalue) && ((List<?>)options.get(optkey)).stream().noneMatch(object -> optvalue.getClass().equals(object))) {
                        throw getInvalidConfigException(String.format("option \"%s\" of \"%s\" should be in %s", optkey, rule.getId(), getListRepresentation((List<Object>)options.get(optkey))));
                    }
                } else {
                    if (rule.isListOption(optkey)) {
                        if (!(optvalue instanceof List)) {
                            throw getInvalidConfigException(String.format("option \"%s\" of \"%s\" should be a list", optkey, rule.getId()));
                        }
                    } else if (!optvalue.getClass().equals(options.get(optkey).getClass())) {
                        throw getInvalidConfigException(String.format("option \"%s\" of \"%s\" should be of type %s", optkey, rule.getId(), options.get(optkey).getClass().getSimpleName().toLowerCase()));
                    }
                }
                rule.addParameter(optkey, optvalue);
            }
            for (String optkey : options.keySet()) {
                if (!mapConf.containsKey(optkey)) {
                    mapConf.put(optkey, rule.getDefaultOptionValue(optkey));
                }
            }

            String validationMessage = rule.validate(mapConf);
            if (validationMessage != null && !"".equals(validationMessage)) {
                throw getInvalidConfigException(String.format("%s: %s", rule.getId(), validationMessage));
            }

            return mapConf;
        } else {
            throw getInvalidConfigException(String.format("rule \"%s\": should be either \"enable\", \"disable\" or a dictionary", rule.getId()));
        }
    }

    /**
     * Returns a <code>URL</code> pointing to the passed configuration file: local file system file or bundled configuration
     * file identified by its name (without the ".yaml" extension)
     *
     * @param name the file name
     * @return a <code>URL</code> to this file
     * @throws IllegalArgumentException if name is {@code null} or an error occurs handling the passed file name
     */
    protected URL getExtendedConfigFile(String name) {
        if (name == null || "".equals(name.trim())) {
            throw new IllegalArgumentException("need to extend something");
        }

        // Is it a standard conf shipped with yamllint...
        if (!name.contains(File.separator)) {
            URL url = getClass().getClassLoader().getResource("conf/" + name + ".yaml");

            if (url == null) {
                throw new IllegalArgumentException("Bundled configuration file \"" + name + "\" not found");
            }
            return url;
        }

        // or a custom conf on filesystem?
        try {
            return new File(name.replace('/', File.separatorChar)).toURI().toURL();
        } catch (MalformedURLException e) {
            // Should never happen...
            throw new IllegalArgumentException("Cannot create URL for the configuration file \"" + name + "\"", e);
        }
    }

    /**
     * Returns a representation of the passed list of objects (some object types have a specific representation, the default
     * being <code>Object.toString()</code> or <code>null</code> if applicable.
     *
     * @param list a non {@code null} list of object
     * @return a representation of the list and its objects
     */
    protected static String getListRepresentation(@Nonnull List<Object> list) {
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
                sb.append(((Class<?>)o).getSimpleName().toLowerCase());
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
    @SuppressWarnings("unchecked")
    protected static Map<Object, Object> deepMerge(Map<Object, Object> original, Map<Object, Object> newMap) {
        for (Map.Entry<Object, Object> entry : newMap.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key instanceof Map && original.get(key) instanceof Map) {
                original.put(key, deepMerge((Map<Object, Object>) original.get(key), (Map<Object, Object>) value));
            } else if (key instanceof List && original.get(key) instanceof List) {
                List<Object> originalChild = (List<Object>) original.get(key);
                for (Object each : (List<?>) value) {
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

    /**
     * Returns a {@code YamlLintConfigException} with the message "invalid config: %passed_message%"
     *
     * @param message a message that describes the configuration error
     * @return a {@code YamlLintConfigException} with the passed message
     */
    private static YamlLintConfigException getInvalidConfigException(String message) {
        return getInvalidConfigException(null, message, null);
    }

    /**
     * Returns a {@code YamlLintConfigException} with the message "invalid%specifier% config: %passed_message%"
     *
     * @param message a string to be passed after 'invalid'. Pass {@code null} if you do not want any specifier.
     * @param message a message that describes the configuration error
     * @param e an optional (may be {@code null}) {@code Throwable} to be set as the ancestor of the returned exception
     * @return a {@code YamlLintConfigException} with the passed message
     */
    private static YamlLintConfigException getInvalidConfigException(@Nullable String specifier, String message, @Nullable Throwable e) {
        String m = String.format("invalid%s config: %s", (specifier == null)?"":(" " + specifier), message);
        if (e == null) {
            return new YamlLintConfigException(m);
        } else {
            return new YamlLintConfigException(m, e);
        }
    }
}
