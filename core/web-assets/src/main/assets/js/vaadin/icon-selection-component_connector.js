/* eslint no-console: 0 */

const d3 = require('vendor/d3-js');

if (!window.org_opennms_features_topology_app_internal_ui_icons_IconSelectionComponent) {
    window.org_opennms_features_topology_app_internal_ui_icons_IconSelectionComponent = function IconSelectionComponent() {
        // Initialize SVG Element
        // As the Topology UI already initialized the SVG Definitions, we do not need to do this again here
        // We can just cross-reference them, cool!
        var svgParent = this.getElement();
        var svg = d3.select(svgParent).append('svg:svg');

        var state = this.getState();
        var connector = this;

        var elementsToShow = state.elementsToShow;
        var SPACING = state.spacing;
        var NO_COLUMNS = state.columnCount;
        var MAX_SIZE = state.maxSize;

        // Initialize SVG
        var currentlySelectedIconElement;
        var columnCounter = 0;
        var rowCounter = 0;
        elementsToShow.forEach(function showElement(e) {
            // Create a g element for the element itself (this is the container)
            var element = svg.append('g').attr('class', 'icon');
            element.attr('opacity', 1).style('cursor', 'pointer');

            // The g element for the icons
            var svgIconContainer         = element.append('g').attr('class', 'icon-container').attr('opacity', 1);
            var svgIcon                  = svgIconContainer.append('use');
            var svgIconRollover          = svgIconContainer.append('use');
            var svgIconActive            = svgIconContainer.append('use');

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
            var xPos = MAX_SIZE * columnCounter + (columnCounter * SPACING);
            var yPos = MAX_SIZE * rowCounter + (rowCounter * SPACING);

            // Define scale factor based on svgIcon, we assume _rollover and _active icons have same size
            var iconWidth = svgIcon[0][0].getBBox().width;
            var iconHeight = svgIcon[0][0].getBBox().height;
            var primeLength = iconWidth >= iconHeight ? iconWidth : iconHeight;
            var scaleFactor = primeLength === 0 ? 0.001 : (MAX_SIZE / primeLength);

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
        this.onStateChange = function onStateChange() {
        };

        svg.attr('width', (NO_COLUMNS - 1) * (MAX_SIZE + SPACING) + MAX_SIZE);
        svg.attr('height',(rowCounter - 1) * (MAX_SIZE + SPACING) + MAX_SIZE);
    };

    console.log('init: icon-selection-component_connector');
}

module.exports = window.org_opennms_features_topology_app_internal_ui_icons_IconSelectionComponent;
