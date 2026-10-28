<template>
  <section class="cultivate">
    <!-- ================= 头部 ================= -->
    <div class="cv-head">
      <button class="cv-back" @click="onBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
          <path d="M15 5 8 12l7 7" />
        </svg>
        {{ game ? '收起此试' : '返回仙门' }}
      </button>
      <div class="cv-title">
        <h2>{{ game ? current.name : '修炼' }}</h2>
        <p>{{ game ? current.sub : `${username} · 闭门苦修，一日千里` }}</p>
      </div>
    </div>

    <!-- ================= 修为总览 ================= -->
    <div class="cv-panel cv-status">
      <div class="st-realm">
        <p class="st-label">当前境界</p>
        <p class="st-name">{{ realm.name }}{{ realm.suffix }}</p>
        <p class="st-level">第 {{ realm.level }} {{ realm.unit }}</p>
      </div>

      <div class="st-mid">
        <div class="st-bar"><i :style="{ width: pct + '%' }"></i></div>
        <p class="st-tip">
          <span>{{ next ? `距 ${next.name}${next.suffix}圆满还需 ${next.need} 修为` : '已踏完第九桥 · 逆天超脱' }}</span>
        </p>
        <p class="st-today">
          今日修炼 <b>{{ cave.trainExp }}</b> / {{ TRAIN_LIMIT }}　·　尚余 <b>{{ remaining }}</b>
        </p>
      </div>

      <div class="st-exp">
        <p class="st-exp-num">{{ cave.exp }}</p>
        <p class="st-exp-label">累计修为</p>
      </div>
    </div>

    <!-- ================= 境界总览表 ================= -->
    <RealmTable v-if="!game" class="cv-realm" :default-open="true" />

    <!-- ================= 法门选择 ================= -->
    <div v-if="!game" class="cv-games">
      <article v-for="g in games" :key="g.key" class="cv-game" @click="startGame(g.key)">
        <span class="cg-ico">{{ g.ico }}</span>
        <h3>{{ g.name }}</h3>
        <p class="cg-desc">{{ g.desc }}</p>
        <div class="cg-foot">
          <span class="cg-exp">{{ g.expText }}</span>
          <span class="cg-go">入定 →</span>
        </div>
      </article>

      <p class="cv-note">
        每日三课，可得修为上限 {{ TRAIN_LIMIT }} 点，跨日自新。修为入洞府账中，境界随之精进。
      </p>
    </div>

    <!-- ================= 修炼台 ================= -->
    <div v-else class="cv-stage">
      <!-- ---------- 御剑斩妖 ---------- -->
      <div v-if="game === 'hunt'" class="g-wrap">
        <div class="g-hud">
          <span>余时 <b>{{ hunt.left }}</b> 息</span>
          <span>斩妖 <b>{{ hunt.hits }}</b></span>
          <span>连击 <b>x{{ hunt.combo }}</b></span>
          <span>所得 <b>+{{ hunt.gained }}</b></span>
        </div>

        <div class="hunt-grid">
          <button
            v-for="(c, i) in hunt.cells"
            :key="i"
            class="hunt-cell"
            :class="c ? (c.type === 'demon' ? 'demon' : 'spirit') : ''"
            @click="strike(i)"
          >
            <span v-if="c" class="hc-face">{{ c.icon }}</span>
          </button>
        </div>

        <div class="g-acts">
          <button v-if="!hunt.running && !result" class="g-btn" @click="startHunt">起剑 · 开始斩妖</button>
          <p v-if="hunt.running" class="g-tip">妖灵（妖）现形即刻点斩 · 心魔（魇）触之则连击尽散</p>
        </div>
      </div>

      <!-- ---------- 炼丹火候 ---------- -->
      <div v-else-if="game === 'alchemy'" class="g-wrap">
        <div class="g-hud">
          <span>丹炉 <b>{{ Math.min(alch.round, 3) }}</b> / 3</span>
          <span>所得 <b>+{{ alch.gained }}</b></span>
        </div>

        <div class="alch-furnace">
          <div class="alch-orb" :class="{ hot: alch.moving }">丹</div>
        </div>

        <div class="alch-bar">
          <div class="alch-zone" :style="zoneStyle"></div>
          <i class="alch-cursor" :style="{ left: alch.pos + '%' }"></i>
        </div>

        <ul v-if="alch.results.length" class="alch-log">
          <li v-for="(r, i) in alch.results" :key="i">{{ r }}</li>
        </ul>

        <div class="g-acts">
          <button v-if="!alch.playing && !result" class="g-btn" @click="startAlch">起火 · 开炉炼丹</button>
          <button v-else-if="alch.playing && alch.moving" class="g-btn" @click="sealFurnace">凝 · 丹成</button>
          <button v-else-if="alch.playing" class="g-btn" disabled>炉火重燃…</button>
          <p v-if="alch.playing" class="g-tip">火候游走，落于中心天火之位方可凝成上丹</p>
        </div>
      </div>

      <!-- ---------- 阵心推演 ---------- -->
      <div v-else-if="game === 'array'" class="g-wrap">
        <div class="g-hud">
          <span>重数 <b>{{ zhen.round }}</b> / 8</span>
          <span>所得 <b>+{{ zhen.gained }}</b></span>
        </div>

        <div class="rune-grid">
          <button
            v-for="(r, i) in RUNES"
            :key="r.name"
            class="rune"
            :class="[r.cls, { lit: zhen.lit === i }]"
            @click="tapZhen(i)"
          >
            <span class="rune-name">{{ r.name }}</span>
            <span class="rune-sub">{{ r.sub }}</span>
          </button>
        </div>

        <p class="zhen-hint">{{ zhen.hint }}</p>

        <div class="g-acts">
          <button v-if="zhen.phase === 'idle'" class="g-btn" @click="startZhen">入阵 · 开始推演</button>
        </div>
      </div>

      <!-- ---------- 结算 ---------- -->
      <div v-if="result" class="g-result">
        <p class="gr-title">修炼结算</p>
        <p class="gr-detail">{{ result.detail }}</p>
        <div class="gr-row">
          <span>本次修为 <b>+{{ result.exp }}</b></span>
          <span>今日尚余 <b>{{ result.left }}</b></span>
        </div>
        <p v-if="result.capped" class="gr-warn">今日修炼已至上限，所得不再累加，待明日再续</p>
        <p v-if="result.levelUp" class="gr-up">
          ✦ 境界精进 · {{ result.realm.name }}{{ result.realm.suffix }} 第 {{ result.realm.level }}
          {{ result.realm.unit }} ✦
        </p>
        <div class="gr-acts">
          <button class="g-btn" @click="restart">再试一次</button>
          <button class="g-btn ghost" @click="exitGame">换个法门</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, reactive, computed, onBeforeUnmount } from 'vue'
