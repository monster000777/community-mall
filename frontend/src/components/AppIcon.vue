<template>
  <span role="img" class="app-icon" :style="style" aria-hidden="true">
    <component :is="component" v-if="component" />
    <slot v-else></slot>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  size: {
    type: [Number, String],
    default: undefined
  },
  color: {
    type: String,
    default: undefined
  },
  component: {
    type: Object,
    default: undefined
  }
})

const style = computed(() => {
  const s = {
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    lineHeight: 1
  }

  if (props.size) {
    s.fontSize = typeof props.size === 'number' ? `${props.size}px` : props.size
    s.width = s.fontSize
    s.height = s.fontSize
  }

  if (props.color) {
    s.color = props.color
  }

  return s
})
</script>

<style scoped>
.app-icon {
  vertical-align: middle;
  fill: currentColor;
  stroke: currentColor;
}

.app-icon :deep(svg) {
  width: 1em;
  height: 1em;
}
</style>
