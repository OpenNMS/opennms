package org.opennms.netmgt.config.poller;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


@XmlRegistry
public class ParameterFactory {

    @XmlElementDecl(name="person")
    public JAXBElement<Person> createPerson(Person person) {
        return new JAXBElement<Person>(new QName("person"), Person.class, person);
    }
}
