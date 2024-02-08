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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAcknowledgmentCollection;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

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
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class AcknowledgmentRestServiceIT extends AbstractSpringJerseyRestTestCase {
	@Autowired
	private TransactionTemplate m_template;

	@Autowired
	private DatabasePopulator m_databasePopulator;

	@Autowired
	private ServletContext m_servletContext;

	@Override
	protected void afterServletStart() {
		m_template.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				m_databasePopulator.populateDatabase();
			}
		});
	}

	@Test
	@JUnitTemporaryDatabase
	public void testGetAcksJson() throws Exception {
		String url = "/acks";

		// GET all items
		MockHttpServletRequest jsonRequest = createRequest(m_servletContext, GET, url);
		jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
		String json = sendRequest(jsonRequest, 200);

		JSONObject restObject = new JSONObject(json);
		JSONObject expectedObject = new JSONObject(IOUtils.toString(new FileInputStream("src/test/resources/v1/acks.json")));
		JSONAssert.assertEquals(expectedObject, restObject, true);
	}

	@Test
	@JUnitTemporaryDatabase
	public void testAcknowlegeNotification() throws Exception {
	    final Pattern p = Pattern.compile("^.*<answeredBy>(.*?)</answeredBy>.*$", Pattern.DOTALL & Pattern.MULTILINE);
	    sendData(POST, MediaType.APPLICATION_FORM_URLENCODED, "/acks", "notifId=1&action=ack", 200);
	    String xml = sendRequest(GET, "/notifications/1", new HashMap<String,String>(), 200);
	    Matcher m = p.matcher(xml);
	    assertTrue(m.matches());
	    assertTrue(m.group(1).equals("admin"));
	    sendData(POST, MediaType.APPLICATION_FORM_URLENCODED, "/acks", "notifId=1&action=unack", 200);
	    xml = sendRequest(GET, "/notifications/1", new HashMap<String,String>(), 200);
	    m = p.matcher(xml);
	    assertFalse(m.matches());
	}

	@Test
	@JUnitTemporaryDatabase
	public void testAcknowlegeAlarm() throws Exception {
	    final Pattern p = Pattern.compile("^.*<ackTime>(.*?)</ackTime>.*$", Pattern.DOTALL & Pattern.MULTILINE);
	    sendData(POST, MediaType.APPLICATION_FORM_URLENCODED, "/acks", "alarmId=1&action=ack", 200);

	    // Try to fetch a non-existent ack, get 404 Not Found
	    String xml = sendRequest(GET, "/acks/999999", 404);

	    xml = sendRequest(GET, "/acks/count", 200);
	    // {@link DatabasePopulator} adds one ack so we have 2 total
	    assertEquals("2", xml);

	    Integer newAckId = null;
	    for (OnmsAcknowledgment ack : getXmlObject(JaxbUtils.getContextFor(OnmsAcknowledgmentCollection.class), "/acks", 200, OnmsAcknowledgmentCollection.class).getObjects()) {
	        if (AckType.UNSPECIFIED.equals(ack.getAckType())) {
	            // Ack from DatabasePopulator
	            assertEquals(AckAction.UNSPECIFIED, ack.getAckAction());
	            assertEquals("admin", ack.getAckUser());
	        } else if (AckType.ALARM.equals(ack.getAckType())) {
	            // Ack that we just created
	            assertEquals(new Integer(1), ack.getRefId());
	            assertEquals(AckAction.ACKNOWLEDGE, ack.getAckAction());
	            newAckId = ack.getId();
	        } else {
	            fail("Unrecognized alarm type: " + ack.getAckType().toString());
	        }
	    }

	    if (newAckId == null) {
	        fail("Couldn't determine ID of new ack");
	    }

	    xml = sendRequest(GET, "/acks/" + newAckId, 200);

	    xml = sendRequest(GET, "/alarms/1", new HashMap<String,String>(), 200);
	    Matcher m = p.matcher(xml);
	    assertTrue(m.matches());
	    assertTrue(m.group(1).length() > 0);
	    sendData(POST, MediaType.APPLICATION_FORM_URLENCODED, "/acks", "alarmId=1&action=unack", 200);
	    xml = sendRequest(GET, "/alarms/1", new HashMap<String,String>(), 200);
	    m = p.matcher(xml);
	    assertFalse(m.matches());

	    // POST with no argument, this will ack by default
	    sendData(POST, MediaType.APPLICATION_FORM_URLENCODED, "/acks", "alarmId=1", 200);
	    xml = sendRequest(GET, "/alarms/1", new HashMap<String,String>(), 200);
	    m = p.matcher(xml);
	    assertTrue(m.matches());
	}

	@Test
	@JUnitTemporaryDatabase
	public void testAcknowlegeAlarmWithoutPermission() throws Exception {
		setUser("", new String[]{});
		sendData(POST, MediaType.APPLICATION_FORM_URLENCODED, "/acks", "alarmId=1&action=ack", 403);
	}
}
