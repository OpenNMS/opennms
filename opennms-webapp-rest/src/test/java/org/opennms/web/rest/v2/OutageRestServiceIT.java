/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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
public class OutageRestServiceIT extends AbstractSpringJerseyRestTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(OutageRestServiceIT.class);
    
    public OutageRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        m_databasePopulator.populateDatabase();
    }

    /**
     * The open outage and closed outage from {@link DatabasePopulator}
     * have different {@code ifLostService} times.
     * 
     * @throws Exception
     */
    @Test
    public void testOutages() throws Exception {
        String url = "/outages";

        JSONObject object = new JSONObject(sendRequest(GET, url, parseParamData("orderBy=id"), 200));
        Assert.assertEquals(2, object.getInt("totalCount"));

        // Check timestamp comparisons
        sendRequest(GET, url, parseParamData("_s=outage.ifLostService=gt=2017-04-01T00:00:00.000-0400"), 204);
        object = new JSONObject(sendRequest(GET, url, parseParamData("_s=outage.ifLostService=le=2017-04-01T00:00:00.000-0400"), 200));
        Assert.assertEquals(2, object.getInt("totalCount"));


        // Check for open outages (ifRegainedService is null) with UTC timestamp
        object = new JSONObject(sendRequest(GET, url, parseParamData("_s=outage.ifRegainedService==1970-01-01T00:00:00.000-0000"), 200));
        Assert.assertEquals(1, object.getInt("totalCount"));
        Assert.assertEquals(1436881545000L, ((JSONObject)object.getJSONArray("outage").get(0)).getLong("ifLostService"));

        // Check for closed outages (ifRegainedService is not null) with UTC timestamp
        object = new JSONObject(sendRequest(GET, url, parseParamData("_s=outage.ifRegainedService!=1970-01-01T00:00:00.000-0000"), 200));
        Assert.assertEquals(1, object.getInt("totalCount"));
        Assert.assertEquals(1436881548292L, ((JSONObject)object.getJSONArray("outage").get(0)).getLong("ifLostService"));


        // Check for open outages (ifRegainedService is null) with negative UTC offset timestamp
        object = new JSONObject(sendRequest(GET, url, parseParamData("_s=outage.ifRegainedService==1969-12-31T19:00:00.000-0500"), 200));
        Assert.assertEquals(1, object.getInt("totalCount"));
        Assert.assertEquals(1436881545000L, ((JSONObject)object.getJSONArray("outage").get(0)).getLong("ifLostService"));

        // Check for closed outages (ifRegainedService is not null) with negative UTC offset timestamp
        object = new JSONObject(sendRequest(GET, url, parseParamData("_s=outage.ifRegainedService!=1969-12-31T19:00:00.000-0500"), 200));
        Assert.assertEquals(1, object.getInt("totalCount"));
        Assert.assertEquals(1436881548292L, ((JSONObject)object.getJSONArray("outage").get(0)).getLong("ifLostService"));


        // Check for open outages (ifRegainedService is null) with positive UTC offset timestamp
        object = new JSONObject(sendRequest(GET, url, parseParamData("_s=outage.ifRegainedService==1970-01-01T04:00:00.000%252B0400"), 200));
        Assert.assertEquals(1, object.getInt("totalCount"));
        Assert.assertEquals(1436881545000L, ((JSONObject)object.getJSONArray("outage").get(0)).getLong("ifLostService"));

        // Check for closed outages (ifRegainedService is not null) with positve UTC offset timestamp
        object = new JSONObject(sendRequest(GET, url, parseParamData("_s=outage.ifRegainedService!=1970-01-01T04:00:00.000%252B0400"), 200));
        Assert.assertEquals(1, object.getInt("totalCount"));
        Assert.assertEquals(1436881548292L, ((JSONObject)object.getJSONArray("outage").get(0)).getLong("ifLostService"));


        // Check for outages with null suppressTime
        object = new JSONObject(sendRequest(GET, url, parseParamData("_s=outage.suppressTime==1970-01-01T00:00:00.000-0000"), 200));
        Assert.assertEquals(2, object.getInt("totalCount"));

        // Check for lack of outages with non-null suppressTime
        sendRequest(GET, url, parseParamData("_s=outage.suppressTime!=1970-01-01T00:00:00.000-0000"), 204);

    }

}
