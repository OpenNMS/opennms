/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.map.view;

/**
 * <p>VElementInfo class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class VElementInfo implements Cloneable {
    
	private final int id;

    private String uei;
    
    private int severity;
    
    private String label;
    
    private String ipaddr;
    
    
	/**
	 * <p>Getter for the field <code>ipaddr</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getIpaddr() {
        return ipaddr;
    }
    /**
     * <p>Getter for the field <code>label</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel() {
		return label;
	}
	/**
	 * <p>Getter for the field <code>id</code>.</p>
	 *
	 * @return a int.
	 */
	public int getId() {
		return id;
	}
	/**
	 * <p>Getter for the field <code>severity</code>.</p>
	 *
	 * @return a int.
	 */
	public int getSeverity() {
		return severity;
	}
	/**
	 * <p>Getter for the field <code>uei</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getUei() {
		return uei;
	}

    
    
	/**
	 * <p>Constructor for VElementInfo.</p>
	 *
	 * @param id a int.
	 * @param label a {@link java.lang.String} object.
	 * @param ipaddr a {@link java.lang.String} object.
	 */
	public VElementInfo(int id, String ipaddr, String label) {
		super();
		this.id = id;
		this.ipaddr = ipaddr;
		this.label=label;
	}
	
	/**
	 * <p>Constructor for VElementInfo.</p>
	 *
	 * @param id a int.
	 * @param uei a {@link java.lang.String} object.
	 * @param severity a int.
	 */
	public VElementInfo(int id, String uei, int severity) {
		super();
		this.id = id;
		this.uei = uei;
		this.severity = severity;
	}
}
