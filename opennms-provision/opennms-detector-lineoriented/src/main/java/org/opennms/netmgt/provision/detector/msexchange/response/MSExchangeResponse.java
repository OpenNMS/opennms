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

package org.opennms.netmgt.provision.detector.msexchange.response;


/**
 * <p>MSExchangeResponse class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MSExchangeResponse {
    
    private String m_pop3Response;
    private String m_imapResponse;
    
    /**
     * <p>Constructor for MSExchangeResponse.</p>
     */
    public MSExchangeResponse() {}
    
    /**
     * <p>contains</p>
     *
     * @param pattern a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean contains(final String pattern) {
        return (getPop3Response()!= null && getPop3Response().indexOf(pattern) > -1) || (getImapResponse() != null &&getImapResponse().indexOf(pattern) > -1);
    }

    /**
     * <p>setPop3Response</p>
     *
     * @param ftpResponse a {@link java.lang.String} object.
     */
    public void setPop3Response(final String ftpResponse) {
        m_pop3Response = ftpResponse;
    }

    /**
     * <p>getPop3Response</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPop3Response() {
        return m_pop3Response;
    }

    /**
     * <p>setImapResponse</p>
     *
     * @param imapResponse a {@link java.lang.String} object.
     */
    public void setImapResponse(final String imapResponse) {
        m_imapResponse = imapResponse;
    }

    /**
     * <p>getImapResponse</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getImapResponse() {
        return m_imapResponse;
    }

}
