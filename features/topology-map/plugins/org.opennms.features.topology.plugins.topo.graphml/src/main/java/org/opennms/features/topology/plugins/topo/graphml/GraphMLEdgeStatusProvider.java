/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.graphml;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.opennms.features.topology.api.info.MeasurementsWrapper;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.SimpleConnector;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.plugins.topo.graphml.internal.GraphMLServiceAccessor;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class GraphMLEdgeStatusProvider implements EdgeStatusProvider {

    private final static Logger LOG = LoggerFactory.getLogger(GraphMLEdgeStatusProvider.class);

    /**
     * Representation for the script to execute.
     */
    private class StatusScript {

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

        public GraphMLEdgeStatus eval(final ScriptContext context) throws ScriptException {
            if (this.compiledScript == null) {
                if (this.engine instanceof Compilable) {
                    this.compiledScript = Optional.of(((Compilable) engine).compile(source));
                } else {
                    this.compiledScript = Optional.empty();
                }
            }
            if (this.compiledScript.isPresent()) {
                return (GraphMLEdgeStatus) this.compiledScript.get().eval(context);

            } else {
                return (GraphMLEdgeStatus) this.engine.eval(this.source, context);
            }
        }
    }

    private final Path scriptPath;
    private final GraphMLTopologyProvider provider;
    private final ScriptEngineManager scriptEngineManager;
    private final GraphMLServiceAccessor serviceAccessor;

    public GraphMLEdgeStatusProvider(final GraphMLTopologyProvider provider,
                                     final ScriptEngineManager scriptEngineManager,
                                     final GraphMLServiceAccessor serviceAccessor,
                                     final Path scriptPath) {
        this.provider = Objects.requireNonNull(provider);
        this.scriptEngineManager = Objects.requireNonNull(scriptEngineManager);
        this.serviceAccessor = Objects.requireNonNull(serviceAccessor);
        this.scriptPath = Objects.requireNonNull(scriptPath);
    }

    public GraphMLEdgeStatusProvider(final GraphMLTopologyProvider provider,
                                     final ScriptEngineManager scriptEngineManager,
                                     final GraphMLServiceAccessor serviceAccessor) {
        this(provider, scriptEngineManager, serviceAccessor, Paths.get(System.getProperty("opennms.home"), "etc", "graphml-edge-status"));
    }

    @Override
    public Map<EdgeRef, Status> getStatusForEdges(EdgeProvider edgeProvider, Collection<EdgeRef> edges, Criteria[] criteria) {
        final List<StatusScript> scripts = Lists.newArrayList();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(getScriptPath())) {
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
            LOG.error("Failed to walk template directory: {}", getScriptPath());
            return Collections.emptyMap();
        }

        return serviceAccessor.getTransactionOperations().execute(transactionStatus ->
            edges.stream()
                .filter(eachEdge -> eachEdge instanceof GraphMLEdge)
                .map(edge -> (GraphMLEdge) edge)
                .map(edge -> new HashMap.SimpleEntry<>(edge, computeEdgeStatus(scripts, edge)))
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }

    @Override
    public String getNamespace() {
        return provider.getVertexNamespace();
    }

    @Override
    public boolean contributesTo(String namespace) {
        return getNamespace().equals(namespace);
    }

    public Path getScriptPath() {
        return scriptPath;
    }

    private GraphMLEdgeStatus computeEdgeStatus(final List<StatusScript> scripts, final GraphMLEdge edge) {
        return scripts.stream()
                .flatMap(script -> {
                    final SimpleBindings bindings = createBindings(edge);
                    final StringWriter writer = new StringWriter();
                    final ScriptContext context = new SimpleScriptContext();
                    context.setWriter(writer);
                    context.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
                    try {
                        LOG.debug("Executing script: {}", script);
                        final GraphMLEdgeStatus status = script.eval(context);
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
                .reduce(GraphMLEdgeStatus::merge)
                .orElse(null);
    }

    private SimpleBindings createBindings(GraphMLEdge edge) {
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("edge", edge);
        bindings.put("sourceNode", getNodeForEdgeVertexConnector(edge.getSource()));
        bindings.put("targetNode", getNodeForEdgeVertexConnector(edge.getTarget()));
        bindings.put("measurements", new MeasurementsWrapper(serviceAccessor.getMeasurementsService()));
        bindings.put("nodeDao", serviceAccessor.getNodeDao());
        bindings.put("snmpInterfaceDao", serviceAccessor.getSnmpInterfaceDao());
        return bindings;
    }

    private OnmsNode getNodeForEdgeVertexConnector(SimpleConnector simpleConnector) {
        if (simpleConnector != null && simpleConnector.getVertex() instanceof AbstractVertex) {
            AbstractVertex abstractVertex = (AbstractVertex) simpleConnector.getVertex();
            if (abstractVertex.getNodeID() != null) {
                return serviceAccessor.getNodeDao().get(abstractVertex.getNodeID());
            }
        }
        return null;
    }
}
