package org.opennms.features.topology.plugins.topo.asset.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;
import org.opennms.features.topology.plugins.topo.asset.repo.NodeInfoRepository;
import org.opennms.features.topology.plugins.topo.asset.repo.StringMapFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * TEST  filter name value pair
 * filter = null do not filter results
 * key1=value1&key1=value2 (additive i.e. OR of key 1 values
 * key1=value1,value2,value3 (additive i.e. OR of key 1 values) alternative)
 * key1=value1,key2=value2,value3 etc (AND of keys - both must apply)
 * key1=!value1 (negation - key NOT value1)
 * key1=~regex (regex match of key) or key1=!~regex
 */
public class StringMapFilterTest {
	private static final Logger LOG = LoggerFactory.getLogger(StringMapFilterTest.class);

	/**
	 * test mock map works OK
	 */
	@Test
	public void testMockMap() {
		LOG.debug("Start of testMockMap");
		Map<String, Map<String, String>> m = createMockMap();
		String s = stringMapToString(m);
		LOG.debug(s);
		LOG.debug("End of testMockMap");
	}

	/**
	 * test full map returned if filter is null
	 */
	@Test
	public void testFilter1() {
		LOG.debug("Start of testFilter1()");
		List<String> filter=null;
		Map<String, Map<String, String>> m = createMockMap();
		String expected = stringMapToString(m);

		Map<String, Map<String, String>> newMap;
		newMap = new StringMapFilter(m).filter(filter);
		String actual=stringMapToString(newMap);
		//		LOG.debug("\nFilter:"+setToString(filter));
		//		LOG.debug("\nExpected Result:"+expected);
		//		LOG.debug("\nActual Result:"+actual);

		assertEquals(expected,actual);

		LOG.debug("End of testFilter1()");

	}

	/**
	 * test empty map returned if filter is empty
	 */
	@Test
	public void testFilter2() {
		LOG.debug("Start of testFilter2()");
		List<String> filter=new ArrayList<String>();
		String expected = stringMapToString(new LinkedHashMap<String, Map<String, String>>());

		Map<String, Map<String, String>> m = createMockMap();
		Map<String, Map<String, String>> newMap;
		newMap = new StringMapFilter(m).filter(filter);

		String actual=stringMapToString(newMap);

//		LOG.debug("\nFilter:"+setToString(filter));
//		LOG.debug("\nExpected Result:"+expected);
//		LOG.debug("\nActual Result:"+actual);

		assertEquals(expected,actual);

		LOG.debug("End of testFilter2()");

	}


	/**
	 * test filter incorrectly formatted; no '='
	 */
	@Test
	public void testFilter3() {
		LOG.debug("Start of testFilter3()");
		List<String> filter=Arrays.asList("","");

		boolean isException=false;

		try{

			Map<String, Map<String, String>> m = createMockMap();
			Map<String, Map<String, String>> newMap;
			newMap = new StringMapFilter(m).filter(filter);

		} catch(Exception e){
			isException=true;
			LOG.debug("expected exception:"+e.getMessage());
		}

		assertEquals(true,isException);

		LOG.debug("End of testFilter3()");

	}

	/**
	 * test filter incorrectly formatted; key contains ','
	 */
	@Test
	public void testFilter4() {
		LOG.debug("Start of testFilter4()");
		List<String> filter=Arrays.asList("aaa=bbb","a,b,c=aaa");

		boolean isException=false;

		try{

			Map<String, Map<String, String>> m = createMockMap();
			Map<String, Map<String, String>> newMap;
			newMap = new StringMapFilter(m).filter(filter);

		} catch(Exception e){
			isException=true;
			LOG.debug("expected exception:"+e.getMessage());
		}

		assertEquals(true,isException);

		LOG.debug("End of testFilter4()");

	}

	/**
	 * test filter incorrectly formatted; value contains only '!'
	 */
	@Test
	public void testFilter4b() {
		LOG.debug("Start of testFilter4b()");
		List<String> filter=Arrays.asList("key1=outerMap_0_value1,outerMap_4_value1","key2=!");

		boolean isException=false;

		try{

			Map<String, Map<String, String>> m = createMockMap();
			Map<String, Map<String, String>> newMap;
			newMap = new StringMapFilter(m).filter(filter);

		} catch(Exception e){
			isException=true;
			LOG.debug("expected exception:"+e.getMessage());
		}

		assertEquals(true,isException);

		LOG.debug("End of testFilter4b()");

	}

