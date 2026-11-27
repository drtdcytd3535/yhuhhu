<template>
  <!-- 进入首页的入场特效：GIF 播完即散，整体时长与 GIF 完全一致 -->
  <transition name="entryfx" @after-leave="emit('done')">
    <div
      v-if="shown"
      class="entry-fx"
      :style="{ '--fx-duration': duration + 'ms' }"
      aria-hidden="true"
    >
      <img class="fx-img" :src="src" alt="" />
      <div class="fx-glow"></div>
      <div class="fx-shade"></div>

      <div class="fx-caption">
        <span class="fx-seal">仙逆</span>
        <p class="fx-title">仙 门 已 开</p>
        <p class="fx-sub">逆天改命 · 问道长生</p>
      </div>

      <div class="fx-bar"><i></i></div>
    </div>
  </transition>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({
  /** 入场 GIF 的地址 */
  src: { type: String, required: true },
  /** 特效时长：GIF 一轮 16 帧 × 100ms = 1600ms，这里播两轮共 3200ms（可按需调整） */
  duration: { type: Number, default: 3200 }
})
const emit = defineEmits(['done'])

/** 末尾淡出时间，与下方 .entryfx-leave-active 保持一致 */
const FADE = 320
const shown = ref(false)
let hideTimer = null

onMounted(() => {
  shown.value = true
  // 提前 FADE 毫秒开始淡出：GIF 播完的那一刻，特效也正好散尽
  hideTimer = setTimeout(() => {
    shown.value = false
  }, Math.max(0, props.duration - FADE))
})

onBeforeUnmount(() => clearTimeout(hideTimer))
</script>

<style scoped>
.entry-fx {
  position: fixed;
  inset: 0;
  z-index: 1000;
  overflow: hidden;
  background: #04050b;
  pointer-events: none;
}

/* 主画面：铺满视口，轻微推近，收尾时回到原尺寸 */
.fx-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: saturate(1.12) contrast(1.06);
  animation: fxZoom var(--fx-duration) cubic-bezier(0.22, 0.7, 0.25, 1) both;
}

/* 青金灵光：呼应画面里的雷光 */
.fx-glow {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(60% 45% at 50% 55%, rgba(111, 233, 255, 0.16), transparent 70%),
    radial-gradient(80% 60% at 50% 110%, rgba(232, 198, 107, 0.18), transparent 70%);
  mix-blend-mode: screen;
  animation: fxGlow var(--fx-duration) ease-out both;
}

/* 暗角 + 底部压暗，保证题字可读 */
.fx-shade {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(4, 5, 11, 0.35) 0%, transparent 28%, transparent 58%, rgba(4, 5, 11, 0.88) 100%),
    radial-gradient(120% 90% at 50% 50%, transparent 45%, rgba(4, 5, 11, 0.65) 100%);
}

.fx-caption {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 11%;
  text-align: center;
  animation: fxText calc(var(--fx-duration) * 0.42) ease-out both;
}

.fx-seal {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  border: 1px solid rgba(232, 198, 107, 0.55);
  background: rgba(232, 198, 107, 0.1);
  box-shadow: 0 0 22px rgba(232, 198, 107, 0.28);
  color: var(--gold, #e8c66b);
  font-family: var(--font-display, serif);
  font-size: 22px;
  writing-mode: vertical-rl;
  letter-spacing: 2px;
  margin-bottom: 16px;
}

.fx-title {
  font-family: var(--font-display, serif);
  font-size: clamp(30px, 5vw, 56px);
  font-weight: 400;
  letter-spacing: 0.32em;
  color: #fff;
  text-shadow:
    0 0 24px rgba(111, 233, 255, 0.55),
    0 0 56px rgba(232, 198, 107, 0.42);
}

.fx-sub {
  margin-top: 12px;
  font-size: clamp(12px, 1.5vw, 16px);
  letter-spacing: 0.6em;
  color: rgba(232, 198, 107, 0.88);
}

/* 进度光条：从左推到右，正好一帧不差地走完 GIF 时长 */
.fx-bar {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 2px;
  background: rgba(111, 233, 255, 0.14);
}

.fx-bar i {
  display: block;
  height: 100%;
  width: 100%;
  transform-origin: left center;
  background: linear-gradient(90deg, var(--gold, #e8c66b), var(--cyan, #6fe9ff));
  box-shadow: 0 0 12px rgba(111, 233, 255, 0.7);
  animation: fxBar var(--fx-duration) linear both;
}

@keyframes fxZoom {
  from {
    transform: scale(1.09);
  }
  to {
    transform: scale(1);
  }
}

@keyframes fxGlow {
  from {
    opacity: 0.2;
  }
  55% {
    opacity: 1;
  }
  to {
    opacity: 0.65;
  }
}

@keyframes fxText {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fxBar {
  from {
    transform: scaleX(0);
  }
  to {
    transform: scaleX(1);
  }
}

/* 与脚本里的 FADE 一致 */
.entryfx-leave-active {
  transition: opacity 0.32s ease;
}

.entryfx-leave-to {
  opacity: 0;
}
</style>
