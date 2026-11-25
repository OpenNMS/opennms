<template>
  <div class="xml-group-list-editor">
    <TogglePanel
      v-for="(group, index) in modelValue"
      :key="keys[index]"
      :collapsed="collapsed[keys[index]] ?? false"
      class="group-panel"
      :data-test="`${idPrefix}-group-${index}`"
      @update:collapsed="collapsed[keys[index]] = $event"
    >
      <template #header>
        <div class="group-header">
          <span class="group-title">
            {{ group.name || 'New group' }}
            <span class="group-meta">{{ group.resourceType }} · {{ plural(group.objects.length, 'object') }}</span>
            <OnmsTag v-if="hasErrors(validateGroup(group, otherNames(index)))" value="Incomplete" severity="danger" />
          </span>
          <OnmsIconButton
            :icon="DeleteIcon"
            severity="danger"
            tooltip="Remove group"
            aria-label="Remove group"
            :data-test="`${idPrefix}-group-remove-${index}`"
            @click.stop="remove(index)"
          />
        </div>
      </template>
      <XmlGroupForm
        :modelValue="group"
        :idPrefix="`${idPrefix}-group-${index}`"
        :otherNames="otherNames(index)"
        :resourceTypes="resourceTypes"
        :attributeTypes="attributeTypes"
        @update:modelValue="update(index, $event)"
      />
    </TogglePanel>

    <p v-if="!modelValue.length" class="empty" :data-test="`${idPrefix}-groups-empty`">{{ emptyMessage }}</p>

    <OnmsButton variant="outlined" :data-test="`${idPrefix}-group-add`" @click="add">Add group</OnmsButton>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import TogglePanel from '@/components/Common/TogglePanel.vue'
import XmlGroupForm from '@/components/XmlCollectionConfiguration/XmlGroupForm.vue'
import { plural } from '@/lib/utils'
import { hasErrors, validateGroup } from '@/lib/xmlCollectionValidator'
import { getDefaultGroup } from '@/stores/xmlCollectionConfigStore'
import type { XmlGroup, XmlResourceType } from '@/types/xmlCollectionConfig'
import { OnmsButton, OnmsIconButton, OnmsTag } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'

const props = withDefaults(defineProps<{
  modelValue: XmlGroup[]
  idPrefix: string
  resourceTypes?: XmlResourceType[]
  attributeTypes?: string[]
  emptyMessage?: string
}>(), {
  resourceTypes: undefined,
  attributeTypes: undefined,
  emptyMessage: 'No groups defined.'
})

const emit = defineEmits<{
  'update:modelValue': [value: XmlGroup[]]
}>()

// Stable keys, so removing a group does not hand its panel state to the next one. Existing groups start
// collapsed, so a long group file stays scannable; added groups start open.
let nextKey = 0
const keys = ref<number[]>(props.modelValue.map(() => nextKey++))
const collapsed = reactive<Record<number, boolean>>(Object.fromEntries(keys.value.map(key => [key, true])))

// The list can also be replaced from outside, e.g. when the page reloads after a failed save.
watch(() => props.modelValue.length, (length) => {
  while (keys.value.length < length) {
    const key = nextKey++
    keys.value.push(key)
    collapsed[key] = true
  }
  keys.value.splice(length)
})

const otherNames = (index: number) =>
  props.modelValue.filter((_, i) => i !== index).map(group => group.name.trim())

const update = (index: number, group: XmlGroup) => {
  emit('update:modelValue', props.modelValue.map((candidate, i) => (i === index ? group : candidate)))
}

const add = () => {
  const key = nextKey++
  keys.value.push(key)
  collapsed[key] = false
  emit('update:modelValue', [...props.modelValue, getDefaultGroup()])
}

const remove = (index: number) => {
  keys.value.splice(index, 1)
  emit('update:modelValue', props.modelValue.filter((_, i) => i !== index))
}
</script>

<style lang="scss" scoped>
.xml-group-list-editor {
  display: flex;
  flex-direction: column;
  gap: 0.75em;
  align-items: stretch;

  > .p-button,
  > button {
    align-self: flex-start;
  }

  .group-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1em;
    width: 100%;
  }

  .group-title {
    display: flex;
    align-items: center;
    gap: 0.75em;
    font-weight: 700;
  }

  .group-meta {
    font-weight: 400;
    color: var(--p-text-muted-color);
  }

  .empty {
    color: var(--p-text-muted-color);
    margin: 0;
  }
}
</style>
