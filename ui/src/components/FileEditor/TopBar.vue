<template>
  <div class="top-bar">
    <div class="icon subtitle2 pointer" @click="triggerHelp">
      Files<FeatherIcon :icon="ChevronRight" />
    </div>
    <div class="save">
      <FeatherButton :disabled="disableBtn" primary @click="save">Save</FeatherButton>
    </div>
    <div class="reset">
      <FeatherButton :disabled="disableBtn" primary @click="reset">Reset</FeatherButton>
    </div>
    <div class="filename headline3">{{ filename }}</div>
  </div>
  <hr />
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import ChevronRight from '@featherds/icon/navigation/ChevronRight'
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
