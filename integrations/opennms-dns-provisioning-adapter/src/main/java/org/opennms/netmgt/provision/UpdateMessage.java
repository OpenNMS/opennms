/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision;

import java.util.Set;

/**
 * <p>UpdateMessage class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
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
    
    
    /**
     * <p>getHeader</p>
     *
     * @return a {@link org.opennms.netmgt.provision.UpdateMessage.UpdateHeader} object.
     */
    public UpdateHeader getHeader() {
        return m_header;
    }
    /**
     * <p>setHeader</p>
     *
     * @param header a {@link org.opennms.netmgt.provision.UpdateMessage.UpdateHeader} object.
     */
    public void setHeader(UpdateHeader header) {
        m_header = header;
    }
    /**
     * <p>getZone</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getZone() {
        return m_zone;
    }
    /**
     * <p>setZone</p>
     *
     * @param zone a {@link java.lang.String} object.
     */
    public void setZone(String zone) {
        m_zone = zone;
    }
    /**
     * <p>getPrereq</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPrereq() {
        return m_prereq;
    }
    /**
     * <p>setPrereq</p>
     *
     * @param prereq a {@link java.lang.String} object.
     */
    public void setPrereq(String prereq) {
        m_prereq = prereq;
    }
    /**
     * <p>getUpdate</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<ResourceRecord> getUpdate() {
        return m_update;
    }
    /**
     * <p>setUpdate</p>
     *
     * @param update a {@link java.util.Set} object.
     */
    public void setUpdate(Set<ResourceRecord> update) {
        m_update = update;
    }
    /**
     * <p>getAdditionalData</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAdditionalData() {
        return m_additionalData;
    }
    /**
     * <p>setAdditionalData</p>
     *
     * @param additionalData a {@link java.lang.String} object.
     */
    public void setAdditionalData(String additionalData) {
        m_additionalData = additionalData;
    }
    
    
    
    
    private static class UpdateHeader {
        
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
        
        @SuppressWarnings("unused")
        private int m_id;
        @SuppressWarnings("unused")
        private int m_flag;
        @SuppressWarnings("unused")
        private int m_optCode;
        @SuppressWarnings("unused")
        private static final int Z = 0;
        @SuppressWarnings("unused")
        private int m_rCode;
        @SuppressWarnings("unused")
        private int m_zOCount;
        @SuppressWarnings("unused")
        private int m_upCount;
        @SuppressWarnings("unused")
        private int m_adCount;
        
    }

}
