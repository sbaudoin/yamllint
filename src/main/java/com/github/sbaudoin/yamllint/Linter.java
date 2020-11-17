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
import org.yaml.snakeyaml.error.MarkedYAMLException;
import com.github.sbaudoin.yamllint.rules.CommentRule;
import com.github.sbaudoin.yamllint.rules.LineRule;
import com.github.sbaudoin.yamllint.rules.Rule;
import com.github.sbaudoin.yamllint.rules.TokenRule;
import org.yaml.snakeyaml.reader.UnicodeReader;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main linter class. This is the class that does the main lint job. As it contains only static methods can be invoked
 * directly outside of the {@link Cli} class.
 */
public class Linter {
    /**
     * Rule token
     */
    private static final String RULE_TOKEN = "rule:";

    /**
     * Key for error levels
     */
    public static final String LEVEL_KEY = "level";

    /**
     * Error level for when there is no error...
     */
    public static final String NONE_LEVEL = "none";

    /**
     * Info error level
     */
    public static final String INFO_LEVEL = "info";

    /**
     * Warning error level
     */
    public static final String WARNING_LEVEL = "warning";

    /**
     * Highest error level
     */
    public static final String ERROR_LEVEL = "error";


    /**
     * Map used to resolve the error levels by number or ID
     */
    private static final Map<Object, Object> PROBLEM_LEVELS = Collections.unmodifiableMap(
            Stream.of(
                    new AbstractMap.SimpleEntry<>(0, NONE_LEVEL),
                    new AbstractMap.SimpleEntry<>(NONE_LEVEL, 0),
                    new AbstractMap.SimpleEntry<>(1, INFO_LEVEL),
                    new AbstractMap.SimpleEntry<>(INFO_LEVEL, 1),
                    new AbstractMap.SimpleEntry<>(2, WARNING_LEVEL),
                    new AbstractMap.SimpleEntry<>(WARNING_LEVEL, 2),
                    new AbstractMap.SimpleEntry<>(3, ERROR_LEVEL),
                    new AbstractMap.SimpleEntry<>(ERROR_LEVEL, 3)
            ).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue))
    );


    /**
     * Hide constructor
     */
    private Linter() {
    }


    /**
     * Lints a YAML source represented as a string
     *
     * @param buffer a YAML configuration
     * @param conf yamllint configuration. Cannot be <code>null</code>.
     * @return the list of problems found for the passed file, possibly empty (never <code>null</code>)
     * @throws IllegalArgumentException if <var>conf</var> is {@code null}
     */
    public static List<LintProblem> run(CharSequence buffer, YamlLintConfig conf) {
        return run(buffer, conf, new Yaml());
    }

    /**
     * Lints a YAML source represented as a string
     *
     * @param buffer a YAML configuration
     * @param conf yamllint configuration
     * @param yaml the YAML parser to use for syntax checking
     * @return the list of problems found for the passed file, possibly empty (never <code>null</code>)
     */
    public static List<LintProblem> run(CharSequence buffer, YamlLintConfig conf, Yaml yaml) {
        return run(buffer, conf, yaml, null);
    }

    /**
     * Lints a YAML source represented as a string
     *
     * @param buffer a YAML configuration. Be aware that this {@code InputStream} is not closed by this method,
     *               you will have to do it yourself later.
     * @param conf yamllint configuration. Cannot be <code>null</code>.
     * @return the list of problems found for the passed file, possibly empty (never <code>null</code>)
     * @throws IOException if there is a problem reading the file
     * @throws IllegalArgumentException if <var>conf</var> is {@code null}
     */
    public static List<LintProblem> run(InputStream buffer, YamlLintConfig conf) throws IOException {
        return run(buffer, conf, new Yaml());
    }

    /**
     * Lints a YAML source represented as a string
     *
     * @param buffer a YAML configuration. Be aware that this {@code InputStream} is not closed by this method,
     *               you will have to do it yourself later.
     * @param conf yamllint configuration
     * @param yaml the YAML parser to use for syntax checking
     * @return the list of problems found for the passed file, possibly empty (never <code>null</code>)
     * @throws IOException if there is a problem reading the file
     */
    public static List<LintProblem> run(InputStream buffer, YamlLintConfig conf, Yaml yaml) throws IOException {
        return run(buffer, conf, yaml, null);
    }

    /**
     * Lints a YAML source represented as a file
     *
     * @param conf yamllint configuration
     * @param file the (YAML) file to lint
     * @return the list of problems found for the passed file, possibly empty (never <code>null</code>)
     * @throws IOException if there is a problem reading the file
     */
    public static List<LintProblem> run(YamlLintConfig conf, @Nullable File file) throws IOException {
        return run(conf, new Yaml(), file);
    }

    /**
     * Lints a YAML source represented as a file
     *
     * @param conf yamllint configuration. Cannot be <code>null</code>.
     * @param yaml the YAML parser to use for syntax checking
     * @param file the (YAML) file to lint
     * @return the list of problems found for the passed file, possibly empty (never <code>null</code>)
     * @throws IOException if there is a problem reading the file
     * @throws NullPointerException if <var>conf</var> is {@code null}
     */
    public static List<LintProblem> run(YamlLintConfig conf, Yaml yaml, File file) throws IOException {
        Objects.requireNonNull(conf);
        Objects.requireNonNull(file);

        if (conf.isFileIgnored(file.getPath())) {
            return new ArrayList<>();
        }

        try (FileInputStream in = new FileInputStream(file)) {
            return run(in, conf, yaml, file);
        }
    }

    /**
     * Checks a YAML string and returns a list of problems
     *
     * @param in the YAML content to be analyzed. Be aware that this {@code InputStream} is not closed by this method,
     *           you will have to do it yourself later.
     * @param conf yamllint configuration. Cannot be <code>null</code>.
     * @param file the file whose content has been passed as the <var>buffer</var>. May be <code>null</code>.
     * @return the list of problems found on the passed YAML string
     * @throws IOException if an error occurred while reading the input stream
     * @throws NullPointerException if <var>conf</var> is {@code null}
     */
    public static List<LintProblem> run(InputStream in, YamlLintConfig conf, @Nullable File file) throws IOException {
        return run(in, conf, new Yaml(), file);
    }

    /**
     * Checks a YAML string and returns a list of problems
     *
     * @param in the YAML content to be analyzed. Be aware that this {@code InputStream} is not closed by this method,
     *           you will have to do it yourself later.
     * @param conf yamllint configuration. Cannot be <code>null</code>.
     * @param yaml the YAML parser to use for syntax checking
     * @param file the file whose content has been passed as the <var>buffer</var>. May be <code>null</code>.
     * @return the list of problems found on the passed YAML string
     * @throws IOException if an error occurred while reading the input stream
     */
    public static List<LintProblem> run(final InputStream in, final YamlLintConfig conf, final Yaml yaml, final @Nullable File file) throws IOException {
        Objects.requireNonNull(conf);
        Objects.requireNonNull(in);

        // Properly read buffer, taking the BOM into account
        Reader reader = new UnicodeReader(in);

        char[] arr = new char[8 * 1024];
        StringBuilder buffer = new StringBuilder();
        int numCharsRead;
        while ((numCharsRead = reader.read(arr, 0, arr.length)) != -1) {
            buffer.append(arr, 0, numCharsRead);
        }

        return run(buffer.toString(), conf, yaml, file);
    }

    /**
     * Checks a YAML stream and returns a list of problems
     *
     * @param buffer the YAML content to be analyzed
     * @param conf yamllint configuration. Cannot be <code>null</code>.
     * @param file the file whose content has been passed as the <var>buffer</var>. May be <code>null</code>.
     * @return the list of problems found on the passed YAML string
     * @throws NullPointerException if <var>conf</var> is {@code null}
     */
    public static List<LintProblem> run(CharSequence buffer, YamlLintConfig conf, @Nullable File file) {
        return run(buffer, conf, new Yaml(), file);
    }

    /**
     * Checks a YAML stream and returns a list of problems
     *
     * @param buffer the YAML content to be analyzed
     * @param conf yamllint configuration. Cannot be <code>null</code>.
     * @param yaml the YAML parser to use for syntax checking
     * @param file the file whose content has been passed as the <var>buffer</var>. May be <code>null</code>.
     * @return the list of problems found on the passed YAML string
     */
    public static List<LintProblem> run(final CharSequence buffer, final YamlLintConfig conf, final Yaml yaml, final @Nullable File file) {
        Objects.requireNonNull(conf);

        // Use a set to avoid duplicated problems
        TreeSet<LintProblem> problems = new TreeSet<>((p1, p2) -> {
            if (p1.getLine() < p2.getLine()) {
                return -1;
            }
            if (p1.getLine() > p2.getLine()) {
                return 1;
            }
            if (p1.getColumn() < p2.getColumn()) {
                return -1;
            }
            if (p1.getColumn() > p2.getColumn()) {
                return 1;
            }
            if (p1.getRuleId() == null && p2.getRuleId() != null) {
                // p1 is a syntax error, it comes first
                return -1;
            }
            return p1.getMessage().compareTo(p2.getMessage());
        });

        // If the document contains a syntax error, save it
        LintProblem syntaxError = getSyntaxError(buffer, yaml);
        if (syntaxError != null) {
            problems.add(syntaxError);
        }

        // If there is already a yamllint error at the same place, discard
        // it as it is probably redundant (and maybe it's just a 'warning',
        // in which case the script won't even exit with a failure status).
        problems.addAll(
                getCosmeticProblems(buffer, conf, file).stream().filter(
                        problem -> syntaxError == null || syntaxError.getLine() != problem.getLine() || syntaxError.getColumn() != problem.getColumn()
                ).collect(Collectors.toList()));

        return new ArrayList<>(problems);
    }

    /**
     * Returns the level (<code>Integer</code>) or ID (<code>String</code>) of the passed ID (<code>String</code>)
     * or level (<code>Integer</code>)
     *
     * @param key the problem level or ID
     * @return an <code>Integer</code> or a <code>String</code>, or <code>null</code> if not found
     */
    public static Object getProblemLevel(Object key) {
        return PROBLEM_LEVELS.get(key);
    }

    /**
     * Parses the passed YAML string to detect syntax errors. If an error is met, a problem is return.
     *
     * @param buffer a YAML string
     * @return a problem or <code>null</code> if there is no syntax error
     */
    public static LintProblem getSyntaxError(CharSequence buffer) {
        return getSyntaxError(buffer, new Yaml());
    }

    /**
     * Parses the passed YAML string to detect syntax errors. If an error is met, a problem is return.
     *
     * @param buffer a YAML string
     * @param yaml the YAML parser to use for syntax checking
     * @return a problem or <code>null</code> if there is no syntax error
     */
    public static LintProblem getSyntaxError(final CharSequence buffer, final Yaml yaml) {
        try {
            // Need to use loadAll in the event there are multiple documents in the same stream
            yaml.parse(new CharSequenceReader(buffer)).forEach(o -> { /* Do nothing on purpose, required to have the parser to process each document */ });
        } catch (MarkedYAMLException e) {
            LintProblem problem = new LintProblem(e.getProblemMark().getLine() + 1,
                    e.getProblemMark().getColumn() + 1,
                    "syntax error: " + e.getProblem());
            problem.setLevel(ERROR_LEVEL);
            problem.setExtraDesc(e.getMessage());
            return problem;
        }
        return null;
    }

    /**
     * Returns the list of non-syntax related problems found with the passed YAML string. The file is optional ({@code null}) and is there
     * for filtering the rules to be applied.
     *
     * @param buffer the YAML string to be checked
     * @param conf the YAML lint configuration. Cannot be {@code null}.
     * @param file file supposed to be the passed YAML string. Used to determined the rules to be applied. May be {@code null}.
     * @return a list of problems found on the passed string
     * @throws NullPointerException if <var>conf</var> is {@code null}
     */
    @SuppressWarnings("unchecked")
    public static List<LintProblem> getCosmeticProblems(final CharSequence buffer, final YamlLintConfig conf, final @Nullable File file) {
        Objects.requireNonNull(conf);

        List<Rule> rules = conf.getEnabledRules(file);

        // Split token rules from line rules
        List<Rule> tokenRules = rules.stream().filter(rule -> rule.getType() == Rule.TYPE.TOKEN).collect(Collectors.toList());
        List<Rule> commentRules = rules.stream().filter(rule -> rule.getType() == Rule.TYPE.COMMENT).collect(Collectors.toList());
        List<Rule> lineRules = rules.stream().filter(rule -> rule.getType() == Rule.TYPE.LINE).collect(Collectors.toList());

        final Map<String, Object> context = new HashMap<>();
        tokenRules.forEach(rule -> context.put(rule.getId(), new HashMap<String, Object>()));

        // Use a cache to store problems and flush it only when a end of line is
        // found. This allows the use of yamllint directive to disable some rules on
        // some lines.
        List<LintProblem> cache = new ArrayList<>();
        DisableDirective disabled = new DisableDirective(rules);
        DisableLineDirective disabledForLine = new DisableLineDirective(rules);
        DisableLineDirective disabledForNextLine = new DisableLineDirective(rules);

        String sBuffer = buffer.toString();
        List<LintProblem> problems = new ArrayList<>();
        List<Parser.Lined> items = Parser.getTokensOrCommentsOrLines(sBuffer);
        for (Parser.Lined elem : items) {
            if (elem instanceof Parser.Token) {
                for (Rule rule : tokenRules) {
                    Map<Object, Object> ruleConf = (Map<Object, Object>)conf.getRuleConf(rule.getId());
                    saveProblemsInCache(cache, rule, ruleConf,
                            ((TokenRule)rule).check(ruleConf, ((Parser.Token)elem).getCurr(), ((Parser.Token)elem).getPrev(), ((Parser.Token)elem).getNext(),
                            ((Parser.Token) elem).getNextNext(), (Map<String, Object>)context.get(rule.getId())));
                }
            } else if (elem instanceof Parser.Comment) {
                for (Rule rule : commentRules) {
                    Map<Object, Object> ruleConf = (Map<Object, Object>)conf.getRuleConf(rule.getId());
                    saveProblemsInCache(cache, rule, ruleConf, ((CommentRule)rule).check(ruleConf, (Parser.Comment)elem));
                }

                disabled.processComment((Parser.Comment)elem);
                if (((Parser.Comment)elem).isInline()) {
                    disabledForLine.processComment((Parser.Comment)elem);
                } else {
                    disabledForNextLine.processComment((Parser.Comment)elem);
                }
            } else if (elem instanceof Parser.Line) {
                for (Rule rule : lineRules) {
                    Map<Object, Object> ruleConf = (Map<Object, Object>)conf.getRuleConf(rule.getId());
                    saveProblemsInCache(cache, rule, ruleConf, ((LineRule)rule).check(ruleConf, (Parser.Line)elem));
                }

                // This is the last token / comment / line of this line, let's flush the
                // problems found (but filter them according to the directives)
                for (LintProblem problem : cache) {
                    if (!(disabledForLine.isDisabledByDirective(problem) || disabled.isDisabledByDirective(problem))) {
                        problems.add(problem);
                    }
                }

                disabledForLine = disabledForNextLine;
                disabledForNextLine = new DisableLineDirective(rules);
                cache.clear();
            }
        }
        return problems;
    }


    /**
     * Saves in the passed cache the problems with the proper level and rule Id
     *
     * @param cache the cache where to save the problems
     * @param rule the rule that detected the problems
     * @param conf the rule configuration
     * @param problems the problems to be saved
     */
    private static void saveProblemsInCache(List<LintProblem> cache, Rule rule, Map<?, ?> conf, List<LintProblem> problems) {
        for (LintProblem problem : problems) {
            problem.setRuleId(rule.getId());
            problem.setLevel((String)conf.get(LEVEL_KEY));
            cache.add(problem);
        }
    }


    private static class DisableDirective {
        protected List<String> rules;
        protected List<String> allRules;

        DisableDirective(List<Rule> rules) {
            this.rules = new ArrayList<>();
            allRules = new ArrayList<>();
            rules.forEach(rule -> allRules.add(rule.getId()));
        }

        public void processComment(final Parser.Comment token) {
            String comment = token.toString();

            Matcher disableMatcher = Pattern.compile("# yamllint disable(( rule:\\S+)*)\\s*$").matcher(comment);
            Matcher enableMatcher = Pattern.compile("# yamllint enable(( rule:\\S+)*)\\s*$").matcher(comment);
            if (disableMatcher.find()) {
                String[] cRules = disableMatcher.group(1).trim().replace(RULE_TOKEN, "").split(" ");
                if (cRules.length == 0 || "".equals(cRules[0])) {
                    rules = new ArrayList<>(allRules);
                } else {
                    rules.addAll(Arrays.stream(cRules).filter(id -> allRules.contains(id)).collect(Collectors.toList()));
                }
            } else if (enableMatcher.find()) {
                String[] cRules = enableMatcher.group(1).trim().replace(RULE_TOKEN, "").split(" ");
                if (cRules.length == 0 || "".equals(cRules[0])) {
                    rules.clear();
                } else {
                    for (String id : cRules) {
                        rules.remove(id);
                    }
                }
            }
        }

        /**
         * Tells if a problem relates to a disabled rule
         *
         * @param problem a problem
         * @return <code>true</code> if the rule is disabled for this problem, <code>false</code> is not
         */
        public boolean isDisabledByDirective(LintProblem problem) {
            return rules.contains(problem.getRuleId());
        }
    }

    /**
     * Extension to <code>disableDirective</code> for line-oriented rules
     */
    private static class DisableLineDirective extends DisableDirective {
        public DisableLineDirective(List<Rule> rules) {
            super(rules);
        }

        @Override
        public void processComment(final Parser.Comment token) {
            String comment = token.toString();

            Matcher disableMatcher = Pattern.compile("# yamllint disable-line(( rule:\\S+)*)\\s*$").matcher(comment);
            if (disableMatcher.find()) {
                String[] cRules = disableMatcher.group(1).trim().replace(RULE_TOKEN, "").split(" ");
                if (cRules.length == 0 || "".equals(cRules[0])) {
                    rules = new ArrayList<>(allRules);
                } else {
                    for (String id : cRules) {
                        if (allRules.contains(id)) {
                            rules.add(id);
                        }
                    }
                }
            }
        }
    }
}
