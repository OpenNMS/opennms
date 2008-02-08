/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.ovapi;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.Union;
import com.sun.jna.ptr.ByReference;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;

public interface OVsnmp extends Library {
    
    public static final OVsnmp INSTANCE = (OVsnmp)Native.loadLibrary("ovsnmp", OVsnmp.class);

    public static final int MEM_SIZE = 512;
    
    public static class MemHolder extends ThreadLocal {
        protected synchronized Object initialValue() {
            return new Memory(MEM_SIZE);
        }
        
        public Memory getMemory() {
            return (Memory)get();
        }
    }
    
    public static final MemHolder s_memHolder = new MemHolder();

    // void  * OVsnmpMalloc (size_t size);
    Pointer OVsnmpMalloc(int size);
    
    // void * OVsnmpCalloc  (size_t nelem, size_t size);
    Pointer OVsnmpCalloc(int nelem, int size);
    
    // void * OVsnmpRealloc  (void *ptr, size_t size);
    Pointer OVsnmpRealloc(Pointer ptr, int size);
   
    // void OVsnmpFree (void  *ptr);
    void OVsnmpFree(Pointer ptr);
    

    public static class ObjectID extends PointerType {
        public ObjectID() { super(); }
        public ObjectID(Pointer pointer) {
            super(pointer);
        }
        
        public String toString(int len) {
            if (this.getPointer() == Pointer.NULL) {
                return null;
            }
            return OVsnmp.INSTANCE.OVsnmpOidToStr(this, len, s_memHolder.getMemory(), MEM_SIZE);
        }
        
        public ObjectID copy(int len) {
            if (this.getPointer() == Pointer.NULL) {
                return new ObjectID(Pointer.NULL);
            }
            return OVsnmp.INSTANCE.OVsnmpOidCopy(this, len);
        }
        
        public void free() {
            if (this.getPointer() != Pointer.NULL) {
                OVsnmp.INSTANCE.OVsnmpFree(this.getPointer());
                this.setPointer(Pointer.NULL);
            }
        }
        
        public int fromString(String oidStr) {

            ObjectIDByReference pOid = new ObjectIDByReference();
            IntByReference pLen = new IntByReference();
            
            OVsnmp.INSTANCE.OVsnmpOidFromStr(pOid, pLen, oidStr);
            
            this.setPointer(pOid.getPointer().getPointer(0));
            
            return pLen.getValue();
            
        }
        
    }

    public static class ObjectIDByReference extends ByReference {
        public ObjectIDByReference() { super(Pointer.SIZE); }
        public ObjectIDByReference(ObjectID oid) { 
            super(Pointer.SIZE);
            setValue(oid);
        }
        public ObjectID getValue() {
            return new ObjectID(getPointer().getPointer(0));
        }
        public void setValue(ObjectID val) {
            getPointer().setPointer(0, val.getPointer());
        }
    }
    
    /* ObjectID manipulation functions */

    // ObjectID  * OVsnmpOidAppend (ObjectID **p_oid1, u_int *p_oidlen1,
    //        const ObjectID *oid2, u_int oidlen2);
    ObjectID OVsnmpOidAppend(ObjectIDByReference pOid1, IntByReference oOidlen1,
                            ObjectID oid2, int oidlen2);

    // OVsnmpOidConcat
    ObjectID OVsnmpOidConcat(ObjectIDByReference result, IntByReference resultLen,
            ObjectID oid1, int len1,
            ObjectID oid2, int len2);
    
    // OVsnmpOidCompare
    int OVsnmpOidCompare(ObjectID oid1, int len1, ObjectID oid2, int len2, int comparelen);
    
    // ObjectID  * OVsnmpOidCopy (const ObjectID *oid, u_int oidlen);
    ObjectID OVsnmpOidCopy(ObjectID oid, int len);

    // ObjectID  * OVsnmpOidFromStr (ObjectID **p_oid, u_int *p_oidlen,
    //         const char *oidstr);
    ObjectID OVsnmpOidFromStr(ObjectIDByReference pOid, IntByReference pLen, String oidstr);
    
