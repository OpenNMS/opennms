package org.opennms.sms.monitor.internal.config;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.junit.Test;

public class JaxbTest {

	@XmlRootElement(name="canyon")
	public static class Canyon {
		private String m_name;
		
//		@XmlElement(name="wonder")
//		@XmlAnyElement
		@XmlElementRef
		private List<NaturalWonder> m_wonders;
		@XmlElementRef
		private List<RockFormation> m_rocks;

		public void inspire(String name) {
			for (NaturalWonder w : m_wonders) {
				w.inspire(name);
			}
		}

		public void addRockFormation(RockFormation rf) {
			if (m_rocks == null) {
				m_rocks = new ArrayList<RockFormation>();
			}
			m_rocks.add(rf);
		}
		
		public void addWonder(NaturalWonder w) {
			if (m_wonders == null) {
				m_wonders = new ArrayList<NaturalWonder>();
			}
			m_wonders.add(w);
		}

		public void setName(String name) {
			m_name = name;
		}
		
		@XmlAttribute(name="name")
		public String getName() {
			return m_name;
		}
	}

	public static class RockAdapter extends XmlAdapter<Rock,RockFormation> {

		@Override
		public Rock marshal(RockFormation v) throws Exception {
			Rock r = new Rock();
			r.setName(v.getName());
			r.setType(v instanceof IgneousRockFormation? "igneous":"sedimentary");
			return r;
		}

		@Override
		public RockFormation unmarshal(Rock v) throws Exception {
			RockFormation formation;
			if ("igneous".equals(v.getType())) {
				formation = new IgneousRockFormation(v.getName());
			} else {
				formation = new SedimentaryRockFormation(v.getName());
			}
			return formation;
		}
		
	}
	
	@XmlJavaTypeAdapter(RockAdapter.class)
	public static interface RockFormation {
		public String getName();
	}

	@XmlRootElement(name="rock")
	public static class Rock {
		private String m_name;
		private String m_type;
		
		public void setName(String name) {
			m_name = name;
		}
		@XmlAttribute(name="name")
		public String getName() {
			return m_name;
		}
		public void setType(String type) {
			m_type = type;
		}
		@XmlAttribute(name="type")
		public String getType() {
			return m_type;
		}
	}

	@XmlJavaTypeAdapter(RockAdapter.class)
	@XmlRootElement(name="rock")
	public static class IgneousRockFormation extends NaturalWonder implements RockFormation {
		public IgneousRockFormation() {
			
		}
		
		public IgneousRockFormation(String name) {
			setName(name);
		}
		public void inspire(String name) {
			System.err.println("the " + getName() + " rock's shinyness is blinding " + name);
		}
	}

	@XmlJavaTypeAdapter(RockAdapter.class)
	@XmlRootElement(name="rock")
	public static class SedimentaryRockFormation extends NaturalWonder implements RockFormation {
		public SedimentaryRockFormation() {
			
		}
		public SedimentaryRockFormation(String name) {
			setName(name);
		}
		public void inspire(String name) {
			System.err.println(name + " is slowly getting buried under the " + getName() + " rock formation");
		}
	}

	public static class NaturalWonder {
		private String m_name;
		
		public void inspire(String name) {
			System.err.println("the " + m_name + " natural wonder inspires awe in " + name);
		}
		
		public void setName(String name) {
			m_name = name;
		}
		
		@XmlAttribute(name="name")
		public String getName() {
			return m_name;
		}
	}

	@XmlRootElement(name="waterfall")
	public static class Waterfall extends NaturalWonder {
		private int m_height;
		
		public void inspire(String name) {
			System.err.println("the " + getName() + " waterfall is spraying " + name + " with water from a height of " + m_height + " feet!");
		}
		
		@XmlElement(name="height")
		public int getHeight() {
			return m_height;
		}
		
		public void setHeight(int height) {
			m_height = height;
		}
	}

	@XmlRootElement(name="river")
	public static class River extends NaturalWonder {
		private int m_width;

		public void inspire(String name) {
			System.err.println("pushing " + name + " into the " + m_width + "-foot wide " + getName() + " river");
		}
		
		public void setWidth(int width) {
			m_width = width;
		}
		
		@XmlAttribute(name="width")
		public int getWidth() {
			return m_width;
		}
	}

	private String m_testXml = "<canyon xmlns=\"http://xmlns.opennms.org/xsd/config/sms-sequence\" name=\"box\">" +
			"<waterfall name=\"super big huge\"><height>5280</height></waterfall>" +
			"<waterfall name=\"snakey\"><height>40</height></waterfall>" +
			"<river name=\"neuse\" width=\"200\" />" +
			"<rock type=\"igneous\" name=\"dwayne\" />" +
			"<rock type=\"sedimentary\" name=\"lightning sand\" />" +
			"</canyon>";
//	private String m_testXml = "<canyon name=\"box\"><waterfall name=\"super big huge\" /><river name=\"snakey\" /></canyon>";

	@Test
	public void testBasicStuff() throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(Canyon.class, Waterfall.class, River.class, SedimentaryRockFormation.class, IgneousRockFormation.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		Canyon c = new Canyon();
		c.setName("box");
		Waterfall w = new Waterfall();
		w.setHeight(5280);
		w.setName("super big huge");
		c.addWonder(w);
		w = new Waterfall();
		w.setHeight(40);
		w.setName("snakey");
		c.addWonder(w);
		
		River r = new River();
		r.setName("neuse");
		r.setWidth(200);
		c.addWonder(r);
		
		RockFormation formation = new SedimentaryRockFormation("lightning sand");
		c.addRockFormation(formation);
		
		formation = new IgneousRockFormation("dwayne");
		c.addRockFormation(formation);
		
		StringWriter writer = new StringWriter();
		marshaller.marshal(c, writer);
		System.err.println(writer.toString());

		c = (Canyon)unmarshaller.unmarshal(new StringReader(m_testXml));
		c.inspire("Ben");
	}

}
