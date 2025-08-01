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
package org.opennms.features.topology.app.internal.info;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.info.InfoPanelItemProvider;
import org.opennms.features.topology.api.info.MeasurementsWrapper;
import org.opennms.features.topology.api.info.item.InfoPanelItem;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.measurements.api.MeasurementsService;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.Label;

public class GenericInfoPanelItemProvider implements InfoPanelItemProvider {

    private final static Logger LOG = LoggerFactory.getLogger(GenericInfoPanelItemProvider.class);

    private final static Path DIR = Paths.get(System.getProperty("opennms.home", "."), "etc", "infopanel");

    // Workaround for OSGI-classloader-foo: Jinjava is using JUEL which
    // chooses the wrong classloader to load other dependencies. By
    // switching the classloader we can inject the correct one.
    private static <T> T withClassLoaderFix(final Supplier<T> supplier) {
        final ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(GenericInfoPanelItemProvider.class.getClassLoader());
            return supplier.get();
        } finally {
            Thread.currentThread().setContextClassLoader(previousClassLoader);
        }
    }

    private final NodeDao nodeDao;

    private final Jinjava jinjava;

    private final MeasurementsService measurementsService;

    public GenericInfoPanelItemProvider(NodeDao nodeDao, MeasurementsService measurementsService) {
        this.jinjava = withClassLoaderFix(Jinjava::new);
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.measurementsService = Objects.requireNonNull(measurementsService);

        this.jinjava.getGlobalContext().registerFunction(new ELFunctionDefinition("System", "currentTimeMillis", System.class, "currentTimeMillis"));
    }

    private class TemplateItem implements InfoPanelItem {

        final RenderResult renderResult;

        private TemplateItem(final RenderResult renderResult) {
            this.renderResult = renderResult;
        }

        @Override
        public Component getComponent() {
            return new Label(this.renderResult.getOutput(), ContentMode.HTML);
        }

        @Override
        public String getTitle() {
            return (String) this.renderResult.getContext().get("title", "No Title defined");
        }

        @Override
        public int getOrder() {
            return (int) (long) this.renderResult.getContext().get("order", 0L);
        }
    }

    private class ErrorItem implements InfoPanelItem {

        final Path path;
        final List<TemplateError> errors;

        private ErrorItem(final Path path,
                          final List<TemplateError> errors) {
            this.path = path;
            this.errors = errors;
        }

        @Override
        public Component getComponent() {
            final StringBuilder message = new StringBuilder();

            for (TemplateError error : this.errors) {
                message.append(error.getSeverity())
                        .append(": ")
                        .append(error.getMessage())
                        .append("@")
                        .append(error.getLineno())
                        .append("\n");

                // NMS-8660: Retrieve the full stack trace from the underlying exception if available
                if (error.getException() != null) {
                    StringWriter stackTraceStringWriter = new StringWriter();
                    PrintWriter stackTracePrintWriter = new PrintWriter(stackTraceStringWriter);
                    error.getException().printStackTrace(stackTracePrintWriter);
                    message.append(stackTraceStringWriter.toString());
                    message.append("\n");
                }
            }

            return new Label(message.toString(), ContentMode.PREFORMATTED);
        }

        @Override
        public String getTitle() {
            return "Error in " + this.path;
        }

        @Override
        public int getOrder() {
            return Integer.MIN_VALUE;
        }
    }

    @Override
    public Collection<InfoPanelItem> getContributions(final GraphContainer container) {
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(DIR, "*.html")) {
            final Set<InfoPanelItem> items = Sets.newHashSet();
            for (final Path path : stream) {
                try {
                    final RenderResult result = this.render(path, container);

                    if (Iterables.any(result.getErrors(),
                                      e -> e.getSeverity() == TemplateError.ErrorType.FATAL)) {
                        // Only show the errors to the user if there are real errors, ignoring warnings
                        items.add(new ErrorItem(path, result.getErrors()));

                    } else if ((Boolean) result.getContext().getOrDefault("visible", false)) {
                        items.add(new TemplateItem(result));
                    }

                } catch (final IOException e) {
                    LOG.error("Failed to load template: {}: {}", path, e);
                    return Collections.emptySet();
                }
            }
            return items;
        } catch (final IOException e) {
            LOG.error("Failed to walk template directory: {}", DIR);
            return Collections.emptySet();
        }
    }

    public Map<String, Object> createVertexContext(final VertexRef vertex) {
        final Map<String, Object> context = Maps.newHashMap();

        if (vertex instanceof AbstractVertex) {
            final AbstractVertex abstractVertex = (AbstractVertex) vertex;
            if (abstractVertex.getNodeID() != null) {
                final OnmsNode node = this.nodeDao.get(abstractVertex.getNodeID());
                if (node != null) {
                    context.put("node", node);
                }
            }
        }

        context.put("vertex", vertex);
        return context;
    }

    public Map<String, Object> createEdgeContext(final EdgeRef edge) {
        final Map<String, Object> context = Maps.newHashMap();

        context.put("edge", edge);
        return context;
    }

    private Map<String, Object> createContext(final GraphContainer container) {
        final Map<String, Object> context = Maps.newHashMap();

        Optional.ofNullable(Iterables.getOnlyElement(container.getSelectionManager().getSelectedEdgeRefs(), null))
                .map(this::createEdgeContext)
                .ifPresent(context::putAll);

        Optional.ofNullable(Iterables.getOnlyElement(container.getSelectionManager().getSelectedVertexRefs(), null))
                .map(this::createVertexContext)
                .ifPresent(context::putAll);

        context.put("measurements", new MeasurementsWrapper(measurementsService));

        return context;
    }

    private RenderResult render(final Path path,
                                final GraphContainer container) throws IOException {
        final Map<String, Object> context = this.createContext(container);

        try (final Stream<String> lines = Files.lines(path, Charset.defaultCharset())) {
            final String template = lines.collect(Collectors.joining("\n"));
            return withClassLoaderFix(() -> jinjava.renderForResult(template, context));
        }
    }
}
