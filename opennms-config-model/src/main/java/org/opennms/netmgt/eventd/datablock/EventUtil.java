/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.eventd.datablock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.Base64;
import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.capsd.DbIpInterfaceEntry;
import org.opennms.netmgt.model.capsd.DbSnmpInterfaceEntry;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.event.Tticket;
import org.opennms.netmgt.xml.event.Value;

/**
 * EventUtil is used primarily for the event parm expansion - has methods used
 * by all the event components to send in the event and the element to expanded
 * and have the 'expanded' value sent back
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="mailto:weave@oculan.com">Brain Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public final class EventUtil {
    private static final Logger LOG = LoggerFactory.getLogger(EventUtil.class);
	/**
	 * The Event ID xml
	 */

	static final String TAG_EVENT_DB_ID = "eventid";

	/**
	 * The UEI xml tag
	 */
	public static final String TAG_UEI = "uei";

	/**
	 * The event source xml tag
	 */
	static final String TAG_SOURCE = "source";

	/**
	* The event descr xml tag
	*/
	static final String TAG_DESCR = "descr";

        /**
        * The event logmsg xml tag
        */
        static final String TAG_LOGMSG = "logmsg";

	/**
	 * The event time xml tag
	 */
	static final String TAG_TIME = "time";
	
	/**
	 * The event time xml tag, short format
	 */
	static final String TAG_SHORT_TIME = "shorttime";

	/**

	/**
	 * The event dpname xml tag
	 */
	static final String TAG_DPNAME = "dpname";

	/**
	 * The event nodeid xml tag
	 */
	static final String TAG_NODEID = "nodeid";

	/**
	 * The event nodelabel xml tag
	 */
	static final String TAG_NODELABEL = "nodelabel";

	/**
	 * The event host xml tag
	 */
	static final String TAG_HOST = "host";

	/**
	 * The event interface xml tag
	 */
	static final String TAG_INTERFACE = "interface";

    /**
     * The event ifindex xml tag
     */
    static final String TAG_IFINDEX = "ifindex";

	/**
	 * The reverse DNS lookup of the interface
	 */
	static final String TAG_INTERFACE_RESOLVE = "interfaceresolve";

	/**
	 * The reverse DNS lookup of the interface
	 */
	static final String TAG_IFALIAS = "ifalias";
	
	/**
	 * The event snmp id xml tag
	 */
	static final String TAG_SNMP_ID = "id";

	/**
	 * The SNMP xml tag
	 */
	static final String TAG_SNMP = "snmp";

	/**
	 * The event snmp idtext xml tag
	 */
	static final String TAG_SNMP_IDTEXT = "idtext";

	/**
	 * The event snmp version xml tag
	 */
	static final String TAG_SNMP_VERSION = "version";

	/**
	 * The event snmp specific xml tag
	 */
	static final String TAG_SNMP_SPECIFIC = "specific";

	/**
	 * The event snmp generic xml tag
	 */
	static final String TAG_SNMP_GENERIC = "generic";

	/**
	 * The event snmp community xml tag
	 */
	static final String TAG_SNMP_COMMUNITY = "community";

	/**
	 * The event snmp host xml tag
	 */
	static final String TAG_SNMPHOST = "snmphost";

	/**
	 * The event service xml tag
	 */
	static final String TAG_SERVICE = "service";

	/**
	 * The event severity xml tag
	 */
	public static final String TAG_SEVERITY = "severity";

	/**
	 * The event operinstruct xml tag
	 */
	static final String TAG_OPERINSTR = "operinstruct";

	/**
	 * The event mouseovertext xml tag
	 */
	static final String TAG_MOUSEOVERTEXT = "mouseovertext";

        static final Object TAG_TTICKET_ID = "tticketid";

	/**
	 * The string that starts the expansion for an asset field - used to lookup values
	 * of asset fields by their names
	 */
	final static String ASSET_BEGIN = "asset[";

	/**
	 * The string that ends the expansion of a parm
	 */
	final static String ASSET_END_SUFFIX = "]";

	/**
	 * The '%' sign used to indicate parms to be expanded
	 */
	final static char PERCENT = '%';

	/**
	 * The string that should be expanded to a list of all parm names
	 */
	final static String PARMS_NAMES = "parm[names-all]";

	/**
	 * The string that should be expanded to a list of all parm values
	 */
	final static String PARMS_VALUES = "parm[values-all]";

	/**
	 * The string that should be expanded to a list of all parms
	 */
	final static String PARMS_ALL = "parm[all]";

	/**
	 * The string that starts the expansion for a parm - used to lookup values
	 * of parameters by their names
	 */
	final static String PARM_BEGIN = "parm[";

	/**
	 * The length of PARM_BEGIN
	 */
	final static int PARM_BEGIN_LENGTH = 5;

	/**
	 * The string that should be expanded to the number of parms
	 */
	final static String NUM_PARMS_STR = "parm[##]";

	/**
	 * The string that starts a parm number - used to lookup values of
	 * parameters by their position
	 */
	final static String PARM_NUM_PREFIX = "parm[#";

	/**
	 * The length of PARM_NUM_PREFIX
	 */
	final static int PARM_NUM_PREFIX_LENGTH = 6;
	
	/**
	 * The string that starts a request for the name of a numbered parm
	 */
	final static String PARM_NAME_NUMBERED_PREFIX = "parm[name-#";
	
	/**
	 * The length of PARM_NAME_NUMBERED_PREFIX
	 */
	final static int PARM_NAME_NUMBERED_PREFIX_LENGTH = 11;

	/**
	 * The string that ends the expansion of a parm
	 */
	final static String PARM_END_SUFFIX = "]";

	/**
	 * For expansion of the '%parms[all]%' - the parm name and value are added
	 * as delimiter separated list of ' <parmName>= <value>' strings
	 */
	final static char NAME_VAL_DELIM = '=';

	/**
	 */
	final static char SPACE_DELIM = ' ';

	/**
	 * The values and the corresponding attributes of an element are added
	 * delimited by ATTRIB_DELIM
	 */
	final static char ATTRIB_DELIM = ',';

	/**
	 * Substitute the actual percent sign
	 */
	static final String TAG_PERCENT_SIGN = "pctsign";

	/**
	 * Converts the value of a parm ('Value') of the instance to a string
	 *
	 * @param pvalue a {@link org.opennms.netmgt.xml.event.Value} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getValueAsString(Value pvalue) {
		if (pvalue == null)
			return null;
		
		if (pvalue.getContent() == null)
			return null;

		String result = "";
		String encoding = pvalue.getEncoding();
		if (encoding.equals(EventConstants.XML_ENCODING_TEXT)) {
			result = pvalue.getContent();
		} else if (encoding.equals(EventConstants.XML_ENCODING_BASE64)) {
			byte[] bytes = Base64.decodeBase64(pvalue.getContent().toCharArray());
			result = "0x"+toHexString(bytes);
		} else if (encoding.equals(EventConstants.XML_ENCODING_MAC_ADDRESS)) {
			result = pvalue.getContent();
		} else {
			throw new IllegalStateException("Unknown encoding for parm value: " + encoding);
		}
		
		return result.trim();
	}
	
	public static String toHexString(byte[] data) {
		final StringBuffer b = new StringBuffer();
		for (int i = 0; i < data.length; ++i) {
			final int x = (int) data[i] & 0xff;
			if (x < 16) b.append("0");
			b.append(Integer.toString(x, 16).toLowerCase());
		}
		return b.toString();
	}

	/**
	 * <P>
	 * This method is used to escape required values from strings that may
	 * contain those values. If the passed string contains the passed value then
	 * the character is reformatted into its <EM>%dd</EM> format.
	 * </P>
	 *
	 * @param inStr
	 *            string that might contain the delimiter
	 * @param delimchar
	 *            delimiter to escape
	 * @return The string with the delimiter escaped as in URLs
	 * @see #ATTRIB_DELIM
	 */
	public static String escape(String inStr, char delimchar) {
		// integer equivalent of the delimiter
		int delim = delimchar;

		// convert this to a '%<int>' string
		String delimEscStr = "%" + String.valueOf(delim);

		// the buffer to return
		StringBuffer outBuffer = new StringBuffer(inStr);

		int index = 0;
		int delimIndex = inStr.indexOf(delimchar, index);
		while (delimIndex != -1) {
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
	 * @param parm
	 *            the parm for which value is needed from the event
	 * @param event
	 *            the event whose parm value is required
	 * @return value of the event parm/element
	 */
	public static String getValueOfParm(String parm, Event event) {
        
		String retParmVal = null;
		final String ifString = event.getInterface();
		
		if (parm.equals(TAG_UEI)) {
			retParmVal = event.getUei();
		}
		if (parm.equals(TAG_EVENT_DB_ID)) {
			if (event.hasDbid()) {
				retParmVal = Integer.toString(event.getDbid());
			} else {
				retParmVal = "eventid-unknown";
			}
		} else if (parm.equals(TAG_SOURCE)) {
			retParmVal = event.getSource();
		} else if (parm.equals(TAG_DPNAME)) {
			retParmVal = event.getDistPoller();
		} else if (parm.equals(TAG_DESCR)) {
			retParmVal = event.getDescr();
                } else if (parm.equals(TAG_LOGMSG)) {
                        retParmVal = event.getLogmsg().getContent();
		} else if (parm.equals(TAG_NODEID)) {
			retParmVal = Long.toString(event.getNodeid());
		} else if (parm.equals(TAG_NODELABEL)) {
			retParmVal = Long.toString(event.getNodeid());
			String nodeLabel = null;
			if (event.getNodeid() > 0) {
				try {
					nodeLabel = getNodeLabel(event.getNodeid());
				} catch (SQLException sqlE) {
					// do nothing
				}
			}
			if (nodeLabel != null)
				retParmVal = nodeLabel;
			else
				retParmVal = "Unknown";
		} else if (parm.equals(TAG_TIME)) {
			String eventTime = event.getTime(); //This will be in GMT
			try {
				Date actualDate = org.opennms.netmgt.EventConstants.parseToDate(eventTime);
				DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL,
						DateFormat.FULL);
				retParmVal = df.format(actualDate);
			} catch (java.text.ParseException e) {
				LOG.error("could not parse event date '{}'", eventTime, e);

				//Give up and just use the original string - don't bother with
				// messing around
				retParmVal = eventTime;	
			} 
		} else if (parm.equals(TAG_SHORT_TIME)) {
			String eventTime = event.getTime(); //This will be in GMT
			try {
				Date actualDate = org.opennms.netmgt.EventConstants.parseToDate(eventTime);
				DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
								DateFormat.SHORT);
				retParmVal = df.format(actualDate);
			} catch (java.text.ParseException e) {
				LOG.error("could not parse event date '{}'", eventTime, e);
				
				//Give up and just use the original string - don't bother with
				// messing around
				retParmVal = eventTime;
				}
		} else if (parm.equals(TAG_HOST)) {
			retParmVal = event.getHost();
		} else if (parm.equals(TAG_INTERFACE)) {
			retParmVal = ifString;
		} else if (parm.equals(TAG_IFINDEX)) {
	          if (event.hasIfIndex()) {
	              retParmVal = Integer.toString(event.getIfIndex());
	            } else {
	                retParmVal = "N/A";
	            }
		} else if (parm.equals(TAG_INTERFACE_RESOLVE)) {
			InetAddress addr = event.getInterfaceAddress();
			if (addr != null) retParmVal = addr.getHostName();
		} else if (parm.equals(TAG_IFALIAS)) {
			String ifAlias = null;
			if (event.getNodeid() > 0
					&& event.getInterface() != null) {
				try {
					ifAlias = getIfAlias(event.getNodeid(), ifString);
				} catch (SQLException sqlE) {
					// do nothing
					LOG.info("ifAlias Unavailable for {}:{}", event.getNodeid(), event.getInterface(), sqlE);
				}
			}
			if (ifAlias != null)
				retParmVal = ifAlias;
			else
				retParmVal = ifString;
		} else if (parm.equals(TAG_PERCENT_SIGN)) {
			String pctSign = "%";
			retParmVal = pctSign;
		} else if (parm.equals(TAG_SNMPHOST)) {
			retParmVal = event.getSnmphost();
		} else if (parm.equals(TAG_SERVICE)) {
			retParmVal = event.getService();
		} else if (parm.equals(TAG_SNMP)) {
			Snmp info = event.getSnmp();
			if (info == null)
				retParmVal = null;
			else {
				StringBuffer snmpStr = new StringBuffer(info.getId());
				if (info.getIdtext() != null)
					snmpStr.append(ATTRIB_DELIM
							+ escape(info.getIdtext().trim(), ATTRIB_DELIM));
				else
					snmpStr.append(ATTRIB_DELIM + "undefined");

				snmpStr.append(ATTRIB_DELIM + info.getVersion());

				if (info.hasSpecific())
					snmpStr.append(ATTRIB_DELIM
							+ Integer.toString(info.getSpecific()));
				else
					snmpStr.append(ATTRIB_DELIM + "undefined");

				if (info.hasGeneric())
					snmpStr.append(ATTRIB_DELIM
							+ Integer.toString(info.getGeneric()));
				else
					snmpStr.append(ATTRIB_DELIM + "undefined");

				if (info.getCommunity() != null) {
					snmpStr.append(ATTRIB_DELIM + info.getCommunity().trim());
				} else
					snmpStr.append(ATTRIB_DELIM + "undefined");

				retParmVal = snmpStr.toString();
			}
		} else if (parm.equals(TAG_SNMP_ID)) {
			Snmp info = event.getSnmp();
			if (info != null) {
				retParmVal = info.getId();
			}
		} else if (parm.equals(TAG_SNMP_IDTEXT)) {
			Snmp info = event.getSnmp();
			if (info != null && info.getIdtext() != null) {
				retParmVal = info.getIdtext();
			}
		} else if (parm.equals(TAG_SNMP_VERSION)) {
			Snmp info = event.getSnmp();
			if (info != null) {
				retParmVal = info.getVersion();
			}
		} else if (parm.equals(TAG_SNMP_SPECIFIC)) {
			Snmp info = event.getSnmp();
			if (info != null && info.hasSpecific()) {
				retParmVal = Integer.toString(info.getSpecific());
			}
		} else if (parm.equals(TAG_SNMP_GENERIC)) {
			Snmp info = event.getSnmp();
			if (info != null && info.hasGeneric()) {
				retParmVal = Integer.toString(info.getGeneric());
			}
		} else if (parm.equals(TAG_SNMP_COMMUNITY)) {
			Snmp info = event.getSnmp();
			if (info != null && info.getCommunity() != null) {
				retParmVal = info.getCommunity();
			}
		} else if (parm.equals(TAG_SEVERITY)) {
			retParmVal = event.getSeverity();
		} else if (parm.equals(TAG_OPERINSTR)) {
			retParmVal = event.getOperinstruct();
		} else if (parm.equals(TAG_MOUSEOVERTEXT)) {
			retParmVal = event.getMouseovertext();
                } else if (parm.equals(TAG_TTICKET_ID)) {
                        Tticket ticket = event.getTticket();
                        retParmVal = ticket == null ? "" : ticket.getContent();
		} else if (parm.equals(PARMS_VALUES)) {
			retParmVal = getAllParmValues(event);
		} else if (parm.equals(PARMS_NAMES)) {
			retParmVal = getAllParmNames(event);
		} else if (parm.equals(PARMS_ALL)) {
			retParmVal = getAllParamValues(event);
		} else if (parm.equals(NUM_PARMS_STR)) {
			retParmVal = String.valueOf(event.getParmCollection().size());
		} else if (parm.startsWith(PARM_NUM_PREFIX)) {
			retParmVal = getNumParmValue(parm, event);
		} else if (parm.startsWith(PARM_NAME_NUMBERED_PREFIX)) {
		    retParmVal = getNumParmName(parm, event);
		} else if (parm.startsWith(PARM_BEGIN)) {
			if (parm.length() > PARM_BEGIN_LENGTH) {
				retParmVal = getNamedParmValue(parm, event);
			}
                } else if (parm.startsWith(ASSET_BEGIN)) {
			retParmVal = null;
			String assetFieldValue = null;
			if (event.getNodeid() > 0) {
				assetFieldValue = getAssetFieldValue(parm, event.getNodeid());
			}
			if (assetFieldValue != null)
				retParmVal = assetFieldValue;
			else
				retParmVal = "Unknown";
		}

		return (retParmVal == null ? null : retParmVal.trim());
	}

    /**
     * Helper method.
     * 
     * @param event
     * @return All event parameter values as a String.
     */
    private static String getAllParmValues(Event event) {
        String retParmVal = null;
        if (event.getParmCollection().size() < 1) {
        	retParmVal = null;
        } else {
        	StringBuffer ret = new StringBuffer();

        	for (Parm evParm : event.getParmCollection()) {
        		Value parmValue = evParm.getValue();
        		if (parmValue == null)
        			continue;

        		String parmValueStr = getValueAsString(parmValue);
        		if (parmValueStr == null)
        			continue;

        		if (ret.length() == 0) {
        			ret.append(parmValueStr);
        		} else {
        			ret.append(SPACE_DELIM + parmValueStr);
        		}
        	}

        	retParmVal = ret.toString();
        }
        return retParmVal;
    }

    /**
     * Helper method.
     * @param event
     * @return The names of all the event parameters.
     */
    private static String getAllParmNames(Event event) {
        if (event.getParmCollection().size() <= 0) {
            return null;
        } else {
            StringBuffer ret = new StringBuffer();

            for (Parm evParm : event.getParmCollection()) {
                String parmName = evParm.getParmName();
                if (parmName == null)
                    continue;

                if (ret.length() == 0) {
                    ret.append(parmName.trim());
                } else {
                    ret.append(SPACE_DELIM + parmName.trim());
                }
            }
            return ret.toString();
        }
    }

    /**
     * Helper method.
     * 
     * @param event
     * @return All event parameter values as a String
     */
    private static String getAllParamValues(final Event event) {
        if (event.getParmCollection().size() < 1) {
            return null;
        } else {
            final StringBuffer ret = new StringBuffer();

            for (final Parm evParm : event.getParmCollection()) {
                final String parmName = evParm.getParmName();
                if (parmName == null) continue;

                final Value parmValue = evParm.getValue();
                if (parmValue == null) continue;

                final String parmValueStr = getValueAsString(parmValue);
                if (ret.length() != 0) {
                    ret.append(SPACE_DELIM);
                }

                ret.append(parmName.trim()).append(NAME_VAL_DELIM).append("\"").append(parmValueStr).append("\"");
            }

            return ret.toString().intern();
        }
    }

    /**
     * Helper method.
     * 
     * @param parm
     * @param event
     * @return The name of a parameter based on its ordinal position in the event's list of parameters
     */
    private static String getNumParmName(String parm, Event event) {
        String retParmVal = null;
        final List<Parm> parms = event.getParmCollection();
        int end = parm.lastIndexOf(PARM_END_SUFFIX);
        if (end != -1 && parms != null && parms.size() > 0) {
        	// Get the string between the '#' and ']'
        	String parmSpec = parm.substring(PARM_NAME_NUMBERED_PREFIX_LENGTH, end);
            String eparmnum = null;
            String eparmsep = null;
            String eparmoffset = null;
            String eparmrangesep = null;
            String eparmrangelen = null;
            if (parmSpec.matches("^\\d+$")) {
                eparmnum = parmSpec;
            } else {
                Matcher m = Pattern.compile("^(\\d+)([^0-9+-]+)([+-]?\\d+)((:)([+-]?\\d+)?)?$").matcher(parmSpec);
                if (m.matches()) {
                    eparmnum = m.group(1);
                    eparmsep = m.group(2);
                    eparmoffset = m.group(3);
                    eparmrangesep = m.group(5);
                    eparmrangelen = m.group(6);
                }
            }
        	int parmNum = -1;
        	try {
        		parmNum = Integer.parseInt(eparmnum);
        	} catch (NumberFormatException nfe) {
        		parmNum = -1;
        		retParmVal = null;
        	}
    
        	if (parmNum > 0 && parmNum <= parms.size()) {
        	    final Parm evParm = parms.get(parmNum - 1);
    
        		// get parm name
        		String eparmname = evParm.getParmName();
        		
        		// If separator and offset specified, split and extract accordingly
        		if ((eparmsep != null) && (eparmoffset != null)) {
        		    int parmOffset = Integer.parseInt(eparmoffset);
        		    boolean doRange = ":".equals(eparmrangesep);
        		    int parmRangeLen = (eparmrangelen == null) ? 0 : Integer.parseInt(eparmrangelen);
        		    retParmVal = splitAndExtract(eparmname, eparmsep, parmOffset, doRange, parmRangeLen);
        		} else {
        			retParmVal = eparmname;
        		}
        	} else {
        		retParmVal = null;
        	}
        }
        return retParmVal;
    }
    
    private static String splitAndExtract(String src, String sep, int offset, boolean doRange, int rangeLen) {
        String sepLiteral = Pattern.quote(sep);
        
        // If the src string starts with the separator, lose the first separator
        if (src.startsWith(sep)) {
            src = src.replaceFirst(sepLiteral, "");
        }
        
        String components[] = src.split(sepLiteral);
        int startIndex, endIndex;
        if ((Math.abs(offset) > components.length) || (offset == 0)) {
            return null;
        } else if (offset < 0) {
            startIndex = components.length + offset;
        } else {
            // offset is, by definition, > 0
            startIndex = offset - 1;
        }
        
        endIndex = startIndex;
        
        if (! doRange) {
            return components[startIndex];
        } else if (rangeLen == 0) {
            endIndex = components.length - 1;
        } else if (rangeLen < 0) {
            endIndex = startIndex + 1 + rangeLen;
        } else {
            // rangeLen is, by definition, > 0
            endIndex = startIndex - 1 + rangeLen;
        }
        
        StringBuffer retVal = new StringBuffer();
        for (int i = startIndex; i <= endIndex; i++) {
            retVal.append(components[i]);
            if (i < endIndex) {
                retVal.append(sep);
            }
        }
        return retVal.toString();
    }

    /**
     * Helper method.
     * 
     * @param parm
     * @param event
     * @return The value of a parameter based on its ordinal position in the event's list of parameters
     */
    private static String getNumParmValue(String parm, Event event) {
        String retParmVal = null;
        final List<Parm> parms = event.getParmCollection();
        int end = parm.lastIndexOf(PARM_END_SUFFIX);
        if (end != -1 && parms != null && parms.size() > 0) {
        	// Get the value between the '#' and ']'
        	String eparmname = parm.substring(PARM_NUM_PREFIX_LENGTH, end);
        	int parmNum = -1;
        	try {
        		parmNum = Integer.parseInt(eparmname);
        	} catch (NumberFormatException nfe) {
        		parmNum = -1;
        		retParmVal = null;
        	}

        	if (parmNum > 0 && parmNum <= parms.size()) {
        	    final Parm evParm = parms.get(parmNum - 1);

        		// get parm value
        		Value eparmval = evParm.getValue();
        		if (eparmval != null) {
        			retParmVal = getValueAsString(eparmval);
        		}
        	} else {
        		retParmVal = null;
        	}
        }
        return retParmVal;
    }

    /**
     * Helper method.
     *
     * @param parm a {@link java.lang.String} object.
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return A parameter's value as a String using the parameter's name..
     */
    public static String getNamedParmValue(String parm, Event event) {
        String retParmVal = null;
        int end = parm.indexOf(PARM_END_SUFFIX, PARM_BEGIN_LENGTH);
        if (end != -1) {
            // Get the value between the '[' and ']'
            String eparmname = parm.substring(PARM_BEGIN_LENGTH, end);

            for (Parm evParm : event.getParmCollection()) {
                String parmName = evParm.getParmName();
                if (parmName != null
                        && parmName.trim().equals(eparmname)) {
                    // get parm value
                    Value eparmval = evParm.getValue();
                    if (eparmval != null) {
                        retParmVal = getValueAsString(eparmval);
                        break;
                    }
                }
            }
        }
        return retParmVal;
    }

    /**
     * Expand the value if it has parms in one of the following formats -
     * %element% values are expanded to have the value of the element where
     * 'element' is an element in the event DTD - %parm[values-all]% is expanded
     * to a delimited list of all parmblock values - %parm[names-all]% is
     * expanded to a list of all parm names - %parm[all]% is expanded to a full
     * dump of all parmblocks - %parm[name]% is expanded to the value of the
     * parameter named 'name' - %parm[ <name>]% is replaced by the value of the
     * parameter named 'name', if present - %parm[# <num>]% is replaced by the
     * value of the parameter number 'num', if present - %parm[##]% is replaced
     * by the number of parameters
     *
     * @param inp
     *            the input string in which parm values are to be expanded
     * @return expanded value if the value had any parameter to expand, null
     *         otherwise
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static String expandParms(String inp, Event event) {
        return EventUtil.expandParms(inp, event, null);
    }

    /**
     * Expand the value if it has parms in one of the following formats -
     * %element% values are expanded to have the value of the element where
     * 'element' is an element in the event DTD - %parm[values-all]% is expanded
     * to a delimited list of all parmblock values - %parm[names-all]% is
     * expanded to a list of all parm names - %parm[all]% is expanded to a full
     * dump of all parmblocks - %parm[name]% is expanded to the value of the
     * parameter named 'name' - %parm[ <name>]% is replaced by the value of the
     * parameter named 'name', if present - %parm[# <num>]% is replaced by the
     * value of the parameter number 'num', if present - %parm[##]% is replaced
     * by the number of parameters
     *
     * @param inp
     *            the input string in which parm values are to be expanded
     * @param decode
     *            the varbind decode for this
     * @return expanded value if the value had any parameter to expand, null
     *         otherwise
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static String expandParms(String inp, Event event, Map<String, Map<String, String>> decode) {
        int index1 = -1;
        int index2 = -1;

        if (inp == null) {
            return null;
        }

        StringBuffer ret = new StringBuffer();

        String tempInp = inp;
        int inpLen = inp.length();

        // check input string to see if it has any %xxx% substring
        while ((tempInp != null) && ((index1 = tempInp.indexOf(PERCENT)) != -1)) {
            // copy till first %
            ret.append(tempInp.substring(0, index1));
            tempInp = tempInp.substring(index1);

            index2 = tempInp.indexOf(PERCENT, 1);
            if (index2 != -1) {
                // Get the value between the %s
                String parm = tempInp.substring(1, index2);
                // m_logger.debug("parm: " + parm + " found in value");

                // If there's any whitespace in between the % signs, then do not try to 
                // expand it with a parameter value
                if (parm.matches(".*\\s.*")) {
                    ret.append(PERCENT);
                    tempInp = tempInp.substring(1);
                    continue;
                }

                String parmVal = getValueOfParm(parm, event);
                // m_logger.debug("value of parm: " + parmVal);

                if (parmVal != null) {
                    if (decode != null && decode.containsKey(parm) && decode.get(parm).containsKey(parmVal)) {
                        ret.append(decode.get(parm).get(parmVal));
                        ret.append("(");
                        ret.append(parmVal);
                        ret.append(")");
                    } else {
                        ret.append(parmVal);
                    }
                }

                if (index2 < (inpLen - 1)) {
                    tempInp = tempInp.substring(index2 + 1);
                } else {
                    tempInp = null;
                }
            }
            else {
                break;
            }
        }

        if ((index1 == -1 || index2 == -1) && (tempInp != null)) {
            ret.append(tempInp);
        }

        String retStr = ret.toString();
        if (retStr != null && !retStr.equals(inp)) {
            return retStr;
        } else {
            return null;
        }
    }


	/**
	 * Retrieve nodeLabel from the node table of the database given a particular
	 * nodeId.
	 * 
	 * @param nodeId
	 *            Node identifier
	 * 
	 * @return nodeLabel Retreived nodeLabel
	 * 
	 * @throws SQLException
	 *             if database error encountered
	 */
	private static String getNodeLabel(long nodeId) throws SQLException {

		String nodeLabel = null;
		java.sql.Connection dbConn = null;
		try {
		    Statement stmt = null;
		    try {
		        // Get datbase connection from the factory
		        dbConn = DataSourceFactory.getInstance().getConnection();

		        // Issue query and extract nodeLabel from result set
		        stmt = dbConn.createStatement();
		        ResultSet rs = stmt
		                .executeQuery("SELECT nodelabel FROM node WHERE nodeid="
		                        + String.valueOf(nodeId));
		        if (rs.next()) {
		            nodeLabel = (String) rs.getString("nodelabel");
		        }
		    } finally {
		        // Close the statement
		        if (stmt != null) {
		            try {
		                stmt.close();
		            } catch (Throwable e) {
		                // do nothing
		            }
		        }
		    }
		} finally {

			// Close the database connection
			if (dbConn != null) {
				try {
					dbConn.close();
				} catch (Throwable t) {
					// do nothing
				}
			}
		}

		return nodeLabel;
	}

	/**
	 * Retrieve ifAlias from the snmpinterface table of the database given a particular
	 * nodeId and ipAddr.
	 *
	 * @param nodeId
	 *            Node identifier
	 * @param ipAddr
	 *            Interface IP address
	 *
	 * @return ifAlias Retreived ifAlias
	 *
	 * @throws SQLException
	 *             if database error encountered
	 */
	private static String getIfAlias(long nodeId, String ipaddr) throws SQLException {
		
		String ifAlias = null;
		java.sql.Connection dbConn = null;
		try {
	        Statement stmt = null;
	        try {
	            // Get database connection from the factory
	            dbConn = DataSourceFactory.getInstance().getConnection();
	            DbIpInterfaceEntry ipif = DbIpInterfaceEntry.get(dbConn, nodeId, InetAddressUtils.getInetAddress(ipaddr));
	            if (ipif != null) {
	                DbSnmpInterfaceEntry snmpif = DbSnmpInterfaceEntry.get(dbConn, nodeId, ipif.getIfIndex());
	                if (snmpif != null) ifAlias = snmpif.getAlias();
	            }
	        } finally {
	            // Close the statement
	            if (stmt != null) {
	                try {
	                    stmt.close();
	                } catch (Throwable e) {
	                    // do nothing
	                }
	            }
	        }
		} finally {
			
			// Close the database connection
			if (dbConn != null) {
				try {
					dbConn.close();
				} catch (Throwable t) {
					// do nothing
				}
			}
		}
		
		return ifAlias;
	}

	/**
	 * <p>cloneEvent</p>
	 *
	 * @param orig a {@link org.opennms.netmgt.xml.event.Event} object.
	 * @return a {@link org.opennms.netmgt.xml.event.Event} object.
	 */
	public static Event cloneEvent(Event orig) {
	       Event copy = null;
	        try {
	            // Write the object out to a byte array
	            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
	            ObjectOutputStream out = new ObjectOutputStream(bos);
	            out.writeObject(orig);
	            out.flush();
	            out.close();
	
	            // Make an input stream from the byte array and read
	            // a copy of the object back in.
	            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
	            copy = (Event)in.readObject();
	        }
	        catch(IOException e) {
	            LOG.error("Exception cloning event", e);
	        }
	        catch(ClassNotFoundException cnfe) {
	            LOG.error("Exception cloning event", cnfe);
	        }
	        return copy;
	}	

    /**
     * Helper method.
     * 
     * @param parm
     * @param event
     * @return The value of an asset field based on the nodeid of the event 
     */
    private static String getAssetFieldValue(String parm, long nodeId) {
        String retParmVal = null;
        int end = parm.lastIndexOf(ASSET_END_SUFFIX);
        // The "asset[" start of this parameter is 6 characters long
	String assetField = parm.substring(6,end);
        java.sql.Connection dbConn = null;
        try {
             Statement stmt = null;
             try {
                    // Get datbase connection from the factory
                    dbConn = DataSourceFactory.getInstance().getConnection();

                    // Issue query and extract nodeLabel from result set
                    stmt = dbConn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT " + assetField + " FROM assets WHERE nodeid=" + String.valueOf(nodeId));
                         if (rs.next()) {
                             retParmVal = (String) rs.getString(assetField);
                         }
                  } catch (SQLException sqlE) {
                                // do nothing
                    } finally {
                        // Close the statement
                        if (stmt != null) {
                            try {
                                stmt.close();
                            } catch (Throwable e) {
                                // do nothing
                            }
                        }
                    }
                  } finally {

                        // Close the database connection
                        if (dbConn != null) {
                                try {
                                        dbConn.close();
                                } catch (Throwable t) {
                                        // do nothing
                                }
                        }
                }

        return retParmVal;

    }
}
