/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Apr 10: Created this file. - dj@opennms.org
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
package org.opennms.web.validator;

import org.opennms.netmgt.dao.StatisticsReportDao;
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
 * @since 1.6.12
 */
public class StatisticsReportCommandValidator implements Validator, InitializingBean {
    private StatisticsReportDao m_statisticsReportDao;

    /** {@inheritDoc} */
    public boolean supports(Class clazz) {
        return clazz.equals(StatisticsReportCommand.class);
    }

    /** {@inheritDoc} */
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
    public void afterPropertiesSet() {
        if (m_statisticsReportDao == null) {
            throw new IllegalStateException("statisticsReportDao property not set");
        }
    }

    /**
     * <p>getStatisticsReportDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.StatisticsReportDao} object.
     */
    public StatisticsReportDao getStatisticsReportDao() {
        return m_statisticsReportDao;
    }

    /**
     * <p>setStatisticsReportDao</p>
     *
     * @param statisticsReportDao a {@link org.opennms.netmgt.dao.StatisticsReportDao} object.
     */
    public void setStatisticsReportDao(StatisticsReportDao statisticsReportDao) {
        m_statisticsReportDao = statisticsReportDao;
    }

}
