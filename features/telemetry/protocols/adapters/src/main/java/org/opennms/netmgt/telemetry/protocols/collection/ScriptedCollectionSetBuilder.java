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
package org.opennms.netmgt.telemetry.protocols.collection;

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
import java.util.Date;
import java.util.Map;

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
     * @param props
     *            additional global properties to pass into the script
     * @return a collection set
     * @throws ScriptException
     */
    public CollectionSet build(CollectionAgent agent, Object message, Long timestamp, Map<String,Object> props) throws ScriptException {
        final CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        if (timestamp != null && timestamp > 0) {
            builder.withTimestamp(new Date(timestamp));
        }
        final SimpleBindings globals = new SimpleBindings();
        globals.put("agent", agent);
        globals.put("builder", builder);
        globals.put("msg", message);

        if (props != null && !props.isEmpty()) {
            for (String key : props.keySet()) {
                globals.put(key, props.get(key));
            }
        }

        compiledScript.eval(globals);
        return builder.build();
    }

    public CollectionSet build(CollectionAgent agent, Object message, Long timestamp) throws ScriptException {
        return build(agent, message, timestamp, null);
    }
}
