//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.invd;

import org.opennms.netmgt.config.invd.InvdPackage;
import org.opennms.netmgt.config.invd.InvdService;
import org.opennms.netmgt.config.invd.InvdServiceParameter;

public class ScannerTestUtils {
    static ScannerSpecification createScannerSpec(String svcName, InventoryScanner svcCollector, String collectionName) {
        InvdPackage pkg = new InvdPackage();
        pkg.setFilter("IPADDR IPLIKE *.*.*.*");
        InvdService svc = new InvdService();
        svc.setName(svcName);
        InvdServiceParameter param = new InvdServiceParameter();
        param.setKey("collection");
        param.setValue("collectionName");
        svc.addServiceParameter(param);
        pkg.addService(svc);
        
        return new ScannerSpecification(pkg, svcName, svcCollector);
    }
}
