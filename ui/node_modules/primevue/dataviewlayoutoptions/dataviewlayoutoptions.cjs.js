'use strict';

var vue = require('vue');

var script = {
        name: 'DataViewLayoutOptions',
		emits: ['update:modelValue'],
		props: {
			modelValue: String
		},
		computed: {
			buttonListClass(){
				return [
					'p-button p-button-icon-only',
					{'p-highlight': this.modelValue === 'list'}
				]
			},
			buttonGridClass() {
				return [
					'p-button p-button-icon-only',
					{'p-highlight': this.modelValue === 'grid'}
				]
			}
		},
		methods: {
			changeLayout(layout){
				this.$emit('update:modelValue', layout);
			}
		}
	};

const _hoisted_1 = { class: "p-dataview-layout-options p-selectbutton p-buttonset" };
const _hoisted_2 = /*#__PURE__*/vue.createVNode("i", { class: "pi pi-bars" }, null, -1);
const _hoisted_3 = /*#__PURE__*/vue.createVNode("i", { class: "pi pi-th-large" }, null, -1);

function render(_ctx, _cache, $props, $setup, $data, $options) {
  return (vue.openBlock(), vue.createBlock("div", _hoisted_1, [
    vue.createVNode("button", {
      class: $options.buttonListClass,
      onClick: _cache[1] || (_cache[1] = $event => ($options.changeLayout('list'))),
      type: "button"
    }, [
      _hoisted_2
    ], 2),
    vue.createVNode("button", {
      class: $options.buttonGridClass,
      onClick: _cache[2] || (_cache[2] = $event => ($options.changeLayout('grid'))),
      type: "button"
    }, [
      _hoisted_3
    ], 2)
  ]))
}

script.render = render;

module.exports = script;
