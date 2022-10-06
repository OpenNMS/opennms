/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.support.menu.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

public class MenuXml {
    public static class ConstructorArgElement {
        private List<BeanElement> beans = new ArrayList<>();

        // <constructor-arg>
        //   <list>
        //     <bean>
        @XmlElementWrapper(name="list")
        @XmlElement(name="bean")
        public List<BeanElement> getBeans() {
            return this.beans;
        }

        public void setBeans(List<BeanElement> list) {
            this.beans = list;
        }
    }

    public static class BeanPropertyElement {
        private String name;
        private String value;
        private List<BeanElement> beans = new ArrayList<>();

        @XmlAttribute(name="name")
        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @XmlAttribute(name="value")
        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        // <property name="entries">
        //   <list>
        //     <bean>
        @XmlElementWrapper(name="list")
        @XmlElement(name="bean")
        public List<BeanElement> getBeans() {
            return this.beans;
        }

        public void setList(List<BeanElement> list) {
            this.beans = list;
        }
    }

    public static class BeanElement {
        private String id;
        private String className;
        private List<BeanPropertyElement> properties = new ArrayList<>();
        private ConstructorArgElement constructorArgElement;

        @XmlAttribute(name="id")
        public String getId() {
            return this.id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @XmlAttribute(name="class")
        public String getClassName() {
            return this.className;
        }

        public void setClassName(String s) {
            this.className = s;
        }

        @XmlElement(name="property", type=BeanPropertyElement.class)
        public List<BeanPropertyElement> getProperties() {
            return this.properties;
        }

        public void setProperties(List<BeanPropertyElement> properties) {
            this.properties = properties;
        }

        @XmlElement(name="constructor-arg", type=ConstructorArgElement.class)
        public ConstructorArgElement getConstructorArgElement() {
            return this.constructorArgElement;
        }

        public void setConstructorArgElement(ConstructorArgElement c) {
            this.constructorArgElement = c;
        }
    }

    @XmlRootElement(name="beans")
    public static class BeansElement {
        private List<BeanElement> beans = new ArrayList<>();

        @XmlElement(name="bean", type=BeanElement.class)
        public List<BeanElement> getBeans() {
            return this.beans;
        }

        public void setBeans(List<BeanElement> beans) {
            this.beans = beans;
        }
    }
}
