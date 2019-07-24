/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.api.PollOutagesConfigModifiable;
import org.opennms.netmgt.dao.api.EffectiveConfigurationDao;
import org.opennms.netmgt.model.EffectiveConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * This is the singleton class used to load the configuration for the poller outages from the poll-outages xml file.
 */
public final class PollOutagesConfigFactory extends PollOutagesConfigManager implements PollOutagesConfigModifiable {

    private File configFile;

    @Autowired
    private EffectiveConfigurationDao effectiveConfigurationDao;

    public synchronized void init() throws IOException {
        configFile = ConfigFileConstants.getFile(ConfigFileConstants.POLL_OUTAGES_CONFIG_FILE_NAME);
        setConfigResource(new FileSystemResource(configFile));
        loadConfigFile(configFile);
    }

    private void loadConfigFile(File configFile) throws IOException {
        setConfigResource(new FileSystemResource(configFile));
        afterPropertiesSet();
        saveEffective();
    }

    @Override
    public void reload() {
        try {
            loadConfigFile(configFile);
        } catch (IOException e) {
            // TODO Log WARN but continue with current config.
            e.printStackTrace();
        }
    }

    @Override
    public void saveCurrent() throws IOException {
        super.saveCurrent();
        saveEffective();
    }

    public void setEffectiveConfigurationDao(EffectiveConfigurationDao effectiveConfigurationDao) {
        this.effectiveConfigurationDao = effectiveConfigurationDao;
    }

    private void saveEffective() {
        EffectiveConfiguration entity = new EffectiveConfiguration();
        entity.setKey(ConfigFileConstants.getFileName(ConfigFileConstants.POLL_OUTAGES_CONFIG_FILE_NAME));
        entity.setConfiguration(getJsonConfig());
        entity.setHashCode(getObject().hashCode());
        entity.setLastUpdated(new Date());
        effectiveConfigurationDao.save(entity);
    }

    private String getJsonConfig() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(objectMapper.getTypeFactory()));
            return objectMapper.writeValueAsString(getObject());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void setConfigFile(File file) {
        configFile = file;
        reload();
    }
}
