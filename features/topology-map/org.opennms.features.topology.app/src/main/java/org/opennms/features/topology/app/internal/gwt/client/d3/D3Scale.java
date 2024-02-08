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
import com.google.gwt.core.client.JsArray;

public class D3Scale extends JavaScriptObject {
    
    protected D3Scale() {}

    public final native D3Scale ordinal() /*-{
        return this.ordinal();
    }-*/;

    public final native D3Scale domain(JsArray<?> array) /*-{
        return this.domain(array);
    }-*/;
    
    public final native D3Scale domain(int[] data) /*-{
    	return this.domain(data);
    }-*/;

    public final native JavaScriptObject rangePoints(JsArray<?> rangeArray, int i) /*-{
        return this.rangePoints(rangeArray, i);
    }-*/;
}
