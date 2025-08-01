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
