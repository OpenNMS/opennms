/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.wmi;

/**
 * This class contains the parameters used to perform and validate checks
 * against WMI agents.
 *
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski</A>
 * @version $Id: $
 */
public class WmiParams {
    /** Constant <code>WMI_OPERATION_INSTANCEOF="InstanceOf"</code> */
    public static final String WMI_OPERATION_INSTANCEOF = "InstanceOf";
    /** Constant <code>WMI_OPERATION_WQL="Wql"</code> */
    public static final String WMI_OPERATION_WQL = "Wql";
    /** Constant <code>WMI_DEFAULT_NAMESPACE="root/cimv2"</code> */
    public static final String WMI_DEFAULT_NAMESPACE = "root/cimv2";

    /**
	 * Contains the value to perform a comparison against.
	 */
	private Object m_CompareValue = null;

	private String m_CompareOperation = null;

	private String m_WmiClass = null;

	private String m_WmiObject = null;

    private String m_WmWqlStr = null;

    private String m_WmiOperation = null;

	/**
	 * Constructor, sets the critical threshold.
	 *
	 * @param queryType
	 *            the type of query operation to perform.
	 * @param compVal
	 *            the value to be used for the comparison.
	 * @param compOp
	 *            the operation to be used in the comparison.
	 * @param wmiObj1
	 *            the WMI class to be queried, or WQL to be executed.
	 * @param wmiObj2
	 *            the WMI object within to be queried.
	 */
	public WmiParams(final String queryType, final Object compVal, final String compOp, final String wmiObj1, final String wmiObj2) {
		m_CompareValue = compVal;
		m_CompareOperation = compOp;
        if(queryType.equals(WMI_OPERATION_INSTANCEOF)) {
            m_WmiClass = wmiObj1;
        } else {
            m_WmWqlStr = wmiObj1;
        }
        m_WmiObject = wmiObj2;
        m_WmiOperation = queryType;
    }

    @SuppressWarnings("unused")
    private WmiParams() {
		// do nothing, disallow this default ctor. All params are required.
	}

	/**
	 * <p>getCompareValue</p>
	 *
	 * @return the m_CompareValue
	 */
	public Object getCompareValue() {
		return m_CompareValue;
	}

	/**
	 * <p>setCompareValue</p>
	 *
	 * @param compareValue the m_CompareValue to set
	 */
	public void setCompareValue(final Object compareValue) {
		m_CompareValue = compareValue;
	}

	/**
	 * <p>getCompareOperation</p>
	 *
	 * @return the m_CompareOperation
	 */
	public String getCompareOperation() {
		return m_CompareOperation;
	}

	/**
	 * <p>setCompareOperation</p>
	 *
	 * @param compareOperation the m_CompareOperation to set
	 */
	public void setCompareOperation(final String compareOperation) {
		m_CompareOperation = compareOperation;
	}

	/**
	 * <p>getWmiClass</p>
	 *
	 * @return the m_WmiClass
	 */
	public String getWmiClass() {
		return m_WmiClass;
	}

	/**
	 * <p>setWmiClass</p>
	 *
	 * @param wmiClass the m_WmiClass to set
	 */
	public void setWmiClass(final String wmiClass) {
		m_WmiClass = wmiClass;
	}

	/**
	 * <p>getWmiObject</p>
	 *
	 * @return the m_WmiObject
	 */
	public String getWmiObject() {
		return m_WmiObject;
	}

	/**
	 * <p>setWmiObject</p>
	 *
	 * @param wmiObject the m_WmiObject to set
	 */
	public void setWmiObject(final String wmiObject) {
		m_WmiObject = wmiObject;
	}

    /**
     * <p>getWql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getWql() {
        return m_WmWqlStr;
    }

    /**
     * <p>setWql</p>
     *
     * @param wmiWql a {@link java.lang.String} object.
     */
    public void setWql(final String wmiWql) {
        this.m_WmWqlStr = wmiWql;
    }

    /**
     * <p>getWmiOperation</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getWmiOperation() {
        return m_WmiOperation;
    }

    /**
     * <p>setWmiOperation</p>
     *
     * @param wmiOperation a {@link java.lang.String} object.
     */
    public void setWmiOperation(final String wmiOperation) {
        this.m_WmiOperation = wmiOperation;
    }
}
