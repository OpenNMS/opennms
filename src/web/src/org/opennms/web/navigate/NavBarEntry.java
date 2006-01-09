package org.opennms.web.navigate;

public class NavBarEntry {
        private String m_locationMatch;
        private String m_URL;
        private String m_name;

        public NavBarEntry(String locationMatch, String URL, String name) {
                m_locationMatch = locationMatch;
                m_URL = URL;
                m_name = name;
        }

        public String getLocationMatch() {
                return m_locationMatch;
        }

        public String getURL() {
                return m_URL;
        }

        public String getName() {
                return m_name;
        }

        public boolean isMatchingLocation(String locationMatch) {
                return m_locationMatch.equals(locationMatch);
        }
}
