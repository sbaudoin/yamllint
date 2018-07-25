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

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import com.github.sbaudoin.yamllint.rules.CommentRule;
import com.github.sbaudoin.yamllint.rules.LineRule;
import com.github.sbaudoin.yamllint.rules.Rule;
import com.github.sbaudoin.yamllint.rules.TokenRule;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
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
            ).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()))
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
     * @param conf yamllint configuration
     * @return the list of problems found for the passed file, possibly empty (never <code>null</code>)
     */
    public static List<LintProblem> run(String buffer, YamlLintConfig conf) {
        return run(buffer, conf, null);
    }

    /**
     * Lints a YAML source represented as a file
     *
     * @param conf yamllint configuration
     * @param file the (YAML) file to lint
     * @return the list of problems found for the passed file, possibly empty (never <code>null</code>)
     * @throws IOException if there is a problem reading the file
     */
    public static List<LintProblem> run(YamlLintConfig conf, File file) throws IOException {
        if (conf.isFileIgnored(file.getPath())) {
            return new ArrayList<>();
        }

        // Read file into a string and process it
        return run(new String(Files.readAllBytes(file.toPath())), conf, file);
    }

    /**
     * Checks a YAML string and returns a list of problems
     *
     * @param buffer the YAML content to be analyzed
     * @param conf yamllint configuration. Cannot be <code>null</code>.
     * @param file the file whose content has been passed as the <var>buffer</var>. May be <code>null</code>.
     * @return the list of problems found on the passed YAML string
     */
    public static List<LintProblem> run(String buffer, YamlLintConfig conf, File file) {
        // Use a set to avoid duplicated problems
        LinkedHashSet<LintProblem> problems = new LinkedHashSet<>();

        // If the document contains a syntax error, save it and yield it at the
        // right line
        LintProblem syntaxError = getSyntaxError(buffer);

        for (LintProblem problem : getCosmeticProblems(buffer, conf, file)) {
            // Insert the syntax error (if any) at the right place...
            if (syntaxError != null && syntaxError.getLine() <= problem.getLine() && syntaxError.getColumn() <= problem.getColumn()) {
                problems.add(syntaxError);
                // If there is already a yamllint error at the same place, discard
                // it as it is probably redundant (and maybe it's just a 'warning',
                // in which case the script won't even exit with a failure status).
                if (syntaxError.getLine() == problem.getLine() && syntaxError.getColumn() == problem.getColumn()) {
                    syntaxError = null;
                    continue;
                }
            }
            problems.add(problem);
        }

        if (syntaxError != null) {
            problems.add(syntaxError);
        }
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
    public static LintProblem getSyntaxError(String buffer) {
        try {
            // Need to use loadAll in the event there are multiple documents in the same stream
            new Yaml().loadAll(new StringReader(buffer)).forEach(o -> { /* Do nothing on purpose, required to have the parser to process each document */ });
        } catch (MarkedYAMLException e) {
            LintProblem problem = new LintProblem(e.getProblemMark().getLine() + 1,
                    e.getProblemMark().getColumn() + 1,
                    "syntax error: " + e.getProblem());
            problem.setLevel(ERROR_LEVEL);
            return problem;
        }
        return null;
    }

    /**
     * Returns the list of non-syntax related problems found with the passed YAML string. The file is optional ({@code null}) and is there
     * for filtering the rules to be applied.
     *
     * @param buffer the YAML string to be checked
     * @param conf the YAML lint configuration
     * @param file file supposed to be the passed YAML string. Used to determined the rules to be applied. May be {@code null}.
     * @return a list of problems found on the passed string
     */
    @SuppressWarnings("unchecked")
    public static List<LintProblem> getCosmeticProblems(String buffer, YamlLintConfig conf, @Nullable File file) {
        List<Rule> rules = conf.getEnabledRules(file);

        // Split token rules from line rules
        List<Rule> tokenRules = rules.stream().filter(rule -> rule.getType() == Rule.TYPE.TOKEN).collect(Collectors.toList());
        List<Rule> commentRules = rules.stream().filter(rule -> rule.getType() == Rule.TYPE.COMMENT).collect(Collectors.toList());
        List<Rule> lineRules = rules.stream().filter(rule -> rule.getType() == Rule.TYPE.LINE).collect(Collectors.toList());

        Map<String, Object> context = new HashMap<>();
        for (Rule rule : tokenRules) {
            context.put(rule.getId(), new HashMap<String, Object>());
        }

        // Use a cache to store problems and flush it only when a end of line is
        // found. This allows the use of yamllint directive to disable some rules on
        // some lines.
        List<LintProblem> cache = new ArrayList<>();
        DisableDirective disabled = new DisableDirective(rules);
        DisableLineDirective disabledForLine = new DisableLineDirective(rules);
        DisableLineDirective disabledForNextLine = new DisableLineDirective(rules);

        List<LintProblem> problems = new ArrayList<>();
        List<Parser.Lined> items = Parser.getTokensOrCommentsOrLines(buffer);
        for (Parser.Lined elem : items) {
            if (elem instanceof Parser.Token) {
                for (Rule rule : tokenRules) {
                    Map ruleConf = (Map)conf.getRuleConf(rule.getId());
                    for (LintProblem problem : ((TokenRule)rule).check(ruleConf, ((Parser.Token)elem).getCurr(), ((Parser.Token)elem).getPrev(), ((Parser.Token) elem).getNext(),
                            ((Parser.Token) elem).getNextNext(), (Map<String, Object>)context.get(rule.getId()))) {
                        problem.setRuleId(rule.getId());
                        problem.setLevel((String)ruleConf.get(LEVEL_KEY));
                        cache.add(problem);
                    }
                }
            } else if (elem instanceof Parser.Comment) {
                for (Rule rule : commentRules) {
                    Map ruleConf = (Map)conf.getRuleConf(rule.getId());
                    for (LintProblem problem : ((CommentRule)rule).check(ruleConf, (Parser.Comment)elem)) {
                        problem.setRuleId(rule.getId());
                        problem.setLevel((String)ruleConf.get(LEVEL_KEY));
                        cache.add(problem);
                    }
                }

                disabled.processComment((Parser.Comment)elem);
                if (((Parser.Comment)elem).isInline()) {
                    disabledForLine.processComment((Parser.Comment)elem);
                } else {
                    disabledForNextLine.processComment((Parser.Comment)elem);
                }
            } else if (elem instanceof Parser.Line) {
                for (Rule rule : lineRules) {
                    Map ruleConf = (Map)conf.getRuleConf(rule.getId());
                    for (LintProblem problem : ((LineRule)rule).check(ruleConf, (Parser.Line)elem)) {
                        problem.setRuleId(rule.getId());
                        problem.setLevel((String)ruleConf.get(LEVEL_KEY));
                        cache.add(problem);
                    }
                }

                // This is the last token / comment / line of this line, let 's flush the
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


    private static class DisableDirective {
        protected List<String> rules;
        protected List<String> allRules;

        DisableDirective(List<Rule> rules) {
            this.rules = new ArrayList<>();
            allRules = new ArrayList<>();
            rules.forEach(rule -> allRules.add(rule.getId()));
        }

        public void processComment(Parser.Comment token) {
            String comment = token.toString();

            Matcher disableMatcher = Pattern.compile("# yamllint disable(( rule:\\S+)*)\\s*$").matcher(comment);
            Matcher enableMatcher = Pattern.compile("# yamllint enable(( rule:\\S+)*)\\s*$").matcher(comment);
            if (disableMatcher.find()) {
                String[] cRules = disableMatcher.group(1).trim().replaceAll(RULE_TOKEN, "").split(" ");
                if (cRules.length == 0 || "".equals(cRules[0])) {
                    rules = new ArrayList<>(allRules);
                } else {
                    for (String id : cRules) {
                        if (allRules.contains(id)) {
                            rules.add(id);
                        }
                    }
                }
            } else if (enableMatcher.find()) {
                String[] cRules = enableMatcher.group(1).trim().replaceAll(RULE_TOKEN, "").split(" ");
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
        public void processComment(Parser.Comment token) {
            String comment = token.toString();

            Matcher disableMatcher = Pattern.compile("# yamllint disable-line(( rule:\\S+)*)\\s*$").matcher(comment);
            if (disableMatcher.find()) {
                String[] cRules = disableMatcher.group(1).trim().replaceAll(RULE_TOKEN, "").split(" ");
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
