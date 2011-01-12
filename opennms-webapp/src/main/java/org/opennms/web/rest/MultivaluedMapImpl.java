package org.opennms.web.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

/**
 * <p>MultivaluedMapImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class MultivaluedMapImpl extends HashMap<String, List<String>> implements MultivaluedMap<String, String> {

    private static final long serialVersionUID = 8520828454808579795L;

    public MultivaluedMapImpl() {
        super();
    }

    /** 
	 * This constructor can be used as a convenience method to create populated
	 * {@link MultivaluedMapImpl} instances.
	 */
	public MultivaluedMapImpl(String[][] keyValuePairs) {
	    super();
	    for (String[] keyValuePair : keyValuePairs) {
	        this.add(keyValuePair[0], keyValuePair[1]);
	    }
	}

	/**
	 * <p>add</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 */
	public void add(String key, String value) {
		List<String> valueList=super.get(key);
		if(valueList==null) {
			valueList=new ArrayList<String>();
			super.put(key, valueList);
		}
		valueList.add(value);
	}

	/**
	 * <p>getFirst</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getFirst(String key) {
		List<String> values=super.get(key);
		if(values.size()==0) {
			return null;
		}
		return values.get(0);
	}

	/**
	 * <p>putSingle</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 */
	public void putSingle(String key, String value) {
		List<String> list=new ArrayList<String>();
		list.add(value);
		super.put(key, list);
	}
	
	/**
	 * <p>put</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param values a {@link java.lang.String} object.
	 */
	public void put(String key, String...values){
	    put(key, new ArrayList<String>(Arrays.asList(values)));
	}

}
