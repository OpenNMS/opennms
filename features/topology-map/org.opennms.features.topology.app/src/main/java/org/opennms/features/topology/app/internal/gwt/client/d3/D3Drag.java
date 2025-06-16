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

import org.opennms.features.topology.app.internal.gwt.client.d3.D3Events.Handler;

import com.google.gwt.core.client.JavaScriptObject;

public class D3Drag extends JavaScriptObject {
    
    protected D3Drag() {};
    
    public final native D3Drag on(String event, Handler<?> handler) /*-{
        
        var f = function(d, i) {
            return handler.@org.opennms.features.topology.app.internal.gwt.client.d3.D3Events.Handler::call(Ljava/lang/Object;I)(d,i);
        }
    
        return this.on(event, f);
    }-*/;

}
