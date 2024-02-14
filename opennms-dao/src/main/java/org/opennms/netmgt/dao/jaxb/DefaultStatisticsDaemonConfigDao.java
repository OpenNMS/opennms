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
package org.opennms.netmgt.dao.jaxb;

import java.util.List;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.statsd.StatisticsDaemonConfiguration;
import org.opennms.netmgt.config.statsd.model.Report;
import org.opennms.netmgt.config.statsd.model.StatsdConfig;
import org.opennms.netmgt.config.statsd.model.StatsdPackage;
import org.opennms.netmgt.dao.api.StatisticsDaemonConfigDao;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * DAO implementation for accessing the XML configuration for
 * statsd.  The objects are translated into a more friendly
 * set of objects that are exposed through the DAO.  The
 * objects are <i>not</i> exposed.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see StatsdConfig
 * @version $Id: $
 */
public class DefaultStatisticsDaemonConfigDao extends AbstractJaxbConfigDao<StatisticsDaemonConfiguration, StatsdConfig> implements StatisticsDaemonConfigDao {
    /**
     * <p>Constructor for DefaultStatisticsDaemonConfigDao.</p>
     */
    public DefaultStatisticsDaemonConfigDao() {
        super(StatisticsDaemonConfiguration.class, "statistics daemon configuration");
    }
    
    /** {@inheritDoc} */
    @Override
    public StatsdConfig translateConfig(StatisticsDaemonConfiguration config) {
        return new StatsdConfig(config);
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
    }

}
