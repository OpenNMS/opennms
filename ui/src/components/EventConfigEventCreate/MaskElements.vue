<template>
  <div class="mask-elements">
    <div class="section-content">
      <div class="mask-elements-header">
        <h3>Mask Elements</h3>
        <OnmsButton
          variant="outlined"
          @click="$emit('setMaskElements', 'addMaskRow', null, -1)"
          data-test="add-mask-row-button"
        >
          <OnmsIcon :icon="Add" />
          Add
        </OnmsButton>
      </div>
      <div
        v-for="(row, index) in maskElements"
        :key="index"
        class="form-row"
      >
        <div class="dropdown">
          <FormField
            :for="`mask-element-name-${index}`"
            :error="errors.maskElements?.[index]?.name"
          >
            <OnmsSelect
              :inputId="`mask-element-name-${index}`"
              :options="availableMaskOptions(index)"
              optionLabel="_text"
              showClear
              :invalid="!!errors.maskElements?.[index]?.name"
              :modelValue="MaskElementNameOptions.find(
                (o: ISelectItemType) => o._value === row.name._value
              )"
              @update:modelValue="$emit('setMaskElements', 'setName', $event, index)"
              data-test="mask-element-name"
              fluid
              placeholder="Name"
              :aria-label="'Name'"
            />
          </FormField>
        </div>
        <div class="input-field">
          <div class="value-input">
            <FormField
              :for="`mask-element-value-${index}`"
              :error="errors.maskElements?.[index]?.value"
            >
              <OnmsInputText
                :id="`mask-element-value-${index}`"
                :modelValue="row.value"
                :invalid="!!errors.maskElements?.[index]?.value"
                @update:model-value="$emit('setMaskElements', 'setValue', $event, index)"
                data-test="mask-element-value"
                fluid
                placeholder="Value"
                :aria-label="'Value'"
              />
            </FormField>
          </div>
          <OnmsButton
            variant="outlined"
            severity="danger"
            data-test="remove-mask-row-button"
            @click="$emit('setMaskElements', 'removeMaskRow', null, index)"
          >
            <OnmsIcon :icon="Delete" />
          </OnmsButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

import { EventFormErrors } from '@/types/eventConfig'
import { OnmsButton, OnmsIcon, OnmsInputText, OnmsSelect } from '@opennms/onms-ui'
import Add from '@opennms/onms-ui/icons/action/Add.vue'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import { ISelectItemType } from '@/types'
import FormField from '@/components/Common/FormField.vue'
import { MaskElementNameOptions } from './constants'

defineEmits<{
  (e: 'setMaskElements', key: string, value: any, index: number): void
}>()

const props = defineProps<{
  maskElements: Array<{ name: ISelectItemType; value: string }>
  errors: EventFormErrors
}>()

const elements = ref<Array<{ name: ISelectItemType; value: string }>>([
  { name: { _text: '', _value: '' }, value: '' }
])

const availableMaskOptions = (index: number): ISelectItemType[] => {
  const selectedNames = elements.value.map(r => r.name._value)
  return MaskElementNameOptions.filter((option) => {
    const value = option._value as string
    return (
      !selectedNames.includes(value) ||
      elements.value[index].name._value === value
    )
  })
}

watch(() => props, () => {
  elements.value = props.maskElements
}, { immediate: true, deep: true })
</script>

<style scoped lang="scss">
.mask-elements {
  .mask-elements-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
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
    }

    .input-field {
      display: flex;
      align-items: flex-start;
      gap: 10px;

      .value-input {
        flex: 1;
      }
    }
  }
}
</style>
