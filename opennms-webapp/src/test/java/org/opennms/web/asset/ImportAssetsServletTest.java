/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
