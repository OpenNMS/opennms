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
// 2003 Aug 22: Added the ScriptD code.
//
// Copyright (C) 2003 Tavve Software Company.  All rights reserved.
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

package org.opennms.netmgt.scriptd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.netmgt.config.ScriptdConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.xml.event.Event;

/**
 * This class implements a script execution service. This service subscribes to
 * all events, and passes received events to the set of configured scripts.
 *
 * This services uses the Bean Scripting Framework (BSF) in order to allow
 * scripts to be written in a variety of registered languages.
 *
 * @author <a href="mailto:jim.doble@tavve.com">Jim Doble </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 * @author <a href="mailto:jim.doble@tavve.com">Jim Doble </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 * @version $Id: $
 */
public final class Scriptd extends AbstractServiceDaemon {

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
    	super("OpenNMS.Scriptd");
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
        } catch (Exception ex) {
            log().error("Failed to setup event reader", ex);
            throw new UndeclaredThrowableException(ex);
        }

        m_execution = new Executor(execQ, aFactory);
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
        } catch (Exception e) {
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
