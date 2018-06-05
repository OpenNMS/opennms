/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.eventd.processor.expandable.EventTemplate;
import org.opennms.netmgt.eventd.processor.expandable.ExpandableParameterResolver;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionOperations;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * EventUtil is used primarily for the event parm expansion - has methods used
 * by all the event components to send in the event and the element to expanded
 * and have the 'expanded' value sent back
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="mailto:weave@oculan.com">Brain Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public abstract class AbstractEventUtil implements EventUtil {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractEventUtil.class);

	/**
	 * The Event ID xml
	 */
	protected static final String TAG_EVENT_DB_ID = "eventid";

	/**
	 * The UEI xml tag
	 */
	protected static final String TAG_UEI = "uei";

	/**
	 * The event source xml tag
	 */
	protected static final String TAG_SOURCE = "source";

	/**
	* The event descr xml tag
	*/
	protected static final String TAG_DESCR = "descr";

	/**
	 * The event logmsg xml tag
	 */
	protected static final String TAG_LOGMSG = "logmsg";

	/**
	 * The event time xml tag
	 */
	protected static final String TAG_TIME = "time";
	
	/**
	 * The event time xml tag, short format
	 */
	protected static final String TAG_SHORT_TIME = "shorttime";

	/**
	 * The event dpname xml tag
	 */
	protected static final String TAG_DPNAME = "dpname";

	/**
	 * The event nodeid xml tag
	 */
	protected static final String TAG_NODEID = "nodeid";

	/**
	 * The event nodelabel xml tag
	 */
	protected static final String TAG_NODELABEL = "nodelabel";

	/**
	 * The event nodelocation xml tag
	 */
	protected static final String TAG_NODELOCATION = "nodelocation";

	/**
	 * The event host xml tag
	 */
	protected static final String TAG_HOST = "host";

	/**
	 * The event interface xml tag
	 */
	protected static final String TAG_INTERFACE = "interface";

	/**
	 * The foreignsource for the event's nodeid xml tag
	 */
	protected static final String TAG_FOREIGNSOURCE = "foreignsource";

	/**
	 * The foreignid for the event's nodeid xml tag
	 */
	protected static final String TAG_FOREIGNID = "foreignid";

	/**
	 * The event ifindex xml tag
	 */
	protected static final String TAG_IFINDEX = "ifindex";

	/**
	 * The reverse DNS lookup of the interface
	 */
	protected static final String TAG_INTERFACE_RESOLVE = "interfaceresolve";

	/**
	 * The reverse DNS lookup of the interface
	 */
	protected static final String TAG_IFALIAS = "ifalias";
	
	/**
	 * The event snmp id xml tag
	 */
	protected static final String TAG_SNMP_ID = "id";

	/**
	 * The SNMP xml tag
	 */
	protected static final String TAG_SNMP = "snmp";

	/**
	 * The event snmp idtext xml tag
	 */
	protected static final String TAG_SNMP_IDTEXT = "idtext";

	/**
	 * The event snmp version xml tag
	 */
	protected static final String TAG_SNMP_VERSION = "version";

	/**
	 * The event snmp specific xml tag
	 */
	protected static final String TAG_SNMP_SPECIFIC = "specific";

	/**
	 * The event snmp generic xml tag
	 */
	protected static final String TAG_SNMP_GENERIC = "generic";

	/**
	 * The event snmp community xml tag
	 */
	protected static final String TAG_SNMP_COMMUNITY = "community";

	/**
	 * The event snmp host xml tag
	 */
	protected static final String TAG_SNMPHOST = "snmphost";

	/**
	 * The event service xml tag
	 */
	protected static final String TAG_SERVICE = "service";

	/**
	 * The event severity xml tag
	 */
	protected static final String TAG_SEVERITY = "severity";

	/**
	 * The event operinstruct xml tag
	 */
	protected static final String TAG_OPERINSTR = "operinstruct";

	/**
	 * The event mouseovertext xml tag
	 */
	protected static final String TAG_MOUSEOVERTEXT = "mouseovertext";

	protected static final String TAG_TTICKET_ID = "tticketid";

	/**
	 * The string that starts the expansion for an asset field - used to lookup values
	 * of asset fields by their names
	 */
	protected static final String ASSET_BEGIN = "asset[";

	/**
	 * The string that ends the expansion of a parm
	 */
	protected static final String ASSET_END_SUFFIX = "]";

	/**
	 * The string that should be expanded to a list of all parm names
	 */
	protected static final String PARMS_NAMES = "parm[names-all]";

	/**
	 * The string that should be expanded to a list of all parm values
	 */
	protected static final String PARMS_VALUES = "parm[values-all]";

	/**
	 * The string that should be expanded to a list of all parms
	 */
	protected static final String PARMS_ALL = "parm[all]";

	/**
	 * The string that starts the expansion for a parm - used to lookup values
	 * of parameters by their names
	 */
	protected static final String PARM_BEGIN = "parm[";

	/**
	 * Pattern used to match and parse 'parm' tokens.
	 */
	protected static final Pattern PARM_REGEX = Pattern.compile("^parm\\[(.*)\\]$");

	/**
	 * The length of PARM_BEGIN
	 */
	protected static final int PARM_BEGIN_LENGTH = 5;

	/**
	 * The string that should be expanded to the number of parms
	 */
	protected static final String NUM_PARMS_STR = "parm[##]";

	/**
	 * The string that starts a parm number - used to lookup values of
	 * parameters by their position
	 */
	protected static final String PARM_NUM_PREFIX = "parm[#";

	/**
	 * The length of PARM_NUM_PREFIX
	 */
	protected static final int PARM_NUM_PREFIX_LENGTH = 6;
	
	/**
	 * The string that starts a request for the name of a numbered parm
	 */
	protected static final String PARM_NAME_NUMBERED_PREFIX = "parm[name-#";
	
	/**
	 * The length of PARM_NAME_NUMBERED_PREFIX
	 */
	protected static final int PARM_NAME_NUMBERED_PREFIX_LENGTH = 11;

	/**
	 * The string that ends the expansion of a parm
	 */
	protected static final String PARM_END_SUFFIX = "]";

	/**
	 * For expansion of the '%parms[all]%' - the parm name and value are added
	 * as delimiter separated list of ' <parmName>= <value>' strings
	 */
	protected static final char NAME_VAL_DELIM = '=';

	/**
	 */
	protected static final char SPACE_DELIM = ' ';

	/**
	 * The values and the corresponding attributes of an element are added
	 * delimited by ATTRIB_DELIM
	 */
	protected static final char ATTRIB_DELIM = ',';

	/**
	 * Substitute the actual percent sign
	 */
	protected static final String TAG_PERCENT_SIGN = "pctsign";

	/**
	 * The string that starts the expansion for a hardware field - used to lookup values
	 * of hardware attributes by their index|name 
	 */
	protected static final String HARDWARE_BEGIN = "hardware[";

	/**
	 * The string that ends the expansion of a hardware
	 */
	protected static final String HARDWARE_END_SUFFIX = "]";

	private static EventUtil m_instance = null; 

	public static synchronized EventUtil getInstance() {
		if (m_instance == null) {
			return BeanUtils.getBean("eventDaemonContext", "eventUtil", EventUtil.class);
		} else {
			return m_instance;
		}
	}

	/**
	 * Used only for unit testing.
	 * 
	 * @param instance
	 */
	public static void setInstance(EventUtil instance) {
		m_instance = instance;
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
		final StringBuilder outBuffer = new StringBuilder(inStr);

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

	@Autowired
	private TransactionOperations transactionOperations;

	private final LoadingCache<String, EventTemplate> eventTemplateCache;

	private final ExpandableParameterResolverRegistry resolverRegistry = new ExpandableParameterResolverRegistry();

	public AbstractEventUtil() {
	    this(null);
	}

	public AbstractEventUtil(MetricRegistry registry) {
	    // Build the cache, and enable statistics collection if we've been given a metric registry
	    final long maximumCacheSize = Long.parseLong(System.getProperty("org.opennms.eventd.eventTemplateCacheSize", "1000"));
	    final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
                .maximumSize(maximumCacheSize);
	    if (registry != null) {
	        cacheBuilder.recordStats();
	    }
	    eventTemplateCache = cacheBuilder.build(new CacheLoader<String, EventTemplate>() {
                public EventTemplate load(String key) throws Exception {
                   return new EventTemplate(key, AbstractEventUtil.this);
	        }
	    });

	    if (registry != null) {
	        // Expose the cache statistics via a series of gauges
	        registry.register(MetricRegistry.name("eventutil.cache.capacity"),
	                new Gauge<Long>() {
	                    @Override
	                    public Long getValue() {
	                        return maximumCacheSize;
	                    }
	                });

	        registry.register(MetricRegistry.name("eventutil.cache.size"),
	                new Gauge<Long>() {
	                    @Override
	                    public Long getValue() {
	                        return eventTemplateCache.size();
	                    }
	                });

	        registry.register(MetricRegistry.name("eventutil.cache.evictioncount"),
	                new Gauge<Long>() {
	                    @Override
	                    public Long getValue() {
	                        return eventTemplateCache.stats().evictionCount();
	                    }
	                });

	        registry.register(MetricRegistry.name("eventutil.cache.avgloadpenalty"),
	                new Gauge<Double>() {
	                    @Override
	                    public Double getValue() {
	                        return eventTemplateCache.stats().averageLoadPenalty();
	                    }
	                });
	    }
	}

	/**
	 * Helper method.
	 * 
	 * @param event
	 * @return All event parameter values as a String.
	 */
	protected static String getAllParmValues(Event event) {
		String retParmVal = null;
		if (event.getParmCollection().size() < 1) {
			retParmVal = null;
		} else {
			final StringBuilder ret = new StringBuilder();

			for (Parm evParm : event.getParmCollection()) {
				Value parmValue = evParm.getValue();
				if (parmValue == null) continue;

				String parmValueStr = EventConstants.getValueAsString(parmValue);
				if (parmValueStr == null) continue;

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
	protected static String getAllParmNames(Event event) {
		if (event.getParmCollection().size() <= 0) {
			return null;
		} else {
			final StringBuilder ret = new StringBuilder();

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
	protected static String getAllParamValues(final Event event) {
		if (event.getParmCollection().size() < 1) {
			return null;
		} else {
			final StringBuilder ret = new StringBuilder();

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

	/**
	 * Helper method.
	 * 
	 * @param parm
	 * @param event
	 * @return The name of a parameter based on its ordinal position in the event's list of parameters
	 */
	protected static String getNumParmName(String parm, Event event) {
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

	public static String splitAndExtract(String src, String sep, int offset, boolean doRange, int rangeLen) {
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
		
		final StringBuilder retVal = new StringBuilder();
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
	protected static String getNumParmValue(String parm, Event event) {
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

	/**
	 * Helper method.
	 *
	 * @param parm a {@link java.lang.String} object.
	 * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
	 * @return A parameter's value as a String using the parameter's name..
	 */
	public String getNamedParmValue(String parm, Event event) {
	    final Matcher matcher = PARM_REGEX.matcher(parm);
	    if (!matcher.matches()) {
	        return null;
	    }

	    final String eparmname = matcher.group(1);
	    final Parm evParm = event.getParmTrim(eparmname);
	    if (evParm != null) {
	        final Value eparmval = evParm.getValue();
            if (eparmval != null) {
                return EventConstants.getValueAsString(eparmval);
            }
	    }
	    return null;
	}

	/**
	 * <p>expandMapValues</p>
	 *
	 * @param map a {@link java.util.Map} object.
	 * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
	 */
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
	public String expandParms(String inp, Event event) {
		return expandParms(inp, event, null);
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
	 * @param input
	 *            the input string in which parm values are to be expanded
	 * @param decode
	 *            the varbind decode for this
	 * @return expanded value if the value had any parameter to expand, null
	 *         otherwise
	 * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
	 */
	public String expandParms(String input, Event event, Map<String, Map<String, String>> decode) {
		if (input == null) {
			return null;
		}
		try {
			final EventTemplate eventTemplate = eventTemplateCache.get(input);
			Supplier<String> expander = () -> eventTemplate.expand(event, decode);
			if (eventTemplate.requiresTransaction()) {
				Objects.requireNonNull(transactionOperations);
				return transactionOperations.execute(session -> expander.get());
			} else {
				return expander.get();
			}
		} catch (ExecutionException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * <p>getEventHost</p>
	 *
	 * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
	 * @return a {@link java.lang.String} object.
	 */
	@Override
	public String getEventHost(final Event event) {
		if (event.getHost() == null) {
			return null;
		}

		// If the event doesn't have a node ID, we can't lookup the IP address and be sure we have the right one since we don't know what node it is on
		if (!event.hasNodeid()) {
			return event.getHost();
		}

		try {
			return getHostName(event.getNodeid().intValue(), event.getHost());
		} catch (final Throwable t) {
			LOG.warn("Error converting host IP \"{}\" to a hostname, storing the IP.", event.getHost(), t);
			return event.getHost();
		}
	}

	@Override
	public ExpandableParameterResolver getResolver(String token) {
		return resolverRegistry.getResolver(token);
	}
	
	@Override
	public Date decodeSnmpV2TcDateAndTime(BigInteger octetStringValue) {
	    BigInteger year, month, dom, hour, min, sec, millis, offsetD, offsetH, offsetM;
	    int offsetMultiplier = 1;
	    octetStringValue.bitCount();
	    if (octetStringValue.bitLength() == 59) {
	        year =   octetStringValue.and(new BigInteger("ffff000000000000", 16)).shiftRight(6*8);
	        month =  octetStringValue.and(new BigInteger("0000ff0000000000", 16)).shiftRight(5*8).subtract(BigInteger.valueOf(1L));
	        dom =    octetStringValue.and(new BigInteger("000000ff00000000", 16)).shiftRight(4*8);
	        hour =   octetStringValue.and(new BigInteger("00000000ff000000", 16)).shiftRight(3*8);
	        min =    octetStringValue.and(new BigInteger("0000000000ff0000", 16)).shiftRight(2*8);
	        sec =    octetStringValue.and(new BigInteger("000000000000ff00", 16)).shiftRight(1*8);
	        millis = octetStringValue.and(new BigInteger("00000000000000ff", 16)).multiply(BigInteger.valueOf(100L));
	        offsetD = new BigInteger("2b", 16);  // '+' for positive offset versus UTC
	        offsetH = BigInteger.valueOf(0L);
	        offsetM = BigInteger.valueOf(0L);
	    } else if (octetStringValue.bitLength() == 83) {
                year =    octetStringValue.and(new BigInteger("ffff000000000000000000", 16)).shiftRight(9*8);
                month =   octetStringValue.and(new BigInteger("0000ff0000000000000000", 16)).shiftRight(8*8).subtract(BigInteger.valueOf(1L));
                dom =     octetStringValue.and(new BigInteger("000000ff00000000000000", 16)).shiftRight(7*8);
                hour =    octetStringValue.and(new BigInteger("00000000ff000000000000", 16)).shiftRight(6*8);
                min =     octetStringValue.and(new BigInteger("0000000000ff0000000000", 16)).shiftRight(5*8);
                sec =     octetStringValue.and(new BigInteger("000000000000ff00000000", 16)).shiftRight(4*8);
                millis =  octetStringValue.and(new BigInteger("00000000000000ff000000", 16)).shiftRight(3*8).multiply(BigInteger.valueOf(100L));
                offsetD = octetStringValue.and(new BigInteger("0000000000000000ff0000", 16)).shiftRight(2*8);
                offsetH = octetStringValue.and(new BigInteger("000000000000000000ff00", 16)).shiftRight(1*8);
                offsetM = octetStringValue.and(new BigInteger("00000000000000000000ff", 16));
	    } else {
	        LOG.warn("Not sure what to do with the DateAndTime value '{}'. Using current time instead.");
	        return null;
	    }

	    if (offsetD.intValueExact() == '-') {
	        offsetMultiplier = -1;
	    }
	    BigInteger offsetMs = offsetH.multiply(BigInteger.valueOf(3600));
	    offsetMs.add(offsetM.multiply(BigInteger.valueOf(60L)));
	    offsetMs.multiply(BigInteger.valueOf(1000L));
	    offsetMs.multiply(BigInteger.valueOf(offsetMultiplier));

	    // Now we can build our Date
	    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	    cal.set(Calendar.YEAR, year.intValue());
	    cal.set(Calendar.MONTH, month.intValue());
	    cal.set(Calendar.DAY_OF_MONTH, dom.intValue());
	    cal.set(Calendar.HOUR_OF_DAY, hour.intValue());
	    cal.set(Calendar.MINUTE, min.intValue());
	    cal.set(Calendar.SECOND, sec.intValue());
	    cal.set(Calendar.MILLISECOND, millis.intValue());
	    cal.set(Calendar.ZONE_OFFSET, offsetMs.intValue());
	    return cal.getTime();
	}
}
