<template>
  <!-- 全屏主视觉背景：登录 / 注册页共用，随页面比例自适应 -->
  <div ref="root" class="art-bg" aria-hidden="true">
    <img class="art-img" src="../assets/hhh.jpg" alt="" draggable="false" />

    <!-- 血色灵光：缓慢呼吸 -->
    <div class="art-glow"></div>

    <!-- 斜向流光扫过 -->
    <div class="art-sheen"></div>

    <!-- 灵烬上浮 -->
    <div class="embers">
      <span
        v-for="e in embers"
        :key="e.id"
        :style="{
          left: e.left + '%',
          width: e.size + 'px',
          height: e.size + 'px',
          background: e.color,
          animationDuration: e.dur + 's',
          animationDelay: e.delay + 's',
          '--drift': e.drift + 'px'
        }"
      ></span>
    </div>

    <!-- 暗色纱罩：压暗边缘，保证表单文字可读 -->
    <div class="art-veil"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const root = ref(null)

/* 灵烬粒子：位置、大小、时长、漂移随机，向上飘散 */
const embers = Array.from({ length: 28 }, (_, i) => ({
  id: i,
  left: Math.random() * 100,
  size: +(Math.random() * 2.6 + 1.4).toFixed(1),
  dur: +(Math.random() * 9 + 9).toFixed(1),
  delay: -+(Math.random() * 18).toFixed(1),
  drift: Math.round(Math.random() * 70 - 35),
  color: Math.random() > 0.55 ? 'rgba(232, 198, 107, 0.9)' : 'rgba(255, 92, 112, 0.85)'
}))

/* 鼠标视差：画面随光标轻微位移，制造纵深；位移做平滑插值 */
let raf = null
let tx = 0
let ty = 0
let cx = 0
let cy = 0
const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches

function onMove(e) {
  const w = window.innerWidth || 1
  const h = window.innerHeight || 1
  tx = (e.clientX / w - 0.5) * -28
  ty = (e.clientY / h - 0.5) * -20
  if (!raf) raf = requestAnimationFrame(tick)
}

function tick() {
  cx += (tx - cx) * 0.07
  cy += (ty - cy) * 0.07
  if (root.value) {
    root.value.style.setProperty('--px', cx.toFixed(2) + 'px')
    root.value.style.setProperty('--py', cy.toFixed(2) + 'px')
  }
  if (Math.abs(tx - cx) > 0.1 || Math.abs(ty - cy) > 0.1) {
    raf = requestAnimationFrame(tick)
  } else {
    raf = null
  }
}

onMounted(() => {
  if (reduced) return
  window.addEventListener('mousemove', onMove, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('mousemove', onMove)
  if (raf) cancelAnimationFrame(raf)
})
</script>

<style scoped>
.art-bg {
  position: fixed;
  inset: 0;
  z-index: 2; /* 远山/灵雾之上，表单卡片(.wrap z-index:3)之下 */
  overflow: hidden;
  pointer-events: none;
  transform: translate3d(var(--px, 0), var(--py, 0), 0);
  will-change: transform;
  animation: artIn 1.4s ease both;
}

/* 底图：等比铺满 + 缓慢推近横移（Ken Burns） */
.art-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center 32%;
  display: block;
  user-select: none;
  transform-origin: 50% 45%;
  animation: kenburns 30s ease-in-out infinite alternate;
}

/* 血色灵光呼吸 */
.art-glow {
  position: absolute;
  inset: -10%;
  background:
    radial-gradient(38% 34% at 26% 62%, rgba(255, 40, 68, 0.32), transparent 70%),
    radial-gradient(32% 30% at 74% 68%, rgba(190, 20, 60, 0.26), transparent 72%);
  mix-blend-mode: screen;
  animation: pulse 9s ease-in-out infinite;
  filter: blur(2px);
}

/* 斜向流光 */
.art-sheen {
  position: absolute;
  top: -60%;
  left: -40%;
  width: 55%;
  height: 220%;
  background: linear-gradient(
    100deg,
    transparent,
    rgba(255, 255, 255, 0.055) 45%,
    rgba(111, 233, 255, 0.07) 55%,
    transparent
  );
  transform: rotate(14deg);
  animation: sweep 13s linear infinite;
}

/* 灵烬 */
.embers {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.embers span {
  position: absolute;
  bottom: -6vh;
  border-radius: 50%;
  opacity: 0;
  box-shadow: 0 0 8px currentColor;
  animation-name: rise;
  animation-timing-function: linear;
  animation-iteration-count: infinite;
}

/* 暗色纱罩 */
.art-veil {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(95% 75% at 50% 42%, rgba(8, 9, 22, 0.08), rgba(8, 9, 22, 0.46)),
    linear-gradient(to top, rgba(8, 9, 22, 0.68), transparent 58%);
}

@keyframes artIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes kenburns {
  from {
    transform: scale(1.07) translate3d(-1.2%, -0.8%, 0);
  }
  to {
    transform: scale(1.17) translate3d(1.6%, 1.2%, 0);
  }
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.45;
    transform: scale(1);
  }
  50% {
    opacity: 0.9;
    transform: scale(1.08);
  }
}

@keyframes sweep {
  0% {
    transform: translateX(-30%) rotate(14deg);
  }
  100% {
    transform: translateX(320%) rotate(14deg);
  }
}

@keyframes rise {
  0% {
    opacity: 0;
    transform: translate3d(0, 0, 0) scale(0.7);
  }
  12% {
    opacity: 0.85;
  }
  70% {
    opacity: 0.5;
  }
  100% {
    opacity: 0;
    transform: translate3d(var(--drift, 0), -108vh, 0) scale(1.15);
  }
}

/* 尊重系统「减弱动态效果」设置 */
@media (prefers-reduced-motion: reduce) {
  .art-bg,
  .art-img,
  .art-glow,
  .art-sheen,
  .embers span {
    animation: none !important;
  }
  .art-bg {
    transform: none !important;
  }
}
</style>
