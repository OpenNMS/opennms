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

import java.io.FileWriter;
import java.util.List;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.javamail.End2endMailConfig;
import org.opennms.netmgt.config.javamail.JavamailConfiguration;
import org.opennms.netmgt.config.javamail.ReadmailConfig;
import org.opennms.netmgt.config.javamail.SendmailConfig;
import org.opennms.netmgt.dao.api.JavaMailConfigurationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * The Class DefaultJavamailConfigurationDao.
 */
public class DefaultJavamailConfigurationDao extends AbstractJaxbConfigDao<JavamailConfiguration, JavamailConfiguration> implements JavaMailConfigurationDao {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultJavamailConfigurationDao.class);

    /**
     * Instantiates a new default javamail configuration DAO.
     */
    public DefaultJavamailConfigurationDao() {
        super(JavamailConfiguration.class, "Javamail configuration");
    }

    /**
     * Instantiates a new default javamail configuration DAO.
     *
     * @param entityClass the entity class
     * @param description the description
     */
    public DefaultJavamailConfigurationDao(Class<JavamailConfiguration> entityClass, String description) {
        super(entityClass, description);
    }

    /* (non-Javadoc)
     * @see org.opennms.core.xml.AbstractJaxbConfigDao#translateConfig(java.lang.Object)
     */
    @Override
    public JavamailConfiguration translateConfig(JavamailConfiguration config) {
        return config;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#getDefaultReadmailConfig()
     */
    @Override
    public ReadmailConfig getDefaultReadmailConfig() {
        return getReadMailConfig(getContainer().getObject().getDefaultReadConfigName());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#setDefaultReadmailConfig(java.lang.String)
     */
    @Override
    public void setDefaultReadmailConfig(String readmailConfigName) {
        if (getReadMailConfig(readmailConfigName) != null) {
            getContainer().getObject().setDefaultReadConfigName(readmailConfigName);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#getReadMailConfig(java.lang.String)
     */
    @Override
    public ReadmailConfig getReadMailConfig(String name) {
        ReadmailConfig config = null;
        List<ReadmailConfig> configs = getReadmailConfigs();

        for (ReadmailConfig readmailConfig : configs) {
            if (readmailConfig.getName() != null && readmailConfig.getName().equals(name)) {
                config = readmailConfig;
            }
        }
        return config;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#getReadmailConfigs()
     */
    @Override
    public List<ReadmailConfig> getReadmailConfigs() {
        return getContainer().getObject().getReadmailConfigs();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#getDefaultSendmailConfig()
     */
    @Override
    public SendmailConfig getDefaultSendmailConfig() {
        return getSendMailConfig(getContainer().getObject().getDefaultSendConfigName());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#setDefaultSendmailConfig(java.lang.String)
     */
    @Override
    public void setDefaultSendmailConfig(String sendmailConfigName) {
        if (getSendMailConfig(sendmailConfigName) != null) {
            getContainer().getObject().setDefaultSendConfigName(sendmailConfigName);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#getSendMailConfig(java.lang.String)
     */
    @Override
    public SendmailConfig getSendMailConfig(String name) {
        SendmailConfig config = null;
        List<SendmailConfig> configs = getSendmailConfigs();

        for (SendmailConfig sendmailConfig : configs) {
            if (sendmailConfig.getName() != null && sendmailConfig.getName().equals(name)) {
                config = sendmailConfig;
            }
        }
        return config;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#addSendMailConfig(org.opennms.netmgt.config.javamail.SendmailConfig)
     */
    @Override
    public void addSendMailConfig(SendmailConfig sendmailConfig) {
        int index = -1;
        List<SendmailConfig> configs = getSendmailConfigs();
        for (int i = 0; i < configs.size(); i++) {
            final SendmailConfig c = configs.get(i);
            if (c.getName().equals(sendmailConfig.getName())) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            configs.remove(index);
            configs.add(index, sendmailConfig);
        } else {
            configs.add(sendmailConfig);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#removeSendMailConfig(java.lang.String)
     */
    @Override
    public boolean removeSendMailConfig(String sendmailConfig) {
        int index = -1;
        List<SendmailConfig> configs = getSendmailConfigs();
        for (int i = 0; i < configs.size(); i++) {
            final SendmailConfig c = configs.get(i);
            if (c.getName() != null && c.getName().equals(sendmailConfig)) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            configs.remove(index);
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#getSendmailConfigs()
     */
    @Override
    public List<SendmailConfig> getSendmailConfigs() {
        return getContainer().getObject().getSendmailConfigs();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#getEnd2endConfig(java.lang.String)
     */
    @Override
    public End2endMailConfig getEnd2endConfig(String name) {
        End2endMailConfig config = null;
        List<End2endMailConfig> configs = getEnd2EndConfigs();

        for (End2endMailConfig end2endMailConfig : configs) {
            if (end2endMailConfig.getName() != null && end2endMailConfig.getName().equals(name)) {
                config = end2endMailConfig;
            }
        }
        return config;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#addReadMailConfig(org.opennms.netmgt.config.javamail.ReadmailConfig)
     */
    @Override
    public void addReadMailConfig(ReadmailConfig readmailConfig) {
        int index = -1;
        List<ReadmailConfig> configs = getReadmailConfigs();
        for (int i = 0; i < configs.size(); i++) {
            if (configs.get(i).getName().equals(readmailConfig.getName())) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            configs.remove(index);
            configs.add(index, readmailConfig);
        } else {
            configs.add(readmailConfig);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#removeReadMailConfig(java.lang.String)
     */
    @Override
    public boolean removeReadMailConfig(String readmailConfig) {
        int index = -1;
        List<ReadmailConfig> configs = getReadmailConfigs();
        for (int i = 0; i < configs.size(); i++) {
            final String name = configs.get(i).getName();
            if (name != null && name.equals(readmailConfig)) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            configs.remove(index);
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#getEnd2EndConfigs()
     */
    @Override
    public List<End2endMailConfig> getEnd2EndConfigs() {
        return getContainer().getObject().getEnd2endMailConfigs();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#addEnd2endMailConfig(org.opennms.netmgt.config.javamail.End2endMailConfig)
     */
    @Override
    public void addEnd2endMailConfig(End2endMailConfig end2endConfig) {
        int index = -1;
        List<End2endMailConfig> configs = getEnd2EndConfigs();
        for (int i = 0; i < configs.size(); i++) {
            if (configs.get(i).getName().equals(end2endConfig.getName())) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            configs.remove(index);
            configs.add(index, end2endConfig);
        } else {
            configs.add(end2endConfig);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#removeEnd2endConfig(java.lang.String)
     */
    @Override
    public boolean removeEnd2endConfig(String end2endConfig) {
        int index = -1;
        List<End2endMailConfig> configs = getEnd2EndConfigs();
        for (int i = 0; i < configs.size(); i++) {
            final String name = configs.get(i).getName();
            if (name != null && name.equals(end2endConfig)) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            configs.remove(index);
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#verifyMarshaledConfiguration()
     */
    @Override
    public void verifyMarshaledConfiguration() throws IllegalStateException {
        // TODO verify that the default config names match as specified in javamail configuration element
        // TODO verify that the config names match as specified in all the end2end configuration elements
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#reloadConfiguration()
     */
    @Override
    public void reloadConfiguration() throws DataAccessResourceFailureException {
        getContainer().reload();
        this.verifyMarshaledConfiguration();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.JavaMailConfigurationDao#saveConfiguration()
     */
    public synchronized void saveConfiguration() {
        try {
            JaxbUtils.marshal(getContainer().getObject(), new FileWriter(getContainer().getFile()));
        } catch (Exception e) {
            LOG.error("Can't save JavaMail configuration.", e);
        }
    }

}
