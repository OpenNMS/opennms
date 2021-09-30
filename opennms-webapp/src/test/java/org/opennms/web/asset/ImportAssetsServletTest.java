/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.asset;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * ImportAssetsServletTest.java
 * </p>
 * 
 * @author <a href="mailto:markus@opennms.com">Markus Neumann</a>
 * @version $Id: $
 */

public class ImportAssetsServletTest {

    private Logger logger = LoggerFactory.getLogger(ImportAssetsServletTest.class);
    private ImportAssetsServlet m_importAssetServlet;

    private List<String> m_assetsAsCvs;
    private String m_testCvs = "";

    @Before
    public void setUp() throws InterruptedException {
        m_importAssetServlet = new ImportAssetsServlet();
        m_assetsAsCvs = new ArrayList<>();
        m_assetsAsCvs.add("\"TakPad\",\"1\",\"Unspecified\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"cassandra\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\" \",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"4\"");
        m_assetsAsCvs.add("'Indigo','2','Unspecified','','','','','','','','','','','','','','','','','','','Jasperrrr','','','','','','','','','','','','','','','','','','','','',' ','','','','','','','','','','','','','','','','3'");
        m_assetsAsCvs.add("S. Lem,3,Unspecified,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,8");
        m_assetsAsCvs.add("Fake Node Label,431,Category,Manufacturer,Vendor,Model Number,Serial Number,Description,Circuit ID,Asset Number,Operating System,Rack,Slot,Port,Region,Division,Department,Address 1,Address 2,City,State,Zip,Building,Floor,Room,Vendor Phone,Vendor Fax,Date Installed,Lease,Lease Expires,Support Phone,Maint Contract,Vendor Asset Number,Maint Contract Expires,Display Category,Notification Category,Poller Category,Threshold Category,Username,Password,Enable,Connection,A,Comments,Cpu,Ram,Storage Controller,HDD 1,HDD 2,HDD 3,HDD 4,HDD 5,HDD 6,9,Inputpower,Additional hardware,Admin,SNMP Community,12");
        m_assetsAsCvs.add("\"Marria\",\"5\",\"Unspecified\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"Jasperrrr\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\" \",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"4\"");

        for (String assetCvs : m_assetsAsCvs) {
            m_testCvs = m_testCvs.concat(assetCvs + "\n");
        }
    }

    @Test
    public void testDecodeAssetsText() throws Exception {
        List<Asset> decodedAssets;
        logger.debug(m_testCvs);
        decodedAssets = m_importAssetServlet.decodeAssetsText(m_testCvs);
        Assert.assertEquals(5, decodedAssets.size());
        for (Asset decodedAsset : decodedAssets) {
            logger.debug("decodedAsset:'{}'", decodedAsset);
        }
    }
}
