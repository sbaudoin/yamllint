package com.github.sbaudoin.yamllint.rules;

public class BrokenRule extends Rule {
    public BrokenRule() {
        throw new IllegalStateException("This exception is expected");
    }

    @Override
    public Rule.TYPE getType() {
        return null;
    }
}