import RealmTable from './RealmTable.vue'
import {
  cave,
  realm,
  realmProgress,
  nextRealm,
  addTrainExp,
  trainRemaining,
  TRAIN_LIMIT
} from '../composables/useCave.js'

const props = defineProps({
  username: { type: String, default: '道友' }
})

const emit = defineEmits(['back', 'toast'])

/* ---------------- 通用状态 ---------------- */
const game = ref('')          // '' | hunt | alchemy | array
const result = ref(null)      // 结算数据
const pending = []            // 待清理的定时器

const pct = computed(() => Math.round(realmProgress.value * 100))
const next = computed(() => nextRealm.value)
const remaining = computed(() => trainRemaining())

const games = [
  {
    key: 'hunt',
    ico: '⚔',
    name: '御剑斩妖',
    sub: '剑随心动 · 一斩千里',
    desc: '三十息内以剑意斩尽现身妖灵，连击愈绵长，剑势愈沉厚。',
    expText: '每斩一妖 +4 起（连击加成）'
  },
  {
    key: 'alchemy',
    ico: '⚗',
    name: '炼丹火候',
    sub: '文武之火 · 凝气成丹',
    desc: '炉火往复不定，于天火之位凝丹，三炉之中验你一线真火。',
    expText: '天火 +30 · 地火 +18 · 凡火 +6'
  },
  {
    key: 'array',
    ico: '☯',
    name: '阵心推演',
    sub: '以心布阵 · 一念千变',
    desc: '四象灵纹依次亮起，依其序复诵布阵，一重深过一重。',
    expText: '每过一重 +12 修为'
  }
]

const current = computed(() => games.find((g) => g.key === game.value) || games[0])

/* ---------------- 音效（无资源，纯合成） ---------------- */
let actx = null
function beep(freq = 520, dur = 0.09, type = 'sine', vol = 0.035) {
  try {
    const AC = window.AudioContext || window.webkitAudioContext
    if (!AC) return
    if (!actx) actx = new AC()
    if (actx.state === 'suspended') actx.resume()
    const osc = actx.createOscillator()
    const gain = actx.createGain()
    osc.type = type
    osc.frequency.value = freq
    gain.gain.value = vol
    osc.connect(gain)
    gain.connect(actx.destination)
    osc.start()
    gain.gain.exponentialRampToValueAtTime(0.0001, actx.currentTime + dur)
    osc.stop(actx.currentTime + dur)
  } catch {
    /* 无音频环境时静默 */
  }
}

/* =============================================================
 * 御剑斩妖
 * ============================================================= */
