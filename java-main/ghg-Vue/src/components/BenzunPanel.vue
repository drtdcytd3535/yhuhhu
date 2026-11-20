<template>
  <section class="bz">
    <!-- ================= 头部：返回 + 标题 ================= -->
    <div class="bz-head">
      <button class="bz-back" @click="emit('back')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
          <path d="M15 5 8 12l7 7" />
        </svg>
        返回仙门
      </button>
      <div class="bz-title">
        <h2>本尊 · 古神苦修</h2>
        <p>分身历凡尘 · 本尊炼真身</p>
      </div>
    </div>

    <!-- ================= 页面简介（顶部） ================= -->
    <div class="bz-panel bz-intro">
      <p class="intro-text">
        本尊隐于虚空，苦修古神肉身，逆修大道，沉淀修为，静待突破之机。
      </p>
    </div>

    <!-- ================= 主体：左状态操作 + 右侧常驻短句 ================= -->
    <div class="bz-body">
      <div class="bz-main">
        <!-- ---------- 本尊状态 ---------- -->
        <div class="bz-panel bz-status">
          <div class="st-row">
            <div class="st-left">
              <p class="st-label">本尊境界</p>
              <p class="st-realm">{{ realmName }}</p>
              <p class="st-level">{{ atTop ? '古神之巅 · 不可再进' : `第 ${b.state.level + 1} 重 · 共 ${GOD_REALMS.length} 重` }}</p>
            </div>
            <div class="st-right">
              <p class="st-exp">◆ {{ b.state.exp }}</p>
              <p class="st-exp-label">本尊修为</p>
            </div>
          </div>
          <div class="st-mid">
            <div class="st-bar">
              <i :style="{ width: pct + '%' }" :class="{ glow: canBreak }"></i>
            </div>
            <p class="st-tip">
              {{
                atTop
                  ? '肉身已臻至极 · 苦修不止'
                  : canBreak
                    ? `修为已足 · 可尝试冲击「${GOD_REALMS[b.state.level + 1].name}」`
                    : `距冲击「${GOD_REALMS[b.state.level + 1].name}」尚需 ${GOD_REALMS[b.state.level + 1].need - b.state.exp} 修为`
              }}
            </p>
          </div>
        </div>

        <!-- ---------- 苦修三课 ---------- -->
        <div class="bz-panel bz-acts">
          <div class="panel-head">
            <h3>苦修三课</h3>
            <span class="panel-count">吐纳养身 · 感悟养道 · 突破搏命</span>
          </div>
          <div class="act-grid">
            <button class="act-btn" :disabled="cooling" @click="onBreath">
              <span class="act-ico">▚</span>
              <span class="act-name">▫️静坐吐纳</span>
              <span class="act-desc">引虚空灵气淬炼肉身</span>
            </button>
            <button class="act-btn" :disabled="cooling" @click="onDao">
              <span class="act-ico">☯</span>
              <span class="act-name">▫️感悟道则</span>
              <span class="act-desc">参悟天地法理逆修大道</span>
            </button>
            <button
              class="act-btn break"
              :class="{ ready: canBreak }"
              :disabled="cooling"
              @click="onBreak"
            >
              <span class="act-ico">⚡</span>
              <span class="act-name">▫️尝试突破</span>
              <span class="act-desc">{{ canBreak ? '修为已足 · 一搏古神境' : '修为未足 · 不可强闯' }}</span>
            </button>
          </div>
        </div>

        <!-- ---------- 星域古图（点星球收取灵蕴） ---------- -->
        <div class="bz-panel bz-map-panel">
          <div class="panel-head">
            <h3>星域古图</h3>
            <span class="panel-count">点星球收取灵蕴 · 一星之蕴有限 · 枯竭不可再修</span>
          </div>
          <div class="map-wrap">
            <img class="map-bg" :src="nebulaBg" alt="" aria-hidden="true" />
            <div class="map-veil" aria-hidden="true"></div>
            <svg class="starmap" viewBox="0 0 800 400" role="img" aria-label="仙逆星域图">
              <!-- 星轨 -->
              <ellipse cx="400" cy="200" rx="150" ry="80" fill="none" stroke="rgba(111, 233, 255, 0.16)" stroke-dasharray="3 6" />
              <ellipse cx="400" cy="200" rx="260" ry="130" fill="none" stroke="rgba(111, 233, 255, 0.13)" stroke-dasharray="3 6" />
              <ellipse cx="400" cy="200" rx="360" ry="175" fill="none" stroke="rgba(111, 233, 255, 0.1)" stroke-dasharray="3 6" />

              <!-- 远处散星 -->
              <circle cx="90" cy="60" r="1.4" fill="rgba(233, 238, 251, 0.55)" />
              <circle cx="160" cy="330" r="1.2" fill="rgba(233, 238, 251, 0.45)" />
              <circle cx="330" cy="40" r="1.5" fill="rgba(233, 238, 251, 0.5)" />
              <circle cx="470" cy="60" r="1.1" fill="rgba(233, 238, 251, 0.45)" />
              <circle cx="620" cy="50" r="1.4" fill="rgba(233, 238, 251, 0.55)" />
              <circle cx="730" cy="120" r="1.2" fill="rgba(233, 238, 251, 0.45)" />
              <circle cx="760" cy="250" r="1.4" fill="rgba(233, 238, 251, 0.5)" />
              <circle cx="120" cy="220" r="1.1" fill="rgba(233, 238, 251, 0.4)" />
              <circle cx="60" cy="300" r="1.3" fill="rgba(233, 238, 251, 0.45)" />
              <circle cx="710" cy="360" r="1.2" fill="rgba(233, 238, 251, 0.45)" />

              <!-- 星球：贴图 + 光晕，可点收取灵蕴 -->
              <g
                v-for="p in PLANETS"
                :key="p.id"
                class="planet"
                :class="{ dead: isDrained(p) }"
                :title="p.name + ' · ' + TIER_NAME[p.tier]"
                @click="onPlanet(p)"
              >
                <defs>
                  <clipPath :id="'pc-' + p.id">
                    <circle :cx="p.x" :cy="p.y" :r="p.r * 1.52" />
                  </clipPath>
                </defs>
                <!-- 星球本色光晕（两层） -->
                <circle :cx="p.x" :cy="p.y" :r="p.r * 2.4" :fill="p.hue" opacity="0.1" />
                <circle :cx="p.x" :cy="p.y" :r="p.r * 1.85" :fill="p.hue" opacity="0.16" />
                <!-- 星球贴图：黑底融入星空，圆形裁切去除图外水印区 -->
                <image
                  class="p-img"
                  :href="p.img"
                  :x="p.x - p.r * 1.7"
                  :y="p.y - p.r * 1.7"
                  :width="p.r * 3.4"
                  :height="p.r * 3.4"
                  :clip-path="`url(#pc-${p.id})`"
                  preserveAspectRatio="xMidYMid slice"
                />
                <text class="p-name" :x="p.x" :y="p.y + p.r + 18">{{ p.name }}</text>
                <text class="p-left" :x="p.x" :y="p.y + p.r + 31">
                  {{ isDrained(p) ? '灵蕴枯竭' : `灵蕴 ${b.state.planets[p.id].left}` }}
                </text>
              </g>
            </svg>
          </div>
          <div class="map-legend">
            <span class="lg"><i style="background: #6fe9ff"></i>下界灵星</span>
            <span class="lg"><i style="background: #ffd76a"></i>中界灵星</span>
            <span class="lg"><i style="background: #ff9a4d"></i>上界灵星</span>
            <span class="lg"><i style="background: #ffe27a"></i>古神之地</span>
            <span class="lg-tip">每星灵蕴之量，随缘而定；一旦枯竭，万载不复</span>
          </div>
        </div>
      </div>

      <!-- ---------- 侧边常驻短句 ---------- -->
      <aside class="bz-side">
        <div class="bz-panel side-quotes">
          <div class="panel-head">
            <h3>本尊箴言</h3>
            <span class="panel-count">常驻</span>
          </div>
          <ul class="quotes">
            <li v-for="(q, i) in QUOTES" :key="i" :class="{ on: quoteIdx === i }">
              <span class="q-no">{{ i + 1 }}</span>
              <p>{{ q }}</p>
            </li>
          </ul>
        </div>
      </aside>
    </div>

    <!-- ================= 弹窗提示 ================= -->
    <transition name="fade">
      <div v-if="modal.show" class="bz-mask" @click.self="closeModal">
        <div class="bz-modal">
          <p class="modal-title">【{{ modal.title }}】</p>
          <p class="modal-text">{{ modal.text }}</p>
          <button class="modal-btn" @click="closeModal">领 悟</button>
        </div>
      </div>
    </transition>
  </section>
