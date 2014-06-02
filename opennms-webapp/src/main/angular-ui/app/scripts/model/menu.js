function MenuDropdown(name) {
	'use strict';
	var self = this;

    self.name = name;
    self.entries = [];

    self.addEntry = function(entry) {
        self.entries.push(entry);
    };

    self.removeEntry = function(entry) {
        if (typeof entry === 'string' || entry instanceof String) {
            for (var i = 0; i < self.entries.length; i++) {
                var e = self.entries[i];
                if (e.getLabel() === entry) {
                    self.entries.splice(i, 1);
                    return;
                }
            }
        } else {
            for (var i = 0; i < self.entries.length; i++) {
                var e = self.entries[i];
                if (e.getLabel() === entry.getLabel()) {
                    self.entries.splice(i, 1);
                    return;
                }
            }
        }
    };

    self.getEntries = function() {
        return angular.copy(self.entries);
    };
}

function MenuEntry(name, route) {
    'use strict';
	var self = this;

    self.name = name;
    self.route = route;
    
    self.getName = function() {
        return angular.copy(self.name);
    };
    self.getRoute = function() {
        return angular.copy(self.route);
    };
}