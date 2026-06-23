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
            <IftaLabel>
              <InputText
                :id="`varbind-parmid-${index}`"
                :modelValue="row.parmId"
                @update:model-value="$emit('setVarbindsDecode', 'setParmId', $event, index, -1)"
                data-test="varbind-index-input"
                :invalid="!!errors.varbindsDecode?.[index]?.parmId"
                fluid
              />
              <label :for="`varbind-parmid-${index}`">Parm ID</label>
            </IftaLabel>
            <small
              v-if="errors.varbindsDecode?.[index]?.parmId"
              class="field-error"
            >
              {{ errors.varbindsDecode?.[index]?.parmId }}
            </small>
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
            <IftaLabel>
              <InputText
                :id="`decode-value-${index}-${decodeIndex}`"
                type="number"
                min="0"
                :modelValue="decodeRow.value"
                @update:model-value="$emit('setVarbindsDecode', 'setDecodeValue', $event, index, decodeIndex)"
                data-test="varbind-value-input"
                :invalid="!!errors.varbindsDecode?.[index]?.decode?.[decodeIndex]?.value"
                fluid
              />
              <label :for="`decode-value-${index}-${decodeIndex}`">Varbind Value</label>
            </IftaLabel>
            <small
              v-if="errors.varbindsDecode?.[index]?.decode?.[decodeIndex]?.value"
              class="field-error"
            >
              {{ errors.varbindsDecode?.[index]?.decode?.[decodeIndex]?.value }}
            </small>
          </div>
          <div class="value-field">
            <div class="input-field">
              <IftaLabel>
                <InputText
                  :id="`decode-key-${index}-${decodeIndex}`"
                  :modelValue="decodeRow.key"
                  @update:model-value="$emit('setVarbindsDecode', 'setDecodeKey', $event, index, decodeIndex)"
                  data-test="varbind-value-input"
                  :invalid="!!errors.varbindsDecode?.[index]?.decode?.[decodeIndex]?.key"
                  fluid
                />
                <label :for="`decode-key-${index}-${decodeIndex}`">Decoded String</label>
              </IftaLabel>
              <small
                v-if="errors.varbindsDecode?.[index]?.decode?.[decodeIndex]?.key"
                class="field-error"
              >
                {{ errors.varbindsDecode?.[index]?.decode?.[decodeIndex]?.key }}
              </small>
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
import IftaLabel from 'primevue/iftalabel'
import InputText from 'primevue/inputtext'

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

  .field-error {
    display: block;
    margin-top: 0.25rem;
    color: var(--p-red-500);
  }
}
</style>
