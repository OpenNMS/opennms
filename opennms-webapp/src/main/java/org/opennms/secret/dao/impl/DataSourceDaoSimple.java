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
package org.opennms.secret.dao.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import org.opennms.secret.dao.DataSourceDao;
import org.opennms.secret.model.DataSource;
import org.opennms.secret.model.InterfaceService;
import org.opennms.secret.model.Node;
import org.opennms.secret.model.NodeInterface;

/**
 * <p>DataSourceDaoSimple class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class DataSourceDaoSimple implements DataSourceDao {
    private HashMap<String, DataSource> m_dataSources = new HashMap<String, DataSource>();

	/** {@inheritDoc} */
	public void inititalize(Object obj) {
	}

	/** {@inheritDoc} */
	public List<DataSource> getDataSourcesByInterface(NodeInterface iface) {
		List<DataSource> dataSources = new LinkedList<DataSource>();
		DataSource a = new DataSource();
		a.setId ("interface-" + iface.getId() + "-performance-" + "ifInOctets");
		a.setName ("In Octets");
		a.setSource ("/tmp/rrd/ifInOctets.rrd");
		a.setDataSource ("ifInOctets");
		dataSources.add (a);
        		
		DataSource b = new DataSource();
		b.setId ("interface-" + iface.getId() + "-performance-" + "ifOutOctets");
		b.setName ("Out Octets");
		b.setSource ("/tmp/rrd/ifOutOctets.rrd");
		b.setDataSource ("ifOutOctets");
		dataSources.add (b);
		
		DataSource c = new DataSource();
		c.setId ("interface-" + iface.getId() + "-performance-" + "ifInErrors");
		c.setName ("In Errors");
		c.setSource ("/tmp/rrd/ifInErrors.rrd");
		c.setDataSource ("ifInErrors");
		dataSources.add (c);
		
		DataSource d = new DataSource();
		d.setId ("interface-" + iface.getId() + "-performance-" + "ifInDiscards");
		d.setName ("In Discards");
		d.setSource ("/tmp/rrd/ifInDiscards.rrd");
		d.setDataSource ("ifInDiscards");
		dataSources.add (d);
		
        addAll(dataSources);
		return dataSources;
	}

	/** {@inheritDoc} */
	public DataSource getDataSourceByService(InterfaceService service) {
        DataSource a = new DataSource();
        
        if ("ICMP".equals(service.getServiceName())) {
            a.setId (service.getId() + "-service-" + "icmp");
            a.setName ("ICMP");
            a.setSource ("/tmp/rrd/icmp.rrd");
            a.setDataSource ("icmp");
        } else if ("HTTP".equals(service.getServiceName())) {
            a.setId (service.getId() + "-service-" + "http");
            a.setName ("HTTP");
            a.setSource ("/tmp/rrd/http.rrd");
            a.setDataSource ("http");
        } else if ("DNS".equals(service.getServiceName())) {
            a.setId (service.getId() + "-service-" + "dns");
            a.setName ("DNS");
            a.setSource ("/tmp/rrd/dns.rrd");
            a.setDataSource ("dns");
        } else if ("SSH".equals(service.getServiceName())) {
            a.setId (service.getId() + "-service-" + "ssh");
            a.setName ("SSH");
            a.setSource ("/tmp/rrd/ssh.rrd");
            a.setDataSource ("ssh");
        } else {
            return null;
        }
		
        add(a);
		return a;
	}

	/** {@inheritDoc} */
	public List<DataSource> getDataSourcesByNode(Node node) {
		List<DataSource> dataSources = new LinkedList<DataSource>();
		DataSource a = new DataSource();
		a.setId ("node-" + node.getNodeId() + "-performance-" + "rsUserProcessTime");
		a.setName ("CPU User Process Time");
		a.setSource ("/tmp/rrd/rsUserProcessTime.rrd");
		a.setDataSource ("rsUserProcessTime");
		dataSources.add (a);
		
		DataSource b = new DataSource();
		b.setId ("node-" + node.getNodeId() + "-performance-" + "rsIdleModeTime");
		b.setName ("CPU Idle Time");
		b.setSource ("/tmp/rrd/rsIdleModeTime.rrd");
		b.setDataSource ("rsIdleModeTime");
		dataSources.add (b);
		
		DataSource c = new DataSource();
		c.setId ("node-" + node.getNodeId() + "-performance-" + "rsDiskXfer1");
		c.setName ("Disk 1 Transfers");
		c.setSource ("/tmp/rrd/rsDiskXfer1.rrd");
		c.setDataSource ("rsDiskXfer1");
		dataSources.add (c);
	
        addAll(dataSources);
		return dataSources;
	}

	/** {@inheritDoc} */
	public DataSource getDataSourceById(String id) {
        return (DataSource) m_dataSources.get(id);
	}
    
    private void add(DataSource ds) {
        m_dataSources.put(ds.getId(), ds);
    }
    
    private void addAll(Collection c) {
        for (Iterator i = c.iterator(); i.hasNext(); ) {
            add((DataSource) i.next());
        }
    }

}
