/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * Created: January 23, 2009
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.dao.castor;

import java.util.List;

import org.opennms.netmgt.config.common.End2endMailConfig;
import org.opennms.netmgt.config.common.JavamailConfiguration;
import org.opennms.netmgt.config.common.ReadmailConfig;
import org.opennms.netmgt.config.common.SendmailConfig;
import org.opennms.netmgt.dao.JavaMailConfigurationDao;
import org.springframework.dao.DataAccessResourceFailureException;

public class DefaultJavamailConfigurationDao extends AbstractCastorConfigDao<JavamailConfiguration, JavamailConfiguration> implements JavaMailConfigurationDao {

    public DefaultJavamailConfigurationDao() {
        super(JavamailConfiguration.class, "Javamail configuration");
    }
    
    public DefaultJavamailConfigurationDao(Class<JavamailConfiguration> entityClass, String description) {
        super(entityClass, description);
    }

    @Override
    public JavamailConfiguration translateConfig(JavamailConfiguration castorConfig) {
        return castorConfig;
    }
    
    public ReadmailConfig getDefaultReadmailConfig() {
        String name = getContainer().getObject().getDefaultReadConfigName();
        return getReadMailConfig(name);
    }
    
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

    public List<ReadmailConfig> getReadmailConfigs() {
        return getContainer().getObject().getReadmailConfigCollection();
    }

    public SendmailConfig getDefaultSendmailConfig() {
        String name = getContainer().getObject().getDefaultSendConfigName();
        return getSendMailConfig(name);
    }

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

    public List<SendmailConfig> getSendmailConfigs() {
        return getContainer().getObject().getSendmailConfigCollection();
    }

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
    
    public List<End2endMailConfig> getEnd2EndConfigs() {
        return getContainer().getObject().getEnd2endMailConfigCollection();
    }
    
    public void verifyMarshaledConfiguration() throws IllegalStateException {
        // TODO verify that the default config names match as specified in javamail configuration element
        // TODO verify that the config names match as specified in all the end2end configuration elements
        
    }

    public void reloadConfiguration() throws DataAccessResourceFailureException {
        getContainer().reload();
        this.verifyMarshaledConfiguration();
    }

}
