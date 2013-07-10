/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;

import java.util.List;

import org.opennms.netmgt.config.javamail.End2endMailConfig;
import org.opennms.netmgt.config.javamail.ReadmailConfig;
import org.opennms.netmgt.config.javamail.SendmailConfig;
import org.springframework.dao.DataAccessResourceFailureException;


/**
 * <p>JavaMailConfigurationDao interface.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public interface JavaMailConfigurationDao {

    /**
     * <p>getDefaultSendmailConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.javamail.SendmailConfig} object.
     */
    SendmailConfig getDefaultSendmailConfig();
    
    /**
     * <p>getSendMailConfig</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.javamail.SendmailConfig} object.
     */
    SendmailConfig getSendMailConfig(String name);
    
    /**
     * <p>getSendmailConfigs</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<SendmailConfig> getSendmailConfigs();
    
    /**
     * <p>getDefaultReadmailConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.javamail.ReadmailConfig} object.
     */
    ReadmailConfig getDefaultReadmailConfig();
    
    /**
     * <p>getReadMailConfig</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.javamail.ReadmailConfig} object.
     */
    ReadmailConfig getReadMailConfig(String name);
    
    /**
     * <p>getReadmailConfigs</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<ReadmailConfig> getReadmailConfigs();
    
    /**
     * <p>getEnd2EndConfig</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.javamail.End2endMailConfig} object.
     */
    End2endMailConfig getEnd2EndConfig(String name);
    
    /**
     * <p>getEnd2EndConfigs</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<End2endMailConfig> getEnd2EndConfigs();
    
    /**
     * <p>verifyMarshaledConfiguration</p>
     *
     * @throws java.lang.IllegalStateException if any.
     */
    void verifyMarshaledConfiguration() throws IllegalStateException;
    
    /**
     * <p>reloadConfiguration</p>
     *
     * @throws org.springframework.dao.DataAccessResourceFailureException if any.
     */
    void reloadConfiguration() throws DataAccessResourceFailureException;
    
}
