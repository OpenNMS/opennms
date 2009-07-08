/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.detector.msexchange.response;


public class MSExchangeResponse {
    
    private String m_pop3Response;
    private String m_imapResponse;
    
    public MSExchangeResponse() {}
    
    public boolean contains(String pattern) {
        Boolean result = (getPop3Response()!= null && getPop3Response().indexOf(pattern) > -1) || (getImapResponse() != null &&getImapResponse().indexOf(pattern) > -1); 
        return result;
    }

    public void setPop3Response(String ftpResponse) {
        m_pop3Response = ftpResponse;
    }

    public String getPop3Response() {
        return m_pop3Response;
    }

    public void setImapResponse(String imapResponse) {
        m_imapResponse = imapResponse;
    }

    public String getImapResponse() {
        return m_imapResponse;
    }

}
