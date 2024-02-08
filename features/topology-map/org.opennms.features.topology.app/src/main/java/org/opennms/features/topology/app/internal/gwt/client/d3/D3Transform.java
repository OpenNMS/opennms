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
package org.opennms.features.topology.app.internal.gwt.client.d3;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayNumber;

public class D3Transform extends JavaScriptObject {

    protected D3Transform() {};
    
    public final native JsArrayInteger getTranslate() /*-{
        return this.translate;
    }-*/;
    
    public final native int getX() /*-{
        if(this.translate != "undefined"){
            return this.translate[0];
        }
        return -1;
    }-*/;
    
    public final native int getY() /*-{
        if(this.translate != "undefined"){
            return this.translate[1];
        }
        return -1;
    }-*/;
    
    public final native JsArrayNumber getScale() /*-{
        return this.scale;
    }-*/;
    
    public final native double getScaleX() /*-{
    	return this.scale[0];
    }-*/;
    
    public final native double getScaleY() /*-{
		return this.scale[1];
	}-*/;
    
    public final native double getRotate() /*-{
        return this.rotate;
    }-*/;
    
    public final native double getSkew() /*-{
        return this.skew;
    }-*/;
    
}
