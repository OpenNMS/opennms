/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.jicmp.standalone;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;


/**
 * Main
 *
 * @author brozow
 */
public class Main {
    
    public static void main(String[] args) throws Exception {
        System.exit(new Main().execute(args));
    }
    
    public int execute(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("java -jar jna-jicmp-VERSION.jar <hostname or ip address>");
            return 1;
        }
        
        InetAddress addr = InetAddress.getByName(args[0]);

        PingReplyMetric metric;
        if (addr instanceof Inet4Address) {
            V4Pinger pinger = new V4Pinger(1234);
            metric = pinger.ping((Inet4Address)addr);
        } else if (addr instanceof Inet6Address){
            V6Pinger pinger = new V6Pinger(1234);
            metric = pinger.ping((Inet6Address)addr);
        } else {
            System.err.println("Unrecognized address type " + addr.getClass());
            return 1;
        }
        
        metric.await();
        System.err.println(metric.getSummary(TimeUnit.MILLISECONDS));
        
        return 0;
    }

}
