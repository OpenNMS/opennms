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
package org.opennms.netmgt.enlinkd.snmp;

import java.util.Objects;

import org.opennms.netmgt.snmp.ErrorStatusException;
import org.opennms.netmgt.snmp.NamedSnmpVar;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CiscoVtpTracker extends AggregateTracker
{
	private static final Logger LOG = LoggerFactory.getLogger(CiscoVtpTracker.class);
    /**
     * the bridge type
     */
	//
	// Lookup strings for specific table entries
	//
	public final static	String CISCO_VTP_VERSION	= "vtpVersion";
    public final static	String CISCO_VTP_VERSION_OID	= ".1.3.6.1.4.1.9.9.46.1.1.1";

    public enum VtpVersion {
        one(1), two(2), none(3), three(4);

        private final Integer value;

        VtpVersion(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }

        public static VtpVersion getByValue(Integer vtpVersion) {
            for (VtpVersion version: values()) {
                if (Objects.equals(version.getValue(), vtpVersion)) {
                    return version;
                }
            }
            return null;
        }
    }

	public final static NamedSnmpVar[] ms_elemList = new NamedSnmpVar[] {
		/*
		 * vtpVersion OBJECT-TYPE
    	 * SYNTAX          INTEGER  {
         *               one(1),
         *               two(2),
         *               none(3),
         *               three(4)
         *           }
    	 * MAX-ACCESS      read-only
    	 * STATUS          current
    	 * DESCRIPTION
         *	"The version of VTP in use on the local system.  A device
         *	will report its version capability and not any particular
         *	version in use on the device. If the device does not support
         *	vtp, the version is none(3)."
		 */
		new NamedSnmpVar(NamedSnmpVar.SNMPINT32, CISCO_VTP_VERSION,CISCO_VTP_VERSION_OID)
	};

    private final SnmpStore m_store;
	
	/**
	 * <P>The class constructor is used to initialize the collector
	 * and send out the initial SNMP packet requesting data. The
	 * data is then received and store by the object. When all the
	 * data has been collected the passed signaler object is <EM>notified</em>
	 * using the notifyAll() method.</P>
	 *
	 */
	public CiscoVtpTracker() {
        super(NamedSnmpVar.getTrackersFor(ms_elemList));
        m_store = new SnmpStore(ms_elemList); 
	}

    /** {@inheritDoc} */
    protected void storeResult(SnmpResult res) {
        m_store.storeResult(res);
    }

    /** {@inheritDoc} */
    protected void reportGenErr(final String msg) {
        LOG.warn("Error retrieving vtpVersion: {}", msg);
    }

    /** {@inheritDoc} */
    protected void reportNoSuchNameErr(final String msg) {
        LOG.info("Error retrieving vtpVersion: {}", msg);
    }

    @Override
    protected void reportFatalErr(final ErrorStatusException ex) {
        LOG.warn("Error retrieving vtpVersion: {}", ex.getMessage(), ex);
    }

    /**
     * <p>getBridgeAddress</p>
     *
     * @return a {@link Integer} object.
     */
    public Integer getVtpVersion() {
        return m_store.getInt32(CISCO_VTP_VERSION);
    }

    public VtpVersion decodeVtpVersion() {
        return VtpVersion.getByValue(m_store.getInt32(CISCO_VTP_VERSION));
    }

    @Override
    public void printSnmpData() {
        System.out.printf("\t\t%s (%s)= %s (%s)\n", CISCO_VTP_VERSION_OID, CISCO_VTP_VERSION, getVtpVersion(), decodeVtpVersion());
    }
}
