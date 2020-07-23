import Intervals from '../../../main/assets/js/lib/onms-schedule-editor/scripts/Intervals';

test('verify Minutes', () => {
    expect(Intervals.Minutes).toEqual(['30', '15', '10', '5']);
});

test('Verify Hours', () => {
    expect(Intervals.Hours).toEqual(['180', '120', '60']);
});

test('Verify all', () => {
    const expectedResult = [].concat(Intervals.Hours).concat(Intervals.Minutes);
    expect(Intervals.all).toEqual(expectedResult);
});