const HUNT_TIME = 30

const hunt = reactive({
  running: false,
  left: HUNT_TIME,
  hits: 0,
  combo: 0,
  best: 0,
  gained: 0,
  cells: Array(9).fill(null),
  timer: null,
  spawnTimer: null,
  timers: []
})

function resetHunt() {
  clearHuntTimers()
  hunt.running = false
  hunt.left = HUNT_TIME
  hunt.hits = 0
  hunt.combo = 0
  hunt.best = 0
  hunt.gained = 0
  hunt.cells = Array(9).fill(null)
}

function clearHuntTimers() {
  if (hunt.timer) clearInterval(hunt.timer)
  if (hunt.spawnTimer) clearTimeout(hunt.spawnTimer)
  hunt.timer = null
  hunt.spawnTimer = null
  hunt.timers.forEach((t) => clearTimeout(t))
  hunt.timers = []
}

function startHunt() {
  resetHunt()
  result.value = null
  hunt.running = true
  hunt.timer = setInterval(() => {
    hunt.left -= 1
    if (hunt.left <= 0) endHunt()
  }, 1000)
  spawnLoop()
}

function spawnLoop() {
  if (!hunt.running) return
  spawnOne()
  const elapsed = HUNT_TIME - hunt.left
  const delay = Math.max(360, 820 - elapsed * 12) + Math.random() * 260
  hunt.spawnTimer = setTimeout(spawnLoop, delay)
}

function spawnOne() {
  const free = hunt.cells.map((c, i) => (c ? -1 : i)).filter((i) => i >= 0)
  if (!free.length) return

  const idx = free[Math.floor(Math.random() * free.length)]
  const isSpirit = Math.random() < 0.22
  const myId = Math.random()
  const life = Math.max(460, 1050 - (HUNT_TIME - hunt.left) * 14)

  hunt.cells[idx] = {
    id: myId,
    type: isSpirit ? 'spirit' : 'demon',
    icon: isSpirit ? '魇' : '妖'
  }

  const t = setTimeout(() => {
    const cur = hunt.cells[idx]
    if (!cur || cur.id !== myId) return
    // 妖灵遁走：断连击
    if (cur.type === 'demon' && hunt.running) hunt.combo = 0
    hunt.cells[idx] = null
  }, life)
  hunt.timers.push(t)
}

function strike(i) {
  const c = hunt.cells[i]
  if (!c || !hunt.running) return
  hunt.cells[i] = null

  if (c.type === 'demon') {
    hunt.hits += 1
    hunt.combo += 1
    if (hunt.combo > hunt.best) hunt.best = hunt.combo
    // 基础 4 修为，连击每满 3 追加 1，单次最多 +10
    hunt.gained += 4 + Math.min(6, Math.floor(hunt.combo / 3))
    beep(640 + Math.min(420, hunt.combo * 26), 0.06, 'triangle')
  } else {
    hunt.combo = 0
    beep(170, 0.16, 'sawtooth', 0.03)
    emit('toast', '误触心魔 · 连击已散')
  }
}

function endHunt() {
  hunt.running = false
  clearHuntTimers()
  hunt.cells = Array(9).fill(null)
  settle(hunt.gained, `斩妖 ${hunt.hits} 头 · 最高连击 x${hunt.best}`)
}

/* =============================================================
 * 炼丹火候
 * ============================================================= */
const alch = reactive({
  playing: false,
  moving: false,
  round: 1,
  pos: 0,
  dir: 1,
  speed: 48,
  center: 50,
  half: 17,
  results: [],
  gained: 0,
  raf: 0,
  last: 0
})

const zoneStyle = computed(() => ({
  left: alch.center - alch.half + '%',
  width: alch.half * 2 + '%'
}))

function startAlch() {
  stopAlch()
  result.value = null
  alch.round = 1
  alch.gained = 0
  alch.results = []
  alch.playing = true
  beginAlchRound()
}

function beginAlchRound() {
  alch.center = 24 + Math.random() * 52
  alch.half = Math.max(9, 17 - (alch.round - 1) * 3)
  alch.speed = 48 + (alch.round - 1) * 18
  alch.pos = 0
  alch.dir = 1
  alch.last = 0
  alch.moving = true
  alch.raf = requestAnimationFrame(loopAlch)
}

function loopAlch(ts) {
  if (!alch.moving) return
  if (!alch.last) alch.last = ts
  const dt = Math.min(64, ts - alch.last) / 1000
  alch.last = ts
  alch.pos += alch.dir * alch.speed * dt
  if (alch.pos >= 100) {
    alch.pos = 100
    alch.dir = -1
  } else if (alch.pos <= 0) {
    alch.pos = 0
    alch.dir = 1
  }
  alch.raf = requestAnimationFrame(loopAlch)
}

