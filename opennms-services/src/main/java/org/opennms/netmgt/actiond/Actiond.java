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
package org.opennms.netmgt.actiond;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
        BlockingQueue<String> execQ = new LinkedBlockingQueue<>();

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
