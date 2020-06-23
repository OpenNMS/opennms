/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
