/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.jasper.rrdtool;

import java.io.FileReader;
import java.io.Reader;
import java.util.Date;

import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRPropertiesHolder;
import net.sf.jasperreports.engine.JRPropertiesMap;

import org.exolab.castor.xml.Unmarshaller;
import org.junit.Assert;
import org.junit.Test;

public class RrdtoolDataSourceTest {

	private class RrdField implements JRField {

		private String name;
		private Class<?> value;

		public RrdField(String name, Class<?> value) {
			this.name = name;
			this.value = value;
		}

                @Override
		public JRPropertiesHolder getParentProperties() {
			return null;
		}

                @Override
		public JRPropertiesMap getPropertiesMap() {
			return null;
		}

                @Override
		public boolean hasProperties() {
			return false;
		}

                @Override
		public String getDescription() {
			return null;
		}

                @Override
		public String getName() {
			return name;
		}

                @Override
		public Class<?> getValueClass() {
			return value;
		}

                @Override
		public String getValueClassName() {
			return value.getName();
		}

                @Override
		public void setDescription(String arg0) {
		}

		@Override
		public Object clone() {
			return null;
		}
	}

	@Test
	public void testCommand() {
		System.setProperty("rrd.binary", "/usr/bin/rrdtool");
		String queryString = "--start 1287005100 --end 1287018990\n          DEF:xx=src/test/resources/http-8980.jrb:http-8980:AVERAGE\n          DEF:zz=src/test/resources/ssh.jrb:ssh:AVERAGE\r\n          XPORT:xx:HttpLatency XPORT:zz:SshLatency";
		System.err.println(queryString);
		String command = System.getProperty("rrd.binary") + " xport " + queryString.replaceAll("[\r\n]+", " ").replaceAll("\\s+", " ");
		System.err.println("single line: " + command);
	}

	@Test
	public void testEmptyData() throws Exception {
		RrdtoolDataSource ds = new RrdtoolDataSource(null);
		Assert.assertFalse(ds.next());
	}

	@Test
	public void testRrdData() throws Exception {
		Reader reader = new FileReader("src/test/resources/rrdtool-xport.xml");
		Xport xport = (Xport) Unmarshaller.unmarshal(Xport.class, reader);
		Assert.assertEquals("SampleData", xport.getMeta().getLegend().getEntry(0).getContent());
		Assert.assertEquals(13, xport.getData().getRowCount());
		RrdtoolDataSource ds = new RrdtoolDataSource(xport);

		Assert.assertTrue(ds.next());

		Date d1 = (Date) ds.getFieldValue(new RrdField("Timestamp", Date.class));
        Assert.assertEquals(new Date(1206312900000L).toString(), d1.toString());

		Double v1 = (Double) ds.getFieldValue(new RrdField("SampleData", Double.class));
		Assert.assertEquals(new Double(19.86), v1);

		Assert.assertTrue(ds.next());

		Date d2 = (Date) ds.getFieldValue(new RrdField("Timestamp", Date.class));
        Assert.assertEquals(new Date(1206313200000L).toString(), d2.toString());

		Double v2 = (Double) ds.getFieldValue(new RrdField("SampleData", Double.class));
		Assert.assertEquals(new Double(Double.NaN), v2);

		Assert.assertTrue(ds.next());

		Date d3 = (Date) ds.getFieldValue(new RrdField("Timestamp", Date.class));
        Assert.assertEquals(new Date(1206313500000L).toString(), d3.toString());

		Double v3 = (Double) ds.getFieldValue(new RrdField("SampleData", Double.class));
		Assert.assertEquals(new Double(Double.NaN), v3);

		Assert.assertTrue(ds.next());

		Date d4 = (Date) ds.getFieldValue(new RrdField("Timestamp", Date.class));
        Assert.assertEquals(new Date(1206313800000L).toString(), d4.toString());

		Double v4 = (Double) ds.getFieldValue(new RrdField("SampleData", Double.class));
		Assert.assertEquals(new Double(26.00), v4);

		for (int i=4; i<13; i++)
			ds.next();

		Date d13 = (Date) ds.getFieldValue(new RrdField("Timestamp", Date.class));
        Assert.assertEquals(new Date(1206316500000L).toString(), d13.toString());

		Double v13 = (Double) ds.getFieldValue(new RrdField("SampleData", Double.class));
		Assert.assertEquals(new Double(50.00), v13);

		Assert.assertFalse(ds.next());
	}
    
}
