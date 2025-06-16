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
package org.opennms.netmgt.graph.provider.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.opennms.netmgt.model.OnmsApplication;

public class TestObjectCreator {

    private static int ids = 0;

    public static OnmsApplication createOnmsApplication() {
        OnmsApplication app = new OnmsApplication();
        app.setId(ids++);
        app.setName(UUID.randomUUID().toString());
        return app;
    }

    public static List<OnmsApplication> createOnmsApplications(int amount) {
        List<OnmsApplication> applications = new ArrayList<>();
        for(int i=0; i<amount; i++) {
            applications.add(createOnmsApplication());
        }
        return applications;
    }
}
