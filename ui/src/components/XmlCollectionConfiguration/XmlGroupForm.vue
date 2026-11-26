<template>
  <div class="xml-group-form">
    <div class="grid">
      <FormField label="Name" :for="`${idPrefix}-name`" required :error="errors.name">
        <OnmsInputText
          :inputId="`${idPrefix}-name`"
          :data-test="`${idPrefix}-name`"
          :modelValue="modelValue.name"
          :invalid="!!errors.name"
          fluid
          @update:modelValue="update({ name: String($event ?? '') })"
        />
      </FormField>
      <FormField
        label="Resource type"
        :for="`${idPrefix}-resource-type`"
        required
        :error="errors.resourceType"
        :info="XML_FIELD_HINTS.resourceType"
      >
        <OnmsSelect
          :inputId="`${idPrefix}-resource-type`"
          :data-test="`${idPrefix}-resource-type`"
          optionLabel="label"
          optionValue="name"
          :options="resourceTypeOptions"
          :modelValue="modelValue.resourceType"
          :invalid="!!errors.resourceType"
          @update:modelValue="update({ resourceType: String($event ?? '') })"
        />
      </FormField>
      <FormField
        label="Resource XPath"
        :for="`${idPrefix}-resource-xpath`"
        required
        :error="errors.resourceXpath"
        :info="XML_FIELD_HINTS.resourceXpath"
      >
        <OnmsInputText
          :inputId="`${idPrefix}-resource-xpath`"
          :data-test="`${idPrefix}-resource-xpath`"
          :modelValue="modelValue.resourceXpath"
          :invalid="!!errors.resourceXpath"
          fluid
          @update:modelValue="update({ resourceXpath: String($event ?? '') })"
        />
      </FormField>
      <FormField label="Key XPath" :for="`${idPrefix}-key-xpath`" :info="XML_FIELD_HINTS.keyXpath">
        <OnmsInputText
          :inputId="`${idPrefix}-key-xpath`"
          :data-test="`${idPrefix}-key-xpath`"
          :modelValue="modelValue.keyXpath ?? ''"
          fluid
          @update:modelValue="update({ keyXpath: String($event ?? '') })"
        />
      </FormField>
      <FormField label="Timestamp XPath" :for="`${idPrefix}-timestamp-xpath`" :info="XML_FIELD_HINTS.timestampXpath">
        <OnmsInputText
          :inputId="`${idPrefix}-timestamp-xpath`"
          :data-test="`${idPrefix}-timestamp-xpath`"
          :modelValue="modelValue.timestampXpath ?? ''"
          fluid
          @update:modelValue="update({ timestampXpath: String($event ?? '') })"
        />
      </FormField>
      <FormField label="Timestamp format" :for="`${idPrefix}-timestamp-format`" :info="XML_FIELD_HINTS.timestampFormat">
        <OnmsInputText
          :inputId="`${idPrefix}-timestamp-format`"
          :data-test="`${idPrefix}-timestamp-format`"
          :modelValue="modelValue.timestampFormat ?? ''"
          fluid
          @update:modelValue="update({ timestampFormat: String($event ?? '') })"
        />
      </FormField>
    </div>

    <FormField label="Resource key XPaths" :info="XML_FIELD_HINTS.resourceKeys">
      <StringListEditor
        :modelValue="modelValue.resourceKeys"
        itemLabel="key XPath"
        :idPrefix="`${idPrefix}-resource-key`"
        @update:modelValue="update({ resourceKeys: $event })"
      />
    </FormField>

    <FormField label="Objects" required :info="XML_FIELD_HINTS.objects">
      <XmlObjectTable
        :modelValue="modelValue.objects"
        :idPrefix="idPrefix"
        :attributeTypes="attributeTypes"
        :error="errors.objects"
        @update:modelValue="update({ objects: $event })"
      />
    </FormField>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import FormField from '@/components/Common/FormField.vue'
import StringListEditor from '@/components/Common/StringListEditor.vue'
import XmlObjectTable from '@/components/XmlCollectionConfiguration/XmlObjectTable.vue'
import { XML_FIELD_HINTS } from '@/lib/xmlCollectionHelpText'
import { validateGroup } from '@/lib/xmlCollectionValidator'
import type { XmlGroup, XmlResourceType } from '@/types/xmlCollectionConfig'
import { OnmsInputText, OnmsSelect } from '@opennms/onms-ui'

const props = withDefaults(defineProps<{
  modelValue: XmlGroup
  idPrefix: string
  /** Names of the other groups of the same source or group file. */
  otherNames?: string[]
  resourceTypes?: XmlResourceType[]
  attributeTypes?: string[]
}>(), {
  otherNames: () => [],
  resourceTypes: () => [{ name: 'node', label: 'Node' }],
  attributeTypes: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: XmlGroup]
}>()

const errors = computed(() => validateGroup(props.modelValue, props.otherNames))

// A stored resource type the server does not know (yet) stays selectable rather than being blanked.
const resourceTypeOptions = computed(() => {
  const current = props.modelValue.resourceType
  return current && !props.resourceTypes.some(type => type.name === current)
    ? [{ name: current, label: `${current} (unknown resource type)` }, ...props.resourceTypes]
    : props.resourceTypes
})

const update = (change: Partial<XmlGroup>) => {
  emit('update:modelValue', { ...props.modelValue, ...change })
}
</script>

<style lang="scss" scoped>
.xml-group-form {
  display: flex;
  flex-direction: column;
  gap: 1em;

  .grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(18em, 1fr));
    gap: 1em;
  }
}
</style>
