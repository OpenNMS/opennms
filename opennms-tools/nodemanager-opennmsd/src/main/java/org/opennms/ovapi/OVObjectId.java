package org.opennms.ovapi;

import org.opennms.ovapi.OVsnmp.ObjectID;
import org.opennms.ovapi.OVsnmp.ObjectIDByReference;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class OVObjectId {
    
    private ObjectID m_oid;
    private int m_len;
    
    public OVObjectId() {
        m_oid = new ObjectID();
        m_len = -1;
    }
    
    public OVObjectId(String objectId) {
        ObjectIDByReference pOid = new ObjectIDByReference();
        IntByReference pLen = new IntByReference();
        
        ovsnmp().OVsnmpOidFromStr(pOid, pLen, objectId);
        
        m_oid = pOid.getValue();
        m_len = pLen.getValue();
    }
    
    public OVObjectId(ObjectID oid, int len) {
        m_oid = oid;
        m_len = len;
    }
    
    public boolean isNull() {
        return m_oid.getPointer() == Pointer.NULL;
    }
    
    public String toString() {
        if (isNull()) {
            return "";
        }
        return ovsnmp().OVsnmpOidToStr(m_oid, m_len, OVsnmp.s_memHolder.getMemory(), OVsnmp.MEM_SIZE);
    }
    
    public OVObjectId copy() {
        if (isNull()) {
            return new OVObjectId();
        }
        
        ObjectID oid = ovsnmp().OVsnmpOidCopy(m_oid, m_len);
        
        return new OVObjectId(oid, m_len);
        
    }
    
    public void append(OVObjectId oid) {
        if (oid.isNull()) {
            return;
        }

        ObjectIDByReference pOid = new ObjectIDByReference(m_oid);
        IntByReference pLen = new IntByReference(m_len);
        
        ovsnmp().OVsnmpOidAppend(pOid, pLen, oid.m_oid, oid.m_len);
        
        m_oid = pOid.getValue();
        m_len = pLen.getValue();
        
        
    }
    
    public OVObjectId concat(OVObjectId oid2) {
        ObjectIDByReference pOid = new ObjectIDByReference();
        IntByReference pLen = new IntByReference();
        
        ovsnmp().OVsnmpOidConcat(pOid, pLen, m_oid, m_len, oid2.m_oid, oid2.m_len);
        
        return new OVObjectId(pOid.getValue(), pLen.getValue());

    }
    
    public void free() {
        if (isNull()) {
            ovsnmp().OVsnmpFree(m_oid.getPointer());
            m_oid.setPointer(Pointer.NULL);
            m_len = -1;
        }
    }
    
    protected void finalize() throws Throwable {
        free();
    }
    
    OVsnmp ovsnmp() {
        return OVsnmp.INSTANCE;
    }
    
}
