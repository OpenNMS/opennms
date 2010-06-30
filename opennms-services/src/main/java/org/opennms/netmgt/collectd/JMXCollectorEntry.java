//
//This file is part of the OpenNMS(R) Application.
//
//OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.                                                            
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
//For more information contact: 
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/
//
package org.opennms.netmgt.collectd;

import java.util.Set;
import java.util.TreeMap;
/*
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */


/**
 * <p>JMXCollectorEntry class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JMXCollectorEntry extends TreeMap {
	
	private String objectName;
    
    /**
     * <P>
     * Creates a default instance of the JMXCollector entry map. The map
     * represents a singular instance from the MibObject. Each column in the
     * table for the loaded instance may be retrieved through its OID from the
     * MIBObject.
     * </P>
     *
     * <P>
     * The initial table is constructed with zero elements in the map.
     * </P>
     */
    public JMXCollectorEntry() {
        super();
    }

    /**
     * <P>
     * The class constructor used to initialize the object to its initial state.
     * Although the object's member variables can change after an instance is
     * created, this constructor will initialize all the variables as per their
     * named variable from the passed array of JMX varbinds.
     * </P>
     *
     * <P>
     * If the information in the object should not be modified then a <EM>final
     * </EM> modifier can be applied to the created object.
     * </P>
     *
     * @param vars
     *            The array of collected JMX variable bindings
     * @param objectName a {@link java.lang.String} object.
     * @param types an array of {@link java.lang.String} objects.
     */
    public JMXCollectorEntry(String objectName, String[] vars, String[] types) {
        this();
        
        this.objectName = objectName;
        
        for (int i = 0; i < vars.length;i++ ) {
            put(vars[i], types[i]);
        }
    }

    /* (non-Javadoc)
     * @see java.util.TreeMap#keySet()
     */
    /**
     * <p>attributeNames</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set attributeNames() {
        return super.keySet();
    }

	/**
	 * <p>Getter for the field <code>objectName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getObjectName() {
		return objectName;
	}

	/**
	 * <p>Setter for the field <code>objectName</code>.</p>
	 *
	 * @param objectName a {@link java.lang.String} object.
	 */
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
}
