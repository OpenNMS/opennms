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
package org.opennms.features.deviceconfig.sink.module;

import org.opennms.core.ipc.sink.api.Message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.Objects;

@XmlRootElement(name = "device-config-dto")
@XmlAccessorType(XmlAccessType.NONE)
public class DeviceConfigSinkDTO implements Message {

    @XmlElement(name = "location")
    public String location;
    // the source address of the device config upload (as returned by InetAddress.getAddress())
    @XmlElement(name = "address")
    public byte[] address;

    @XmlElement(name = "fileName")
    public String fileName;

    @XmlElement(name = "config")
    public byte[] config;

    public DeviceConfigSinkDTO(String location, byte[] address, String fileName, byte[] config) {
        this.location = location;
        this.address = address;
        this.fileName = fileName;
        this.config = config;
    }

    public DeviceConfigSinkDTO() {

    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getConfig() {
        return config;
    }

    public void setConfig(byte[] config) {
        this.config = config;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceConfigSinkDTO)) return false;
        DeviceConfigSinkDTO that = (DeviceConfigSinkDTO) o;
        return Objects.equals(location, that.location) && Arrays.equals(address, that.address) && Objects.equals(fileName, that.fileName) && Arrays.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(location, fileName);
        result = 31 * result + Arrays.hashCode(address);
        result = 31 * result + Arrays.hashCode(config);
        return result;
    }
}