</template>

<script setup>
import { computed, reactive, ref, onMounted, onBeforeUnmount } from 'vue'
import imgZhuque from '../assets/planets/zhuque.png'
import imgTianyun from '../assets/planets/tianyun.png'
import imgLuotian from '../assets/planets/luotian.png'
import imgQinggang from '../assets/planets/qinggang.png'
import imgXiaoyue from '../assets/planets/xiaoyue.png'
import imgYouming from '../assets/planets/youming.png'
import imgLihuo from '../assets/planets/lihuo.png'
import imgYunshen from '../assets/planets/yunshen.png'
import imgGushen from '../assets/planets/gushen.png'
import nebulaBg from '../assets/planets/nebula-bg.png'

const props = defineProps({
  username: { type: String, default: '道友' }
})

const emit = defineEmits(['back', 'toast'])

/* =============================================================
 * 本尊・古神苦修
 * 与洞府修为各自独立：本尊修为只在这里增长，
 * 按道号存于 localStorage，换号互不串档
 * ============================================================= */

/* ---------------- 古神九重境界 ---------------- */
/** 每重所需本尊修为（突破时一次性消耗） */
const GOD_REALMS = [
  { name: '古神初醒', need: 100 },
  { name: '肉身铸骨', need: 200 },
  { name: '血髓淬炼', need: 350 },
  { name: '神纹初现', need: 550 },
  { name: '骨纹大成', need: 800 },
  { name: '血脉返祖', need: 1100 },
  { name: '古神之躯', need: 1500 },
  { name: '万劫不磨', need: 2000 },
  { name: '不朽真身', need: 2600 }
]

