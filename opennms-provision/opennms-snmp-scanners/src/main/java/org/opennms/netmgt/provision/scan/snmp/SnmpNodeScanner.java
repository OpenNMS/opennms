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
package org.opennms.netmgt.provision.scan.snmp;

import org.opennms.netmgt.provision.ScanContext;
import org.opennms.netmgt.snmp.SnmpResult;


/**
 * <p>SnmpNodeScanner class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SnmpNodeScanner extends AbstractSnmpScanner {
    
    /**
     * <p>Constructor for SnmpNodeScanner.</p>
     */
    public SnmpNodeScanner() {
        super("Node Scanner");
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        getSingleInstance(".1.3.6.1.2.1.1.2", "0").andStoreIn(sysObjectId());
        getSingleInstance(".1.3.6.1.2.1.1.5", "0").andStoreIn(sysName());
        getSingleInstance(".1.3.6.1.2.1.1.1", "0").andStoreIn(sysDescription());
        getSingleInstance(".1.3.6.1.2.1.1.6", "0").andStoreIn(sysLocation());
        getSingleInstance(".1.3.6.1.2.1.1.4", "0").andStoreIn(sysContact());
    }

    /**
     * <p>sysObjectId</p>
     *
     * @return a Storer object.
     */
    public static Storer sysObjectId() {
        return new Storer() {
            @Override
            public void storeResult(ScanContext scanContext, SnmpResult res) {
                scanContext.updateSysObjectId(res.getValue().toDisplayString());
            }
        };
    }

    /**
     * <p>sysName</p>
     *
     * @return a Storer object.
     */
    public static Storer sysName() {
        return new Storer() {
            @Override
            public void storeResult(ScanContext scanContext, SnmpResult res) {
                scanContext.updateSysName(res.getValue().toDisplayString());
            }
        };
    }

    /**
     * <p>sysDescription</p>
     *
     * @return a Storer object.
     */
    public static Storer sysDescription() {
        return new Storer() {
            @Override
            public void storeResult(ScanContext scanContext, SnmpResult res) {
                scanContext.updateSysDescription(res.getValue().toDisplayString());
            }
        };
    }

    /**
     * <p>sysLocation</p>
     *
     * @return a Storer object.
     */
    public static Storer sysLocation() {
        return new Storer() {
            @Override
            public void storeResult(ScanContext scanContext, SnmpResult res) {
                scanContext.updateSysLocation(res.getValue().toDisplayString());
            }
        };
    }

    /**
     * <p>sysContact</p>
     *
     * @return a Storer object.
     */
    public static Storer sysContact() {
        return new Storer() {
            @Override
            public void storeResult(ScanContext scanContext, SnmpResult res) {
                scanContext.updateSysContact(res.getValue().toDisplayString());
            }
        };
    }
}
