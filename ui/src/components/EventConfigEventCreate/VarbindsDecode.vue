<template>
  <div class="varbinds-decode-info">
    <div class="section-content">
      <div class="varbinds-decode-header">
        <div class="varbinds-decode-title">
          <h3>Varbinds Decoding</h3>
          <p>
            Convert the following numeric values for the varbind parm to the decoded string value when displaying the
            event description:
          </p>
        </div>
        <Button
          outlined
          @click="$emit('setVarbindsDecode', 'addVarbindDecodeRow', null, -1, -1)"
          data-test="add-varbind-row-button"
        >
          <FeatherIcon :icon="Add" />
          Add
        </Button>
      </div>
      <div
        v-for="(row, index) in varbindsDecodeElements"
        :key="index"
        class="form-row"
      >
        <div class="parm-field">
          <div class="input-field">
            <FormField
              :for="`varbind-parmid-${index}`"
              :error="errors.varbindsDecode?.[index]?.parmId"
            >
              <InputText
                :id="`varbind-parmid-${index}`"
                :modelValue="row.parmId"
                @update:model-value="$emit('setVarbindsDecode', 'setParmId', $event, index, -1)"
                data-test="varbind-index-input"
                :invalid="!!errors.varbindsDecode?.[index]?.parmId"
                fluid
                placeholder="Parm ID"
                :aria-label="'Parm ID'"
              />
            </FormField>
          </div>
          <div class="action-btns">
            <Button
              class="remove"
              outlined
              severity="danger"
              data-test="remove-varbind-row-button"
              @click="$emit('setVarbindsDecode', 'removeVarbindDecodeRow', null, index, -1)"
            >
              <FeatherIcon :icon="Delete" />
            </Button>
            <Button
              outlined
              data-test="add-varbind-row-button"
              @click="$emit('setVarbindsDecode', 'addDecodeRow', null, index, -1)"
            >
              <FeatherIcon :icon="Add" />
              Add Decode
            </Button>
          </div>
        </div>
        <div
          v-for="(decodeRow, decodeIndex) in row.decode"
          :key="decodeIndex"
          class="decode-field"
        >
          <div class="input-field">
            <FormField
              :for="`decode-value-${index}-${decodeIndex}`"
              :error="errors.varbindsDecode?.[index]?.decode?.[decodeIndex]?.value"
            >
              <InputText
                :id="`decode-value-${index}-${decodeIndex}`"
                type="number"
                min="0"
                :modelValue="decodeRow.value"
                @update:model-value="$emit('setVarbindsDecode', 'setDecodeValue', $event, index, decodeIndex)"
                data-test="varbind-value-input"
                :invalid="!!errors.varbindsDecode?.[index]?.decode?.[decodeIndex]?.value"
                fluid
                placeholder="Varbind Value"
                :aria-label="'Varbind Value'"
              />
            </FormField>
          </div>
          <div class="value-field">
            <div class="input-field">
              <FormField
                :for="`decode-key-${index}-${decodeIndex}`"
                :error="errors.varbindsDecode?.[index]?.decode?.[decodeIndex]?.key"
              >
                <InputText
                  :id="`decode-key-${index}-${decodeIndex}`"
                  :modelValue="decodeRow.key"
                  @update:model-value="$emit('setVarbindsDecode', 'setDecodeKey', $event, index, decodeIndex)"
                  data-test="decode-key-input"
                  :invalid="!!errors.varbindsDecode?.[index]?.decode?.[decodeIndex]?.key"
                  fluid
                  placeholder="Decoded String"
                  :aria-label="'Decoded String'"
                />
              </FormField>
            </div>
            <Button
              class="remove"
              outlined
              severity="danger"
              data-test="remove-varbind-row-button"
              @click="$emit('setVarbindsDecode', 'removeDecodeRow', null, index, decodeIndex)"
            >
              <FeatherIcon :icon="Delete" />
            </Button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, toRefs, watch } from 'vue'

import { EventFormErrors } from '@/types/eventConfig'
import { FeatherIcon } from '@featherds/icon'
import Add from '@featherds/icon/action/Add'
import Delete from '@featherds/icon/action/Delete'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import FormField from '@/components/Common/FormField.vue'

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
    gap: 20px;

    .varbinds-decode-title {
      flex: 1;
    }
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
      }
    }
  }

}
</style>
