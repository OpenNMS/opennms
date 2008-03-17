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

public class PropertiesBackedManagedObject implements ManagedObject, MockSnmpMOLoader, Updatable, MOAccess {
    
    private TreeMap<OID, Object> m_vars = null;
    
    private MOScope m_scope = null;
    
    public PropertiesBackedManagedObject(Resource moFile) {
        
        Properties props = PropsMockSnmpMOLoaderImpl.loadProperties(moFile);
        
        m_vars = new TreeMap<OID, Object>();

        for(Entry<Object, Object> e : props.entrySet()) {
            String key = (String)e.getKey();
            Object value = e.getValue();
            m_vars.put(new OID(key), value);
        }
        
        
        m_scope = new DefaultMOScope(
                    m_vars.firstKey(),
                    true,
                    m_vars.lastKey(),
                    true
                );
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

    public List<ManagedObject> loadMOs() {
        return Collections.singletonList((ManagedObject)this);
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
