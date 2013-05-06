/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.jmxconfiggenerator.webui.ui;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.opennms.core.utils.LogUtils;
import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.ViewStateChangedEvent;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

/**
 * This class provides several helper methods for ui stuff, e.g. creating a
 * button. So the amount of code is reduced generally.
 * 
 * @author Markus von Rüden
 */
public class UIHelper {

	/**
	 * Helper to create a layout (Horizontal, Vertical, Form, ...) with none, one or
	 * multiple components and allow basic configuration of the layout using
	 * fluent API.
	 * 
	 * @author Markus von Rüden
	 * 
	 */
	public static class LayoutCreator {

		private Class<?> layoutType = null;
		private List<Component> components = new ArrayList<Component>();
		private boolean spacing = false;

		public LayoutCreator withComponents(Component... components) {
			this.components.addAll(Arrays.asList(components));
			return this;
		}

		/**
		 * Create a Horizontal Layout.
		 * @return this.
		 */
		public LayoutCreator setHorizontal() {
			this.layoutType = HorizontalLayout.class;
			return this;
		}

		/**
		 * Create a Vertical Layout.
		 * @return this.
		 */
		public LayoutCreator setVertical() {
			this.layoutType = VerticalLayout.class;
			return this;
		}

		/**
		 * Create a Form.
		 * @return this.
		 */
		public LayoutCreator setForm() {
			this.layoutType = FormLayout.class;
			return this;
		}

		/**
		 * Add the component to the layout during creation.
		 * @param component
		 * @return
		 */
		public LayoutCreator withComponent(Component component) {
			this.components.add(component);
			return this;
		}

		public AbstractOrderedLayout toLayout() {
			AbstractOrderedLayout layout = createLayout(layoutType, components);
			layout.setSpacing(spacing);
			return layout;
		}

		public LayoutCreator withSpacing() {
			spacing = true;
			return this;
		}

		public LayoutCreator withoutSpacing() {
			spacing = false;
			return this;
		}

		private static AbstractOrderedLayout createLayout(Class<?> clazz, List<Component> components) {
			try {
				AbstractOrderedLayout layout = (AbstractOrderedLayout) clazz.newInstance();
				for (Component c : components)
					layout.addComponent(c);
				return layout;
			} catch (InstantiationException ex) {
				;
			} catch (IllegalAccessException ex) {
				;
			}
			return null;
		}
	}

	public static Button createButton(final String buttonCaption, final String iconName,
			final ClickListener clickListener) {
		Button button = new Button();
		button.setCaption(buttonCaption);
		button.setIcon(IconProvider.getIcon(iconName));
		if (clickListener != null) button.addListener(clickListener);
		return button;
	}

	public static Button createButton(final String buttonCaption, final String iconName) {
		return createButton(buttonCaption, iconName, null);
	}

	/**
	 * This method enables or disables all tabs in a tabSheet. Therefore the
	 * <code>ViewStatechangedEvent</code> is used. If the new view state is Edit
	 * the method returns the last selected tab position. If the new view state
	 * is not Edit the " <code>oldSelectedTabPosition</code>" is selected in the
	 * given <code>tabSheet</code>.
	 * 
	 * @param tabSheet
	 *            the tabsheet to enable or disable all tabs in
	 * @param event
	 * @param oldSelectedTabPosition
	 *            which tab was selected before view state was Edit
	 * @return
	 */
	public static int enableTabs(final TabSheet tabSheet, final ViewStateChangedEvent event,
			final int oldSelectedTabPosition) {
		boolean editMode = event.getNewState().isEdit();
		boolean enabled = !editMode;
		int newSelectedTabPosition = 0;
		// remember which tab was selected (before editing)
		if (editMode) newSelectedTabPosition = getSelectedTabPosition(tabSheet);
		// disable or enable
		for (int i = 0; i < tabSheet.getComponentCount(); i++)
			tabSheet.getTab(i).setEnabled(enabled);
		// select tab depending on selection (after editing)
		if (!editMode) tabSheet.setSelectedTab(tabSheet.getTab(oldSelectedTabPosition));
		// return currently selected tab
		return editMode ? newSelectedTabPosition : getSelectedTabPosition(tabSheet);
	}

	private static int getSelectedTabPosition(final TabSheet tabSheet) {
		if (tabSheet == null) return 0;
		if (tabSheet.getSelectedTab() == null) return 0;
		if (tabSheet.getTab(tabSheet.getSelectedTab()) == null) return 0;
		return tabSheet.getTabPosition(tabSheet.getTab(tabSheet.getSelectedTab()));
	}

	/**
	 * Closes the given Closeable silently. That means if an error during
	 * {@link Closeable#close()} occurs, the IOException is catched and logged.
	 * No further information is forwarded.
	 */
	public static final void closeSilently(Closeable closeable) {
		if (closeable == null) return; // prevent NPE
		try {
			closeable.close();
		} catch (IOException e) {
			LogUtils.warnf(UIHelper.class, e, "Error while closing resource '%s'.", closeable);
		}
	}

	/**
	 * Loads the <code>resourceName</code> from the classpath using the given
	 * <code>clazz</code>. If the resource couldn't be loaded an empty string is
	 * returned.
	 * 
	 * @param clazz
	 *            The class to use for loading the resource.
	 * @param resourceName
	 *            The name of the resource to be loaded (e.g.
	 *            /folder/filename.txt)
	 * @return The content of the file, each line separated by line.separator or
	 *         empty string if the resource does not exist.
	 */
	public static String loadContentFromFile(final Class<?> clazz, final String resourceName) {
		// prevent NullPointerException
		if (clazz == null || resourceName == null) {
			LogUtils.warnf(UIHelper.class, "loadContentFromFile not invoked, due to null arguments");
			return "";
		}

		// check if resource is there
		final InputStream is = clazz.getResourceAsStream(resourceName);
		if (is == null) {
			LogUtils.warnf(UIHelper.class, "Resource '%s' couldn't be loaded from class '%s'", resourceName,
					clazz.getName());
			return "";
		}

		// resource is there, so we can try loading it
		BufferedReader bufferedReader = null;
		StringBuilder result = new StringBuilder(100);
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(is));
			String eachLine = null;
			while ((eachLine = bufferedReader.readLine()) != null) {
				result.append(eachLine);
				result.append(System.getProperty("line.separator"));
			}
		} catch (IOException ioEx) {
			LogUtils.errorf(clazz, ioEx, "Error while reading resource from '%s'.", resourceName);
		} finally {
			closeSilently(bufferedReader);
		}
		return result.toString();
	}

	/**
	 * Shows a validation error to the user.
	 * 
	 * @param errorMessage
	 *            the error message.
	 */
	public static void showValidationError(Window window, String errorMessage) {
		if (window == null) return;
		window.showNotification("Validation error.", errorMessage != null ? errorMessage : "An unknown error occured.",
				Notification.TYPE_WARNING_MESSAGE);

	}
}
