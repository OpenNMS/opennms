/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
/*
 *   Copyright 2005 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.opennms.features.telemetry.adpaters.collection.script;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 * This is a wrapper class for the ScriptEngineFactory class that deals with context class loader issues
 * It is necessary because engines (at least ruby) use the context classloader to find their resources (i.e., their "native" classes)
 *
 */
public class OSGiScriptEngineFactory implements ScriptEngineFactory{
    private ScriptEngineFactory factory;
    private ClassLoader contextClassLoader;
    public OSGiScriptEngineFactory (ScriptEngineFactory factory, ClassLoader contextClassLoader){
        this.factory=factory;
        this.contextClassLoader=contextClassLoader;
    }
    public String getEngineName() {
        return factory.getEngineName();
    }
    public String getEngineVersion() {
        return factory.getEngineVersion();
    }
    public List<String> getExtensions() {
        return factory.getExtensions();
    }
    public String getLanguageName() {
        return factory.getLanguageName();
    }
    public String getLanguageVersion() {
        return factory.getLanguageVersion();
    }
    public String getMethodCallSyntax(String obj, String m, String... args) {
        return factory.getMethodCallSyntax(obj, m, args);
    }
    public List<String> getMimeTypes() {
        return factory.getMimeTypes();
    }
    public List<String> getNames() {
        return factory.getNames();
    }
    public String getOutputStatement(String toDisplay) {
        return factory.getOutputStatement(toDisplay);
    }
    public Object getParameter(String key) {
        return factory.getParameter(key);
    }
    public String getProgram(String... statements) {
        return factory.getProgram(statements);
    }
    public ScriptEngine getScriptEngine() {
        ScriptEngine engine=null;
        if(contextClassLoader!=null){
        ClassLoader old=Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(contextClassLoader);
        engine=factory.getScriptEngine();
        Thread.currentThread().setContextClassLoader(old);
        }
        else engine=factory.getScriptEngine();
        return engine;
    }
    

}
