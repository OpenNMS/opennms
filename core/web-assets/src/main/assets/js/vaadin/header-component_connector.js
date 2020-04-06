require('vendor/jquery-js');

if (!window.org_opennms_features_vaadin_components_header_HeaderComponent) {
    window.org_opennms_features_vaadin_components_header_HeaderComponent = function HeaderComponent() {
        console.log('headercomponent: registering state change');
        this.onStateChange = function onStateChange() {
            console.log('headercomponent: state change triggered', this.getState());

            $("#onmsheader").empty();
            var div = $("<div></div>").load("/opennms/includes/bootstrap.jsp?nobreadcrumbs=true&superQuiet=true");
            $(div).appendTo("#onmsheader");
        };
    };
    console.log('init: headercomponent');
}

module.exports = window.org_opennms_features_vaadin_components_header_HeaderComponent;