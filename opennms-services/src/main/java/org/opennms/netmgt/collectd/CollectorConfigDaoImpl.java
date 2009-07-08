/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.collectd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CollectdConfig;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.CollectdPackage;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.dao.CollectorConfigDao;

public class CollectorConfigDaoImpl implements CollectorConfigDao {

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

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    private CollectdConfig getConfig() {
        return CollectdConfigFactory.getInstance().getCollectdConfig();
    }


    public int getSchedulerThreads() {
        return getConfig().getThreads();
    }


    public Collection<Collector> getCollectors() {
        return getConfig().getConfig().getCollectorCollection();
    }

    public void rebuildPackageIpListMap() {
        getConfig().rebuildPackageIpListMap();
    }
    
    public Collection<CollectdPackage> getPackages() {
        return getConfig().getPackages();
    }
    
    public CollectdPackage getPackage(String name) {
        return getConfig().getPackage(name);
    }

}
