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
package org.opennms.web.rest.v2.infopanel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.measurements.api.MeasurementsService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;

/**
 * Renders operator-authored Jinjava templates from {@code $OPENNMS_HOME/etc/infopanel/}
 * into structured {@link InfoPanelItem}s for a selected node. This is the
 * Vaadin-free counterpart of the legacy topology map's
 * {@code GenericInfoPanelItemProvider}: it builds an equivalent template context
 * (so existing templates render unchanged) but returns HTML rather than Vaadin
 * components, for the REST layer.
 *
 * <p>Node selections render with the {@code node}/{@code nodeResource}
 * context; edge selections render with the {@code edge} context (see
 * {@link EdgeInfo} for how the legacy {@code LinkdEdge} surface maps). The
 * legacy {@code vertex} variable is not populated; templates guard their use
 * with {@code vertex != null}, so vertex-only templates are simply not shown.
 */
public class InfoPanelRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(InfoPanelRenderer.class);

    private final NodeDao nodeDao;
    private final ResourceDao resourceDao;
    private final MeasurementsService measurementsService; // may be null
    private final Path templateDir;

    private final Jinjava jinjava;
    private final NodeDaoWrapper nodeDaoWrapper;
    private final ResourceDaoWrapper resourceDaoWrapper;

    public InfoPanelRenderer(final NodeDao nodeDao,
                             final ResourceDao resourceDao,
                             final MeasurementsService measurementsService,
                             final Path templateDir) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.resourceDao = Objects.requireNonNull(resourceDao);
        this.measurementsService = measurementsService;
        this.templateDir = Objects.requireNonNull(templateDir);

        this.jinjava = withClassLoaderFix(Jinjava::new);
        this.jinjava.getGlobalContext().registerFunction(
                new ELFunctionDefinition("System", "currentTimeMillis", System.class, "currentTimeMillis"));

        this.nodeDaoWrapper = new NodeDaoWrapper(nodeDao);
        this.resourceDaoWrapper = new ResourceDaoWrapper(resourceDao);
    }

    /**
     * Render every applicable {@code etc/infopanel/} template for the given node,
     * returning the visible, error-free items sorted by their {@code order}.
     * Returns an empty list when no template directory exists (the common case).
     */
    public List<InfoPanelItem> renderForNode(final OnmsNode node) {
        Objects.requireNonNull(node);
        return renderAll(createContext(node));
    }

    /**
     * Render every applicable template for the given edge ({@code edge} in the
     * template context, alongside the shared DAO/measurements helpers), same
     * visible/title/order contract as {@link #renderForNode}.
     */
    public List<InfoPanelItem> renderForEdge(final EdgeInfo edge) {
        Objects.requireNonNull(edge);
        final Map<String, Object> context = sharedContext();
        context.put("edge", edge);
        return renderAll(context);
    }

    private List<InfoPanelItem> renderAll(final Map<String, Object> context) {
        if (!Files.isDirectory(templateDir)) {
            return Collections.emptyList();
        }
        final List<InfoPanelItem> items = new ArrayList<>();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(templateDir, "*.html")) {
            for (final Path path : stream) {
                try {
                    final RenderResult result = render(path, context);
                    final boolean fatal = result.getErrors().stream()
                            .anyMatch(e -> e.getSeverity() == TemplateError.ErrorType.FATAL);
                    if (fatal) {
                        LOG.warn("Skipping info-panel template {} due to fatal render error(s): {}", path, result.getErrors());
                        continue;
                    }
                    if (Boolean.TRUE.equals(result.getContext().getOrDefault("visible", false))) {
                        final String title = String.valueOf(result.getContext().getOrDefault("title", "No title defined"));
                        // Templates control this value; tolerate non-numeric junk.
                        final Object rawOrder = result.getContext().getOrDefault("order", 0L);
                        final int order = rawOrder instanceof Number ? ((Number) rawOrder).intValue() : 0;
                        items.add(new InfoPanelItem(title, order, result.getOutput()));
                    }
                } catch (final IOException | RuntimeException e) {
                    // A broken template skips itself, never the whole request.
                    LOG.warn("Failed to render info-panel template {}: {}", path, e.getMessage());
                }
            }
        } catch (final IOException e) {
            LOG.warn("Failed to read info-panel template directory {}: {}", templateDir, e.getMessage());
            return Collections.emptyList();
        }
        items.sort(Comparator.comparingInt(InfoPanelItem::getOrder));
        return items;
    }

    private RenderResult render(final Path path, final Map<String, Object> context) throws IOException {
        // Fresh map per template: templates write visible/title/order into it.
        final Map<String, Object> templateContext = new HashMap<>(context);
        try (final Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            final String template = lines.collect(Collectors.joining("\n"));
            return withClassLoaderFix(() -> jinjava.renderForResult(template, templateContext));
        }
    }

    private Map<String, Object> createContext(final OnmsNode node) {
        final Map<String, Object> context = sharedContext();
        context.put("node", node);
        final OnmsResource resource = resourceDao.getResourceForNode(node);
        if (resource != null) {
            context.put("nodeResource", resource);
        }
        return context;
    }

    /** Context entries common to node- and edge-scoped renders. */
    private Map<String, Object> sharedContext() {
        final Map<String, Object> context = new HashMap<>();
        context.put("nodeDao", nodeDaoWrapper);
        context.put("resourceDao", resourceDaoWrapper);
        if (measurementsService != null) {
            context.put("measurements", new MeasurementsWrapper(measurementsService));
        }
        return context;
    }

    /**
     * Jinjava's JUEL expression engine resolves helper classes via the thread
     * context classloader; pin it to this class's loader for the duration of the
     * call so it finds the bundled dependencies (mirrors the legacy provider).
     */
    private static <T> T withClassLoaderFix(final Supplier<T> supplier) {
        final ClassLoader previous = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(InfoPanelRenderer.class.getClassLoader());
            return supplier.get();
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }
}