function sealFurnace() {
  if (!alch.moving) return
  alch.moving = false
  if (alch.raf) cancelAnimationFrame(alch.raf)
  alch.raf = 0

  const dist = Math.abs(alch.pos - alch.center)
  let grade
  let exp
  if (dist <= alch.half * 0.42) {
    grade = '天火 · 丹成有光'
    exp = 30
    beep(880, 0.2, 'sine', 0.045)
  } else if (dist <= alch.half) {
    grade = '地火 · 丹成可用'
    exp = 18
    beep(620, 0.15)
  } else {
    grade = '凡火 · 丹有微瑕'
    exp = 6
    beep(300, 0.18, 'sine', 0.03)
  }

  alch.gained += exp
  alch.results.push(`第 ${alch.round} 炉 · ${grade}（+${exp}）`)

  if (alch.round >= 3) {
    alch.playing = false
    const best = alch.results.filter((r) => r.includes('天火')).length
    settle(alch.gained, `三炉丹成 · 天火 ${best} 次`)
    return
  }

  alch.round += 1
  pending.push(setTimeout(beginAlchRound, 720))
}

function stopAlch() {
  alch.moving = false
  alch.playing = false
  if (alch.raf) cancelAnimationFrame(alch.raf)
  alch.raf = 0
}

/* =============================================================
 * 阵心推演
 * ============================================================= */
const RUNES = [
  { name: '青龙', sub: '东', cls: 'r-cyan' },
  { name: '白虎', sub: '西', cls: 'r-gold' },
  { name: '朱雀', sub: '南', cls: 'r-red' },
  { name: '玄武', sub: '北', cls: 'r-violet' }
]

const ZEN_MAX_ROUND = 8

const zhen = reactive({
  phase: 'idle', // idle | show | input | wait | done
  round: 1,
  seq: [],
  input: [],
  lit: -1,
  gained: 0,
  hint: '凝神静气，方可入阵'
})

function startZhen() {
  result.value = null
  zhen.round = 1
  zhen.gained = 0
  zhen.seq = []
  zhen.input = []
  zhen.lit = -1
  nextZhenRound()
}

function nextZhenRound() {
  zhen.input = []
  zhen.lit = -1
  zhen.phase = 'show'
  zhen.hint = '灵纹流转 · 静心默记'
  const len = 2 + zhen.round
  const seq = []
  for (let i = 0; i < len; i++) seq.push(Math.floor(Math.random() * RUNES.length))
  zhen.seq = seq
  playZhen()
}

function playZhen() {
  const step = 620
  zhen.seq.forEach((idx, i) => {
    pending.push(
      setTimeout(() => {
        zhen.lit = idx
        beep(430 + idx * 110, 0.12)
      }, 560 + i * step)
    )
    pending.push(
      setTimeout(() => {
        zhen.lit = -1
      }, 560 + i * step + 400)
    )
  })
  pending.push(
    setTimeout(() => {
      zhen.phase = 'input'
      zhen.hint = '依序点触灵纹，复其阵位'
    }, 560 + zhen.seq.length * step)
  )
}

function tapZhen(i) {
  if (zhen.phase !== 'input') return
  zhen.lit = i
  pending.push(
    setTimeout(() => {
      zhen.lit = -1
    }, 180)
  )
  beep(540 + i * 100, 0.1)

  zhen.input.push(i)
  const k = zhen.input.length - 1

  if (zhen.seq[k] !== i) {
    zhen.phase = 'done'
    zhen.hint = '阵纹错乱 · 推演止于此'
    beep(160, 0.3, 'sawtooth', 0.035)
    settle(zhen.gained, `推演过 ${zhen.round - 1} 重，止步第 ${zhen.round} 重`)
    return
  }

  if (zhen.input.length === zhen.seq.length) {
    zhen.gained += 12
    beep(760, 0.16, 'sine', 0.04)
    if (zhen.round >= ZEN_MAX_ROUND) {
      zhen.phase = 'done'
      zhen.hint = '八重圆满 · 心阵大成'
      settle(zhen.gained, '推演八重圆满 · 心阵大成')
      return
    }
    zhen.round += 1
    zhen.phase = 'wait'
    zhen.hint = `第 ${zhen.round} 重 · 灵纹将现`
    pending.push(setTimeout(nextZhenRound, 900))
  }
}

/* =============================================================
 * 结算与切换
 * ============================================================= */
function settle(rawExp, detail) {
  const r = addTrainExp(rawExp)
  result.value = {
    detail,
    exp: r.exp,
    left: r.left,
    capped: r.capped,
    levelUp: r.levelUp,
    realm: r.realm
  }
  if (r.exp > 0 && r.levelUp) beep(980, 0.3, 'sine', 0.05)
  emit('toast', r.msg)
}

