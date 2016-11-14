package org.opennms.features.eifadapter;

import org.junit.Assert;
import org.junit.Test;

public class EifParserTest {

    @Test
    public void verifyMappingForOnmsSeverityExists() {
        for (EifParser.EifSeverity severity : EifParser.EifSeverity.values()) {
            Assert.assertNotNull(severity.toOnmsSeverity());
            Assert.assertNotNull(severity.toOnmsSeverity().getLabel());
        }
    }

}