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

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.config.ScriptdConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.access.BeanFactoryReference;

/**
 * This class implements a script execution service. This service subscribes to
 * all events, and passes received events to the set of configured scripts.
 *
 * This services uses the Bean Scripting Framework (BSF) in order to allow
 * scripts to be written in a variety of registered languages.
 *
 * @author <a href="mailto:jim.doble@tavve.com">Jim Doble</a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org</a>
 */
@EventListener(name = Scriptd.NAME)
public final class Scriptd extends AbstractServiceDaemon {
    
    private static final Logger LOG = LoggerFactory.getLogger(Scriptd.class);
    
    public static final String NAME = "scriptd";

    /**
     * The singleton instance.
     */
    private static final Scriptd m_singleton = new Scriptd();

    /**
     * The execution launcher
     */
    private Executor m_executor = null;

    /**
     * Constructs a new Script execution daemon.
     */
    private Scriptd() {
        super(NAME);
    }

    /**
     * Initialize the <em>Scriptd</em> service.
     */
    @Override
    protected void onInit() {

        // Load the configuration information
        //
        ScriptdConfigFactory aFactory = null;

        try {
            ScriptdConfigFactory.reload();
            aFactory = ScriptdConfigFactory.getInstance();
        } catch (IOException ex) {
            LOG.error("Failed to load scriptd configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }

        // get the node DAO and sessionUtils
        BeanFactoryReference bf = BeanUtils.getBeanFactory("daoContext");
        NodeDao nodeDao = BeanUtils.getBean(bf, "nodeDao", NodeDao.class);
        SessionUtils sessionUtils = BeanUtils.getBean(bf, "sessionUtils", SessionUtils.class);

        m_executor = new Executor(aFactory, nodeDao, sessionUtils);
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

        LOG.info("Scriptd started");
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
            LOG.warn("Unexpected throwable when stopping Scriptd", e);
        }

        m_executor = null;

        LOG.info("Scriptd stopped");
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

    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadConfigEvent(final IEvent event) {
        if (Executor.isReloadConfigEvent(event)) {
            m_executor.doReload();
        }
    }
}
