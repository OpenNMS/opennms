/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 Feb 15: Pass the newly required description argument to our super's constructor. - dj@opennms.org
 * 2007 Apr 10: Created this file.
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.dao.castor;

import java.util.List;

import org.opennms.netmgt.config.statsd.StatisticsDaemonConfiguration;
import org.opennms.netmgt.dao.StatisticsDaemonConfigDao;
import org.opennms.netmgt.dao.castor.statsd.Report;
import org.opennms.netmgt.dao.castor.statsd.StatsdConfig;
import org.opennms.netmgt.dao.castor.statsd.StatsdPackage;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * DAO implementation for accessing the XML configuration for
 * statsd.  The Castor objects are translated into a more friendly
 * set of objects that are exposed through the DAO.  The Castor
 * objects are <i>not</i> exposed.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see StatsdConfig
 */
public class DefaultStatisticsDaemonConfigDao extends AbstractCastorConfigDao<StatisticsDaemonConfiguration, StatsdConfig> implements StatisticsDaemonConfigDao {
    public DefaultStatisticsDaemonConfigDao() {
        super(StatisticsDaemonConfiguration.class, "statistics daemon configuration");
    }
    
    @Override
    public StatsdConfig translateConfig(StatisticsDaemonConfiguration castorConfig) {
        return new StatsdConfig(castorConfig);
    }
    
    private StatsdConfig getConfig() {
        return getContainer().getObject();
    }

    public List<Report> getReports() {
        return getConfig().getReports();
    }
    
    public List<StatsdPackage> getPackages() {
        return getConfig().getPackages();
    }
    
    public void reloadConfiguration() throws DataAccessResourceFailureException {
        getContainer().reload();
        this.verifyMarshaledConfiguration();
    }

    private void verifyMarshaledConfiguration() {
        // TODO Auto-generated method stub
        
    }

}