function stopAll() {
  clearHuntTimers()
  hunt.running = false
  stopAlch()
  pending.forEach((t) => clearTimeout(t))
  pending.length = 0
}

function startGame(key) {
  stopAll()
  result.value = null
  game.value = key
  window.scrollTo({ top: 0, behavior: 'smooth' })

  if (key === 'hunt') resetHunt()
  if (key === 'alchemy') {
    alch.playing = false
    alch.moving = false
    alch.round = 1
    alch.gained = 0
    alch.results = []
    alch.pos = 0
    alch.dir = 1
    alch.center = 50
    alch.half = 17
    alch.raf = 0
  }
  if (key === 'array') {
    zhen.phase = 'idle'
    zhen.round = 1
    zhen.gained = 0
    zhen.seq = []
    zhen.input = []
    zhen.lit = -1
    zhen.hint = '凝神静气，方可入阵'
  }
}

function restart() {
  startGame(game.value)
}

function exitGame() {
  stopAll()
  game.value = ''
  result.value = null
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function onBack() {
  if (game.value) {
    exitGame()
    return
  }
  emit('back')
}

onBeforeUnmount(stopAll)
</script>

<style scoped>
.cultivate {
  max-width: 1000px;
  margin: 0 auto;
  padding: 34px 24px 60px;
  animation: cvIn 0.4s ease both;
}

@keyframes cvIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ================= 头部 ================= */
.cv-head {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 22px;
}

.cv-back {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 8px 15px;
  border-radius: 10px;
  border: 1px solid rgba(160, 190, 230, 0.24);
  background: rgba(255, 255, 255, 0.04);
  color: rgba(233, 238, 251, 0.78);
  font-family: inherit;
  font-size: 13px;
  letter-spacing: 2px;
  cursor: pointer;
  transition: 0.22s;
}

.cv-back svg {
  width: 15px;
  height: 15px;
}

.cv-back:hover {
  color: var(--cyan);
  border-color: rgba(111, 233, 255, 0.5);
  box-shadow: 0 0 16px rgba(111, 233, 255, 0.2);
}

.cv-title h2 {
  font-family: 'Ma Shan Zheng', serif;
  font-size: 30px;
  font-weight: 400;
  letter-spacing: 7px;
  color: #fff;
  text-shadow: 0 0 24px rgba(232, 198, 107, 0.4);
}

.cv-title p {
  margin-top: 4px;
  font-size: 12px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.45);
}

/* ================= 通用面板 ================= */
.cv-panel {
  border-radius: 16px;
  background: linear-gradient(160deg, rgba(20, 22, 46, 0.7), rgba(8, 9, 22, 0.76));
  border: 1px solid rgba(111, 233, 255, 0.18);
  box-shadow: inset 0 0 28px rgba(58, 42, 107, 0.24);
  backdrop-filter: blur(10px);
}

/* ================= 修为总览 ================= */
.cv-status {
  display: flex;
  align-items: center;
  gap: 22px;
  padding: 20px 24px;
  margin-bottom: 22px;
}

.st-realm {
  flex-shrink: 0;
  min-width: 108px;
}

.st-label {
  font-size: 11px;
  letter-spacing: 3px;
  color: rgba(233, 238, 251, 0.42);
}

.st-name {
  margin-top: 5px;
  font-family: 'Ma Shan Zheng', serif;
  font-size: 25px;
  letter-spacing: 3px;
  color: var(--gold);
  text-shadow: 0 0 16px rgba(232, 198, 107, 0.5);
}

.st-level {
  margin-top: 2px;
  font-size: 12px;
  letter-spacing: 2px;
  color: rgba(111, 233, 255, 0.85);
}

.st-mid {
  flex: 1;
  min-width: 0;
}

.st-bar {
  position: relative;
  height: 9px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.07);
  overflow: hidden;
  box-shadow: inset 0 0 8px rgba(0, 0, 0, 0.5);
}

