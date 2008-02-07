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

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.Union;
import com.sun.jna.ptr.ByReference;
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

    
    
    

    
    
//    OVsnmpSession  * OVsnmpEventOpen ( const char *peername,
//            const char  *entityName, OVsnmpCallback callback,
//            void *callback_data,  const char *filter);
    
    // OVsnmpGetRetryInfo
    
    
    // OVsnmpRecv and OVsnmpRead
    
    
    // OVsnmpClose
    
    public static class OVsnmpVal extends Union {
        
    }
    
    public static class OVsnmpPdu extends Structure {
        
        
    }
    
    
  

}
