<template>
    <TabMenu :model="TabMenuItem" />
    <router-view></router-view>
</template>
<script setup lang="ts">

import TabMenu from 'primevue/tabmenu';

const TabMenuItem = [
    { label: 'Requisition Definitions', to: '/provisionConfig' },
    { label: 'Thread Pools', to: '/threadPools' },
];
</script>