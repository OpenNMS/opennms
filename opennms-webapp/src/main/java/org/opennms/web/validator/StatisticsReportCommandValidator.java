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

package org.opennms.web.validator;

import org.opennms.netmgt.dao.api.StatisticsReportDao;
import org.opennms.web.command.StatisticsReportCommand;
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
