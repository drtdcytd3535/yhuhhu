<template>
  <canvas ref="cv" class="bg-particles" aria-hidden="true"></canvas>
</template>

<script setup>
/**
 * 首页背景 · 灵光粒子（动态版）
 * 五层动态元素，从慢到快：
 *   1. 巨型光晕   大字号的柔光团缓慢游走、明暗呼吸（最慢，定基调）
 *   2. 灵光尘     上浮 + 摆动 + 闪烁，远快近慢形成纵深
 *   3. 灵光流     每隔几秒一道细光带斜掠而过
 *   4. 滚动视差   页面滚动时粒子层按 0.12 系数反向偏移
 *   5. 气息扰动   鼠标周边 140px 内光尘被推开再回弹
 * 性能：光点/光晕全部离屏预渲染成精灵，逐帧只做 drawImage
 */
import { onMounted, onBeforeUnmount, ref } from 'vue'

const cv = ref(null)

let ctx = null
let raf = 0
let w = 0
let h = 0
let dpr = 1
let particles = []
let glows = []
let streaks = []
let sprites = []
let glowSprites = []
let nextStreakIn = 3
let lastT = 0
let visible = true
let scrollY = 0

/* ---------------- 可调参数 ---------------- */
/* 四色灵光：青、金、月白、素白 */
const TINTS = ['111,233,255', '216,178,106', '159,196,255', '255,255,255']
/* 上浮速度（px/秒）：远的快、近的慢，制造纵深 */
const RISE = { near: [9, 20], far: [26, 58] }
/* 左右摆幅（px）与摆动周期（弧度/秒） */
const SWAY = { amp: [5, 16], speed: [0.25, 0.95] }
const CURSOR_R = 140
/* 灵光流：嫌花可改 false 一行关掉 */
const STREAKS = true
const STREAK_GAP = { min: 4, max: 9 }
/* 滚动视差系数（0 = 关闭） */
const PARALLAX = 0.12
/* 光晕层：数量、半径、明灭周期 */
const GLOW_COUNT = 3

const reduceMotion =
  typeof window !== 'undefined' &&
  window.matchMedia &&
  window.matchMedia('(prefers-reduced-motion: reduce)').matches
/* 系统开了"减少动态效果"时降速到四成，而不是完全静止 */
const motionScale = reduceMotion ? 0.4 : 1

/* 指针位置（-9999 表示不在窗口内） */
const pointer = { x: -9999, y: -9999 }

const rand = (a, b) => a + Math.random() * (b - a)

/* ---------------- 精灵预渲染 ---------------- */
function makeSprite(rgb, size, stops) {
  const c = document.createElement('canvas')
  c.width = size
  c.height = size
  const g = c.getContext('2d')
  const half = size / 2
  const grad = g.createRadialGradient(half, half, 0, half, half, half)
  for (const [pos, a] of stops) grad.addColorStop(pos, `rgba(${rgb},${a})`)
  g.fillStyle = grad
  g.fillRect(0, 0, size, size)
  return c
}

function buildSprites() {
  sprites = TINTS.map((rgb) =>
    makeSprite(rgb, 64, [
      [0, 1],
      [0.22, 0.62],
      [0.55, 0.16],
      [1, 0]
    ])
  )
  // 光晕用的柔光大图（边缘更软、留更长衰减）
  glowSprites = [TINTS[0], TINTS[1]].map((rgb) =>
    makeSprite(rgb, 256, [
      [0, 0.5],
      [0.28, 0.24],
      [0.6, 0.07],
      [1, 0]
    ])
  )
}

/* ---------------- 粒子池 ---------------- */
function makeParticle(anywhere) {
  const tint = Math.floor(Math.random() * sprites.length)
  // 三成是近处大光点，其余是远处细尘
  const near = Math.random() < 0.32
  const [rMin, rMax] = near ? RISE.near : RISE.far
  return {
    x: Math.random() * w,
    y: anywhere ? Math.random() * h : h + 14 + Math.random() * 70,
    r: near ? rand(1.8, 4.4) : rand(0.7, 1.9),
    vy: -rand(rMin, rMax) * motionScale,
    vx: (Math.random() - 0.5) * 8,
    sway: rand(SWAY.amp[0], SWAY.amp[1]),
    swaySpeed: rand(SWAY.speed[0], SWAY.speed[1]),
    phase: Math.random() * Math.PI * 2,
    base: near ? rand(0.5, 0.92) : rand(0.2, 0.55),
    // 两成是"脉动星"，明暗起伏更明显
    pulse: Math.random() < 0.2 ? rand(0.45, 0.7) : rand(0.12, 0.3),
    twinkle: rand(0.25, 1.05),
    sprite: sprites[tint],
    dx: 0,
    dy: 0
  }
}

