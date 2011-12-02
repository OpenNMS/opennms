package org.opennms.netmgt.alarmd.api.support.http;

import org.junit.Test;
import org.opennms.netmgt.alarmd.api.Alarm;
import org.opennms.netmgt.alarmd.api.support.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.support.http.HttpNorthbounderConfig.HttpMethod;
import org.opennms.netmgt.model.OnmsAlarm;

public class HttpNorthBounderTest {

    @Test
    public void testForwardAlarms() {
        
        HttpNorthbounder nb = new HttpNorthbounder();
        HttpNorthbounderConfig config = new HttpNorthbounderConfig("localhost");
        config.setMethod(HttpMethod.POST);
        config.setPath("/jms/post");
        
        nb.setConfig(config);
        
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setId(1);
        alarm.setUei("uei.opennms.org/test/httpNorthBounder");
        
        Alarm a = new NorthboundAlarm(alarm);
        
        nb.onAlarm(a);
    }

}
