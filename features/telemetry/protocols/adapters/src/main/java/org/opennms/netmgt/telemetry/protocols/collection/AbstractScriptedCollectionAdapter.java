/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.collection;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.ScriptException;

import org.opennms.core.fileutils.FileUpdateCallback;
import org.opennms.core.fileutils.FileUpdateWatcher;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.osgi.framework.BundleContext;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;

public abstract class AbstractScriptedCollectionAdapter extends AbstractCollectionAdapter {

    private FileUpdateWatcher scriptUpdateWatcher;

    private String script;

    /*
     * Since ScriptCollectionSetBuilder is not thread safe , loading of script
     * is handled in ThreadLocal.
     */
    private final ThreadLocal<ScriptedCollectionSetBuilder> scriptedCollectionSetBuilders = new ThreadLocal<ScriptedCollectionSetBuilder>() {
        @Override
        protected ScriptedCollectionSetBuilder initialValue() {
            try {
                return loadCollectionBuilder(bundleContext, script);
            } catch (Exception e) {
                LOG.error("Failed to create builder for script '{}'.", script, e);
                return null;
            }
        }
    };

    /*
     * Flag to reload script if script didn't compile in earlier invocation,
     * need to be ThreadLocal as script itself loads in ThreadLocal.
     */
    private ThreadLocal<Boolean> scriptCompiled = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return true;
        }

    };

    /*
     * This map is needed since loading of script happens in a ThreadLocal and
     * status of script update needs to be propagated to each thread. This map
     * collects ScriptedCollectionSetBuilder and set it's value as false
     * initially. Whenever script updates and callback reload() gets called, all
     * values will be set to true to trigger reload of script in corresponding
     * thread, see getCollectionBuilder().
     */
    private Map<ScriptedCollectionSetBuilder, Boolean> scriptUpdateMap = new ConcurrentHashMap<>();

    public AbstractScriptedCollectionAdapter(final AdapterDefinition adapterConfig,
                                             final MetricRegistry metricRegistry) {
        super(adapterConfig, metricRegistry);
    }

    /*
     * This method checks and reloads script if there is an update else returns
     * existing builder
     */
    protected ScriptedCollectionSetBuilder getCollectionBuilder() {
        ScriptedCollectionSetBuilder builder = scriptedCollectionSetBuilders.get();
        // Reload script if reload() happened or earlier invocation of script didn't compile
        if ((builder != null && scriptUpdateMap.get(builder)) || !scriptCompiled.get()) {
            scriptedCollectionSetBuilders.remove();
            builder = scriptedCollectionSetBuilders.get();
        }
        if (builder == null) {
            // script didn't compile, set flag to false
            scriptCompiled.set(false);
            return null;
        } else if (!scriptCompiled.get()) {
            scriptCompiled.set(true);
        }
        return builder;
    }

    private ScriptedCollectionSetBuilder loadCollectionBuilder(BundleContext bundleContext, String script)
            throws IOException, ScriptException {
        ScriptedCollectionSetBuilder builder;
        if (bundleContext != null) {
            builder = new ScriptedCollectionSetBuilder(new File(script), bundleContext);
            scriptUpdateMap.put(builder, false);
            return builder;
        } else {
            builder = new ScriptedCollectionSetBuilder(new File(script));
            scriptUpdateMap.put(builder, false);
            return builder;
        }
    }

    private ScriptedCollectionSetBuilder checkScript(BundleContext bundleContext, String script)
            throws IOException, ScriptException {
        if (bundleContext != null) {
            return new ScriptedCollectionSetBuilder(new File(script), bundleContext);
        } else {
            return new ScriptedCollectionSetBuilder(new File(script));
        }
    }

    private void setFileUpdateCallback(String script) {
        if (!Strings.isNullOrEmpty(script)) {
            try {
                scriptUpdateWatcher = new FileUpdateWatcher(script, reloadScript());
            } catch (Exception e) {
                LOG.info("Script reload Utils is not registered", e);
            }
        }
    }

    private FileUpdateCallback reloadScript() {

        return new FileUpdateCallback() {
            /* Callback method for script update */
            @Override
            public void reload() {
                try {
                    checkScript(bundleContext, script);
                    LOG.debug("Updated script compiled");
                    // Set all the values in Map to true to trigger reload of script in all threads
                    scriptUpdateMap.replaceAll((builder, Boolean) -> true);
                } catch (Exception e) {
                    LOG.error("Updated script failed to build, using existing script'{}'.", script, e);
                }
            }

        };
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
        setFileUpdateCallback(script);
    }

    @Override
    public void destroy() {
        if (scriptUpdateWatcher != null) {
            scriptUpdateWatcher.destroy();
        }
    }
}