/* ---------------- 侧边常驻短句 ---------------- */
const QUOTES = [
  '本尊炼肉身，分身历凡尘',
  '顺为凡，逆则仙，只在心中一念间',
  '古神之路，磨难不绝',
  '吾之古神道，不从天道',
  '万法皆逆，肉身称雄',
  '肉身不灭，大道不绝',
  '不求天怜，只修己身'
]

/* =============================================================
 * 星域古图：仙逆诸星，点星球收取灵蕴化为本尊修为
 * 每颗星球的灵蕴总量随机生成，收取完毕即「枯竭」，永久不可再修
 * 星球灵蕴余量随道号一并存入 localStorage
 * ============================================================= */

/** 星球目录：tier 决定灵蕴区间；x/y/r 为星图坐标与星体大小；img 为星球贴图 */
const PLANETS = [
  { id: 'zhuque', name: '朱雀星', tier: 1, x: 170, y: 140, r: 10, hue: '#ff7a59', img: imgZhuque },
  { id: 'tianyun', name: '天运星', tier: 1, x: 300, y: 90, r: 8, hue: '#6fe9ff', img: imgTianyun },
  { id: 'luotian', name: '罗天星', tier: 2, x: 430, y: 150, r: 12, hue: '#ffd76a', img: imgLuotian },
  { id: 'qinggang', name: '青罡星', tier: 2, x: 560, y: 100, r: 9, hue: '#7dff9e', img: imgQinggang },
  { id: 'xiaoyue', name: '啸月星', tier: 2, x: 650, y: 180, r: 8, hue: '#b8a6ff', img: imgXiaoyue },
  { id: 'youming', name: '幽冥星', tier: 3, x: 240, y: 262, r: 11, hue: '#9b8cff', img: imgYouming },
  { id: 'lihuo', name: '离火星', tier: 3, x: 390, y: 300, r: 10, hue: '#ff9a4d', img: imgLihuo },
  { id: 'yunshen', name: '陨神星', tier: 3, x: 540, y: 268, r: 12, hue: '#ff6a8a', img: imgYunshen },
  { id: 'gushen', name: '古神星', tier: 4, x: 680, y: 318, r: 15, hue: '#ffe27a', img: imgGushen }
]

