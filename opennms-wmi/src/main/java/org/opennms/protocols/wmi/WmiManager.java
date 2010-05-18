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

import java.net.UnknownHostException;
import java.util.ArrayList;

import org.jinterop.dcom.common.JIException;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet;
import org.opennms.protocols.wmi.wbem.OnmsWbemObject;
import org.opennms.protocols.wmi.wbem.OnmsWbemProperty;
import org.opennms.protocols.wmi.wbem.OnmsWbemPropertySet;
import org.opennms.core.utils.ThreadCategory;
import org.apache.log4j.Category;

/**
 * <P>
 * This provides an easy abtraction of the WmiClient functionality. It allows
 * programmers to make simple WMI Class + WMI Object queries and then measure
 * the resulting values against parameters.
 *
 * The purpose of the WMI Manager is to provide poller-style functionality where
 * low-level access to WBEM objects and methods is not essential.
 * </P>
 *
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski </A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS </A>
 */
public class WmiManager {
	/**
	 * The default socket timeout.
	 */
	public static int DEFAULT_SOCKET_TIMEOUT = 5000;

	/**
	 * Stores the host name the manager is connect(ing/ed) to.
	 */
	private String m_HostName = null;

	/**
	 * The domain to use when requesting a check.
	 */
	private String m_Domain = null;

	/**
	 * The username to use when requesting a check.
	 */
	private String m_Username = null;

	/**
	 * The password to use when requesting a check.
	 */
	private String m_Password = null;

	private IWmiClient m_WmiClient = null;

	private String m_MatchType = null;

	private int m_Timeout = DEFAULT_SOCKET_TIMEOUT;

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
	 * This method is used for setting the password used to perform service
	 * checks.
	 * 
	 * @param pass
	 *            the password to use when performing service checks.
	 */
	public void setPassword(String pass) {
		m_Password = pass;
	}

	/**
	 * This method is used to set the host name to connect to for performing
	 * remote service checks. This method must be called before calling the
	 * init() method or it will have no effect.
	 * 
	 * @param host
	 *            the host name to connect to.
	 * @see init
	 */
	public void setHostName(String host) {
		m_HostName = host;
	}

	/**
	 * Returns the host name being used to connect to the remote service.
	 * 
	 * @return the host name being used to connect to the remote service.
	 */
	public String getHostName() {
		return m_HostName;
	}

	/**
	 * This method is used to set the TCP socket timeout to be used when
	 * connecting to the remote service. This must be called before calling
	 * <code>init</code> or it will have no effect.
	 * 
	 * @param timeout
	 *            the TCP socket timeout.
	 */
	public void setTimeout(int timeout) {
		m_Timeout = timeout;
	}

	/**
	 * Returns the TCP socket timeout used when connecting to the remote
	 * service.
	 * 
	 * @return the tcp socket timeout.
	 */
	public int getTimeout() {
		return m_Timeout;
	}

	/**
	 * Constructor.
	 * 
	 * @param host
	 *            sets the host name to connect to. 
	 * @param user
	 *            sets the username to connect with
	 * @param pass
	 *            sets the password to connect with.
	 */
	public WmiManager(String host, String user, String pass) {
		m_HostName = host;
		m_Username = user;
		m_Password = pass;
		m_Domain = host;
        m_MatchType = "all";
    }

