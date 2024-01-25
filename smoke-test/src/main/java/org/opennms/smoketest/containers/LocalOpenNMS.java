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
package org.opennms.smoketest.containers;

import java.net.MalformedURLException;
import java.net.URL;

import org.opennms.smoketest.stacks.OpenNMSProfile;
import org.opennms.smoketest.stacks.StackModel;

public class LocalOpenNMS extends OpenNMSContainer {
    public LocalOpenNMS() {
        super(StackModel.newBuilder().build(), OpenNMSProfile.DEFAULT);
    }

    @Override
    public URL getBaseUrlInternal() {
        try {
            return new URL(String.format("http://%s:%d/", "host.docker.internal", 8980));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public URL getBaseUrlExternal() {
        try {
            return new URL(String.format("http://%s:%d/", "localhost", 8980));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "I'm an empty opennms container, y'all!";
    }
}
