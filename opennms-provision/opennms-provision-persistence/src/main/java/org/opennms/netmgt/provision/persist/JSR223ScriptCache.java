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
package org.opennms.netmgt.provision.persist;

import com.google.common.io.Files;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to compile and cache scripts stored on the filesystem.
 *
 * Caching the compiled scripts, allows us to:
 *  1) Avoid having to compile them on every use and reuse them across multiple policy objects
 *       Compilation can be expensive and the resulting objects are normally thread safe.
 *  2) Avoid Groovy classloader issues
 *       Groovy leaks classes everytime we compile, so we can store them here to avoid compiling as much as possible
 *
 * @author jwhite
 */
public class JSR223ScriptCache {

    private final Map<String, ScriptState> m_scriptStates = new ConcurrentHashMap<>();

    private final ScriptEngineManager m_scriptManager = new ScriptEngineManager();

    /**
     * Compile the script at the given path and cache the result.
     * Reload and re-compile the script if the file has been modified it was last compiled.
     */
    public CompiledScript getCompiledScript(final File scriptFile) throws IOException, ScriptException {
        Objects.requireNonNull(scriptFile, "scriptFile must not be null");

        // Get or create state object
        ScriptState state = m_scriptStates.computeIfAbsent(scriptFile.getAbsolutePath(), k -> new ScriptState());

        // Compile if needed
        synchronized (state.lock) {
            long lastModified = scriptFile.lastModified();
            if (lastModified > state.lastCompiled) {
                final String fileExtension = Files.getFileExtension(scriptFile.getAbsolutePath());
                final ScriptEngine engine = m_scriptManager.getEngineByExtension(fileExtension);
                if (engine == null) {
                    throw new IllegalStateException("No engine found for file extension: " + fileExtension);
                }

                if (!(engine instanceof Compilable)) {
                    throw new IllegalStateException("Only engines that can compile scripts are supported: " +
                            engine.getClass().getCanonicalName());
                }
                final Compilable compilable = (Compilable) engine;
                try (FileReader reader = new FileReader(scriptFile)) {
                    state.compiledScript = compilable.compile(reader);
                }

                state.lastCompiled = lastModified;
            }
        }

        return state.compiledScript;
    }

    private static class ScriptState {
        private final Object lock = new Object();
        private long lastCompiled = -1;
        private CompiledScript compiledScript = null;
    }
}
