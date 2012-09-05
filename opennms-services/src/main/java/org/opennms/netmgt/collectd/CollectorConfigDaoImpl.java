
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

package org.opennms.netmgt.collectd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CollectdConfig;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.CollectdPackage;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.dao.CollectorConfigDao;

/**
 * <p>CollectorConfigDaoImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class CollectorConfigDaoImpl implements CollectorConfigDao {

    /**
     * <p>Constructor for CollectorConfigDaoImpl.</p>
     */
    public CollectorConfigDaoImpl() {

        loadConfigFactory();

    }

    private void loadConfigFactory() {
        // Load collectd configuration file
        try {
            // XXX was reload(); this doesn't work well from unit tests, however
            CollectdConfigFactory.init();
        } catch (MarshalException ex) {
            log().fatal("loadConfigFactory: Failed to load collectd configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
            log().fatal("loadConfigFactory: Failed to load collectd configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
            log().fatal("loadConfigFactory: Failed to load collectd configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    public ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    private CollectdConfig getConfig() {
        return CollectdConfigFactory.getInstance().getCollectdConfig();
    }


    /**
     * <p>getSchedulerThreads</p>
     *
     * @return a int.
     */
    @Override
    public int getSchedulerThreads() {
        return getConfig().getThreads();
    }


    /**
     * <p>getCollectors</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<Collector> getCollectors() {
        return getConfig().getConfig().getCollectorCollection();
    }

    /**
     * <p>rebuildPackageIpListMap</p>
     */
    @Override
    public void rebuildPackageIpListMap() {
        getConfig().rebuildPackageIpListMap();
    }
    
    /**
     * <p>getPackages</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<CollectdPackage> getPackages() {
        return getConfig().getPackages();
    }
    
    /** {@inheritDoc} */
    @Override
    public CollectdPackage getPackage(String name) {
        return getConfig().getPackage(name);
    }

}
