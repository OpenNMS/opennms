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
package org.opennms.netmgt.alarmd.northbounder.snmptrap;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class TrapData.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class TrapData {

    /** The enterprise OID. */
    private String enterpriseOid;

    /** The generic. */
    private int generic;

    /** The specific. */
    private int specific;

    /** The parameters. */
    private List<TrapParameter> parameters = new ArrayList<>();

    /**
     * Instantiates a new trap data.
     *
     * @param enterpriseOid the enterprise OID
     * @param generic the generic
     * @param specific the specific
     */
    public TrapData(String enterpriseOid, int generic, int specific) {
        super();
        this.enterpriseOid = enterpriseOid;
        this.generic = generic;
        this.specific = specific;
    }

    /**
     * Gets the enterprise OID.
     *
     * @return the enterprise OID
     */
    public String getEnterpriseOid() {
        return enterpriseOid;
    }

    /**
     * Gets the generic.
     *
     * @return the generic
     */
    public int getGeneric() {
        return generic;
    }

    /**
     * Gets the specific.
     *
     * @return the specific
     */
    public int getSpecific() {
        return specific;
    }

    /**
     * Gets the parameters.
     *
     * @return the parameters
     */
    public List<TrapParameter> getParameters() {
        return parameters;
    }

    /**
     * Adds the parameter.
     *
     * @param name the name
     * @param value the value
     */
    public void addParameter(String name, String value) {
        parameters.add(new TrapParameter(name, value));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TrapData [enterpriseOid=" + enterpriseOid + ", generic=" + generic + ", specific=" + specific + ", parameters=" + parameters + "]";
    }

}
