<template>
  <li
    class="pointer"
    :id="item.fullPath === selectedFile ? 'selected' : ''"
    :class="[{ hidden: item.isHidden }, { selected: !isFolder && !isEditing && item.fullPath === selectedFile }]"
    @click="!isFolder && item.fullPath ? getFile(item.fullPath) : ''"
  >
    <div :class="{ subtitle1: isFolder, subtitle2: !isFolder }" @click="toggle">
      <span v-if="isFolder">
        <FeatherIcon :icon="isOpen ? Open : Close" />
      </span>

      <span v-if="!isEditing">{{ item.name }}</span>

      <span v-if="isFolder" class="add" @click.stop="addNewFile(item)">&nbsp; +</span>

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

<script setup lang="ts">
import { FeatherIcon } from '@featherds/icon'
import Open from '@featherds/icon/navigation/ExpandMore'
import Close from '@featherds/icon/navigation/ChevronRight'
import Remove from '@featherds/icon/action/Remove'
import NewFileInput from './NewFileInput.vue'
import { useFileEditorStore, IFile } from '@/stores/fileEditorStore'
import { PropType } from 'vue'

const props = defineProps({
  item: {
    required: true,
    type: Object as PropType<IFile>
  }
})

const fileEditorStore = useFileEditorStore()

// open first folder by default
const firstFolder = props.item.name === undefined || props.item.name === 'etc'
const isOpen = ref(firstFolder)
const searchValue = computed(() => fileEditorStore.searchValue)
const isFolder = computed(() => props.item.children && props.item.children.length)
const isEditing = computed(() => props.item.isEditing)
const selectedFile = computed(() => fileEditorStore.selectedFileName)

watch(searchValue, (searchValue) => {
  // open all folders if searching
  if (searchValue) {
    isOpen.value = true
  } else {
    // else only etc folder and folder with file
    const selectedFilePath = selectedFile.value
    isOpen.value = firstFolder || selectedFilePath.toLowerCase().includes(props.item.name.toLowerCase())
  }
})

const getFile = (filename: string) => fileEditorStore.getFile(filename)

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

const openConfirmDeleteModal = (file: IFile) => fileEditorStore.setFileToDelete(file)
</script>

<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";
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
  background: var($shade-3);
  span {
    color: var($primary);
  }
}
.hidden {
  display: none;
}
.remove {
  float: right;
  margin-right: 10px;
  color: var($primary-text-on-surface) !important;
}
</style>
