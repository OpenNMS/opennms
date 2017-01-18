var options = {
    legend: {
        statements: [],
        margin: {
            left: 5,
            right: 0,
            top: 0,
            bottom: 0
        },
        style: {
            fontSize: 10,
            minFontSize: 3,
            /*
            badgeSize: =fontSize
            badgeMarginRight: =fontSize*0.25 (no less than 1)
            */
            lineSpacing: 5,
        }
    }
};

var TOKENS = Object.freeze({
    'Badge': 'badge',
    'Text': 'text',
    'Unit': 'unit',
    'Lf': 'lf',
    'Newline': 'newline'
});