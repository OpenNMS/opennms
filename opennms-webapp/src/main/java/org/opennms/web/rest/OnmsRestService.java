package org.opennms.web.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Category;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsCriteria;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class OnmsRestService {

	protected enum ComparisonOperation { EQ, NE, ILIKE, LIKE, GT, LT, GE, LE, CONTAINS}

	public OnmsRestService() {
		super();
	}

	/**
	 * Convenience for the other setLimitOffset method with the extra parameter, passing a default limit of 10
	 * @param params See other setLimitOffset
	 * @param criteria See other setLimitOffset
	 */
	protected void setLimitOffset(MultivaluedMap<java.lang.String, java.lang.String> params, OnmsCriteria criteria) {
		setLimitOffset(params, criteria, 10);  //Default limit is 10
	}
	
	/**
	 * Uses parameters in params to setup criteria with standard limit and offset parameters.  
	 * If "limit" is in params, is used, otherwise default limit is used.  If limit is 0, then no limit is set
	 * If "offset" is in params, is set as the offset into the result set
	 * In both cases, the limit and offset parameters are removed if found.
	 * @param params Set of parameters to look in for limit and offset
	 * @param criteria The Criteria that will be modified with the limit and offset
	 * @param defaultLimit A limit to use if none is specified in the params
	 */
	protected void setLimitOffset(MultivaluedMap<java.lang.String, java.lang.String> params, OnmsCriteria criteria, int defaultLimit) {
		int limit=defaultLimit;
		boolean hasOffset = false;
		if(params.containsKey("limit")) {
			limit=Integer.parseInt(params.getFirst("limit"));
			params.remove("limit");
		}
		if(limit!=0) {
			criteria.setMaxResults(limit);
		}
	
		if(params.containsKey("offset")) {
			criteria.setFirstResult(Integer.parseInt(params.getFirst("offset")));
			params.remove("offset");
			hasOffset = true;
		}
		
		//added for the ExtJS will remove once it gets working with the offset
		if(params.containsKey("start") && !hasOffset){
		    criteria.setFirstResult(Integer.parseInt(params.getFirst("start")));
		    params.remove("start");
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
	protected void addFiltersToCriteria(MultivaluedMap<java.lang.String, java.lang.String> params, OnmsCriteria criteria, Class objectClass) {
		
		setOrdering(params, criteria);
		
		if(params.containsKey("query")) {
			String query=params.getFirst("query");
			criteria.add(Restrictions.sqlRestriction(query));
			params.remove("query");
		}
		
		if(params.containsKey("node.id")) {
		    String nodeId = params.getFirst("node.id");
		    Integer id = new Integer(nodeId);
		    criteria.createCriteria("node").add(Restrictions.eq("id", id));
		    //criteria.add(Restrictions.eq("id", 13255));
		    params.remove("node.id");
		    //criteria.add(Restrictions.like("id", "13508")); //.createCriteria("node").add(Restrictions.eq("id",147));
	        
		}
		
		params.remove("_dc");

		//By default, just do equals comparision
		ComparisonOperation op=ComparisonOperation.EQ;
		if(params.containsKey("comparator")) {
			String comparatorLabel=params.getFirst("comparator");
			params.remove("comparator");
	
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
			}
		}
		BeanWrapper wrapper = new BeanWrapperImpl(objectClass);
		wrapper.registerCustomEditor(java.util.Date.class, new ISO8601DateEditor());
		for(String key: params.keySet()) {
		    
		    String stringValue=params.getFirst(key);
		   
			if("null".equals(stringValue)) {
				criteria.add(Restrictions.isNull(key));
			} else if ("notnull".equals(stringValue)) {
				criteria.add(Restrictions.isNotNull(key));
			} else {
				Object thisValue=wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(key));
				switch(op) {
		   		case EQ:
		    		criteria.add(Restrictions.eq(key, thisValue));
					break;
		  		case NE:
		    		criteria.add(Restrictions.ne(key,thisValue));
					break;
		   		case ILIKE:
		    		criteria.add(Restrictions.ilike(key, thisValue));
					break;
		   		case LIKE:
		    		criteria.add(Restrictions.like(key, thisValue));
					break;
		   		case GT:
		    		criteria.add(Restrictions.gt(key, thisValue));
					break;
		   		case LT:
		    		criteria.add(Restrictions.lt(key, thisValue));
					break;
		   		case GE:
		    		criteria.add(Restrictions.ge(key, thisValue));
					break;
		   		case LE:
		    		criteria.add(Restrictions.le(key, thisValue));
					break;
		   		case CONTAINS:
		   		    criteria.add(Restrictions.ilike(key, stringValue, MatchMode.ANYWHERE));
		   		    
				}
			}
		}

	}

	/**
	 * Does ordering processing; pulled out to a separate method for visual clarity.  Configures ordering as defined in addFiltersToCriteria
	 * @param params - set of values to look in for the "order" and "orderBy" values
	 * @param criteria - the criteria object which will be updated with ordering configuration
	 */
	private void setOrdering(MultivaluedMap<java.lang.String, java.lang.String> params, OnmsCriteria criteria) {
		if(params.containsKey("orderBy")) {
			String orderBy=params.getFirst("orderBy");
			params.remove("orderBy");
			boolean orderAsc=true;
			if(params.containsKey("order")) {
				if("desc".equals(params.getFirst("order"))) {
					orderAsc=false;
				}
				params.remove("order");
			}
			if(orderAsc) {
				criteria.addOrder(Order.asc(orderBy));
			} else {
				criteria.addOrder(Order.desc(orderBy));
			}
		}
	}
	
	    protected <T> T throwException(Status status, String msg) {
	        System.out.println("error: " + msg);
	        log().error(msg);
	        throw new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(msg).build());
	    }
	    
	    protected Category log() {
	        return ThreadCategory.getInstance(getClass());
	    }

}