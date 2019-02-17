/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.enlinkd.generator;

import java.util.ArrayList;
import java.util.List;

public class TopologyContext {

    private final TopologyGenerator.ProgressCallback progressCallback;

    private final TopologyPersister topologyPersister;

    private final List<Action> postActions;

    private TopologyContext(TopologyGenerator.ProgressCallback progressCallback, TopologyPersister topologyPersister, List<Action> postActions) {
        this.progressCallback = progressCallback;
        this.topologyPersister = topologyPersister;
        this.postActions = postActions;
    }



    public void currentProgress(String msg){
        this.progressCallback.currentProgress(msg);
    }

    public void currentProgress(String msg, Object...args){
        this.progressCallback.currentProgress(msg, args);
    }

    public TopologyPersister getTopologyPersister(){
        return this.topologyPersister;
    }

    public List<Action> getPostActions() {
        return postActions;
    }

    public static TopologyContextBuilder builder() {
        return new TopologyContextBuilder();
    }

    @FunctionalInterface
    public interface Action {
        void invoke() throws Exception;
    }

    public static class TopologyContextBuilder {
        private TopologyGenerator.ProgressCallback progressCallback;
        private TopologyPersister topologyPersister;
        private List<Action> postActions = new ArrayList<>();

        private TopologyContextBuilder() {
        }

        public TopologyContextBuilder progressCallback(TopologyGenerator.ProgressCallback progressCallback) {
            this.progressCallback = progressCallback;
            return this;
        }

        public TopologyContextBuilder topologyPersister(TopologyPersister topologyPersister) {
            this.topologyPersister = topologyPersister;
            return this;
        }

        public TopologyContextBuilder addPostAction(Action postAction) {
            this.postActions.add(postAction);
            return this;
        }

        public TopologyContext build() {
            return new TopologyContext(progressCallback, topologyPersister, postActions);
        }
    }
}
