<template>
  <div class="lazy-image-wrapper">
    <img
      ref="imgRef"
      :data-src="src"
      :alt="alt"
      :class="['lazy-image', { loaded: isLoaded }]"
      @load="handleLoad"
      @error="handleError"
    />
    <div v-if="!isLoaded && !hasError" class="skeleton"></div>
    <div v-if="hasError" class="error-placeholder">
      <span>图片加载失败</span>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  src: {
    type: String,
    required: true
  },
  alt: {
    type: String,
    default: ''
  }
})

const imgRef = ref(null)
const isLoaded = ref(false)
const hasError = ref(false)
let observer = null

const handleLoad = () => {
  isLoaded.value = true
}

const handleError = () => {
  hasError.value = true
}

onMounted(() => {
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          const img = entry.target
          img.src = img.dataset.src
          observer.unobserve(img)
        }
      })
    },
    {
      rootMargin: '50px' // 提前50px开始加载
    }
  )

  if (imgRef.value) {
    observer.observe(imgRef.value)
  }
})

onBeforeUnmount(() => {
  if (observer) {
    observer.disconnect()
  }
})
</script>

<style scoped>
.lazy-image-wrapper {
  position: relative;
  overflow: hidden;
  width: 100%;
  height: 100%;
  background-color: var(--bg-body);
}

.lazy-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.lazy-image.loaded {
  opacity: 1;
}

.skeleton {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
}

.error-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  color: #999;
  font-size: 14px;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 暗黑模式适配 */
[data-theme='dark'] .skeleton {
  background: linear-gradient(90deg, #2a2a2a 25%, #3a3a3a 50%, #2a2a2a 75%);
  background-size: 200% 100%;
}

[data-theme='dark'] .error-placeholder {
  background-color: #2a2a2a;
  color: #666;
}
</style>
