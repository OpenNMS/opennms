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

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.netmgt.config.ScriptdConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.xml.event.Event;

import org.springframework.beans.factory.access.BeanFactoryReference;

/**
 * This class implements a script execution service. This service subscribes to
 * all events, and passes received events to the set of configured scripts.
 *
 * This services uses the Bean Scripting Framework (BSF) in order to allow
 * scripts to be written in a variety of registered languages.
 *
 * @author <a href="mailto:jim.doble@tavve.com">Jim Doble </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 */
public final class Scriptd extends AbstractServiceDaemon {

    /** Constant <code>NAME="OpenNMS.Scriptd"</code> */
    public static final String NAME = "OpenNMS.Scriptd";

    /**
     * The singleton instance.
     */
    private static final Scriptd m_singleton = new Scriptd();

    /**
     * The execution launcher
     */
    private Executor m_execution;

    /**
     * The broadcast event receiver.
     */
    private BroadcastEventProcessor m_eventReader;

    /**
     * Constructs a new Script execution daemon.
     */
    private Scriptd() {
    	super(NAME);
        m_execution = null;
        m_eventReader = null;
    }

    /**
     * Initialize the <em>Scriptd</em> service.
     */
    protected void onInit() {

        // Load the configuration information
        //
        ScriptdConfigFactory aFactory = null;

        try {
            ScriptdConfigFactory.reload();
            aFactory = ScriptdConfigFactory.getInstance();
        } catch (MarshalException ex) {
            log().error("Failed to load scriptd configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
            log().error("Failed to load scriptd configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
            log().error("Failed to load scriptd configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }

        // A queue for execution

        FifoQueue<Event> execQ = new FifoQueueImpl<Event>();

        // start the event reader

        try {
            m_eventReader = new BroadcastEventProcessor(execQ);
        } catch (Throwable ex) {
            log().error("Failed to setup event reader", ex);
            throw new UndeclaredThrowableException(ex);
        }

        // get the node DAO
        BeanFactoryReference bf = BeanUtils.getBeanFactory("daoContext");
        NodeDao nodeDao = BeanUtils.getBean(bf, "nodeDao", NodeDao.class);

        m_execution = new Executor(execQ, aFactory, nodeDao);
    }

    /**
     * <p>onStart</p>
     */
    protected void onStart() {
		if (m_execution == null) {
		    init();
		}

		m_execution.start();
		log().info("Scriptd running");
	}

    /**
     * <p>onStop</p>
     */
    protected void onStop() {
		try {
            if (m_execution != null) {
                m_execution.stop();
            }
        } catch (Throwable e) {
        }

        if (m_eventReader != null) {
            m_eventReader.close();
        }

        m_eventReader = null;
        m_execution = null;
	}

    /**
     * <p>onPause</p>
     */
    protected void onPause() {
		m_execution.pause();
	}

    /**
     * <p>onResume</p>
     */
    protected void onResume() {
		m_execution.resume();
	}

    /**
     * Returns the singular instance of the <em>Scriptd</em> daemon. There can
     * be only one instance of this service per virtual machine.
     *
     * @return The singular instance.
     */
    public static Scriptd getInstance() {
        return m_singleton;
    }
}
