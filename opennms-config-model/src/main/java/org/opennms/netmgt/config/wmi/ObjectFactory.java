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
package org.opennms.netmgt.config.wmi;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.opennms.xmlns.xsd.config.wmi_datacollection package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.opennms.xmlns.xsd.config.wmi_datacollection
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Rrd }
     * 
     */
    public Rrd createRrd() {
        return new Rrd();
    }

    /**
     * Create an instance of {@link WmiDatacollectionConfig }
     * 
     */
    public WmiDatacollectionConfig createWmiDatacollectionConfig() {
        return new WmiDatacollectionConfig();
    }

    /**
     * Create an instance of {@link WmiCollection }
     * 
     */
    public WmiCollection createWmiCollection() {
        return new WmiCollection();
    }

    /**
     * Create an instance of {@link Wpm }
     * 
     */
    public Wpm createWpm() {
        return new Wpm();
    }

    /**
     * Create an instance of {@link Attrib }
     * 
     */
    public Attrib createAttrib() {
        return new Attrib();
    }

}
