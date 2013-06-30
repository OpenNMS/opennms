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

package org.opennms.netmgt.actiond;

import java.lang.reflect.UndeclaredThrowableException;

import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.netmgt.config.ActiondConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to represent the auto action execution service. When an
 * event is received by this service that has one of either a notification,
 * trouble ticket, or auto action then a process is launched to execute the
 * appropriate commands.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 */
public final class Actiond extends AbstractServiceDaemon {
    
    private static final Logger LOG = LoggerFactory.getLogger(Actiond.class);
    
    /**
     * The singleton instance.
     */
    private static final Actiond m_singleton = new Actiond();

    /**
     * The execution launcher and reaper
     */
    private Executor m_executor;

    /**
     * The broadcast event receiver.
     */
    private BroadcastEventProcessor m_eventReader;

    private ActiondConfigFactory m_actiondConfig;

    /**
     * Constructs a new Action execution daemon.
     */
    private Actiond() {
    	super("actiond");
        m_executor = null;
        m_eventReader = null;
    }

	/**
	 * <p>onInit</p>
	 */
    @Override
	protected void onInit() {
		// A queue for execution
        //
        FifoQueue<String> execQ = new FifoQueueImpl<String>();

        // start the event reader
        //
        try {
            m_eventReader = new BroadcastEventProcessor(execQ);
        } catch (Throwable ex) {
            LOG.error("Failed to setup event reader", ex);
            throw new UndeclaredThrowableException(ex);
        }

        m_executor = new Executor(execQ, m_actiondConfig.getMaxProcessTime(), m_actiondConfig.getMaxOutstandingActions());
	}

    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {
		if (m_executor == null) {
		    init();
		}

		m_executor.start();
	}

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
		try {
            if (m_executor != null) {
                m_executor.stop();
            }
        } catch (Throwable e) {
        }

        if (m_eventReader != null) {
            m_eventReader.close();
        }

        m_eventReader = null;
        m_executor = null;
        m_actiondConfig = null;
	}

    /**
     * <p>onPause</p>
     */
    @Override
    protected void onPause() {
		m_executor.pause();
	}

    /**
     * <p>onResume</p>
     */
    @Override
    protected void onResume() {
		m_executor.resume();
	}

    /**
     * Returns the singular instance of the actiond daemon. There can be only
     * one instance of this service per virtual machine.
     *
     * @return a {@link org.opennms.netmgt.actiond.Actiond} object.
     */
    public static Actiond getInstance() {
        return m_singleton;
    }

    /**
     * <p>getEventReader</p>
     *
     * @return a {@link org.opennms.netmgt.actiond.BroadcastEventProcessor} object.
     */
    public BroadcastEventProcessor getEventReader() {
        return m_eventReader;
    }

    /**
     * <p>setEventReader</p>
     *
     * @param eventReader a {@link org.opennms.netmgt.actiond.BroadcastEventProcessor} object.
     */
    public void setEventReader(BroadcastEventProcessor eventReader) {
        m_eventReader = eventReader;
    }

    /**
     * <p>getExecutor</p>
     *
     * @return a {@link org.opennms.netmgt.actiond.Executor} object.
     */
    public Executor getExecutor() {
        return m_executor;
    }

    /**
     * <p>setExecutor</p>
     *
     * @param executor a {@link org.opennms.netmgt.actiond.Executor} object.
     */
    public void setExecutor(Executor executor) {
        m_executor = executor;
    }

    /**
     * <p>getActiondConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.ActiondConfigFactory} object.
     */
    public ActiondConfigFactory getActiondConfig() {
        return m_actiondConfig;
    }

    /**
     * <p>setActiondConfig</p>
     *
     * @param actiondConfig a {@link org.opennms.netmgt.config.ActiondConfigFactory} object.
     */
    public void setActiondConfig(ActiondConfigFactory actiondConfig) {
        m_actiondConfig = actiondConfig;
    }
    

}
