package org.opennms.netmgt.jmx.connection;

import junit.framework.Assert;
import org.junit.Test;
import org.opennms.netmgt.jmx.connection.connectors.DefaultConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class ConnectorsTest {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectorsTest.class);

    @Test
    public void testConnectionHasConnectorAssigned() throws IllegalAccessException, MBeanServerConnectionException {
        DefaultConnectionManager connectionManager = new DefaultConnectionManager();

        for (Field eachField : Connectors.class.getFields()) {

            Object connectorName = eachField.get(null);
            Assert.assertNotNull(connectorName);
            Assert.assertEquals(String.class, connectorName.getClass());
            Assert.assertFalse("ConnectorName should not be an empty string", ((String) connectorName).isEmpty());
            Assert.assertNotNull(connectionManager.getConnector((String) connectorName));
        }
    }
}
