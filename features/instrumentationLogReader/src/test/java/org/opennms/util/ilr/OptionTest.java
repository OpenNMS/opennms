/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.util.ilr;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


import org.junit.Before;
import org.junit.Test;

public class OptionTest {
    private OptionTester m_argHandler;
    private ArgumentParser m_argParser;
    public static class OptionTester {
        
        
        boolean arg1 = false;
        boolean arg2 = false;
        boolean arg3 = false;
        String fileName = "";
        
        @Option(shortName = "arg1", longName = "argumentOne", help = "this is argument one")
        public void handleArg1() {
            arg1 = true;
        }
        @Option(shortName = "arg2", longName = "argumentTwo", help = "this is argument two")
        public void handleArg2() {
            arg2 = true;
        }
        @Option(shortName = "arg3", longName = "argumentThree", help = "this is argument three")
        public void handleArg3() {
            arg3 = true;
        }
        @Arguments(help = "this is one or more String Arguments")
        public void handleFileArguments(String arg) {
            fileName += " "+ arg;
        }
        
        public boolean getArg1(){
            return arg1;
        }
        public boolean getArg2(){
            return arg2;
        }
        public boolean getArg3(){
            return arg3;
        }
        
        public String getFileName(){
            return fileName;
        }
        public void reset() {
            arg1 = false;
            arg2 = false;
            arg3 = false;
        }
    }
    @Before
    public void setUp() {
        m_argHandler = new OptionTester();
        m_argParser = new ArgumentParser("Test", m_argHandler);
    }
    
    @Test
    public void testAnnotations() {
        OptionTester argHandler = new OptionTester();
        Method [] methods = argHandler.getClass().getMethods();
        for(Method m: methods){                   
          
            if(m.isAnnotationPresent(Option.class)){
                System.err.println(m);
                Option option = m.getAnnotation(Option.class);
                System.err.println(option);
                
            }
        }
    }

    @Test
    public void testParseLongOption() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m_argParser.processArgs(new String []{"--argumentOne"});
        assertTrue(m_argHandler.getArg1());
    }
    
    @Test
    public void testParseShortOption() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m_argParser.processArgs(new String []{"-arg1"});
        assertTrue(m_argHandler.getArg1());
    }
    
    @Test
    public void testFindOption() {
        assertNotNull(m_argParser.findOptionByShortName("arg1"));
        assertNotNull(m_argParser.findOptionByLongName("argumentOne"));
    }
    @Test
    public void testFindAndParseMultipleOptions() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
        m_argParser.processArgs(new String []{"--argumentTwo", "-arg1", "--argumentThree"});
        assertTrue(m_argHandler.getArg1());
        assertTrue(m_argHandler.getArg2());
        assertTrue(m_argHandler.getArg3());
        
        m_argHandler.reset();
        
        m_argParser.processArgs(new String []{"--argumentOne", "-arg3"});
        assertTrue(m_argHandler.getArg1());
        assertFalse(m_argHandler.getArg2());
        assertTrue(m_argHandler.getArg3());
        
        m_argHandler.reset();
        
        m_argParser.processArgs(new String []{"TestLogFile.log", "-arg1", "--argumentTwo"});
        assertTrue(m_argHandler.getArg1());
        assertTrue(m_argHandler.getArg2());
        assertFalse(m_argHandler.getArg3());
    }
    @Test
    public void testParseHelpOption() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m_argParser.processArgs(new String []  {"-h"});
        
    }
    @Test(expected=IllegalArgumentException.class)
    public void testParseInvalidArgument() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m_argParser.processArgs(new String [] {"-arg4"});
    }
    @Test
    public void testFileArgument() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        m_argParser.processArgs(new String [] {"-arg3", "--argumentTwo", "a", "b", "c", "d", "e"});
        assertEquals(" a b c d e", m_argHandler.getFileName());
        assertFalse(m_argHandler.getArg1());
        assertTrue(m_argHandler.getArg2());
        assertTrue(m_argHandler.getArg3());
    }
}
