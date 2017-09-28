package org.opennms.features.vaadin.jmxconfiggenerator;

import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.NameProvider;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Attrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompAttrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompMember;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

import java.util.Collections;
import java.util.Map;

public class TestHelper {

    public static NameProvider DUMMY_NAME_PROVIDER = new NameProvider() {
        @Override
        public Map<Object, String> getNamesMap() {
            return Collections.emptyMap();
        }
    };

    public static Attrib createAttrib(String name, String alias) {
        Attrib attrib = new Attrib();
        attrib.setName(name);
        attrib.setAlias(alias);
        return attrib;
    }

    public static CompMember createCompMember(String name, String alias) {
        CompMember compMember = new CompMember();
        compMember.setName(name);
        compMember.setAlias(alias);
        return compMember;
    }

    public static Mbean createMbean(String name) {
        Mbean mbean = new Mbean();
        mbean.setName(name);
        return mbean;
    }

    public static CompAttrib createCompAttrib(String name, CompMember... compMember) {
        CompAttrib compAttrib = new CompAttrib();
        compAttrib.setName(name);
        for (CompMember eachMember : compMember) {
            compAttrib.getCompMember().add(eachMember);
        }
        return compAttrib;
    }
}
