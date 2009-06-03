/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created August 28, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.mock.snmp;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.snmp4j.agent.DefaultMOScope;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.MOScope;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.request.SubRequest;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.springframework.core.io.Resource;

/**
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
public class PropertiesBackedManagedObject implements ManagedObject, MockSnmpMOLoader, Updatable, MOAccess {
    
    
    private TreeMap<OID, Object> m_vars = null;
    
    private MOScope m_scope = null;
    
    public List<ManagedObject> loadMOs(Resource moFile) {
        Properties props = PropsMockSnmpMOLoaderImpl.loadProperties(moFile);

        m_vars = new TreeMap<OID, Object>();

        for(Entry<Object, Object> e : props.entrySet()) {
            String key = (String)e.getKey();
            Object value = e.getValue();
            m_vars.put(new OID(key), value);
        }


        m_scope = new DefaultMOScope(m_vars.firstKey(),
                                     true,
                                     m_vars.lastKey(),
                                     true
        );
        
        return Collections.singletonList((ManagedObject)this);
    }
    
    public void cleanup(SubRequest request) {
        throw new UnsupportedOperationException("this object read only");
    }

    public void commit(SubRequest request) {
        throw new UnsupportedOperationException("this object read only");
    }

    public OID find(MOScope range) {
        if (!m_scope.isOverlapping(range)) {
            return null;
        }
        
        OID first = range.getLowerBound();
        
        if (range.isLowerIncluded()) {
            first = first.successor();
        }

        SortedMap<OID, Object> tail = m_vars.tailMap(first);
        if (tail.isEmpty()) {
            return null;
        }
        return tail.firstKey(); // skip the leading '.'
    }
    
    public OID findNextOid(OID given) {
        
        OID next = given.successor();
        
        SortedMap<OID, Object> tail = m_vars.tailMap(next);
        if (tail.isEmpty()) {
            return null;
        }
        return tail.firstKey();
    }
    
    private Variable findValueForOID(OID oid) {
        Object val = m_vars.get(oid);
        if (val == null) {
            return null;
        } else if (val instanceof Variable) {
            return (Variable)val;
        }
        return PropsMockSnmpMOLoaderImpl.getVariableFromValueString(oid.toString(), (String)val);
    }

    public void get(SubRequest request) {
        getVariable(request, request.getVariableBinding().getOid());
    }

    private void getVariable(SubRequest request, OID oid) {
        Variable value = findValueForOID(oid);
        VariableBinding vb = request.getVariableBinding();
        vb.setOid(oid);
        vb.setVariable(value == null ? Null.noSuchObject : value);
        request.completed();
    }

    public MOScope getScope() {
        return m_scope;
    }

    public boolean next(SubRequest request) {
        OID nextOid = findNextOid(request.getVariableBinding().getOid());
        if (nextOid == null) {
            return false;
        }
        getVariable(request, nextOid);
        return true;
    }

    public void prepare(SubRequest request) {
        throw new UnsupportedOperationException("this object read only");
    }

    public void undo(SubRequest request) {
        throw new UnsupportedOperationException("this object read only");
    }

    public void updateValue(OID oid, Variable value) {
        m_vars.put(oid, value);
    }

    public boolean isAccessibleForCreate() {
        return false;
    }

    public boolean isAccessibleForNotify() {
        return false;
    }

    public boolean isAccessibleForRead() {
        return true;
    }

    public boolean isAccessibleForWrite() {
        return false;
    }

}
