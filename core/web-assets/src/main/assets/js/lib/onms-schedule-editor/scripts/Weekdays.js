class Weekdays {
    constructor(id, label) {
        this.id = id;
        this.label = label;
    }

    get shortcut() {
        return this.label.substr(0, 3).toUpperCase();
    }
}

const Sunday = new Weekdays(1, 'Sunday');
const Monday = new Weekdays(2, 'Monday');
const Tuesday = new Weekdays(3, 'Tuesday');
const Wednesday = new Weekdays(4, 'Wednesday');
const Thursday = new Weekdays(5, 'Thursday');
const Friday = new Weekdays(6, 'Friday');
const Saturday = new Weekdays(7, 'Saturday');

const all = [
    Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday
];

export default {
    all,
    Weekdays,
    Sunday,
    Monday,
    Tuesday,
    Wednesday,
    Thursday,
    Friday,
    Saturday
};
