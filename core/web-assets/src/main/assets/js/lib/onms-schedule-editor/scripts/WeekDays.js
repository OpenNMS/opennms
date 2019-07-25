class WeekDay {
    constructor(id, label) {
        this.id = id;
        this.label = label;
    }

    get shortcut() {
        return this.label.substr(0, 3).toUpperCase();
    }
}

const Sunday = new WeekDay(1, 'Sunday');
const Monday = new WeekDay(2, 'Monday');
const Tuesday = new WeekDay(3, 'Tuesday');
const Wednesday = new WeekDay(4, 'Wednesday');
const Thursday = new WeekDay(5, 'Thursday');
const Friday = new WeekDay(6, 'Friday');
const Saturday = new WeekDay(7, 'Saturday');

const all = [
    Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday
];

export default {
    all,
    WeekDay,
    Sunday,
    Monday,
    Tuesday,
    Wednesday,
    Thursday,
    Friday,
    Saturday
};
