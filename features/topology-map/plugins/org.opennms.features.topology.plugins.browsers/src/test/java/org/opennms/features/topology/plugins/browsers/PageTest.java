package org.opennms.features.topology.plugins.browsers;


import junit.framework.Assert;
import org.junit.Test;

public class PageTest {

    @Test
    public void testUpdateOffset() {
        OnmsDaoContainer.Page p = new OnmsDaoContainer.Page(30, new OnmsDaoContainer.Size(0, new OnmsDaoContainer.SizeReloadStrategy() {
            @Override
            public int reload() {
                return 400;
            }
        }));

        // first page
        Assert.assertFalse(p.updateOffset(0));
        Assert.assertEquals(30, p.length);
        Assert.assertEquals(0, p.offset);

        // somewhere in between
        Assert.assertTrue(p.updateOffset(210));
        Assert.assertEquals(30, p.length);
        Assert.assertEquals(210 / 30 * 30, p.offset);

        // last page
        Assert.assertTrue(p.updateOffset(399));
        Assert.assertEquals(30, p.length);
        Assert.assertEquals(399 / 30 * 30, p.offset);
    }
}
