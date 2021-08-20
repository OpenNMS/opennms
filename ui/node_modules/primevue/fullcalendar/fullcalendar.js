this.primevue = this.primevue || {};
this.primevue.fullcalendar = (function (vdom, core, vue) {
    'use strict';

    var script = {
        name: 'FullCalendar',
        props: {
            events: Array,
            options: null
        },
        calendar: null,
        watch: {
            events(value) {
                if (value && this.calendar) {
                    this.calendar.removeAllEventSources();
                    this.calendar.addEventSource(value);
                }
            },
            options(value) {
                if (value && this.calendar) {
                    for (let prop in value) {
                        this.calendar.setOption(prop, value[prop]);
                    }
                }
            }
        },
        mounted() {
            if (this.$el.offsetParent) {
                this.initialize();
            }
        },
        updated() {
            if (!this.calendar && this.$el.offsetParent) {
                this.initialize();
            }
        },
        beforeUnmount() {
            if (this.calendar) {
                this.calendar.destroy();
                this.calendar = null;
            }
        },
        methods: {
            initialize() {
                let defaultConfig = {themeSystem: 'standard'};
                let config = this.options ? {...this.options, ...defaultConfig} : defaultConfig;
                this.calendar = new core.Calendar(this.$el, config);
                this.calendar.render();

                if (this.events) {
                    this.calendar.removeAllEventSources();
                    this.calendar.addEventSource(this.events);
                }
            }
        }
    };

    function render(_ctx, _cache, $props, $setup, $data, $options) {
      return (vue.openBlock(), vue.createBlock("div"))
    }

    function styleInject(css, ref) {
      if ( ref === void 0 ) ref = {};
      var insertAt = ref.insertAt;

      if (!css || typeof document === 'undefined') { return; }

      var head = document.head || document.getElementsByTagName('head')[0];
      var style = document.createElement('style');
      style.type = 'text/css';

      if (insertAt === 'top') {
        if (head.firstChild) {
          head.insertBefore(style, head.firstChild);
        } else {
          head.appendChild(style);
        }
      } else {
        head.appendChild(style);
      }

      if (style.styleSheet) {
        style.styleSheet.cssText = css;
      } else {
        style.appendChild(document.createTextNode(css));
      }
    }

    var css_248z = "\n";
    styleInject(css_248z);

    script.render = render;

    return script;

}(null, FullCalendar, Vue));
