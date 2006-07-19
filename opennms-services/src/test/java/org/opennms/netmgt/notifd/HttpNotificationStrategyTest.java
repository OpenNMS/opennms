package org.opennms.netmgt.notifd;

import java.util.ArrayList;
import java.util.List;

import org.opennms.core.utils.Argument;
import org.opennms.netmgt.mock.OpenNMSTestCase;

public class HttpNotificationStrategyTest extends OpenNMSTestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'org.opennms.netmgt.notifd.HttpNotificationStrategy.send(List)'
     */
    public void XXXtestSend() {
        
        try {
        NotificationStrategy ns = new HttpNotificationStrategy();
        List arguments = new ArrayList();
        Argument arg = null;
        arg = new Argument("url", null, "http://172.16.8.68/cgi-bin/noauth/nmsgw.pl", false);
        arguments.add(arg);
        arg = new Argument("post-NodeID", null, "1", false);
        arguments.add(arg);
//        arg = new Argument("post-event", null, "199", false);
//        arguments.add(arg);
//        arg = new Argument("post-nasid", null, "1", false);
//        arguments.add(arg);
//        arg = new Argument("post-message", null, "JUnit Test RT Integration", false);
//        arguments.add(arg);
        
//        arg = new Argument("result-match", null, ".*OK\\s([0-9]+)\\s.*", false);
        arg = new Argument("result-match", null, "(?s).*OK\\s+([0-9]+).*", false);
        arguments.add(arg);
        
        arg = new Argument("post-message", null, "-tm", false);
        arguments.add(arg);
        
        arg = new Argument("-tm", null, "text message for unit testing", false);
        arguments.add(arg);
        
        arg = new Argument("sql", null, "UPDATE alarms SET tticketID=${1} WHERE lastEventID = 1", false);
        arguments.add(arg);
        
        ns.send(arguments);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Caught Exception:");
        }
    }

}
