<template>
  <div class="top-bar">
    <div class="icon subtitle2 pointer" @click="triggerHelp">
      Files<OnmsIcon :icon="ChevronRight" />
    </div>
    <div class="save">
      <OnmsButton :disabled="disableBtn" @click="save">Save</OnmsButton>
    </div>
    <div class="reset">
      <OnmsButton :disabled="disableBtn" @click="reset">Reset</OnmsButton>
    </div>
    <div class="filename headline3">{{ filename }}</div>
  </div>
  <hr />
</template>

<script setup lang="ts">
import { computed } from 'vue'

import { OnmsButton, OnmsIcon } from '@opennms/onms-ui'
import ChevronRight from '@opennms/onms-ui/icons/navigation/ChevronRight.vue'
import { useFileEditorStore } from '@/stores/fileEditorStore'

const fileEditorStore = useFileEditorStore()

const filename = computed(() => fileEditorStore.selectedFileName)
const contentModified = computed(() => fileEditorStore.contentModified)
const hasSelectedFile = computed(() => fileEditorStore.selectedFileName !== '')
const disableBtn = computed(() => !contentModified.value || !hasSelectedFile.value)

const reset = () => fileEditorStore.triggerFileReset()
const save = () => fileEditorStore.saveModifiedFile()
const triggerHelp = () => fileEditorStore.setIsHelpOpen(false)
</script>

<style scoped lang="scss">
.top-bar {
  display: flex;
  .icon {
    line-height: 3.6;
    margin-bottom: -5px;
    margin-right: 10px;
  }
  .filename {
    margin-left: 23px;
    line-height: 2.5;
    margin-bottom: -8px;
  }
  .save,
  .reset {
    margin-left: 10px;
    button {
      margin-top: 5px;
      margin-bottom: 0px;
    }
  }
}
</style>
