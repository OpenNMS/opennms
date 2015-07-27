package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans;

import org.opennms.xmlns.xsd.config.jmx_datacollection.Attrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompAttrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompMember;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by mvrueden on 14/07/15.
 */
public interface SelectionManager {

    Collection<Attrib> getSelectedAttributes(Mbean mbean);

    Collection<CompMember> getSelectedCompositeMembers(CompAttrib compAttrib);

    Collection<CompAttrib> getSelectedCompositeAttributes(Mbean mbean);

    Collection<Mbean> getSelectedMbeans();

    SelectionManager EMPTY = new SelectionManager() {
        @Override
        public Collection<Attrib> getSelectedAttributes(Mbean mbean) {
            return Collections.emptyList();
        }

        @Override
        public Collection<CompMember> getSelectedCompositeMembers(CompAttrib compAttrib) {
            return Collections.emptyList();
        }

        @Override
        public Collection<CompAttrib> getSelectedCompositeAttributes(Mbean mbean) {
            return Collections.emptyList();
        }

        @Override
        public Collection<Mbean> getSelectedMbeans() {
            return Collections.emptyList();
        }
    };
}
