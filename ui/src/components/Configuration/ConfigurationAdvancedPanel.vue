<template>
  <TogglePanel
    id="advanced-panel"
    class="expansion-panel advanced-panel"
    header="Advanced Options (optional)"
    :collapsed="!props.active"
    @update:collapsed="(v) => props.activeUpdate(!v)"
  >
    <div>
      <div
        v-bind:key="index"
        v-for="(item, index) in props.items"
        class="item-wrapper"
      >
        <FormField label="Key" class="key-field">
          <PAutoComplete
            :modelValue="item.key"
            optionLabel="name"
            :suggestions="results.list[index]"
            dropdown
            @complete="(e) => search(e.query, props.type, props.subType, index)"
            @item-select="(e) => onKeySelect(e.value, index)"
          >
            <template #empty>
              <div class="autocomplete-empty">{{ labels.noResults }}</div>
            </template>
          </PAutoComplete>
        </FormField>
        <FormField label="Value" class="value-field" :hint="item.hint || ' '">
          <PInputText v-model="item.value" />
        </FormField>
        <PButton
          text
          aria-label="Delete"
          v-tooltip="'Delete'"
          @click="() => deleteAdvancedOption(index)"
        >
          <FeatherIcon
            class="delete-icon"
            :icon="Delete"
          ></FeatherIcon>
        </PButton>
      </div>
      <div class="button-wrapper">
        <PButton
          :disabled="buttonAddDisabled"
          @click="addAdvancedOption"
        >Add</PButton>
      </div>
    </div>
  </TogglePanel>
</template>

<script
  setup
  lang="ts"
>
import { PropType, computed, reactive, ref } from 'vue'

import AutoComplete from 'primevue/autocomplete'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import { FeatherIcon } from '@featherds/icon'
import Delete from '@featherds/icon/action/Delete'
import TogglePanel from '@/components/Common/TogglePanel.vue'
import FormField from '@/components/Common/FormField.vue'

import { orderBy } from 'lodash'

import { advancedKeys, dnsKeys, openDaylightKeys, aciKeys, zabbixKeys, prisKeys } from './copy/advancedKeys'
import { RequisitionPluginSubTypes, RequisitionTypes, VMWareFields, LabelStrings } from './copy/requisitionTypes'
import { AdvancedKey, AdvancedOption } from './configuration.types'

const PAutoComplete = AutoComplete
const PButton = Button
const PInputText = InputText

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
  activeUpdate: { type: Function as PropType<(_v: boolean) => void>, required: true },
  helpState: Object
})

/**
 * Local State
 */
const results = reactive({
  list: [[{}]] as AdvancedKey[][]
})
const defaultLabels = { noResults: LabelStrings.duplicateKey }
const labels = ref(defaultLabels)

/**
 * Disabled when last item (key.name and value) is null,
 * hence preventing from adding new item.
 */
const buttonAddDisabled = computed(() => {
  const itemsLength = props.items.length

  if (!itemsLength) {
    return false
  } // enabled

  const { key, value } = props.items[itemsLength - 1] // last item
  return !(key.name && value) // disabled
})

const onKeySelect = (key: AdvancedKey, index: number) => {
  // The parent passes a reactive items array and expects in-place mutation here
  // (the original used v-model="item.key" for the same effect).
  // eslint-disable-next-line vue/no-mutating-props
  props.items[index].key = key
  props.advancedKeyUpdate(key, index)
}

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
  // prevent username/Username/password/Password key, using Advanced Options section, from adding to the URL, since they can be set in their respective input field of the form
  const vmWareFields = Object.entries(VMWareFields).map(e => e[1])
  if (vmWareFields.includes(searchVal)) {
    labels.value = { noResults: LabelStrings.optionNotAvailable }
    results.list[index] = []
    return
  }

  const advancedKeys = getKeysBasedOnType(type, subType)

  //Find keys based on search text.
  let newResu = advancedKeys.filter(key => key.name.includes(searchVal) || key.name === searchVal)

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

  labels.value = defaultLabels

  results.list[index] = [...newResu]
}
</script>

<style
  lang="scss"
  scoped
>
@import "@featherds/styles/mixins/typography";

.advanced-panel {
  :deep(.p-panel-title) {
    @include headline4();
    color: var(--p-primary-color);
  }
}
.item-wrapper {
  display: flex;
  align-items: flex-start;
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
.autocomplete-empty {
  padding: 0.5rem 0.75rem;
}
.button-wrapper {
  display: flex;
  justify-content: flex-end;
}
.delete-icon {
  color: var(--p-red-500);
}
</style>
