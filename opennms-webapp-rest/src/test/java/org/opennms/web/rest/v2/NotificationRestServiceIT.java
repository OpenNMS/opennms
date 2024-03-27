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
package org.opennms.web.rest.v2;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
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
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class NotificationRestServiceIT extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationRestServiceIT.class);

    public NotificationRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        m_databasePopulator.populateDatabase();
    }

    @Test
    public void testFiql() throws Exception {
        String url = "/notifications";

        LOG.warn(sendRequest(GET, url, parseParamData("_s=notification.answeredBy==\u0000"), 200));
    }

    @Test
    public void testRootAliasFiltering() throws Exception {
        // ID that doesn't exist
        int id = Integer.MAX_VALUE;
        executeQueryAndVerify("_s=notification.notifyId==" + id, 0);
        executeQueryAndVerify("_s=notification.notifyId!=" + id, 1);
        executeQueryAndVerify("_s=notification.answeredBy==root", 0);
        executeQueryAndVerify("_s=notification.answeredBy!=root", 1);
        executeQueryAndVerify("_s=notification.numericMsg==Message", 0);
        executeQueryAndVerify("_s=notification.numericMsg!=Message", 1);
        executeQueryAndVerify("_s=notification.pageTime==1970-01-01T00:00:00.000-0000", 1);
        executeQueryAndVerify("_s=notification.pageTime!=1970-01-01T00:00:00.000-0000", 0);
        executeQueryAndVerify("_s=notification.queueId==Message", 0);
        executeQueryAndVerify("_s=notification.queueId!=Message", 1);
        executeQueryAndVerify("_s=notification.respondTime==1970-01-01T00:00:00.000-0000", 1);
        executeQueryAndVerify("_s=notification.respondTime!=1970-01-01T00:00:00.000-0000", 0);
        executeQueryAndVerify("_s=notification.subject==Message", 0);
        executeQueryAndVerify("_s=notification.subject!=Message", 1);
        executeQueryAndVerify("_s=notification.textMsg==Message", 0);
        executeQueryAndVerify("_s=notification.textMsg!=Message", 1);

        // Verify IP address queries including iplike queries
        executeQueryAndVerify("_s=ipAddress==192.168.1.1", 1);
        executeQueryAndVerify("_s=ipAddress!=192.168.1.1", 0);
        executeQueryAndVerify("_s=ipAddress==192.168.1.2", 0);
        executeQueryAndVerify("_s=ipAddress==192.*.*.1", 1);
        executeQueryAndVerify("_s=ipAddress==192.*.*.2", 0);
        executeQueryAndVerify("_s=ipAddress==192.168.1.1-2", 1);
        executeQueryAndVerify("_s=ipAddress==127.0.0.1", 0);
        executeQueryAndVerify("_s=ipAddress!=127.0.0.1", 1);

        // Verify IP address queries including iplike queries
        executeQueryAndVerify("_s=notification.ipAddress==192.168.1.1", 1);
        executeQueryAndVerify("_s=notification.ipAddress!=192.168.1.1", 0);
        executeQueryAndVerify("_s=notification.ipAddress==192.168.1.2", 0);
        executeQueryAndVerify("_s=notification.ipAddress==192.*.*.1", 1);
        executeQueryAndVerify("_s=notification.ipAddress==192.*.*.2", 0);
        executeQueryAndVerify("_s=notification.ipAddress==192.168.1.1-2", 1);
        executeQueryAndVerify("_s=notification.ipAddress==127.0.0.1", 0);
        executeQueryAndVerify("_s=notification.ipAddress!=127.0.0.1", 1);
}

    /**
     * Test filtering for properties of {@link OnmsCategory}. The implementation
     * for this filtering is different because the node-to-category relationship
     * is a many-to-many relationship.
     * 
     * @throws Exception
     */
    @Test
    public void testCategoryFiltering() throws Exception {
        int categoryId;
        categoryId = m_databasePopulator.getCategoryDao().findByName("Routers").getId();
        executeQueryAndVerify("_s=category.id==" + categoryId, 1);
        executeQueryAndVerify("_s=category.id!=" + categoryId, 0);

        // Category that doesn't exist
        categoryId = Integer.MAX_VALUE;
        executeQueryAndVerify("_s=category.id==" + categoryId, 0);
        executeQueryAndVerify("_s=category.id!=" + categoryId, 1);

        executeQueryAndVerify("_s=category.name==Routers", 1);
        executeQueryAndVerify("_s=category.name!=Routers", 0);
        executeQueryAndVerify("_s=category.name==Rou*", 1);
        executeQueryAndVerify("_s=category.name!=Rou*", 0);
        executeQueryAndVerify("_s=category.name==Ro*ers", 1);
        executeQueryAndVerify("_s=category.name!=Ro*ers", 0);
        executeQueryAndVerify("_s=category.name==DoesntExist", 0);
        executeQueryAndVerify("_s=category.name!=DoesntExist", 1);
    }

    private void executeQueryAndVerify(String query, int totalCount) throws Exception {
        if (totalCount == 0) {
            sendRequest(GET, "/notifications", parseParamData(query), 204);
        } else {
            JSONObject object = new JSONObject(sendRequest(GET, "/notifications", parseParamData(query), 200));
            Assert.assertEquals(totalCount, object.getInt("totalCount"));
        }
    }

}
