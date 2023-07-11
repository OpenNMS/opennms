/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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
