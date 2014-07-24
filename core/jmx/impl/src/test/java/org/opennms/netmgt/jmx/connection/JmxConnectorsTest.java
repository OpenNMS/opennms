package org.opennms.netmgt.jmx.connection;


import org.opennms.netmgt.jmx.connection.connectors.DefaultConnectionManager;
import org.junit.Assert;
import org.junit.Test;
import java.lang.reflect.Field;

public class JmxConnectorsTest {

    @Test
    public void testConnectionHasConnectorAssigned() throws IllegalAccessException, JmxServerConnectionException {
        DefaultConnectionManager connectionManager = new DefaultConnectionManager();

        for (Field eachField : JmxConnectors.class.getFields()) {
            Object connectorName = eachField.get(null);
            Assert.assertNotNull(connectorName);
            Assert.assertEquals(String.class, connectorName.getClass());
            Assert.assertFalse("ConnectorName should not be an empty string", ((String) connectorName).isEmpty());
            Assert.assertNotNull(connectionManager.getConnector((String) connectorName));
        }
    }
}
