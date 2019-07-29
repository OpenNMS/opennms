import DailyParser from "../../../../main/assets/js/lib/onms-schedule-editor/scripts/parsers/DailyParser";

test('Verify unsupported minutes interval', () => {
    // TODO MVR constant
    const supportedMinutes = ['5','10', '15', '30'];
    for (let i=1; i<60; i++) {
        const canParse = new DailyParser().canParse('0 0/' + String(i) + ' 1-10 * * ?');
        const supported = supportedMinutes.indexOf(String(i)) >= 0;
        expect(canParse).toBe(supported);
    }
});