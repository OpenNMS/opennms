package org.opennms.netmgt.poller.monitor;

import static org.junit.Assert.assertEquals;
import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.Test;
import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.opennms.netmgt.junit.runner.SeleniumComputer;


public class GroovyRunnerTest {

    
    @Test
    public void testGroovyClassLoader() throws CompilationFailedException, IOException {
        String filename = "src/main/resources/groovy/SeleniumGroovyTest.groovy";
        Result result = runJUnitTests(getGroovyClass(filename));
        assertEquals(0, result.getFailureCount());
    }
    
    
    @Test
    public void testAnnotatedGroovyClassWithBaseUrl() throws IOException {
        String filename = "src/main/resources/groovy/AnnotatedGroovyTest.groovy";
        Result result = runJUnitTests(getGroovyClass(filename));
        
        assertEquals(0, result.getFailureCount());
    }
    
    @Test
    public void testCustomJUnitRunnerWithComputer() throws CompilationFailedException, IOException {
        String filename = "src/main/resources/groovy/GroovyRunnerTest.groovy";
        
        Computer computer = new SeleniumComputer("http://www.papajohns.co.uk");
        
        Result result = runJunitTestWithComputer(computer, getGroovyClass(filename));
        
        assertEquals(0, result.getFailureCount());
    }

    private Result runJunitTestWithComputer(Computer computer, Class<?> clazz) {
        Result result = JUnitCore.runClasses(computer, clazz);
        
        List<Failure> failures = result.getFailures();
        for(Failure failure : failures) {
            System.out.println(failure.getMessage());
        }
        return result;
    }

    private Class<?> getGroovyClass(String filename) throws IOException {
        GroovyClassLoader gcl = new GroovyClassLoader();
        Class<?> clazz = gcl.parseClass(new File(filename));
        return clazz;
    }
    
    private Result runJUnitTests(Class<?> clazz) {
        Result result = JUnitCore.runClasses(clazz);
        List<Failure> failures = result.getFailures();
        for(Failure failure : failures) {
            System.out.println(failure.getMessage());
        }
        return result;
    }
}
