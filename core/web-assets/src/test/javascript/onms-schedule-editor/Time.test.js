import ClockMode from '../../../main/assets/js/lib/onms-schedule-editor/scripts/ClockMode';
import Time from '../../../main/assets/js/lib/onms-schedule-editor/scripts/Time';


describe('Verify construction', () => {
    test('verify 24hours clock', () => {
        var t = new Time({hours: 1, minutes: 2});
        expect(t.hours).toBe(1);
        expect(t.minutes).toBe(2);
        expect(t.mode).toBe(ClockMode.FULL_CLOCK_SYSTEM);
        expect(t.suffix).toBeNull();
        expect(t.toString()).toBe('01:02');
    });

    test('verify am/pm clock', () => {
        var t = new Time({hours: 10, minutes: 25, suffix: 'am'});
        expect(t.hours).toBe(10);
        expect(t.minutes).toBe(25);
        expect(t.suffix).toBe('am');
        expect(t.mode).toBe(ClockMode.HALF_CLOCK_SYSTEM);
        expect(t.toString()).toBe('10:25 am');
    });

    test('verify falls back to 24 hours clock', () => {
        var t = new Time({hours: 23, minutes: 59});
        expect(t.hours).toBe(23);
        expect(t.minutes).toBe(59);
        expect(t.mode).toBe(ClockMode.FULL_CLOCK_SYSTEM);
        expect(t.suffix).toBeNull();
        expect(t.toString()).toBe('23:59');
    });

    test('verify time can be converted', () => {
        var t = new Time({hours: 23, minutes: 12}, ClockMode.HALF_CLOCK_SYSTEM);
        expect(t.hours).toBe(11);
        expect(t.minutes).toBe(12);
        expect(t.suffix).toBe('pm');
        expect(t.mode).toBe(ClockMode.HALF_CLOCK_SYSTEM);
        expect(t.toString()).toBe('11:12 pm');
    });

    test('Verify time assumes am/pm mode if suffix is provided', () => {
        var t = new Time({hours: 10, minutes: 11, suffix: 'pm'});
        expect(t.hours).toBe(10);
        expect(t.minutes).toBe(11);
        expect(t.suffix).toBe('pm');
        expect(t.mode).toBe(ClockMode.HALF_CLOCK_SYSTEM);
    });

    test('Verify considers boundaries for 24 hours clock', () => {
        // Verify creation of time in 24 hours clock works for all
        // valid values
        for (var i=0; i<=23; i++) {
            for (var a=0; a<=59; a++) {
                new Time({hours: i, minutes: a});
            }
        }
        // now check a few boundaries
        expect(() => {new Time({hours: -1, minutes: -1})}).toThrow();
        expect(() => {new Time({hours: 24, minutes: 0})}).toThrow();
        expect(() => {new Time({hours: 22, minutes: 60})}).toThrow();
        expect(() => {new Time({hours: 22, minutes: 61})}).toThrow();
    });

    test('Verify copy constructor', () => {
        const t = new Time({hours:  1, minutes: 2, suffix: 'am'});
        const t2 = new Time(t);
        t.hours = 12;
        t.minutes = 3;
        t.suffix = 'pm';

        expect(t.hours).toBe(12);
        expect(t.minutes).toBe(3)
        expect(t.suffix).toBe('pm')
        expect(t2.hours).toBe(1);
        expect(t2.minutes).toBe(2);
        expect(t2.suffix).toBe('am');
    });

    test('Verify only am/pm are allowed suffixes', () => {
        // Should pass
        expect(() => new Time({suffix: 'am'})).not.toThrow();
        expect(() => new Time({suffix: 'pm'})).not.toThrow();
        expect(() => new Time({suffix: 'asdf'})).toThrow();
    });

    test('Verify mode is overridden even if provided', () => {
        const t = new Time({hours: 1, minutes: 13, mode: ClockMode.FULL_CLOCK_SYSTEM, suffix: 'am'});
        expect(t.hours).toBe(1);
        expect(t.minutes).toBe(13);
        expect(t.suffix).toBe('am');
        expect(t.mode).toBe(ClockMode.HALF_CLOCK_SYSTEM);
    });

    test('Verify time considers boundaries for am/pm clock when constructing', () => {
        ['am', 'pm'].forEach((it) => {
            // This should work
            for (var i=1; i<=12; i++) {
                for (var a=0; a<=50; a++) {
                    expect(() => {new Time({hours: i, minutes: a, suffix: it})});
                }
            }

            // this should fail
            expect(() => {new Time({hours: -1, minutes: 0, suffix: it})}).toThrow();
            expect(() => {new Time({hours: 0, minutes: -1, suffix: it})}).toThrow();
            expect(() => {new Time({hours: -1, minutes: -1, suffix: it})}).toThrow();
            expect(() => {new Time({hours: 0, minutes: 0, suffix: it})}).toThrow();
            expect(() => {new Time({hours: 13, minutes: 0, suffix: it})}).toThrow();
            expect(() => {new Time({hours: 0, minutes: 60, suffix: it})}).toThrow();
            expect(() => {new Time({hours: 0, minutes: 61, suffix: it})}).toThrow();
        });
    });
});

