<template>
  <div class="attribute-list-editor">
    <div
      v-for="(attrib, index) in modelValue"
      :key="index"
      class="row"
    >
      <FormField label="Alias" :for="`http-attrib-alias-${index}`">
        <OnmsInputText
          :modelValue="attrib.alias"
          :inputId="`http-attrib-alias-${index}`"
          :data-test="`http-attrib-alias-${index}`"
          :maxlength="MAX_DS_NAME_LENGTH"
          :invalid="!attrib.alias?.trim()"
          fluid
          @update:modelValue="onUpdate(index, { alias: String($event ?? '') })"
        />
      </FormField>
      <FormField label="Match group" :for="`http-attrib-group-${index}`">
        <OnmsInputNumber
          :modelValue="attrib.matchGroup"
          :inputId="`http-attrib-group-${index}`"
          :data-test="`http-attrib-group-${index}`"
          :min="1"
          fluid
          @update:modelValue="onUpdate(index, { matchGroup: $event })"
        />
      </FormField>
      <FormField label="Type" :for="`http-attrib-type-${index}`">
        <OnmsSelect
          :modelValue="normalizedType(attrib.type)"
          :inputId="`http-attrib-type-${index}`"
          :data-test="`http-attrib-type-${index}`"
          :options="types"
          @update:modelValue="onUpdate(index, { type: String($event ?? '') })"
        />
      </FormField>
      <OnmsIconButton
        :icon="DeleteIcon"
        severity="danger"
        tooltip="Remove attribute"
        aria-label="Remove attribute"
        :data-test="`http-attrib-remove-${index}`"
        @click="onRemove(index)"
      />
    </div>

    <p v-if="!modelValue.length" class="empty">No attributes configured; the URI is requested but nothing is stored.</p>

    <OnmsButton variant="text" data-test="http-attrib-add" @click="onAdd">Add attribute</OnmsButton>
  </div>
</template>

<script setup lang="ts">
import FormField from '@/components/Common/FormField.vue'
import { MAX_DS_NAME_LENGTH } from '@/lib/httpCollectionValidator'
import { getDefaultAttrib } from '@/stores/httpCollectionStore'
import type { HttpAttrib } from '@/types/httpCollectionConfig'
import { OnmsButton, OnmsIconButton, OnmsInputNumber, OnmsInputText, OnmsSelect } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'

const props = defineProps<{
  modelValue: HttpAttrib[]
  types: string[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: HttpAttrib[]]
}>()

// Stored configurations may use an SNMP-style alias such as counter32; show the type it stands for.
const ALIASES: Record<string, string> = { gauge32: 'gauge', integer32: 'gauge', counter32: 'counter' }
const normalizedType = (type: string) => ALIASES[type?.toLowerCase()] ?? type?.toLowerCase()

const onUpdate = (index: number, change: Partial<HttpAttrib>) => {
  emit('update:modelValue', props.modelValue.map((attrib, i) => (i === index ? { ...attrib, ...change } : attrib)))
}

const onAdd = () => {
  emit('update:modelValue', [...props.modelValue, getDefaultAttrib()])
}

const onRemove = (index: number) => {
  emit('update:modelValue', props.modelValue.filter((_, i) => i !== index))
}
</script>

<style lang="scss" scoped>
.attribute-list-editor {
  .row {
    display: grid;
    grid-template-columns: 2fr 1fr 1fr auto;
    gap: 0.75em;
    align-items: end;
    margin-bottom: 0.5em;
  }

  .empty {
    margin: 0.25em 0;
    color: var(--p-text-muted-color);
  }
}
</style>
