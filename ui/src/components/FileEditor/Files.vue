<template>
  <FeatherList v-for="filename of fileNames" :key="filename">
    <FeatherListItem @click="getFile(filename)">{{ filename }}</FeatherListItem>
  </FeatherList>
</template>

<script setup lang="ts">
import { computed } from '@vue/reactivity'
import { useStore } from 'vuex'
import {
  FeatherListItem,
  FeatherList,
} from "@featherds/list"

const store = useStore()
const fileNames = computed(() => store.state.fileEditorModule.fileNames)
const getFile = (filename: string) => store.dispatch('fileEditorModule/getFile', filename)
</script>
