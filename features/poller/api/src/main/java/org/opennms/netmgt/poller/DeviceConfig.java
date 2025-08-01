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
package org.opennms.netmgt.poller;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name="device-config")
@XmlAccessorType(XmlAccessType.NONE)
public class DeviceConfig {

    @XmlAttribute(name="content")
    private byte[] content;

    @XmlAttribute(name="filename")
    private String filename;

    @XmlAttribute(name="scriptOutput")
    @XmlJavaTypeAdapter(EscapeSequenceAdapter.class)
    private String scriptOutput;

    public DeviceConfig(String scriptOutput) {
        this(null, null, scriptOutput);
    }

    public DeviceConfig(byte[] content, String fileName) {
        this(content, fileName, null);
    }

    public DeviceConfig(byte[] content, String fileName, String scriptOutput) {
        this.content = content;
        this.filename = fileName;
        this.scriptOutput = scriptOutput;
    }

    // for JAXB
    public DeviceConfig() {
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getScriptOutput() {
        return scriptOutput;
    }

    public void setScriptOutput(String scriptOutput) {
        this.scriptOutput = scriptOutput;
    }
}
