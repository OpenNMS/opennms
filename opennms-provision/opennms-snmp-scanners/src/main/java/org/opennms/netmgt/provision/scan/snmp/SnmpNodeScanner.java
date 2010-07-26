/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
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
    public void onInit() {
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
    public Storer sysObjectId() {
        return new Storer() {
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
    public Storer sysName() {
        return new Storer() {
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
    public Storer sysDescription() {
        return new Storer() {
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
    public Storer sysLocation() {
        return new Storer() {
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
    public Storer sysContact() {
        return new Storer() {
            public void storeResult(ScanContext scanContext, SnmpResult res) {
                scanContext.updateSysContact(res.getValue().toDisplayString());
            }
        };
    }
}
