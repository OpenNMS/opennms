<template>
  <div
    class="mask-elements"
    v-if="store.selectedSource && store.eventModificationState.eventConfigEvent"
  >
    <div class="section-content">
      <div class="mask-elements-header">
        <h3>Mask Elements</h3>
        <FeatherButton
          secondary
          @click="$emit('setMaskElements', 'addMaskRow', null, -1)"
          data-test="add-mask-row-button"
        >
          <FeatherIcon :icon="Add" />
          Add
        </FeatherButton>
      </div>
      <div
        v-for="(row, index) in maskElements"
        :key="index"
        class="form-row"
      >
        <div class="dropdown">
          <FeatherSelect
            label="Element Name"
            :options="availableMaskOptions(index)"
            :error="errors.maskElements?.[index]?.name"
            :modelValue="MaskElementNameOptions.find(
              (o: ISelectItemType) => o._value === row.name._value
            )"
            @update:modelValue="$emit('setMaskElements', 'setName', $event, index)"
            data-test="mask-element-name"
          />
        </div>
        <div class="input-field">
          <FeatherInput
            label="Element Value"
            :model-value="row.value"
            :error="errors.maskElements?.[index]?.value"
            @update:model-value="$emit('setMaskElements', 'setValue', $event, index)"
            data-test="mask-element-value"
          />
          <FeatherButton
            secondary
            data-test="remove-mask-row-button"
            @click="$emit('setMaskElements', 'removeMaskRow', null, index)"
          >
            <FeatherIcon :icon="Delete" />
          </FeatherButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { EventFormErrors } from '@/types/eventConfig'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Add from '@featherds/icon/action/Add'
import Delete from '@featherds/icon/action/Delete'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import { MaskElementNameOptions } from './constants'

defineEmits<{
  (e: 'setMaskElements', key: string, value: any, index: number): void
}>()
const props = defineProps<{
  maskElements: Array<{ name: ISelectItemType; value: string }>
  errors: EventFormErrors
}>()

const store = useEventModificationStore()
const elements = ref<Array<{ name: ISelectItemType; value: string }>>([
  { name: { _text: '', _value: '' }, value: '' }
])

const availableMaskOptions = (index: number): ISelectItemType[] => {
  const selectedNames = elements.value.map(r => r.name._value)
  return MaskElementNameOptions.filter(option => {
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
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';

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

      >div {
        flex: 1;
      }

      >button {
        min-width: 40px !important;
        height: 40px !important;
        display: flex;
        align-items: center;
        justify-content: center;
        line-height: 0px;

        span {
          svg {
            fill: #a5021f;
            font-size: 22px;
          }
        }
      }
    }
  }

}
</style>

