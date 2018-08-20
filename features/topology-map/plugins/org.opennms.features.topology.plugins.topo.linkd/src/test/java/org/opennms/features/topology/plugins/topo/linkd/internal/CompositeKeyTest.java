package org.opennms.features.topology.plugins.topo.linkd.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class CompositeKeyTest {
    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNoKeysInvocation() {
        new CompositeKey();
    }

    @Test
    public void equalsAndHashCodeShouldWork() {
        CompositeKey same1 = new CompositeKey("aa", 33);
        CompositeKey same2 = new CompositeKey("aa", 33);
        CompositeKey different = new CompositeKey("aa", 31);
        assertEquals(same1, same2);
        assertEquals(same1.hashCode(), same2.hashCode());
        assertNotEquals(same1, different);
    }
}