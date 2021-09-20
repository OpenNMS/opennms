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

package org.opennms.util.ilr;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;


public class ArgumentParser {

    private String m_programName;
    private Object m_argHandler;
    private Method m_argMethod;
    private String m_argHelp;
    PrintStream out = System.err;
    private Map<Option, Method> m_options = new LinkedHashMap<Option, Method>();
     
    public ArgumentParser(String programName, Object argHandler) {
        m_programName = programName;
        m_argHandler = argHandler;  
        Method [] methods = m_argHandler.getClass().getMethods();
        for(Method m: methods){
            if(m.isAnnotationPresent(Option.class)){
                Option option = m.getAnnotation(Option.class);
                m_options.put(option, m);
            }
            if(m.isAnnotationPresent(Arguments.class)) {
                m_argMethod = m;
                m_argHelp = m.getAnnotation(Arguments.class).help();
            }
        }
    }
    public void processArgs(String[] args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        for(int i = 0 ; i < args.length ; i++){
            String arg = args[i];
            if (arg.equals("-h") || arg.equals("--help")){
                printHelpOptions();
            }
            else if(arg.startsWith("--")){
                Option o = findOptionByLongName(arg.substring(2));
                executeOption(o, arg);
            }
            else if(arg.startsWith("-")){
                Option o = findOptionByShortName(arg.substring(1));
                executeOption(o, arg);
            }else{
                m_argMethod.invoke(m_argHandler, arg);
            }
        }
        
    }
    public void printHelpOptions() {
        out.printf("Usage: %s <options> <arguments> %n", m_programName);
        out.printf("   where <arguments> is %s%n", m_argHelp);
        out.printf("Options: %n");
        out.printf("   -%-5s or --%-15s : %s%n", "h", "help", "print this help");
        for(Option o : m_options.keySet()){
            out.printf("   -%-5s or --%-15s : %s%n", o.shortName(), o.longName(), o.help());
        }
      
        
    }
    public void executeOption(Option o, String arg) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if(o != null){
            Method m = m_options.get(o);
            m.invoke(m_argHandler);      
        }else{
            throw new IllegalArgumentException("Illegal Argument: " + arg);
        }
    }
    public Option findOptionByShortName(String arg) {
        for(Option o : m_options.keySet()){
            if(arg.equals(o.shortName())){
                return o;
            }
        }
        return null;
    }
    public Option findOptionByLongName(String arg) {
        for(Option o : m_options.keySet()){
            if(arg.equals(o.longName())){
                return o;
            }
        }
        return null;
    }
}