/** 各品阶星球灵蕴总量区间（随机取值） */
const TIER_RANGE = {
  1: [40, 90],
  2: [90, 180],
  3: [180, 320],
  4: [320, 600]
}

const TIER_NAME = { 1: '下界灵星', 2: '中界灵星', 3: '上界灵星', 4: '古神之地' }

/** 首次开辟时为诸星随机生成灵蕴总量 */
function genPlanets() {
  const o = {}
  for (const p of PLANETS) {
    const [a, b] = TIER_RANGE[p.tier]
    const total = a + Math.floor(Math.random() * (b - a + 1))
    o[p.id] = { total, left: total }
  }
  return o
}

/** 某星是否已枯竭（不可再修） */
function isDrained(p) {
  const st = b.state.planets[p.id]
  return !st || st.left <= 0
}

const quoteIdx = ref(0)
let quoteTimer = null

/* ---------------- 存档 ---------------- */
const KEY_PREFIX = 'xianni_benzun_'

function storageKey() {
  return KEY_PREFIX + (props.username || 'guest')
}

const b = reactive({
  // exp 本尊修为 / level 当前第几重（0 起）/ planets 各星灵蕴余量
  state: { exp: 0, level: 0, planets: genPlanets() }
})

function loadState() {
  try {
    const raw = localStorage.getItem(storageKey())
    if (raw) {
      const s = JSON.parse(raw) || {}
      b.state.exp = Math.max(0, Math.floor(Number(s.exp) || 0))
      b.state.level = Math.max(0, Math.min(GOD_REALMS.length - 1, Math.floor(Number(s.level) || 0)))
      // 星球灵蕴：缺哪颗补哪颗（新增星球也能自动补上），已枯竭的保持枯竭
      const saved = s.planets && typeof s.planets === 'object' ? s.planets : {}
      const fresh = genPlanets()
      const merged = {}
      for (const p of PLANETS) {
        const sv = saved[p.id]
        if (sv && Number(sv.left) >= 0 && Number(sv.total) > 0) {
          merged[p.id] = { total: Number(sv.total), left: Math.min(Number(sv.left), Number(sv.total)) }
        } else {
          merged[p.id] = fresh[p.id]
        }
      }
      b.state.planets = merged
      return
    }
  } catch {
    /* 档案损坏则从零再修 */
  }
  b.state.exp = 0
  b.state.level = 0
  b.state.planets = genPlanets()
}

function saveState() {
  try {
    localStorage.setItem(
      storageKey(),
      JSON.stringify({ exp: b.state.exp, level: b.state.level, planets: b.state.planets })
    )
  } catch {
    /* 隐私模式忽略 */
  }
}

onMounted(() => {
  loadState()
  // 箴言随机轮换：四息一换，且不与当前重复，如本尊低语
  quoteTimer = setInterval(() => {
    if (QUOTES.length <= 1) return
    let next
    do {
      next = Math.floor(Math.random() * QUOTES.length)
    } while (next === quoteIdx.value)
    quoteIdx.value = next
  }, 4000)
})

onBeforeUnmount(() => {
  clearInterval(quoteTimer)
  clearTimeout(harvestTimer)
})

/* ---------------- 状态推导 ---------------- */
const atTop = computed(() => b.state.level >= GOD_REALMS.length - 1)
const realmName = computed(() => GOD_REALMS[b.state.level].name)
/** 突破所需：当前重数的 need（圆满后进入下一重则清零重计） */
const need = computed(() => GOD_REALMS[b.state.level].need)
const pct = computed(() => Math.min(100, Math.round((b.state.exp / need.value) * 100)))
const canBreak = computed(() => !atTop.value && b.state.exp >= need.value)

