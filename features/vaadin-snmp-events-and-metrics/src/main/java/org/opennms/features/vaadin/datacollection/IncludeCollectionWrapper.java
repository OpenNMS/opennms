/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.datacollection;

import org.opennms.netmgt.config.datacollection.IncludeCollection;

/**
 * The Class Include Collection Wrapper.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class IncludeCollectionWrapper {

    /** The Constant SYSTEM_DEF. */
    public static final String SYSTEM_DEF = "SystemDef";

    /** The Constant DC_GROUP. */
    public static final String DC_GROUP = "DataCollectionGroup";

    /** The type. */
    private String type = DC_GROUP;

    /** The value. */
    private String value;

    /**
     * Instantiates a new include collection Wrapper.
     */
    public IncludeCollectionWrapper() {}

    /**
     * Instantiates a new include collection Wrapper.
     *
     * @param ic the source include collection
     */
    public IncludeCollectionWrapper(IncludeCollection ic) {
        if (ic.getSystemDef() == null || ic.getSystemDef().trim().equals("")) {
            setType(DC_GROUP);
            setValue(ic.getDataCollectionGroup());
        } else {
            setType(SYSTEM_DEF);
            setValue(ic.getSystemDef());
        }
    }

    /**
     * Instantiates a new include object.
     *
     * @param type the type
     * @param value the value
     */
    public IncludeCollectionWrapper(String type, String value) {
        setType(type);
        setValue(value);
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Creates the include collection.
     *
     * @return the include collection
     */
    public IncludeCollection createIncludeCollection() {
        IncludeCollection ic = new IncludeCollection();
        if (getType().equals(SYSTEM_DEF)) {
            ic.setSystemDef(getValue());
        }
        if (getType().equals(IncludeCollectionWrapper.DC_GROUP)) {
            ic.setDataCollectionGroup(getValue());
        }
        return ic;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return value + " (" + type + ")";
    }

}
