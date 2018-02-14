/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
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
import org.opennms.web.api.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The Test Class for AssetSuggestionsRestService.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
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
public class AssetSuggestionsRestServiceIT extends AbstractSpringJerseyRestTestCase {

    /** The transaction template. */
    @Autowired
    private TransactionTemplate m_template;

    /** The database populator. */
    @Autowired
    private DatabasePopulator m_databasePopulator;

    /** The servlet context. */
    @Autowired
    private ServletContext m_servletContext;

    /* (non-Javadoc)
     * @see org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase#afterServletStart()
     */
    @Override
    protected void afterServletStart() {
        MockLogAppender.setupLogging(true, "DEBUG");
        m_template.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                m_databasePopulator.populateDatabase();

                m_databasePopulator.getNode1().getAssetRecord().getGeolocation().setCity("New York");
                m_databasePopulator.getNodeDao().update(m_databasePopulator.getNode1());
                m_databasePopulator.getNode2().getAssetRecord().getGeolocation().setCity("San Francisco");
                m_databasePopulator.getNodeDao().update(m_databasePopulator.getNode2());
                m_databasePopulator.getNode3().getAssetRecord().getGeolocation().setCity("Boston");
                m_databasePopulator.getNodeDao().update(m_databasePopulator.getNode3());
                m_databasePopulator.getNode4().getAssetRecord().getGeolocation().setCity("Chicago");
                m_databasePopulator.getNodeDao().update(m_databasePopulator.getNode4());
                m_databasePopulator.getNode5().getAssetRecord().getGeolocation().setCity("Los Angeles");
                m_databasePopulator.getNodeDao().update(m_databasePopulator.getNode5());
                m_databasePopulator.getNodeDao().flush();
            }
        });
    }

    /**
     * Test suggestions.
     *
     * @throws Exception the exception
     */
    @Test
    @JUnitTemporaryDatabase
    public void testSuggestions() throws Exception {
        String xml = sendRequest(GET, "/assets/suggestions", 200);
        Assert.assertTrue(xml.contains("<suggestion>Boston</suggestion>"));
    }

    /**
     * Test suggestions JSON.
     *
     * @throws Exception the exception
     */
    @Test
    @JUnitTemporaryDatabase
    public void testSuggestionsJson() throws Exception {
        MockHttpServletRequest jsonRequest = createRequest(m_servletContext, GET, "/assets/suggestions", "admin", Arrays.asList(new String[]{ Authentication.ROLE_ADMIN }));
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        String json = sendRequest(jsonRequest, 200);
        JSONArray cities = new JSONObject(json).getJSONObject("city").getJSONArray("suggestion");
        Assert.assertEquals(5, cities.length());
        Assert.assertEquals("Boston", cities.get(0));
    }

}
