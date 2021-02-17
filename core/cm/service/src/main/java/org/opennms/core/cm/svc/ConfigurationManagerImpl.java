/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.core.cm.svc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.xml.transform.stream.StreamSource;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.opennms.core.cm.api.ConfigurationManager;

import com.google.common.io.Resources;

public class ConfigurationManagerImpl implements ConfigurationManager {

    private final Map<String,XmlSchema> schemaMap = new ConcurrentSkipListMap<>();
    private final Map<String,Object> objectStore = new ConcurrentSkipListMap<>();

    @Override
    public void registerXSD(String service, String pathToXsd) throws IOException {
        final XmlSchema schema;
        final URL url = Resources.getResource(pathToXsd);
        try (InputStream is = url.openStream()) {
            XmlSchemaCollection schemaCol = new XmlSchemaCollection();
            schema = schemaCol.read(new StreamSource(is));
        }
        // TODO: Define behavior when an existing XSD is present
        schemaMap.put(service, schema);
    }

    @Override
    public <T> Optional<T> getModel(String service, Class<T> clazz) {
        if (!schemaMap.containsKey(service)) {
            throw new IllegalStateException("Oops. I don't know about this service yet: " + service);
        }
        final Object o = objectStore.get(service);
        if (o == null) {
            return Optional.empty();
        }
        return Optional.of((T)o);
    }

    @Override
    public <T> void setModel(String service, T model) {
        objectStore.put(service, model);
    }
}
