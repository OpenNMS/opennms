/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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
package org.opennms.features.topology.plugins.topo.graphml.internal;

import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.opennms.features.topology.api.topo.Ref;
import org.opennms.features.topology.api.topo.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Scripting<E extends Ref, S extends Status> {

    private final static Logger LOG = LoggerFactory.getLogger(Scripting.class);

    /**
     * Representation for the script to execute.
     */
    private static class StatusScript<S extends Status> {
        private final ScriptEngine engine;
        private final String source;

        /**
         * Indicator if script has been compiled.
         * If null, not determined yet if compilable at all.
         */
        private Optional<CompiledScript> compiledScript = null;

        private StatusScript(final ScriptEngine engine, final String source) {
            this.engine = Objects.requireNonNull(engine);
            this.source = Objects.requireNonNull(source);
        }

        public S eval(final ScriptContext context) throws ScriptException {
            if (this.compiledScript == null) {
                if (this.engine instanceof Compilable) {
                    this.compiledScript = Optional.of(((Compilable) engine).compile(source));
                } else {
                    this.compiledScript = Optional.empty();
                }
            }
            if (this.compiledScript.isPresent()) {
                return (S) this.compiledScript.get().eval(context);
            } else {
                return (S) this.engine.eval(this.source, context);
            }
        }
    }

    private final Path scriptPath;
    private final ScriptEngineManager scriptEngineManager;

    private final Supplier<S> statusDefault;
    private final BinaryOperator<S> statusAccumulator;

    public Scripting(final Path scriptPath,
                     final ScriptEngineManager scriptEngineManager,
                     final Supplier<S> statusDefault,
                     final BinaryOperator<S> statusAccumulator) {
        this.scriptPath = Objects.requireNonNull(scriptPath);
        this.scriptEngineManager = Objects.requireNonNull(scriptEngineManager);
        this.statusDefault = Objects.requireNonNull(statusDefault);
        this.statusAccumulator = Objects.requireNonNull(statusAccumulator);
    }

    private List<StatusScript<S>> loadScripts() {
        final List<StatusScript<S>> scripts = Lists.newArrayList();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(this.scriptPath)) {
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
                    scripts.add(new StatusScript(scriptEngine, source));
                }
            }
        } catch (final IOException e) {
            LOG.error("Failed to walk template directory: " + this.scriptPath, e);
            return Collections.emptyList();
        }

        return scripts;
    }

    private S computeStatus(final List<StatusScript<S>> scripts,
                            final E element,
                            final Function<E, SimpleBindings> refBindingMapper) {
        return scripts.stream()
                      .flatMap(script -> {
                          final SimpleBindings bindings = refBindingMapper.apply(element);
                          final StringWriter writer = new StringWriter();
                          final ScriptContext context = new SimpleScriptContext();
                          context.setWriter(writer);
                          context.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
                          try {
                              LOG.debug("Executing script: {}", script);
                              final S status = script.eval(context);
                              if (status != null) {
                                  return Stream.of(status);
                              } else {
                                  return Stream.empty();
                              }
                          } catch (final ScriptException e) {
                              LOG.error("Failed to execute script: {}", e);
                              return Stream.empty();
                          } finally {
                              LOG.info(writer.toString());
                          }
                      })
                      .filter(Objects::nonNull)
                      .reduce(this.statusAccumulator)
                      .orElseGet(this.statusDefault);
    }

    public Map<E, S> compute(final Stream<E> elements,
                             final Function<E, SimpleBindings> refBindingMapper) {
        final List<StatusScript<S>> scripts = this.loadScripts();

        return elements.map(element -> new HashMap.SimpleEntry<>(element, this.computeStatus(scripts, element, refBindingMapper)))
                       .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
