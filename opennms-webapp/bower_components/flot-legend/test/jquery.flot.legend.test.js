describe('jquery.flot.legend', function () {

    describe('.tokenizeStatement', function() {
        it('should be able to convert statements to tokens', function() {
            var tokens = tokenizeStatement('Max  : %8.2lf %s\\n');
            expect(tokens.length).toBe(5);

            expect(tokens[0].type).toBe(TOKENS.Text);
            expect(tokens[0].value).toBe('Max  : ');

            expect(tokens[1].type).toBe(TOKENS.Lf);
            expect(tokens[1].length).toBe(8);
            expect(tokens[1].precision).toBe(2);

            expect(tokens[2].type).toBe(TOKENS.Text);
            expect(tokens[2].value).toBe(' ');

            expect(tokens[3].type).toBe(TOKENS.Unit);

            expect(tokens[4].type).toBe(TOKENS.Newline);
        });

        it('should be able to handle lf tokens with and without specifiers', function() {
            var tokens = tokenizeStatement('%10.5lf');
            expect(tokens[0].type).toBe(TOKENS.Lf);
            expect(tokens[0].length).toBe(10);
            expect(tokens[0].precision).toBe(5);

            tokens = tokenizeStatement('%.3lf');
            expect(tokens[0].type).toBe(TOKENS.Lf);
            expect(tokens[0].length).toBe(null);
            expect(tokens[0].precision).toBe(3);

            tokens = tokenizeStatement('%lf');
            expect(tokens[0].type).toBe(TOKENS.Lf);
            expect(tokens[0].length).toBe(null);
            expect(tokens[0].precision).toBe(null);

            tokens = tokenizeStatement('%7lf');
            expect(tokens[0].type).toBe(TOKENS.Lf);
            expect(tokens[0].length).toBe(7);
            expect(tokens[0].precision).toBe(null);
        });

        it('should always insert a space for statement that include badges', function() {
            var tokens = tokenizeStatement('%g');
            expect(tokens.length).toBe(2);
            expect(tokens[0].type).toBe(TOKENS.Badge);
            expect(tokens[1].type).toBe(TOKENS.Text);
            expect(tokens[1].value).toBe(' ');
        });
    });

    describe('.reduceWithAggregate', function() {
        var dataWithNaNs = [
            [0,0],
            [1,1],
            [2,NaN],
            [3,3],
            null
        ];

        var stackedDataWithNaNs = [
            [0,1,1],
            [1,3,2],
            [2,NaN,NaN],
            [3,6,3],
            null
        ];

        it('should support MIN, MAX, AVERAGE and LAST aggregation functions', function () {
            expect(reduceWithAggregate(dataWithNaNs, 'MIN')).toBeCloseTo(0, 0.0001);
            expect(reduceWithAggregate(dataWithNaNs, 'MAX')).toBeCloseTo(3, 0.0001);
            expect(reduceWithAggregate(dataWithNaNs, 'AVERAGE')).toBeCloseTo(1.3333, 0.0001);
            expect(reduceWithAggregate(dataWithNaNs, 'LAST')).toBeCloseTo(3, 0.0001);
        });

        it('should support MIN, MAX, AVERAGE and LAST aggregation functions on stacked data', function () {
            expect(reduceWithAggregate(stackedDataWithNaNs, 'MIN')).toBeCloseTo(0, 0.0001);
            expect(reduceWithAggregate(stackedDataWithNaNs, 'MAX')).toBeCloseTo(3, 0.0001);
            expect(reduceWithAggregate(stackedDataWithNaNs, 'AVERAGE')).toBeCloseTo(1.3333, 0.0001);
            expect(reduceWithAggregate(stackedDataWithNaNs, 'LAST')).toBeCloseTo(3, 0.0001);
        });
    });

    describe('.getSeriesWithMetricName', function() {
        it('should be able to retrieve series by metric name', function () {

            var allSeries = [
                {
                    metric: 'xx',
                    id: 'xx'
                }
            ];

            var options = {
                hiddenSeries: [{
                    metric: 'yy',
                    id: 'yy'
                }]
            };

            expect(getSeriesWithMetricName('xx', allSeries, options).id).toBe('xx');
            expect(getSeriesWithMetricName('yy', allSeries, options).id).toBe('yy');
        });

        it('should throw an exception if a series with the specified name does not exist', function () {

            expect(function() {
                getSeriesWithMetricName('zz', [], {});
            }).toThrow();
        });
    });

    describe('.renderStatement', function() {
        it('should be able to render Lf and Unit statements', function () {

            var stmt = {
                metric: 'main',
                aggregation: 'AVERAGE',
                format: 'Avg: %8.2lf %s\\n'
            };

            var series = {
                    metric: 'main',
                    color: '#feeded',
                    data: [[0,0], [1,1], [2,2], [3,3], [4,4], [5,500000]]
            };

            var renderer = {texts: []};
            renderer.drawText = function(text) {
              this.texts.push(text);
            };
            renderer.drawNewline = function() {
                this.texts.push('\n');
            };

            renderStatement(stmt, series, renderer);

            expect(renderer.texts.length).toBe(5);
            expect(renderer.texts[0]).toBe('Avg: ');
            expect(renderer.texts[1]).toBe('   83.33');
            expect(renderer.texts[2]).toBe(' ');
            expect(renderer.texts[3]).toBe('k ');
            expect(renderer.texts[4]).toBe('\n');
        });

        it('should be able to render Lf statements without the length modifier', function () {

            var stmt = {
                metric: 'main',
                aggregation: 'AVERAGE',
                format: 'Avg: %.2lf %s\\n'
            };

            var series = {
                metric: 'main',
                color: '#feeded',
                data: [[0,0], [1,1], [2,2], [3,3], [4,4], [5,500000]]
            };

            var renderer = {texts: []};
            renderer.drawText = function(text) {
                this.texts.push(text);
            };
            renderer.drawNewline = function() {
                this.texts.push('\n');
            };

            renderStatement(stmt, series, renderer);

            expect(renderer.texts.length).toBe(5);
            expect(renderer.texts[0]).toBe('Avg: ');
            expect(renderer.texts[1]).toBe('83.33');
            expect(renderer.texts[2]).toBe(' ');
            expect(renderer.texts[3]).toBe('k ');
            expect(renderer.texts[4]).toBe('\n');
        });
    });

    describe('CanvasLegend renderer', function() {
        var opts = JSON.parse(JSON.stringify(options));

        describe('.getLineHeight', function() {
            it('should return a sensible default', function () {

                var renderer = new CanvasLegend(null, opts);
                expect(renderer.getLineHeight()).toBe(15);
            });
        });

        describe('.getLegendHeight', function() {
            it('should return a sensible default', function () {

                opts.legend.statements = [
                    {
                        value: '!'
                    }
                ];

                var renderer = new CanvasLegend(null, opts, [{
                    type: TOKENS.Text,
                    value: '!'
                }]);
                expect(renderer.getLegendHeight()).toBe(15);
            });
        });
    });

    describe('flot integration', function() {
        var div;

        var series = [
            {
                metric: 'main',
                color: '#feeded',
                data: [[0, 12], [7, 12], null, [7, 2.5], [12, 2.5]]
            }
        ];

        beforeEach(function () {
            div = d3.select('body').append('div');
            $(div.node()).width(640).height(480);
        });

        it('should render a canvas', function() {
            var options = {
                legend: {
                    statements: [
                        {
                            metric: 'main',
                            format: '%g nominal Watts'
                        },
                        {
                            metric: 'main',
                            aggregation: 'AVERAGE',
                            format: 'Avg: %8.2lf %s'
                        },
                        {
                            metric: 'main',
                            aggregation: 'MIN',
                            format: 'Min: %8.2lf %s'
                        },
                        {
                            metric: 'main',
                            aggregation: 'MAX',
                            format: 'Max: %8.2lf %s\n'
                        }
                    ]
                }
            };

            $.plot(div.node(), series, options);

            setTimeout(function(){
                var html = "" + div.node().innerHTML;
                expect(html).toContain("canvas");

                done();
            }, 500);
        });

        it('should throw an error if a statement references a series that does exist', function() {
            var options = {
                legend: {
                    statements: [
                        {
                            metric: '!main!',
                            format: '%g nominal Watts'
                        }
                    ]
                }
            };

            expect(function() {
                $.plot(div.node(), series, options);
            }).toThrow();
        });
    });
});
