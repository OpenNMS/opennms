/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.scriptd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.log4j.Category;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.ScriptdConfigFactory;
import org.opennms.netmgt.config.scriptd.Engine;
import org.opennms.netmgt.config.scriptd.EventScript;
import org.opennms.netmgt.config.scriptd.ReloadScript;
import org.opennms.netmgt.config.scriptd.StartScript;
import org.opennms.netmgt.config.scriptd.StopScript;
import org.opennms.netmgt.config.scriptd.Uei;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Script;

/**
 * This class is used as a thread for launching scripts to handle received
 * events.
 * 
 * @author <a href="mailto:jim.doble@tavve.com">Jim Doble</a>
 * @author <a href="http://www.opennms.org"/>OpenNMS</a>
 * 
 */
final class Executor implements Runnable, PausableFiber {
    /**
     * The input queue of events.
     */
    private final FifoQueue<Event> m_execQ;

    /**
     * The worker thread that executes the <code>run</code> method.
     */
    private Thread m_worker;

    /**
     * The name of this Fiber
     */
    private final String m_name;

    /**
     * The status of this fiber.
     */
    private int m_status;

    /**
     * The configuration.
     */
    private ScriptdConfigFactory m_config;

    /**
     * The configured scripts (no UEI specified).
     */
    private ArrayList<EventScript> m_eventScripts;

    /**
     * The configured scripts (UEI specified).
     */
    private Map<String,List<EventScript>> m_eventScriptMap;

    /**
     * The BSF manager
     */
    private BSFManager m_mgr;

    /**
     * The DAO object for fetching nodes
     */
    private NodeDao m_nodeDao;

    /**
     * Constructs a new action daemon execution environment. The constructor
     * takes three arguments that define the source of commands to be executed and
     * the maximum time that a command may run.
     * 
     * @param execQ
     *            The execution queue
     * @param config
     *            The <em>Scriptd</em> configuration.
     * @param nodeDao
     *            The <em>DAO</em> for fetching node information
     */
    Executor(FifoQueue<Event> execQ, ScriptdConfigFactory config, NodeDao nodeDao) {
        m_execQ = execQ;
        m_config = config;

        loadConfig();

        m_worker = null;
        m_name = "Scriptd-Executor";
        m_mgr = null;
        m_status = START_PENDING;

	m_nodeDao = nodeDao;
    }

    /**
     * Load the m_scripts and m_scriptMap data structures from the
     * configuration.
     */
    private void loadConfig() {

        EventScript[] scripts = m_config.getEventScripts();

        m_eventScripts = new ArrayList<EventScript>();
        m_eventScriptMap = new ConcurrentHashMap<String,List<EventScript>>();

        for (int i = 0; i < scripts.length; i++) {
            Uei[] ueis = scripts[i].getUei();

            if (ueis.length == 0) {
                m_eventScripts.add(scripts[i]);
            } else {
                for (int j = 0; j < ueis.length; j++) {

                    String uei = ueis[j].getName();

                    List<EventScript> list = m_eventScriptMap.get(uei);

                    if (list == null) {
                        list = new ArrayList<EventScript>();
                        list.add(scripts[i]);
                        m_eventScriptMap.put(uei, list);
                    } else {
                        list.add(scripts[i]);
                    }
                }
            }
        }
    }