/* ---------------- 冷却：防连点 ---------------- */
const cooling = ref(false)
let coolTimer = null

function withCool(ms, fn) {
  if (cooling.value) return
  cooling.value = true
  coolTimer = setTimeout(() => {
    cooling.value = false
    coolTimer = null
  }, ms)
  fn()
}

/* ---------------- 弹窗提示 ---------------- */
const modal = reactive({ show: false, title: '', text: '' })
let modalTimer = null

function openModal(title, text) {
  modal.title = title
  modal.text = text
  modal.show = true
  clearTimeout(modalTimer)
  modalTimer = setTimeout(closeModal, 3600)
}

function closeModal() {
  modal.show = false
  clearTimeout(modalTimer)
  modalTimer = null
}

/* ---------------- 苦修三课 ---------------- */
/** 静坐吐纳：灵气入体则小有所得，灵气稀薄则收获甚微 */
function onBreath() {
  withCool(2000, () => {
    const rich = Math.random() > 0.3
    const gain = rich ? 8 + Math.floor(Math.random() * 8) : 1
    b.state.exp += gain
    saveState()
    openModal(
      '静坐吐纳',
      rich ? '灵气入体流转，本尊修为缓缓提升。' : '灵气稀薄，此番吐纳收获甚微。'
    )
    if (rich) emit('toast', `本尊修为 +${gain}`)
  })
}

/** 感悟道则：参悟得法则修为微长，否则无果 */
function onDao() {
  withCool(4000, () => {
    const hit = Math.random() > 0.4
    const gain = hit ? 20 + Math.floor(Math.random() * 15) : 0
    if (gain > 0) {
      b.state.exp += gain
      saveState()
    }
    openModal(
      '感悟道则',
      hit ? '参悟天地法理，修为微长，道心稍定。' : '道则玄妙难寻，本次感悟无果。'
    )
    if (hit) emit('toast', `本尊修为 +${gain}`)
  })
}

/* ---------------- 星域古图 · 收取灵蕴 ---------------- */
const harvestCool = ref(false)
let harvestTimer = null

/**
 * 点星球收取灵蕴：一次随机收取 8~25 点灵蕴化为本尊修为，
 * 星球余量随之递减；取尽即枯竭，永久不可再修（存档一并记录）
 */
function onPlanet(p) {
  if (harvestCool.value) return
  const st = b.state.planets[p.id]
  if (!st || st.left <= 0) {
    emit('toast', `「${p.name}」灵蕴已枯竭 · 不可再修`)
    return
  }
  harvestCool.value = true
  harvestTimer = setTimeout(() => {
    harvestCool.value = false
    harvestTimer = null
  }, 900)
  const gain = Math.min(st.left, 8 + Math.floor(Math.random() * 18))
  st.left -= gain
  b.state.exp += gain
  saveState()
  if (st.left <= 0) {
    st.left = 0
    openModal('灵蕴收取', `「${p.name}」灵蕴入体，本尊修为 +${gain}。此星灵蕴已枯竭，从此不可再修。`)
    emit('toast', `「${p.name}」灵蕴枯竭 · 本尊修为 +${gain}`)
  } else {
    openModal('灵蕴收取', `「${p.name}」灵蕴入体，本尊修为 +${gain}。此星尚余 ${st.left} 灵蕴。`)
  }
}

/** 尝试突破：修为足够方可一搏；成则踏入新境，败则道心动摇折损修为 */function onBreak() {
  if (atTop.value) {
    emit('toast', '肉身已臻古神之巅 · 何须再破')
    return
  }
  if (!canBreak.value) {
    emit('toast', `修为未足 · 尚需 ${need.value - b.state.exp} 修为方可冲击`)
    return
  }
  withCool(2500, () => {
    if (Math.random() < 0.45) {
      // 突破成功：消耗本次所需修为，踏入全新境界
      b.state.exp -= need.value
      b.state.level += 1
      saveState()
      openModal('突破成功', '逆道破障！本尊成功踏入全新境界！')
      emit('toast', `本尊已入「${realmName.value}」`)
    } else {
      // 突破失败：道心动摇，折损三成修为
      const lost = Math.max(1, Math.floor(b.state.exp * 0.3))
      b.state.exp -= lost
      saveState()
      openModal('突破失败', '道心动摇，突破受阻，损耗部分修为。')
      emit('toast', `突破受阻 · 本尊修为 -${lost}`)
    }
  })
}
</script>

