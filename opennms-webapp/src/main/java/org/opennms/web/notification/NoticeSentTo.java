//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.notification;

import java.util.Date;

/**
 * NoticeSentTo Bean, containing data from the usersNotified table for a single
 * user/notice pair.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NoticeSentTo {
    /**
     * User this notice was sent to
     */
    public String m_userId;

    /**
     * Time the notice was sent to the user in milliseconds.
     */
    public long m_time;

    /**
     * Contact info.
     */
    public String m_contactInfo;

    /**
     * The type of notification mechanism.
     */
    public String m_media;

    /**
     * Default Constructor
     */
    public NoticeSentTo() {
    }

    /**
     * <p>setUserId</p>
     *
     * @param userid a {@link java.lang.String} object.
     */
    public void setUserId(String userid) {
        m_userId = userid;
    }

    /**
     * <p>getUserId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUserId() {
        return m_userId;
    }

    /**
     * <p>setTime</p>
     *
     * @param time a long.
     */
    public void setTime(long time) // no see!
    {
        m_time = time;
    }

    /**
     * <p>getTime</p>
     *
     * @return a java$util$Date object.
     */
    public Date getTime() {
        return new Date(m_time);
    }

    /**
     * <p>setMedia</p>
     *
     * @param media a {@link java.lang.String} object.
     */
    public void setMedia(String media) {
        m_media = media;
    }

    /**
     * <p>getMedia</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMedia() {
        return m_media;
    }

    /**
     * <p>setContactInfo</p>
     *
     * @param contact a {@link java.lang.String} object.
     */
    public void setContactInfo(String contact) {
        m_contactInfo = contact;
    }

    /**
     * <p>getContactInfo</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getContactInfo() {
        return m_contactInfo;
    }
}
