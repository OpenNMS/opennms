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

package org.opennms.core.xml;

import java.io.IOException;

import org.springframework.dao.DataAccessException;

/**
 * This is modeled after the Spring SQLExceptionTrnaslator.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class MarshallingExceptionTranslator {
    /**
     * <p>translate</p>
     *
     * @param task a {@link java.lang.String} object.
     * @param e a {@link java.io.IOException} object.
     * @return a {@link org.springframework.dao.DataAccessException} object.
     */
    public DataAccessException translate(final String task, final IOException e) {
        return new MarshallingResourceFailureException("Failed to perform IO while " + task + ": " + e, e);
    }

    /**
     * <p>translate</p>
     *
     * @param task a {@link java.lang.String} object.
     * @param e an {@link Exception} object.
     * @return a {@link org.springframework.dao.DataAccessException} object.
     */
    public DataAccessException translate(final String task, final Exception e) {
        return new MarshallingResourceFailureException("Failed to marshal/unmarshal XML file while " + task + ": " + e, e);
    }

    /**
     * <p>translate</p>
     *
     * @param task a {@link java.lang.String} object.
     * @param e an {@link Error} object.
     * @return a {@link org.springframework.dao.DataAccessException} object.
     */
    public DataAccessException translate(final String task, final Error e) {
        return new MarshallingResourceFailureException("Failed to marshal/unmarshal XML file while " + task + ": " + e, e);
    }
}
