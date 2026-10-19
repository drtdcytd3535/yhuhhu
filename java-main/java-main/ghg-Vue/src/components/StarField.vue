<template>
  <canvas ref="cv" class="stars"></canvas>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const cv = ref(null)
let ctx, W, H, pts, rafId

function resize() {
  if (!cv.value) return
  W = cv.value.width = window.innerWidth
  H = cv.value.height = window.innerHeight
  init()
}

function init() {
  const n = Math.min(140, Math.floor((W * H) / 12000))
  pts = Array.from({ length: n }, () => ({
    x: Math.random() * W,
    y: Math.random() * H,
    r: Math.random() * 1.6 + 0.4,
    s: Math.random() * 0.4 + 0.1,
    a: Math.random() * 0.5 + 0.3,
    c: Math.random() > 0.5 ? '255,233,168' : '111,233,255'
  }))
}

function tick() {
  ctx.clearRect(0, 0, W, H)
  for (const p of pts) {
    p.y -= p.s
    if (p.y < -5) {
      p.y = H + 5
      p.x = Math.random() * W
    }
    ctx.beginPath()
    ctx.arc(p.x, p.y, p.r, 0, 7)
    ctx.fillStyle = `rgba(${p.c},${p.a})`
    ctx.shadowBlur = 8
    ctx.shadowColor = `rgba(${p.c},.8)`
    ctx.fill()
  }
  rafId = requestAnimationFrame(tick)
}

onMounted(() => {
  ctx = cv.value.getContext('2d')
  resize()
  tick()
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(rafId)
  window.removeEventListener('resize', resize)
})
</script>

<style scoped>
.stars {
  position: fixed;
  inset: 0;
  z-index: 0;
}
</style>
