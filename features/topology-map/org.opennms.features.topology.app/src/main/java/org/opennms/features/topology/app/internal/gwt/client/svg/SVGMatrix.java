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
