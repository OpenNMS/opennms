package org.opennms.netmgt.junit.runner;

import org.junit.runner.Computer;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class SeleniumComputer extends Computer{
    private String m_baseUrl = "";
    private int m_timeout = 3;
    
    public SeleniumComputer() {
        
    }
    
    public SeleniumComputer(String baseUrl) {
        setBaseUrl(baseUrl);
    }
    
    public SeleniumComputer(String baseUrl, int timeoutInSeconds) {
        setBaseUrl(baseUrl);
        setTimeout(timeoutInSeconds);
    }
    
    @Override
    protected Runner getRunner(RunnerBuilder builder, Class<?> testClass) throws Throwable {
        TestClassRunnerForSelenium runner = new TestClassRunnerForSelenium(testClass, getBaseUrl(), getTimeout());
        return runner;
    }

    public String getBaseUrl() {
        return m_baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        m_baseUrl = baseUrl;
    }

    public int getTimeout() {
        return m_timeout;
    }

    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }
    
}