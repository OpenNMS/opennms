/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.vaadin.dashboard.ui.wallboard;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.opennms.features.vaadin.dashboard.config.DashletSelector;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletSelectorAccess;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.ProgressIndicator;

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

        progressIndicator = new ProgressIndicator();
        progressIndicator.setWidth("100%");
        progressIndicator.setPollingInterval(250);
        progressIndicator.setVisible(true);
        addComponent(progressIndicator);

        setExpandRatio(contentLayout, 0.95f);
        setExpandRatio(progressIndicator, 0.05f);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final UI ui = getUI();
                if (ui != null) {
                    ui.accessSynchronously(new Runnable() {
                        @Override
                        public void run() {
                            advanceTimer();
                        }
                    });
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

        try {
            getUI().getSession().lock();
            progressIndicator.setVisible(true);
        } finally {
            getUI().getSession().unlock();
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
        dashlet.getWallboardComponent(getUI()).getComponent().setCaption(null);
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
                dashlet.getWallboardComponent(getUI()).getComponent().addStyleName("wallboard");

                dashlets.put(index, dashlet);

                dashlets.get(index).getWallboardComponent(getUI()).refresh();

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

                dashlets.get(index).getWallboardComponent(getUI()).refresh();

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

                    Component component = dashlets.get(next).getWallboardComponent(getUI()).getComponent();

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
