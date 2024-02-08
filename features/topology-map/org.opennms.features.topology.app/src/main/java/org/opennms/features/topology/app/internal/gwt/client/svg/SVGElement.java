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
