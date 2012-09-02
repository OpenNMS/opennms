/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rtc;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.RTCConfigFactory;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.filter.FilterParseException;
import org.opennms.netmgt.rtc.datablock.RTCCategory;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

@RunWith(SpringJUnit4ClassRunner.class)
public class DataSenderTest {
    /*
     * This doesn't work unless we have a receiver on the other end.... more of an integration test
     */
    @Test
    @Ignore
    public void testSendData() throws MarshalException, ValidationException, IOException, FilterParseException, SAXException, SQLException, RTCException {
        InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this, "/org/opennms/netmgt/config/rtc-configuration.xml");
        RTCConfigFactory.setInstance(new RTCConfigFactory(stream));
        stream.close();
        
        Resource categoryResource = ConfigurationTestUtils.getSpringResourceForResource(this, "/org/opennms/netmgt/config/categories.xml");
        CategoryFactory.setInstance(new CategoryFactory(categoryResource));
        
        stream = ConfigurationTestUtils.getInputStreamForResource(this, "/org/opennms/netmgt/config/test-database-schema.xml");
        DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(stream));
        stream.close();

        
        DataManager dataManager = new DataManager();
        RTCManager.setDataManager(dataManager);

        String categoryName = "Database Servers";
        String categoryNameUrl = "Database+Servers";
        Category category = new Category();
        category.setLabel(categoryName);
        category.setComment("Some database servers.  Exciting, eh?");
        category.setNormal(99.0);
        category.setWarning(97.0);
        RTCCategory rtcCategory = new RTCCategory(category, categoryName);
        Map<String, RTCCategory> rtcCategories = new HashMap<String, RTCCategory>();
        rtcCategories.put(categoryName, rtcCategory);
        
        DataSender sender = new DataSender(rtcCategories, 1);
        sender.subscribe("http://localhost:8080/opennms-webapp/rtc/post/" + categoryNameUrl, categoryName, "rtc", "rtc");
        sender.sendData();
    }
}
