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
