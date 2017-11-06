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
package org.opennms.features.osgi.script;

import java.io.Reader;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

public class OSGiScriptEngine implements ScriptEngine, Invocable {
    private ScriptEngine engine;
    private OSGiScriptEngineFactory factory;
    public OSGiScriptEngine(ScriptEngine engine, OSGiScriptEngineFactory factory){
        this.engine=engine;
        this.factory=factory;
    }
    @Override
    public Bindings createBindings() {
        return engine.createBindings();
    }
    @Override
    public Object eval(Reader reader, Bindings n) throws ScriptException {
        return engine.eval(reader, n);
    }
    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        return engine.eval(reader, context);
    }
    @Override
    public Object eval(Reader reader) throws ScriptException {
        return engine.eval(reader);
    }
    @Override
    public Object eval(String script, Bindings n) throws ScriptException {
        return engine.eval(script, n);
    }
    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        return engine.eval(script, context);
    }
    @Override
    public Object eval(String script) throws ScriptException {
        return engine.eval(script);
    }
    @Override
    public Object get(String key) {
        return engine.get(key);
    }
    @Override
    public Bindings getBindings(int scope) {
        return engine.getBindings(scope);
    }
    @Override
    public ScriptContext getContext() {
        return engine.getContext();
    }
    @Override
    public ScriptEngineFactory getFactory() {
        return factory;
    }
    @Override
    public void put(String key, Object value) {
        engine.put(key, value);
    }
    @Override
    public void setBindings(Bindings bindings, int scope) {
        engine.setBindings(bindings, scope);
    }
    @Override
    public void setContext(ScriptContext context) {
        engine.setContext(context);
    }
    @Override
    public Object invokeMethod(Object thiz, String name, Object... args) throws ScriptException, NoSuchMethodException {
        return getInvocableEngine().invokeMethod(thiz, name, args);
    }
    @Override
    public Object invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException {
        return getInvocableEngine().invokeFunction(name, args);
    }
    @Override
    public <T> T getInterface(Class<T> clasz) {
        return getInvocableEngine().getInterface(clasz);
    }
    @Override
    public <T> T getInterface(Object thiz, Class<T> clasz) {
        return getInvocableEngine().getInterface(thiz, clasz);
    }
    private Invocable getInvocableEngine() {
        return (Invocable)engine;
    }
}
