/* eslint no-console: 0 */

const GraphContainers = require('apps/onms-graph');

if (!window.org_opennms_features_vaadin_components_graph_InlineGraphContainer) {
  window.org_opennms_features_vaadin_components_graph_InlineGraphContainer = function InlineGraphContainer() {
    var e = this.getElement();

    console.log('inlinegraphcontainer: registering state change');
    this.onStateChange = function onStateChange() {
      console.log('inlinegraphcontainer: state change triggered', this.getState());
      // Globals
      window.onmsGraphContainers = {
        'baseHref': this.getState().baseHref,
        'engine': this.getState().engine
      };

      GraphContainers.render();
    };
  };

  console.log('init: inline-graphcontainer');
}

module.exports = window.org_opennms_features_vaadin_components_graph_InlineGraphContainer;
