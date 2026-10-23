<script setup>
import { toasts } from '../toast'
</script>

<template>
  <div class="toast-stack" aria-live="polite">
    <TransitionGroup name="toast">
      <div v-for="item in toasts" :key="item.id" class="toast-item" :class="`is-${item.type}`">
        <span class="toast-dot"></span>
        <span>{{ item.message }}</span>
      </div>
    </TransitionGroup>
  </div>
</template>
