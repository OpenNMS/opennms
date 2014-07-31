package org.opennms.netmgt.eventd;

import java.net.InetAddress;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.event.Tticket;
import org.opennms.netmgt.xml.event.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class EventUtilDaoImpl implements EventUtil{
	
	private static final Logger LOG = LoggerFactory.getLogger(EventUtilDaoImpl.class);
	
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
	static final String ASSET_BEGIN = "asset[";

	/**
	 * The string that ends the expansion of a parm
	 */
	static final String ASSET_END_SUFFIX = "]";

	/**
	 * The '%' sign used to indicate parms to be expanded
	 */
	static final char PERCENT = '%';

	/**
	 * The string that should be expanded to a list of all parm names
	 */
	static final String PARMS_NAMES = "parm[names-all]";

	/**
	 * The string that should be expanded to a list of all parm values
	 */
	static final String PARMS_VALUES = "parm[values-all]";

	/**
	 * The string that should be expanded to a list of all parms
	 */
	static final String PARMS_ALL = "parm[all]";

	/**
	 * The string that starts the expansion for a parm - used to lookup values
	 * of parameters by their names
	 */
	static final String PARM_BEGIN = "parm[";

	/**
	 * The length of PARM_BEGIN
	 */
	static final int PARM_BEGIN_LENGTH = 5;

	/**
	 * The string that should be expanded to the number of parms
	 */
	static final String NUM_PARMS_STR = "parm[##]";

	/**
	 * The string that starts a parm number - used to lookup values of
	 * parameters by their position
	 */
	static final String PARM_NUM_PREFIX = "parm[#";

	/**
	 * The length of PARM_NUM_PREFIX
	 */
	static final int PARM_NUM_PREFIX_LENGTH = 6;
	
	/**
	 * The string that starts a request for the name of a numbered parm
	 */
	static final String PARM_NAME_NUMBERED_PREFIX = "parm[name-#";
	
	/**
	 * The length of PARM_NAME_NUMBERED_PREFIX
	 */
	static final int PARM_NAME_NUMBERED_PREFIX_LENGTH = 11;

	/**
	 * The string that ends the expansion of a parm
	 */
	static final String PARM_END_SUFFIX = "]";

	/**
	 * For expansion of the '%parms[all]%' - the parm name and value are added
	 * as delimiter separated list of ' <parmName>= <value>' strings
	 */
	static final char NAME_VAL_DELIM = '=';

	/**
	 */
	static final char SPACE_DELIM = ' ';

	/**
	 * The values and the corresponding attributes of an element are added
	 * delimited by ATTRIB_DELIM
	 */
	static final char ATTRIB_DELIM = ',';

	/**
	 * Substitute the actual percent sign
	 */
	static final String TAG_PERCENT_SIGN = "pctsign";

	@Autowired
	private NodeDao nodeDao;
	
	@Autowired
	private SnmpInterfaceDao snmpInterfaceDao;
	
	@Autowired
	private AssetRecordDao assetRecordDao;
	
	@Autowired
	private IpInterfaceDao ipInterfaceDao;
	
	@Override
	public String getNodeLabel(long nodeId) throws SQLException {
		return nodeDao.getLabelForId(Integer.valueOf((int)nodeId));
	}

	@Override
	public String getIfAlias(long nodeId, String ipaddr) throws SQLException {
		return ipInterfaceDao.findByNodeIdAndIpAddress((int)nodeId, ipaddr).getSnmpInterface().getIfAlias();
				
	}

	@Override
	public String getAssetFieldValue(String parm, long nodeId) {
		return assetRecordDao.findByNodeId((int)nodeId).getNode().getLabel();
    	
		
	}

	@Override
	public String expandParms(String string, Event event) {
		return expandParms(string, event, null);
	}

	@Override
	public String expandParms(String inp, Event event, Map<String, Map<String, String>> decode) {
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

	@Override
	public String escape(String inStr, char delimchar) {
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

	public String getValueOfParm(String parm, Event event) {
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
			try {
				nodeLabel = getNodeLabel(event.getNodeid());
			} catch (SQLException e) {
				LOG.info("nodelabel Unavailable for {}", event.getNodeid(), e);
			}
			if (nodeLabel != null)
				retParmVal = WebSecurityUtils.sanitizeString(nodeLabel);
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
			if (event.getNodeid() > 0 && event.getInterface() != null) {
				try {
					ifAlias = getIfAlias(event.getNodeid(), ifString);
				} catch (SQLException e) {
					LOG.info("ifAlias Unavailable for {}:{}", event.getNodeid(), event.getInterface(), e);
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
	
	private String getNumParmName(String parm, Event event) {
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
	
	public String splitAndExtract(String src, String sep, int offset, boolean doRange, int rangeLen) {
		String sepLiteral = Pattern.quote(sep);
        
        // If the src string starts with the separator, lose the first separator
        if (src.startsWith(sep)) {
            src = src.replaceFirst(sepLiteral, "");
        }
        
        String[] components = src.split(sepLiteral);
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

	private String getNumParmValue(String parm, Event event) {
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
        			retParmVal = EventConstants.getValueAsString(eparmval);
        		}
        	} else {
        		retParmVal = null;
        	}
        }
        return retParmVal;
	}

	private String getAllParmNames(Event event) {
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

	public String getAllParmValues(Event event) {
		String retParmVal = null;
        if (event.getParmCollection().size() < 1) {
        	retParmVal = null;
        } else {
        	StringBuffer ret = new StringBuffer();

        	for (Parm evParm : event.getParmCollection()) {
        		Value parmValue = evParm.getValue();
        		if (parmValue == null)
        			continue;

        		String parmValueStr = EventConstants.getValueAsString(parmValue);
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
	
	public void expandMapValues(final Map<String, String> map, final Event event) {
		for (final Entry<String,String> entry : map.entrySet()) {
            final String key = entry.getKey();
            final String mapValue = entry.getValue();
            if (mapValue == null) {
                continue;
            }
            final String expandedValue = expandParms(map.get(key), event);
            if (expandedValue == null) {
                // Don't use this value to replace the existing value if it's null
            } else {
                map.put(key, expandedValue);
            }
        }
		
	}
	
	public String getAllParamValues(Event event) {
		if (event.getParmCollection().size() < 1) {
            return null;
        } else {
            final StringBuffer ret = new StringBuffer();

            for (final Parm evParm : event.getParmCollection()) {
                final String parmName = evParm.getParmName();
                if (parmName == null) continue;

                final Value parmValue = evParm.getValue();
                if (parmValue == null) continue;

                final String parmValueStr = EventConstants.getValueAsString(parmValue);
                if (ret.length() != 0) {
                    ret.append(SPACE_DELIM);
                }

                ret.append(parmName.trim()).append(NAME_VAL_DELIM).append("\"").append(parmValueStr).append("\"");
            }

            return ret.toString().intern();
        }
	}
	
	public String getNamedParmValue(String parm, Event event) {
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
                        retParmVal = EventConstants.getValueAsString(eparmval);
                        break;
                    }
                }
            }
        }
        return retParmVal;
		
	}

}
