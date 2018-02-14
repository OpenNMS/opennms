/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.scriptd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.config.ScriptdConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.api.NodeDao;
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

        // get the node DAO
        BeanFactoryReference bf = BeanUtils.getBeanFactory("daoContext");
        NodeDao nodeDao = BeanUtils.getBean(bf, "nodeDao", NodeDao.class);

        m_executor = new Executor(aFactory, nodeDao);
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
}