.st-bar i {
  display: block;
  height: 100%;
  border-radius: 6px;
  background: linear-gradient(90deg, var(--gold), #ffe9a8 55%, var(--cyan));
  box-shadow: 0 0 14px rgba(232, 198, 107, 0.6);
  transition: width 0.45s ease;
}

.st-tip {
  margin-top: 8px;
  font-size: 12px;
  letter-spacing: 1px;
  color: rgba(233, 238, 251, 0.55);
}

.st-today {
  margin-top: 4px;
  font-size: 12px;
  letter-spacing: 1px;
  color: rgba(233, 238, 251, 0.45);
}

.st-today b {
  color: var(--cyan);
  font-weight: 400;
}

.st-exp {
  flex-shrink: 0;
  padding-left: 20px;
  border-left: 1px solid rgba(160, 190, 230, 0.14);
  text-align: right;
}

.st-exp-num {
  font-family: 'Ma Shan Zheng', serif;
  font-size: 26px;
  color: #fff;
  text-shadow: 0 0 18px rgba(111, 233, 255, 0.5);
}

.st-exp-label {
  margin-top: 2px;
  font-size: 11px;
  letter-spacing: 3px;
  color: rgba(233, 238, 251, 0.42);
}

/* ================= 境界总览 ================= */
.cv-realm {
  display: block;
  margin-bottom: 22px;
}

/* ================= 法门卡片 ================= */
.cv-games {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.cv-game {
  position: relative;
  padding: 24px 20px 18px;
  border-radius: 16px;
  background: linear-gradient(160deg, rgba(20, 22, 46, 0.66), rgba(8, 9, 22, 0.74));
  border: 1px solid rgba(111, 233, 255, 0.18);
  box-shadow: inset 0 0 26px rgba(58, 42, 107, 0.24);
  backdrop-filter: blur(10px);
  cursor: pointer;
  overflow: hidden;
  transition: 0.28s;
}

.cv-game::after {
  content: '';
  position: absolute;
  top: -60%;
  right: -40%;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(232, 198, 107, 0.16), transparent 70%);
  opacity: 0;
  transition: 0.35s;
}

.cv-game:hover {
  transform: translateY(-4px);
  border-color: rgba(232, 198, 107, 0.5);
  box-shadow:
    0 18px 44px rgba(0, 0, 0, 0.5),
    inset 0 0 30px rgba(58, 42, 107, 0.3);
}

.cv-game:hover::after {
  opacity: 1;
}

.cg-ico {
  font-size: 24px;
  color: var(--gold);
  text-shadow: 0 0 16px rgba(232, 198, 107, 0.6);
}

.cv-game h3 {
  margin: 12px 0 9px;
  font-family: 'Ma Shan Zheng', serif;
  font-size: 22px;
  font-weight: 400;
  letter-spacing: 3px;
  color: #fff;
}

.cg-desc {
  min-height: 66px;
  font-size: 13px;
  line-height: 1.75;
  color: rgba(233, 238, 251, 0.58);
}

.cg-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid rgba(160, 190, 230, 0.12);
  font-size: 12px;
}

.cg-exp {
  color: rgba(232, 198, 107, 0.9);
  letter-spacing: 0.5px;
}

.cg-go {
  flex-shrink: 0;
  color: rgba(111, 233, 255, 0.85);
  letter-spacing: 1px;
}

.cv-note {
  grid-column: 1 / -1;
  margin-top: 6px;
  text-align: center;
  font-size: 12px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.35);
}

/* ================= 修炼台 ================= */
.cv-stage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.g-wrap {
  padding: 22px 24px 24px;
  border-radius: 18px;
  background: linear-gradient(160deg, rgba(20, 22, 46, 0.74), rgba(8, 9, 22, 0.8));
  border: 1px solid rgba(111, 233, 255, 0.2);
  box-shadow:
    0 18px 50px rgba(0, 0, 0, 0.4),
    inset 0 0 30px rgba(58, 42, 107, 0.26);
  backdrop-filter: blur(10px);
}

.g-hud {
  display: flex;
  flex-wrap: wrap;
  gap: 26px;
  padding-bottom: 16px;
  margin-bottom: 18px;
  border-bottom: 1px dashed rgba(160, 190, 230, 0.16);
  font-size: 13px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.55);
}

.g-hud b {
  margin-left: 4px;
  font-family: 'Ma Shan Zheng', serif;
  font-size: 19px;
  font-weight: 400;
  color: var(--gold);
}

.g-acts {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-top: 22px;
}

.g-btn {
  padding: 12px 34px;
  border: none;
  border-radius: 12px;
  font-family: inherit;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 4px;
  color: #1a1023;
  background: linear-gradient(100deg, var(--gold), #ffe9a8 55%, var(--cyan));
  box-shadow:
    0 8px 26px rgba(232, 198, 107, 0.3),
    0 0 16px rgba(111, 233, 255, 0.26);
  cursor: pointer;
  transition: 0.25s;
}

.g-btn:hover {
  transform: translateY(-2px);
  box-shadow:
    0 14px 34px rgba(232, 198, 107, 0.44),
    0 0 24px rgba(111, 233, 255, 0.4);
}

.g-btn:active {
  transform: translateY(0);
}

.g-btn:disabled {
  opacity: 0.5;
  cursor: wait;
  transform: none;
}

.g-btn.ghost {
  border: 1px solid rgba(111, 233, 255, 0.42);
  background: rgba(111, 233, 255, 0.09);
  color: var(--cyan);
  box-shadow: none;
}

.g-btn.ghost:hover {
  background: rgba(111, 233, 255, 0.18);
  box-shadow: 0 0 18px rgba(111, 233, 255, 0.28);
}

.g-tip {
  font-size: 12px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.42);
}

