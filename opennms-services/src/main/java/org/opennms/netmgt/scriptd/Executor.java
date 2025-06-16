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
package org.opennms.netmgt.scriptd;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.netmgt.config.ScriptdConfigFactory;
import org.opennms.netmgt.config.scriptd.Engine;
import org.opennms.netmgt.config.scriptd.EventScript;
import org.opennms.netmgt.config.scriptd.ReloadScript;
import org.opennms.netmgt.config.scriptd.StartScript;
import org.opennms.netmgt.config.scriptd.StopScript;
import org.opennms.netmgt.config.scriptd.Uei;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.IParm;
import org.opennms.netmgt.events.api.model.IScript;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used as a thread for launching scripts to handle received
 * events.
 * 
 * @author <a href="mailto:jim.doble@tavve.com">Jim Doble</a>
 * @author <a href="http://www.opennms.org"/>OpenNMS</a>
 */
public class Executor {

    private static final Logger LOG = LoggerFactory.getLogger(Executor.class);

    /**
     * The configured scripts (no UEI specified).
     */
    private final Set<EventScript> m_eventScripts = new CopyOnWriteArraySet<>();

    /**
     * The configured scripts (UEI specified).
     */
    private final Map<String,Set<EventScript>> m_eventScriptMap = new ConcurrentHashMap<>();

    /**
     * The DAO object for fetching nodes
     */
    private final NodeDao m_nodeDao;

    /**
     * The BSF manager
     */
    private BSFManager m_scriptManager = null;

    /**
     * The {@link ExecutorService} that will execute tasks for each
     * event.
     */
    private ExecutorService m_executorService;

    /**
     * The broadcast event receiver.
     */
    private BroadcastEventProcessor m_broadcastEventProcessor;

    /**
     * The configuration.
     */
    private ScriptdConfigFactory m_config;


    private SessionUtils m_sessionUtils;


    /**
     * @param config
     *            The <em>Scriptd</em> configuration.
     * @param nodeDao
     *            The <em>DAO</em> for fetching node information
     */
    Executor(ScriptdConfigFactory config, NodeDao nodeDao, SessionUtils sessionUtils) {

        m_config = config;

        m_nodeDao = nodeDao;

        m_sessionUtils = sessionUtils;

        loadConfig();
    }

    /**
     * Load the m_scripts and m_scriptMap data structures from the
     * configuration.
     * 
     * TODO: If we ever make Scriptd multithreaded then we will need
     * to handle synchronization for m_eventScripts and m_eventScriptMap
     * correctly.
     */
    private void loadConfig() {

        m_eventScripts.clear();
        m_eventScriptMap.clear();

        for (final EventScript script : m_config.getEventScripts()) {
            List<Uei> ueis = script.getUeis();

            if (ueis.isEmpty()) {
                m_eventScripts.add(script);
            } else {
                for (final Uei uei : ueis) {
                    final String ueiName = uei.getName();

                    Set<EventScript> list = m_eventScriptMap.get(ueiName);

                    if (list == null) {
                        list = new CopyOnWriteArraySet<>();
                        list.add(script);
                        m_eventScriptMap.put(ueiName, list);
                    } else {
                        list.add(script);
                    }
                }
            }
        }
    }

    public void addTask(IEvent event) {
        m_executorService.execute(new ScriptdRunnable(event));
    }

    private class ScriptdRunnable implements Runnable {

        private final IEvent m_event;

        public ScriptdRunnable(IEvent event) {
            m_event = event;
        }

        /**
         * The main worker of the fiber. This method is executed by the encapsulated
         * thread to read events from the execution queue and to execute any
         * configured scripts, allowing these scripts to react to the received
         * event. If the thread is interrupted then the method will return as quickly as
         * possible.
         */
        @Override
        public void run() {

            if (isReloadConfigEvent(m_event)) {
                // reloads are handled by Scriptd#handleReloadConfigEvent
                return;
            }

            if (m_config.getTransactional()) {
                m_sessionUtils.withReadOnlyTransaction(() -> {
                    executeEventScripts(m_event);
                    return null;
                });
            } else {
                executeEventScripts(m_event);
            }

        } // end run
    }

    protected void doReload() {
        try {
            ScriptdConfigFactory.reload();
            m_config = ScriptdConfigFactory.getInstance();
            registerEventProcessor();
            loadConfig();

            for (final ReloadScript script : m_config.getReloadScripts()) {
                final var scriptContent = script.getContent();
                if (scriptContent.isPresent()) {
                    try {
                        m_scriptManager.exec(script.getLanguage(), "", 0, 0, scriptContent.get());
                    } catch (BSFException e) {
                        LOG.error("Reload script[{}] failed.", script, e);
                    }
                } else {
                    LOG.warn("Reload Script does not have script contents: {}", script);
                }
            }

            LOG.debug("Scriptd configuration reloaded");
        } catch (final Exception e) {
            LOG.error("Unable to reload Scriptd configuration", e);
        }
    }

