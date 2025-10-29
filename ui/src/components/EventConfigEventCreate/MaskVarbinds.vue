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
          @click="handleAddVarbind"
          data-test="add-varbind-row-button"
          :disabled="!canAddVarbind"
        >
          <FeatherIcon :icon="Add" />
          Add
        </FeatherButton>
      </div>

      <div
        v-for="(row, index) in varbinds"
        :key="index"
        class="form-row"
      >
        <div class="dropdown">
          <FeatherInput
            type="number"
            label="Varbind Index"
            min="0"
            :model-value="row.index"
            @update:model-value="emit('setVarbinds', 'setIndex', $event, index)"
            data-test="varbind-index-input"
          />
        </div>

        <div class="input-field">
          <FeatherInput
            label="Varbind Value"
            :model-value="row.value"
            @update:model-value="emit('setVarbinds', 'setValue', $event, index)"
            data-test="varbind-value-input"
          />

          <FeatherButton
            secondary
            data-test="remove-varbind-row-button"
            @click="emit('setVarbinds', 'removeVarbindRow', null, index)"
          >
            <FeatherIcon :icon="Delete" />
          </FeatherButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { EventFormErrors } from '@/types/eventConfig'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Add from '@featherds/icon/action/Add'
import Delete from '@featherds/icon/action/Delete'
import { FeatherInput } from '@featherds/input'

const emit = defineEmits<{
  (e: 'setVarbinds', key: string, value: any, index: number): void
}>()

const props = defineProps<{
  varbinds: Array<{ index: number; value: string }>
  maskElements: Array<{ name: any; value: string }>
  errors: EventFormErrors
}>()

const store = useEventModificationStore()

const hasValidMaskElement = computed(() => {
  return props.maskElements.some(
    (mask) =>
      mask.name &&
      mask.name._value &&
      mask.name._value.trim() !== '' &&
      mask.value &&
      mask.value.trim() !== ''
  )
})

const canAddVarbind = computed(() => {
  return hasValidMaskElement.value
})

const handleAddVarbind = () => {
  if (!canAddVarbind.value) return
  emit('setVarbinds', 'addVarbindRow', null, -1)
}

watch(
  () => props.maskElements,
  (newMaskElements) => {
    const hasAnyValid = newMaskElements.some(
      (mask) =>
        mask.name &&
        mask.name._value &&
        mask.name._value.trim() !== '' &&
        mask.value &&
        mask.value.trim() !== ''
    )

    if (!hasAnyValid && props.varbinds.length > 0) {
      emit('setVarbinds', 'clearAllVarbinds', null, -1)
    }
  },
  { deep: true, immediate: true }
)
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

      > div {
        flex: 1;
      }

      > button {
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
