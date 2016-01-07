/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
 * The Interface JavaMailConfigurationDao.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public interface JavaMailConfigurationDao {

    /**
     * Gets the default sendmail configuration.
     *
     * @return the default sendmail configuration
     */
    SendmailConfig getDefaultSendmailConfig();

    /**
     * Gets the send mail configuration.
     *
     * @param name the name
     * @return the send mail configuration
     */
    SendmailConfig getSendMailConfig(String name);

    /**
     * Adds the send mail configuration.
     * <p>If there is a sendmail-config object with the same name, it will be replaced; otherwise, the new object will be added.</p>
     *
     * @param sendmailConfig the sendmail configuration
     */
    void addSendMailConfig(SendmailConfig sendmailConfig);

    /**
     * Gets the sendmail configurations.
     *
     * @return the sendmail configurations
     */
    List<SendmailConfig> getSendmailConfigs();

    /**
     * Gets the default readmail configuration.
     *
     * @return the default readmail configuration
     */
    ReadmailConfig getDefaultReadmailConfig();

    /**
     * Gets the read mail configuration.
     *
     * @param name the name
     * @return the read mail configuration
     */
    ReadmailConfig getReadMailConfig(String name);

    /**
     * Adds the read mail configuration.
     * <p>If there is a readmail-config object with the same name, it will be replaced; otherwise, the new object will be added.</p>
     *
     * @param readmailConfig the readmail configuration
     */
    void addReadMailConfig(ReadmailConfig readmailConfig);

    /**
     * Gets the readmail configurations.
     *
     * @return the readmail configurations
     */
    List<ReadmailConfig> getReadmailConfigs();

    /**
     * Gets the end2end mail configuration.
     *
     * @param name the name
     * @return the end2end configuration
     */
    End2endMailConfig getEnd2EndConfig(String name);

    /**
     * Adds the end2end mail configuration.
     * <p>If there is a end2end-mail-config object with the same name, it will be replaced; otherwise, the new object will be added.</p>
     *
     * @param end2endConfig the end2end configuration
     */
    void addEnd2endMailConfig(End2endMailConfig end2endConfig);

    /**
     * Gets the end2end mail configurations.
     *
     * @return the end2end mail configurations
     */
    List<End2endMailConfig> getEnd2EndConfigs();

    /**
     * Verifies marshaled configuration.
     *
     * @throws IllegalStateException the illegal state exception
     */
    void verifyMarshaledConfiguration() throws IllegalStateException;

    /**
     * Reloads the configuration.
     *
     * @throws DataAccessResourceFailureException the data access resource failure exception
     */
    void reloadConfiguration() throws DataAccessResourceFailureException;

    /**
     * Saves the current configuration on disk.
     */
    void saveConfiguration();
}