function spawn(count) {
  particles = new Array(count)
  for (let i = 0; i < count; i++) particles[i] = makeParticle(true)
}

/* ---------------- 光晕 ---------------- */
function makeGlow(anywhere) {
  const si = Math.random() < 0.55 ? 0 : 1
  const r = rand(150, 340)
  return {
    x: Math.random() * w,
    y: anywhere ? Math.random() * h : h + r,
    r,
    vx: rand(-7, 7) * motionScale,
    vy: -rand(3, 9) * motionScale,
    base: rand(0.14, 0.26),
    phase: Math.random() * Math.PI * 2,
    breath: rand(0.12, 0.3),
    sprite: glowSprites[si]
  }
}

function syncGlows() {
  while (glows.length > GLOW_COUNT) glows.pop()
  while (glows.length < GLOW_COUNT) glows.push(makeGlow(true))
}

function drawGlows(dt, t) {
  for (const g of glows) {
    g.x += g.vx * dt
    g.y += g.vy * dt
    if (g.y + g.r < -20) Object.assign(g, makeGlow(false), { sprite: g.sprite })
    if (g.x + g.r < -40) g.x = w + g.r
    else if (g.x - g.r > w + 40) g.x = -g.r
    const alpha = g.base * (0.55 + 0.45 * Math.sin(t * g.breath + g.phase))
    ctx.globalAlpha = Math.max(0, alpha)
    ctx.drawImage(g.sprite, g.x - g.r, g.y - g.r, g.r * 2, g.r * 2)
  }
  ctx.globalAlpha = 1
}

/* ---------------- 灵光流 ---------------- */
function makeStreak() {
  const fromLeft = Math.random() < 0.6
  const angle = ((fromLeft ? 26 : 154) + (Math.random() - 0.5) * 14) * (Math.PI / 180)
  const speed = rand(260, 460)
  const si = Math.random() < 0.55 ? 0 : 1
  return {
    x: fromLeft ? -60 + Math.random() * w * 0.45 : w + 60 - Math.random() * w * 0.45,
    y: -30 + Math.random() * h * 0.42,
    vx: Math.cos(angle) * speed,
    vy: Math.sin(angle) * speed,
    len: rand(90, 230),
    life: 0,
    ttl: rand(1.6, 3),
    tint: TINTS[si],
    sprite: sprites[si],
    width: rand(0.7, 1.5),
    peak: rand(0.28, 0.48)
  }
}

function drawStreaks(dt) {
  for (let i = streaks.length - 1; i >= 0; i--) {
    const s = streaks[i]
    s.life += dt
    s.x += s.vx * dt
    s.y += s.vy * dt
    if (s.life > s.ttl || s.x < -s.len - 90 || s.x > w + s.len + 90 || s.y > h + 140) {
      streaks.splice(i, 1)
      continue
    }
    const k = s.life / s.ttl
    const alpha = s.peak * Math.sin(Math.min(1, k) * Math.PI)
    const d = Math.hypot(s.vx, s.vy) || 1
    const tailX = s.x - (s.vx / d) * s.len
    const tailY = s.y - (s.vy / d) * s.len
    const grad = ctx.createLinearGradient(s.x, s.y, tailX, tailY)
    grad.addColorStop(0, `rgba(${s.tint},${alpha})`)
    grad.addColorStop(0.45, `rgba(${s.tint},${alpha * 0.35})`)
    grad.addColorStop(1, `rgba(${s.tint},0)`)
    ctx.strokeStyle = grad
    ctx.lineWidth = s.width
    ctx.lineCap = 'round'
    ctx.beginPath()
    ctx.moveTo(s.x, s.y)
    ctx.lineTo(tailX, tailY)
    ctx.stroke()
    ctx.globalAlpha = alpha * 1.5
    ctx.drawImage(s.sprite, s.x - 8, s.y - 8, 16, 16)
    ctx.globalAlpha = 1
  }
}

/* ---------------- 尺寸 ---------------- */
function resize() {
  const el = cv.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  const nextW = Math.max(1, Math.round(rect.width || window.innerWidth))
  const nextH = Math.max(1, Math.round(rect.height || window.innerHeight))
  dpr = Math.min(window.devicePixelRatio || 1, 2)
  const first = w === 0 && h === 0
  w = nextW
  h = nextH
  el.width = Math.round(w * dpr)
  el.height = Math.round(h * dpr)
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  // 密度按面积走：1080p 约 80 颗
  const count = Math.max(40, Math.min(130, Math.round((w * h) / 24000)))
  if (first || particles.length === 0) spawn(count)
  else {
    while (particles.length > count) particles.pop()
    while (particles.length < count) particles.push(makeParticle(true))
  }
  syncGlows()
}