<style scoped>
.bz {
  position: relative;
  z-index: 3;
  max-width: 1080px;
  margin: 0 auto;
  padding: 26px 24px 40px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* ---------- 头部 ---------- */
.bz-head {
  display: flex;
  align-items: center;
  gap: 18px;
}

.bz-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 10px;
  border: 1px solid rgba(111, 233, 255, 0.28);
  background: rgba(111, 233, 255, 0.07);
  color: rgba(111, 233, 255, 0.92);
  font-family: inherit;
  font-size: 14px;
  letter-spacing: 2px;
  cursor: pointer;
  transition: 0.22s;
}

.bz-back svg {
  width: 15px;
  height: 15px;
}

.bz-back:hover {
  color: #1a1023;
  background: linear-gradient(100deg, var(--cyan), #b8f4ff 70%, var(--gold));
  border-color: transparent;
}

.bz-title h2 {
  font-family: var(--font-display);
  font-size: 34px;
  letter-spacing: 6px;
  color: var(--gold);
  text-shadow: 0 0 20px rgba(232, 198, 107, 0.45);
}

.bz-title p {
  margin-top: 4px;
  font-size: 13px;
  letter-spacing: 3px;
  color: rgba(233, 238, 251, 0.6);
}

/* ---------- 面板通用 ---------- */
.bz-panel {
  position: relative;
  padding: 20px 20px 18px;
  border-radius: 16px;
  background: linear-gradient(160deg, rgba(20, 22, 46, 0.62), rgba(8, 9, 22, 0.7));
  border: 1px solid rgba(111, 233, 255, 0.18);
  box-shadow: inset 0 0 26px rgba(58, 42, 107, 0.24);
  backdrop-filter: blur(10px);
}

.panel-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.panel-head h3 {
  margin-right: auto;
  font-family: var(--font-display);
  font-size: 21px;
  letter-spacing: 4px;
  color: var(--gold);
}

.panel-count {
  font-size: 12px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.5);
}

/* ---------- 页面简介 ---------- */
.bz-intro {
  padding: 16px 20px;
}

.intro-text {
  font-size: 14px;
  letter-spacing: 2px;
  line-height: 1.8;
  color: rgba(233, 238, 251, 0.78);
  text-align: center;
}

.intro-text::before,
.intro-text::after {
  content: '❖';
  margin: 0 12px;
  font-size: 11px;
  color: rgba(232, 198, 107, 0.6);
}

/* ---------- 主体布局：左主右侧 ---------- */
.bz-body {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 18px;
  align-items: start;
}