    private void executeEventScripts(IEvent m_event) {

        IScript[] attachedScripts = m_event.getScript();

        Set<EventScript> mapScripts = null;

        try {
            mapScripts = m_eventScriptMap.get(m_event.getUei());
        } catch (Throwable e) {
            LOG.warn("Unexpected exception: " + e.getMessage(), e);
        }

        if (attachedScripts.length > 0 || mapScripts != null || m_eventScripts.size() > 0) {
            LOG.debug("Executing scripts for: {}", m_event.getUei());

            m_scriptManager.registerBean("event", m_event);

            // And the event's node to the script context
            OnmsNode node = null;

            if (m_event.hasNodeid()) {
                Long nodeLong = m_event.getNodeid();
                Integer nodeInt = Integer.valueOf(nodeLong.intValue());
                node = m_nodeDao.get(nodeInt);
                m_scriptManager.registerBean("node", node);
            }

            // execute the scripts attached to the event

            LOG.debug("Executing attached scripts");
            if (attachedScripts.length > 0) {
                for (final IScript script : attachedScripts) {
                    try {
                        m_scriptManager.exec(script.getLanguage(), "", 0, 0, script.getContent());
                    } catch (BSFException e) {
                        LOG.error("Attached script [{}] execution failed", script, e);
                    }
                }
            }

            // execute the scripts mapped to the UEI

            LOG.debug("Executing mapped scripts");
            if (mapScripts != null) {
                for (final EventScript script : mapScripts) {
                    if (script.getContent().isPresent()) {
                        try {
                            m_scriptManager.exec(script.getLanguage(), "", 0, 0, script.getContent().get());
                        } catch (BSFException e) {
                            LOG.error("UEI-specific event handler script execution failed: {}", m_event.getUei(), e);
                        }
                    } else {
                        LOG.warn("UEI-specific event handler script missing contents: {}", script);
                    }
                }
            }

            // execute the scripts that are not mapped to any UEI

            LOG.debug("Executing global scripts");
            for (final EventScript script : m_eventScripts) {
                if (script.getContent().isPresent()) {
                    try {
                        m_scriptManager.exec(script.getLanguage(), "", 0, 0, script.getContent().get());
                    } catch (BSFException e) {
                        LOG.error("Non-UEI-specific event handler script execution failed : " + script, e);
                    }
                } else {
                    LOG.warn("Non-UEI-specific event handler script missing contents: {}", script);
                }
            }

            if (node != null) {
                m_scriptManager.unregisterBean("node");
            }

            m_scriptManager.unregisterBean("event");

            LOG.debug("Finished executing scripts for: {}", m_event.getUei());
        }

    }


    protected static boolean isReloadConfigEvent(IEvent event) {
        boolean isTarget = false;
        
        if (EventConstants.RELOAD_DAEMON_CONFIG_UEI.equals(event.getUei())) {
            List<IParm> parmCollection = event.getParmCollection();
            
            for (IParm parm : parmCollection) {
                if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && Scriptd.NAME.equalsIgnoreCase(parm.getValue().getContent())) {
                    isTarget = true;
                    break;
                }
            }
        } else if ("uei.opennms.org/internal/reloadScriptConfig".equals(event.getUei())) {
            // Deprecating this one...
            isTarget = true;
        }
        
        return isTarget;
    }

    public synchronized void start() {

        for (final Engine engine : m_config.getEngines()) {

            LOG.debug("Registering engine: {}", engine.getLanguage());

            String[] extensions = null;

            if (engine.getExtensions().isPresent()) {
                StringTokenizer st = new StringTokenizer(engine.getExtensions().get());

                extensions = new String[st.countTokens()];

                int j = 0;

                while (st.hasMoreTokens()) {
                    extensions[j++] = st.nextToken();
                }
            }

            BSFManager.registerScriptingEngine(engine.getLanguage(), engine.getClassName(), extensions);
        }

        m_scriptManager = new BSFManager();
        m_scriptManager.registerBean("log", LOG);

        // Run all start scripts
        for (final StartScript startScript : m_config.getStartScripts()) {
            if (startScript.getContent().isPresent()) {
                try {
                    m_scriptManager.exec(startScript.getLanguage(), "", 0, 0, startScript.getContent().get());
                } catch (BSFException e) {
                    LOG.error("Start script failed: " + startScript, e);
                }
            } else {
                LOG.warn("Start script has no script content: " + startScript);
            }
        }

        // Start the thread pool
        m_executorService = Executors.newFixedThreadPool(1, new LogPreservingThreadFactory("Scriptd-Executor", 1));

        registerEventProcessor();

        LOG.debug("Scriptd executor started");
    }

    protected void registerEventProcessor() {
        if (m_broadcastEventProcessor != null) {
            // if there's an existing processor, close it, just in case
            m_broadcastEventProcessor.close();
        }

        // Register the event listener after the thread pool has been started
        try {
            m_broadcastEventProcessor = new BroadcastEventProcessor(this);
        } catch (final Exception e) {
            LOG.error("Failed to setup event reader", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    public synchronized void stop() {

        // Shut down the event listener
        if (m_broadcastEventProcessor != null) {
            m_broadcastEventProcessor.close();
        }

        m_broadcastEventProcessor = null;

        // Shut down the thread pool
        m_executorService.shutdown();

        // Run all stop scripts
        for (final StopScript stopScript : m_config.getStopScripts()) {
            if (stopScript.getContent().isPresent()) {
                try {
                    m_scriptManager.exec(stopScript.getLanguage(), "", 0, 0, stopScript.getContent().get());
                } catch (BSFException e) {
                    LOG.error("Stop script failed: " + stopScript, e);
                }
            } else {
                LOG.warn("Stop script has no script contents: " + stopScript);
            }
        }

        LOG.debug("Scriptd executor stopped");
    }
}
