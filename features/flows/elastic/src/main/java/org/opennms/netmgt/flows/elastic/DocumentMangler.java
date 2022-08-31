/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.commons.beanutils.BeanMap;
import org.opennms.core.fileutils.FileUpdateWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.swrve.ratelimitedlogger.RateLimitedLog;

public class DocumentMangler {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentMangler.class);
    private static final Logger RL_LOG = RateLimitedLog.withRateLimit(LOG)
                                                      .maxRate(5).every(Duration.ofSeconds(30))
                                                      .build();

    private final ScriptEngineManager sem;

    private Path path;
    private FileUpdateWatcher watcher;

    private CompiledScript script;

    public DocumentMangler(final ScriptEngineManager sem) {
        this.sem = Objects.requireNonNull(sem);
    }

    public Path getPath() {
        return this.path;
    }

    public void setPath(final Path path) throws IOException {
        this.path = path;

        if (this.path != null) {
            if (Files.exists(this.path)) {
                this.watcher = new FileUpdateWatcher(this.path.toAbsolutePath().toString(), this::reload);
                this.reload();
                return;
            } else {
                LOG.warn("Flow mangle script not found: {}", this.path.toString());
            }
        }

        this.watcher = null;
        this.script = null;
    }

    public void setPath(final String path) throws IOException {
        this.setPath(Strings.isNullOrEmpty(path)
                     ? null
                     : Paths.get(path));
    }

    private void reload() {
        LOG.info("Loading flow mangle script: {}", this.path);

        final var ext = com.google.common.io.Files.getFileExtension(this.path.toString());

        LOG.debug("Searching engine for file extension {}", ext);
        final ScriptEngine engine = this.sem.getEngineByExtension(ext);
        if (engine == null) {
            LOG.error("No engine found for extension: {}", ext);
            this.script = null;
            return;
        }

        LOG.debug("Using engine: {}", engine.getClass());

        if (!(engine instanceof Compilable)) {
            LOG.error("Only engines that can compile scripts are supported");
            this.script = null;
            return;
        }

        final Compilable compilable = (Compilable) engine;

        try (final var reader = Files.newBufferedReader(this.path)) {
            this.script = compilable.compile(reader);
        } catch (final IOException | ScriptException e) {
            LOG.error("Failed to load script", e);
            this.script = null;
            return;
        }

        LOG.info("Script loaded successfully");
    }

    public FlowDocument mangle(final FlowDocument flow) {
        if (this.script == null) {
            return flow;
        }

        try {
            final var globals = new SimpleBindings();
            globals.put("flow", flow);

            final var result = this.script.eval(globals);

            if (result == null){
                return null;
            } else if (result instanceof FlowDocument) {
                return (FlowDocument) result;
            } else {
                RL_LOG.error("Mangle script returns garbage: {}", result.getClass());
                return null;
            }
        } catch (final ScriptException e) {
            RL_LOG.error("Failed to execute mangle script", e);
            return null;
        }
    }

    public void destroy() {
        if (this.watcher != null) {
            this.watcher.destroy();
        }
    }
}
