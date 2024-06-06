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
package org.opennms.netmgt.config.service;

import java.util.Objects;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="at")
@XmlEnum
public enum InvokeAtType {
    @XmlEnumValue("start")
    START("Start", "Starting"),
    
    @XmlEnumValue("stop")
    STOP("Stop", "Stopping"),
    
    @XmlEnumValue("status")
    STATUS("Status", "Getting status");


    private final String label;
    private final String presentParticiple;

    InvokeAtType(String label, String presentParticiple) {
        this.label = Objects.requireNonNull(label);
        this.presentParticiple = Objects.requireNonNull(presentParticiple);
    }

    public String getLabel() {
        return label;
    }

    public String getPresentParticiple() { return presentParticiple; }
}
