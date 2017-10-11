/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
