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
package org.opennms.netmgt.translator.jmx;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;

import org.apache.commons.lang.NotImplementedException;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.netmgt.config.EventTranslatorConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>EventTranslator class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 */
public class EventTranslator extends AbstractServiceDaemon implements EventTranslatorMBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(EventTranslator.class);

    /**
     * <p>Constructor for EventTranslator.</p>
     */
    public EventTranslator() {
        super(NAME);
    }

    public static final String NAME = "event-translator";

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {

        try {
            EventTranslatorConfigFactory.init();
        } catch (IOException e) {
        	LOG.error("IOException: ", e);
            throw new UndeclaredThrowableException(e);
        } catch (ClassNotFoundException e) {
		LOG.error("Unable to initialize database", e);
            throw new UndeclaredThrowableException(e);
        } catch (SQLException e) {
        	LOG.error("SQLException: ", e);
            throw new UndeclaredThrowableException(e);
        } catch (PropertyVetoException e) {
        	LOG.error("PropertyVetoException: ", e);
            throw new UndeclaredThrowableException(e);
        }
        
        EventIpcManagerFactory.init();
        EventIpcManager mgr = EventIpcManagerFactory.getIpcManager();

        org.opennms.netmgt.translator.EventTranslator keeper = getEventTranslator();
        keeper.setConfig(EventTranslatorConfigFactory.getInstance());
        keeper.setEventManager(mgr);
        keeper.setDataSource(DataSourceFactory.getInstance());
        keeper.init();
    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {
        getEventTranslator().start();
    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
        getEventTranslator().stop();
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    @Override
    public int getStatus() {
        return getEventTranslator().getStatus();
    }

    @Override
    public long getStartTimeMilliseconds() {
        throw new NotImplementedException();
    }

    private org.opennms.netmgt.translator.EventTranslator getEventTranslator() {
        return org.opennms.netmgt.translator.EventTranslator.getInstance();
    }
}
