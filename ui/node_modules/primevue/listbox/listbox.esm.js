import { ObjectUtils, DomHandler } from 'primevue/utils';
import { FilterService } from 'primevue/api';
import Ripple from 'primevue/ripple';
import { resolveDirective, openBlock, createBlock, renderSlot, createVNode, withDirectives, vModelText, createCommentVNode, Fragment, renderList, createTextVNode, toDisplayString } from 'vue';

var script = {
    name: 'Listbox',
    emits: ['update:modelValue', 'change', 'filter'],
    props: {
        modelValue: null,
        options: Array,
        optionLabel: null,
        optionValue: null,
        optionDisabled: null,
        optionGroupLabel: null,
        optionGroupChildren: null,
		listStyle: null,
        disabled: Boolean,
        dataKey: null,
        multiple: Boolean,
        metaKeySelection: Boolean,
        filter: Boolean,
        filterPlaceholder: String,
        filterLocale: String,
        filterMatchMode: {
            type: String,
            default: 'contains'
        },
        filterFields: {
            type: Array,
            default: null
        },
        emptyFilterMessage: {
            type: String,
            default: null
        },
        emptyMessage: {
            type: String,
            default: null
        }
    },
    optionTouched: false,
    data() {
        return {
            filterValue: null
        };
    },
    methods: {
        getOptionLabel(option) {
            return this.optionLabel ? ObjectUtils.resolveFieldData(option, this.optionLabel) : option;
        },
        getOptionValue(option) {
            return this.optionValue ? ObjectUtils.resolveFieldData(option, this.optionValue) : option;
        },
        getOptionRenderKey(option) {
            return this.dataKey ? ObjectUtils.resolveFieldData(option, this.dataKey) : this.getOptionLabel(option);
        },
        isOptionDisabled(option) {
            return this.optionDisabled ? ObjectUtils.resolveFieldData(option, this.optionDisabled) : false;
        },
        getOptionGroupRenderKey(optionGroup) {
            return ObjectUtils.resolveFieldData(optionGroup, this.optionGroupLabel);
        },
        getOptionGroupLabel(optionGroup) {
            return ObjectUtils.resolveFieldData(optionGroup, this.optionGroupLabel);
        },
        getOptionGroupChildren(optionGroup) {
            return ObjectUtils.resolveFieldData(optionGroup, this.optionGroupChildren);
        },
        onOptionSelect(event, option) {
            if (this.disabled || this.isOptionDisabled(option)) {
                return;
            }

            if(this.multiple)
                this.onOptionSelectMultiple(event, option);
            else
                this.onOptionSelectSingle(event, option);

            this.optionTouched = false;
        },
        onOptionTouchEnd() {
            if (this.disabled) {
                return;
            }

            this.optionTouched = true;
        },
        onOptionSelectSingle(event, option) {
            let selected = this.isSelected(option);
            let valueChanged = false;
            let value = null;
            let metaSelection = this.optionTouched ? false : this.metaKeySelection;

            if (metaSelection) {
                let metaKey = (event.metaKey || event.ctrlKey);

                if (selected) {
                    if (metaKey) {
                        value = null;
                        valueChanged = true;
                    }
                }
                else {
                    value = this.getOptionValue(option);
                    valueChanged = true;
                }
            }
            else {
                value = selected ? null : this.getOptionValue(option);
                valueChanged = true;
            }

            if (valueChanged) {
                this.updateModel(event, value);
            }
        },
        onOptionSelectMultiple(event, option) {
            let selected = this.isSelected(option);
            let valueChanged = false;
            let value = null;
            let metaSelection = this.optionTouched ? false : this.metaKeySelection;

            if (metaSelection) {
                let metaKey = (event.metaKey || event.ctrlKey);

                if (selected) {
                    if(metaKey)
                        value = this.removeOption(option);
                    else
                        value = [this.getOptionValue(option)];

                    valueChanged = true;
                }
                else {
                    value = (metaKey) ? this.modelValue || [] : [];
                    value = [...value, this.getOptionValue(option)];
                    valueChanged = true;
                }
            }
            else {
                if (selected)
                    value = this.removeOption(option);
                else
                    value = [...this.modelValue || [], this.getOptionValue(option)];

                valueChanged = true;
            }

            if(valueChanged) {
                this.updateModel(event, value);
            }
        },
        isSelected(option) {
            let selected = false;
            let optionValue = this.getOptionValue(option);

            if (this.multiple) {
                if (this.modelValue) {
                    for (let val of this.modelValue) {
                        if (ObjectUtils.equals(val, optionValue, this.equalityKey)) {
                            selected = true;
                            break;
                        }
                    }
                }
            }
            else {
                selected = ObjectUtils.equals(this.modelValue, optionValue, this.equalityKey);
            }

            return selected;
        },
        removeOption(option) {
            return this.modelValue.filter(val => !ObjectUtils.equals(val, this.getOptionValue(option), this.equalityKey));
        },
        updateModel(event, value) {
            this.$emit('update:modelValue', value);
            this.$emit('change', {originalEvent: event, value: value});
        },
        onOptionKeyDown(event, option) {
            let item = event.currentTarget;

            switch (event.which) {
                //down
                case 40:
                    var nextItem = this.findNextItem(item);
                    if(nextItem) {
                        nextItem.focus();
                    }

                    event.preventDefault();
                break;

                //up
                case 38:
                    var prevItem = this.findPrevItem(item);
                    if(prevItem) {
                        prevItem.focus();
                    }

                    event.preventDefault();
                break;

                //enter
                case 13:
                    this.onOptionSelect(event, option);
                    event.preventDefault();
                break;
            }
        },
        findNextItem(item) {
            let nextItem = item.nextElementSibling;

            if (nextItem)
                return DomHandler.hasClass(nextItem, 'p-disabled') || DomHandler.hasClass(nextItem, 'p-listbox-item-group') ? this.findNextItem(nextItem) : nextItem;
            else
                return null;
        },
        findPrevItem(item) {
            let prevItem = item.previousElementSibling;

            if (prevItem)
                return DomHandler.hasClass(prevItem, 'p-disabled') || DomHandler.hasClass(prevItem, 'p-listbox-item-group') ? this.findPrevItem(prevItem) : prevItem;
            else
                return null;
        },
        onFilterChange(event) {
            this.$emit('filter', {originalEvent: event, value: event.target.value});
        }
    },
    computed: {
        visibleOptions() {
            if (this.filterValue) {
                if (this.optionGroupLabel) {
                    let filteredGroups = [];
                    for (let optgroup of this.options) {
                        let filteredSubOptions = FilterService.filter(this.getOptionGroupChildren(optgroup), this.searchFields, this.filterValue, this.filterMatchMode, this.filterLocale);
                        if (filteredSubOptions && filteredSubOptions.length) {
                            filteredGroups.push({...optgroup, ...{items: filteredSubOptions}});
                        }
                    }
                    return filteredGroups
                }
                else {
                    return FilterService.filter(this.options, this.searchFields, this.filterValue, 'contains', this.filterLocale);
                }
            }
            else {
                return this.options;
            }
        },
        equalityKey() {
            return this.optionValue ? null : this.dataKey;
        },
        searchFields() {
            return this.filterFields || [this.optionLabel];
        },
        emptyFilterMessageText() {
            return this.emptyFilterMessage || this.$primevue.config.locale.emptyFilterMessage;
        },
        emptyMessageText() {
            return this.emptyMessage || this.$primevue.config.locale.emptyMessage;
        }
    },
    directives: {
        'ripple': Ripple
    }
};

