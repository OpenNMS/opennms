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
     * Sets the default sendmail configuration.
     *
     * @param sendmailConfigName the new default sendmail configuration
     */
    void setDefaultSendmailConfig(String sendmailConfigName);

    /**
     * Gets the default readmail configuration.
     *
     * @return the default readmail configuration
     */
    ReadmailConfig getDefaultReadmailConfig();

    /**
     * Sets the default readmail configuration.
     *
     * @param readmailConfigName the new default readmail configuration
     */
    void setDefaultReadmailConfig(String readmailConfigName);

    /**
     * Gets the send mail configuration.
     *
     * @param sendmailConfigName the sendmail configuration name
     * @return the send mail configuration
     */
    SendmailConfig getSendMailConfig(String sendmailConfigName);

    /**
     * Adds the send mail configuration.
     * <p>If there is a sendmail-config object with the same name, it will be replaced; otherwise, the new object will be added.</p>
     *
     * @param sendmailConfig the sendmail configuration
     */
    void addSendMailConfig(SendmailConfig sendmailConfig);

    /**
     * Removes the sendmail configuration.
     *
     * @param sendmailConfigName the sendmail configuration name
     * @return true, if successful
     */
    boolean removeSendMailConfig(String sendmailConfigName);

    /**
     * Gets the sendmail configurations.
     *
     * @return the sendmail configurations
     */
    List<SendmailConfig> getSendmailConfigs();

    /**
     * Gets the read mail configuration.
     *
     * @param readmailConfigName the readmail configuration name
     * @return the read mail configuration
     */
    ReadmailConfig getReadMailConfig(String readmailConfigName);

    /**
     * Adds the read mail configuration.
     * <p>If there is a readmail-config object with the same name, it will be replaced; otherwise, the new object will be added.</p>
     *
     * @param readmailConfig the readmail configuration
     */
    void addReadMailConfig(ReadmailConfig readmailConfig);

    /**
     * Removes the readmail configuration.
     *
     * @param readmailConfigName the readmail configuration name
     * @return true, if successful
     */
    boolean removeReadMailConfig(String readmailConfigName);

    /**
     * Gets the readmail configurations.
     *
     * @return the readmail configurations
     */
    List<ReadmailConfig> getReadmailConfigs();

    /**
     * Gets the end2end mail configuration.
     *
     * @param end2endConfigName the end2end configuration name
     * @return the end2end configuration
     */
    End2endMailConfig getEnd2endConfig(String end2endConfigName);

    /**
     * Adds the end2end mail configuration.
     * <p>If there is a end2end-mail-config object with the same name, it will be replaced; otherwise, the new object will be added.</p>
     *
     * @param end2endConfig the end2end configuration
     */
    void addEnd2endMailConfig(End2endMailConfig end2endConfig);

    /**
     * Removes the end2 end configuration.
     *
     * @param end2endConfigName the end2end configuration name
     * @return true, if successful
     */
    boolean removeEnd2endConfig(String end2endConfigName);

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
