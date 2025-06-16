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
package org.opennms.reporting.datablock;

/**
 * This class gives a name to the object.
 *
 * @author <A HREF="mailto:jacinta@oculan.com">Jacinta Remedios </A>
 * @author <A HREF="http://www.oculan.com">oculan.com </A>
 */
public class StandardNamedObject extends Object {
    /**
     * The name of the object
     */
    private String m_name;

    /**
     * Default Constructor.
     */
    public StandardNamedObject() {
        m_name = "";
    }

    /**
     * Constructor.
     *
     * @param name a {@link java.lang.String} object.
     */
    public StandardNamedObject(String name) {
        m_name = name;
    }

    /**
     * Set the name
     *
     * @param name
     *            The name to be set.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * Return the name
     *
     * @return the name.
     */
    public String getName() {
        return m_name;
    }
}
