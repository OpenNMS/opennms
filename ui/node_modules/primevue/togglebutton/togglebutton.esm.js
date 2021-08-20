import Ripple from 'primevue/ripple';
import { resolveDirective, withDirectives, openBlock, createBlock, createCommentVNode, createVNode, toDisplayString } from 'vue';

var script = {
    name: 'ToggleButton',
    emits: ['update:modelValue', 'change'],
    props: {
        modelValue: Boolean,
		onIcon: String,
		offIcon: String,
        onLabel: String,
        offLabel: String,
        iconPos: {
            type: String,
            default: 'left'
        }
    },
    methods: {
        onClick(event) {
            if (!this.$attrs.disabled) {
                this.$emit('update:modelValue', !this.modelValue);
                this.$emit('change', event);
            }
        }
    },
    computed: {
        buttonClass() {
            return {
                'p-button p-togglebutton p-component': true,
                'p-button-icon-only': this.hasIcon && !this.hasLabel,
                'p-disabled': this.$attrs.disabled,
                'p-highlight': this.modelValue === true
            }
        },
        iconClass() {
            return [
                this.modelValue ? this.onIcon: this.offIcon,
                'p-button-icon',
                {
                    'p-button-icon-left': this.iconPos === 'left' && this.label,
                    'p-button-icon-right': this.iconPos === 'right' && this.label
                }
            ]
        },
        hasLabel() {
            return this.onLabel && this.onLabel.length > 0 && this.offLabel && this.offLabel.length > 0;
        },
        hasIcon() {
            return this.onIcon && this.onIcon.length > 0 && this.offIcon && this.offIcon.length > 0;
        },
        label() {
            return this.hasLabel ? (this.modelValue ? this.onLabel : this.offLabel): '&nbsp;';
        }
    },
    directives: {
        'ripple': Ripple
    }
};

const _hoisted_1 = { class: "p-button-label" };

function render(_ctx, _cache, $props, $setup, $data, $options) {
  const _directive_ripple = resolveDirective("ripple");

  return withDirectives((openBlock(), createBlock("div", {
    class: $options.buttonClass,
    onClick: _cache[1] || (_cache[1] = $event => ($options.onClick($event))),
    role: "checkbox",
    "aria-checked": $props.modelValue,
    tabindex: _ctx.$attrs.disabled ? null : '0'
  }, [
    ($options.hasIcon)
      ? (openBlock(), createBlock("span", {
          key: 0,
          class: $options.iconClass
        }, null, 2))
      : createCommentVNode("", true),
    createVNode("span", _hoisted_1, toDisplayString($options.label), 1)
  ], 10, ["aria-checked", "tabindex"])), [
    [_directive_ripple]
  ])
}

script.render = render;

export default script;
