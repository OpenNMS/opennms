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
package org.opennms.features.topology.netutils.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class ResourceGraphsWindowTest {

    ResourceGraphsWindow window1;

    ResourceGraphsWindow window2;

    Window mainWindow;

    UI app;

    @Before
    public void setUp() throws Exception {
        Node testNode1 = new Node(9, "192.0.2.10", "Cartman");
        final URL url = new URL("http://localhost:8080/");
        window1 = new ResourceGraphsWindow(testNode1, url);
        window2 = new ResourceGraphsWindow(null, url);
        mainWindow = new Window();
        app = new UI() { // Empty Application

            private static final long serialVersionUID = -8945754438079223762L;

            @Override
            public void init(VaadinRequest request) {
            }
        };
    }

    @Test
    public void testAttach() {
        app.addWindow(window1);
        assertTrue(app.getWindows().contains(window1));
        app.removeWindow(window1);
        assertFalse(app.getWindows().contains(window1));
    }

}
