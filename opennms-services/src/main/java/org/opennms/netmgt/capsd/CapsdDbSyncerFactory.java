/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.
 * All rights reserved.
 * 
 * OpenNMS(R) is a derivative work, containing both original code, included
 * code and modified code that was published under the GNU General Public
 * License.  Copyrights for modified and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 May 06: Created this file. - dj@opennms.org
 *
 * Copyright (C) 2007 DJ Gregor.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact: 
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.capsd;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CapsdConfigFactory;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.springframework.util.Assert;

/**
 * Factory for accessing the CapsdDbSyncer.
 * 
 * @see JdbcCapsdDbSyncer
 */
public final class CapsdDbSyncerFactory {
    private static CapsdDbSyncer s_singleton = null;
    
    /**
     * This class only has static methods.
     */
    private CapsdDbSyncerFactory() {
    }

    public static synchronized void init()  {
        if (isLoaded()) {
            return;
        }
        
        log().debug("init: Performing default initialization using JdbcCapsdDbSyncer");

        JdbcCapsdDbSyncer syncer = new JdbcCapsdDbSyncer();
        syncer.setOpennmsServerConfig(OpennmsServerConfigFactory.getInstance());
        syncer.setCapsdConfig(CapsdConfigFactory.getInstance());
        syncer.setPollerConfig(PollerConfigFactory.getInstance());
        syncer.setCollectdConfig(CollectdConfigFactory.getInstance());
        syncer.afterPropertiesSet();
    
        setInstance(syncer);
    }

    public static synchronized CapsdDbSyncer getInstance() {
        Assert.state(isLoaded(), "The factory has not been initialized");

        return s_singleton;
    }
    
    public static synchronized void setInstance(JdbcCapsdDbSyncer singleton) {
        s_singleton = singleton;
    }

    private static boolean isLoaded() {
        return s_singleton != null;
    }

    private static Category log() {
        return ThreadCategory.getInstance(CapsdDbSyncerFactory.class);
    }

}
