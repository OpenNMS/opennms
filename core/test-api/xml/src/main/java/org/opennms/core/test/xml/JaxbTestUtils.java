package org.opennms.core.test.xml;

import org.junit.Assert;
import org.opennms.core.xml.JaxbUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;


public class JaxbTestUtils {

        private static final Logger LOG = LoggerFactory.getLogger(JaxbTestUtils.class);

        /**
         * This method verifies that all classes in the given package annotated with {@link javax.xml.bind.annotation.XmlRootElement}
         * are handled with Eclipse Moxy JAXB implementation instead of the default one.
         */
        public static void verifyJaxbContext(final String basePackage) throws JAXBException {
            final Reflections reflections = new Reflections(basePackage);
            final Set<Class<?>> allClasses = reflections.getTypesAnnotatedWith(XmlRootElement.class);
            final ArrayList<Class<?>> sortedClasses = new ArrayList<>(allClasses);

            if (sortedClasses.isEmpty()) {
                Assert.fail("No classes annotated with @XmlRootElement found in package " + basePackage);
            }

            Collections.sort(sortedClasses, new Comparator<Class<?>>() {
                @Override
                public int compare(Class<?> o1, Class<?> o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            for (Class<?> eachClass : sortedClasses) {
                if (eachClass.isAnonymousClass()) {
                    LOG.warn("Class {} is anonymous, cannot be verified, please verify manually. Skipping.", eachClass);
                    continue;
                }
                verifyJaxbContext(eachClass);
            }
        }

        public static void verifyJaxbContext(final Class clazz) throws JAXBException {
            LOG.info("Verifying JAXBContext for class {} ...", clazz);
            Objects.requireNonNull(clazz);

            final JAXBContext defaultJaxbContext = JAXBContext.newInstance(clazz);
            final JAXBContext jaxbUtilsContext = JaxbUtils.getContextFor(clazz);

            Assert.assertNotNull("JAXBContext.newInstance returned null.", defaultJaxbContext);
            Assert.assertNotNull("JaxbUtils.getContextFor returned null.", defaultJaxbContext);

            final String defaultJaxbContextClass = defaultJaxbContext.getClass().getName();
            final URL defaultJaxbSourceLocation = getSourceLocation(defaultJaxbContext);
            final String jaxbUtilsJaxbContextClass =  jaxbUtilsContext.getClass().getName();
            final URL jaxbUtilsJaxbSourceLocation = getSourceLocation(jaxbUtilsContext);

            LOG.info("Default JAXBContext {} for class {}", defaultJaxbContextClass, clazz);
            LOG.info("Default JAXBContext source location: {}", defaultJaxbSourceLocation);
            LOG.info("JaxbUtils JAXBContext {} for class {}", jaxbUtilsJaxbContextClass, clazz);
            LOG.info("JaxbUtils JAXBContext source location: {}", jaxbUtilsJaxbSourceLocation);

            Assert.assertEquals("org.eclipse.persistence.jaxb.JAXBContext", defaultJaxbContextClass);
            Assert.assertEquals("org.eclipse.persistence.jaxb.JAXBContext", jaxbUtilsJaxbContextClass);
            Assert.assertEquals(defaultJaxbContextClass, jaxbUtilsJaxbContextClass);
            Assert.assertEquals(defaultJaxbSourceLocation, jaxbUtilsJaxbSourceLocation);
        }

    // Sometimes the source code location is null, to prevent NPE we encapsulate the
    // get functionality here.
    private static <T> URL getSourceLocation(T object) {
        if (object != null
                && object.getClass().getProtectionDomain() != null
                && object.getClass().getProtectionDomain().getCodeSource() != null) {
            return object.getClass().getProtectionDomain().getCodeSource().getLocation();
        }
        return null;
    }

}
