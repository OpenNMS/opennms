package org.opennms.features.config.dao.impl.util;

// Reverse engineering the naming conventions of Jaxb Dynamic classes.
public class TopLevelElementToClass {

    public static String topLevelElementToClass(String toplevelElement) {
        // Java classes start with capital letters:
        String s = toplevelElement.substring(0,1).toUpperCase() + toplevelElement.substring(1);

        //replace dash
        s = s.replace("-c", "C");
        return s;
    }
}
