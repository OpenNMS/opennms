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
package org.opennms.spring.xml;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;

@EventListener(name="AspectJITEventHandler")
public class AspectJITEventHandler {

    
    private Throwable thrownException = null;
    private int handlerCallCount = 0;
    
    public void setThrownException(Throwable throwable) {
        this.thrownException = throwable;
    }

    public int getHandlerCallCount() {
        return handlerCallCount;
    }

    public void setHandlerCallCount(int handlerCallCount) {
        this.handlerCallCount = handlerCallCount;
    }

    @EventHandler(uei=EventConstants.NODE_LOST_SERVICE_EVENT_UEI)
    public void handleAnEvent(IEvent e) throws Throwable {
        System.err.println("Received Event "+e.getUei());
        handlerCallCount++;
        if (thrownException != null) {
            throw thrownException;
        }
    }

    public void reset() {
        handlerCallCount = 0;
        thrownException = null;
    }
}
