<template>
  <div class="mask-varbinds">
    <div class="section-content">
      <div class="mask-varbinds-header">
        <h3>Mask Varbinds</h3>
        <OnmsButton
          variant="outlined"
          @click="$emit('setVarbinds', 'addVarbindRow', null, -1)"
          data-test="add-varbind-row-button"
          :disabled="!hasMaskElements"
        >
          <OnmsIcon :icon="Add" />
          Add
        </OnmsButton>
      </div>
      <div
        v-for="(row, index) in maskVarbinds"
        :key="index"
        class="form-row"
      >
        <div class="dropdown">
          <FormField
            :for="`varbind-type-${index}`"
            :error="errors.varbinds?.[index]?.type"
          >
            <OnmsSelect
              :inputId="`varbind-type-${index}`"
              :options="MaskVarbindsTypeOptions"
              optionLabel="_text"
              :modelValue="MaskVarbindsTypeOptions.find(
                (o: ISelectItemType) => o._value === row.type._value
              )"
              @update:modelValue="$emit('setVarbinds', 'setVarbindType', $event, index)"
              :invalid="!!errors.varbinds?.[index]?.type"
              data-test="varbind-type-select"
              fluid
              placeholder="Varbind Type"
              :aria-label="'Varbind Type'"
            />
          </FormField>
        </div>
        <div
          v-if="row.type._value === MaskVarbindsTypeValue.vbNumber"
          class="dropdown"
        >
          <FormField
            :for="`varbind-number-${index}`"
            :error="errors.varbinds?.[index]?.index"
          >
            <OnmsInputText
              :id="`varbind-number-${index}`"
              type="number"
              min="0"
              :modelValue="row.index"
              @update:model-value="$emit('setVarbinds', 'setVarbindNumber', $event, index)"
              data-test="varbind-number-input"
              :invalid="!!errors.varbinds?.[index]?.index"
              fluid
              placeholder="Varbind Number"
              :aria-label="'Varbind Number'"
            />
          </FormField>
        </div>
        <div
          v-if="row.type._value === MaskVarbindsTypeValue.vbOid"
          class="dropdown"
        >
          <FormField
            :for="`varbind-oid-${index}`"
            :error="errors.varbinds?.[index]?.index"
          >
            <OnmsInputText
              :id="`varbind-oid-${index}`"
              :modelValue="row.index"
              @update:model-value="$emit('setVarbinds', 'setVarbindOid', $event, index)"
              data-test="varbind-oid-input"
              :invalid="!!errors.varbinds?.[index]?.index"
              fluid
              placeholder="Varbind OID"
              :aria-label="'Varbind OID'"
            />
          </FormField>
        </div>
        <div class="input-field">
          <div class="value-input">
            <FormField
              :for="`varbind-value-${index}`"
              :error="errors.varbinds?.[index]?.value"
            >
              <OnmsInputText
                :id="`varbind-value-${index}`"
                :modelValue="row.value"
                @update:model-value="$emit('setVarbinds', 'setValue', $event, index)"
                data-test="varbind-value-input"
                :invalid="!!errors.varbinds?.[index]?.value"
                fluid
                placeholder="Varbind Value"
                :aria-label="'Varbind Value'"
              />
            </FormField>
          </div>
          <OnmsButton
            variant="outlined"
            severity="danger"
            data-test="remove-varbind-row-button"
            @click="$emit('setVarbinds', 'removeVarbindRow', null, index)"
          >
            <OnmsIcon :icon="Delete" />
          </OnmsButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, toRefs, watch } from 'vue'

import { EventFormErrors } from '@/types/eventConfig'
import { OnmsButton, OnmsIcon, OnmsInputText, OnmsSelect } from '@opennms/onms-ui'
import Add from '@/components/icons/action/Add.vue'
import Delete from '@/components/icons/action/Delete.vue'
import { ISelectItemType } from '@/types'
import FormField from '@/components/Common/FormField.vue'
import { MaskVarbindsTypeOptions, MaskVarbindsTypeValue } from './constants'

const emit = defineEmits<{
  (e: 'setVarbinds', key: string, value: any, index: number): void
}>()

const props = defineProps<{
  varbinds: Array<{ index: string; value: string, type: ISelectItemType }>
  maskElements: Array<{ name: ISelectItemType; value: string }>
  errors: EventFormErrors
}>()

const { varbinds, maskElements, errors } = toRefs(props)
const maskVarbinds = ref<Array<{ index: string; value: string, type: ISelectItemType }>>([])
const hasMaskElements = computed(() => maskElements.value.length > 0)

watch(varbinds, () => {
  maskVarbinds.value = [...props.varbinds]
}, { deep: true, immediate: true })

watch(maskElements, () => {
  if (props.maskElements.length === 0) {
    emit('setVarbinds', 'clearAllVarbinds', null, -1)
  }
}, { deep: true, immediate: true })
</script>

<style scoped lang="scss">
.mask-varbinds {
  .mask-varbinds-header {
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
