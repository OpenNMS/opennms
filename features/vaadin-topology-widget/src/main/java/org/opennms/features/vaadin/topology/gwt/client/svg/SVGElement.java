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

package org.opennms.features.vaadin.topology.gwt.client.svg;

import com.google.gwt.dom.client.Element;

public class SVGElement extends Element{

    protected SVGElement() {}
    
    public final static native SVGElement wrapElement(Element svg) /*-{
        return elem;
    }-*/;

    public final native SVGMatrix createSVGMatrix() /*-{
        return this.createSVGMatrix();
    }-*/;

    public final native SVGElement getCTM() /*-{
        return this.getCTM();
    }-*/;

    public final native SVGPoint createSVGPoint() /*-{
        return this.createSVGPoint();
    }-*/;

    public final native void setX(int x) /*-{
        this.x = x;
    }-*/;
    
    public final native void setY(int y) /*-{
        this.y = y;
    }-*/;

    public final native SVGRect getBBox() /*-{
        return this.getBBox();
    }-*/;

}
