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

package org.opennms.netmgt.telemetry.adapters.collection;

import com.google.common.io.Files;

import org.opennms.features.osgi.script.OSGiScriptEngineManager;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.osgi.framework.BundleContext;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Uses an external script, executed via JSR-223, to generate a
 * {@link CollectionSet} from some given object using the
 * {@link CollectionSetBuilder}.
 *
 * @author jwhite
 */
public class ScriptedCollectionSetBuilder {

    private CompiledScript compiledScript;

    public ScriptedCollectionSetBuilder(File script) throws IOException, ScriptException {
        this(script, new ScriptEngineManager());
    }

    public ScriptedCollectionSetBuilder(File script, BundleContext bundleContext) throws IOException, ScriptException {
        this(script, new OSGiScriptEngineManager(bundleContext));
    }

    public ScriptedCollectionSetBuilder(File script, ScriptEngineManager manager) throws IOException, ScriptException {
        if (!script.canRead()) {
            throw new IllegalStateException("Cannot read script at '" + script + "'.");
        }

        final String ext = Files.getFileExtension(script.getAbsolutePath());

        final ScriptEngine engine = manager.getEngineByExtension(ext);
        if (engine == null) {
            throw new IllegalStateException("No engine found for extension: " + ext);
        }

        if (!(engine instanceof Compilable)) {
            throw new IllegalStateException("Only engines that can compile scripts are supported.");
        }
        final Compilable compilable = (Compilable) engine;
        try (FileReader reader = new FileReader(script)) {
            compiledScript = compilable.compile(reader);
        }
    }

    /**
     * Builds a collection set from the given message.
     *
     * WARNING: This method is not necessarily thread safe. This depends on the
     * script, and the script engine that is being used.
     *
     * @param agent
     *            the agent associated with the collection set
     * @param message
     *            the messaged passed to script containing the metrics
     * @return a collection set
     * @throws ScriptException
     */
    public CollectionSet build(CollectionAgent agent, Object message) throws ScriptException {
        final CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        final SimpleBindings globals = new SimpleBindings();
        globals.put("agent", agent);
        globals.put("builder", builder);
        globals.put("msg", message);
        compiledScript.eval(globals);
        return builder.build();
    }

}
