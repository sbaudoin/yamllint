package com.github.sbaudoin.yamllint.rules;

public class BrokenRule extends Rule {
    public BrokenRule() {
        throw new IllegalStateException("Crash!");
    }

    @Override
    public Rule.TYPE getType() {
        return null;
    }
}