    /**
     * The main worker of the fiber. This method is executed by the encapsulated
     * thread to read events from the execution queue and to execute any
     * configured scripts, allowing these scripts to react to the received
     * event. If the thread is interrupted or the status changes to
     * <code>STOP_PENDING</code> then the method will return as quickly as
     * possible.
     */
    public void run() {
        ThreadCategory log = ThreadCategory.getInstance(Executor.class);

        synchronized (this) {
            m_status = RUNNING;
        }

        for (;;) {
            synchronized (this) {
                // if stopped or stop pending then break out

                if (m_status == STOP_PENDING || m_status == STOPPED) {
                    break;
                }

                // if paused or pause pending then block

                while (m_status == PAUSE_PENDING || m_status == PAUSED) {
                    m_status = PAUSED;
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        // exit
                        break;
                    }
                }

                // if resume pending then change to running

                if (m_status == RESUME_PENDING) {
                    m_status = RUNNING;
                }
            }

            // Extract the next event

            Event event = null;
            try {
                event = m_execQ.remove(1000);
                if (event == null) // status check time
                {
                    continue; // goto top of loop
                }
            } catch (InterruptedException ex) {
                break;
            } catch (FifoQueueException ex) {
                log.warn("The input event queue has errors, exiting...", ex);
                break;
            }

            // check for reload event
            if (isReloadConfigEvent(event)) {
                try {
                    ScriptdConfigFactory.reload();
                    m_config = ScriptdConfigFactory.getInstance();
                    loadConfig();

                    ReloadScript[] reloadScripts = m_config.getReloadScripts();

                    for (int i = 0; i < reloadScripts.length; i++) {
                        try {
                            m_mgr.exec(reloadScripts[i].getLanguage(), "", 0, 0, reloadScripts[i].getContent());
                        }

                        catch (BSFException ex) {
                            log.error("Reload script[" + i + "] failed.", ex);
                        }
                    }

                    log.debug("Script configuration reloaded");
                }

                catch (Throwable ex) {
                    log.error("Unable to reload ScriptD configuration: ", ex);
                }
            }

            Script[] attachedScripts = event.getScript();

            List<EventScript> mapScripts = null;

            try {
                mapScripts = m_eventScriptMap.get(event.getUei());
            }

            catch (Throwable ex) {
            }

            if (attachedScripts.length > 0 || mapScripts != null || m_eventScripts.size() > 0) {
                log.debug("Executing scripts for: " + event.getUei());

                m_mgr.registerBean("event", event);

                // And the events node
                OnmsNode node = null;

                if (event.hasNodeid()) {
                    Long nodeLong = event.getNodeid();
                    Integer nodeInt = Integer.valueOf(nodeLong.intValue());
                    node = m_nodeDao.get(nodeInt);
                    m_mgr.registerBean("node", node);
                }

                // execute the scripts attached to the event

                log.debug("Executing attached scripts");
                if (attachedScripts.length > 0) {
                    for (int i = 0; i < attachedScripts.length; i++) {
                        try {
                            Script script = attachedScripts[i];
                            m_mgr.exec(script.getLanguage(), "", 0, 0, script.getContent());
                        }

                        catch (BSFException ex) {
                            log.error("Attached script [" + i + "] execution failed", ex);
                        }
                    }
                }

                // execute the scripts mapped to the UEI

                log.debug("Executing mapped scripts");
                if (mapScripts != null) {
                    for (int i = 0; i < mapScripts.size(); i++) {
                        try {
                            EventScript script = (EventScript) mapScripts.get(i);
                            m_mgr.exec(script.getLanguage(), "", 0, 0, script.getContent());
                        }

                        catch (BSFException ex) {
                            log.error("UEI-specific event handler script execution failed: " + event.getUei(), ex);
                        }
                    }
                }

                // execute the scripts that are not mapped to any UEI

                log.debug("Executing global scripts");
                for (int i = 0; i < m_eventScripts.size(); i++) {
                    try {
                        EventScript script = (EventScript) m_eventScripts.get(i);
                        m_mgr.exec(script.getLanguage(), "", 0, 0, script.getContent());
                    }

                    catch (BSFException ex) {
                        log.error("Non-UEI-specific event handler script [" + i + "] execution failed", ex);
                    }
                }

		if (node != null)
		    m_mgr.unregisterBean("node");
		
                m_mgr.unregisterBean("event");

                log.debug("Finished executing scripts for: " + event.getUei());

            }
        } // end infinite loop

