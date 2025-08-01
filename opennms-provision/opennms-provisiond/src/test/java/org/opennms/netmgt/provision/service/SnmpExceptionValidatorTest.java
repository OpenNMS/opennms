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
