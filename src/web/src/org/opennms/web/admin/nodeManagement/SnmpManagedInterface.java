
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