/* ---------------- 绘制 ---------------- */
const wrap = (v, m) => ((v % m) + m) % m

function draw(dt) {
  ctx.clearRect(0, 0, w, h)
  const t = performance.now() / 1000
  const off = scrollY * PARALLAX

  // 1. 光晕在最底层
  drawGlows(dt, t)

  // 2. 灵光流
  if (STREAKS && dt > 0) {
    nextStreakIn -= dt
    if (nextStreakIn <= 0) {
      if (streaks.length < 2) streaks.push(makeStreak())
      nextStreakIn = rand(STREAK_GAP.min, STREAK_GAP.max)
    }
    drawStreaks(dt)
  }

  // 3. 灵光尘
  const span = h + 90
  for (const p of particles) {
    p.y += p.vy * dt
    p.x += (p.vx + Math.sin(t * p.swaySpeed + p.phase) * p.sway) * dt

    // 鼠标附近轻微避让，像被气息推开
    const mx = p.x
    const my = p.y
    if (Math.abs(mx - pointer.x) < CURSOR_R && Math.abs(my - pointer.y) < CURSOR_R) {
      const ax = mx - pointer.x
      const ay = my - pointer.y
      const d2 = ax * ax + ay * ay || 1
      if (d2 < CURSOR_R * CURSOR_R) {
        const f = (1 - Math.sqrt(d2) / CURSOR_R) * 44
        p.dx += (ax / Math.sqrt(d2)) * f * dt
        p.dy += (ay / Math.sqrt(d2)) * f * dt
      }
    }
    p.dx *= 1 - Math.min(1, dt * 1.8)
    p.dy *= 1 - Math.min(1, dt * 1.8)

    // 飘出顶部后从底部重新进入
    if (p.y + p.dy < -24) {
      Object.assign(p, makeParticle(false), { sprite: p.sprite })
      continue
    }
    if (p.x + p.dx < -24) p.x = w + 20
    else if (p.x + p.dx > w + 24) p.x = -20

    // 明暗起伏（脉动星幅度更大，一眼能看出在动）
    p.phase += p.twinkle * dt
    const flicker = 1 - p.pulse * (0.5 + 0.5 * Math.sin(p.phase * 2.6))
    const alpha = Math.max(0, Math.min(1, p.base * flicker))

    const size = p.r * 6
    const px = p.x + p.dx
    const py = wrap(p.y + p.dy - off, span) - 45
    ctx.globalAlpha = alpha
    ctx.drawImage(p.sprite, px - size / 2, py - size / 2, size, size)
  }
  ctx.globalAlpha = 1
}

function loop(now) {
  if (!visible) return
  const dt = Math.min(0.05, lastT ? (now - lastT) / 1000 : 0.016)
  lastT = now
  draw(dt)
  raf = requestAnimationFrame(loop)
}

/* ---------------- 事件 ---------------- */
function onMove(e) {
  pointer.x = e.clientX
  pointer.y = e.clientY
}
function onLeave() {
  pointer.x = -9999
  pointer.y = -9999
}
function onScroll() {
  scrollY = window.scrollY || window.pageYOffset || 0
}

let resizeTimer = 0
function onResize() {
  clearTimeout(resizeTimer)
  resizeTimer = window.setTimeout(resize, 180)
}

function onVisibility() {
  visible = !document.hidden
  if (visible) {
    lastT = 0
    cancelAnimationFrame(raf)
    raf = requestAnimationFrame(loop)
  } else {
    cancelAnimationFrame(raf)
  }
}

/* ---------------- 生命周期 ---------------- */
onMounted(() => {
  const el = cv.value
  if (!el) return
  ctx = el.getContext('2d')
  if (!ctx) return
  buildSprites()
  scrollY = window.scrollY || 0
  resize()

  raf = requestAnimationFrame(loop)
  window.addEventListener('resize', onResize)
  window.addEventListener('scroll', onScroll, { passive: true })
  window.addEventListener('mousemove', onMove, { passive: true })
  window.addEventListener('mouseout', onLeave, { passive: true })
  document.addEventListener('visibilitychange', onVisibility)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(raf)
  clearTimeout(resizeTimer)
  window.removeEventListener('resize', onResize)
  window.removeEventListener('scroll', onScroll)
  window.removeEventListener('mousemove', onMove)
  window.removeEventListener('mouseout', onLeave)
  document.removeEventListener('visibilitychange', onVisibility)
})
</script>

<style scoped>
.bg-particles {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  display: block;
  pointer-events: none;
  opacity: 0;
  animation: particleFadeIn 2s ease 0.2s forwards;
  /* 提亮合成，避免在暗纱上发灰 */
  mix-blend-mode: screen;
}

@keyframes particleFadeIn {
  to {
    opacity: 1;
  }
}
</style>
