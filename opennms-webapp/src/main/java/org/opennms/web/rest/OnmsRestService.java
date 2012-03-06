
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.datatype.XMLGregorianCalendar;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsRestrictions;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.provision.persist.StringXmlCalendarPropertyEditor;
import org.opennms.web.rest.support.InetAddressTypeEditor;
import org.opennms.web.rest.support.OnmsSeverityTypeEditor;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessorFactory;

import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * <p>OnmsRestService class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class OnmsRestService {

	protected static final int DEFAULT_LIMIT = 10;

	protected enum ComparisonOperation { EQ, NE, ILIKE, LIKE, IPLIKE, GT, LT, GE, LE, CONTAINS }

	private List<Order> m_ordering = new ArrayList<Order>();
	private Integer m_limit = null;
	private Integer m_offset = null;

	/**
	 * <p>Constructor for OnmsRestService.</p>
	 */
	public OnmsRestService() {
		super();
	}

	/**
	 * Convenience for the other setLimitOffset method with the extra parameter, passing a default limit of 10
	 *
	 * @param params See other setLimitOffset
	 * @param criteria See other setLimitOffset
	 */
	protected void setLimitOffset(MultivaluedMap<java.lang.String, java.lang.String> params, OnmsCriteria criteria) {
		setLimitOffset(params, criteria, 10);  //Default limit is 10
	}
	
    /**
     * <p>setLimitOffset</p>
     *
     * @param params a {@link javax.ws.rs.core.MultivaluedMap} object.
     * @param criteria a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     * @param defaultLimit a int.
     */
    protected void setLimitOffset(MultivaluedMap<java.lang.String, java.lang.String> params, OnmsCriteria criteria, int defaultLimit) {
        setLimitOffset(params, criteria, defaultLimit, true);
    }

	/**
	 * Uses parameters in params to setup criteria with standard limit and offset parameters.
	 * If "limit" is in params, is used, otherwise default limit is used.  If limit is 0, then no limit is set
	 * If "offset" is in params, is set as the offset into the result set
	 * In both cases, the limit and offset parameters are removed if found.
	 *
	 * @param params Set of parameters to look in for limit and offset
	 * @param criteria The Criteria that will be modified with the limit and offset
	 * @param defaultLimit A limit to use if none is specified in the params
	 * @param addImmediately a boolean.
	 */
	protected void setLimitOffset(MultivaluedMap<java.lang.String, java.lang.String> params, OnmsCriteria criteria, int defaultLimit, boolean addImmediately) {
		Integer limit=defaultLimit;
		if(params.containsKey("limit")) {
		    limit = Integer.valueOf(params.getFirst("limit"));
			params.remove("limit");
		}
		if(limit != null && limit != 0) {
		    if (addImmediately) {
		        criteria.setMaxResults(limit);
		    } else {
		        m_limit = limit;
		    }
		}

		Integer offset = null;
		if(params.containsKey("offset")) {
		    offset = Integer.valueOf(params.getFirst("offset"));
			params.remove("offset");
		}
		
		//added for the ExtJS will remove once it gets working with the offset
		if(params.containsKey("start") && offset == null){
		    offset = Integer.valueOf(params.getFirst("start"));
		    params.remove("start");
		}
		
		if (offset != null && offset != 0) {
		    if (addImmediately) {
		        criteria.setFirstResult(offset);
		    } else {
		        m_offset = offset;
		    }
		}
		
		
	}

    protected void applyQueryFilters(final MultivaluedMap<String,String> p, final CriteriaBuilder builder) {
		final MultivaluedMap<String, String> params = new MultivaluedMapImpl();
	    params.putAll(p);

	    builder.limit(DEFAULT_LIMIT);

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

        // Set default ordering
	    builder.orderBy("lastEventTime").desc();

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

		final BeanWrapper wrapper = new BeanWrapperImpl(builder.toCriteria().getCriteriaClass());
		wrapper.registerCustomEditor(java.util.Date.class, new ISO8601DateEditor());
		wrapper.registerCustomEditor(java.net.InetAddress.class, new InetAddressTypeEditor());
		wrapper.registerCustomEditor(OnmsSeverity.class, new OnmsSeverityTypeEditor());

		final String comparatorParam = removeParameter(params, "comparator", "eq").toLowerCase();

		for (final String key : params.keySet()) {
			for (final String paramValue : params.get(key)) {
				if ("null".equalsIgnoreCase(paramValue)) {
					builder.isNull(key);
				} else if ("notnull".equalsIgnoreCase(paramValue)) {
					builder.isNotNull(key);
				} else {
					final Object value;
					if (comparatorParam.equals("contains") || comparatorParam.equals("iplike") || comparatorParam.equals("ilike") || comparatorParam.equals("like")) {
						value = paramValue;
					} else {
						value = convertIfNecessary(wrapper, key, paramValue);
					}
    				LogUtils.warnf(this, "comparator = %s, key = %s, propertyType = %s", comparatorParam, key, wrapper.getPropertyType(key));

					try {
	    				final Method m = builder.getClass().getMethod(comparatorParam, String.class, Object.class);
						m.invoke(builder, new Object[] { key, value });
					} catch (final Throwable t) {
    					LogUtils.warnf(this, t, "Unable to find method for comparator: %s, key: %s, value: %s", comparatorParam, key, value);
					}
				}
			}
		}
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
	 * Method to pull out all the named params in params and use them to add restriction filters to a criteria object.
	 * Uses the objectClass to determine parameters and types; auto converts from strings to appropriate types, if at all possible.
	 * Additionally, the param "comparator", if set, will change the comparision from the default of equality.  Acceptable comparators are:
	 * "equals", "ilike", "like", "gt", "lt", "ge", "le", "ne" (other values will default to equality).
	 * If there is an "orderBy" param, results will be ordered by the property name given.  Default is ascending, unless "order" is set to "desc"
	 * If there is a "query" param, it will be added to the criteria as a raw hibernate SQL statement (in addition to any other parameters specified
	 *
	 * The "criteria" object will be populated with the filter and ordering details provided
	 *
	 * @param params set of string parameters from which various configuration properties are extracted
	 * @param criteria the object which will be populated with the filter/ordering
	 * @param objectClass the type of thing being filtered.
	 */
	protected void addFiltersToCriteria(MultivaluedMap<java.lang.String, java.lang.String> params, OnmsCriteria criteria, Class<?> objectClass) {
		
		MultivaluedMap<String, String> paramsCopy = new MultivaluedMapImpl();
	    paramsCopy.putAll(params);

	    final String query = removeParameter(paramsCopy, "query");
	    if (query != null) criteria.add(Restrictions.sqlRestriction(query));

		paramsCopy.remove("_dc");

		final String matchType;
		final String match = removeParameter(paramsCopy, "match");
		if (match == null) {
			matchType = "all";
		} else {
			matchType = match;
		}

		//By default, just do equals comparison
		ComparisonOperation op=ComparisonOperation.EQ;
		if(paramsCopy.containsKey("comparator")) {
			String comparatorLabel=paramsCopy.getFirst("comparator");
			paramsCopy.remove("comparator");
	
			if(comparatorLabel.equals("equals")) {
				op=ComparisonOperation.EQ;
			}else if (comparatorLabel.equals("ilike")) {
				op=ComparisonOperation.ILIKE;
			}else if (comparatorLabel.equals("like")) {
				op=ComparisonOperation.LIKE;
			}else if (comparatorLabel.equals("gt")) {
				op=ComparisonOperation.GT;
			}else if (comparatorLabel.equals("lt")) {
				op=ComparisonOperation.LT;
			}else if (comparatorLabel.equals("ge")) {
				op=ComparisonOperation.GE;
			}else if (comparatorLabel.equals("le")) {
				op=ComparisonOperation.LE;
			}else if (comparatorLabel.equals("ne")) {
				op=ComparisonOperation.NE;
			} else if (comparatorLabel.equals("contains")) {
			    op=ComparisonOperation.CONTAINS;
			} else if (comparatorLabel.equals("iplike")) {
			    op=ComparisonOperation.IPLIKE;
			}
		}
		BeanWrapper wrapper = new BeanWrapperImpl(objectClass);
		wrapper.registerCustomEditor(java.util.Date.class, new ISO8601DateEditor());
		wrapper.registerCustomEditor(java.net.InetAddress.class, new InetAddressTypeEditor());
		
		List<Criterion> criteriaList = new ArrayList<Criterion>();
		
		for(String key: paramsCopy.keySet()) {

		    for (String stringValue : paramsCopy.get(key)) {
    			if("null".equals(stringValue)) {
    				criteriaList.add(Restrictions.isNull(key));
    			} else if ("notnull".equals(stringValue)) {
    				criteriaList.add(Restrictions.isNotNull(key));
    			} else {
    				LogUtils.debugf(this, "key = %s, propertyType = %s", key, wrapper.getPropertyType(key));
					Object thisValue;
    				if(op == ComparisonOperation.CONTAINS || op == ComparisonOperation.IPLIKE) {
    				    thisValue = stringValue;
    				}else {
    				    thisValue = convertIfNecessary(wrapper, key, stringValue);
    				}
    				
    				switch(op) {
    		   		case EQ:
    		    		criteriaList.add(Restrictions.eq(key, thisValue));
    					break;
    		  		case NE:
    		  		    criteriaList.add(Restrictions.ne(key,thisValue));
    					break;
    		   		case ILIKE:
    		   		    criteriaList.add(Restrictions.ilike(key, thisValue));
    					break;
    		   		case LIKE:
    		   		    criteriaList.add(Restrictions.like(key, thisValue));
    					break;
    		   		case GT:
    		   		    criteriaList.add(Restrictions.gt(key, thisValue));
    					break;
    		   		case LT:
    		    		criteriaList.add(Restrictions.lt(key, thisValue));
    					break;
    		   		case GE:
    		    		criteriaList.add(Restrictions.ge(key, thisValue));
    					break;
    		   		case LE:
    		    		criteriaList.add(Restrictions.le(key, thisValue));
    					break;
    		   		case CONTAINS:
    		   		    criteriaList.add(Restrictions.ilike(key, stringValue, MatchMode.ANYWHERE));
    		   		    break;
    		   		case IPLIKE:
    		   		    criteriaList.add(OnmsRestrictions.ipLike(stringValue));
    		   		    break;
    				}
    			}
		    }
		}

		if (criteriaList.size() > 1 && matchType.equalsIgnoreCase("any")) {
		    // OR everything
		    Criterion lhs = criteriaList.remove(0);
		    Criterion rhs = criteriaList.remove(0);
	            
		    Criterion or = Restrictions.or(lhs, rhs);
		    while (criteriaList.size() > 0) {
		        rhs = criteriaList.remove(0);
		        or = Restrictions.or(or, rhs);
		    }
		    
		    criteria.add(or);
		} else {
		    for (Criterion c : criteriaList) {
		        criteria.add(c);
		    }
		}
	}

	@SuppressWarnings("unchecked")
    private Object convertIfNecessary(final BeanWrapper wrapper, final String key, final String stringValue) {
		LogUtils.debugf(this, "convertIfNecessary(%s, %s)", key, stringValue);
        return wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(key));
    }

    /**
     * Does ordering processing; pulled out to a separate method for visual clarity.  Configures ordering as defined in addFiltersToCriteria
     *
     * @param params - set of values to look in for the "order" and "orderBy" values
     * @param criteria - the criteria object which will be updated with ordering configuration
     */
    protected void addOrdering(MultivaluedMap<java.lang.String, java.lang.String> params, OnmsCriteria criteria) {
        addOrdering(params, criteria, true);
    }

    /**
     * Same as addOrdering() but you can say whether to add the order to the criteria object immediately.
     *
     * @param params - set of values to look in for the "order" and "orderBy" values
     * @param criteria - the criteria object which will be updated with ordering configuration
     * @param addImmediately - whether to add immediately to the criteria object.  Use "false" if you intend to
     * build a joined/distinct criteria object using {@link #getDistinctIdCriteria(Class, OnmsCriteria)}, or "true" otherwise.
     */
    protected void addOrdering(MultivaluedMap<java.lang.String, java.lang.String> params, OnmsCriteria criteria, boolean addImmediately) {
	    if(params.containsKey("orderBy")) {
			String orderBy=params.getFirst("orderBy");
			params.remove("orderBy");
			boolean orderAsc=true;
			if(params.containsKey("order")) {
				if("desc".equalsIgnoreCase(params.getFirst("order"))) {
					orderAsc=false;
				}
				params.remove("order");
			}
			Order o;
			if(orderAsc) {
			    o = Order.asc(orderBy);
			} else {
				o = Order.desc(orderBy);
			}
			if (addImmediately) {
			    criteria.addOrder(o);
			}
			m_ordering.add(o);
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
    protected <T> WebApplicationException getException(final Status status, final String msg) throws WebApplicationException {
        log().error(msg);
        return new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(msg).build());
    }

    protected <T> WebApplicationException getException(Status status, Throwable t) throws WebApplicationException {
        log().error(t.getMessage(), t);
        return new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(t.getMessage()).build());
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
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

    /**
     * <p>getDistinctIdCriteria</p>
     *
     * @param clazz a {@link java.lang.Class} object.
     * @param criteria a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     * @return a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     */
    protected OnmsCriteria getDistinctIdCriteria(Class<?> clazz, OnmsCriteria criteria) {
        criteria.setProjection(
                               Projections.distinct(
                                   Projections.projectionList().add(
                                       Projections.alias( Projections.property("id"), "id" )
                                   )
                               )
                           );
        LogUtils.infof(this, "**** m_offset: " + (m_offset == null ? 0 : m_offset) + " ****");
        OnmsCriteria rootCriteria = new OnmsCriteria(clazz);
        rootCriteria.add(Subqueries.propertyIn("id", criteria.getDetachedCriteria()));
        for (Order o : m_ordering) {
            rootCriteria.addOrder(o);
        }
        if (m_limit != null) {
            rootCriteria.setMaxResults(m_limit);
        }
        if (m_offset != null) {
            rootCriteria.setFirstResult(m_offset);
        }
        return rootCriteria;
    }
    
    /**
     * <p>setProperties</p>
     *
     * @param params a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
     * @param req a {@link java.lang.Object} object.
     */
	protected void setProperties(org.opennms.web.rest.MultivaluedMapImpl params, Object req) {
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(req);
        wrapper.registerCustomEditor(XMLGregorianCalendar.class, new StringXmlCalendarPropertyEditor());
        for(String key : params.keySet()) {
            String propertyName = convertNameToPropertyName(key);
            if (wrapper.isWritableProperty(propertyName)) {
                Object value = null;
                String stringValue = params.getFirst(key);
                value = convertIfNecessary(wrapper, propertyName, stringValue);
                wrapper.setPropertyValue(propertyName, value);
            }
        }
    }

}
