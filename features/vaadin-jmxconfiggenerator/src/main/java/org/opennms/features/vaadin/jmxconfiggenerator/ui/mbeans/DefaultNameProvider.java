package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans;

import com.vaadin.ui.Field;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Attrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompAttrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompMember;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

import java.util.*;

public class DefaultNameProvider implements NameProvider {

    private final FieldValueProvider fieldValueProvider;

    private final SelectionManager selectionManager;

    public DefaultNameProvider(SelectionManager selectionManager, FieldValueProvider fieldValueProvider) {
        Objects.requireNonNull(selectionManager);
        this.selectionManager = selectionManager;
        this.fieldValueProvider = fieldValueProvider;
    }

    public DefaultNameProvider(SelectionManager selectionManager) {
        this(selectionManager, null);
    }

    private String getName(Object input) {
        return fieldValueProvider == null ? null : fieldValueProvider.getFieldValue(input);
    }

    private String getAliasFor(Attrib attrib) {
        return getName(attrib) != null ? getName(attrib) : attrib.getAlias();
    }

    private String getAliasFor(CompMember compMember) {
        return getName(compMember) != null ? getName(compMember) : compMember.getAlias();
    }

    @Override
    public List<String> getNames() {
        List<String> nameList = new ArrayList<>();
        for (Mbean bean : selectionManager.getSelectedMbeans()) {
            for (Attrib att : selectionManager.getSelectedAttributes(bean)) {
                nameList.add(getAliasFor(att));
            }
            for (CompAttrib compAttrib : selectionManager.getSelectedCompositeAttributes(bean)) {
                for (CompMember compMember : selectionManager.getSelectedCompositeMembers(compAttrib)) {
                    nameList.add(getAliasFor(compMember));
                }
            }
        }
        return nameList;
    }
}
