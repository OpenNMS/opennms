
package org.opennms.web.admin.nodeManagement;

import java.util.*;

/**
 * A servlet that stores node, interface, service information for setting 
 * up SNMP data collection
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class SnmpManagedNode
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
	public SnmpManagedNode()
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
	public void addInterface(SnmpManagedInterface newInterface)
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
