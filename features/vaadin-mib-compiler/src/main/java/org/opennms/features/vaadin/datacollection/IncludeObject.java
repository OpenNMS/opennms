/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
 * The Include Object.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class IncludeObject {

    public static final String SYSTEM_DEF = "SystemDef";
    public static final String DC_GROUP = "DataCollectionGroup";

    /** The type. */
    private String type = DC_GROUP;

    /** The value. */
    private String value;

    /**
     * Instantiates a new include object.
     */
    public IncludeObject() {}

    public IncludeObject(IncludeCollection ic) {
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
    public IncludeObject(String type, String value) {
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

    public IncludeCollection createIncludeCollection() {
        IncludeCollection ic = new IncludeCollection();
        if (getType().equals(SYSTEM_DEF))
            ic.setSystemDef(getValue());
        if (getType().equals(IncludeObject.DC_GROUP))
            ic.setDataCollectionGroup(getValue());
        return ic;
    }

    @Override
    public String toString() {
        return value + " (" + type + ")";
    }

}
