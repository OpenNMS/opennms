//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.actiond;

import java.lang.reflect.UndeclaredThrowableException;

import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.netmgt.config.ActiondConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;

/**
 * This class is used to represent the auto action execution service. When an
 * event is received by this service that has one of either a notification,
 * trouble ticket, or auto action then a process is launched to execute the
 * appropriate commands.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 * @version $Id: $
 */
public final class Actiond extends AbstractServiceDaemon {

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
    	super("OpenNMS.Actiond");
        m_executor = null;
        m_eventReader = null;
    }

	/**
	 * <p>onInit</p>
	 */
	protected void onInit() {
		// A queue for execution
        //
        FifoQueue<String> execQ = new FifoQueueImpl<String>();

        // start the event reader
        //
        try {
            m_eventReader = new BroadcastEventProcessor(execQ);
        } catch (Exception ex) {
            log().error("Failed to setup event reader", ex);
            throw new UndeclaredThrowableException(ex);
        }

        m_executor = new Executor(execQ, m_actiondConfig.getMaxProcessTime(), m_actiondConfig.getMaxOutstandingActions());
	}

    /**
     * <p>onStart</p>
     */
    protected void onStart() {
		if (m_executor == null) {
		    init();
		}

		m_executor.start();
	}

    /**
     * <p>onStop</p>
     */
    protected void onStop() {
		try {
            if (m_executor != null) {
                m_executor.stop();
            }
        } catch (Exception e) {
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
    protected void onPause() {
		m_executor.pause();
	}

    /**
     * <p>onResume</p>
     */
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
