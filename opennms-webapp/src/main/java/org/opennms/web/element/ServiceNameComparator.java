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
package org.opennms.web.element;

import java.util.Comparator;

/**
 * <p>ServiceNameComparator class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class ServiceNameComparator implements Comparator<Service> {
    /**
     * <p>compare</p>
     *
     * @param s1 a {@link org.opennms.web.element.Service} object.
     * @param s2 a {@link org.opennms.web.element.Service} object.
     * @return a int.
     */
    @Override
    public int compare(Service s1, Service s2) {
        return s1.getServiceName().compareTo(s2.getServiceName());
    }

//  public boolean equals(Service s1, Service s2) {
//      return s1.getServiceName().equals(s2.getServiceName());
//  }        
}
