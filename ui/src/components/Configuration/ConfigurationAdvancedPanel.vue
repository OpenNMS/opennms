<template>
  <FeatherExpansionPanel
    id="advanced-panel"
    class="expansion-panel advanced-panel"
    title="Advanced Options (not required)"
    :modelValue="props.active"
    @update:modelValue="props.activeUpdate"
  >
 
    <div>
      <div v-bind:key="index" v-for="(item, index) in props.items" class="item-wrapper">
        <FeatherAutocomplete
          type="single"
          label="Key"
          textProp="name"
          @search="(query: string) => search(query, props.type,props.subType, index)"
          v-model="item.key"
          :results="results.list[index]"
        ></FeatherAutocomplete>
        <FeatherInput label="Value" hint="Hint Text" v-model="item.value" />
        <FeatherButton icon="Delete" @click="() => deleteAdvancedOption(index)">
          <FeatherIcon class="delete-icon" :icon="Delete"></FeatherIcon>
        </FeatherButton>
      </div>
      <div class="button-wrapper">
        <FeatherButton @click="addAdvancedOption" primary>
          <FeatherIcon :icon="Add" class="button-icon" />Add Advanced Option
        </FeatherButton>
      </div>
    </div>
  </FeatherExpansionPanel>
</template>

<script setup lang="ts">
import { reactive, PropType, watch } from 'vue'

import { FeatherExpansionPanel } from '@featherds/expansion'
import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'
import { FeatherInput } from '@featherds/input'
import { FeatherAutocomplete } from '@featherds/autocomplete'

import Add from '@featherds/icon/action/Add'
import Delete from '@featherds/icon/action/Delete'

import { advancedKeys,dnsKeys, openDaylightKeys,aciKeys,zabbixKeys,prisKeys } from './copy/advancedKeys'
import { RequisitionPluginSubTypes, RequisitionTypes } from './copy/requisitionTypes'

/**
 * Props
 */
const props = defineProps({
  items: { type: Array as PropType<Array<AdvancedOption>>, required: true },
  type: {type: String, required:true},
  subType: {type: String, required:true},
  addAdvancedOption: { type: Function, required: true },
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

const getKeysBasedOnType = (type:string,subType:string) => {
  
  let keys = new Array<AdvancedKey>();

  if (type === RequisitionTypes.DNS){
    keys = dnsKeys;
  }else if (type === RequisitionTypes.VMWare){
    keys = advancedKeys;
  }else if (type === RequisitionTypes.RequisitionPlugin){
    if (subType === RequisitionPluginSubTypes.OpenDaylight){
      keys = openDaylightKeys;
    }else if (subType === RequisitionPluginSubTypes.ACI){
      keys = aciKeys;
    }else if (subType === RequisitionPluginSubTypes.Zabbix){
      keys = zabbixKeys;
    }else if (subType === RequisitionPluginSubTypes.PRIS){
      keys = prisKeys;
    }
  }
  return keys;
}

/**
 * 
 * @param searchVal The Key Name to search for
 * @param index Since there are multiple search boxes, we need to know which one for which to generate results.
 */
const search = (searchVal: string, type:string,subType:string, index: number) => {
  const advancedKeys = getKeysBasedOnType(type,subType)
  let newResu = advancedKeys.filter((key) => key.name.includes(searchVal) || key.name === searchVal)
  if (newResu.length === 0) {
    newResu.push({ name: searchVal, _text: searchVal, id: props.items?.length || 1 })
  }
  newResu = newResu.filter((res) => {
    let includeInResults = true;
    props.items.forEach((item) => {
      if (item.key.name === res.name){
        includeInResults = false;
      }
    })
    return includeInResults;
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