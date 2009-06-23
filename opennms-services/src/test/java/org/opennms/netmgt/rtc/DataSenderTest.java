//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Feb 10: Organize imports. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.rtc;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.RTCConfigFactory;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.filter.FilterParseException;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.rtc.datablock.RTCCategory;
import org.opennms.test.ConfigurationTestUtils;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

public class DataSenderTest extends OpenNMSTestCase {
    /*
     * This doesn't work unless we have a receiver on the other end.... more of an integration test
     */
    public void XXXtestSendData() throws MarshalException, ValidationException, IOException, FilterParseException, SAXException, SQLException, RTCException {
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
