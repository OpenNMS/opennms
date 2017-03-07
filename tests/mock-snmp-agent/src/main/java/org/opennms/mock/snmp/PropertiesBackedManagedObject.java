/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.mock.snmp;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import org.opennms.mock.snmp.responder.DynamicVariable;
import org.opennms.mock.snmp.responder.SnmpErrorStatusException;
import org.snmp4j.agent.DefaultMOScope;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.MOScope;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.request.RequestStatus;
import org.snmp4j.agent.request.SubRequest;
import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

/**
 * <p>PropertiesBackedManagedObject class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
public class PropertiesBackedManagedObject implements ManagedObject, MockSnmpMOLoader, Updatable, MOAccess {
    
	private static final LogAdapter s_log = LogFactory.getLogger(PropertiesBackedManagedObject.class);
    
    private TreeMap<OID, Object> m_vars = null;
    
    private MOScope m_scope = null;

	private Object m_oldValue;
	
	/*
	 * Cache the dynamic variable types to speed things up.
	 * This removes the need to search the class-path and use reflection at every call.
	 */
	Map<String,DynamicVariable> m_dynamicVariableCache = new HashMap<String,DynamicVariable>();
    
    /** {@inheritDoc} */
        @Override
    public List<ManagedObject> loadMOs(URL moFile) {
    	final Properties props = loadProperties(moFile);
    	
    	// Clear cache on reload
    	m_dynamicVariableCache.clear();

    	if (props.isEmpty()) {
            m_scope = new DefaultMOScope(new OID(".1"), false, new OID(".1"), false);
            return Collections.singletonList((ManagedObject)this);
    	}

    	m_vars = new TreeMap<OID, Object>();

        for(final Entry<Object, Object> e : props.entrySet()) {
            final String key = (String)e.getKey();
            final Object value = e.getValue();
            if (!key.startsWith(".")) {
            	s_log.debug(String.format("key does not start with '.', probably a linewrap issue in snmpwalk: %s = %s", key, value));
            	continue;
            }
            try {
                m_vars.put(new OID(key), value);
            } catch (final Throwable ex) {
                // Catch any malformed OIDs and create a more descriptive error message
            	final IllegalArgumentException nfe = new IllegalArgumentException("Could not load OID value: [" + key + "] [" + value + "]");
                nfe.initCause(ex);
                throw nfe;
            }
        }

        m_scope = new DefaultMOScope(m_vars.firstKey(), true, m_vars.lastKey(), true);
        
        return Collections.singletonList((ManagedObject)this);
    }

	private Properties loadProperties(URL moFile) {
		final Properties moProps = new Properties();
		InputStream inStream = null;
		try {
		    inStream = moFile.openStream();
			moProps.load( inStream );
		} catch (final Exception ex) {
			s_log.error("Unable to read property file " + moFile, ex);
			return null;
		} finally {
			closeQuietly(inStream);
		}
		return moProps;
	}
	
	private void closeQuietly(InputStream in) {
		try {
			if (in != null) {
				in.close();
			}
		} catch (IOException e) {
			// ignore this -- hence the quietly
		}
	}
    
    /** {@inheritDoc} */
        @Override
    public OID find(final MOScope range) {
        if (!m_scope.isOverlapping(range)) {
            return null;
        }
        
        OID first = range.getLowerBound();
        
        if (range.isLowerIncluded()) {
            first = first.successor();
        }

        final SortedMap<OID, Object> tail = m_vars.tailMap(first);
        if (tail.isEmpty()) {
            return null;
        }
        return tail.firstKey(); // skip the leading '.'
    }
    
    /**
     * <p>findNextOid</p>
     *
     * @param given a {@link org.snmp4j.smi.OID} object.
     * @return a {@link org.snmp4j.smi.OID} object.
     */
    public OID findNextOid(final OID given) {
        
    	final OID next = given.successor();
        
        final SortedMap<OID, Object> tail = m_vars.tailMap(next);
        if (tail.isEmpty()) {
            return null;
        }
        return tail.firstKey();
    }
    
    private Variable findValueForOID(final OID oid) throws SnmpErrorStatusException {
    	final Object val = m_vars.get(oid);
        if (val == null) {
            return null;
        } else if (val instanceof Variable) {
            return (Variable)val;
        }
        return getVariableFromValueString(oid.toString(), (String)val);
    }

    /** {@inheritDoc} */
        @Override
    public void get(final SubRequest request) {
        getVariable(request, request.getVariableBinding().getOid());
    }

    private void getVariable(final SubRequest request, final OID oid) {
        try {
            final Variable value = findValueForOID(oid);
            final VariableBinding vb = request.getVariableBinding();
            vb.setOid(oid);
            vb.setVariable(value == null ? Null.noSuchObject : value);
            request.completed();
        } catch (SnmpErrorStatusException e) {
            request.setErrorStatus(e.getErrorStatus());
            request.completed();
        }
    }

    /**
     * <p>getScope</p>
     *
     * @return a {@link org.snmp4j.agent.MOScope} object.
     */
        @Override
    public MOScope getScope() {
        return m_scope;
    }

    /** {@inheritDoc} */
        @Override
    public boolean next(final SubRequest request) {
    	final OID nextOid = findNextOid(request.getVariableBinding().getOid());
        if (nextOid == null) {
            return false;
        }
        getVariable(request, nextOid);
        return true;
    }

    /** {@inheritDoc} */
        @Override
    public void prepare(final SubRequest request) {
    	// store the old value, in case we undo it
    	final VariableBinding vb = request.getVariableBinding();
    	m_oldValue = m_vars.get(vb.getOid());
    	final RequestStatus status = request.getStatus();
		status.setErrorStatus(SnmpConstants.SNMP_ERROR_SUCCESS);
		status.setPhaseComplete(true);
    }

    /** {@inheritDoc} */
        @Override
    public void commit(final SubRequest request) {
    	final VariableBinding vb = request.getVariableBinding();
    	final Variable v = vb.getVariable();
    	m_vars.put(vb.getOid(), v);
    	final RequestStatus status = request.getStatus();
		status.setPhaseComplete(true);
    }

    /** {@inheritDoc} */
        @Override
    public void cleanup(final SubRequest request) {
    	m_oldValue = null;
    	final RequestStatus status = request.getStatus();
		status.setPhaseComplete(true);
    }

    /** {@inheritDoc} */
        @Override
    public void undo(final SubRequest request) {
    	m_vars.put(request.getVariableBinding().getOid(), m_oldValue);
    	m_oldValue = null;
    	final RequestStatus status = request.getStatus();
		status.setErrorStatus(SnmpConstants.SNMP_ERROR_SUCCESS);
		status.setPhaseComplete(true);
    }

    /** {@inheritDoc} */
        @Override
    public void updateValue(final OID oid, final Variable value) {
        m_vars.put(oid, value);
    }

    /**
     * <p>isAccessibleForCreate</p>
     *
     * @return a boolean.
     */
        @Override
    public boolean isAccessibleForCreate() {
        return false;
    }

    /**
     * <p>isAccessibleForNotify</p>
     *
     * @return a boolean.
     */
        @Override
    public boolean isAccessibleForNotify() {
        return false;
    }

    /**
     * <p>isAccessibleForRead</p>
     *
     * @return a boolean.
     */
        @Override
    public boolean isAccessibleForRead() {
        return true;
    }

    /**
     * <p>isAccessibleForWrite</p>
     *
     * @return a boolean.
     */
        @Override
    public boolean isAccessibleForWrite() {
        return false;
    }

	/**
	 * <p>getVariableFromValueString</p>
	 *
	 * @param oidStr a {@link java.lang.String} object.
	 * @param valStr a {@link java.lang.String} object.
	 * @return a {@link org.snmp4j.smi.Variable} object.
	 * @throws SnmpErrorStatusException
	 */
	private Variable getVariableFromValueString(String oidStr, String valStr) throws SnmpErrorStatusException {
	    Variable newVar;
	    
	    if (valStr.startsWith("Wrong Type")) {
	        String newVal = valStr.replaceFirst("Wrong Type \\(should be .*\\): ", "");
	        s_log.error("Bad Mib walk has value: '"+ valStr + "' using '"+newVal+"'");
	        valStr = newVal;
	    }

	
	    if ("\"\"".equals(valStr)) {
	        newVar = new Null();
	    }
	    else {
	        String moTypeStr = valStr.substring(0, valStr.indexOf(':'));
	        String moValStr = valStr.substring(valStr.indexOf(':') + 2);
	
	        try {
	
	            if (moTypeStr.equals("STRING")) {
                   if (moValStr.startsWith("\"") && moValStr.endsWith("\"")) {
                       moValStr = moValStr.substring(1, moValStr.length() - 1);
                   }
	                newVar = new OctetString(moValStr);
	            } else if (moTypeStr.equals("Hex-STRING")) {
	                newVar = OctetString.fromHexString(moValStr.trim().replace(' ', ':'));
	            } else if (moTypeStr.equals("INTEGER")) {
	                newVar = new Integer32(Integer.parseInt(moValStr));
	            } else if (moTypeStr.equals("Gauge32")) {
	                newVar = new Gauge32(Long.parseLong(moValStr));
	            } else if (moTypeStr.equals("Counter32")) {
	                newVar = new Counter32(Long.parseLong(moValStr)); // a 32 bit counter can be > 2 ^ 31, which is > INTEGER_MAX
	            } else if (moTypeStr.equals("Counter64")) {
	                newVar = new Counter64(Long.parseLong(moValStr));
	            } else if (moTypeStr.equals("Timeticks")) {
	                Integer ticksInt = Integer.parseInt( moValStr.substring( moValStr.indexOf('(') + 1, moValStr.indexOf(')') ) );
	                newVar = new TimeTicks(ticksInt);
	            } else if (moTypeStr.equals("OID")) {
	                newVar = new OID(moValStr);
	            } else if (moTypeStr.equals("IpAddress")) {
	                newVar = new IpAddress(moValStr.trim());
	            } else if (moTypeStr.equals("Network Address")) {
	                newVar = OctetString.fromHexString(moValStr.trim());
	            } else if (moTypeStr.equals("Responder")) {
	            	newVar = handleDynamicVariable(oidStr,moValStr);
	            } else {
	                // Punt, assume it's a String
	                //newVar = new OctetString(moValStr);
	                throw new IllegalArgumentException("Unrecognized SNMP Type "+moTypeStr);
	            }
	        } catch (SnmpErrorStatusException e) {
	            throw e;
	        } catch (Throwable t) {
	            throw new UndeclaredThrowableException(t, "Could not convert value '" + moValStr + "' of type '" + moTypeStr + "' to SNMP object for OID " + oidStr);
	        }
	    }
	    return newVar;
	}

    /**
     * <p>handleDynamicVariable</p>
     *
     * @param oidStr a {@link java.lang.String} object.
     * @param typeStr a {@link java.lang.String} object.
     * @return a {@link org.snmp4j.smi.Variable} object.
     * @throws SnmpErrorStatusException
     */
	protected Variable handleDynamicVariable(String oidStr, String typeStr) throws SnmpErrorStatusException {
		DynamicVariable responder = m_dynamicVariableCache.get(oidStr);
		
		if( responder != null ) {
			return responder.getVariableForOID(oidStr);
		} else if( m_dynamicVariableCache.containsKey(oidStr) ) {
			throw new IllegalArgumentException("Already failed to initialize the dynamic variable "+typeStr);
		}
		
		try{
			// Create a new instance of the class in typeStr
			final Class<? extends DynamicVariable> dv = Class.forName(typeStr).asSubclass(DynamicVariable.class);
			if (!DynamicVariable.class.isAssignableFrom(dv)) {
				throw new IllegalArgumentException(typeStr+" must implement the DynamicVariable interface");
			}
			
			// Attempt to instantiate the object using the singleton pattern
			try{
				Method method = dv.getMethod("getInstance", new Class[0]);
				responder = (DynamicVariable)method.invoke(dv, new Object[0]);
			} catch(NoSuchMethodException e) {
				// Do nothing
			}
			
			// If the singleton initialization failed, then create a new instance
			if(responder==null) {
				responder = (DynamicVariable)dv.newInstance();
			}
		} catch(IllegalArgumentException e ) {
			throw e;
		} catch(Throwable t) {
			throw new IllegalArgumentException("Failed to marshall "+typeStr);
		} finally {
			// Cache the result - good or bad
			m_dynamicVariableCache.put(oidStr, responder);
		}
		
		return responder.getVariableForOID(oidStr);
	}
}
