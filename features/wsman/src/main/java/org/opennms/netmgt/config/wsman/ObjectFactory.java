/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.wsman;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.opennms.netmgt.config.wsman package. 
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

    private final static QName _IpMatch_QNAME = new QName("http://xmlns.opennms.org/xsd/config/wsman", "ip-match");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.opennms.netmgt.config.wsman
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Collection }
     * 
     */
    public Collection createCollection() {
        return new Collection();
    }

    /**
     * Create an instance of {@link Range }
     * 
     */
    public Range createRange() {
        return new Range();
    }

    /**
     * Create an instance of {@link WsmanConfig }
     * 
     */
    public WsmanConfig createWsmanConfig() {
        return new WsmanConfig();
    }

    /**
     * Create an instance of {@link Definition }
     * 
     */
    public Definition createDefinition() {
        return new Definition();
    }

    /**
     * Create an instance of {@link Rrd }
     * 
     */
    public Rrd createRrd() {
        return new Rrd();
    }

    /**
     * Create an instance of {@link WsmanDatacollectionConfig }
     * 
     */
    public WsmanDatacollectionConfig createWsmanDatacollectionConfig() {
        return new WsmanDatacollectionConfig();
    }

    /**
     * Create an instance of {@link Collection.IncludeAllSystemDefinitions }
     * 
     */
    public Collection.IncludeAllSystemDefinitions createCollectionIncludeAllSystemDefinitions() {
        return new Collection.IncludeAllSystemDefinitions();
    }

    /**
     * Create an instance of {@link Group }
     * 
     */
    public Group createGroup() {
        return new Group();
    }

    /**
     * Create an instance of {@link Attrib }
     * 
     */
    public Attrib createAttrib() {
        return new Attrib();
    }

    /**
     * Create an instance of {@link SystemDefinition }
     * 
     */
    public SystemDefinition createSystemDefinition() {
        return new SystemDefinition();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xmlns.opennms.org/xsd/config/wsman", name = "ip-match")
    public JAXBElement<String> createIpMatch(String value) {
        return new JAXBElement<String>(_IpMatch_QNAME, String.class, null, value);
    }

}
