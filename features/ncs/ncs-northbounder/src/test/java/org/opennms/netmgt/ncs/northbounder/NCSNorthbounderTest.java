/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ncs.northbounder;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.http.JUnitHttpServerExecutionListener;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.ncs.northbounder.NCSNorthbounderConfig.HttpMethod;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests the HTTP North Bound Interface
 * FIXME: This is far from completed
 * 
 * @author <a mailto:brozow@opennms.org>Matt Brozowski</a>
 * @author <a mailto:david@opennms.org>David Hustace</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
	JUnitHttpServerExecutionListener.class
})
public class NCSNorthbounderTest {

	String url = "https://localhost/fmpm/restful/NotificationMessageRelay";
	
	String xml = "" +
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
			"<ServiceAlarmNotification xmlns=\"http://junosspace.juniper.net/monitoring\">\n" + 
			"    <ServiceAlarm>\n" + 
			"        <Id>FS:1</Id>\n" + 
			"        <Name>NAM1</Name>\n" + 
			"        <Status>Down</Status>\n" + 
			"    </ServiceAlarm>\n" + 
			"    <ServiceAlarm>\n" + 
			"        <Id>FS:2</Id>\n" + 
			"        <Name>NAM2</Name>\n" + 
			"        <Status>Up</Status>\n" + 
			"    </ServiceAlarm>\n" + 
			"    <ServiceAlarm>\n" + 
			"        <Id>FS:3</Id>\n" + 
			"        <Name>NAM3</Name>\n" + 
			"        <Status>Down</Status>\n" + 
			"    </ServiceAlarm>\n" + 
			"    <ServiceAlarm>\n" + 
			"        <Id>FS:4</Id>\n" + 
			"        <Name>NAM4</Name>\n" + 
			"        <Status>Up</Status>\n" + 
			"    </ServiceAlarm>\n" + 
			"</ServiceAlarmNotification>\n" + 
			"";	
	
	@Test
    @JUnitHttpServer(port=10342, https=false, webapps={
            @Webapp(context="/fmpm", path="src/test/resources/test-webapp")
    })
    public void testTestServlet() throws Exception {
    	
    	TestServlet.reset();
    	
        HttpClient client = new DefaultHttpClient();
        HttpEntity entity = new StringEntity(xml);
        HttpPost method = new HttpPost("http://localhost:10342/fmpm/restful/NotificationMessageRelay");
        method.setEntity(entity);
        HttpResponse response = client.execute(method);
        assertEquals(200, response.getStatusLine().getStatusCode());
        
        assertEquals(xml, TestServlet.getPosted());
        
    }


	// convert alarms to xml
	// configure batching
	// configure url
	// https
	// filter alarms
	// what about resolutions?

    @Test
    @JUnitHttpServer(port=10342, https=false, webapps={
            @Webapp(context="/fmpm", path="src/test/resources/test-webapp")
    })
    public void testForwardAlarms() throws Exception {
    	
    	TestServlet.reset();
        
        NCSNorthbounderConfig config = new NCSNorthbounderConfig();
        config.setScheme("http");
        config.setHost("localhost");
        config.setPort(10342);
        config.setPath("/fmpm/restful/NotificationMessageRelay");
        config.setMethod(HttpMethod.POST);

        NCSNorthbounder nb = new NCSNorthbounder(config);
        
        List<NorthboundAlarm> alarms = Arrays.asList(alarm(1), alarm(2), alarm(3), alarm(4));
        nb.forwardAlarms(alarms);
        
        assertEquals(xml, TestServlet.getPosted());
        
    }


	private NorthboundAlarm alarm(int alarmId) {
		OnmsAlarm alarm = new OnmsAlarm();
		alarm.setId(alarmId);
        alarm.setUei("uei.opennms.org/test/httpNorthBounder");
        alarm.setEventParms("componentType=Service(string,text);componentName=NAM"+alarmId+"(string,text);componentForeignSource=FS(string,text);componentForeignId="+alarmId+"(string,text);cause=17(string,text)");
        alarm.setAlarmType((alarmId+1) % 2 + 1);
        
        return new NorthboundAlarm(alarm);
	}

    @Test
    @JUnitHttpServer(port=10342, https=true, webapps={
            @Webapp(context="/fmpm", path="src/test/resources/test-webapp")
    })
    public void testForwardAlarmsToHttps() throws Exception {
    	
    	TestServlet.reset();
        
        NCSNorthbounderConfig config = new NCSNorthbounderConfig();
        config.setScheme("https");
        config.setHost("localhost");
        config.setPort(10342);
        config.setPath("/fmpm/restful/NotificationMessageRelay");
        config.setMethod(HttpMethod.POST);

        NCSNorthbounder nb = new NCSNorthbounder(config);
        
        List<NorthboundAlarm> alarms = Arrays.asList(alarm(1), alarm(2), alarm(3), alarm(4));
        nb.forwardAlarms(alarms);

        assertEquals(xml, TestServlet.getPosted());
        
    }
}
