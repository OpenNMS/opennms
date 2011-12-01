package org.opennms.netmgt.junit.runner;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

public class SeleniumJUnitRunner extends Suite{
    
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface BaseUrl{
        String url();
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TimeoutInSeconds{
        int timeout();
    }
    
    private final List<Runner> m_runners = new ArrayList<Runner>();
    
    public SeleniumJUnitRunner(Class<?> testClass) throws InitializationError 
    {
        super(testClass, Collections.<Runner>emptyList());
        m_runners.add(new TestClassRunnerForSelenium( getTestClass().getJavaClass(), getBaseUrlAnnotation( testClass ), getTimeoutAnnotation(testClass) ) );
    }
    

    private int getTimeoutAnnotation(Class<?> testClass) {
        SeleniumJUnitRunner.TimeoutInSeconds timeout = testClass.getAnnotation(SeleniumJUnitRunner.TimeoutInSeconds.class);
        if(timeout == null) {
            return 3;
        }else {
            return timeout.timeout();
        }
    }


    private String getBaseUrlAnnotation(Class<?> klass) {
        SeleniumJUnitRunner.BaseUrl baseUrl = klass.getAnnotation(SeleniumJUnitRunner.BaseUrl.class);
        if(baseUrl == null) {
            return "";
        }else {
            return baseUrl.url();
        }
    }


    @Override
    protected List<Runner> getChildren(){
        return m_runners;
    }
}