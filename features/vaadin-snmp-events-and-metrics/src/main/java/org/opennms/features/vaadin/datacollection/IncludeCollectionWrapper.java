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
