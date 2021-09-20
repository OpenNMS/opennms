/*
 * Copyright 2012 Achim Nierbeck.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opennms.vaadin.extender.internal.extender;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServlet;

import org.opennms.vaadin.extender.AbstractApplicationFactory;
import org.opennms.vaadin.extender.VaadinResourceService;
import org.opennms.vaadin.extender.internal.servlet.OSGiUIProvider;
import org.opennms.vaadin.extender.internal.servlet.VaadinOSGiServlet;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;

public class PaxVaadinBundleTracker extends BundleTracker<Object> {

    private static class ApplicationFactoryWrapper extends AbstractApplicationFactory {

        private UI m_application;

        public ApplicationFactoryWrapper(UI application) {
            m_application = application;
        }

        @Override
        public Class<? extends UI> getUIClass() {
            return m_application.getClass();
        }

        @Override
        public UI createUI() {
            return m_application;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(PaxVaadinBundleTracker.class);

    private final Logger logger = LoggerFactory.getLogger(PaxVaadinBundleTracker.class.getName());

	private final Map<Bundle, ServiceRegistration<?>> registeredServlets = new HashMap<Bundle, ServiceRegistration<?>>();

	public PaxVaadinBundleTracker(BundleContext context) {
		super(context, Bundle.ACTIVE, null);
	}

	@Override
	public Object addingBundle(Bundle bundle, BundleEvent event) {

		if (isApplicationBundle(bundle)) {
			logger.debug("found a vaadin-app bundle: {}", bundle);
			String applicationClass = (String) bundle.getHeaders().get(
					org.opennms.vaadin.extender.Constants.VAADIN_APPLICATION);
			String alias = (String) bundle.getHeaders().get("Vaadin-Alias");
			UI application = null;
			try {
				Class<?> appClazz = bundle.loadClass(applicationClass);

				Constructor<?>[] ctors = appClazz.getDeclaredConstructors();
				Constructor<?> ctor = null;
				for (int i = 0; i < ctors.length; i++) {
					ctor = ctors[i];
					if (ctor.getGenericParameterTypes().length == 0)
						break;
				}
				ctor.setAccessible(true);
				application = (UI) ctor.newInstance();

			} catch (ClassNotFoundException e) {
				LOG.error("Could not add bundle: ", e);
			} catch (SecurityException e) {
                LOG.error("Could not add bundle: ", e);
			} catch (IllegalArgumentException e) {
                LOG.error("Could not add bundle: ", e);
			} catch (InstantiationException e) {
                LOG.error("Could not add bundle: ", e);
			} catch (IllegalAccessException e) {
                LOG.error("Could not add bundle: ", e);
			} catch (InvocationTargetException e) {
                LOG.error("Could not add bundle: ", e);
			}

			final String widgetset = findWidgetset(bundle);
			if (application != null) {
				final ApplicationFactoryWrapper applicationFactoryWrapper = new ApplicationFactoryWrapper(application);
				final UIProvider uiProvider = new OSGiUIProvider(applicationFactoryWrapper);
				VaadinOSGiServlet servlet = new VaadinOSGiServlet(uiProvider, bundle.getBundleContext());

				Map<String, Object> props = new Hashtable<String, Object>();
				props.put(org.opennms.vaadin.extender.Constants.ALIAS, alias);
				props.put(org.opennms.vaadin.extender.Constants.OSGI_HTTP_WHITEBOARD_SERVLET_PATTERN, alias);
				props.put("servlet.init.ui.class", applicationFactoryWrapper.getUIClass().getCanonicalName());

				if (widgetset != null) {
					props.put("servlet.init.widgetset", widgetset);
				}

				@SuppressWarnings({"unchecked"})
				ServiceRegistration<?> registeredServlet = bundle
						.getBundleContext().registerService(
								HttpServlet.class.getName(), servlet,
								(Dictionary<String,?>) props);

				registeredServlets.put(bundle, registeredServlet);
			}

		}

		if (isThemeBundle(bundle)) {
			logger.debug("found a vaadin-resource bundle: {}", bundle);
			// TODO do VAADIN Themes handling
			ServiceReference<?> serviceReference = bundle.getBundleContext().getServiceReference(VaadinResourceService.class.getName());
			VaadinResourceService service = (VaadinResourceService) bundle.getBundleContext().getService(serviceReference);
			service.addResources(bundle);
		}

		return super.addingBundle(bundle, event);
	}

	protected static String findWidgetset(Bundle bundle) {
		Enumeration<URL> widgetEntries = bundle.findEntries("", "*.gwt.xml", true);
//		Enumeration widgetEntries = bundle.getEntryPaths(VAADIN_PATH);
		if (widgetEntries == null || !widgetEntries.hasMoreElements())
			return null;

		URL widgetUrl = widgetEntries.nextElement();
		String path = widgetUrl.getPath();
		path = path.substring(1,path.length()-8);
		path = path.replace("/", ".");
		return path;
	}

	@Override
	public void removedBundle(Bundle bundle, BundleEvent event, Object object) {

		ServiceRegistration<?> registeredServlet = registeredServlets.get(bundle);
		if (registeredServlet != null)
			registeredServlet.unregister();

		super.removedBundle(bundle, event, object);
	}

	private boolean isApplicationBundle(Bundle bundle) {
		if (!isVaadinBundle(bundle))
			return false;

		String applicationClass = (String) bundle.getHeaders().get(
				org.opennms.vaadin.extender.Constants.VAADIN_APPLICATION);

		if (applicationClass != null && !applicationClass.isEmpty())
			return true;

		return false;
	}

	private boolean isThemeBundle(Bundle bundle) {
		if ("com.vaadin".equals(bundle.getSymbolicName()))
			return false;

		Enumeration<?> vaadinPaths = bundle.getEntryPaths(org.opennms.vaadin.extender.Constants.VAADIN_PATH);
		if (vaadinPaths == null || !vaadinPaths.hasMoreElements())
			return false;

		return true;
	}

	private boolean isVaadinBundle(Bundle bundle) {
		String importedPackages = (String) bundle.getHeaders().get(
				Constants.IMPORT_PACKAGE);
		if (importedPackages == null) {
			return false;
		}

		if (importedPackages.contains("com.vaadin")) {
			return true;
		}

		return false;
	}
}
