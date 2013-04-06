package org.opennms.netmgt.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class OnmsGeolocationTest {
    private OnmsGeolocation m_geolocation;
    private String m_expectedAddress;

    public OnmsGeolocationTest(final String address1, final String address2, final String city, final String state, final String zip, final String country, final String expectedAddress) {
        m_geolocation = new OnmsGeolocation();
        m_geolocation.setAddress1(address1);
        m_geolocation.setAddress2(address2);
        m_geolocation.setCity(city);
        m_geolocation.setState(state);
        m_geolocation.setZip(zip);
        m_geolocation.setCountry(country);
        m_expectedAddress = expectedAddress;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {
                "220 Chatham Business Dr",
                null,
                "Pittsboro",
                "NC",
                "27312",
                "USA",
                "220 Chatham Business Dr, Pittsboro, NC 27312, USA"
            },
            {
                "220 Chatham Business Dr",
                null,
                "Pittsboro",
                "NC",
                "27312",
                null,
                "220 Chatham Business Dr, Pittsboro, NC 27312"
            },
            {
                "220 Chatham Business Dr",
                null,
                "Pittsboro",
                "NC",
                null,
                null,
                "220 Chatham Business Dr, Pittsboro, NC"
            },
            {
                "220 Chatham Business Dr",
                null,
                "Pittsboro",
                null,
                "27312",
                null,
                "220 Chatham Business Dr, Pittsboro, 27312"
            },
            {
                null,
                null,
                null,
                null,
                "PR7 3JE",
                "UK",
                "PR7 3JE, UK"
            },
        });
    }

    @Test
    public void testUSAddress() {
        assertEquals(m_expectedAddress, m_geolocation.asAddressString());
    }
}