	/**
	 * test filter incorrectly formatted; several '='
	 */
	@Test
	public void testFilter5() {
		LOG.debug("Start of testFilter5()");
		List<String> filter=Arrays.asList("a=aa=bbb","c=aaa");

		boolean isException=false;

		try{

			Map<String, Map<String, String>> m = createMockMap();
			Map<String, Map<String, String>> newMap;
			newMap = new StringMapFilter(m).filter(filter);

		} catch(Exception e){
			isException=true;
			LOG.debug("expected exception:"+e.getMessage());
		}

		assertEquals(true,isException);

		LOG.debug("End of testFilter5()");

	}

	/**
	 * check all values returned with matching key value pair
	 * key5=value5
	 * 
	 */
	@Test
	public void testFilter6() {
		LOG.debug("Start of testFilter6()");
		List<String> filter=Arrays.asList("key5=value5");

		String expected = "List<String>[outerMap_0,outerMap_1,outerMap_2,outerMap_3,outerMap_4]";

		Map<String, Map<String, String>> m = createMockMap();

		Map<String, Map<String, String>> newMap;

		newMap = new StringMapFilter(m).filter(filter);

		String actual=setToString(newMap.keySet());

//		LOG.debug("\nFilter:"+setToString(filter));
//		LOG.debug("\nExpected Result:"+expected);
//		LOG.debug("\nActual Result:"+actual);

		assertEquals(expected,actual);

		LOG.debug("End of testFilter6()");

	}

	/**
	 * check value omitted if negation !
	 * key5=value5
	 * key2=!outerMap_1_value2
	 */
	@Test
	public void testFilter7() {
		LOG.debug("Start of testFilter7()");
		List<String> filter=Arrays.asList("key5=value5","key2=!outerMap_1_value2");

		String expected = "List<String>[outerMap_0,outerMap_2,outerMap_3,outerMap_4]";

		Map<String, Map<String, String>> m = createMockMap();

		Map<String, Map<String, String>> newMap;

		newMap = new StringMapFilter(m).filter(filter);

		String actual=setToString(newMap.keySet());

//		LOG.debug("\nFilter:"+setToString(filter));
//		LOG.debug("\nExpected Result:"+expected);
//		LOG.debug("\nActual Result:"+actual);

		assertEquals(expected,actual);

		LOG.debug("End of testFilter7()");

	}

	/**
	 * check comma separated values work
	 * key1=outerMap_0_value1,outerMap_4_value1
	 * 
	 */
	@Test
	public void testFilter8() {
		LOG.debug("Start of testFilter8()");
		List<String> filter=Arrays.asList("key1=outerMap_0_value1,outerMap_4_value1");

		String expected = "List<String>[outerMap_0,outerMap_4]";

		Map<String, Map<String, String>> m = createMockMap();

		Map<String, Map<String, String>> newMap;

		newMap = new StringMapFilter(m).filter(filter);

		String actual=setToString(newMap.keySet());

//		LOG.debug("\nFilter:"+setToString(filter));
//		LOG.debug("\nExpected Result:"+expected);
//		LOG.debug("\nActual Result:"+actual);

		assertEquals(expected,actual);

		LOG.debug("End of testFilter8()");

	}
	
	/**
	 * check AND works
	 * key1=outerMap_0_value1,outerMap_1_value1","key5=value5","key2=outerMap_1_value2"
	 * 
	 */
	@Test
	public void testFilter9() {
		LOG.debug("Start of testFilter9()");
		List<String> filter=Arrays.asList("key1=outerMap_0_value1,outerMap_1_value1","key5=value5","key2=outerMap_1_value2");

		String expected = "List<String>[outerMap_1]";

		Map<String, Map<String, String>> m = createMockMap();

		Map<String, Map<String, String>> newMap;

		newMap = new StringMapFilter(m).filter(filter);

		String actual=setToString(newMap.keySet());

//		LOG.debug("\nFilter:"+setToString(filter));
//		LOG.debug("\nExpected Result:"+expected);
//		LOG.debug("\nActual Result:"+actual);

		assertEquals(expected,actual);

		LOG.debug("End of testFilter9()");

	}
	