.bz-main {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* ---------- 本尊状态 ---------- */
.st-row {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.st-left {
  flex: 1;
}

.st-label {
  font-size: 12px;
  letter-spacing: 3px;
  color: rgba(233, 238, 251, 0.5);
}

.st-realm {
  margin-top: 6px;
  font-family: var(--font-display);
  font-size: 30px;
  letter-spacing: 4px;
  color: #fff;
  text-shadow: 0 0 18px rgba(111, 233, 255, 0.5);
}

.st-level {
  margin-top: 6px;
  font-size: 13px;
  letter-spacing: 2px;
  color: rgba(111, 233, 255, 0.85);
}

.st-right {
  text-align: right;
}

.st-exp {
  font-family: var(--font-display);
  font-size: 26px;
  letter-spacing: 2px;
  color: var(--gold);
}

.st-exp-label {
  margin-top: 4px;
  font-size: 12px;
  letter-spacing: 3px;
  color: rgba(233, 238, 251, 0.5);
}

.st-mid {
  margin-top: 16px;
}

.st-bar {
  height: 8px;
  border-radius: 6px;
  background: rgba(8, 9, 22, 0.8);
  border: 1px solid rgba(111, 233, 255, 0.2);
  overflow: hidden;
}

.st-bar i {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, var(--cyan), var(--gold));
  transition: width 0.5s ease;
}

/* 修为已足：进度条泛起古神金芒 */
.st-bar i.glow {
  animation: barGlow 1.6s ease-in-out infinite;
}

@keyframes barGlow {
  0%,
  100% {
    box-shadow: 0 0 6px rgba(232, 198, 107, 0.5);
  }
  50% {
    box-shadow: 0 0 16px rgba(232, 198, 107, 0.95);
  }
}

.st-tip {
  margin-top: 8px;
  font-size: 12px;
  letter-spacing: 1px;
  color: rgba(233, 238, 251, 0.6);
}

/* ---------- 苦修三课 ---------- */
.act-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.act-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 12px 16px;
  border-radius: 14px;
  border: 1px solid rgba(111, 233, 255, 0.24);
  background: linear-gradient(160deg, rgba(58, 42, 107, 0.3), rgba(8, 9, 22, 0.55));
  color: #e9eefb;
  font-family: inherit;
  cursor: pointer;
  transition: 0.24s;
}

.act-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  border-color: rgba(232, 198, 107, 0.55);
  box-shadow: 0 0 22px rgba(232, 198, 107, 0.18);
}

.act-btn:disabled {
  opacity: 0.5;
  cursor: wait;
}

.act-ico {
  font-size: 24px;
  color: var(--cyan);
  text-shadow: 0 0 14px rgba(111, 233, 255, 0.6);
}

.act-name {
  font-size: 15px;
  letter-spacing: 2px;
  color: var(--gold);
}

.act-desc {
  font-size: 12px;
  letter-spacing: 1px;
  color: rgba(233, 238, 251, 0.55);
}

/* 突破按钮：修为已足时金光常亮 */
.act-btn.break {
  border-color: rgba(232, 198, 107, 0.28);
}

.act-btn.break.ready {
  border-color: rgba(232, 198, 107, 0.7);
  background: linear-gradient(160deg, rgba(232, 198, 107, 0.16), rgba(8, 9, 22, 0.55));
  box-shadow: 0 0 20px rgba(232, 198, 107, 0.22);
}

.act-btn.break.ready .act-ico {
  color: var(--gold);
  text-shadow: 0 0 16px rgba(232, 198, 107, 0.8);
}

/* ---------- 星域古图 ---------- */
.map-wrap {
  position: relative;
  border-radius: 12px;
  border: 1px solid rgba(111, 233, 255, 0.16);
  background: #05060f;
  overflow: hidden;
}

/* 星域背景图：放大左上对齐，右下（原图水印区）被裁出视野 */
.map-bg {
  position: absolute;
  inset: 0;
  width: 116%;
  height: 116%;
  object-fit: cover;
  object-position: left top;
  pointer-events: none;
}

/* 星图薄纱：压暗四角、突出星球，同时进一步淡化边缘 */
.map-veil {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(120% 110% at 50% 45%, rgba(5, 6, 15, 0) 42%, rgba(5, 6, 15, 0.55) 100%),
    linear-gradient(to bottom, rgba(5, 6, 15, 0.14), rgba(5, 6, 15, 0));
  pointer-events: none;
}

.starmap {
  position: relative;
  display: block;
  width: 100%;
  height: auto;
}

.planet {
  cursor: pointer;
}

/* 星球贴图：黑底以滤色融入星空 */
.p-img {
  mix-blend-mode: screen;
  transition: filter 0.22s, opacity 0.22s;
  filter: saturate(1.05) brightness(1.02);
}

.planet:hover .p-img {
  filter: brightness(1.3) saturate(1.15);
}

/* 枯竭之星：黯淡不可再修 */
.planet.dead {
  cursor: default;
  opacity: 0.38;
}

.planet.dead .p-img {
  filter: grayscale(1) brightness(0.55);
}

.planet.dead:hover .p-img {
  filter: grayscale(1) brightness(0.55);
}

