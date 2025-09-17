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
package org.opennms.web.rest.support.menu.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

public class MenuXml {
    public static class ConstructorArgElement {
        // List of BeanElement and/or BeanRefElement
        private List<BeanOrRefElement> beansOrRefs = new ArrayList<>();

        // <constructor-arg>
        //   <list>
        //     <bean>
        //     <ref>
        @XmlElementWrapper(name="list")
        @XmlElements({
            @XmlElement(name="bean", type=BeanElement.class),
            @XmlElement(name="ref", type=BeanRefElement.class)
        })
        public List<BeanOrRefElement> getBeansOrRefs() {
            return this.beansOrRefs;
        }

        public void setBeansOrRefs(List<BeanOrRefElement> list) {
            this.beansOrRefs = list;
        }
    }

    // wrapper class
    // <constructor-arg><list> can include either <bean> or <ref @bean> elements.
    public static abstract class BeanOrRefElement {
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

    public static class BeanElement extends BeanOrRefElement {
        private String id;
        private String name;
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

        @XmlAttribute(name="name")
        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
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

    public static class BeanRefElement extends BeanOrRefElement{
        private String beanRef;

        @XmlAttribute(name="bean")
        public String getBeanRef() {
            return this.beanRef;
        }

        public void setBeanRef(String s) {
            this.beanRef = s;
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