	/**
	 * Constructor.
	 * 
	 * @param host
	 *            sets the host name to connect to. 
	 * @param user
	 *            sets the username to connect with
	 * @param pass
	 *            sets the password to connect with.
	 * @param domain
	 *            sets the domain to connect to.
	 */
	public WmiManager(String host, String user, String pass, String domain) {
		m_HostName = host;
		m_Username = user;
		m_Password = pass;
		m_MatchType = "all";
		if (domain.equals("")) {
			m_Domain = host;
		} else {
			m_Domain = domain;
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param host
	 *            sets the host name to connect to. 
	 * @param user
	 *            sets the username to connect with
	 * @param pass
	 *            sets the password to connect with.
	 * @param domain
	 *            sets the domain to connect to.
	 * @param matchType
	 *            the type of matching to be used for multiple results: all, none, some, one.
	 */
	public WmiManager(String host, String user, String pass, String domain,
			String matchType) {
		m_HostName = host;
		m_Username = user;
		m_Password = pass;
		if (isValidMatchType(matchType)) {
			m_MatchType = matchType;
		} else {
			m_MatchType = "all";
		}
		if (domain == null || domain.equals("")) {
			m_Domain = host;
		} else {
			m_Domain = domain;
		}
	}

	/**
	 * Constructor. Made private to prevent construction without parameters.
	 */
	private WmiManager() {
		// nothing to do, don't allow it.
	}

	public static boolean isValidMatchType(String matchType) {
		if (matchType.equals("all") || matchType.equals("none")
				|| matchType.equals("some") || matchType.equals("one")) {
			return true;
		}

		return false;
	}
	
	public static boolean isValidOpType(String opType) {
		try {
			WmiMgrOperation op = WmiMgrOperation.valueOf(opType);
			return true;
		} catch(IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * This creates a new WmiClient and creates a connection to the host.
	 * 
	 * @throws WmiException
	 *             An exception will be thrown if the system is unable to look
	 *             up the host and if J-Interop throws an exception this will
	 *             re-throw that exception so that implementors need not know
	 *             J-Interop exceptions.
	 */
	public void init() throws WmiException {
		m_WmiClient = (IWmiClient)new WmiClient(m_HostName);
		m_WmiClient.connect(m_Domain, m_Username, m_Password);
	}

	/**
	 * This is for tests to harness and create a mock client. Do not use!
	 *
     * @param client allows a IWmiClient to be pre-instantiated. Used for mock testing.
	 * @throws WmiException is thrown if there are any problems connecting.
	 */
	public void init(IWmiClient client) throws WmiException {
		m_WmiClient = client;
		m_WmiClient.connect(m_Domain, m_Username, m_Password);
	}
	
	public void close() throws WmiException {
		if(m_WmiClient == null)
		{
			throw new WmiException("Failed to close client: WmiClient was not initialized.");
		}
		m_WmiClient.disconnect();
	}

	public WmiResult performOp(WmiParams params) throws WmiException {
        // If we defined a WQL query string, exec the query.
        if( params.getWmiOperation().equals(WmiParams.WMI_OPERATION_WQL)) {
            return performExecQuery(params);
        } else {
            // Otherwise perform an InstanceOf.
            return performInstanceOf(params);
        }
    }

    public WmiResult performExecQuery(WmiParams params) throws WmiException {
        ArrayList<Object> wmiObjects = new ArrayList<Object>();

		OnmsWbemObjectSet wos = m_WmiClient.performExecQuery(params.getWql());
        
        for(int i=0; i<wos.count(); i++) {
            wmiObjects.add(wos.get(i).getWmiProperties().getByName(params.getWmiObject()).getWmiValue());
        }

        WmiResult result = new WmiResult(wmiObjects);

		if (params.getCompareOperation().equals("NOOP")) {
			result.setResultCode(WmiResult.RES_STATE_OK);
		} else if (params.getCompareOperation().equals("EQ")
				|| params.getCompareOperation().equals("NEQ")
				|| params.getCompareOperation().equals("GT")
				|| params.getCompareOperation().equals("LT")) {
			performResultCheck(result, params);
		} else {
			result.setResultCode(WmiResult.RES_STATE_UNKNOWN);
		}

		return result;
    }

    public WmiResult performInstanceOf(WmiParams params) throws WmiException {
        ArrayList<Object> wmiObjects = new ArrayList<Object>();

		OnmsWbemObjectSet wos = m_WmiClient.performInstanceOf(params.getWmiClass());

        for(int i=0; i<wos.count(); i++) {
            wmiObjects.add(wos.get(i).getWmiProperties().getByName(params.getWmiObject()).getWmiValue());
        }

        WmiResult result = new WmiResult(wmiObjects);

		if (params.getCompareOperation().equals("NOOP")) {
			result.setResultCode(WmiResult.RES_STATE_OK);
		} else if (params.getCompareOperation().equals("EQ")
				|| params.getCompareOperation().equals("NEQ")
				|| params.getCompareOperation().equals("GT")
				|| params.getCompareOperation().equals("LT")) {
			performResultCheck(result, params);
		} else {
			result.setResultCode(WmiResult.RES_STATE_UNKNOWN);
		}

		return result;
	}

	private void performResultCheck(WmiResult wmiResult, WmiParams params) throws WmiException {
		ArrayList<Object> wmiObjects = wmiResult.getResponse();

		int matches = 0;
		int total = wmiObjects.size();
		for (int i = 0; i < total; i++) {
			Object wmiObj = wmiObjects.get(i);

			WmiMgrOperation op = WmiMgrOperation.valueOf(params.getCompareOperation());
			
			if(op.compareString(wmiObj, (String)params.getCompareValue())) {
				matches++;
			}
		}

		/*
		 * Check that we meet the match requirements:
		 * - all: all objects must match, one or more.
		 * - none: no objects must match
		 * - one: only one object must match
		 * - some: more than one object but not all objects must match.
		 */
        if (m_MatchType.equals("all") && matches == total && matches > 0) {
			wmiResult.setResultCode(WmiResult.RES_STATE_OK);
		} else if (m_MatchType.equals("none") && matches == 0) {
			wmiResult.setResultCode(WmiResult.RES_STATE_OK);
		} else if (m_MatchType.equals("one") && matches == 1) {
			wmiResult.setResultCode(WmiResult.RES_STATE_OK);
		} else if (m_MatchType.equals("some") && matches > 1) {
			// we want to match more than one but not all.
			if (matches != total) {
				wmiResult.setResultCode(WmiResult.RES_STATE_OK);
			} else {
				wmiResult.setResultCode(WmiResult.RES_STATE_CRIT);
			}
		} else {
			wmiResult.setResultCode(WmiResult.RES_STATE_CRIT);
		}
	}

	/**
	 * @return the m_MatchType
	 */
	public String getMatchType() {
		return m_MatchType;
	}

	/**
	 * @param matchType the m_MatchType to set
	 */
	public void setMatchType(String matchType) {
		m_MatchType = matchType;
	}
}