.p-name {
  text-anchor: middle;
  font-size: 12px;
  letter-spacing: 1px;
  fill: rgba(233, 238, 251, 0.92);
  paint-order: stroke;
  stroke: rgba(5, 6, 15, 0.8);
  stroke-width: 2.5px;
}

.p-left {
  text-anchor: middle;
  font-size: 10px;
  fill: rgba(232, 198, 107, 0.85);
  paint-order: stroke;
  stroke: rgba(5, 6, 15, 0.8);
  stroke-width: 2px;
}

.planet.dead .p-left {
  fill: rgba(233, 238, 251, 0.4);
}

.map-legend {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}

.lg {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  letter-spacing: 1px;
  color: rgba(233, 238, 251, 0.6);
}

.lg i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  box-shadow: 0 0 6px rgba(255, 255, 255, 0.35);
}

.lg-tip {
  margin-left: auto;
  font-size: 11px;
  letter-spacing: 1px;
  color: rgba(233, 238, 251, 0.35);
}

@media (max-width: 620px) {
  .lg-tip {
    margin-left: 0;
  }
}

/* ---------- 侧边常驻短句 ---------- */
.quotes {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quotes li {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 12px;
  border-radius: 10px;
  background: rgba(8, 9, 22, 0.45);
  border: 1px solid rgba(111, 233, 255, 0.1);
  transition: 0.5s;
}

.quotes li.on {
  border-color: rgba(232, 198, 107, 0.5);
  background: rgba(58, 42, 107, 0.3);
  box-shadow: 0 0 16px rgba(232, 198, 107, 0.12);
}

.q-no {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  font-size: 11px;
  color: var(--gold);
  background: rgba(232, 198, 107, 0.12);
  border: 1px solid rgba(232, 198, 107, 0.35);
}

.quotes p {
  font-size: 13px;
  letter-spacing: 1px;
  line-height: 1.7;
  color: rgba(233, 238, 251, 0.75);
}

.quotes li.on p {
  color: var(--gold);
}

/* ---------- 弹窗提示 ---------- */
.bz-mask {
  position: fixed;
  inset: 0;
  z-index: 40;
  display: grid;
  place-items: center;
  background: rgba(4, 5, 12, 0.66);
  backdrop-filter: blur(4px);
}

.bz-modal {
  width: min(420px, calc(100vw - 48px));
  padding: 30px 26px 22px;
  border-radius: 18px;
  background: linear-gradient(160deg, rgba(24, 26, 54, 0.97), rgba(8, 9, 22, 0.98));
  border: 1px solid rgba(232, 198, 107, 0.5);
  box-shadow:
    0 22px 60px rgba(0, 0, 0, 0.65),
    0 0 34px rgba(232, 198, 107, 0.18),
    inset 0 0 30px rgba(58, 42, 107, 0.3);
  text-align: center;
}

.modal-title {
  font-family: var(--font-display);
  font-size: 22px;
  letter-spacing: 4px;
  color: var(--gold);
  text-shadow: 0 0 18px rgba(232, 198, 107, 0.5);
}

.modal-text {
  margin-top: 14px;
  font-size: 14px;
  letter-spacing: 1.5px;
  line-height: 1.9;
  color: rgba(233, 238, 251, 0.85);
}

.modal-btn {
  margin-top: 20px;
  padding: 9px 30px;
  border-radius: 10px;
  border: 1px solid rgba(232, 198, 107, 0.45);
  background: rgba(232, 198, 107, 0.12);
  color: var(--gold);
  font-family: inherit;
  font-size: 14px;
  letter-spacing: 4px;
  cursor: pointer;
  transition: 0.22s;
}

.modal-btn:hover {
  color: #1a1023;
  background: linear-gradient(100deg, var(--gold), #ffe9a8 60%, var(--cyan));
  border-color: transparent;
}

.fade-enter-active,
.fade-leave-active {
  transition: 0.25s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ---------- 响应式 ---------- */
@media (max-width: 860px) {
  .bz-body {
    grid-template-columns: 1fr;
  }

  .act-grid {
    grid-template-columns: 1fr;
  }
}
</style>
