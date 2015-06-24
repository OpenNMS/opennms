/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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
