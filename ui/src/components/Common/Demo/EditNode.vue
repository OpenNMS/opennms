<template>
    <button>
        <router-link to="/dataTableDemo" >Back to Node Table</router-link>
    </button>
    <p>
        Edit Node ID ::
        <strong>{{ $route.params.id }}</strong>
    </p>
</template>

<script setup lang="ts">
</script>