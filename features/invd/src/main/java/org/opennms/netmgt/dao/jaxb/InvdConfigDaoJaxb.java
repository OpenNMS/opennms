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
package org.opennms.netmgt.dao.jaxb;

import org.opennms.netmgt.dao.InvdConfigDao;
import org.opennms.netmgt.dao.jaxb.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.invd.InvdConfiguration;
import org.opennms.netmgt.config.invd.InvdPackage;
import org.opennms.netmgt.config.invd.InvdScanner;

import java.util.List;

public class InvdConfigDaoJaxb extends AbstractJaxbConfigDao<InvdConfiguration, InvdConfiguration> implements InvdConfigDao {
    public InvdConfigDaoJaxb() {
    	super(InvdConfiguration.class, "Inventory Daemon Configuration");
    }

    public InvdConfiguration getConfig() {
    	return getContainer().getObject();
    }
    
    public int getSchedulerThreads() {
        return getConfig().getThreadCount();
    }

    public List<InvdScanner> getScanners() {
        return getConfig().getScanners();
    }

    public void rebuildPackageIpListMap() {
        getConfig().createPackageIpListMap();
    }

    public List<InvdPackage> getPackages() {
        return getConfig().getPackages();
    }

    public InvdPackage getPackage(String name) {
        return getConfig().getPackage(name);
    }
    
    @Override
    public InvdConfiguration translateConfig(InvdConfiguration jaxbConfig) {
    	jaxbConfig.createIpLists();
        return jaxbConfig;
    }
}
