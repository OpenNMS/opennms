<template>
  <div
    class="mask-elements"
    v-if="store.selectedSource && store.eventModificationState.eventConfigEvent"
  >
    <div class="section-content">
      <h3 class="mask-elements-header">
        Mask Elements
        <FeatherIcon
          :icon="Add"
          class="icon-size add-icon heading-add-icon"
          @click="addMaskRow"
        />
      </h3>

      <div
        v-for="(row, index) in maskElements"
        :key="index"
        class="form-row"
      >
        <div class="dropdown">
          <FeatherSelect
            label="Element Name"
            :options="availableMaskOptions(index)"
            :modelValue="MaskElementNameOptions.find(
              (o: ISelectItemType) => o._value === row.name._value
            )"
            @update:modelValue="(val?: ISelectItemType) => handleSelectChange(val, index)"
            data-test="mask-element-name"
          />
        </div>

        <div class="input-field">
          <FeatherInput
            label="Element Value"
            v-model.trim="row.value"
            @input="(event: InputEvent) => handleInputChange(event, index)"
            data-test="mask-element-value"
          />
        </div>

        <div class="rule-icon-buttons">
          <FeatherIcon
            v-if="maskElements.length > 1"
            :icon="Cancel"
            class="icon-size remove-icon"
            @click="removeMaskRow(index)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { FeatherIcon } from '@featherds/icon'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import { MaskElementNameOptions } from './constants'
import Add from '@featherds/icon/action/Add'
import Cancel from '@featherds/icon/navigation/Cancel'
import { MaskElement } from '@/types/eventConfig'

const store = useEventModificationStore()

const maskElements = ref<MaskElement[]>([
  { name: { _text: '', _value: '' }, value: '' }
])

const addMaskRow = () => {
  maskElements.value.push({
    name: { _text: '', _value: '' },
    value: ''
  })
}

const removeMaskRow = (index: number) => {
  maskElements.value.splice(index, 1)
}

const handleSelectChange = (val?: ISelectItemType, index?: number) => {
  if (index === undefined) return
  maskElements.value[index].name = val || { _text: '', _value: '' }
  maskElements.value[index].value = ''
}

const handleInputChange = (event: InputEvent, index: number) => {
  const value = (event.target as HTMLInputElement).value
  maskElements.value[index].value = value
}

const availableMaskOptions = (index: number): ISelectItemType[] => {
  const selectedNames = maskElements.value.map(r => r.name._value)
  return MaskElementNameOptions.filter(option => {
    const value = option._value as string
    return (
      !selectedNames.includes(value) ||
      maskElements.value[index].name._value === value
    )
  })
}
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';

.mask-elements {
  .mask-elements-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
    font-weight: 600;

    .heading-add-icon {
      cursor: pointer;
      font-size: 1.4rem;

      &:hover {
        color: #0039cb;
      }
    }
  }

  .form-row {
    display: flex;
    align-items: flex-start;
    gap: 20px;
    flex-wrap: wrap;
    margin-bottom: 10px;

    .dropdown,
    .input-field {
      flex: 1;
      min-width: 200px;
    }
  }

  .rule-icon-buttons {
    display: flex;
    align-items: center;
    padding-top: 5px;

    .icon-size {
      font-size: 1.5rem;
    }

    .remove-icon {
      color: #d32f2f;
      cursor: pointer;
      transition: color 0.2s ease;

      &:hover {
        color: #b71c1c;
      }
    }
  }
}

</style>
