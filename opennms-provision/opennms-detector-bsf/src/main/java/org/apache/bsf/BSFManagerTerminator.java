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
package org.apache.bsf;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class allows {@link BSFManager} instances to be cleanly
 * terminated as a workaround for {@link https://issues.apache.org/jira/browse/BSF-41}
 *
 * The bug is fixed in v2.5.0, but no releases are available.
 *
 * See NMS-8109 for details.
 *
 * NOTE: This class is duplicated in the opennms-services project since there
 * is no good common place to put this.
 * 
 * @author jwhite
 */
public class BSFManagerTerminator {

    @SuppressWarnings("rawtypes")
    public static void terminate(BSFManager manager) {
        // Termination code from 2.5.0-SNAPSHOT
        Enumeration<?> enginesEnum = manager.loadedEngines.elements();
        BSFEngine engine;
        while (enginesEnum.hasMoreElements()) {
            engine = (BSFEngine) enginesEnum.nextElement();
            manager.pcs.removePropertyChangeListener(engine);   // rgf, 2014-12-30: removing memory leak
            engine.terminate();
        }
        manager.loadedEngines = new Hashtable();

        // Call terminate again for sanity
        manager.terminate();
    }
}
