
package org.opennms.web.admin.nodeManagement;

import java.util.*;

/**
 * A servlet that stores interface information
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class ManagedInterface
{
	/**
         *
         */
        protected String address;
        
        /**
         *
         */
        protected List services;
        
        /**
         *
         */
        protected String status;
        
	/**
         *
         */
        protected int nodeid;
        
	/**
	*/
	public ManagedInterface()
	{
		services = new ArrayList();
	}
	
	/**
	*/
	public void addService(ManagedService newService)
	{
		services.add(newService);
	}
	
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
	*/
	public List getServices()
	{
		return services;
	}
        
        /**
         *
         */
        public int getServiceCount()
        {
                return services.size();
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
        public void setStatus(String newStatus)
        {
                if (newStatus.equals("M"))
		{
			status = "managed";
		}
                else if (newStatus.equals("A"))
                {
                        status = "managed";
                }
		else
		{
			status = "unmanaged";
		}
        }
        
        /**
         *
         */
        public String getStatus()
        {
                return status;
        }
}
