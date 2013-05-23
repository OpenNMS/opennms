/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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
