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
package org.opennms.features.vaadin.dashboard.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a collection of {@link Wallboard}.
 *
 * @author Christian Pape
 */
@XmlRootElement
public class Wallboards {
    /**
     * A {@link List} holding the {@link Wallboard} instances
     */
    private List<Wallboard> m_wallboards = new ArrayList<>();

    /**
     * Default constructor.
     */
    public Wallboards() {
    }

    /**
     * Returns the {@link List} of {@link Wallboard} instances.
     *
     * @return the {@link Wallboard} instances
     */
    @XmlElement(name = "wallboard")
    public List<Wallboard> getWallboards() {
        return m_wallboards;
    }
}
