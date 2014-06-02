PluginManager = {
    plugins: ['opennms'],
    register: function(moduleName) {
        this.plugins.push(moduleName);
    },
    getModules: function() {
        return angular.copy(this.plugins);
    }
};
