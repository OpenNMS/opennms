//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.opennms.com/
//
package org.opennms.protocols.wmi;

/**
 * This class contains the parameters used to perform and validate checks
 * against WMI agents.
 * 
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski</A>
 */
public class WmiParams {
	/**
	 * Contains the value to perform a comparison against.
	 */
	private Object m_CompareValue = null;

	private String m_CompareOperation = null;

	private String m_WmiClass = null;

	private String m_WmiObject = null;

    private String m_WmWqlStr = null;

    /**
	 * Constructor, sets the critical threshold.
	 * 
	 * @param compVal
	 *            the value to be used for the comparison.
	 * @param compOp
	 *            the operation to be used in the comparison.
	 * @param wmiClass
	 *            the WMI class to be queried.
	 * @param wmiObject
	 *            the WMI object within wmiClass to be queried.
	 */
	public WmiParams(Object compVal, String compOp, String wmiClass,
			String wmiObject) {
		m_CompareValue = compVal;
		m_CompareOperation = compOp;
		m_WmiClass = wmiClass;
		m_WmiObject = wmiObject;
	}

    /**
	 * Constructor, sets the critical threshold.
	 *
	 * @param compVal
	 *            the value to be used for the comparison.
	 * @param compOp
	 *            the operation to be used in the comparison.
	 * @param wmiWql
	 *            the WMI WQL Query to execute..
	 */
	public WmiParams(Object compVal, String compOp, String wmiWql) {
		m_CompareValue = compVal;
		m_CompareOperation = compOp;
		m_WmWqlStr = wmiWql;
	}

    private WmiParams() {
		// do nothing, disallow this default ctor. All params are required.
	}

	/**
	 * @return the m_CompareValue
	 */
	public Object getCompareValue() {
		return m_CompareValue;
	}

	/**
	 * @param compareValue the m_CompareValue to set
	 */
	public void setCompareValue(Object compareValue) {
		m_CompareValue = compareValue;
	}

	/**
	 * @return the m_CompareOperation
	 */
	public String getCompareOperation() {
		return m_CompareOperation;
	}

	/**
	 * @param compareOperation the m_CompareOperation to set
	 */
	public void setCompareOperation(String compareOperation) {
		m_CompareOperation = compareOperation;
	}

	/**
	 * @return the m_WmiClass
	 */
	public String getWmiClass() {
		return m_WmiClass;
	}

	/**
	 * @param wmiClass the m_WmiClass to set
	 */
	public void setWmiClass(String wmiClass) {
		m_WmiClass = wmiClass;
	}

	/**
	 * @return the m_WmiObject
	 */
	public String getWmiObject() {
		return m_WmiObject;
	}

	/**
	 * @param wmiObject the m_WmiObject to set
	 */
	public void setWmiObject(String wmiObject) {
		m_WmiObject = wmiObject;
	}

    public String getWql() {
        return m_WmWqlStr;
    }

    public void setWql(String wmiWql) {
        this.m_WmWqlStr = wmiWql;
    }
}
