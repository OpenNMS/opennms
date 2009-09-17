/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 6, 2007
 *
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.map.db;


/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class LinkInfo {
	int nodeid;
	int ifindex;
	int nodeparentid;
	int parentifindex;
	int snmpiftype;
	long snmpifspeed;
	int snmpifoperstatus;
    int snmpifadminstatus;


	
	LinkInfo(int nodeid, int ifindex, int nodeparentid, int parentifindex, int snmpiftype, long snmpifspeed, int snmpifoperstatus, int snmpifadminstatus) {
		super();
		this.nodeid = nodeid;
		this.ifindex = ifindex;
		this.nodeparentid = nodeparentid;
		this.parentifindex = parentifindex;
		this.snmpiftype = snmpiftype;
		this.snmpifspeed = snmpifspeed;
		this.snmpifoperstatus = snmpifoperstatus;
        this.snmpifadminstatus = snmpifadminstatus;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof LinkInfo ) {
			LinkInfo ol = (LinkInfo) obj;
			return 
			(ol.nodeid == this.nodeid 
					&& ol.ifindex == this.ifindex 
					&& ol.nodeparentid== this.nodeparentid 
					&& ol.parentifindex == this.parentifindex
					&& ol.snmpiftype == this.snmpiftype
					&& ol.snmpifspeed == this.snmpifspeed
					&& ol.snmpifoperstatus==this.snmpifoperstatus
					&& ol.snmpifadminstatus==this.snmpifadminstatus);
			
		} 
		return false;
	}
	
	public int hashCode() {
		return (3*nodeid)+(5*ifindex)+(7*nodeparentid)+(11*parentifindex)+(13*snmpiftype)+(17*snmpifoperstatus)+(19*snmpifadminstatus);
	}

}
