package org.opennms.netmgt.provision.requisition.command;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.ImmutableEvent;
import org.opennms.netmgt.events.api.model.ImmutableParm;
import org.opennms.netmgt.events.api.model.ImmutableValue;

import com.google.common.collect.Lists;

public class ImportEventListenerTest {

    @Test
    public void testSuccess() {
        ImportRequisition.ImportEventListener importEventListener = new ImportRequisition.ImportEventListener("my://crazy.url?password=bar");
        importEventListener.onEvent(event(EventConstants.IMPORT_STARTED_UEI, ImportRequisition.ImportEventListener.stripCredentials("my://crazy.url?password=bar")));
        Assert.assertFalse(importEventListener.isDone());
        importEventListener.onEvent(event(EventConstants.IMPORT_SUCCESSFUL_UEI, ImportRequisition.ImportEventListener.stripCredentials("my://crazy.url?password=bar")));
        Assert.assertTrue(importEventListener.isDone());
        Assert.assertEquals(EventConstants.IMPORT_SUCCESSFUL_UEI, importEventListener.getReceivedUei());
    }

    @Test
    public void testFail() {
        ImportRequisition.ImportEventListener importEventListener = new ImportRequisition.ImportEventListener("my://crazy.url?password=bar");
        importEventListener.onEvent(event(EventConstants.IMPORT_STARTED_UEI, ImportRequisition.ImportEventListener.stripCredentials("my://crazy.url?password=bar")));
        Assert.assertFalse(importEventListener.isDone());
        importEventListener.onEvent(event(EventConstants.IMPORT_FAILED_UEI, ImportRequisition.ImportEventListener.stripCredentials("my://crazy.url?password=bar")));
        Assert.assertTrue(importEventListener.isDone());
        Assert.assertEquals(EventConstants.IMPORT_FAILED_UEI, importEventListener.getReceivedUei());
    }

    @Test
    public void testNoMatch() {
        ImportRequisition.ImportEventListener importEventListener = new ImportRequisition.ImportEventListener("my://crazy.url?password=bar");
        importEventListener.onEvent(event(EventConstants.IMPORT_SUCCESSFUL_UEI, ImportRequisition.ImportEventListener.stripCredentials("my://crazy.url?nomatch=bar")));
        Assert.assertFalse(importEventListener.isDone());
        importEventListener.onEvent(event(EventConstants.IMPORT_FAILED_UEI, ImportRequisition.ImportEventListener.stripCredentials("my://crazy.url?nomatch=bar")));
        Assert.assertFalse(importEventListener.isDone());
        Assert.assertNull(importEventListener.getReceivedUei());
    }

    private IEvent event(final String uei, final String importResource) {
        return ImmutableEvent.newBuilder()
                .setUei(uei)
                .setParms(Lists.newArrayList(
                        ImmutableParm.newBuilder()
                                .setParmName("importResource")
                                .setValue(ImmutableValue.newBuilder()
                                        .setContent(importResource)
                                        .build())
                                .build()))
                .build();
    }
}