    // OVsnmpOidToStr
    // char  * OVsnmpOidToStr (ObjectID *oid, u_int oidlen,
    //         char  *buf, u_int buflen);
    String OVsnmpOidToStr(ObjectID oid, int oidlen, Memory buf, int buflen);
    
    
    public static class OVuint64 extends Structure {
        
        public static class ByReference extends OVuint64 implements Structure.ByReference { }

        public NativeLong high;
        public NativeLong low;
        
        public OVuint64() {
            this(0);
        }
        
        public OVuint64(int val) {
            setValue(val);
        }
        
        public OVuint64(OVuint64 val) {
            setValue(val);
        }
        
        public OVuint64(String val) {
            setValue(val);
        }

        public OVuint64(long val) {
            setValue(val);
        }

        private void setValue(int val) {
            OVsnmp.INSTANCE.OVuint64AssignUInt32(this, new NativeLong(val));
        }
        
        private void setValue(OVuint64 val) {
            OVsnmp.INSTANCE.OVuint64Assign(this, val);
        }
        
        private void setValue(String str) {
            OVsnmp.INSTANCE.OVuint64FromStr(this, str, Pointer.NULL, 10);
        }
        
        private void setValue(long val) {
            int lowerBits = (int)(val & 0x00000000ffffffffL);
            int upperBits = (int)((val >>> 32) & 0x00000000ffffffffL);
            OVuint64 acc = new OVuint64(upperBits);
            acc = acc.shiftLeft(32);
            acc = acc.add(new OVuint64(lowerBits));
            setValue(acc);
        }
        
        public OVuint64 shiftLeft(int cnt) {
            OVuint64 result = new OVuint64();
            OVsnmp.INSTANCE.OVuint64Shift(this, result, (byte)'l', cnt);
            return result;
        }
        
        public OVuint64 shiftRight(int cnt) {
            OVuint64 result = new OVuint64();
            OVsnmp.INSTANCE.OVuint64Shift(this, result, (byte)'r', cnt);
            return result;
        }
        
        public OVuint64 add(OVuint64 val) {
            OVuint64 result = new OVuint64();
            OVsnmp.INSTANCE.OVuint64Add(this, val, result);
            return result;
        }
        
        public OVuint64 subtract(OVuint64 val) {
            OVuint64 result = new OVuint64();
            OVsnmp.INSTANCE.OVuint64Subtract(this, val, result);
            return result;
        }
        
        public String toString() {
            Memory mem = OVsnmp.s_memHolder.getMemory();
            OVsnmp.INSTANCE.OVuint64ToStr(mem, this, 10, "#");
            return new String(mem.getString(0));
        }
        
        public String toHexString() {
            Memory mem = OVsnmp.s_memHolder.getMemory();
            OVsnmp.INSTANCE.OVuint64ToStr(mem, this, 16, "#");
            return new String(mem.getString(0));
        }
        
        public String toNativeValue() {
            return "[h:"+Integer.toHexString(high.intValue())+", l:"+Integer.toHexString(low.intValue())+"]";
        }
        
        public long longValue() {
            // XXX This is a cheat we are using the representation even though
            // the OV docs say that its platform dependent.  
            long upper = (high.intValue() & 0xffffffffL);
            long lower = (low.intValue() & 0xffffffffL);
            long result = (upper << 32) | lower;
            
            return result;
        }

        public boolean equals(Object o) {
            if (o instanceof OVuint64) {
                return (OVsnmp.INSTANCE.OVuint64Cmp(this, (OVuint64)o) == 0);
            }
            return false;
        }

        public int hashCode() {
            return high.intValue() * 31 + low.intValue();
        }
        

    }
    
    
    // void OVuint64Assign( OVuint64 *val1, const OVuint64 *val2 );
    void OVuint64Assign( OVuint64 val1, OVuint64 val2);
    
    // void OVuint64AssignUInt32( OVuint64 *val1, const unsigned long val2 );
    void OVuint64AssignUInt32( OVuint64 val1, NativeLong val2);
    
    // int  OVuint64FromStr( OVuint64 *val, const char *s,
    //          char  **sprt, int base );
    int OVuint64FromStr( OVuint64 val, String s, Pointer sprt, int base);
    
