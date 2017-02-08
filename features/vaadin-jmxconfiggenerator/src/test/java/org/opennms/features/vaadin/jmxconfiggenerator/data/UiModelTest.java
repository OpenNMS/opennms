package org.opennms.features.vaadin.jmxconfiggenerator.data;


import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.vaadin.core.UIHelper;

public class UiModelTest {

    @Test
    public void verifyDescriptions() {
        for (UiModel.OutputDataKey eachKey : UiModel.OutputDataKey.values()) {
            String keyDescription = UIHelper.loadContentFromFile(getClass(), eachKey.getDescriptionFilename());
            Assert.assertNotNull(keyDescription);
        }
    }

}
