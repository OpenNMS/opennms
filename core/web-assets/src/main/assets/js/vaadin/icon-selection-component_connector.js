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
/* eslint no-console: 0 */

const d3 = require('vendor/d3-js');

if (!window.org_opennms_features_topology_app_internal_ui_icons_IconSelectionComponent) {
    window.org_opennms_features_topology_app_internal_ui_icons_IconSelectionComponent = function IconSelectionComponent() {
        // Initialize SVG Element
        // As the Topology UI already initialized the SVG Definitions, we do not need to do this again here
        // We can just cross-reference them, cool!
        const svgParent = this.getElement();
        const svg = d3.select(svgParent).append('svg:svg');

        const state = this.getState();

        // eslint-disable-next-line @typescript-eslint/no-this-alias
        const connector = this;

        const elementsToShow = state.elementsToShow;
        const SPACING = state.spacing;
        const NO_COLUMNS = state.columnCount;
        const MAX_SIZE = state.maxSize;

        // Initialize SVG
        let currentlySelectedIconElement;
        let columnCounter = 0;
        let rowCounter = 0;
        elementsToShow.forEach(function showElement(e) {
            // Create a g element for the element itself (this is the container)
            const element = svg.append('g').attr('class', 'icon');
            element.attr('opacity', 1).style('cursor', 'pointer');

            // The g element for the icons
            const svgIconContainer         = element.append('g').attr('class', 'icon-container').attr('opacity', 1);
            const svgIcon                  = svgIconContainer.append('use');
            const svgIconRollover          = svgIconContainer.append('use');
            const svgIconActive            = svgIconContainer.append('use');

            // The element which is actually visible
            element.append('svg:rect')
                .attr('class', 'svgIconOverlay')
                .attr('opacity', 0)
                .attr('width', MAX_SIZE)
                .attr('height', MAX_SIZE)
                .on('mouseover', function show(d) {
                    element.select('.overIcon').attr('opacity', 1);
                })
                .on('mouseout', function hide(d) {
                    element.select('.overIcon').attr('opacity', 0);
                })
                .on('click', function clicked(d) {
                    // deselect the currently selected element
                    if (currentlySelectedIconElement !== undefined) {
                        currentlySelectedIconElement.select('.activeIcon').attr('opacity', 0);
                        currentlySelectedIconElement.select('.svgIconOverlay').classed('selected', false);
                    }
                    // select the new element
                    element.select('.overIcon').attr('opacity', 0);
                    element.select('.activeIcon').attr('opacity', 1);
                    element.select('.svgIconOverlay').classed('selected', true);
                    currentlySelectedIconElement = element;
                    connector.onIconSelection(e);
                });
            svgIcon.attr('xlink:href', '#' + e);
            svgIconRollover.attr('xlink:href', '#' + e + '_rollover').attr('class', 'overIcon').attr('opacity', 0);
            svgIconActive.attr('xlink:href', '#' + e + '_active').attr('class', 'activeIcon').attr('opacity', 0);

            // Tooltip
            element.append('svg:title').text(e);

            // select element if selectedIconId matches
            if (state.selectedIconId !== undefined && e === state.selectedIconId) {
                element.select('.activeIcon').attr('opacity', 1);
                element.select('.svgIconOverlay').classed('selected', true);
                currentlySelectedIconElement = element;
            }

            // Positioning and sizing
            const xPos = MAX_SIZE * columnCounter + (columnCounter * SPACING);
            const yPos = MAX_SIZE * rowCounter + (rowCounter * SPACING);

            // Define scale factor based on svgIcon, we assume _rollover and _active icons have same size
            const iconWidth = svgIcon[0][0].getBBox().width;
            const iconHeight = svgIcon[0][0].getBBox().height;
            const primeLength = iconWidth >= iconHeight ? iconWidth : iconHeight;
            const scaleFactor = primeLength === 0 ? 0.001 : (MAX_SIZE / primeLength);

            // scale icon-container
            svgIconContainer.attr('transform', 'scale(' + scaleFactor + ')');
            element.attr('transform', 'translate(' + xPos + ', ' + yPos +')');

            if (columnCounter === NO_COLUMNS - 1) {
                columnCounter = 0;
                rowCounter++;
            } else {
                columnCounter++;
            }
        });

        // At this point we do not react on changes from server side
        // eslint-disable-next-line @typescript-eslint/no-empty-function
        this.onStateChange = function onStateChange() {
        };

        svg.attr('width', (NO_COLUMNS - 1) * (MAX_SIZE + SPACING) + MAX_SIZE);
        svg.attr('height',(rowCounter - 1) * (MAX_SIZE + SPACING) + MAX_SIZE);
    };

    console.log('init: icon-selection-component_connector');
}

module.exports = window.org_opennms_features_topology_app_internal_ui_icons_IconSelectionComponent;
