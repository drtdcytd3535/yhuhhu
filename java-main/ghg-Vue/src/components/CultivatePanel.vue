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
        六门法门，可得修为上限 {{ TRAIN_LIMIT }} 点，跨日自新。修为入洞府账中，境界随之精进。
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

      <!-- ---------- 御剑渡劫 ---------- -->
      <div v-else-if="game === 'thunder'" class="g-wrap">
        <div class="g-hud">
          <span>余时 <b>{{ thuLeft }}</b> 息</span>
          <span>避雷 <b>{{ thu.dodge }}</b></span>
          <span>罡气 <b>{{ thu.shield }}</b></span>
          <span>所得 <b>+{{ thu.gained }}</b></span>
        </div>

        <div class="thu-stage" :class="{ struck: thu.flash > 0 }">
          <canvas
            ref="thuCanvas"
            class="thu-canvas"
            @pointermove="onThuMove"
            @pointerdown="onThuMove"
          ></canvas>
          <p v-if="!thu.running && !result" class="thu-mask">凝剑于下 · 待雷起</p>
        </div>

        <div class="g-acts">
          <button v-if="!thu.running && !result" class="g-btn" @click="startThu">御剑 · 迎接天劫</button>
          <p v-if="thu.running" class="g-tip">移动鼠标或按 ← → / A D 御剑 · 雷现红光即为预警</p>
        </div>
      </div>

      <!-- ---------- 灵脉采石 ---------- -->
      <div v-else-if="game === 'mining'" class="g-wrap">
        <div class="g-hud">
          <span>余时 <b>{{ mineLeft }}</b> 息</span>
          <span>凿中 <b>{{ mine.hits }}</b></span>
          <span>矿心 <b>{{ mine.perfect }}</b></span>
          <span>所得 <b>+{{ mine.gained }}</b> / 灵石 {{ mine.stones }}</span>
        </div>

        <div class="mine-bar" @pointerdown="strikeMine">
          <i
            v-for="n in mine.nodes"
            :key="n.id"
            class="mine-node"
            :style="{ left: n.x + '%', width: n.w + '%' }"
          >
            <i class="mine-core" :style="{ left: '50%', width: n.core + '%' }"></i>
          </i>
          <i class="mine-cursor" :style="{ left: mine.pos + '%' }"></i>
        </div>

        <ul v-if="mine.log.length" class="mine-log">
          <li v-for="(r, i) in mine.log" :key="i" :class="r.cls">{{ r.text }}</li>
        </ul>

        <div class="g-acts">
          <button v-if="!mine.playing && !result" class="g-btn" @click="startMine">入脉 · 开始采石</button>
          <button v-else-if="mine.playing" class="g-btn" @click="strikeMine">凿 · 落镐</button>
          <p v-if="mine.playing" class="g-tip">点击灵脉条或按空格落镐 · 正中矿心可得双份</p>
        </div>
      </div>

      <!-- ---------- 问心之劫 ---------- -->
      <div v-else-if="game === 'heart'" class="g-wrap">
        <div class="g-hud">
          <span>第 <b>{{ heart.round }}</b> / {{ HEART_ROUNDS }} 念</span>
          <span>心神 <b>{{ heart.mind }}</b></span>
          <span>连辨 <b>x{{ heart.combo }}</b></span>
          <span>所得 <b>+{{ heart.gained }}</b></span>
        </div>

        <div class="heart-stage">
          <div
            class="heart-face"
            :class="[
              heart.phase === 'show' ? (heart.kind === 'demon' ? 'demon' : 'light') : 'idle',
              { judged: heart.phase === 'wait' && heart.mark }
            ]"
          >
            <span v-if="heart.phase === 'show'">{{ heart.kind === 'demon' ? '魇' : '道' }}</span>
            <span v-else-if="heart.mark">{{ heart.mark }}</span>
            <span v-else>心</span>
          </div>
          <div class="heart-timer">
            <i :style="{ width: heart.bar + '%' }"></i>
          </div>
          <p class="heart-hint">{{ heart.hint }}</p>
        </div>

        <div class="heart-acts">
          <button class="h-btn cut" :disabled="heart.phase !== 'show'" @click="judgeHeart('cut')">
            斩 · 心魔
          </button>
          <button class="h-btn take" :disabled="heart.phase !== 'show'" @click="judgeHeart('take')">
            纳 · 灵光
          </button>
        </div>

        <div class="g-acts">
          <button v-if="!heart.playing && !result" class="g-btn" @click="startHeart">入定 · 问心开始</button>
          <p v-if="heart.playing" class="g-tip">现「魇」即斩 · 现「道」即纳 · 迟疑则心神受损</p>
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
        <p v-if="result.stones" class="gr-stone">灵石 +{{ result.stones }} · 已入洞府账中</p>
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
import { ref, reactive, computed, onBeforeUnmount, onMounted, nextTick } from 'vue'
import RealmTable from './RealmTable.vue'
import {
  cave,
  realm,
  realmProgress,
  nextRealm,
  addTrainExp,
  addStones,
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
  },
  {
    key: 'thunder',
    ico: '⚡',
    name: '御剑渡劫',
    sub: '雷落九霄 · 剑走偏锋',
    desc: '天雷自九霄劈下，御剑左右腾挪，二十息内避雷愈多，护体罡气愈不可失。',
    expText: '每避一雷 +6 起（连避加成）'
  },
  {
    key: 'mining',
    ico: '⛏',
    name: '灵脉采石',
    sub: '凿山取髓 · 石中有灵',
    desc: '灵脉游走不定，凿于脉眼之上可得灵石，正中矿心者所获更丰。',
    expText: '脉眼 +8 修为 +1 灵石 · 矿心 +12 修为 +2 灵石'
  },
  {
    key: 'heart',
    ico: '☾',
    name: '问心之劫',
    sub: '心魔与道 · 一念之别',
    desc: '心魔与灵光相继浮现，须于一瞬之间分辨斩纳；心神耗尽则劫散。',
    expText: '每辨一念 +5 起（连辨加成）'
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
 * 御剑渡劫：二十息内御剑避雷，罡气耗尽则劫散
 * ============================================================= */
const THU_TIME = 20
/** 剑光半宽（归一化），与雷柱半宽相加即命中判定 */
const THU_HALF = 0.07

const thuCanvas = ref(null)
let thuCtx = null
let thuW = 0
let thuH = 0
let keyL = false
let keyR = false

const thu = reactive({
  running: false,
  left: THU_TIME,
  shield: 3,
  dodge: 0,
  combo: 0,
  gained: 0,
  flash: 0,
  px: 0.5,
  bolts: [],
  spawnT: 0,
  t: 0,
  raf: 0,
  last: 0
})

/** 余息取整显示（内部仍按浮点推进） */
const thuLeft = computed(() => Math.ceil(thu.left))
const mineLeft = computed(() => Math.ceil(mine.left))

function thuResize() {
  const cv = thuCanvas.value
  if (!cv) return
  const dpr = Math.min(2, window.devicePixelRatio || 1)
  const r = cv.getBoundingClientRect()
  thuW = r.width || 520
  thuH = r.height || 300
  cv.width = Math.round(thuW * dpr)
  cv.height = Math.round(thuH * dpr)
  thuCtx = cv.getContext('2d')
  if (thuCtx) thuCtx.setTransform(dpr, 0, 0, dpr, 0, 0)
}

function resetThu() {
  thu.running = false
  thu.left = THU_TIME
  thu.shield = 3
  thu.dodge = 0
  thu.combo = 0
  thu.gained = 0
  thu.flash = 0
  thu.px = 0.5
  thu.bolts = []
  thu.spawnT = 0.8
  thu.t = 0
  if (thu.raf) cancelAnimationFrame(thu.raf)
  thu.raf = 0
}

async function startThu() {
  resetThu()
  result.value = null
  thu.running = true
  thu.last = 0
  await nextTick()
  thuResize()
  drawThu()
  thu.raf = requestAnimationFrame(loopThu)
}

function spawnBolt() {
  const prog = 1 - thu.left / THU_TIME
  thu.bolts.push({
    x: 0.06 + Math.random() * 0.88,
    w: 0.05 + Math.random() * 0.03,
    state: 'warn',
    warn: Math.max(0.42, 0.85 - prog * 0.35),
    life: 0.32,
    done: false,
    dead: false
  })
}

function loopThu(ts) {
  if (!thu.running) return
  if (!thu.last) thu.last = ts
  const dt = Math.min(50, ts - thu.last) / 1000
  thu.last = ts
  thu.t += dt

  /* 御剑：键盘 ← → / A D */
  if (keyL) thu.px -= dt * 0.8
  if (keyR) thu.px += dt * 0.8
  thu.px = Math.max(0.03, Math.min(0.97, thu.px))

  /* 落雷 */
  thu.spawnT -= dt
  if (thu.spawnT <= 0) {
    spawnBolt()
    thu.spawnT = 0.55 + Math.random() * 0.45
  }
  for (const b of thu.bolts) {
    if (b.state === 'warn') {
      b.warn -= dt
      if (b.warn <= 0) {
        b.state = 'strike'
        b.life = 0.32
      }
    } else if (b.state === 'strike') {
      b.life -= dt
      if (!b.done) {
        b.done = true
        if (Math.abs(b.x - thu.px) < THU_HALF + b.w / 2) hitThu()
        else dodgeThu()
      }
      if (b.life <= 0) b.dead = true
    }
  }
  thu.bolts = thu.bolts.filter((b) => !b.dead)
  if (thu.flash > 0) thu.flash = Math.max(0, thu.flash - dt * 2)

  thu.left -= dt
  if (thu.shield <= 0) {
    thu.left = Math.max(0, thu.left)
    endThu('罡气散尽')
    return
  }
  if (thu.left <= 0) {
    thu.left = 0
    endThu('天劫已渡')
    return
  }

  drawThu()
  thu.raf = requestAnimationFrame(loopThu)
}

function hitThu() {
  thu.shield -= 1
  thu.combo = 0
  thu.flash = 1
  beep(150, 0.3, 'sawtooth', 0.04)
}

function dodgeThu() {
  thu.dodge += 1
  thu.combo += 1
  thu.gained += 6 + Math.min(4, Math.floor(thu.combo / 4))
  beep(700 + Math.min(6, thu.combo) * 40, 0.06, 'sine', 0.02)
}

function endThu(why) {
  thu.running = false
  if (thu.raf) cancelAnimationFrame(thu.raf)
  thu.raf = 0
  drawThu()
  settle(thu.gained, `${why} · 避雷 ${thu.dodge} 道 · 余罡气 ${thu.shield}`)
}

function drawThu() {
  const ctx = thuCtx
  if (!ctx || !thuW) return
  ctx.clearRect(0, 0, thuW, thuH)

  const sky = ctx.createLinearGradient(0, 0, 0, thuH)
  sky.addColorStop(0, 'rgba(30, 22, 62, 0.92)')
  sky.addColorStop(1, 'rgba(6, 7, 20, 0.96)')
  ctx.fillStyle = sky
  ctx.fillRect(0, 0, thuW, thuH)

  /* 云气：两道极淡的横向光带 */
  ctx.fillStyle = 'rgba(120, 150, 220, 0.05)'
  ctx.fillRect(0, thuH * 0.28, thuW, 2)
  ctx.fillRect(0, thuH * 0.56, thuW, 2)

  for (const b of thu.bolts) {
    const x = b.x * thuW
    if (b.state === 'warn') {
      const a = 0.22 + 0.26 * Math.abs(Math.sin(thu.t * 14))
      ctx.strokeStyle = `rgba(255, 124, 112, ${a})`
      ctx.lineWidth = 2
      ctx.setLineDash([6, 9])
      ctx.beginPath()
      ctx.moveTo(x, 0)
      ctx.lineTo(x, thuH * 0.9)
      ctx.stroke()
      ctx.setLineDash([])
      /* 预警箭标 */
      ctx.fillStyle = `rgba(255, 150, 130, ${a + 0.2})`
      ctx.beginPath()
      ctx.moveTo(x, thuH * 0.06)
      ctx.lineTo(x - 6, thuH * 0.02)
      ctx.lineTo(x + 6, thuH * 0.02)
      ctx.closePath()
      ctx.fill()
    } else {
      const a = Math.max(0, b.life / 0.32)
      const lg = ctx.createLinearGradient(x - 10, 0, x + 10, 0)
      lg.addColorStop(0, 'rgba(180, 220, 255, 0)')
      lg.addColorStop(0.5, `rgba(226, 242, 255, ${a})`)
      lg.addColorStop(1, 'rgba(180, 220, 255, 0)')
      ctx.fillStyle = lg
      ctx.fillRect(x - 10, 0, 20, thuH * 0.92)
      ctx.fillStyle = `rgba(255, 255, 255, ${a * 0.9})`
      ctx.fillRect(x - 1.5, 0, 3, thuH * 0.92)
    }
  }

  /* 剑光 */
  const px = thu.px * thuW
  const py = thuH * 0.88
  ctx.save()
  ctx.shadowBlur = 20
  ctx.shadowColor = thu.flash > 0 ? 'rgba(255, 120, 110, 0.95)' : 'rgba(111, 233, 255, 0.9)'
  ctx.fillStyle = thu.flash > 0 ? 'rgba(255, 168, 156, 0.96)' : 'rgba(196, 246, 255, 0.96)'
  ctx.beginPath()
  ctx.moveTo(px, py - 14)
  ctx.lineTo(px + 10, py + 7)
  ctx.lineTo(px, py + 2)
  ctx.lineTo(px - 10, py + 7)
  ctx.closePath()
  ctx.fill()
  ctx.restore()

  /* 护体罡气：底部三枚光点 */
  for (let i = 0; i < 3; i++) {
    ctx.fillStyle = i < thu.shield ? 'rgba(232, 198, 107, 0.85)' : 'rgba(233, 238, 251, 0.14)'
    ctx.beginPath()
    ctx.arc(16 + i * 16, thuH - 14, 4, 0, Math.PI * 2)
    ctx.fill()
  }
}

function onThuMove(e) {
  const cv = thuCanvas.value
  if (!cv) return
  const r = cv.getBoundingClientRect()
  if (!r.width) return
  thu.px = Math.max(0.03, Math.min(0.97, (e.clientX - r.left) / r.width))
}

/* =============================================================
 * 灵脉采石：光标游走，凿于脉眼得灵石，正中矿心加倍
 * ============================================================= */
const MINE_TIME = 20

const mine = reactive({
  playing: false,
  left: MINE_TIME,
  pos: 0,
  dir: 1,
  speed: 52,
  nodes: [],
  nid: 0,
  hits: 0,
  perfect: 0,
  gained: 0,
  stones: 0,
  log: [],
  raf: 0,
  last: 0
})

function makeNode() {
  const w = 9 + Math.random() * 5
  return { id: ++mine.nid, x: 5 + Math.random() * (90 - w), w, core: 34 }
}

function resetMine() {
  stopMine()
  mine.left = MINE_TIME
  mine.pos = 0
  mine.dir = 1
  mine.speed = 52
  mine.hits = 0
  mine.perfect = 0
  mine.gained = 0
  mine.stones = 0
  mine.log = []
  mine.nodes = [makeNode(), makeNode(), makeNode()]
}

function startMine() {
  resetMine()
  result.value = null
  mine.playing = true
  mine.last = 0
  mine.raf = requestAnimationFrame(loopMine)
}

function loopMine(ts) {
  if (!mine.playing) return
  if (!mine.last) mine.last = ts
  const dt = Math.min(50, ts - mine.last) / 1000
  mine.last = ts
  mine.pos += mine.dir * mine.speed * dt
  if (mine.pos >= 100) {
    mine.pos = 100
    mine.dir = -1
  } else if (mine.pos <= 0) {
    mine.pos = 0
    mine.dir = 1
  }
  const prev = Math.ceil(mine.left)
  mine.left -= dt
  if (Math.ceil(mine.left) !== prev) mine.speed += 5
  if (mine.left <= 0) {
    mine.left = 0
    endMine()
    return
  }
  mine.raf = requestAnimationFrame(loopMine)
}

function strikeMine() {
  if (!mine.playing) return
  const p = mine.pos
  let idx = -1
  let isCore = false
  mine.nodes.forEach((n, i) => {
    if (p >= n.x && p <= n.x + n.w) {
      idx = i
      const half = (n.w * n.core) / 200
      isCore = Math.abs(p - (n.x + n.w / 2)) <= half
    }
  })

  if (idx < 0) {
    mine.log.unshift({ text: '凿空 · 灵脉未应', cls: 'miss' })
    if (mine.log.length > 4) mine.log.pop()
    beep(220, 0.1, 'sine', 0.03)
    return
  }

  const exp = isCore ? 12 : 8
  const st = isCore ? 2 : 1
  mine.gained += exp
  mine.stones += st
  mine.hits += 1
  if (isCore) mine.perfect += 1
  mine.log.unshift({
    text: `第 ${mine.hits} 镐 · ${isCore ? '正中矿心' : '凿中脉眼'}（修为 +${exp} / 灵石 +${st}）`,
    cls: isCore ? 'core' : 'ok'
  })
  if (mine.log.length > 4) mine.log.pop()
  beep(isCore ? 900 : 660, 0.12, 'sine', 0.035)
  mine.nodes.splice(idx, 1)
  mine.nodes.push(makeNode())
}

function endMine() {
  mine.playing = false
  if (mine.raf) cancelAnimationFrame(mine.raf)
  mine.raf = 0
  const stones = addStones(mine.stones)
  settle(mine.gained, `凿脉 ${mine.hits} 镐 · 矿心 ${mine.perfect} 次 · 得灵石 ${stones}`)
  if (result.value) result.value.stones = stones
}

function stopMine() {
  mine.playing = false
  if (mine.raf) cancelAnimationFrame(mine.raf)
  mine.raf = 0
}

/* =============================================================
 * 问心之劫：心魔当斩、灵光当纳，一念之差心神受损
 * ============================================================= */
const HEART_ROUNDS = 12

const heart = reactive({
  playing: false,
  phase: 'idle', // idle | show | wait | done
  round: 1,
  kind: '',
  mind: 3,
  combo: 0,
  hits: 0,
  gained: 0,
  bar: 0,
  mark: '',
  hint: '入定静心 · 魔与道皆在一念',
  limit: 0.9,
  raf: 0,
  last: 0
})

function startHeart() {
  stopHeart()
  result.value = null
  heart.round = 1
  heart.mind = 3
  heart.combo = 0
  heart.hits = 0
  heart.gained = 0
  heart.bar = 0
  heart.mark = ''
  heart.playing = true
  nextHeartRound()
}

function nextHeartRound() {
  heart.kind = Math.random() < 0.5 ? 'demon' : 'light'
  heart.limit = Math.max(0.52, 0.95 - (heart.round - 1) * 0.04)
  heart.bar = 100
  heart.mark = ''
  heart.phase = 'show'
  heart.hint = heart.kind === 'demon' ? '心魔现形 · 当斩' : '灵光现形 · 当纳'
  heart.last = 0
  beep(heart.kind === 'demon' ? 250 : 620, 0.1, 'sine', 0.03)
  if (heart.raf) cancelAnimationFrame(heart.raf)
  heart.raf = requestAnimationFrame(loopHeart)
}

function loopHeart(ts) {
  if (heart.phase !== 'show') return
  if (!heart.last) heart.last = ts
  const dt = Math.min(50, ts - heart.last) / 1000
  heart.last = ts
  heart.bar = Math.max(0, heart.bar - (dt / heart.limit) * 100)
  if (heart.bar <= 0) {
    heart.raf = 0
    heartWrong('迟疑')
    return
  }
  heart.raf = requestAnimationFrame(loopHeart)
}

function judgeHeart(act) {
  if (heart.phase !== 'show') return
  if (heart.raf) cancelAnimationFrame(heart.raf)
  heart.raf = 0
  const right =
    (heart.kind === 'demon' && act === 'cut') || (heart.kind === 'light' && act === 'take')
  if (right) heartRight()
  else heartWrong('错判')
}

function heartRight() {
  heart.combo += 1
  heart.hits += 1
  heart.gained += 5 + Math.min(6, Math.floor(heart.combo / 3) * 2)
  heart.mark = '✓'
  heart.phase = 'wait'
  heart.hint = '心念分明 · 此念已破'
  beep(740 + Math.min(8, heart.combo) * 30, 0.14, 'sine', 0.04)
  pending.push(setTimeout(afterHeartRound, 620))
}

function heartWrong(why) {
  heart.mind -= 1
  heart.combo = 0
  heart.mark = '✕'
  heart.phase = 'wait'
  heart.hint = `${why} · 心神受损`
  beep(150, 0.26, 'sawtooth', 0.035)
  if (heart.mind <= 0) {
    pending.push(setTimeout(() => endHeart(`心神耗尽 · 破 ${heart.hits} 念`), 720))
    return
  }
  pending.push(setTimeout(afterHeartRound, 780))
}

function afterHeartRound() {
  if (!heart.playing) return
  if (heart.round >= HEART_ROUNDS) {
    endHeart('十二念尽 · 问心功成')
    return
  }
  heart.round += 1
  nextHeartRound()
}

function endHeart(detail) {
  heart.playing = false
  heart.phase = 'done'
  if (heart.raf) cancelAnimationFrame(heart.raf)
  heart.raf = 0
  settle(heart.gained, detail)
}

function stopHeart() {
  heart.playing = false
  heart.phase = 'idle'
  if (heart.raf) cancelAnimationFrame(heart.raf)
  heart.raf = 0
}

/* =============================================================
 * 结算与切换
 * ============================================================= */
function settle(rawExp, detail) {
  const r = addTrainExp(rawExp)
  result.value = {
    detail,
    exp: r.exp,
    stones: 0,
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
  stopThu()
  stopMine()
  stopHeart()
  pending.forEach((t) => clearTimeout(t))
  pending.length = 0
}

function stopThu() {
  thu.running = false
  if (thu.raf) cancelAnimationFrame(thu.raf)
  thu.raf = 0
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
  if (key === 'thunder') resetThu()
  if (key === 'mining') resetMine()
  if (key === 'heart') {
    stopHeart()
    heart.round = 1
    heart.mind = 3
    heart.combo = 0
    heart.hits = 0
    heart.gained = 0
    heart.bar = 0
    heart.mark = ''
    heart.hint = '入定静心 · 魔与道皆在一念'
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

/* ---------------- 键盘：御剑左右 / 采石空格 ---------------- */
function onKeyDown(e) {
  const k = e.key
  if (game.value === 'thunder') {
    if (k === 'ArrowLeft' || k === 'a' || k === 'A') {
      keyL = true
      e.preventDefault()
    } else if (k === 'ArrowRight' || k === 'd' || k === 'D') {
      keyR = true
      e.preventDefault()
    }
    return
  }
  if (game.value === 'mining' && k === ' ') {
    // 焦点若在按钮上，浏览器会自行触发 click，此处不再重复落镐
    if (e.target && e.target.tagName === 'BUTTON') return
    strikeMine()
    e.preventDefault()
  }
}

function onKeyUp(e) {
  const k = e.key
  if (k === 'ArrowLeft' || k === 'a' || k === 'A') keyL = false
  if (k === 'ArrowRight' || k === 'd' || k === 'D') keyR = false
}

function onWinResize() {
  if (game.value === 'thunder') {
    thuResize()
    drawThu()
  }
}

onMounted(() => {
  window.addEventListener('keydown', onKeyDown)
  window.addEventListener('keyup', onKeyUp)
  window.addEventListener('resize', onWinResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeyDown)
  window.removeEventListener('keyup', onKeyUp)
  window.removeEventListener('resize', onWinResize)
  stopAll()
})
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

/* ---------- 御剑渡劫 ---------- */
.thu-stage {
  position: relative;
  max-width: 560px;
  margin: 0 auto;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid rgba(111, 233, 255, 0.2);
  box-shadow: inset 0 0 40px rgba(58, 42, 107, 0.4);
  transition: 0.12s;
}

.thu-stage.struck {
  border-color: rgba(255, 130, 110, 0.8);
  box-shadow:
    inset 0 0 40px rgba(150, 46, 40, 0.5),
    0 0 26px rgba(255, 110, 90, 0.4);
}

.thu-canvas {
  display: block;
  width: 100%;
  height: 300px;
  touch-action: none;
  cursor: none;
}

.thu-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  letter-spacing: 5px;
  color: rgba(233, 238, 251, 0.4);
  background: rgba(8, 9, 22, 0.45);
}

/* ---------- 灵脉采石 ---------- */
.mine-bar {
  position: relative;
  height: 62px;
  max-width: 560px;
  margin: 0 auto;
  border-radius: 12px;
  border: 1px solid rgba(111, 233, 255, 0.2);
  background: linear-gradient(180deg, rgba(20, 22, 46, 0.8), rgba(8, 9, 22, 0.9));
  cursor: pointer;
  overflow: hidden;
  touch-action: none;
}

.mine-node {
  position: absolute;
  top: 12px;
  bottom: 12px;
  /* 左边界即 n.x，与命中判定 [n.x, n.x + w] 对齐（勿加 translateX） */
  border-radius: 6px;
  background: rgba(111, 233, 255, 0.16);
  border: 1px solid rgba(111, 233, 255, 0.5);
  box-shadow: 0 0 14px rgba(111, 233, 255, 0.24);
}

.mine-core {
  position: absolute;
  top: -2px;
  bottom: -2px;
  transform: translateX(-50%);
  border-radius: 4px;
  background: rgba(232, 198, 107, 0.34);
  box-shadow: 0 0 16px rgba(232, 198, 107, 0.45);
}

.mine-cursor {
  position: absolute;
  top: -4px;
  bottom: -4px;
  width: 3px;
  margin-left: -1.5px;
  border-radius: 2px;
  background: linear-gradient(180deg, #fff, var(--cyan));
  box-shadow: 0 0 16px rgba(111, 233, 255, 0.9);
}

.mine-log {
  max-width: 560px;
  margin: 16px auto 0;
  padding: 0;
  list-style: none;
  font-size: 12.5px;
  letter-spacing: 1.5px;
  color: rgba(233, 238, 251, 0.55);
}

.mine-log li {
  padding: 5px 0;
  border-bottom: 1px dashed rgba(160, 190, 230, 0.12);
}

.mine-log li.ok {
  color: rgba(111, 233, 255, 0.85);
}

.mine-log li.core {
  color: var(--gold);
}

.mine-log li.miss {
  color: rgba(255, 160, 140, 0.8);
}

/* ---------- 问心之劫 ---------- */
.heart-stage {
  max-width: 380px;
  margin: 0 auto;
  text-align: center;
}

.heart-face {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 168px;
  height: 168px;
  margin: 0 auto;
  border-radius: 50%;
  font-family: 'Ma Shan Zheng', serif;
  font-size: 62px;
  color: rgba(233, 238, 251, 0.5);
  border: 1px solid rgba(160, 190, 230, 0.24);
  background: radial-gradient(circle at 50% 45%, rgba(58, 42, 107, 0.4), rgba(8, 9, 22, 0.85));
  transition: 0.2s;
}

.heart-face.demon {
  color: #ffb3a6;
  border-color: rgba(255, 130, 110, 0.85);
  background: radial-gradient(circle at 50% 45%, rgba(150, 46, 40, 0.6), rgba(24, 8, 10, 0.92));
  box-shadow:
    0 0 40px rgba(255, 110, 90, 0.45),
    inset 0 0 30px rgba(255, 120, 90, 0.24);
  animation: heartIn 0.18s ease both;
}

.heart-face.light {
  color: #fff4cf;
  border-color: rgba(232, 198, 107, 0.85);
  background: radial-gradient(circle at 50% 45%, rgba(120, 96, 40, 0.5), rgba(12, 10, 26, 0.92));
  box-shadow:
    0 0 40px rgba(232, 198, 107, 0.45),
    inset 0 0 30px rgba(232, 198, 107, 0.22);
  animation: heartIn 0.18s ease both;
}

.heart-face.judged {
  font-size: 54px;
}

@keyframes heartIn {
  from {
    opacity: 0;
    transform: scale(0.86);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.heart-timer {
  height: 5px;
  margin: 20px 0 12px;
  border-radius: 3px;
  background: rgba(160, 190, 230, 0.14);
  overflow: hidden;
}

.heart-timer i {
  display: block;
  height: 100%;
  border-radius: 3px;
  background: linear-gradient(90deg, var(--gold), var(--cyan));
  box-shadow: 0 0 12px rgba(111, 233, 255, 0.5);
}

.heart-hint {
  font-size: 13px;
  letter-spacing: 3px;
  color: rgba(233, 238, 251, 0.55);
}

.heart-acts {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 22px;
}

.h-btn {
  min-width: 132px;
  padding: 12px 18px;
  border-radius: 12px;
  font-family: inherit;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 3px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: 0.2s;
}

.h-btn.cut {
  color: #ffd9d2;
  border-color: rgba(255, 130, 110, 0.55);
  background: rgba(255, 110, 90, 0.14);
}

.h-btn.cut:hover:not(:disabled) {
  background: rgba(255, 110, 90, 0.26);
  box-shadow: 0 0 20px rgba(255, 110, 90, 0.4);
}

.h-btn.take {
  color: #fff4cf;
  border-color: rgba(232, 198, 107, 0.55);
  background: rgba(232, 198, 107, 0.14);
}

.h-btn.take:hover:not(:disabled) {
  background: rgba(232, 198, 107, 0.26);
  box-shadow: 0 0 20px rgba(232, 198, 107, 0.4);
}

.h-btn:disabled {
  opacity: 0.4;
  cursor: wait;
}

/* ---------- 结算 ---------- */
.gr-stone {
  margin-top: 10px;
  font-size: 13px;
  letter-spacing: 2px;
  color: var(--cyan);
  text-shadow: 0 0 14px rgba(111, 233, 255, 0.5);
}

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
