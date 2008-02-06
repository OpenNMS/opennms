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
    }
    
    // void OVuint64Assign( OVuint64 *val1, const OVuint64 *val2 );
    void OVuint64Assign( OVuint64 val1, OVuint64 val2);

    
    // void OVuint64AssignUInt32( OVuint64 *val1, const unsigned long val2 );
    void OVuint64AssingUInt32( OVuint64 val1, NativeLong val2);
    
    // int  OVuint64FromStr( OVuint64 *val, const char *s,
    //          char  **sprt, int base );
    int OVuint64FromStr( OVuint64 val, String s, Pointer sprt, int base);
    
    // int OVuint64ToStr( char *s,  const OVuint64 *val,
    //         int base, const char *flags);
    int OVuint64ToStr( byte[] s, OVuint64 val, int base, String flags);

    
    
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
