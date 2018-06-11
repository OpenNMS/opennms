const $ = require('vendor/jquery-js');
const c3 = require('c3');
const d3 = require('d3');
require('./status-box.css');

d3.document = window.document;

// the color palette to map each severity to
const colorPalette = {
	Normal: '#336600',
	Warning: '#ffcc00',
	Minor: '#ff9900',
	Major: '#ff3300',
	Critical: '#cc0000'
};

// the size of each donut
const donutSize = {
	// Don't set size to allow resizing automatically based on space available
	width: 0,
	height: 0
};

const loadChartData = function(graph) {
	$.ajax({
		method: 'GET',
		url: graph.url,
		headers: {
			'X-Requested-With': 'XMLHttpRequest'
		},
		dataType: 'json',
		success: function(data) {
		var columns = [];

		// Only include values > 0
		for (let i = 0; i < data.length; i++) {
			if (data[i].length >= 2) {
				if (data[i][1] > 0) {
					columns.push(data[i]);
				}
			}
		}

		// Decide to show or hide the graph
		var sum = 0;
		for (let i = 0; i < data.length; i++) {
			sum += data[i][1];
		}
		if (sum > 0) {
			// The first chart with data shows the box
			$('#status-overview-box').show();
			$('#' + graph.id).parent().show();

			// Generate graph
			var chart = c3.generate({
				bindto: '#' + graph.id,
				size: donutSize,
				data: {
					order: null,
					columns: [],
					colors: colorPalette,
					type: 'donut',
					onclick: graph.onclick
				},
				donut: {
					title: graph.title,
					label: {
						format: function(value, id, ratio) {
							return value;
						}
					}
				}
			});
			chart.load({
				columns: columns
			});

			// Add graph tooltip
			var description = graph.description || graph.title || '';
			if (description !== '') {
				d3.select('#' + graph.id)
					.select('.c3-chart')
					.append('svg:title')
					.text(description);
			}
		}
		}
	});
};

const render = function(options) {
	var graphs = options.graphs;
	if (graphs === undefined || graphs === null || graphs.length === 0) {
		return;
	}
	if (options.parentContainer === undefined || graphs.parentContainer === null) {
		return;
	}
	if ($(options.parentContainer) === undefined) {
		return;
	}

	for (let i = 0; i < graphs.length; i++) {
		// Gather options to draw graph
		var graph = graphs[i];

		// Skip the entry when any of the required fields are missing
		if (graph.id === undefined || graph.id === null || graph.id === '') {
			return;
		}
		if (graph.url === undefined || graph.url === null || graph.url === '') {
			return;
		}

		// create container for graph if it does not exist yet
		if ($('#' + graph.id).length === 0) {
			var graphContainer = $('<div/>', {
				class: 'col-centered col-xs-12 col-sm-6 col-md-6 col-lg-4'
			});
			graphContainer.append($('<div></div>', {
				id: graph.id
			}));
			graphContainer.hide();
			$(options.parentContainer).append(graphContainer);
		}

		// load data and populate graph
		loadChartData(graph);
	}
};

// all supported graphs
const graphDefinitions = {
	'business-services': {
		id: 'businessServiceProblemChart',
		title: 'Business Services',
		description: 'Business Services Status Overview',
		url: 'api/v2/status/summary/business-services',
		onclick: function(e) {
			window.location = 'status/index.jsp?title=Business Service List&type=business-services&severityFilter=' + e.id;
		}
	},

	applications: {
		id: 'applicationProblemChart',
		title: 'Applications',
		description: 'Applications Status Overview',
		url: 'api/v2/status/summary/applications',
		onclick: function(e) {
			window.location = 'status/index.jsp?title=Application List&type=applications&severityFilter=' + e.id;
		}
	},

	'nodes-by-alarms': {
		id: 'nodeProblemChartsByAlarms',
		title: 'Alarms',
		description: 'Nodes grouped by unacknowledged Alarms',
		url: 'api/v2/status/summary/nodes/alarms',
		onclick: function(e) {
			window.location = 'status/index.jsp?title=Node List&type=nodes&strategy=alarms&severityFilter=' + e.id;
		}
	},
	'nodes-by-outages': {
		id: 'nodeProblemChartByOutages',
		title: 'Outages',
		description: 'Nodes grouped by current Outages',
		url: 'api/v2/status/summary/nodes/outages',
		onclick: function(e) {
			window.location = 'status/index.jsp?title=Node List&type=nodes&strategy=outages&severityFilter=' + e.id;
		}
	}
};

const doRender = function(graphKeys) {
	//console.log('doRender:',graphKeys);
	var graphs = [];
	for (let i = 0; i < graphKeys.length; i++) {
		var graphKey = graphKeys[i];
		if (graphKey !== undefined && graphKey in graphDefinitions) {
			graphs.push(graphDefinitions[graphKey]);
		}
	}

	// only render if something is configured to show
	if (graphs.length !== 0) {
		//console.log('rendering ' + graphs.length + ' graphs');
		render({
			parentContainer: '#chart-content',
			graphs: graphs
		})
	} else {
		//console.log('no graphs found');
	}
};

window.renderStatusGraphs = doRender;
module.exports = doRender;