	/**
	 * check AND with negation works
	 * key1=outerMap_0_value1,outerMap_1_value1","key5=value5","key2=!outerMap_1_value2"
	 * 
	 */
	@Test
	public void testFilter10() {
		LOG.debug("Start of testFilter10()");
		List<String> filter=Arrays.asList("key1=outerMap_0_value1,outerMap_1_value1","key5=value5","key2=!outerMap_1_value2");

		String expected = "List<String>[outerMap_0]";

		Map<String, Map<String, String>> m = createMockMap();

		Map<String, Map<String, String>> newMap;

		newMap = new StringMapFilter(m).filter(filter);

		String actual=setToString(newMap.keySet());

//		LOG.debug("\nFilter:"+setToString(filter));
//		LOG.debug("\nExpected Result:"+expected);
//		LOG.debug("\nActual Result:"+actual);

		assertEquals(expected,actual);

		LOG.debug("End of testFilter9()");

	}


	/* *************************
	 * UTILITY METHODS FOR TESTS
	 * *************************
	 */

	/**
	 * Create mock map with format resolving to
	 *  Map<String,Map<String,String>>
	 *  [outerMap_0=[key1=outerMap_0_value1,key2=outerMap_0_value2,key3=outerMap_0_value3,key4=outerMap_0_value4,key5=value5]
	 *	 outerMap_1=[key1=outerMap_1_value1,key2=outerMap_1_value2,key3=outerMap_1_value3,key4=outerMap_1_value4,key5=value5]
	 *	 outerMap_2=[key1=outerMap_2_value1,key2=outerMap_2_value2,key3=outerMap_2_value3,key4=outerMap_2_value4,key5=value5]
	 *	 outerMap_3=[key1=outerMap_3_value1,key2=outerMap_3_value2,key3=outerMap_3_value3,key4=outerMap_3_value4,key5=value5]
	 *	 outerMap_4=[key1=outerMap_4_value1,key2=outerMap_4_value2,key3=outerMap_4_value3,key4=outerMap_4_value4,key5=value5]
	 *	]
	 */
	public Map<String,Map<String,String>> createMockMap(){

		Map<String,Map<String,String>> mockMap = new  LinkedHashMap<String,Map<String,String>>();

		for(int i=0; i<5; i++ ){
			String valPrefix ="outerMap_"+Integer.toString(i);
			mockMap.put(valPrefix, createMockValues(valPrefix));
		}

		return mockMap;
	}

	public Map<String,String> createMockValues(String valPrefix){
		Map<String,String> stringMap= new LinkedHashMap<String,String>();

		stringMap.put("key1",valPrefix+ "_value1");
		stringMap.put("key2",valPrefix+ "_value2");
		stringMap.put("key3",valPrefix+ "_value3");
		stringMap.put("key4",valPrefix+ "_value4");
		stringMap.put("key5","value5");

		return stringMap;
	}

	//	public String stringMapKeysString(Map<String,Map<String,String>> mockMap){
	//		StringBuffer s= new StringBuffer("Keys[");
	//		for(String key: mockMap.keySet()){
	//			s.append(key+",");
	//			Map<String, String> innerMap = mockMap.get(key);
	//			for(String innerKey: innerMap.keySet()){
	//				s.append(innerKey+"="+innerMap.get(innerKey)+",");
	//			}
	//			if (innerMap.size()!=0) s.deleteCharAt(s.lastIndexOf(","));
	//			s.append("]\n");
	//		}
	//		s.append("]\n");
	//		return s.toString();
	//	}

	public String stringMapToString(Map<String,Map<String,String>> mockMap){
		StringBuffer s= new StringBuffer("Map<String,Map<String,String>>[");

		for(String key: mockMap.keySet()){
			s.append(key+"=[");
			Map<String, String> innerMap = mockMap.get(key);
			for(String innerKey: innerMap.keySet()){
				s.append(innerKey+"="+innerMap.get(innerKey)+",");
			}
			if (innerMap.size()!=0) s.deleteCharAt(s.lastIndexOf(","));
			s.append("]\n");
		}
		s.append("]");
		return s.toString();	
	}

	public String setToString(Collection<String> list){
		StringBuffer s= new StringBuffer();

		if (list==null){
			s.append("null");
		} else {
			s.append("List<String>[");
			for(String value :list){
				s.append(value+",");
			}
			if (list.size()!=0) s.deleteCharAt(s.lastIndexOf(","));
			s.append("]");
		}
		return s.toString();
	}

}