/* ---------- 御剑斩妖 ---------- */
.hunt-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  max-width: 460px;
  margin: 0 auto;
}

.hunt-cell {
  position: relative;
  aspect-ratio: 1 / 1;
  border-radius: 14px;
  border: 1px solid rgba(111, 233, 255, 0.14);
  background:
    radial-gradient(circle at 50% 45%, rgba(58, 42, 107, 0.34), rgba(8, 9, 22, 0.7));
  cursor: pointer;
  transition: 0.18s;
}

.hunt-cell:hover {
  border-color: rgba(111, 233, 255, 0.35);
}

.hunt-cell.demon {
  border-color: rgba(255, 140, 120, 0.75);
  background: radial-gradient(circle at 50% 45%, rgba(150, 46, 40, 0.6), rgba(24, 8, 10, 0.9));
  box-shadow:
    0 0 24px rgba(255, 110, 90, 0.42),
    inset 0 0 22px rgba(255, 120, 90, 0.24);
  animation: demonIn 0.16s ease both;
}

.hunt-cell.spirit {
  border-color: rgba(160, 130, 255, 0.75);
  background: radial-gradient(circle at 50% 45%, rgba(70, 48, 150, 0.62), rgba(10, 8, 26, 0.9));
  box-shadow:
    0 0 22px rgba(150, 120, 255, 0.4),
    inset 0 0 22px rgba(150, 120, 255, 0.22);
  animation: demonIn 0.16s ease both;
}

@keyframes demonIn {
  from {
    transform: scale(0.72);
    opacity: 0.4;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.hc-face {
  font-family: 'Ma Shan Zheng', serif;
  font-size: 30px;
  color: #fff;
  text-shadow: 0 0 16px rgba(255, 200, 180, 0.85);
}

.hunt-cell.spirit .hc-face {
  color: #e3d8ff;
  text-shadow: 0 0 16px rgba(180, 160, 255, 0.9);
}

/* ---------- 炼丹火候 ---------- */
.alch-furnace {
  display: flex;
  justify-content: center;
  margin: 6px 0 22px;
}

.alch-orb {
  width: 104px;
  height: 104px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-family: 'Ma Shan Zheng', serif;
  font-size: 36px;
  color: rgba(232, 198, 107, 0.85);
  border: 1px solid rgba(232, 198, 107, 0.35);
  background: radial-gradient(circle at 50% 42%, rgba(232, 198, 107, 0.16), rgba(20, 12, 8, 0.9));
  transition: 0.4s;
}

.alch-orb.hot {
  color: #1a1023;
  border-color: transparent;
  background: radial-gradient(circle at 50% 42%, #ffe9a8, #e8c66b 55%, #a3702a);
  box-shadow:
    0 0 40px rgba(232, 198, 107, 0.55),
    0 0 80px rgba(255, 140, 60, 0.35);
  animation: orbPulse 1.4s ease-in-out infinite;
}

@keyframes orbPulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.045);
  }
}

.alch-bar {
  position: relative;
  height: 24px;
  max-width: 560px;
  margin: 0 auto;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.055);
  border: 1px solid rgba(160, 190, 230, 0.16);
  overflow: hidden;
}

.alch-zone {
  position: absolute;
  top: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(232, 198, 107, 0.42), rgba(232, 198, 107, 0.16));
  border-left: 1px solid rgba(232, 198, 107, 0.75);
  border-right: 1px solid rgba(232, 198, 107, 0.75);
  box-shadow: inset 0 0 18px rgba(232, 198, 107, 0.4);
  transition: 0.25s;
}

.alch-cursor {
  position: absolute;
  top: -3px;
  bottom: -3px;
  width: 3px;
  margin-left: -1.5px;
  border-radius: 2px;
  background: var(--cyan);
  box-shadow:
    0 0 12px rgba(111, 233, 255, 0.95),
    0 0 24px rgba(111, 233, 255, 0.6);
}

.alch-log {
  list-style: none;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  margin-top: 16px;
}

.alch-log li {
  padding: 5px 14px;
  border-radius: 20px;
  font-size: 12px;
  letter-spacing: 1px;
  color: rgba(232, 198, 107, 0.9);
  background: rgba(232, 198, 107, 0.08);
  border: 1px solid rgba(232, 198, 107, 0.28);
}

