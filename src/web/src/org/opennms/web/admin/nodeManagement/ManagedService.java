
package org.opennms.web.admin.nodeManagement;

/**
 * A servlet that stores service information
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class ManagedService
{
	/**
         *
         */
        protected String name;
        
        /**
         *
         */
        protected String status;
        
	/**
	 *
	 */
	protected int serviceId;
	
	/**
	*/
	public ManagedService()
	{
	}
	
	/**
	 *
	 */
	public void setId(int id)
	{
		serviceId = id;
	}
	
	/**
	*/
	public int getId()
	{
		return serviceId;
	}
	
        /**
         *
         */
        public void setName(String newName)
        {
                name = newName;
        }
        
	/**
	*/
	public String getName()
	{
		return name;
	}
	
	/**
         *
         */
        public void setStatus(String newStatus)
        {
                if (newStatus.equals("A"))
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
