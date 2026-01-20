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
package org.opennms.web.rest.v1;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
@Transactional
public class AssetRecordResourceIT extends AbstractSpringJerseyRestTestCase {


    @Autowired
    private TransactionTemplate template;

    @Autowired
    private DatabasePopulator databasePopulator;

    private Date yesterday = new Date(System.currentTimeMillis()-(1000*60*24));

    @Override
    protected void afterServletStart() {
        MockLogAppender.setupLogging(true, "INFO");
        template.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                databasePopulator.populateDatabase();
                databasePopulator.getNode1().getAssetRecord().setLastModifiedBy("oldUser");
                databasePopulator.getNode1().getAssetRecord().setLastModifiedDate(yesterday);
                databasePopulator.getNodeDao().flush();
            }
        });
    }


    @Test
    @JUnitTemporaryDatabase
    public void shouldUpdateLastModifiedWithCurrentUser() throws Exception {

        String username="admin";
        Map<String, String> params = new HashMap<>();
        params.put("operatingSystem", "Linux");
        params.put("category", "Unspecified");
        params.put("password", "admin");
        params.put("description", "");
        params.put("username", "admin");
        params.put("manufacturer", "");
        params.put("autoenable", "");
        params.put("comment", "");
        setUser(username, new String[]{});
        sendRequest(PUT, "/nodes/1/assetRecord", params, 204);

        assertEquals(username, databasePopulator.getNode1().getAssetRecord().getLastModifiedBy());
        assertTrue("lastModifiedDate should be within the last second"
                , databasePopulator.getNode1().getAssetRecord().getLastModifiedDate().after(new Date(System.currentTimeMillis()-1000)));
    }

}

