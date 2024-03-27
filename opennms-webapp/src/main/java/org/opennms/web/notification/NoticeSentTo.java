/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
