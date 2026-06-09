<template>
  <FeatherDropdown class="actions-dropdown-button">
    <template v-slot:trigger="{ attrs, on }">
      <FeatherButton
        primary
        v-bind="attrs"
        v-on="on"
      >
        <template v-slot:icon>
          <FeatherIcon :icon="currentIcon" aria-hidden="true" focusable="false" class="actions-dropdown-icon" />
          {{ label }}
        </template>
      </FeatherButton>
    </template>
    <FeatherDropdownItem
      v-for="action in actions"
      :key="action.action"
      @click="itemClicked(action.action)">
      <span class="actions-dropdown-menu-item">{{ action.label }}</span>
    </FeatherDropdownItem>
  </FeatherDropdown>
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherIcon } from '@featherds/icon'
import IconDownload from '@featherds/icon/action/DownloadFile'
import IconUpload from '@featherds/icon/action/UploadFile'
import { PropType, computed, markRaw } from 'vue'

const props = defineProps({
  label: {
    required: true,
    type: String
  },
  icon: {
    required: true,
    type: String as PropType<'download' | 'upload'>
  },

  actions: {
    required: true,
    type: Object as PropType<{
      label: string,
      action: string
    }[]>
  }
})

const emit = defineEmits(['action-click'])

const downloadIcon = markRaw(IconDownload)
const uploadIcon = markRaw(IconUpload)

const currentIcon = computed(() => {
  if (props.icon === 'download') {
    return downloadIcon
  }

  if (props.icon === 'upload') {
    return uploadIcon
  }

  return null
})

const itemClicked = (action: string) => {
  emit('action-click', action)
}

</script>

<style lang="scss" scoped>
.actions-dropdown-menu-item {
  padding: 1em;
}

button.btn.btn-icon .actions-dropdown-icon {
  font-size: 1.1rem;
}

.actions-dropdown-button {
  text-align: left;
}
</style>
