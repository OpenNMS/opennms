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
