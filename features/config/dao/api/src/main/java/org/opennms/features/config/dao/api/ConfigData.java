/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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


package org.opennms.features.config.dao.api;

import org.opennms.features.distributed.kvstore.api.SerializingBlobStore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class include meta-data of the config
 */
public class ConfigData<T> implements Serializable {
    private String name;
    private String version;
    // TODO: implement validation later
    private Class validatorClass;
    // it should be fileName <> config pair
    private Map<String, T> configs;

    // dummy constructor
    public ConfigData(){

    }
    public ConfigData(String name, String version, Class validatorClass){
        this.name = name;
        this.version = version;
        this.validatorClass = validatorClass;
        configs = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Class getValidatorClass() {
        return validatorClass;
    }

    public void setValidatorClass(Class validatorClass) {
        this.validatorClass = validatorClass;
    }

    public Map<String, T> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, T> configs) {
        this.configs = configs;
    }
}