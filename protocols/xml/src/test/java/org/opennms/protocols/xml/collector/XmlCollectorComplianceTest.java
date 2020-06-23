/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.opennms.netmgt.collection.test.api.CollectorComplianceTest;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlDataCollectionConfig;
import org.opennms.protocols.xml.config.XmlRrd;
import org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao;

import com.google.common.collect.ImmutableMap;

public class XmlCollectorComplianceTest extends CollectorComplianceTest {

    private static final String COLLECTION = "default";

    public XmlCollectorComplianceTest() {
        super(XmlCollector.class, true);
    }

    @Override
    public String getCollectionName() {
        return COLLECTION;
    }

    @Override
    public Map<String, Object> getRequiredParameters() {
        return new ImmutableMap.Builder<String, Object>()
            .put("collection", COLLECTION)
            .build();
    }

    public Map<String, Object> getRequiredBeans() {
        XmlDataCollectionConfig config = mock(XmlDataCollectionConfig.class);
        when(config.getRrdRepository()).thenReturn("target");
        when(config.buildRrdRepository(COLLECTION)).thenReturn(new RrdRepository());

        XmlRrd xmlRrd = new XmlRrd();
        xmlRrd.setStep(300);
        XmlDataCollection collection = new XmlDataCollection();
        collection.setXmlRrd(xmlRrd);
        XmlDataCollectionConfigDao xmlDataCollectionConfigDao = mock(XmlDataCollectionConfigDao.class);
        when(xmlDataCollectionConfigDao.getDataCollectionByName(COLLECTION)).thenReturn(collection);
        when(xmlDataCollectionConfigDao.getConfig()).thenReturn(config);

        return new ImmutableMap.Builder<String, Object>()
                .put("xmlDataCollectionConfigDao", xmlDataCollectionConfigDao)
                .build();
    }
}
