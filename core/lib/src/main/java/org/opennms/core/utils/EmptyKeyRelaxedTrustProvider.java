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
/**
 * @author <a mailto:seth@opennms.org>Seth Leger</a>
 */
package org.opennms.core.utils;

import java.security.Provider;


public final class EmptyKeyRelaxedTrustProvider extends Provider {
    private static final long serialVersionUID = -543349021655585769L;

    public EmptyKeyRelaxedTrustProvider() {
        super(EmptyKeyRelaxedTrustSSLContext.ALGORITHM + "Provider", 1.0, null);
        put(
            "SSLContext." + EmptyKeyRelaxedTrustSSLContext.ALGORITHM,
            EmptyKeyRelaxedTrustSSLContext.class.getName()
        );
    }
}