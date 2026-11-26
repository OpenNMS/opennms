<template>
  <div class="xml-object-table">
    <OnmsTable :value="rows" :data-test="`${idPrefix}-objects`">
      <OnmsColumn header="Name">
        <template #body="{ data }">
          <OnmsInputText
            :modelValue="data.object.name"
            :inputId="`${idPrefix}-object-name-${data.index}`"
            :data-test="`${idPrefix}-object-name-${data.index}`"
            :invalid="!!data.errors.name"
            :aria-label="`Name of object ${data.index + 1}`"
            fluid
            @update:modelValue="update(data.index, { name: String($event ?? '') })"
          />
        </template>
      </OnmsColumn>
      <OnmsColumn header="Type">
        <template #body="{ data }">
          <OnmsSelect
            :inputId="`${idPrefix}-object-type-${data.index}`"
            :data-test="`${idPrefix}-object-type-${data.index}`"
            :options="typeOptions(data.object.type)"
            :modelValue="data.object.type"
            :invalid="!!data.errors.type"
            :aria-label="`Type of object ${data.index + 1}`"
            @update:modelValue="update(data.index, { type: String($event ?? '') })"
          />
        </template>
      </OnmsColumn>
      <OnmsColumn header="XPath">
        <template #body="{ data }">
          <OnmsInputText
            :modelValue="data.object.xpath"
            :inputId="`${idPrefix}-object-xpath-${data.index}`"
            :data-test="`${idPrefix}-object-xpath-${data.index}`"
            :invalid="!!data.errors.xpath"
            :aria-label="`XPath of object ${data.index + 1}`"
            fluid
            @update:modelValue="update(data.index, { xpath: String($event ?? '') })"
          />
        </template>
      </OnmsColumn>
      <OnmsColumn header="Mappings">
        <template #body="{ data }">
          <OnmsButton
            variant="text"
            :data-test="`${idPrefix}-object-mappings-${data.index}`"
            @click="editingIndex = data.index"
          >
            {{ data.object.mappings.length ? plural(data.object.mappings.length, 'mapping') : 'None' }}
          </OnmsButton>
        </template>
      </OnmsColumn>
      <OnmsColumn header="">
        <template #body="{ data }">
          <OnmsIconButton
            :icon="DeleteIcon"
            severity="danger"
            tooltip="Remove object"
            aria-label="Remove object"
            :data-test="`${idPrefix}-object-remove-${data.index}`"
            @click="remove(data.index)"
          />
        </template>
      </OnmsColumn>
    </OnmsTable>

    <small v-if="error" class="field-error" role="alert" :data-test="`${idPrefix}-objects-error`">{{ error }}</small>

    <OnmsButton variant="text" :data-test="`${idPrefix}-object-add`" @click="add">Add object</OnmsButton>

    <XmlMappingsDialog
      v-if="editingIndex !== null"
      :visible="true"
      :objectName="modelValue[editingIndex]?.name ?? ''"
      :mappings="modelValue[editingIndex]?.mappings ?? []"
      @apply="onApplyMappings"
      @cancel="editingIndex = null"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import XmlMappingsDialog from '@/components/XmlCollectionConfiguration/XmlMappingsDialog.vue'
import { plural } from '@/lib/utils'
import { validateObject } from '@/lib/xmlCollectionValidator'
import { getDefaultObject } from '@/stores/xmlCollectionConfigStore'
import type { XmlMapping, XmlObject } from '@/types/xmlCollectionConfig'
import { OnmsButton, OnmsColumn, OnmsIconButton, OnmsInputText, OnmsSelect, OnmsTable } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'

const props = withDefaults(defineProps<{
  modelValue: XmlObject[]
  idPrefix: string
  attributeTypes?: string[]
  error?: string
}>(), {
  attributeTypes: () => ['gauge', 'counter', 'string'],
  error: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: XmlObject[]]
}>()

const editingIndex = ref<number | null>(null)

const rows = computed(() => {
  const names = props.modelValue.map(object => object.name.trim())
  return props.modelValue.map((object, index) => ({
    object,
    index,
    errors: validateObject(object, names.filter((_, i) => i !== index))
  }))
})

// Stored types may use an alias such as GAUGE or counter32; keep them selectable rather than blanking the field.
const typeOptions = (current: string) =>
  current && !props.attributeTypes.includes(current) ? [current, ...props.attributeTypes] : props.attributeTypes

const update = (index: number, change: Partial<XmlObject>) => {
  emit('update:modelValue', props.modelValue.map((object, i) => (i === index ? { ...object, ...change } : object)))
}

const add = () => {
  emit('update:modelValue', [...props.modelValue, getDefaultObject()])
}

const remove = (index: number) => {
  emit('update:modelValue', props.modelValue.filter((_, i) => i !== index))
}

const onApplyMappings = (mappings: XmlMapping[]) => {
  if (editingIndex.value !== null) {
    update(editingIndex.value, { mappings })
  }
  editingIndex.value = null
}
</script>

<style lang="scss" scoped>
.xml-object-table {
  .field-error {
    display: block;
    margin-top: 0.25rem;
    font-size: 0.875rem;
    color: var(--p-red-500);
  }
}
</style>
