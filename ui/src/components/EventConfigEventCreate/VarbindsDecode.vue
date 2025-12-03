<template>
  <div
    class="varbinds-decode-info"
    v-if="store.selectedSource && store.eventModificationState.eventConfigEvent"
  >
    <div class="section-content">
      <div class="varbinds-decode-header">
        <div>
          <h3>Varbinds Decoding</h3>
          <p>
            Convert the following numeric values for the varbind parm to the decoded string value when displaying the
            event description:
          </p>
        </div>
        <FeatherButton
          secondary
          @click="$emit('setVarbindsDecode', 'addVarbindDecodeRow', null, -1, -1)"
          data-test="add-varbind-row-button"
        >
          <FeatherIcon :icon="Add" />
          Add
        </FeatherButton>
      </div>
      <div
        v-for="(row, index) in varbindsDecodeElements"
        :key="index"
        class="form-row"
      >
        <div class="parm-field">
          <div class="input-field">
            <FeatherInput
              label="Parm ID"
              :model-value="row.parmId"
              @update:model-value="$emit('setVarbindsDecode', 'setParmId', $event, index, -1)"
              data-test="varbind-index-input"
              :error="errors.varbindsDecode?.[index]?.parmId"
            />
          </div>
          <div class="action-btns">
            <FeatherButton
              class="remove"
              secondary
              data-test="remove-varbind-row-button"
              @click="$emit('setVarbindsDecode', 'removeVarbindDecodeRow', null, index, -1)"
            >
              <FeatherIcon :icon="Delete" />
            </FeatherButton>
            <FeatherButton
              secondary
              data-test="add-varbind-row-button"
              @click="$emit('setVarbindsDecode', 'addDecodeRow', null, index, -1)"
            >
              <FeatherIcon :icon="Add" />
              Add Decode
            </FeatherButton>
          </div>
        </div>
        <div
          v-for="(decodeRow, decodeIndex) in row.decode"
          :key="decodeIndex"
          class="decode-field"
        >
          <div class="input-field">
            <FeatherInput
              label="Varbind Value"
              type="number"
              min="0"
              :model-value="decodeRow.value"
              @update:model-value="$emit('setVarbindsDecode', 'setDecodeValue', $event, index, decodeIndex)"
              data-test="varbind-value-input"
              :error="errors.varbindsDecode?.[index]?.decode?.[decodeIndex]?.value"
            />
          </div>
          <div class="value-field">
            <div class="input-field">
              <FeatherInput
                label="Decoded String"
                :model-value="decodeRow.key"
                @update:model-value="$emit('setVarbindsDecode', 'setDecodeKey', $event, index, decodeIndex)"
                data-test="varbind-value-input"
                :error="errors.varbindsDecode?.[index]?.decode?.[decodeIndex]?.key"
              />
            </div>
            <FeatherButton
              class="remove"
              secondary
              data-test="remove-varbind-row-button"
              @click="$emit('setVarbindsDecode', 'removeDecodeRow', null, index, decodeIndex)"
            >
              <FeatherIcon :icon="Delete" />
            </FeatherButton>
          </div>
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

const store = useEventModificationStore()
const props = defineProps<{
  varbindsDecode: Array<{ parmId: string; decode: Array<{ key: string; value: string }> }>
  errors: EventFormErrors
}>()
defineEmits<{
  (e: 'setVarbindsDecode', key: string, value: any, index: number, decodeIndex: number): void
}>()

const { varbindsDecode, errors } = toRefs(props)
const varbindsDecodeElements = ref<Array<{ parmId: string; decode: Array<{ key: string; value: string }> }>>([])

watch(varbindsDecode, (newVarbindsDecode) => {
  varbindsDecodeElements.value = [...newVarbindsDecode]
}, { deep: true, immediate: true })
</script>

<style lang="scss" scoped>
.varbinds-decode-info {
  .varbinds-decode-header {
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

    .parm-field {
      width: 100%;
      display: flex;
      align-items: flex-start;
      gap: 10px;

      .input-field {
        width: 100%;
      }

      .action-btns {
        display: flex;
        align-items: center;
        gap: 10px;

        button {
          margin: 0px;
        }

        .remove {
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

    .decode-field {
      width: 100%;
      display: flex;
      align-items: flex-start;
      gap: 10px;

      .input-field,
      .value-field {
        flex: 1;
      }

      .value-field {
        display: flex;
        align-items: flex-start;
        gap: 10px;

        .input-field {
          width: 100%;
        }

        .remove {
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
}
</style>