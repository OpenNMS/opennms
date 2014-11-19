/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
    public void testGroovyClassLoaderFailConstructorError() throws CompilationFailedException, IOException {
        String filename = "src/test/resources/groovy/SeleniumGroovyTest.groovy";
        Result result = runJUnitTests(getGroovyClass(filename));
        assertEquals(1, result.getFailureCount());
    }
    
    
    @Test
    public void testAnnotatedGroovyClassWithBaseUrl() throws IOException {
        String filename = "src/test/resources/groovy/AnnotatedGroovyTest.groovy";
        Result result = runJUnitTests(getGroovyClass(filename));
        
        assertEquals(0, result.getFailureCount());
    }
    
    @Test
    public void testCustomJUnitRunnerWithComputer() throws CompilationFailedException, IOException {
        String filename = "src/test/resources/groovy/GroovyRunnerTest.groovy";
        
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