/* ---------- 阵心推演 ---------- */
.rune-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  max-width: 620px;
  margin: 0 auto;
}

.rune {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 22px 8px 18px;
  border-radius: 14px;
  border: 1px solid rgba(160, 190, 230, 0.18);
  background: rgba(255, 255, 255, 0.035);
  font-family: inherit;
  cursor: pointer;
  transition: 0.18s;
}

.rune-name {
  font-family: 'Ma Shan Zheng', serif;
  font-size: 22px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.82);
}

.rune-sub {
  font-size: 11px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.34);
}

.rune:hover {
  border-color: rgba(160, 190, 230, 0.4);
}

.rune.lit {
  transform: translateY(-3px) scale(1.03);
}

.rune.r-cyan.lit {
  border-color: var(--cyan);
  background: rgba(111, 233, 255, 0.18);
  box-shadow:
    0 0 26px rgba(111, 233, 255, 0.55),
    inset 0 0 22px rgba(111, 233, 255, 0.25);
}

.rune.r-gold.lit {
  border-color: var(--gold);
  background: rgba(232, 198, 107, 0.18);
  box-shadow:
    0 0 26px rgba(232, 198, 107, 0.55),
    inset 0 0 22px rgba(232, 198, 107, 0.25);
}

.rune.r-red.lit {
  border-color: #ff8b7a;
  background: rgba(255, 120, 100, 0.18);
  box-shadow:
    0 0 26px rgba(255, 120, 100, 0.5),
    inset 0 0 22px rgba(255, 120, 100, 0.24);
}

.rune.r-violet.lit {
  border-color: #a98cff;
  background: rgba(150, 120, 255, 0.2);
  box-shadow:
    0 0 26px rgba(150, 120, 255, 0.55),
    inset 0 0 22px rgba(150, 120, 255, 0.25);
}

.rune.lit .rune-name {
  color: #fff;
  text-shadow: 0 0 14px currentColor;
}

.zhen-hint {
  margin-top: 20px;
  text-align: center;
  font-size: 13px;
  letter-spacing: 3px;
  color: rgba(233, 238, 251, 0.55);
}

/* ---------- 结算 ---------- */
.g-result {
  padding: 24px;
  border-radius: 18px;
  text-align: center;
  background: linear-gradient(160deg, rgba(28, 24, 52, 0.86), rgba(10, 9, 24, 0.9));
  border: 1px solid rgba(232, 198, 107, 0.42);
  box-shadow:
    0 18px 50px rgba(0, 0, 0, 0.45),
    inset 0 0 34px rgba(232, 198, 107, 0.1);
  animation: cvIn 0.35s ease both;
}

.gr-title {
  font-size: 12px;
  letter-spacing: 6px;
  color: rgba(232, 198, 107, 0.7);
}

.gr-detail {
  margin-top: 12px;
  font-family: 'Ma Shan Zheng', serif;
  font-size: 24px;
  letter-spacing: 3px;
  color: #fff;
  text-shadow: 0 0 22px rgba(232, 198, 107, 0.4);
}

.gr-row {
  display: flex;
  justify-content: center;
  gap: 34px;
  margin-top: 16px;
  font-size: 13px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.55);
}

.gr-row b {
  margin-left: 4px;
  font-family: 'Ma Shan Zheng', serif;
  font-size: 22px;
  font-weight: 400;
  color: var(--gold);
}

.gr-warn {
  margin-top: 12px;
  font-size: 12px;
  letter-spacing: 1px;
  color: rgba(255, 160, 140, 0.9);
}

.gr-up {
  margin-top: 12px;
  font-size: 13px;
  letter-spacing: 3px;
  color: var(--cyan);
  text-shadow: 0 0 16px rgba(111, 233, 255, 0.6);
}

.gr-acts {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 20px;
}

/* ================= 响应式 ================= */
@media (max-width: 860px) {
  .cv-games {
    grid-template-columns: minmax(0, 1fr);
  }
  .cg-desc {
    min-height: 0;
  }
}

@media (max-width: 620px) {
  .cultivate {
    padding: 22px 16px 44px;
  }
  .cv-head {
    flex-wrap: wrap;
    gap: 12px;
  }
  .cv-title h2 {
    font-size: 24px;
  }
  .cv-status {
    flex-wrap: wrap;
    gap: 16px;
    padding: 16px 18px;
  }
  .st-exp {
    padding-left: 0;
    border-left: none;
    text-align: left;
  }
  .g-wrap {
    padding: 18px 16px 20px;
  }
  .g-hud {
    gap: 14px;
  }
  .rune-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .rune-name {
    font-size: 19px;
  }
}
</style>
