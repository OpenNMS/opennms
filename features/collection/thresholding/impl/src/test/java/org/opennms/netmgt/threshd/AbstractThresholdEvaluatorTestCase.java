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
package org.opennms.netmgt.threshd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;

/**
 * @author jeffg
 *
 */
public abstract class AbstractThresholdEvaluatorTestCase {
    protected static void parmPresentAndValueNonNull(Event event, String parmName) {
        boolean parmPresent = false;
        
        for (Parm parm : event.getParmCollection()) {
            if (parmName.equals(parm.getParmName())) {
                assertNotNull("Value content of parm '" + parmName + "'", parm.getValue().getContent());
                parmPresent = true;
            }
        }
        assertTrue("Parm '" + parmName + "' present", parmPresent);
    }
    
    protected static void parmPresentWithValue(Event event, String parmName, String expectedValue) {
        boolean parmPresent = false;
        
        for (Parm parm : event.getParmCollection()) {
            if (parmName.equals(parm.getParmName())) {
                parmPresent = true;
                if (expectedValue.equals(parm.getValue().getContent())) {
                    assertNotNull("Value content of parm '" + parmName + "'", parm.getValue().getContent());
                    assertEquals("Value content of parm '" + parmName + "' should be '" + expectedValue + "'", expectedValue, parm.getValue().getContent());
                    parmPresent = true;
                }
            }
        }
        assertTrue("Parm '" + parmName + "' present", parmPresent);
    }
}