    // int OVuint64ToStr( char *s,  const OVuint64 *val,
    //         int base, const char *flags);
    int OVuint64ToStr( Pointer s, OVuint64 val, int base, String flags);
    
    // int  OVuint64Shift( const OVuint64 *val,
    //         OVuint64 *result,  char dir, int cnt );
    int OVuint64Shift(OVuint64 val, OVuint64 result, byte dir, int cnt);

    // int  OVuint64Add( const OVuint64 *a64,
    //         const OVuint64 *b64,  OVuint64 *c64 );
    int OVuint64Add(OVuint64 a64, OVuint64 b64, OVuint64 c64);

    // int OVuint64AddUInt32( const  OVuint64 *a64,
    //         const unsigned long b32, OVuint64 *c64  );
    
    // int OVuint64Cmp( const  OVuint64 *a64,
    //         const OVuint64 *b64 );
    int OVuint64Cmp(OVuint64 a64, OVuint64 b64);
    
    // int  OVuint64CmpUInt32( const OVuint64 *a64,
    //         const unsigned  long b32 );

    // int OVuint64Divide( const OVuint64  *a64,
    //         const OVuint64 *b64, OVuint64 *c64 );

    // int  OVuint64DivideUInt32( const OVuint64 *a64,
    //         const unsigned  long b32, OVuint64 *c64 );

    // int OVuint64Mutliply(  const OVuint64 *a64,
    //         const OVuint64 *b64, OVuint64  *c64 );

    // int OVuint64MultiplyUInt32( const OVuint64  *a64,
    //         const unsigned long b32, OVuint64 *c64 );

    // int OVuint64Subtract( const  OVuint64 *a64,
    //         const OVuint64 *b64, OVuint64 *c64  );
    int OVuint64Subtract(OVuint64 a64, OVuint64 b64, OVuint64 c64);

    // int OVuint64SubtractUInt32( const OVuint64  *a64,
    //         const unsigned long  b32, OVuint64 *c64:
    
    
    public static class OVsnmpVal extends Union {
        public static class ByReference extends OVsnmpVal implements Structure.ByReference { }
        public IntByReference integer;
        public ByteByReference string;
        public ObjectID objid;
        public OVuint64.ByReference unsigned64;
        
        
    }
    
    public static class OVsnmpVarBind extends Structure {
        public static class ByReference extends OVsnmpVarBind implements Structure.ByReference { }
        
        public OVsnmpVarBind.ByReference next_variable;
        
        public ObjectID oid;
        public int oid_length;
        public byte type;
        public OVsnmpVal val;
        public int val_len;
        
    }
    
    // this is a union but other forms are deprecated
    public static class in_addr extends Structure {
        public int  s_addr;
    }
    
    public static class SockAddr extends Structure {
        public short           sin_family;
        public short           sin_port;
        public in_addr         sin_addr;
        public byte[]          sin_zero = new byte[8];
        
        public String toString() {
            return getIpAddress(sin_addr.s_addr)+"/"+sin_port;
        }
        
        public String getIpAddress() {
            return getIpAddress(sin_addr.s_addr);
        }
            
        public static String getIpAddress(int addr) {
            int ip = addr;

            int[] octets = new int[4];
            for(int i = 0; i < 4; i++) {
                octets[i] = ip & 0xff;
                ip = ip >> 8;
            }

            StringBuffer buf = new StringBuffer(16);
            
            buf.append(octets[3]).append('.');
            buf.append(octets[2]).append('.');
            buf.append(octets[1]).append('.');
            buf.append(octets[0]);
            
            return buf.toString();
            
        }
    }
    
    public static class OVsnmpPdu extends Structure {
        public static final int ASN_CONTEXT     = 0x80;
        public static final int ASN_CONSTRUCTOR = 0x20;
        public static final int GET_REQ_MSG     = (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x00);
        public static final int GETNEXT_REQ_MSG = (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x01);
        public static final int GET_RSP_MSG     = (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x02);
        public static final int SET_REQ_MSG     = (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x03);
        public static final int V1TRAP_REQ_MSG  = (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x04);
        public static final int GETBULK_REQ_MSG = (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x05);
        public static final int INFORM_REQ_MSG  = (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x06);
        public static final int V2TRAP_REQ_MSG  = (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x07);
        public static final int REPORT_MSG      = (ASN_CONTEXT | ASN_CONSTRUCTOR | 0x08);
        
        
        public SockAddr address;
        public int command;
        public int request_id;
        public int error_status;
        public int error_index;
        
