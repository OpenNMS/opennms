<template>
  <li
    class="pointer"
    :class="[{ hidden: item.isHidden }, { selected: !isFolder && !isEditing && item.fullPath === selectedFile }]"
    @click="!isFolder && item.fullPath ? getFile(item.fullPath) : ''"
  >
    <div :class="{ subtitle1: isFolder, subtitle2: !isFolder }" @click="toggle">
      <span v-if="isFolder">
        <FeatherIcon :icon="isOpen ? Open : Close" />
      </span>

      <span v-if="!isEditing">{{ item.name }}</span>

      <span v-if="isFolder" class="add" @click.stop="addNewFile(item)">&nbsp +</span>

      <span class="remove" v-if="item.fullPath === selectedFile">
        <FeatherIcon :icon="Remove" @click.stop="openConfirmDeleteModal(item)" />
      </span>

      <NewFileInput v-if="isEditing" :item="item" />
    </div>

    <!-- Folder -->
    <ul v-show="isOpen && isFolder">
      <FileTreeItem
        class="pointer"
        v-for="(child, index) in item.children"
        :key="index"
        :item="child"
      ></FileTreeItem>
    </ul>
  </li>
</template>

<script setup lang=ts>
import { ref, computed, PropType, watch } from 'vue'
import { useStore } from 'vuex'
import { FeatherIcon } from '@featherds/icon'
import Open from "@featherds/icon/navigation/ExpandMore"
import Close from "@featherds/icon/navigation/ChevronRight"
import Remove from "@featherds/icon/action/Remove"
import NewFileInput from './NewFileInput.vue'
import { IFile } from "@/store/fileEditor/state"

const store = useStore()
const props = defineProps({
  item: {
    required: true,
    type: Object as PropType<IFile>
  }
})

// open first folder by default
const firstFolder = props.item.name === undefined || props.item.name === 'etc'
const isOpen = ref(firstFolder)
const searchValue = computed(() => store.state.fileEditorModule.searchValue)
const isFolder = computed(() => props.item.children && props.item.children.length)
const isEditing = computed(() => props.item.isEditing)
const selectedFile = computed(() => store.state.fileEditorModule.selectedFileName)

watch(searchValue, (searchValue) => {
  // open all folders if searching
  if (searchValue) isOpen.value = true
  // else only files folder
  else isOpen.value = firstFolder
})

const getFile = (filename: string) => store.dispatch('fileEditorModule/getFile', filename)

const toggle = () => {
  if (isFolder.value) {
    isOpen.value = !isOpen.value
  }
}

const addNewFile = (file: IFile) => {
  if (!isOpen.value) toggle()
  file.children?.unshift({
    name: '',
    isEditing: true,
    fullPath: file.fullPath
  })
}

const openConfirmDeleteModal = (file: IFile) => store.dispatch('fileEditorModule/setFileToDelete', file)
</script>

<style lang="scss" scoped>
ul,
li {
  list-style-type: none;
}
ul {
  padding-left: 0px;
}
li {
  padding: 2px;
  padding-left: 14px;
  .subtitle2 {
    padding-left: 10px;
  }
}
.add {
  margin-left: 0px;
}
.selected {
  background: var(--feather-shade-3);
  span {
    color: var(--feather-primary);
  }
}
.hidden {
  display: none;
}
.remove {
  float: right;
  margin-right: 10px;
  color: var(--feather-primary-text-on-surface) !important;
}
</style>
