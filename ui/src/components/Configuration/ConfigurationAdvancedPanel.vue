<template>
  <FeatherExpansionPanel
    id="advanced-panel"
    class="expansion-panel advanced-panel"
    title="Advanced Options (optional)"
    :modelValue="props.active"
    @update:modelValue="props.activeUpdate"
  >
    <div>
      <div v-bind:key="index" v-for="(item, index) in props.items" class="item-wrapper">
        <FeatherAutocomplete
          type="single"
          label="Key"
          textProp="name"
          @search="(query: string) => search(query, props.type, props.subType, index)"
          v-model="item.key"
          @update:modelValue="(key: { hint: string }, index: number): any => {
            ConfigurationHelper.forceSetHint(key, index);
            props.advancedKeyUpdate(key, index)
          }"
          :results="results.list[index]"
        ></FeatherAutocomplete>
        <!-- Blank space ' ' below is part of forceSetHint() workaround for FeatherInput.
            If item.hint is blank on initial load, it will not render the internal element we need
            for forced update. So when item.hint is empty, we supply an empty space which is enough
            to force FeatherInput to render the help label.
        -->
        <FeatherInput class="hint-label" label="Value" :hint="item.hint || ' '" v-model="item.value" />
        <FeatherButton icon="Delete" @click="() => deleteAdvancedOption(index)">
          <FeatherIcon class="delete-icon" :icon="Delete"></FeatherIcon>
        </FeatherButton>
      </div>
      <div class="button-wrapper">
        <FeatherButton :disabled="buttonAddDisabled" @click="addAdvancedOption" primary>Add</FeatherButton>
      </div>
    </div>
  </FeatherExpansionPanel>
</template>

<script setup lang="ts">
import { reactive, PropType, watch, computed } from 'vue'

import { FeatherExpansionPanel } from '@featherds/expansion'
import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'
import { FeatherInput } from '@featherds/input'
import { FeatherAutocomplete } from '@featherds/autocomplete'
import Delete from '@featherds/icon/action/Delete'

import { orderBy } from 'lodash'

import { advancedKeys, dnsKeys, openDaylightKeys, aciKeys, zabbixKeys, prisKeys } from './copy/advancedKeys'
import { RequisitionPluginSubTypes, RequisitionTypes } from './copy/requisitionTypes'
import { AdvancedKey, AdvancedOption } from './configuration.types'
import { ConfigurationHelper } from './ConfigurationHelper'

/**
 * Props
 */
const props = defineProps({
  items: { type: Array as PropType<Array<AdvancedOption>>, required: true },
  type: { type: String, required: true },
  subType: { type: String, required: true },
  addAdvancedOption: { type: Function as PropType<(payload: MouseEvent) => void>, required: true },
  advancedKeyUpdate: { type: Function, required: true },
  deleteAdvancedOption: { type: Function, required: true },
  active: { type: Boolean, required: true },
  activeUpdate: Function,
  helpState: Object,
})

/**
 * Local State
 */
const results = reactive({
  list: [[{}]]
})

/**
 * Disabled when last item (key.name and value) is null,
 * hence preventing from adding new item.
 */
const buttonAddDisabled = computed(() => {
  const itemsLength = props.items.length

  if (!itemsLength) return false // enabled

  const { key, value } = props.items[itemsLength - 1] // last item
  return !(key.name && value) // disabled
})

/**
 * Depending on which Type is selected, we have different
 * keys in our Advanced Options select options. This
 * method determines which to load. This should eventually be
 * moved to an API solution so we don't store values locally.
 */
const getKeysBasedOnType = (type: string, subType: string) => {

  let keys = new Array<AdvancedKey>()

  if (type === RequisitionTypes.DNS) {
    keys = dnsKeys
  } else if (type === RequisitionTypes.VMWare) {
    keys = orderBy(advancedKeys, 'name', 'asc')
  } else if (type === RequisitionTypes.RequisitionPlugin) {
    if (subType === RequisitionPluginSubTypes.OpenDaylight) {
      keys = openDaylightKeys
    } else if (subType === RequisitionPluginSubTypes.ACI) {
      keys = aciKeys
    } else if (subType === RequisitionPluginSubTypes.Zabbix) {
      keys = zabbixKeys
    } else if (subType === RequisitionPluginSubTypes.PRIS) {
      keys = prisKeys
    }
  }
  return keys
}

/**
 *
 * @param searchVal The Key Name to search for
 * @param index Since there are multiple search boxes, we need to know which one to generate results for.
 */
const search = (searchVal: string, type: string, subType: string, index: number) => {
  const advancedKeys = getKeysBasedOnType(type, subType)

  //Find keys based on search text.
  let newResu = advancedKeys.filter((key) => key.name.includes(searchVal) || key.name === searchVal)

  //If there are no results, add one to the list. This enables custom advanced keys.
  if (newResu.length === 0) {
    newResu.push({ name: searchVal, _text: searchVal, id: props.items?.length || 1 })
  }

  //Make sure you can't select the same key twice.
  newResu = newResu.filter((res) => {
    let includeInResults = true
    props.items.forEach((item) => {
      if (item.key.name === res.name) {
        includeInResults = false
      }
    })
    return includeInResults
  })
  results.list[index] = [...newResu]
}

/**
 * Fills in the <textarea> within the FeatherAutocomplete.
 * This is currently a gap with the component and may be removed in the future
 * if this gap is filled.
 */
const fillAutoComplete = () => {
  if (props.items) {
    const inputs = document.querySelectorAll('#advanced-panel .feather-autocomplete-input')
    props.items.forEach((item: any, index) => {
      if (inputs[index]) {
        inputs[index].textContent = item?.key.name
      }
    })
  }
}

/**
 * When you activate the advanced section, our code waits
 * for 150 milliseconds to give FeatherExpansion panel a chance to populate the DOM
 * If FeatherAutoComplete ever includes the option to set a single value
 * by default to the textarea, then this can be removed.
 */
watch(props, () => {
  if (props.active) {
    setTimeout(() => {
      fillAutoComplete()
    }, 150)
  }
})
</script>

<style lang="scss">
@import "@featherds/styles/mixins/typography";
#advanced-panel {
  position: relative;
  a[data-ref-id="feather-form-element-clear"] {
    display: none;
  }
  .feather-expansion-header-button-text {
    @include headline4();
    color: var(--feather-primary);
  }
}
</style>
<style lang="scss" scoped>
.icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  top: 13px;
  right: 60px;
  > button {
    margin: 0;
  }
}
.item-wrapper {
  display: flex;
  > div {
    width: 100%;
  }
  > div:first-child {
    margin-right: 16px;
  }
  > button:last-child {
    margin-left: 8px;
  }
}
.button-icon {
  font-size: 24px;
  padding-top: 2px;
  margin-right: 8px;
}
.button-wrapper {
  display: flex;
  justify-content: flex-end;
}
.delete-icon {
  color: var(--feather-error);
}
</style>

