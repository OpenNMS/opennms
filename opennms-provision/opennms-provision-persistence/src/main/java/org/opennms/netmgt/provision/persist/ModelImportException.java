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
package org.opennms.netmgt.provision.persist;

public class ModelImportException extends Exception {

    private static final long serialVersionUID = 6520991163434052156L;

    /**
     * <p>Constructor for ModelImportException.</p>
     *
     * @param msg a {@link java.lang.String} object.
     */
    public ModelImportException(String msg) {
        super(msg);
    }
    
    /**
     * <p>Constructor for ModelImportException.</p>
     *
     * @param msg a {@link java.lang.String} object.
     * @param t a {@link java.lang.Throwable} object.
     */
    public ModelImportException(String msg, Throwable t) {
        super(msg, t);
    }

}
