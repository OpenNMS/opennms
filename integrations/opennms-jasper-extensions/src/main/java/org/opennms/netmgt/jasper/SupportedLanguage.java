package org.opennms.netmgt.jasper;

import net.sf.jasperreports.engine.query.QueryExecuterFactory;
import org.opennms.netmgt.jasper.measurement.MeasurementExecutorFactory;

/**
 * These are the supported "query languages" to be used within Jasper Report (*.jrxml) files.
 */
public enum SupportedLanguage {
    Measurement(new MeasurementExecutorFactory());

    private final QueryExecuterFactory factory;

    private SupportedLanguage(QueryExecuterFactory factory) {
        this.factory = factory;
    }

    public QueryExecuterFactory getExecutorFactory() {
        return factory;
    }

    public static String[] names() {
        final SupportedLanguage[] supportedLanguages = SupportedLanguage.values();
        final String[] supportedLanguagesNames = new String[supportedLanguages.length];
        for (int i=0; i<supportedLanguages.length; i++) {
            supportedLanguagesNames[i] = supportedLanguages[i].name();
        }
        return supportedLanguagesNames;
    }

    public static SupportedLanguage createFrom(String language) {
        for (SupportedLanguage supportedLanguage : SupportedLanguage.values()) {
            if (supportedLanguage.name().equalsIgnoreCase(language)) {
                return supportedLanguage;
            }
        }
        return null;
    }
}
