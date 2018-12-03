/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import org.apache.commons.io.FilenameUtils;
import org.opennms.netmgt.flows.api.FlowSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class DocumentModifier {

    private final static Logger LOG = LoggerFactory.getLogger(DocumentModifier.class);

    private final static Path PATH = Paths.get(System.getProperty("opennms.home")).resolve("etc").resolve("flow-modifiers");

    /**
     * Representation for the script to execute.
     */
    private static class ModificationScript {
        private final ScriptEngine engine;
        private final String source;

        /**
         * Indicator if script has been compiled.
         * If null, not determined yet if compilable at all.
         */
        private Optional<CompiledScript> compiledScript = null;

        private ModificationScript(final ScriptEngine engine, final String source) {
            this.engine = Objects.requireNonNull(engine);
            this.source = Objects.requireNonNull(source);
        }

        public void eval(final ScriptContext context) throws ScriptException {
            if (this.compiledScript == null) {
                if (this.engine instanceof Compilable) {
                    this.compiledScript = Optional.of(((Compilable) engine).compile(source));
                } else {
                    this.compiledScript = Optional.empty();
                }
            }

            if (this.compiledScript.isPresent()) {
                this.compiledScript.get().eval(context);
            } else {
                this.engine.eval(this.source, context);
            }
        }
    }

    private final ScriptEngineManager scriptEngineManager;

    private List<ModificationScript> scripts;

    public DocumentModifier(final ScriptEngineManager scriptEngineManager) {
        this.scriptEngineManager = Objects.requireNonNull(scriptEngineManager);

        this.scripts = this.loadScripts();
    }

    private List<ModificationScript> loadScripts() {
        final List<ModificationScript> scripts = Lists.newArrayList();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(PATH)) {
            for (final Path path : stream) {
                final String extension = FilenameUtils.getExtension(path.toString());
                final ScriptEngine scriptEngine = this.scriptEngineManager.getEngineByExtension(extension);
                if (scriptEngine == null) {
                    LOG.warn("No script engine found for extension '{}'", extension);
                    continue;
                }

                LOG.debug("Found script: path={}, extension={}, engine={}", path, extension, scriptEngine);
                try (final Stream<String> lines = Files.lines(path, Charset.defaultCharset())) {
                    final String source = lines.collect(Collectors.joining("\n"));
                    scripts.add(new ModificationScript(scriptEngine, source));
                }
            }
        } catch (final IOException e) {
            LOG.error("Failed to walk script directory: " + PATH, e);
            return Collections.emptyList();
        }

        return scripts;
    }

    public void modify(final FlowDocument flowDocument, final FlowSource source) {
        final SimpleBindings bindings = new SimpleBindings();
        bindings.put("flow", flowDocument);
        bindings.put("source", source);

        for (final ModificationScript script : this.scripts) {
            final StringWriter writer = new StringWriter();
            final ScriptContext context = new SimpleScriptContext();
            context.setWriter(writer);
            context.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
            try {
                LOG.debug("Executing script: {}", script);
                script.eval(context);
            } catch (final ScriptException e) {
                LOG.error("Failed to execute script: {}", e);
            } finally {
                LOG.info(writer.toString());
            }
        }
    }
}
