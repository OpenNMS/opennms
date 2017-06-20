package org.opennms.smoketest.minion;

import java.net.InetSocketAddress;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;

import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.smoketest.utils.RestClient;


import static org.junit.Assert.assertEquals;

public class MinionRestServiceIT {

    private static final String MINION_ID = "0001-1234";
    
    @Test
    @Ignore
    public void verifyMinionRestEndPoint() {

        final InetSocketAddress opennmsHttp = new InetSocketAddress("localhost", 8980);
        RestClient client = new RestClient(opennmsHttp);

        OnmsMinion minion = new OnmsMinion();
        minion.setId(MINION_ID);
        minion.setLocation("minion");
        minion.setType(OnmsMonitoringSystem.TYPE_MINION);

        Response response = client.addMinion(minion);
        assertEquals(201, response.getStatus());
        
        OnmsMinion minionReturned = client.getMinion(MINION_ID);
        assertEquals(minionReturned.getId(), MINION_ID);

        // Delete minion doesn't work on docker setup otherwise this test can move to docker
        response = client.deleteMinion(MINION_ID);
        assertEquals(200, response.getStatus());
        
    }

}
