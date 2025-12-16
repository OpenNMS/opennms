<template>
  <div
    class="mask-varbinds"
    v-if="store.selectedSource && store.eventModificationState.eventConfigEvent"
  >
    <div class="section-content">
      <div class="mask-varbinds-header">
        <h3>Mask Varbinds</h3>
        <FeatherButton
          secondary
          @click="$emit('setVarbinds', 'addVarbindRow', null, -1)"
          data-test="add-varbind-row-button"
          :disabled="!hasMaskElements"
        >
          <FeatherIcon :icon="Add" />
          Add
        </FeatherButton>
      </div>
      <div
        v-for="(row, index) in maskVarbinds"
        :key="index"
        class="form-row"
      >
        <div class="dropdown">
          <FeatherSelect
            label="Varbind Type"
            :options="MaskVarbindsTypeOptions"
            :modelValue="MaskVarbindsTypeOptions.find(
              (o: ISelectItemType) => o._value === row.type._value
            )"
            @update:modelValue="$emit('setVarbinds', 'setVarbindType', $event, index)"
            :error="errors.varbinds?.[index]?.type"
            data-test="varbind-oid-input"
          />
        </div>
        <div v-if="row.type._value === MaskVarbindsTypeValue.vbNumber" class="dropdown">
          <FeatherInput
            type="number"
            label="Varbind Number"
            min="0"
            :model-value="row.index"
            @update:model-value="$emit('setVarbinds', 'setIndex', $event, index)"
            data-test="varbind-index-input"
            :error="errors.varbinds?.[index]?.index"
          />
        </div>
        <div v-if="row.type._value === MaskVarbindsTypeValue.vboid" class="dropdown">
          <FeatherInput
            label="Varbind OID"
            :model-value="row.index"
            @update:model-value="$emit('setVarbinds', 'setIndex', $event, index)"
            data-test="varbind-index-input"
            :error="errors.varbinds?.[index]?.index"
          />
        </div>
        <div class="input-field">
          <FeatherInput
            label="Varbind Value"
            :model-value="row.value"
            @update:model-value="$emit('setVarbinds', 'setValue', $event, index)"
            data-test="varbind-value-input"
            :error="errors.varbinds?.[index]?.value"
          />
          <FeatherButton
            secondary
            data-test="remove-varbind-row-button"
            @click="$emit('setVarbinds', 'removeVarbindRow', null, index)"
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
import { MaskVarbindsTypeOptions, MaskVarbindsTypeValue } from './constants'

const emit = defineEmits<{
  (e: 'setVarbinds', key: string, value: any, index: number): void
}>()

const props = defineProps<{
  varbinds: Array<{ index: string; value: string, type: ISelectItemType }>
  maskElements: Array<{ name: ISelectItemType; value: string }>
  errors: EventFormErrors
}>()

const store = useEventModificationStore()
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
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';

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