const _hoisted_1 = { class: "p-listbox p-component" };
const _hoisted_2 = {
  key: 0,
  class: "p-listbox-header"
};
const _hoisted_3 = { class: "p-listbox-filter-container" };
const _hoisted_4 = /*#__PURE__*/createVNode("span", { class: "p-listbox-filter-icon pi pi-search" }, null, -1);
const _hoisted_5 = {
  class: "p-listbox-list",
  role: "listbox",
  "aria-multiselectable": "multiple"
};
const _hoisted_6 = { class: "p-listbox-item-group" };
const _hoisted_7 = {
  key: 2,
  class: "p-listbox-empty-message"
};
const _hoisted_8 = {
  key: 3,
  class: "p-listbox-empty-message"
};

function render(_ctx, _cache, $props, $setup, $data, $options) {
  const _directive_ripple = resolveDirective("ripple");

  return (openBlock(), createBlock("div", _hoisted_1, [
    renderSlot(_ctx.$slots, "header", {
      value: $props.modelValue,
      options: $options.visibleOptions
    }),
    ($props.filter)
      ? (openBlock(), createBlock("div", _hoisted_2, [
          createVNode("div", _hoisted_3, [
            withDirectives(createVNode("input", {
              type: "text",
              class: "p-listbox-filter p-inputtext p-component",
              "onUpdate:modelValue": _cache[1] || (_cache[1] = $event => ($data.filterValue = $event)),
              placeholder: $props.filterPlaceholder,
              onInput: _cache[2] || (_cache[2] = (...args) => ($options.onFilterChange && $options.onFilterChange(...args)))
            }, null, 40, ["placeholder"]), [
              [vModelText, $data.filterValue]
            ]),
            _hoisted_4
          ])
        ]))
      : createCommentVNode("", true),
    createVNode("div", {
      class: "p-listbox-list-wrapper",
      style: $props.listStyle
    }, [
      createVNode("ul", _hoisted_5, [
        (!$props.optionGroupLabel)
          ? (openBlock(true), createBlock(Fragment, { key: 0 }, renderList($options.visibleOptions, (option, i) => {
              return withDirectives((openBlock(), createBlock("li", {
                tabindex: $options.isOptionDisabled(option) ? null : '0',
                class: ['p-listbox-item', {'p-highlight': $options.isSelected(option), 'p-disabled': $options.isOptionDisabled(option)}],
                key: $options.getOptionRenderKey(option),
                onClick: $event => ($options.onOptionSelect($event, option)),
                onTouchend: _cache[3] || (_cache[3] = $event => ($options.onOptionTouchEnd())),
                onKeydown: $event => ($options.onOptionKeyDown($event, option)),
                role: "option",
                "aria-label": $options.getOptionLabel(option),
                "aria-selected": $options.isSelected(option)
              }, [
                renderSlot(_ctx.$slots, "option", {
                  option: option,
                  index: i
                }, () => [
                  createTextVNode(toDisplayString($options.getOptionLabel(option)), 1)
                ])
              ], 42, ["tabindex", "onClick", "onKeydown", "aria-label", "aria-selected"])), [
                [_directive_ripple]
              ])
            }), 128))
          : (openBlock(true), createBlock(Fragment, { key: 1 }, renderList($options.visibleOptions, (optionGroup, i) => {
              return (openBlock(), createBlock(Fragment, {
                key: $options.getOptionGroupRenderKey(optionGroup)
              }, [
                createVNode("li", _hoisted_6, [
                  renderSlot(_ctx.$slots, "optiongroup", {
                    option: optionGroup,
                    index: i
                  }, () => [
                    createTextVNode(toDisplayString($options.getOptionGroupLabel(optionGroup)), 1)
                  ])
                ]),
                (openBlock(true), createBlock(Fragment, null, renderList($options.getOptionGroupChildren(optionGroup), (option, i) => {
                  return withDirectives((openBlock(), createBlock("li", {
                    tabindex: $options.isOptionDisabled(option) ? null : '0',
                    class: ['p-listbox-item', {'p-highlight': $options.isSelected(option), 'p-disabled': $options.isOptionDisabled(option)}],
                    key: $options.getOptionRenderKey(option),
                    onClick: $event => ($options.onOptionSelect($event, option)),
                    onTouchend: _cache[4] || (_cache[4] = $event => ($options.onOptionTouchEnd())),
                    onKeydown: $event => ($options.onOptionKeyDown($event, option)),
                    role: "option",
                    "aria-label": $options.getOptionLabel(option),
                    "aria-selected": $options.isSelected(option)
                  }, [
                    renderSlot(_ctx.$slots, "option", {
                      option: option,
                      index: i
                    }, () => [
                      createTextVNode(toDisplayString($options.getOptionLabel(option)), 1)
                    ])
                  ], 42, ["tabindex", "onClick", "onKeydown", "aria-label", "aria-selected"])), [
                    [_directive_ripple]
                  ])
                }), 128))
              ], 64))
            }), 128)),
        ($data.filterValue && (!$options.visibleOptions || ($options.visibleOptions && $options.visibleOptions.length === 0)))
          ? (openBlock(), createBlock("li", _hoisted_7, [
              renderSlot(_ctx.$slots, "emptyfilter", {}, () => [
                createTextVNode(toDisplayString($options.emptyFilterMessageText), 1)
              ])
            ]))
          : ((!$props.options || ($props.options && $props.options.length === 0)))
            ? (openBlock(), createBlock("li", _hoisted_8, [
                renderSlot(_ctx.$slots, "empty", {}, () => [
                  createTextVNode(toDisplayString($options.emptyMessageText), 1)
                ])
              ]))
            : createCommentVNode("", true)
      ])
    ], 4),
    renderSlot(_ctx.$slots, "footer", {
      value: $props.modelValue,
      options: $options.visibleOptions
    })
  ]))
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

var css_248z = "\n.p-listbox-list-wrapper {\n    overflow: auto;\n}\n.p-listbox-list {\n    list-style-type: none;\n    margin: 0;\n    padding: 0;\n}\n.p-listbox-item {\n    cursor: pointer;\n    position: relative;\n    overflow: hidden;\n}\n.p-listbox-item-group {\n    cursor: auto;\n}\n.p-listbox-filter-container {\n    position: relative;\n}\n.p-listbox-filter-icon {\n    position: absolute;\n    top: 50%;\n    margin-top: -.5rem;\n}\n.p-listbox-filter {\n    width: 100%;\n}\n";
styleInject(css_248z);

script.render = render;

export default script;
