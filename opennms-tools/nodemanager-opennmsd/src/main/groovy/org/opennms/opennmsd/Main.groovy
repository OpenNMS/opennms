/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.opennmsd;

import org.apache.log4j.Logger;

public class Main {
    
    private static Logger log = Logger.getLogger(Main.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            log.info("Starting opennmsd");
            OpenNMSDaemon daemon = new OpenNMSDaemon();
            daemon.setConfiguration(new DefaultConfiguration());
            daemon.setEventForwarder(new DefaultEventForwarder());
            daemon.execute();
        } catch(Throwable e) {
            log.error("Exception executing opennmsd", e);
            System.exit(27);
        }
        
        System.exit(0);
        
    }

}