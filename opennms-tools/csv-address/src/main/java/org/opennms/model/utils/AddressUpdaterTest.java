package org.opennms.model.utils;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class AddressUpdaterTest {

	@Test
	public void validateProps() {
		
		Properties props = new Properties();

		props.setProperty(AddressUpdater.PROPERTY_FIELD_PREFIX+"1z", "address1");
		props.setProperty(AddressUpdater.PROPERTY_FIELD_PREFIX+"2", "city");
		props.setProperty(AddressUpdater.PROPERTY_FIELD_PREFIX+"3", "state");
		props.setProperty(AddressUpdater.PROPERTY_FIELD_PREFIX+"10", "zip");
		props.setProperty(AddressUpdater.PROPERTY_FIELD_PREFIX+"1z", "country");
		
		try {
		    AddressUpdater.validateProperties(props);
		} catch (FileNotFoundException e) {
			//this is bad but works ;)
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail("Validation of properties failed.");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Validation of properties failed.");
		}
		
		
	}
	
	@Test
	public void testRegEx() {
		String regex = "^"+AddressUpdater.PROPERTY_FIELD_PREFIX +"([0-9]+)=.*";
		//regex = "^field([0-9]+).*";
		Pattern pattern = Pattern.compile(regex);
		String testString = "field2=address1";
		Matcher m = pattern.matcher(testString);
		assertTrue(m.matches());
		System.out.println(m.group(1));

	}

}
