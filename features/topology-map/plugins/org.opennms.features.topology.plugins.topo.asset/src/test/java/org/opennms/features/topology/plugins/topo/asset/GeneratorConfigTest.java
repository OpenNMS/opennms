package org.opennms.features.topology.plugins.topo.asset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class GeneratorConfigTest {

	@Test
	public void testFilter1() {
		GeneratorConfig conf=new GeneratorConfig();
		conf.setFilter("");
		assertNull(conf.getFilterList());
	}
	
	@Test
	public void testFilter2() {
		GeneratorConfig conf=new GeneratorConfig();
		List<String> filterExpected=Arrays.asList("a=b","aaa=b,c,d","c=!b,d");
        conf.setFilter("a=b&aaa=b,c,d&c=!b,d");
        List<String> filter=conf.getFilterList();
        assertEquals(filterExpected.size(),filter.size());
        assertEquals(filterExpected.get(0),filter.get(0));
        assertEquals(filterExpected.get(1),filter.get(1));
        assertEquals(filterExpected.get(2),filter.get(2));
        
	}

}
