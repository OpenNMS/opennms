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
          <OnmsAutoComplete
            :modelValue="item.key"
            optionLabel="name"
            :suggestions="results.list[index]"
            dropdown
            @complete="(query: string) => search(query, props.type, props.subType, index)"
            @optionSelect="(value: unknown) => onKeySelect(value as AdvancedKey, index)"
            @update:modelValue="(val) => onKeyInput(val, index)"
          >
            <template #empty>
              <div class="autocomplete-empty">{{ labels.noResults }}</div>
            </template>
          </OnmsAutoComplete>
        </FormField>
        <FormField label="Value" class="value-field" :hint="item.hint || ' '">
          <OnmsInputText v-model="item.value" />
        </FormField>
        <OnmsIconButton
          class="delete-icon"
          aria-label="Delete"
          tooltip="Delete"
          :icon="Delete"
          @click="() => deleteAdvancedOption(index)"
        />
      </div>
      <div class="button-wrapper">
        <OnmsButton
          :disabled="buttonAddDisabled"
          @click="addAdvancedOption"
        >Add</OnmsButton>
      </div>
    </div>
  </TogglePanel>
</template>

<script
  setup
  lang="ts"
>
import { PropType, computed, reactive, ref } from 'vue'
import { OnmsAutoComplete, OnmsButton, OnmsIconButton, OnmsInputText } from '@opennms/onms-ui'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import TogglePanel from '@/components/Common/TogglePanel.vue'
import FormField from '@/components/Common/FormField.vue'
import { orderBy } from 'lodash'
import { advancedKeys, dnsKeys, openDaylightKeys, aciKeys, zabbixKeys, prisKeys } from './copy/advancedKeys'
import { RequisitionPluginSubTypes, RequisitionTypes, VMWareFields, LabelStrings } from './copy/requisitionTypes'
import { AdvancedKey, AdvancedOption } from './configuration.types'

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

const onKeyInput = (val: unknown, index: number) => {
  if (val === null) {
    // eslint-disable-next-line vue/no-mutating-props
    props.items[index].key = { name: '', _text: '', id: (props.items[index]?.key as any)?.id ?? index + 1 }
    props.advancedKeyUpdate(props.items[index].key, index)
    return
  }

  const key: AdvancedKey = typeof val === 'string'
    ? { name: val, _text: val, id: (props.items[index]?.key as any)?.id ?? index + 1 }
    : (val as AdvancedKey)

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
@import '@/styles/onms-typography';

.advanced-panel {
  :deep(.p-panel-title) {
    @include onms-headline4();
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
  // Offset past the FormField label (~1.6875rem) and center within the input's
  // height so the delete icon lines up with the input row, not the hint below it.
  > button:last-child {
    margin-left: 8px;
    margin-top: 1.6875rem;
    height: 3rem;
    display: flex;
    align-items: center;
  }
}
.autocomplete-empty {
  padding: 0.5rem 0.75rem;
}
.button-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 1rem;
}
.delete-icon {
  color: var(--p-red-500);
}
</style>
