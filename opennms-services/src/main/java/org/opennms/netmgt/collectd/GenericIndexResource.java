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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.Collection;

import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.snmp.SnmpInstId;

public class GenericIndexResource extends CollectionResource {

	private SnmpInstId m_inst;
	private String m_name;

	public GenericIndexResource(ResourceType def, String name, SnmpInstId inst) {
		super(def);
		m_name = name;
		m_inst = inst;
	}

	// XXX should be based on the storageStrategy
	@Override
    public File getResourceDir(RrdRepository repository) {
        File rrdBaseDir = repository.getRrdBaseDir();
        File nodeDir = new File(rrdBaseDir, String.valueOf(getCollectionAgent().getNodeId()));
        File typeDir = new File(nodeDir, m_name);
        File instDir = new File(typeDir, m_inst.toString());
        log().debug("getResourceDir: " + instDir.toString());
        return instDir;
    }

    public String toString() {
        return "Node["+getCollectionAgent().getNodeId() + "]/type[" + m_name + "]/instance[" + m_inst + "]";
    }


	@Override
	protected int getType() {
		return -1;	// XXX is this right?
	}

	@Override
	public boolean shouldPersist(ServiceParameters params) {
		return true;// XXX should be based on the persistanceSelectorStrategy
	}

}
