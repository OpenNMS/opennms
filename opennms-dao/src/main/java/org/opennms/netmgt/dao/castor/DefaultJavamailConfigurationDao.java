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

package org.opennms.netmgt.dao.castor;

import java.util.List;

import org.opennms.netmgt.config.javamail.End2endMailConfig;
import org.opennms.netmgt.config.javamail.JavamailConfiguration;
import org.opennms.netmgt.config.javamail.ReadmailConfig;
import org.opennms.netmgt.config.javamail.SendmailConfig;
import org.opennms.netmgt.dao.api.JavaMailConfigurationDao;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * <p>DefaultJavamailConfigurationDao class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultJavamailConfigurationDao extends AbstractCastorConfigDao<JavamailConfiguration, JavamailConfiguration> implements JavaMailConfigurationDao {

    /**
     * <p>Constructor for DefaultJavamailConfigurationDao.</p>
     */
    public DefaultJavamailConfigurationDao() {
        super(JavamailConfiguration.class, "Javamail configuration");
    }
    
    /**
     * <p>Constructor for DefaultJavamailConfigurationDao.</p>
     *
     * @param entityClass a {@link java.lang.Class} object.
     * @param description a {@link java.lang.String} object.
     */
    public DefaultJavamailConfigurationDao(Class<JavamailConfiguration> entityClass, String description) {
        super(entityClass, description);
    }

    /** {@inheritDoc} */
    @Override
    public JavamailConfiguration translateConfig(JavamailConfiguration castorConfig) {
        return castorConfig;
    }
    
    /**
     * <p>getDefaultReadmailConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.javamail.ReadmailConfig} object.
     */
    @Override
    public ReadmailConfig getDefaultReadmailConfig() {
        String name = getContainer().getObject().getDefaultReadConfigName();
        return getReadMailConfig(name);
    }
    
    /** {@inheritDoc} */
    @Override
    public ReadmailConfig getReadMailConfig(String name) {
        ReadmailConfig config = null;
        List<ReadmailConfig> configs = getReadmailConfigs();
        
        for (ReadmailConfig readmailConfig : configs) {
            if (readmailConfig.getName().equals(name)) {
                config = readmailConfig;
            }
        }
        return config;
    }

    /**
     * <p>getReadmailConfigs</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<ReadmailConfig> getReadmailConfigs() {
        return getContainer().getObject().getReadmailConfigCollection();
    }

    /**
     * <p>getDefaultSendmailConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.javamail.SendmailConfig} object.
     */
    @Override
    public SendmailConfig getDefaultSendmailConfig() {
        String name = getContainer().getObject().getDefaultSendConfigName();
        return getSendMailConfig(name);
    }

    /** {@inheritDoc} */
    @Override
    public SendmailConfig getSendMailConfig(String name) {
        SendmailConfig config = null;
        List<SendmailConfig> configs = getSendmailConfigs();
        
        for (SendmailConfig sendmailConfig : configs) {
            if (sendmailConfig.getName().equals(name)) {
                config = sendmailConfig;
            }
        }
        return config;
    }

    /**
     * <p>getSendmailConfigs</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<SendmailConfig> getSendmailConfigs() {
        return getContainer().getObject().getSendmailConfigCollection();
    }

    /** {@inheritDoc} */
    @Override
    public End2endMailConfig getEnd2EndConfig(String name) {
        End2endMailConfig config = null;
        List<End2endMailConfig> configs = getEnd2EndConfigs();
        
        for (End2endMailConfig end2endMailConfig : configs) {
            if (end2endMailConfig.getName().equals(name)) {
                config = end2endMailConfig;
            }
        }
        return config;
    }
    
    /**
     * <p>getEnd2EndConfigs</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<End2endMailConfig> getEnd2EndConfigs() {
        return getContainer().getObject().getEnd2endMailConfigCollection();
    }
    
    /**
     * <p>verifyMarshaledConfiguration</p>
     *
     * @throws java.lang.IllegalStateException if any.
     */
    @Override
    public void verifyMarshaledConfiguration() throws IllegalStateException {
        // TODO verify that the default config names match as specified in javamail configuration element
        // TODO verify that the config names match as specified in all the end2end configuration elements
        
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

}
