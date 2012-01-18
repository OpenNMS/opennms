package org.opennms.netmgt.junit.runner;

import java.util.List;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class TestClassRunnerForSelenium extends BlockJUnit4ClassRunner{
    
    private int m_timeout;
    private String m_baseUrl;
    
    TestClassRunnerForSelenium(Class<?> type, String baseUrl, int timeoutInSeconds) throws InitializationError {
        super(type);
        setBaseUrl(baseUrl);
        setTimeout(timeoutInSeconds);
    }
    
    
    
    @Override
    public Object createTest() throws Exception{
        return getTestClass().getOnlyConstructor().newInstance(getBaseUrl(), getTimeout());
    }
    
    @Override
    protected void validateConstructor(List<Throwable> errors) {
        validateOnlyOneConstructor(errors);
    }



    public int getTimeout() {
        return m_timeout;
    }



    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }



    public String getBaseUrl() {
        return m_baseUrl;
    }



    public void setBaseUrl(String baseUrl) {
        m_baseUrl = baseUrl;
    }
    
}