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
