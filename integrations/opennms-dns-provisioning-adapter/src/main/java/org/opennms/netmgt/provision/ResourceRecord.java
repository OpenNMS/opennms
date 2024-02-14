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
package org.opennms.netmgt.provision;

/**
 * <p>ResourceRecord class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ResourceRecord {

    private String m_name;
    private String m_rClass;
    private Integer m_rdLength;
    private String m_rdata;
    
    private String m_ttl;
    
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * <p>getRClass</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRClass() {
        return m_rClass;
    }

    /**
     * <p>setClass</p>
     *
     * @param class1 a {@link java.lang.String} object.
     */
    public void setClass(String class1) {
        m_rClass = class1;
    }

    /**
     * <p>getRdLength</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getRdLength() {
        return m_rdLength;
    }

    /**
     * <p>setRdLength</p>
     *
     * @param rdLength a {@link java.lang.Integer} object.
     */
    public void setRdLength(Integer rdLength) {
        m_rdLength = rdLength;
    }

    /**
     * <p>getRdata</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRdata() {
        return m_rdata;
    }

    /**
     * <p>setRdata</p>
     *
     * @param rdata a {@link java.lang.String} object.
     */
    public void setRdata(String rdata) {
        m_rdata = rdata;
    }

    /**
     * <p>getTtl</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTtl() {
        return m_ttl;
    }

    /**
     * <p>setTtl</p>
     *
     * @param ttl a {@link java.lang.String} object.
     */
    public void setTtl(String ttl) {
        m_ttl = ttl;
    }

}
