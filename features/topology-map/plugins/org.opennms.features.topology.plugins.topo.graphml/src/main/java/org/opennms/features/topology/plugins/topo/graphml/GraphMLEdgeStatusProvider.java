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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.opennms.features.topology.api.info.MeasurementsWrapper;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.SimpleConnector;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.measurements.api.MeasurementsService;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

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

public class GraphMLEdgeStatusProvider implements EdgeStatusProvider {

    private final static Logger LOG = LoggerFactory.getLogger(GraphMLEdgeStatusProvider.class);

    private final static Path DIR = Paths.get(System.getProperty("opennms.home"), "etc", "graphml-edge-status");

    private final GraphMLTopologyProvider provider;
    private final ScriptEngineManager scriptEngineManager;
    private final TransactionOperations transactionOperations;
    private final NodeDao nodeDao;
    private final SnmpInterfaceDao snmpInterfaceDao;
    private final MeasurementsWrapper measurementsWrapper;

    public GraphMLEdgeStatusProvider(final GraphMLTopologyProvider provider,
                                     final ScriptEngineManager scriptEngineManager,
                                     final TransactionOperations transactionOperations,
                                     final NodeDao nodeDao,
                                     final SnmpInterfaceDao snmpInterfaceDao,
                                     final MeasurementsService measurementsService) {
        this.provider = Objects.requireNonNull(provider);
        this.scriptEngineManager = Objects.requireNonNull(scriptEngineManager);
        this.transactionOperations = Objects.requireNonNull(transactionOperations);
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.snmpInterfaceDao = Objects.requireNonNull(snmpInterfaceDao);
        this.measurementsWrapper = new MeasurementsWrapper(Objects.requireNonNull(measurementsService));
    }


    private class StatusScript {

        private final ScriptEngine engine;
        private final String source;

        private Optional<CompiledScript> compiledScript = null;

        private StatusScript(final ScriptEngine engine,
                             final String source) {
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

    private OnmsNode getNodeForEdgeVertexConnector(SimpleConnector simpleConnector) {
        if (simpleConnector != null && simpleConnector.getVertex() instanceof AbstractVertex) {
            AbstractVertex abstractVertex = (AbstractVertex) simpleConnector.getVertex();
            if (abstractVertex.getNodeID() != null) {
                return nodeDao.get(abstractVertex.getNodeID());
            }
        }
        return null;
    }

    private GraphMLEdgeStatus computeEdgeStatus(final List<StatusScript> scripts, final GraphMLEdge edge) {
        return scripts.stream()
                      .flatMap(script -> {
                          final StringWriter writer = new StringWriter();

                          final ScriptContext context = new SimpleScriptContext();
                          context.setWriter(writer);

                          SimpleBindings bindings = new SimpleBindings();

                          bindings.put("edge", edge);

                          bindings.put("sourceNode", getNodeForEdgeVertexConnector(edge.getSource()));
                          bindings.put("targetNode", getNodeForEdgeVertexConnector(edge.getTarget()));
                          bindings.put("measurements", measurementsWrapper);
                          bindings.put("nodeDao", nodeDao);
                          bindings.put("snmpInterfaceDao", snmpInterfaceDao);

                          context.setBindings(bindings,
                                              ScriptContext.GLOBAL_SCOPE);

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
                      .reduce((s1, s2) -> new GraphMLEdgeStatus(s1.getSeverity().isGreaterThan(s2.getSeverity())
                                                                    ? s1.getSeverity()
                                                                    : s2.getSeverity(),
                                                                ImmutableMap.<String, String>builder()
                                                                        .putAll(s1.getStyleProperties())
                                                                        .putAll(s2.getStyleProperties())
                                                                        .build()))
                      .orElse(null);
    }

    @Override
    public Map<EdgeRef, Status> getStatusForEdges(EdgeProvider edgeProvider, Collection<EdgeRef> edges, Criteria[] criteria) {
        final List<StatusScript> scripts = Lists.newArrayList();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(DIR)) {
            for (final Path path : stream) {
                final String extension = FilenameUtils.getExtension(path.toString());
                final ScriptEngine scriptEngine = this.scriptEngineManager.getEngineByExtension(extension);
                if (scriptEngine == null) {
                    LOG.warn("No script engine found for extension '{}'", extension);
                    continue;
                }

                LOG.debug("Found script: path={}, extension={}, engine={}", path, extension, scriptEngine);

                final String source = Files.lines(path, Charset.defaultCharset())
                                           .collect(Collectors.joining("\n"));

                scripts.add(new StatusScript(scriptEngine, source));
            }
        } catch (final IOException e) {
            LOG.error("Failed to walk template directory: {}", DIR);
            return Collections.emptyMap();
        }

        return this.transactionOperations.execute(transactionStatus -> {
            return edges.stream()
                        .filter(eachEdge -> eachEdge instanceof GraphMLEdge)
                        .map(edge -> (GraphMLEdge) edge)
                        .map(edge -> new HashMap.SimpleEntry<>(edge, computeEdgeStatus(scripts, edge)))
                        .filter(e -> e.getValue() != null)
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                                  Map.Entry::getValue));
        });
    }

    @Override
    public String getNamespace() {
        return provider.getVertexNamespace();
    }

    @Override
    public boolean contributesTo(String namespace) {
        return getNamespace().equals(namespace);
    }
}
