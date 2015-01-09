package org.opennms.smoketest.expectations;


public class ExpectationFailed extends Exception {
    private static final long serialVersionUID = 1L;

    public ExpectationFailed(final Expectation expectation) {
        super(expectation.toString());
    }

    public ExpectationFailed(final Expectation expectation, final String failure) {
        super("(" + failure + "): " + expectation.toString());
    }
}