package org.opennms.smoketest.expectations;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;


public class ExpectationBuilder {
    private final Expectation m_expectation;

    public ExpectationBuilder(final String target) {
        m_expectation = new Expectation(target);
    }

    public ExpectationBuilder withText(final String text) {
        m_expectation.addTextPresent(text);
        return this;
    }

    public ExpectationBuilder and() {
        m_expectation.setMatchType(Expectation.Type.AND);
        return this;
    }

    public ExpectationBuilder or() {
        m_expectation.setMatchType(Expectation.Type.OR);
        return this;
    }

    public ExpectationBuilder waitFor(final long time, final TimeUnit units) {
        m_expectation.setWaitTime(time, units);
        return this;
    }

    public static Expectation frontPage() {
        return new ExpectationBuilder("//a[contains(@href,'/opennms/index.jsp') ]").withText("Nodes with Pending Problems").build();
    }

    public Expectation build() {
        return m_expectation;
    }

    public void check(final WebDriver webdriver) throws Exception {
        build().check(webdriver);
    }
}