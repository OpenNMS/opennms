
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import java.beans.IntrospectionException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.locks.Lock;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.datatype.XMLGregorianCalendar;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSeverityEditor;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.PrimaryTypeEditor;
import org.opennms.netmgt.model.StatusTypeEditor;
import org.opennms.netmgt.provision.persist.StringXmlCalendarPropertyEditor;
import org.opennms.web.rest.support.InetAddressTypeEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.googlecode.concurentlocks.ReadWriteUpdateLock;
import com.googlecode.concurentlocks.ReentrantReadWriteUpdateLock;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * <p>OnmsRestService class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class OnmsRestService {
	private static final Logger LOG = LoggerFactory.getLogger(OnmsRestService.class);

	private final ReadWriteUpdateLock m_globalLock = new ReentrantReadWriteUpdateLock();
	private final Lock m_readLock = m_globalLock.updateLock();
	private final Lock m_writeLock = m_globalLock.writeLock();

	protected static final int DEFAULT_LIMIT = 10;

	protected enum ComparisonOperation { EQ, NE, ILIKE, LIKE, IPLIKE, GT, LT, GE, LE, CONTAINS }

	/**
	 * <p>Constructor for OnmsRestService.</p>
	 */
	public OnmsRestService() {
		super();
	}

	protected void readLock() {
	    m_readLock.lock();
	}
	
	protected void readUnlock() {
	    m_readLock.unlock();
	}

	protected void writeLock() {
	    m_writeLock.lock();
	}

	protected void writeUnlock() {
	    m_writeLock.unlock();
	}


	protected void applyQueryFilters(final MultivaluedMap<String,String> p, final CriteriaBuilder builder) {
		this.applyQueryFilters(p, builder, DEFAULT_LIMIT);
	}

	protected void applyQueryFilters(final MultivaluedMap<String,String> p, final CriteriaBuilder builder, final Integer defaultLimit) {

		final MultivaluedMap<String, String> params = new MultivaluedMapImpl();
	    params.putAll(p);

	    builder.distinct();
	    builder.limit(defaultLimit);

	    // not sure why we remove this, but that's what the old query filter code did, I presume there's a reason  :)
	    params.remove("_dc");

    	if (params.containsKey("limit")) {
    		builder.limit(Integer.valueOf(params.getFirst("limit")));
    		params.remove("limit");
    	}
    	if (params.containsKey("offset")) {
    		builder.offset(Integer.valueOf(params.getFirst("offset")));
    		params.remove("offset");
    	}
    	// Is this necessary anymore? setLimitOffset() comments implies it's for Ext-JS.
    	if (params.containsKey("start")) {
    		builder.offset(Integer.valueOf(params.getFirst("start")));
    		params.remove("start");
    	}
    	
	    if(params.containsKey("orderBy")) {
	    	builder.orderBy(params.getFirst("orderBy"));
			params.remove("orderBy");
			
			if(params.containsKey("order")) {
				if("desc".equalsIgnoreCase(params.getFirst("order"))) {
					builder.desc();
				} else {
					builder.asc();
				}
				params.remove("order");
			}
		}

	    final String query = removeParameter(params, "query");
	    if (query != null) builder.sql(query);

		final String matchType;
		final String match = removeParameter(params, "match");
		if (match == null) {
			matchType = "all";
		} else {
			matchType = match;
		}
		builder.match(matchType);

		final Class<?> criteriaClass = builder.toCriteria().getCriteriaClass();
		final BeanWrapper wrapper = getBeanWrapperForClass(criteriaClass);

		final String comparatorParam = removeParameter(params, "comparator", "eq").toLowerCase();
		final Criteria currentCriteria = builder.toCriteria();

		for (final String key : params.keySet()) {
			for (final String paramValue : params.get(key)) { // NOSONAR
                        // NOSONAR the interface of MultivaluedMap.class declares List<String> as return value, 
                        // the actual implementation com.sun.jersey.core.util.MultivaluedMapImpl returns a String, so this is fine in some way ...
				if ("null".equalsIgnoreCase(paramValue)) {
					builder.isNull(key);
				} else if ("notnull".equalsIgnoreCase(paramValue)) {
					builder.isNotNull(key);
				} else {
					Object value;
					Class<?> type = Object.class;
                    try {
                        type = currentCriteria.getType(key);
                    } catch (final IntrospectionException e) {
                        LOG.debug("Unable to determine type for key {}", key);
                    }
                    if (type == null) {
                        type = Object.class;
                    }
                    LOG.warn("comparator = {}, key = {}, propertyType = {}", comparatorParam, key, type);

                    if (comparatorParam.equals("contains") || comparatorParam.equals("iplike") || comparatorParam.equals("ilike") || comparatorParam.equals("like")) {
						value = paramValue;
					} else {
				        LOG.debug("convertIfNecessary({}, {})", key, paramValue);
				        try {
                            value = wrapper.convertIfNecessary(paramValue, type);
                        } catch (final Throwable t) {
                            LOG.debug("failed to introspect (key = {}, value = {})", key, paramValue, t);
                            value = paramValue;
                        }
					}

					try {
	    				final Method m = builder.getClass().getMethod(comparatorParam, String.class, Object.class);
						m.invoke(builder, new Object[] { key, value });
					} catch (final Throwable t) {
    					LOG.warn("Unable to find method for comparator: {}, key: {}, value: {}", comparatorParam, key, value, t);
					}
				}
			}
		}
    }

	protected BeanWrapper getBeanWrapperForClass(final Class<?> criteriaClass) {
		final BeanWrapper wrapper = new BeanWrapperImpl(criteriaClass);
		wrapper.registerCustomEditor(XMLGregorianCalendar.class, new StringXmlCalendarPropertyEditor());
		wrapper.registerCustomEditor(java.util.Date.class, new ISO8601DateEditor());
		wrapper.registerCustomEditor(java.net.InetAddress.class, new InetAddressTypeEditor());
		wrapper.registerCustomEditor(OnmsSeverity.class, new OnmsSeverityEditor());
		wrapper.registerCustomEditor(PrimaryType.class, new PrimaryTypeEditor());
		wrapper.registerCustomEditor(StatusType.class, new StatusTypeEditor());
		return wrapper;
	}


    protected String removeParameter(final MultivaluedMap<java.lang.String, java.lang.String> params, final String key) {
    	if (params.containsKey(key)) {
    		final String value = params.getFirst(key);
    		params.remove(key);
    		return value;
    	} else {
    		return null;
    	}
    }
    
    protected String removeParameter(final MultivaluedMap<java.lang.String, java.lang.String> params, final String key, final String defaultValue) {
    	final String value = removeParameter(params, key);
    	if (value == null) {
    		return defaultValue;
    	} else {
    		return value;
    	}
    }
    
    /**
     * <p>throwException</p>
     *
     * @param status a {@link javax.ws.rs.core.Response.Status} object.
     * @param msg a {@link java.lang.String} object.
     * @param <T> a T object.
     * @return a T object.
     */
    protected <T> WebApplicationException getException(final Status status, String msg, String... params) throws WebApplicationException {
        if (params != null) msg = MessageFormatter.arrayFormat(msg, params).getMessage();
        LOG.error(msg);
        return new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(msg).build());
    }

    protected <T> WebApplicationException getException(Status status, Throwable t) throws WebApplicationException {
        LOG.error(t.getMessage(), t);
        return new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(t.getMessage()).build());
    }


    /**
     * Convert a column name with underscores to the corresponding property name using "camel case".  A name
     * like "customer_number" would match a "customerNumber" property name.
     *
     * @param name the column name to be converted
     * @return the name using "camel case"
     */
    public static String convertNameToPropertyName(String name) {
        StringBuffer result = new StringBuffer();
        boolean nextIsUpper = false;
        if (name != null && name.length() > 0) {
            if (name.length() > 1 && (name.substring(1, 2).equals("_") || (name.substring(1, 2).equals("-")))) {
                result.append(name.substring(0, 1).toUpperCase());
            } else {
                result.append(name.substring(0, 1).toLowerCase());
            }
            for (int i = 1; i < name.length(); i++) {
                String s = name.substring(i, i + 1);
                if (s.equals("_") || s.equals("-")) {
                    nextIsUpper = true;
                } else {
                    if (nextIsUpper) {
                        result.append(s.toUpperCase());
                        nextIsUpper = false;
                    } else {
                        result.append(s.toLowerCase());
                    }
                }
            }
        }
        return result.toString();
    }

    protected static URI getRedirectUri(final UriInfo m_uriInfo, final Object... pathComponents) {
        if (pathComponents != null && pathComponents.length == 0) {
            final URI requestUri = m_uriInfo.getRequestUri();
            try {
                return new URI(requestUri.getScheme(), requestUri.getUserInfo(), requestUri.getHost(), requestUri.getPort(), requestUri.getPath().replaceAll("/$", ""), null, null);
            } catch (final URISyntaxException e) {
                return requestUri;
            }
        } else {
            UriBuilder builder = m_uriInfo.getRequestUriBuilder();
            for (final Object component : pathComponents) {
                if (component != null) {
                    builder = builder.path(component.toString());
                }
            }
            return builder.build();
        }
    }

    /**
     * <p>setProperties</p>
     *
     * @param params a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
     * @param req a {@link java.lang.Object} object.
     */
	protected void setProperties(final org.opennms.web.rest.MultivaluedMapImpl params, final Object req) {
        RestUtils.setBeanProperties(req, params);
    }

}
