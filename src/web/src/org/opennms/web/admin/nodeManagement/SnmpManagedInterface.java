//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//

package org.opennms.web.admin.nodeManagement;

import java.util.*;

/**
 * A servlet that stores interface information used in setting
 * up SNMP Data Collection
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class SnmpManagedInterface
{
	/**
         *
         */
        protected String address;
        
	/**
         *
         */
        protected int nodeid;
        
	/**
         *
         */
        protected int ifIndex;
        
	/**
         *
         */
        protected String iphostname;
        
	/**
         *
         */
        protected String snmpstatus;
        
	/**
         *
         */
        protected String ifDescr;
        
	/**
         *
         */
        protected int ifType;
        
	/**
         *
         */
        protected String ifName;
        
        /**
         *
         */
        public void setAddress(String newAddress)
        {
                address = newAddress;
        }
        
	/**
	*/
	public String getAddress()
	{
		return address;
	}
	
        
        /**
         *
         */
        public void setNodeid(int id)
        {
                nodeid = id;
        }
        
        /**
         *
         */
        public int getNodeid()
        {
                return nodeid;
        }
        
        /**
         *
         */
        public void setIfIndex(int index)
        {
                ifIndex = index;
        }
        
        /**
         *
         */
        public int getIfIndex()
        {
                return ifIndex;
        }
        
        /**
         *
         */
        public void setIpHostname(String newIpHostname)
        {
		iphostname = newIpHostname;
        }
        
        /**
         *
         */
        public String getIpHostname()
        {
                return iphostname;
        }
        
        /**
         *
         */
        public void setStatus(String newStatus)
        {
		snmpstatus = newStatus;
        }
        
        /**
         *
         */
        public String getStatus()
        {
                return snmpstatus;
        }

        /**
         *
         */
        public void setIfDescr(String newIfDescr)
        {
		ifDescr = newIfDescr;
        }
        
        /**
         *
         */
        public String getIfDescr()
        {
                return ifDescr;
        }

        /**
         *
         */
        public void setIfType(int newIfType)
        {
                ifType = newIfType;
        }
        
        /**
         *
         */
        public int getIfType()
        {
                return ifType;
        }
        
        /**
         *
         */
        public void setIfName(String newIfName)
        {
		ifName = newIfName;
        }
        
        /**
         *
         */
        public String getIfName()
        {
                return ifName;
        }
        
}
