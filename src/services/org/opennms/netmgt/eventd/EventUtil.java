//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Modifications:
//
// 2002 Nov 14: Added non-blocking I/O classes to speed capsd and pollers.
// 2002 Nov 13: Added two new notification fields: nodelabel and interfaceresolve.
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
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.eventd;

import java.util.*;
import java.text.*;
import java.sql.*;
import java.math.BigInteger;

import org.opennms.protocols.snmp.*;
import org.opennms.core.utils.Base64;

import org.opennms.netmgt.config.DatabaseConnectionFactory;


// castor generated classes
import org.opennms.netmgt.xml.event.*;

/**
 * EventUtil is used primarily for the event parm expansion - has methods
 * used by all the event components to send in the event and the element
 * to expanded and have the 'expanded' value sent back
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy</A>
 * @author <A HREF="mailto:weave@oculan.com">Brain Weaver</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public final class EventUtil
{
	/**
	 * The UEI xml tag
	 */
	static final String	TAG_UEI		="uei";

	/**
	 * The event source xml tag
	 */
	static final String	TAG_SOURCE	="source";

	/**
	 * The event time xml tag
	 */
	static final String	TAG_TIME	="time";

	/**
	 * The event nodeid xml tag
	 */
	static final String	TAG_NODEID	="nodeid";

	/**
	 * The event nodeid xml tag
	 */
	static final String	TAG_NODELABEL	="nodelabel";

	/**
	 * The event host xml tag
	 */
	static final String	TAG_HOST	="host";

	/**
	 * The event interface xml tag
	 */
	static final String	TAG_INTERFACE	="interface";

        /**
         * The reverse DNS lookup of the interface
         */
	static final String TAG_INTERFACE_RESOLVE ="interfaceresolve";
        
	/**
	 * The event snmp id xml tag
	 */
	static final String	TAG_SNMP_ID	="id";

	/**
	 * The SNMP xml tag
	 */
	static final String	TAG_SNMP	="snmp";

	/**
	 * The event snmp idtext xml tag
	 */
	static final String	TAG_SNMP_IDTEXT	="idtext";

	/**
	 * The event snmp version xml tag
	 */
	static final String	TAG_SNMP_VERSION="version";

	/**
	 * The event snmp specific xml tag
	 */
	static final String	TAG_SNMP_SPECIFIC="specific";

	/**
	 * The event snmp generic xml tag
	 */
	static final String	TAG_SNMP_GENERIC="generic";

	/**
	 * The event snmp community xml tag
	 */
	static final String	TAG_SNMP_COMMUNITY="community";

	/**
	 * The event snmp host xml tag
	 */
	static final String	TAG_SNMPHOST	="snmphost";

	/**
	 * The event service xml tag
	 */
	static final String	TAG_SERVICE	="service";

	/**
	 * The event severity xml tag
	 */
	static final String	TAG_SEVERITY	="severity";

	/**
	 * The event operinstruct xml tag
	 */
	static final String	TAG_OPERINSTR	="operinstruct";

	/**
	 * The event mouseovertext xml tag
	 */
	static final String	TAG_MOUSEOVERTEXT="mouseovertext";

	/**
	 * The '%' sign used to indicate parms to be expanded
	 */
	final static char	PERCENT			='%';

	/**
	 * The string that should be expanded to a list of all parm names
	 */
	final static String	PARMS_NAMES		= "parm[names-all]";

	/**
	 * The string that should be expanded to a list of all parm values
	 */
	final static String	PARMS_VALUES		= "parm[values-all]";

	/**
	 * The string that should be expanded to a list of all parms
	 */
	final static String	PARMS_ALL		= "parm[all]";

	/**
	 * The string that starts the expansion for a parm - used to
	 * lookup values of parameters by their names
	 */
	final static String	PARM_BEGIN		= "parm[";

	/**
	 * The length of PARM_BEGIN
	 */
	final static int	PARM_BEGIN_LENGTH	= 5;

	/**
	 * The string that should be expanded to the number of parms
	 */
	final static String	NUM_PARMS_STR		= "parm[##]";

	/**
	 * The string that starts a parm number - used to
	 * lookup values of parameters by their position
	 */
	final static String	PARM_NUM_PREFIX		= "parm[#";

	/**
	 * The length of PARM_NUM_PREFIX
	 */
	final static int	PARM_NUM_PREFIX_LENGTH	= 6;

	/**
	 * The string that ends the expansion of a parm
	 */
	final static String	PARM_END_SUFFIX		= "]";

	/**
	 * For expansion of the '%parms[all]%' - the parm name
	 * and value are added as delimiter separated list of 
	 * '<parmName>=<value>' strings
	 */
	final static char	NAME_VAL_DELIM		= '=';

	/**
	 */
	final static char	SPACE_DELIM		= ' ';
	
	/**
	 * The values and the corresponding attributes of an element are added
	 * delimited by ATTRIB_DELIM
	 */
	final static char	ATTRIB_DELIM		= ',';

	/**
	 * Converts the value of a parm ('Value') of the instance to a string
	 */
	public static String getValueAsString(Value pvalue)
	{
		if (pvalue == null)
			return null;

		String result = "";
		String encoding = pvalue.getEncoding();
		Object value = pvalue.getContent();
		if (encoding.equals("text"))
		{
			if(value instanceof String)
				result = (String)value;
			else if(value instanceof Number)
				result = value.toString();
			else if(value instanceof SnmpInt32)
				result = Integer.toString(((SnmpInt32)value).getValue());
			else if(value instanceof SnmpUInt32)
				result = Long.toString(((SnmpUInt32)value).getValue());
			else if(value instanceof SnmpCounter64)
				result = ((SnmpCounter64)value).getValue().toString();
			else if(value instanceof SnmpIPAddress)
				result = value.toString();
			else if(value instanceof SnmpOctetString)
				result = new String(((SnmpOctetString)value).getString());
			else if(value instanceof SnmpObjectId)
				result = value.toString();
		}
		else if (encoding.equals("base64"))
		{
			if(value instanceof String)
				result = new String(Base64.encodeBase64(((String)value).getBytes()));
			else if(value instanceof Number)
			{
				byte[] ibuf = null;
				if(value instanceof Short)
				{
					ibuf = new byte[2];
					ibuf[0] = (byte) ((((Number)value).shortValue() >> 8) & 0xff);
					ibuf[1] = (byte) (((Number)value).shortValue() & 0xff);
				}
				else if(value instanceof Integer)
				{
					ibuf = new byte[4];
					ibuf[0] = (byte) ((((Number)value).intValue() >> 24) & 0xff);
					ibuf[1] = (byte) ((((Number)value).intValue() >> 16) & 0xff);
					ibuf[2] = (byte) ((((Number)value).intValue() >> 8) & 0xff);
					ibuf[3] = (byte) (((Number)value).intValue() & 0xff);
				}
				else if(value instanceof Long)
				{
					ibuf = new byte[8];
					ibuf[0] = (byte) ((((Number)value).longValue() >> 56) & 0xffL);
					ibuf[1] = (byte) ((((Number)value).longValue() >> 48) & 0xffL);
					ibuf[2] = (byte) ((((Number)value).longValue() >> 40) & 0xffL);
					ibuf[3] = (byte) ((((Number)value).longValue() >> 32) & 0xffL);
					ibuf[4] = (byte) ((((Number)value).longValue() >> 24) & 0xffL);
					ibuf[5] = (byte) ((((Number)value).longValue() >> 16) & 0xffL);
					ibuf[6] = (byte) ((((Number)value).longValue() >> 8)  & 0xffL);
					ibuf[7] = (byte) (((Number)value).longValue() & 0xffL);
				}
				else if(value instanceof BigInteger)
				{
					ibuf = ((BigInteger)value).toByteArray();
				}
				result = new String(Base64.encodeBase64(ibuf));
			}
			else if(value instanceof SnmpInt32)
			{
				byte[] ibuf = new byte[4];
				ibuf[0] = (byte) ((((SnmpInt32)value).getValue() >> 24) & 0xff);
				ibuf[1] = (byte) ((((SnmpInt32)value).getValue() >> 16) & 0xff);
				ibuf[2] = (byte) ((((SnmpInt32)value).getValue() >> 8) & 0xff);
				ibuf[3] = (byte) (((SnmpInt32)value).getValue() & 0xff);
				
				result = new String(Base64.encodeBase64(ibuf));
			}
			else if(value instanceof SnmpUInt32)
			{
				byte[] ibuf = new byte[4];
				ibuf[0] = (byte) ((((SnmpUInt32)value).getValue() >> 24) & 0xffL);
				ibuf[1] = (byte) ((((SnmpUInt32)value).getValue() >> 16) & 0xffL);
				ibuf[2] = (byte) ((((SnmpUInt32)value).getValue() >> 8) & 0xffL);
				ibuf[3] = (byte) (((SnmpUInt32)value).getValue() & 0xffL);
				
				result = new String(Base64.encodeBase64(ibuf));
			}
			else if(value instanceof SnmpCounter64)
			{
				byte[] ibuf = ((SnmpCounter64)value).getValue().toByteArray();
				result = new String(Base64.encodeBase64(ibuf));
			}
			else if(value instanceof SnmpOctetString)
			{
				result = new String(Base64.encodeBase64(((SnmpOctetString)value).getString()));
			}
			else if(value instanceof SnmpObjectId)
			{
				result = new String(Base64.encodeBase64(value.toString().getBytes()));
			}
		}
		
		return (result == null ? null : result.trim());
		
	}

	/**
	 * <P>This method is used to escape required values from strings
	 * that may contain those values. If the passed string contains
	 * the passed value then the character is reformatted into its
	 * <EM>%dd</EM> format.</P>
	 *
	 *
	 * @param inStr		string that might contain the delimiter
	 * @param delimChar	delimiter to escape
	 *
	 * @return The string with the delimiter escaped as in URLs
	 *
	 * @see #ATTRIB_DELIM
	 */
	public static String escape(String inStr, char delimchar)
	{
		// integer equivalent of the delimiter
		int  delim = delimchar;

		// convert this to a '%<int>' string
		String delimEscStr = "%" + String.valueOf(delim);

		// the buffer to return
		StringBuffer outBuffer = new StringBuffer(inStr);
		
		int index=0;
		int delimIndex = inStr.indexOf(delimchar, index);
		while(delimIndex != -1)
		{
			// delete the delimiter and add the escape string
			outBuffer.deleteCharAt(delimIndex);
			outBuffer.insert(delimIndex, delimEscStr);

			index = delimIndex + delimEscStr.length() + 1;
			delimIndex = outBuffer.toString().indexOf(delimchar, index);
		}
		
		return outBuffer.toString();
	}

	/**
	 * Get the value of the parm for the event
	 *
	 * @param parm	the parm for which value is needed from the event
	 * @param event	the event whose parm value is required
	 *
	 * @return value of the event parm/element
	 */
	public static String getValueOfParm(String parm, Event event)
	{
		String retParmVal = null;

		if (parm.equals(TAG_UEI))
		{
			retParmVal = event.getUei();
		}
		else if (parm.equals(TAG_SOURCE))
		{
			retParmVal = event.getSource();
		}
		else if (parm.equals(TAG_NODEID))
		{
			retParmVal = Long.toString(event.getNodeid());
		}
		else if (parm.equals(TAG_NODELABEL))
		{
			retParmVal = Long.toString(event.getNodeid());
                       	String nodeLabel = null;
			if (event.getNodeid() > 0)
			{
                        	try
                        	{
                                	nodeLabel = getNodeLabel(event.getNodeid());
                        	}
                        	catch (SQLException sqlE)
                        	{
                                 	// do nothing
                        	}
                       	}
			if (nodeLabel != null)
				retParmVal = nodeLabel;
			else
				retParmVal = "Unknown";
		}
		else if (parm.equals(TAG_TIME))
		{
			retParmVal = event.getTime();
		}
		else if (parm.equals(TAG_HOST))
		{
			retParmVal = event.getHost();
		}
		else if (parm.equals(TAG_INTERFACE))
		{
			retParmVal = event.getInterface();
		}
		else if (parm.equals(TAG_INTERFACE_RESOLVE))
		{
			retParmVal = event.getInterface();
			try
			{
				java.net.InetAddress inet = java.net.InetAddress.getByName(retParmVal);
				retParmVal = inet.getHostName();
			} catch (java.net.UnknownHostException e) {}
		}
		else if (parm.equals(TAG_SNMPHOST))
		{
			retParmVal = event.getSnmphost();
		}
		else if (parm.equals(TAG_SERVICE))
		{
			retParmVal = event.getService();
		}
		else if (parm.equals(TAG_SNMP))
		{
			Snmp info = event.getSnmp();
			if (info == null)
				retParmVal = null;
			else
			{
				StringBuffer snmpStr = new StringBuffer(info.getId());
				if (info.getIdtext() != null)
					snmpStr.append(ATTRIB_DELIM + escape(info.getIdtext().trim(), ATTRIB_DELIM));
				else
					snmpStr.append(ATTRIB_DELIM + "undefined");

				snmpStr.append(ATTRIB_DELIM + info.getVersion());

				if (info.hasSpecific())
					snmpStr.append(ATTRIB_DELIM + Integer.toString(info.getSpecific()));
				else
					snmpStr.append(ATTRIB_DELIM + "undefined");

				if (info.hasGeneric())
					snmpStr.append(ATTRIB_DELIM + Integer.toString(info.getGeneric()));
				else
					snmpStr.append(ATTRIB_DELIM + "undefined");

				if (info.getCommunity() != null)
				{
					snmpStr.append(ATTRIB_DELIM + info.getCommunity().trim() );
				}
				else
					snmpStr.append(ATTRIB_DELIM + "undefined");

				retParmVal = snmpStr.toString();
			}
		}
		else if (parm.equals(TAG_SNMP_ID))
		{
			Snmp info = event.getSnmp();
			if (info != null)
			{
				retParmVal = info.getId();
			}
		}
		else if (parm.equals(TAG_SNMP_IDTEXT))
		{
			Snmp info = event.getSnmp();
			if (info != null && info.getIdtext() != null)
			{
				retParmVal = info.getIdtext();
			}
		}
		else if (parm.equals(TAG_SNMP_VERSION))
		{
			Snmp info = event.getSnmp();
			if (info != null)
			{
				retParmVal = info.getVersion();
			}
		}
		else if (parm.equals(TAG_SNMP_SPECIFIC))
		{
			Snmp info = event.getSnmp();
			if (info != null && info.hasSpecific())
			{
				retParmVal = Integer.toString(info.getSpecific());
			}
		}
		else if (parm.equals(TAG_SNMP_GENERIC))
		{
			Snmp info = event.getSnmp();
			if (info != null && info.hasGeneric())
			{
				retParmVal = Integer.toString(info.getGeneric());
			}
		}
		else if (parm.equals(TAG_SNMP_COMMUNITY))
		{
			Snmp info = event.getSnmp();
			if (info != null && info.getCommunity() != null)
			{
				retParmVal = info.getCommunity();
			}
		}
		else if (parm.equals(TAG_SEVERITY))
		{
			retParmVal = event.getSeverity();
		}
		else if (parm.equals(TAG_OPERINSTR))
		{
			retParmVal = event.getOperinstruct();
		}
		else if (parm.equals(TAG_MOUSEOVERTEXT))
		{
			retParmVal = event.getMouseovertext();
		}
		else if (parm.equals(PARMS_VALUES))
		{
			if (event.getParms() != null && event.getParms().getParmCount() <= 0)
				retParmVal = null;

			else
			{
				StringBuffer ret = new StringBuffer();

				Parms parms = event.getParms();
				Enumeration enum = parms.enumerateParm();
				while(enum.hasMoreElements())
				{
					Parm evParm = (Parm)enum.nextElement();
					Value parmValue = evParm.getValue();
					if (parmValue == null)
						continue;

					String parmValueStr = getValueAsString(parmValue);
					if (parmValueStr == null)
						continue;

					if (ret.length() == 0)
					{
						ret.append(parmValueStr);
					}
					else
					{
						ret.append(SPACE_DELIM + parmValueStr);
					}
				}

				retParmVal = ret.toString();
			}
		}
		else if (parm.equals(PARMS_NAMES))
		{
			if (event.getParms() != null && event.getParms().getParmCount() <= 0)
				retParmVal = null;

			else
			{
				StringBuffer ret = new StringBuffer();

				Parms parms = event.getParms();
				if (parm != null)
				{
					Enumeration enum = parms.enumerateParm();
					while(enum.hasMoreElements())
					{
						Parm evParm = (Parm)enum.nextElement();
						String parmName = evParm.getParmName();
						if (parmName == null)
							continue;

						if (ret.length() == 0)
						{
							ret.append(parmName.trim());
						}
						else
						{
							ret.append(SPACE_DELIM + parmName.trim());
						}
					}
				}

				retParmVal = ret.toString();
			}
		}
		else if (parm.equals(PARMS_ALL))
		{
			if (event.getParms() != null && event.getParms().getParmCount() <= 0)
				retParmVal = null;

			else
			{
				StringBuffer ret = new StringBuffer();

				Parms parms = event.getParms();
				if (parms != null)
				{
					Enumeration enum = parms.enumerateParm();
					while(enum.hasMoreElements())
					{
						Parm evParm = (Parm)enum.nextElement();
						String parmName = evParm.getParmName();
						if (parmName == null)
							continue;

						Value parmValue = evParm.getValue();
						if (parmValue == null)
							continue;

						String parmValueStr = getValueAsString(parmValue);
						if (ret.length() != 0)
						{
							ret.append(SPACE_DELIM);
						}

						ret.append(parmName.trim() + NAME_VAL_DELIM + "\"" + parmValueStr + "\"");
					}
				}

				retParmVal = ret.toString();
			}
		}
		else if (parm.equals(NUM_PARMS_STR))
		{
			if (event.getParms() != null)
			{
				int count = event.getParms().getParmCount();
				retParmVal = String.valueOf(count);
			}
		}
		else if (parm.startsWith(PARM_NUM_PREFIX))
		{
			Parms eventParms = event.getParms();
			int end = parm.lastIndexOf(PARM_END_SUFFIX);
			if (end != -1 && eventParms != null)
			{
				// Get the value between the '#' and ']'
				String eparmname = parm.substring(PARM_NUM_PREFIX_LENGTH, end);
				int parmNum = -1;
				try
				{
					parmNum = Integer.parseInt(eparmname);
				}
				catch(NumberFormatException nfe)
				{
					parmNum = -1;
					retParmVal = null;
				}

				if (parmNum > 0 && parmNum <= eventParms.getParmCount())
				{
					Parm evParm = eventParms.getParm(parmNum-1);

					// get parm value
					Value eparmval = evParm.getValue();
					if (eparmval != null)
					{
						retParmVal = getValueAsString(eparmval);
					}
				}
				else
				{
					retParmVal = null;
				}
			}
		}
		else if (parm.startsWith(PARM_BEGIN))
		{
			if (parm.length() > PARM_BEGIN_LENGTH)
			{
				int end = parm.indexOf(PARM_END_SUFFIX, PARM_BEGIN_LENGTH);
				if (end != -1)
				{
					// Get the value between the '[' and ']'
					String eparmname = parm.substring(PARM_BEGIN_LENGTH, end);

					Parms parms = event.getParms();
					if(parms != null)
					{
						Enumeration enum = parms.enumerateParm();
						while(enum.hasMoreElements())
						{
							Parm evParm = (Parm)enum.nextElement();
							String parmName = evParm.getParmName();
							if (parmName != null && parmName.trim().equals(eparmname))
							{
								// get parm value
								Value eparmval = evParm.getValue();
								if (eparmval != null)
								{
									retParmVal = getValueAsString(eparmval);
									break;
								}
							}
						}
					}
				}
			}
		}

		return (retParmVal == null ? null : retParmVal.trim());
	}

	/**
	 * Expand the value if it has parms in one of the following formats
	 *
	 * - %element% values are expanded to have the value of the element where
	 *   'element' is an element in the event DTD
	 * - %parm[values-all]% is expanded to a delimited list of all parmblock values
	 * - %parm[names-all]% is expanded to a list of all parm names
	 * - %parm[all]% is expanded to a full dump of all parmblocks
	 * - %parm[name]% is expanded to the value of the parameter named 'name'
	 * - %parm[<name>]% is replaced by the value of the parameter named 'name', if present
	 * - %parm[#<num>]% is replaced by the value of the parameter number 'num', if present
	 * - %parm[##]% is replaced by the number of parameters 
	 *
	 * @param inp	the input string in which parm values are to be expanded
	 *
	 * @return expanded value if the value had any parameter to expand, null otherwise
	 */
	public static String expandParms(String inp, Event event)
	{
		int index1=-1;
		int index2=-1;

		StringBuffer ret = new StringBuffer();

		String  tempInp = inp;
		int 	inpLen	= inp.length();

		// check input string to see if it has any %xxx% substring
		while((tempInp != null) && ((index1 = tempInp.indexOf(PERCENT)) != -1))
		{
			// copy till first %
			ret.append(tempInp.substring(0, index1));

			index2 = tempInp.indexOf(PERCENT, index1+1);
			if (index2 != -1)
			{
				// Get the value between the %s
				String parm = tempInp.substring(index1+1, index2);
				//m_logger.debug("parm: " + parm + " found in value");

				String parmVal = getValueOfParm(parm, event);
				//m_logger.debug("value of parm: " + parmVal);

				if (parmVal != null)
				{
					ret.append(parmVal);
				}
				/**
				else
				{
					ret.append(tempInp.substring(index1, index2+1));
				}
				**/

				if (index2 < (inpLen-1) )
				{
					tempInp = tempInp.substring(index2 + 1);
				}
				else
				{
					tempInp = null;
				}
			}

			else
			{
				break;
			}
		}

		if ((index1 == -1 || index2 == -1) && (tempInp != null))
		{
			ret.append(tempInp);
		}
		
		String retStr = ret.toString();
		if (ret.length() > 0 && !retStr.equals(inp))
			return retStr;

		return null;
	}

        /**
         * Retrieve nodeLabel from the node table of the database
         * given a particular nodeId.
         *
         * @param nodeId        Node identifier
         *
         * @return nodeLabel    Retreived nodeLabel
         *
         * @throws SQLException if database error encountered
         */
        private static String getNodeLabel(long nodeId)
                throws SQLException
        {

                String nodeLabel = null;
                java.sql.Connection dbConn = null;
                Statement stmt = null;
                try
                {
                        // Get datbase connection from the factory
                        dbConn = DatabaseConnectionFactory.getInstance().getConnection();

                        // Issue query and extract nodeLabel from result set
                        stmt = dbConn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT nodelabel FROM node WHERE nodeid=" + String.valueOf(nodeId));
                        if (rs.next())
                        {
                                nodeLabel = (String)rs.getString("nodelabel");
                        }
                }
                finally
                {
                        // Close the statement
                        if (stmt != null)
                        {
                                try
                                {
                                        stmt.close();
                                }
                                catch (Exception e)
                                {
					// do nothing
                                }
                        }

                        // Close the database connection
                        if(dbConn != null)
                        {
                                try
                                {
                                        dbConn.close();
                                }
                                catch(Throwable t)
                                {
					// do nothing
                                }
                        }
                }

                return nodeLabel;
        }

}
