package org.opennms.model.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class AssetsUpdaterTest {

	@Test
	public void validateProps() {
		
		Properties props = new Properties();

		props.setProperty(AssetsUpdater.PROPERTY_FIELD_PREFIX+"1", "address1");
		props.setProperty(AssetsUpdater.PROPERTY_FIELD_PREFIX+"22z", "country");
		props.setProperty(AssetsUpdater.PROPERTY_FIELD_PREFIX+"777", "country");
		props.setProperty(AssetsUpdater.PROPERTY_FIELD_PREFIX+"2", "city");
		props.setProperty(AssetsUpdater.PROPERTY_FIELD_PREFIX+"3", "state");
		props.setProperty(AssetsUpdater.PROPERTY_FIELD_PREFIX+"10", "zip");
		props.setProperty(AssetsUpdater.PROPERTY_FIELD_PREFIX+"1z", "country");
		
		try {
		    AssetsUpdater.createCsvFileMappingFromProperties(props);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail("Validation of properties failed.");
		}
		
		
	}
	
	@Test
	public void testRegEx() {
		//String regex = "^"+AddressUpdater.PROPERTY_FIELD_PREFIX +"([0-9]+)=.*";
		String regex = "^"+AssetsUpdater.PROPERTY_FIELD_PREFIX +"([0-9]+)$";
		//regex = "^field([0-9]+).*";
		Pattern pattern = Pattern.compile(regex);
		String testString = "field2";
		Matcher m = pattern.matcher(testString);
		assertTrue(m.matches());
		System.out.println(m.group(1));

	}

}
