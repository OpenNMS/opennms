/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 23, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.provision;

import java.util.Set;

public class UpdateMessage {

    /*
    +---------------------+
    |        Header       |
    +---------------------+
    |         Zone        | specifies the zone to be updated
    +---------------------+
    |     Prerequisite    | RRs or RRsets which must (not) preexist
    +---------------------+
    |        Update       | RRs or RRsets to be added or deleted
    +---------------------+
    |   Additional Data   | additional data
    +---------------------+
    */
    
    private UpdateHeader m_header;
    private String m_zone;
    private String m_prereq;
    private Set<ResourceRecord> m_update;
    private String m_additionalData;
    
    
    public UpdateHeader getHeader() {
        return m_header;
    }
    public void setHeader(UpdateHeader header) {
        m_header = header;
    }
    public String getZone() {
        return m_zone;
    }
    public void setZone(String zone) {
        m_zone = zone;
    }
    public String getPrereq() {
        return m_prereq;
    }
    public void setPrereq(String prereq) {
        m_prereq = prereq;
    }
    public Set<ResourceRecord> getUpdate() {
        return m_update;
    }
    public void setUpdate(Set<ResourceRecord> update) {
        m_update = update;
    }
    public String getAdditionalData() {
        return m_additionalData;
    }
    public void setAdditionalData(String additionalData) {
        m_additionalData = additionalData;
    }
    
    
    
    
    private class UpdateHeader {
        
        /*
                                      1  1  1  1  1  1
        0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
      +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
      |                      ID                       |
      +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
      |QR|   Opcode  |          Z         |   RCODE   |
      +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
      |                    ZOCOUNT                    |
      +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
      |                    PRCOUNT                    |
      +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
      |                    UPCOUNT                    |
      +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
      |                    ADCOUNT                    |
      +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
      
      These fields are used as follows:

   ID      A 16-bit identifier assigned by the entity that generates any
           kind of request.  This identifier is copied in the
           corresponding reply and can be used by the requestor to match
           replies to outstanding requests, or by the server to detect
           duplicated requests from some requestor.

   QR      A one bit field that specifies whether this message is a
           request (0), or a response (1).

   Opcode  A four bit field that specifies the kind of request in this
           message.  This value is set by the originator of a request
           and copied into the response.  The Opcode value that
           identifies an UPDATE message is five (5).

   Z       Reserved for future use.  Should be zero (0) in all requests
           and responses.  A non-zero Z field should be ignored by
           implementations of this specification.

   RCODE   Response code - this four bit field is undefined in requests
           and set in responses.  The values and meanings of this field
           within responses are as follows:

              Mneumonic   Value   Description
              ------------------------------------------------------------
              NOERROR     0       No error condition.
              FORMERR     1       The name server was unable to interpret
                                  the request due to a format error.
              SERVFAIL    2       The name server encountered an internal
                                  failure while processing this request,
                                  for example an operating system error
                                  or a forwarding timeout.
              NXDOMAIN    3       Some name that ought to exist,
                                  does not exist.
              NOTIMP      4       The name server does not support
                                  the specified Opcode.
              REFUSED     5       The name server refuses to perform the
                                  specified operation for policy or
                                  security reasons.
              YXDOMAIN    6       Some name that ought not to exist,
                                  does exist.
              YXRRSET     7       Some RRset that ought not to exist,
                                  does exist.
              NXRRSET     8       Some RRset that ought to exist,
                                  does not exist.
              NOTAUTH     9       The server is not authoritative for
                                  the zone named in the Zone Section.
              NOTZONE     10      A name used in the Prerequisite or
                                  Update Section is not within the
                                  zone denoted by the Zone Section.

   ZOCOUNT The number of RRs in the Zone Section.

   PRCOUNT The number of RRs in the Prerequisite Section.

   UPCOUNT The number of RRs in the Update Section.

   ADCOUNT The number of RRs in the Additional Data Section.

         */
        
        private int m_id;
        private int m_flag;
        private int m_optCode;
        private static final int Z = 0;
        private int m_rCode;
        private int m_zOCount;
        private int m_upCount;
        private int m_adCount;
        
    }

}
