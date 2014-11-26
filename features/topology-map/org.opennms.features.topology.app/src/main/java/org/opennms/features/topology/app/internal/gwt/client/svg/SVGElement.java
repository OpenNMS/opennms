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

import com.google.gwt.dom.client.Element;

public class SVGElement extends Element{

    protected SVGElement() {}
    
    public static final native SVGElement wrapElement(Element svg) /*-{
        return elem;
    }-*/;

    public final native SVGMatrix createSVGMatrix() /*-{
        return this.createSVGMatrix();
    }-*/;

    public final native SVGMatrix getCTM() /*-{
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
    
    public final native ClientRect getBoundingClientRect() /*-{
        return this.getBoundingClientRect();
    }-*/;

    public final native SVGMatrix inverse() /*-{
        return this.inverse();
    }-*/;

    public final native SVGAnimatedLength getWidth() /*-{
        return this.width;
    }-*/;

    public final native SVGAnimatedLength getHeight() /*-{
        return this.height;
    }-*/;

    public final native SVGLength createSVGLength() /*-{
        return this.createSVGLength();
    }-*/;

    public final native SVGMatrix getScreenCTM() /*-{
        return this.getScreenCTM();
    }-*/;

}
