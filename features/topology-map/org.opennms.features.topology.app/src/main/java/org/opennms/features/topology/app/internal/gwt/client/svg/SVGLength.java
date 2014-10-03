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

package org.opennms.features.topology.app.internal.gwt.client.svg;

import com.google.gwt.core.client.JavaScriptObject;

public class SVGLength extends JavaScriptObject {

    public static final int SVG_LENGTHTYPE_PX = 5;

    protected SVGLength() {
        
    }

    public final native int getUnitType() /*-{
        return this.unitType;
    }-*/;

    public final native int getValueInSpecifiedUnits() /*-{
        return this.valueInSpecifiedUnits;
    }-*/;

    public final native void setNewValueSpecifiedUnits(int unitType, int valueInSpecifiedUnits) /*-{
        this.newValueSpecifiedUnits(unitType, valueInSpecifiedUnits);
    }-*/;

    public final native void convertToSpecifiedUnits(int unitType) /*-{
        $wnd.console.log("calling: " + convertToSpecifiedUnits + " with type: " + unitType);
        this.convertToSpecifiedUnits(unitType);
    }-*/;
    
}
