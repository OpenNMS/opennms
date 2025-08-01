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
package org.opennms.web.validator;

import org.opennms.netmgt.dao.api.StatisticsReportDao;
import org.opennms.web.svclayer.model.StatisticsReportCommand;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Command validator for StatisticsReportCommand.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see StatisticsReportCommand
 * @version $Id: $
 * @since 1.8.1
 */
public class StatisticsReportCommandValidator implements Validator, InitializingBean {
    private StatisticsReportDao m_statisticsReportDao;

    /** {@inheritDoc} */
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(StatisticsReportCommand.class);
    }

    /** {@inheritDoc} */
    @Override
    public void validate(Object obj, Errors errors) {
        StatisticsReportCommand cmd = (StatisticsReportCommand) obj;
        
        if (cmd.getId() == null) {
            errors.rejectValue("id", "statisticsReportId.notSpecified",
                               new Object[] { "id" }, 
                               "Value required.");
        } else {
            try {
                int id = cmd.getId();
                m_statisticsReportDao.load(id);
            } catch (DataAccessException e) {
                errors.rejectValue("id", "statisticsReportId.notFound",
                                   new Object[] { "id", cmd.getId() }, 
                                   "Valid statistics report ID required.");
                
            }
        }
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        if (m_statisticsReportDao == null) {
            throw new IllegalStateException("statisticsReportDao property not set");
        }
    }

    /**
     * <p>getStatisticsReportDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.StatisticsReportDao} object.
     */
    public StatisticsReportDao getStatisticsReportDao() {
        return m_statisticsReportDao;
    }

    /**
     * <p>setStatisticsReportDao</p>
     *
     * @param statisticsReportDao a {@link org.opennms.netmgt.dao.api.StatisticsReportDao} object.
     */
    public void setStatisticsReportDao(StatisticsReportDao statisticsReportDao) {
        m_statisticsReportDao = statisticsReportDao;
    }

}