        synchronized (this) {
            m_status = STOPPED;
        }

    } // end run

    private boolean isReloadConfigEvent(Event event) {
        boolean isTarget = false;
        
        if (EventConstants.RELOAD_DAEMON_CONFIG_UEI.equals(event.getUei())) {
            List<Parm> parmCollection = event.getParmCollection();
            
            for (Parm parm : parmCollection) {
                if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Scriptd".equalsIgnoreCase(parm.getValue().getContent())) {
                    isTarget = true;
                    break;
                }
            }
        
        //Depreciating this one...
        } else if ("uei.opennms.org/internal/reloadScriptConfig".equals(event.getUei())) {
            isTarget = true;
        }
        
        return isTarget;
    }

    /**
     * Starts the fiber. If the fiber has already been run or is currently
     * running then an exception is generated. The status of the fiber is
     * updated to <code>STARTING</code> and will transition to <code>
     * RUNNING</code>
     * when the fiber finishes initializing and begins processing the
     * encapsulaed queue.
     *
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber is stopped or has never run.
     */
    public synchronized void start() {
        ThreadCategory log = ThreadCategory.getInstance(Executor.class);

        if (m_worker != null) {
            throw new IllegalStateException("The fiber has already been run");
        }

        m_status = STARTING;

        Engine[] engines = m_config.getEngines();

        for (int i = 0; i < engines.length; i++) {
            Engine engine = engines[i];

            log.debug("Registering engine: " + engine.getLanguage());

            String[] extensions = null;

            String extensionList = engines[i].getExtensions();

            if (extensionList != null) {
                StringTokenizer st = new StringTokenizer(extensionList);

                extensions = new String[st.countTokens()];

                int j = 0;

                while (st.hasMoreTokens()) {
                    extensions[j++] = st.nextToken();
                }
            }

            BSFManager.registerScriptingEngine(engines[i].getLanguage(), engines[i].getClassName(), extensions);
        }

        m_mgr = new BSFManager();
        m_mgr.registerBean("log", ThreadCategory.getInstance(Executor.class));

        StartScript[] startScripts = m_config.getStartScripts();

        for (int i = 0; i < startScripts.length; i++) {
            try {
                m_mgr.exec(startScripts[i].getLanguage(), "", 0, 0, startScripts[i].getContent());
            }

            catch (BSFException ex) {
                log.error("Start script[" + i + "] failed.", ex);
            }
        }

        m_worker = new Thread(this, getName());
        m_worker.start();
    }

    /**
     * Stops a currently running fiber. If the fiber has already been stopped
     * then the command is silently ignored. If the fiber was never started then
     * an exception is generated.
     *
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber was never started.
     */
    public synchronized void stop() {
        Category log = (Category) m_mgr.lookupBean("log");

        if (m_worker == null) {
            throw new IllegalStateException("The fiber has never been run");
        }

        if (m_status != STOPPED) {
            m_status = STOP_PENDING;
        }

        if (m_worker.isAlive()) {
            m_worker.interrupt();
        }

        StopScript[] stopScripts = m_config.getStopScripts();

        notifyAll();

        for (int i = 0; i < stopScripts.length; i++) {
            try {
                m_mgr.exec(stopScripts[i].getLanguage(), "", 0, 0, stopScripts[i].getContent());
            }

            catch (BSFException ex) {
                log.error("Stop script[" + i + "] failed.", ex);
            }
        }

        log.debug("Stopped");
    }

    /**
     * Pauses a currently running fiber. If the fiber was not in a running or
     * resuming state then the command is silently discarded. If the fiber is
     * not running or has terminated then an exception is generated.
     *
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber is stopped or has never run.
     */
    public synchronized void pause() {
        if (m_worker == null || !m_worker.isAlive()) {
            throw new IllegalStateException("The fiber is not running");
        }

        if (m_status == RUNNING || m_status == RESUME_PENDING) {
            m_status = PAUSE_PENDING;
            notifyAll();
        }
    }

    /**
     * Resumes the fiber if it is paused. If the fiber was not in a paused or
     * pause pending state then the request is discarded. If the fiber has not
     * been started or has already stopped then an exception is generated.
     *
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber is stopped or has never run.
     */
    public synchronized void resume() {
        if (m_worker == null || !m_worker.isAlive()) {
            throw new IllegalStateException("The fiber is not running");
        }

        if (m_status == PAUSED || m_status == PAUSE_PENDING) {
            m_status = RESUME_PENDING;
            notifyAll();
        }
    }

    /**
     * Returns the name of this fiber.
     *
     * @return The name of the fiber.
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the current status of the pausable fiber.
     *
     * @return The current status of the fiber.
     * @see org.opennms.core.fiber.PausableFiber
     * @see org.opennms.core.fiber.Fiber
     */
    public synchronized int getStatus() {
        if (m_worker != null && !m_worker.isAlive()) {
            m_status = STOPPED;
        }

        return m_status;
    }
}