        // Trap Information for V1 traps
        public ObjectID enterprise;
        public int enterprise_length;
        public int agent_addr;
        
        public int generic_type;
        public int specific_type;
        public int time;
        
        public OVsnmpVarBind.ByReference variables;
        //public Pointer variables;
        
        
        public ByteByReference community;
        public int community_len;
        
        public Pointer  internal;
        
        public int protocol_version;
        
        // V2 Trap & Inform Fields
        public ObjectID notify_oid;
        public int notify_oid_length;
        
        public String getCommunity() {
            if (community != null) {
                return new String(community.getPointer().getByteArray(0, community_len));
            }
            return null;
        }
        
        
        public static OVsnmpPdu create(int type) {
            return OVsnmp.INSTANCE.OVsnmpCreatePdu(type);
        }
        
        public static OVsnmpPdu createGet() {
            return create(GET_REQ_MSG);
        }
        
        public void addNullVarBind(ObjectID oid, int len) {
            OVsnmp.INSTANCE.OVsnmpAddNullVarBind(this, oid, len);
        }
        
        public void free() {
            OVsnmp.INSTANCE.OVsnmpFree(this.getPointer());
        }
        
        public String toString() {
            StringBuffer buf = new StringBuffer(128);
            
            buf.append("OVsnmpPdu[");
            buf.append("address = "+address).append(", ");
            buf.append("command = "+pduTypeToString(command)).append(", ");
            buf.append("error_stats = "+error_status).append(", ");
            buf.append("error_index = "+error_index).append(", ");
            buf.append("enterprise = " + OVObjectId.get(enterprise, enterprise_length, false)).append(", ");
            buf.append("agent_addr = " + SockAddr.getIpAddress(agent_addr)).append(", ");
            buf.append("generic_type = " + generic_type).append(", ");
            buf.append("specific_type = " + specific_type).append(", ");
            buf.append("time = " + time).append(", ");
            
            buf.append("variables =  "+variables).append(", ");
            
            buf.append("community_len = " + community_len).append(", ");
            buf.append("community = " + getCommunity()).append(", ");
          
            buf.append("protocal_version = " + protocol_version).append(", ");
            
            buf.append("notify_oid = " + OVObjectId.get(notify_oid, notify_oid_length, false));
            
            
            buf.append("]");
            
            return buf.toString();
        }
        
        public static String pduTypeToString(int type) {
            switch(type) {
            case GET_REQ_MSG:
                return "GET_REQ_MSG";
            case GETNEXT_REQ_MSG:
                return "GETNEXT_REQ_MSG";
            case GET_RSP_MSG:
                return "GET_RSP_MSG";
            case SET_REQ_MSG:
                return "SET_REQ_MSG";
            case V1TRAP_REQ_MSG:
                return "V1TRAP_REQ_MSG";
            case GETBULK_REQ_MSG:
                return "GETBULK_REQ_MSG";
            case INFORM_REQ_MSG:
                return "INFORM_REQ_MSG";
            case V2TRAP_REQ_MSG:
                return "V2TRAP_REQ_MSG";
            case REPORT_MSG:
                return "REPORT_MSG";
            default:    
                return "UNKNOWN";   
            }
        }
        
    }
    
    // OVsnmpPdu  * OVsnmpCreatePdu (int type);
    OVsnmpPdu OVsnmpCreatePdu(int type);

    // OVsnmpVarBind  * OVsnmpAddNullVarBind (OVsnmpPdu * pdu,
    //         ObjectID *  oid, int oid_len);
    OVsnmpVarBind OVsnmpAddNullVarBind(OVsnmpPdu pdu, ObjectID oid, int oid_len);

