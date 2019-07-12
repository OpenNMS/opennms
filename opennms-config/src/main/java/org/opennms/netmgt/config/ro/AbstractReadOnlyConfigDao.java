/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.ro;

import java.io.IOException;
import java.util.Date;

import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.netmgt.config.ReadOnlyConfig;
import org.opennms.netmgt.dao.api.EffectiveConfigurationDao;
import org.opennms.netmgt.model.EffectiveConfiguration;
import org.opennms.netmgt.model.OnmsJsonDocument;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractReadOnlyConfigDao<T extends ReadOnlyConfig> implements ReadOnlyConfigDao<T> {

    @Autowired
    private EffectiveConfigurationDao dao;

    @Override
    public T getByKey(Class<T> type, String key) {
        EffectiveConfiguration config = dao.getByKey(key);
        if (config == null) {
            return null;
        }
        return unMarshallConfig(type, config.getConfiguration());
    }

    @Override
    public Date getLastUpdated(String key) {
        return dao.getLastUpdated(key);
    }

    private T unMarshallConfig(Class<T> type, OnmsJsonDocument onmsJsonDocument) {
        try {
            return new ObjectMapper().readValue(onmsJsonDocument.getDocument().getAsString(), type);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

}
