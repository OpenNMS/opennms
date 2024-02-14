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
package org.opennms.netmgt.flows.processing.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Objects;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.opennms.core.fileutils.FileUpdateWatcher;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;
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

    public EnrichedFlow mangle(final EnrichedFlow flow) {
        if (this.script == null) {
            return flow;
        }

        try {
            final var globals = new SimpleBindings();
            globals.put("flow", flow);

            final var result = this.script.eval(globals);

            if (result == null){
                return null;
            } else if (result instanceof EnrichedFlow) {
                return (EnrichedFlow) result;
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
