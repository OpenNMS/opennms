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

package org.opennms.netmgt.rtc.datablock;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class containing the information for the HTTP POST operations - this gets
 * constructed when each time a subscribe event is received and is basically
 * immutable (except for error count).
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 */
public class HttpPostInfo {
    /**
     * The URL to post to
     */
    private final URL m_url;

    /**
     * The category name related to this URL
     */
    private final String m_catlabel;

    /**
     * The user name
     */
    private final String m_user;

    /**
     * The password
     */
    private final String m_passwd;

    /**
     * Number of post errors
     */
    private int m_errors;

    /**
     * Constructor
     *
     * @param hurl a {@link java.net.URL} object.
     * @param clabel a {@link java.lang.String} object.
     * @param user a {@link java.lang.String} object.
     * @param passwd a {@link java.lang.String} object.
     */
    public HttpPostInfo(final URL hurl, final String clabel, final String user, final String passwd) {
        m_url = hurl;
        m_catlabel = clabel;
        m_user = user;
        m_passwd = passwd;
        m_errors = 0;
    }

    /**
     * Constructor
     *
     * @exception MalformedURLException
     *                thrown if the string url passed is not a valid url
     * @param hurl a {@link java.lang.String} object.
     * @param clabel a {@link java.lang.String} object.
     * @param user a {@link java.lang.String} object.
     * @param passwd a {@link java.lang.String} object.
     * @throws java.net.MalformedURLException if any.
     */
    public HttpPostInfo(final String hurl, final String clabel, final String user, final String passwd) throws MalformedURLException {
        m_url = new URL(hurl);
        m_catlabel = clabel;
        m_user = user;
        m_passwd = passwd;
        m_errors = 0;
    }

    /**
     * Increment errors
     */
    public void incrementErrors() {
        m_errors++;
    }

    /**
     * Clear error count if there were errors earlier
     */
    public void clearErrors() {
        if (m_errors != 0) {
            m_errors = 0;
        }
    }

    /**
     * Return the URL
     *
     * @return the URL
     */
    public URL getURL() {
        return m_url;
    }

    /**
     * Return the URL as a string
     *
     * @return the URL as a string
     */
    public String getURLString() {
        return m_url.toString();
    }

    /**
     * Return the category label
     *
     * @return the category label
     */
    public String getCategory() {
        return m_catlabel;
    }

    /**
     * Return the user
     *
     * @return the user
     */
    public String getUser() {
        return m_user;
    }

    /**
     * Return the passwd
     *
     * @return the passwd
     */
    public String getPassword() {
        return m_passwd;
    }

    /**
     * Return the number of errors
     *
     * @return the number of errors
     */
    public int getErrors() {
        return m_errors;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_catlabel == null) ? 0 : m_catlabel.hashCode());
        result = prime * result + ((m_passwd == null) ? 0 : m_passwd.hashCode());
        result = prime * result + ((m_url == null) ? 0 : m_url.toExternalForm().hashCode());
        result = prime * result + ((m_user == null) ? 0 : m_user.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) return false;
        final HttpPostInfo other = (HttpPostInfo) obj;
        if (m_catlabel == null) {
            if (other.m_catlabel != null) return false;
        } else if (!m_catlabel.equals(other.m_catlabel)) {
            return false;
        }
        if (m_passwd == null) {
            if (other.m_passwd != null) return false;
        } else if (!m_passwd.equals(other.m_passwd)) {
            return false;
        }
        if (m_url == null) {
            if (other.m_url != null) return false;
        } else if (!m_url.toExternalForm().equals(other.m_url.toExternalForm())) {
            return false;
        }
        if (m_user == null) {
            if (other.m_user != null) return false;
        } else if (!m_user.equals(other.m_user)) {
            return false;
        }
        return true;
    }
}
