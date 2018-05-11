package org.opennms.netmgt.model.events;

import org.junit.Test;

public class NodeLabelChangedEventBuilderTest {


    @Test(expected = IllegalStateException.class)
    public void ensureThatBuilderFailsOnMissingParameterNewNodeLabel(){
        new NodeLabelChangedEventBuilder(this.getClass().getName()).setOldNodeLabel("aa").getEvent();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureThatBuilderFailsOnMissingParameterOldNodeLabel(){
        new NodeLabelChangedEventBuilder(this.getClass().getName()).setNewNodeLabel("aa").getEvent();
    }

    @Test
    public void ensureThatBuilderSucceedsWhenAllRequiredParameterAreSet(){
        new NodeLabelChangedEventBuilder(this.getClass().getName()).setNewNodeLabel("aa").setOldNodeLabel("aa").getEvent();
    }

}