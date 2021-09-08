<template>
    <TabMenu :model="TabMenuItem" :activeIndex="activeIndex"/>
    <router-view></router-view>
</template>
<script setup lang="ts">

import TabMenu from 'primevue/tabmenu';
import { computed, ref } from 'vue';

const activeIndex = ref(0);

const TabMenuItem = ref([
    { label: 'Requisition Definitions', to: '/provisionConfig/reqDefinition' },
    { label: 'Thread Pools', to: '/provisionConfig/threadPools' },
]);
</script>