    // OVsnmpVarBind * OVsnmpAddTypedVarBind  ( OVsnmpPdu * pdu,
    //          ObjectID * oid, int oid_len, u_char  type,
    //          OVsnmpVal * val, int val_len);
    OVsnmpVarBind OVsnmpAddTypedVarBind(OVsnmpPdu pdu,
            ObjectID oid, int oid_len, byte type, OVsnmpVal val, int val_len);

    public static interface OVsnmpCallback extends Callback {
        public void callback(int command, OVsnmpSession session, OVsnmpPdu pdu, Pointer callbackData);
    }
    
    public static class OVsnmpSession extends Structure {
        public static final String SNMP_USE_DEFAULT_COMMUNITY = null;
        public static final int SNMP_USE_DEFAULT_RETRIES = -1;
        public static final int SNMP_USE_DEFAULT_TIMEOUT = -1;
        public static final int SNMP_USE_DEFAULT_REMOTE_PORT = 0xffff;
        public static final int SNMP_USE_DEFAULT_LOCAL_PORT = 0xffff;

        public ByteByReference community;
        public int community_len;
        
        public ByteByReference setCommunity;
        public int setCommunity_len;
        
        public int sock_fd;
        
        public short session_flags;
        
        OVsnmpCallback callback;
        
        Pointer callback_data;
        
        int protocol_version;
        
        public static OVsnmpSession open(String peername) {
            return open(peername, SNMP_USE_DEFAULT_REMOTE_PORT); 
        }
        
        public static OVsnmpSession open(String peername, int port) {
            return OVsnmp.INSTANCE.OVsnmpOpen(
                    SNMP_USE_DEFAULT_COMMUNITY, peername, 
                    SNMP_USE_DEFAULT_RETRIES, SNMP_USE_DEFAULT_TIMEOUT, 
                    SNMP_USE_DEFAULT_LOCAL_PORT, port, 
                    null, Pointer.NULL);
        }
        
        public void close() {
            OVsnmp.INSTANCE.OVsnmpClose(this);
        }
        
        public String getCommunity() {
            if (community != null) {
                return new String(community.getPointer().getByteArray(0, community_len));
            }
            return null;
        }

        public String getSetCommunity() {
            if (setCommunity != null) {
                return new String(setCommunity.getPointer().getByteArray(0, setCommunity_len));
            }
            return null;
        }
        
        public String toString() {
            
            StringBuffer buf = new StringBuffer(128);
            
            buf.append("OVsession[");
            buf.append("community_len = ").append(community_len).append(", ");
            buf.append("community = ").append(getCommunity()).append(", ");
            buf.append("setCommunity_len = ").append(setCommunity_len).append(", ");
            buf.append("setCommunity = ").append(getSetCommunity()).append(", ");
            buf.append("sock_fd = ").append(sock_fd).append(", ");
            buf.append("session_flags = ").append(session_flags).append(", ");
            buf.append("callback = ").append(callback).append(", ");
            buf.append("calback_data = ").append(callback_data).append(", ");
            buf.append("protocol_version = ").append(protocol_version);
            buf.append("]");
            
            return buf.toString();
            
        }
        
    }

    // OVsnmpSession  * OVsnmpOpen ( char * community,
    //        char * peername,  int retries, int timeout,
    //        int local_port, int remote_port,
    //         void (*callback)(), void* callback_data);
    OVsnmpSession OVsnmpOpen(String community, String peername, 
            int retries, int timeout, int local_port, int remote_port, 
            OVsnmpCallback callback, Pointer callback_data);
    
    //    OVsnmpSession  * OVsnmpEventOpen ( const char *peername,
    //            const char  *entityName, OVsnmpCallback callback,
    //            void *callback_data,  const char *filter);
    OVsnmpSession OVsnmpEventOpen(String peername, String entityName,
            OVsnmpCallback callback, Pointer callback_data, String filter);

    // int  OVsnmpClose(OVsnmpSession *session);
    int OVsnmpClose(OVsnmpSession session);


    
    
    // OVsnmpGetRetryInfo
    
    
    // OVsnmpRecv and OVsnmpRead
    
    
    // OVsnmpClose
    
    
    
  

}
