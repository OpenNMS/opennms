/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.opennms.core.rpc.api.RemoteExecutionException;
import org.opennms.core.rpc.api.RequestTimedOutException;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.snmp.SnmpAgentTimeoutException;
import org.opennms.netmgt.snmp.SnmpException;

public class SnmpExceptionValidatorTest {


    @Test
    public void testSnmpRelatedException() {

        // Any SnmpException and SnmpAgentTimeoutException are valid.
        SnmpException snmpException = new SnmpException(new IOException("Invalid config"));
        String errorMessage = RemoteExecutionException.toErrorMessage(snmpException);
        RemoteExecutionException remoteException = new RemoteExecutionException(errorMessage);
        ExecutionException executionException = new ExecutionException(remoteException);
        assertTrue(NodeInfoScan.isSnmpRelatedException(executionException));

        SnmpAgentTimeoutException snmpAgentTimeoutException = new
                SnmpAgentTimeoutException("snmp-walker", InetAddressUtils.getInetAddress("10.0.0.5"));
        errorMessage = RemoteExecutionException.toErrorMessage(snmpAgentTimeoutException);
        remoteException = new RemoteExecutionException(errorMessage);
        executionException = new ExecutionException(remoteException);
        assertTrue(NodeInfoScan.isSnmpRelatedException(executionException));

        //Nested Snmp Exceptions are also valid.
        executionException = new ExecutionException(snmpException);
        assertTrue(NodeInfoScan.isSnmpRelatedException(executionException));

        executionException = new ExecutionException(snmpException);
        assertTrue(NodeInfoScan.isSnmpRelatedException(executionException));

        remoteException = new RemoteExecutionException("invalid config");
        executionException = new ExecutionException(remoteException);
        assertFalse(NodeInfoScan.isSnmpRelatedException(executionException));

        RequestTimedOutException timedOutException = new RequestTimedOutException(new TimeoutException());
        executionException = new ExecutionException(timedOutException);
        assertFalse(NodeInfoScan.isSnmpRelatedException(executionException));

    }
}
