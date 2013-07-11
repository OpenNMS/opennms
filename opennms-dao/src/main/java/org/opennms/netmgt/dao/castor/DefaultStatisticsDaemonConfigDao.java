/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.castor;

import java.util.List;

import org.opennms.netmgt.config.statsd.StatisticsDaemonConfiguration;
import org.opennms.netmgt.config.statsd.model.Report;
import org.opennms.netmgt.config.statsd.model.StatsdConfig;
import org.opennms.netmgt.config.statsd.model.StatsdPackage;
import org.opennms.netmgt.dao.api.StatisticsDaemonConfigDao;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * DAO implementation for accessing the XML configuration for
 * statsd.  The Castor objects are translated into a more friendly
 * set of objects that are exposed through the DAO.  The Castor
 * objects are <i>not</i> exposed.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see StatsdConfig
 * @version $Id: $
 */
public class DefaultStatisticsDaemonConfigDao extends AbstractCastorConfigDao<StatisticsDaemonConfiguration, StatsdConfig> implements StatisticsDaemonConfigDao {
    /**
     * <p>Constructor for DefaultStatisticsDaemonConfigDao.</p>
     */
    public DefaultStatisticsDaemonConfigDao() {
        super(StatisticsDaemonConfiguration.class, "statistics daemon configuration");
    }
    
    /** {@inheritDoc} */
    @Override
    public StatsdConfig translateConfig(StatisticsDaemonConfiguration castorConfig) {
        return new StatsdConfig(castorConfig);
    }
    
    private StatsdConfig getConfig() {
        return getContainer().getObject();
    }

    /**
     * <p>getReports</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<Report> getReports() {
        return getConfig().getReports();
    }
    
    /**
     * <p>getPackages</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<StatsdPackage> getPackages() {
        return getConfig().getPackages();
    }
    
    /**
     * <p>reloadConfiguration</p>
     *
     * @throws org.springframework.dao.DataAccessResourceFailureException if any.
     */
    @Override
    public void reloadConfiguration() throws DataAccessResourceFailureException {
        getContainer().reload();
        this.verifyMarshaledConfiguration();
    }

    private void verifyMarshaledConfiguration() {
        // TODO Auto-generated method stub
        
    }

}
