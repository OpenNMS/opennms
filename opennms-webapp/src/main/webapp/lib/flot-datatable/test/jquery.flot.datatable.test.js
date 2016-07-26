describe('jquery.flot.datatable', function () {

    describe('.createTable', function() {
        it('should generate the appropriate html', function() {
            var allSeries = [
                {
                    xaxis: {
                        options: {

                        }
                    },
                    yaxis: {
                        options: {

                        }
                    },
                    data: [[0, 100], [1, 101]]
                },
                {
                    label: 'watts',
                    data: [[0, 1], [1, 2]]
                }
            ];

            var tableHtml = createTable(allSeries, options);
            expect(tableHtml).toBe('<tr><th align="left">X</th><th align="left">Y0</th><th align="left">watts</th></tr><tr><td nowrap>0.00</td><td nowrap>100.00</td><td nowrap>1.00</td></tr><tr><td nowrap>1.00</td><td nowrap>101.00</td><td nowrap>2.00</td></tr>');
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

        it('should render a canvas and the data/graph tabs', function() {
            $.plot(div.node(), series);

            setTimeout(function(){
                var html = "" + div.node().innerHTML;
                expect(html).toContain("canvas");
                expect(html).toContain("graphTab");
                expect(html).toContain("dataTab");

                done();
            }, 500);
        });
    });
});