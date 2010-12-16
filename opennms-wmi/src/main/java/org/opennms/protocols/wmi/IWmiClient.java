//
//This file is part of the OpenNMS(R) Application.
//
//OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
package org.opennms.protocols.wmi;

import org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet;

/**
 * <p>IWmiClient interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface IWmiClient {

    /**
     * <p>performExecQuery</p>
     *
     * @param strQuery a {@link java.lang.String} object.
     * @return a {@link org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public OnmsWbemObjectSet performExecQuery(String strQuery) throws WmiException;

    /**
     * <p>performExecQuery</p>
     *
     * @param strQuery a {@link java.lang.String} object.
     * @param strQueryLanguage a {@link java.lang.String} object.
     * @param flags a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public OnmsWbemObjectSet performExecQuery(String strQuery,String strQueryLanguage,Integer flags) throws WmiException;

    /**
     * <p>performInstanceOf</p>
     *
     * @param wmiClass a {@link java.lang.String} object.
     * @return a {@link org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public OnmsWbemObjectSet performInstanceOf(String wmiClass) throws WmiException;

    /**
     * <p>connect</p>
     *
     * @param domain a {@link java.lang.String} object.
     * @param username a {@link java.lang.String} object.
     * @param password a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public void connect(String domain, String username, String password) throws WmiException;
	
	/**
	 * <p>disconnect</p>
	 *
	 * @throws org.opennms.protocols.wmi.WmiException if any.
	 */
	public void disconnect() throws WmiException;
	
}

