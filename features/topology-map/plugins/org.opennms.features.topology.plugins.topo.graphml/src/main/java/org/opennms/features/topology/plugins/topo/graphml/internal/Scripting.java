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