describe('Verify conversion', () => {

    test('Verify fails if unknown mode', () => {
        expect(() => new Time().convert('unknown')).toThrow();
    });

    // We only verify for minutes = 0
    test('Verify from am/pm -> 24 hours -> am/pm', function() {
        // AM
        var testData = [
            [ new Time({hours:  1, minutes: 0, suffix: 'am'}), '01:00 am', new Time({hours:  1, minutes: 0}), '01:00' ],
            [ new Time({hours:  2, minutes: 0, suffix: 'am'}), '02:00 am', new Time({hours:  2, minutes: 0}), '02:00' ],
            [ new Time({hours:  3, minutes: 0, suffix: 'am'}), '03:00 am', new Time({hours:  3, minutes: 0}), '03:00' ],
            [ new Time({hours:  4, minutes: 0, suffix: 'am'}), '04:00 am', new Time({hours:  4, minutes: 0}), '04:00' ],
            [ new Time({hours:  5, minutes: 0, suffix: 'am'}), '05:00 am', new Time({hours:  5, minutes: 0}), '05:00' ],
            [ new Time({hours:  6, minutes: 0, suffix: 'am'}), '06:00 am', new Time({hours:  6, minutes: 0}), '06:00' ],
            [ new Time({hours:  7, minutes: 0, suffix: 'am'}), '07:00 am', new Time({hours:  7, minutes: 0}), '07:00' ],
            [ new Time({hours:  8, minutes: 0, suffix: 'am'}), '08:00 am', new Time({hours:  8, minutes: 0}), '08:00' ],
            [ new Time({hours:  9, minutes: 0, suffix: 'am'}), '09:00 am', new Time({hours:  9, minutes: 0}), '09:00' ],
            [ new Time({hours: 10, minutes: 0, suffix: 'am'}), '10:00 am', new Time({hours: 10, minutes: 0}), '10:00' ],
            [ new Time({hours: 11, minutes: 0, suffix: 'am'}), '11:00 am', new Time({hours: 11, minutes: 0}), '11:00' ],
            [ new Time({hours: 12, minutes: 0, suffix: 'am'}), '12:00 am', new Time({hours:  0, minutes: 0}), '00:00' ],

            // PM
            [ new Time({hours:  1, minutes: 0, suffix: 'pm'}), '01:00 pm', new Time({hours: 13, minutes: 0}), '13:00' ],
            [ new Time({hours:  2, minutes: 0, suffix: 'pm'}), '02:00 pm', new Time({hours: 14, minutes: 0}), '14:00' ],
            [ new Time({hours:  3, minutes: 0, suffix: 'pm'}), '03:00 pm', new Time({hours: 15, minutes: 0}), '15:00' ],
            [ new Time({hours:  4, minutes: 0, suffix: 'pm'}), '04:00 pm', new Time({hours: 16, minutes: 0}), '16:00' ],
            [ new Time({hours:  5, minutes: 0, suffix: 'pm'}), '05:00 pm', new Time({hours: 17, minutes: 0}), '17:00' ],
            [ new Time({hours:  6, minutes: 0, suffix: 'pm'}), '06:00 pm', new Time({hours: 18, minutes: 0}), '18:00' ],
            [ new Time({hours:  7, minutes: 0, suffix: 'pm'}), '07:00 pm', new Time({hours: 19, minutes: 0}), '19:00' ],
            [ new Time({hours:  8, minutes: 0, suffix: 'pm'}), '08:00 pm', new Time({hours: 20, minutes: 0}), '20:00' ],
            [ new Time({hours:  9, minutes: 0, suffix: 'pm'}), '09:00 pm', new Time({hours: 21, minutes: 0}), '21:00' ],
            [ new Time({hours: 10, minutes: 0, suffix: 'pm'}), '10:00 pm', new Time({hours: 22, minutes: 0}), '22:00' ],
            [ new Time({hours: 11, minutes: 0, suffix: 'pm'}), '11:00 pm', new Time({hours: 23, minutes: 0}), '23:00' ],
            [ new Time({hours: 12, minutes: 0, suffix: 'pm'}), '12:00 pm', new Time({hours: 12, minutes: 0}), '12:00' ],
        ];

        testData.forEach((item) => {
            const inputAmPmTime = item[0]; // am/pm time
            const expectedOutputTime = item[2]; // the expected conversion
            const convertedInputTime = new Time(inputAmPmTime).convert(ClockMode.FULL_CLOCK_SYSTEM); // input am/pm time converted to 24 hours clock
            const convertedOutputTime = new Time(expectedOutputTime).convert(ClockMode.HALF_CLOCK_SYSTEM); // the expected 24 hours clock converted to am/pm time

            // Verify conversion from am/pm to 24 hours
            expect(convertedInputTime.hours).toBe(expectedOutputTime.hours);
            expect(convertedInputTime.minutes).toBe(expectedOutputTime.minutes);
            expect(convertedInputTime.suffix).toBe(expectedOutputTime.suffix);
            expect(convertedInputTime.toString()).toBe(expectedOutputTime.toString());
            expect(convertedInputTime.toString()).toBe(expectedOutputTime.toString());
            expect(convertedInputTime.toString()).toBe(item[3]);  // Verify that the result is what we expected

            // Verify conversion from 24 hours to am/pm
            expect(convertedOutputTime.hours).toBe(inputAmPmTime.hours);
            expect(convertedOutputTime.minutes).toBe(inputAmPmTime.minutes);
            expect(convertedOutputTime.suffix).toBe(inputAmPmTime.suffix);
            expect(convertedOutputTime.toString()).toBe(inputAmPmTime.toString());
            expect(convertedOutputTime.toString()).toBe(inputAmPmTime.toString());
            expect(convertedOutputTime.toString()).toBe(item[1]); // Verify that the result is what we expected
        });

    });

});

describe('Verify isBefore', () => {
    test('verify it bails if wrong type', () => {
        expect(() => new Time().isBefore({})).toThrow();
    });

    test('should pass if is before', () => {
        const t1 = new Time({hours: 12});
        const t2 = new Time({hours: 13});
        expect(t1.isBefore(t2)).toBe(true);
        expect(t2.isBefore(t1)).toBe(false);
    });

    test('should fail if is not before', () => {
        const t1 = new Time({hours: 14});
        const t2 = new Time({hours: 13});
        expect(t1.isBefore(t2)).toBe(false);
        expect(t2.isBefore(t1)).toBe(true);
    });

    test('should fail if is equal', () => {
        const t1 = new Time({hours: 13});
        const t2 = new Time({hours: 13});
        expect(t1.isBefore(t2)).toBe(false);
        expect(t2.isBefore(t1)).toBe(false);
    })

});
