package org.opennms.features.config.dao.impl.util;

import static org.junit.Assert.assertEquals;
import static org.opennms.features.config.dao.impl.util.TopLevelElementToClass.topLevelElementToClass;

import org.junit.Test;

public class TopLevelElementToClassTest {

    @Test
    public void shouldConvert() {
        assertEquals("VacuumdConfiguration", topLevelElementToClass("VacuumdConfiguration"));
        assertEquals("ProvisiondConfiguration", topLevelElementToClass("provisiond-configuration"));
    }
}
