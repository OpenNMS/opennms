package org.opennms.netmgt.model.ncs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;

@XmlTransient
public class JAXBMapAdapter extends XmlAdapter<JAXBMapAdapter.JAXBMap, Map<String,String>> {
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "attributes")
	public static class JAXBMap {
	    @XmlElement(name = "attribute", required = true)
	    private final List<JAXBMapEntry> a = new ArrayList<JAXBMapEntry>();
	    public List<JAXBMapEntry> getA() {
	        return this.a;
	    }
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "attribute")
	public static class JAXBMapEntry {

	    @XmlElement(name = "key", required = true)
	    private final String key;

	    @XmlElement(name = "value", required = true)
	    private final String value;

	    public JAXBMapEntry(String key, String value) {
	        this.key = key;
	        this.value = value;
	    }

	    public JAXBMapEntry() {
	        this.key = null;
	        this.value = null;
	    }

	    public String getKey() {
	        return key;
	    }

	    public String getValue() {
	        return value;
	    }
	}

    @Override
    public JAXBMap marshal(Map<String,String> v) throws Exception {
    	if (v.isEmpty()) return null;
        JAXBMap myMap = new JAXBMap();
        List<JAXBMapEntry> aList = myMap.getA();
        for ( Map.Entry<String,String> e : v.entrySet() ) {
            aList.add(new JAXBMapEntry(e.getKey(), e.getValue()));
        }
        return myMap;
    }

    @Override
    public Map<String,String> unmarshal(JAXBMap v) throws Exception {
        Map<String,String> map = new LinkedHashMap<String,String>();
        for ( JAXBMapEntry e : v.getA() ) {
            map.put(e.getKey(), e.getValue());
        }
        return map;
    }
}