/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.gwt.client.svg;

import com.google.gwt.core.client.JavaScriptObject;

public class SVGMatrix extends JavaScriptObject {
    
    protected SVGMatrix() {};
    
    public final native SVGMatrix translate(double x, double y)/*-{
        return this.translate(x, y);
    }-*/;

    public final native SVGMatrix scale(double newScale) /*-{
        return this.scale(newScale);
    }-*/;

    public final native SVGMatrix multiply(SVGMatrix m) /*-{
        return this.multiply(m);
    }-*/;

    public final native double getA() /*-{
        return this.a;
    }-*/;
    
    public final native double getB() /*-{
        return this.b;
    }-*/;
    
    public final native double getC() /*-{
        return this.c;
    }-*/;
    
    public final native double getD() /*-{
        return this.d;
    }-*/;
    
    public final native double getE() /*-{
        return this.e;
    }-*/;
    
    public final native double getF() /*-{
        return this.f;
    }-*/;

    public final native SVGMatrix inverse() /*-{
        return this.inverse();
    }-*/;

    public final native void setX(int clientX) /*-{
        this.x = clientX;
    }-*/;

    public final native void setY(int clientY) /*-{
        this.y = clientY;
    }-*/;

    public final native SVGMatrix matrixTransform(SVGMatrix matrix) /*-{
        return this.matrixTransform(matrix);
    }-*/;

    public final native int getX() /*-{
        return this.e;
    }-*/;

    public final native int getY() /*-{
        return this.f;
    }-*/;

}
