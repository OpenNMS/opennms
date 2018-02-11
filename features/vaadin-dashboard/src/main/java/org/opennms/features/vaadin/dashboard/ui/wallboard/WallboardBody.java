/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.dashboard.ui.wallboard;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.*;
import org.opennms.features.vaadin.dashboard.config.DashletSelector;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletSelectorAccess;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;

import java.util.*;

public class WallboardBody extends VerticalLayout {
    private final CssLayout contentLayout;
    private List<DashletSpec> dashletSpecs = new LinkedList<>();
    private Map<Integer, Dashlet> dashlets = new HashMap<Integer, Dashlet>();
    private Map<Integer, Integer> priorityMap = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> durationMap = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> oldDurationMap = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> oldPriorityMap = new HashMap<Integer, Integer>();
    private Timer timer;
    private int waitFor = 0;
    private int iteration = 1, index = -1;
    private static final int PRIORITY_DECREASE = 1;
    private static final int DURATION_DECREASE = 1;
    private ProgressIndicator progressIndicator;
    private Label debugLabel = new Label("debug");
    private boolean debugEnabled = false;
    private boolean paused = false;

    public WallboardBody() {
        addStyleName("wallboard-board");

        setSizeFull();

        contentLayout = new CssLayout();
        contentLayout.setSizeFull();
        contentLayout.addComponent(new Label("Nothing to display"));

        if (debugEnabled) {
            addComponent(debugLabel);
        }
        addComponent(contentLayout);
        setExpandRatio(contentLayout, 1.0f);

        progressIndicator = new ProgressIndicator();
        progressIndicator.setWidth("100%");
        progressIndicator.setPollingInterval(250);
        progressIndicator.setVisible(false);
        addComponent(progressIndicator);

        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                VaadinSession.getCurrent().lock();
                try {
                    advanceTimer();
                } finally {
                    VaadinSession.getCurrent().unlock();
                }
            }
        }, 250, 250);

        addDetachListener(new DetachListener() {
            @Override
            public void detach(DetachEvent detachEvent) {
                timer.cancel();
            }
        });
    }

    public void setDashletSpecs(List<DashletSpec> dashletSpecs) {
        this.dashletSpecs = dashletSpecs;
        this.dashlets = new HashMap<Integer, Dashlet>();
        this.priorityMap = new HashMap<Integer, Integer>();
        this.durationMap = new HashMap<Integer, Integer>();
        this.oldDurationMap = new HashMap<Integer, Integer>();
        this.oldPriorityMap = new HashMap<Integer, Integer>();

        waitFor = 0;
        iteration = 1;
        index = -1;

        VaadinSession.getCurrent().lock();
        try {
            progressIndicator.setVisible(true);
        } finally {
            VaadinSession.getCurrent().unlock();
        }
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isPausable() {
        return dashletSpecs.size() > 0;
    }

    public Dashlet getDashletInstance(DashletSpec dashletSpec) {
        DashletSelector dashletSelector = ((DashletSelectorAccess) getUI()).getDashletSelector();
        Dashlet dashlet = dashletSelector.getDashletFactoryForName(dashletSpec.getDashletName()).newDashletInstance(dashletSpec);
        dashlet.getWallboardComponent().getComponent().setCaption(null);
        return dashlet;
    }

    private void debug() {
        if (!debugEnabled) {
            return;
        }
        String debug = "#" + iteration + ", i=" + index + ", w=" + waitFor;
        debugLabel.setValue(debug);
    }


    private int next() {
        if (dashletSpecs.size() == 0) {
            return -1;
        }

        int oldIndex = index;

        while (true) {
            index++;

            if (index >= dashletSpecs.size()) {
                iteration++;
                index = 0;
            }

            if (index == oldIndex && dashletSpecs.size() > 1) {
                int minValue = Integer.MAX_VALUE, maxIndex = -1;

                for (Map.Entry<Integer, Integer> entry : priorityMap.entrySet()) {
                    if (entry.getKey() != oldIndex && entry.getValue() < minValue) {
                        minValue = entry.getValue();
                        maxIndex = entry.getKey();
                    }
                }

                if (maxIndex != -1) {
                    index = maxIndex;
                    priorityMap.put(index, 0);
                }
            }

            if (!priorityMap.containsKey(index)) {
                Dashlet dashlet = getDashletInstance(dashletSpecs.get(index));
                dashlet.getWallboardComponent().getComponent().addStyleName("wallboard");

                dashlets.put(index, dashlet);

                dashlets.get(index).getWallboardComponent().refresh();

                if (dashlets.get(index).isBoosted()) {
                    priorityMap.put(index, Math.max(0, dashletSpecs.get(index).getPriority() - dashletSpecs.get(index).getBoostPriority()));
                    durationMap.put(index, dashletSpecs.get(index).getDuration() + dashletSpecs.get(index).getBoostDuration());
                } else {
                    priorityMap.put(index, dashletSpecs.get(index).getPriority());
                    durationMap.put(index, dashletSpecs.get(index).getDuration());
                }

                oldPriorityMap.put(index, priorityMap.get(index));
                oldDurationMap.put(index, durationMap.get(index));
            }

            if (priorityMap.get(index) <= 0) {

                dashlets.get(index).getWallboardComponent().refresh();

                if (dashlets.get(index).isBoosted()) {
                    priorityMap.put(index, Math.max(0, dashletSpecs.get(index).getPriority() - dashletSpecs.get(index).getBoostPriority()));
                    durationMap.put(index, dashletSpecs.get(index).getDuration() + dashletSpecs.get(index).getBoostDuration());
                } else {
                    priorityMap.put(index, Math.min(oldPriorityMap.get(index) + PRIORITY_DECREASE, dashletSpecs.get(index).getPriority()));
                    durationMap.put(index, Math.max(oldDurationMap.get(index) - DURATION_DECREASE, dashletSpecs.get(index).getDuration()));
                }

                oldPriorityMap.put(index, priorityMap.get(index));
                oldDurationMap.put(index, durationMap.get(index));

                return index;
            } else {
                priorityMap.put(index, priorityMap.get(index) - 1);
            }
        }
    }

    private void advanceTimer() {

        if (paused) {
            return;
        }

        waitFor = (waitFor > 250 ? waitFor - 250 : 0);

        if (dashletSpecs.size() > 0) {
            if (waitFor <= 0) {

                int next = next();

                contentLayout.removeAllComponents();

                if (next != -1) {
                    waitFor = oldDurationMap.get(next) * 1000;

                    if (!dashlets.get(next).getName().equals(dashletSpecs.get(next).getDashletName())) {
                        dashlets.put(next, getDashletInstance(dashletSpecs.get(next)));
                    }

                    Panel panel = new Panel();
                    panel.setSizeFull();

                    String caption = dashlets.get(next).getName();

                    if (dashlets.get(next).getDashletSpec().getTitle() != null) {
                        if (!"".equals(dashlets.get(next).getDashletSpec().getTitle())) {
                            caption += ": " + "" + dashlets.get(next).getDashletSpec().getTitle();
                        }
                    }

                    panel.setCaption(caption);

                    Component component = dashlets.get(next).getWallboardComponent().getComponent();

                    VerticalLayout verticalLayout = new VerticalLayout(component);
                    verticalLayout.setSizeFull();
                    verticalLayout.setMargin(true);

                    panel.setContent(verticalLayout);

                    contentLayout.addComponent(panel);

                    if (!progressIndicator.isVisible()) {
                        progressIndicator.setVisible(true);
                    }

                } else {
                    contentLayout.addComponent(new Label("Nothing to display"));
                    progressIndicator.setVisible(false);
                }
            }
        } else {
            contentLayout.removeAllComponents();
            contentLayout.addComponent(new Label("Nothing to display"));
            progressIndicator.setVisible(false);
        }

        if (durationMap.containsKey(index)) {
            float x = 1 - ((float) waitFor / (float) (durationMap.get(index) * 1000));
            progressIndicator.setValue(x);
        }

        debug();
    }
}
