
package org.opennms.web.admin.nodeManagement;

import java.util.*;

/**
 * A servlet that stores node, interface, service information
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class ManagedNode
{
	/**
	*/
	protected int nodeID;
	
	/**
	*/
	protected String nodeLabel;
	
        /**
         *
         */
        protected List interfaces;
        
	/**
	*/
	public ManagedNode()
	{
		interfaces = new ArrayList();
	}
	
	/**
	*/
	public void setNodeID(int id)
	{
		nodeID = id;
	}
	
	/**
	*/
	public void setNodeLabel(String label)
	{
		nodeLabel = label;
	}
	
	/**
	*/
	public void addInterface(ManagedInterface newInterface)
	{
		interfaces.add(newInterface);
	}
        
        /**
         *
         */
        public int getInterfaceCount()
        {
                return interfaces.size();
        }
	
	/**
	*/
	public int getNodeID()
	{
		return nodeID;
	}
	
	/**
	*/
	public String getNodeLabel()
	{
		return nodeLabel;
	}
	
	/**
	*/
	public List getInterfaces()
	{
		return interfaces;
	}
}
