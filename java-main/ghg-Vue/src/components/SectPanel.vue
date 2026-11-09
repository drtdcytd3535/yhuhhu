<template>
  <section class="sect">
    <!-- ================= 名册总览 ================= -->
    <template v-if="!battle.open">
      <div class="sect-head">
        <div class="sh-left">
          <span class="sh-badge">宗门挑战</span>
          <h3>云岚宗 · 十三人试</h3>
          <p>
            由弱到强，自外门弟子起逐位挑战，宗主压轴（修为最高）。须打完一位，方可请下一位；
            出手只两手——<b>防御可挡下一切攻击，不问修为</b>；唯双方皆攻，才比修为，高者伤害更重。
          </p>
        </div>
        <div class="sh-right">
          <p class="sh-progress">已败 <b>{{ count }}</b> / {{ SECT_SIZE }}</p>
          <p class="sh-mine">修为 <b>{{ myLabel }}</b></p>
          <p class="sh-mine">气血 <b>{{ myHp }}</b></p>
          <p class="sh-save">{{ saveHint }}</p>
        </div>
      </div>

      <ol class="sect-list">
        <li
          v-for="row in rows"
          :key="row.id"
          class="sect-row"
          :class="{
            cleared: row.defeated,
            locked: !row.unlocked,
            current: row.unlocked && !row.defeated
          }"
        >
          <span class="sr-no">{{ String(row.index + 1).padStart(2, '0') }}</span>

          <div class="sr-who">
            <p class="sr-name">
              {{ row.name }}
              <span class="sr-title">{{ row.title }}</span>
            </p>
            <p class="sr-line">{{ row.line }}</p>
          </div>

          <div class="sr-realm">
            <p class="sr-realm-name">{{ row.label }}</p>
            <p class="sr-exp">修为 {{ row.exp }}</p>
          </div>

          <div class="sr-power">
            <p class="sr-power-num">气血 {{ row.hp }}</p>
            <p class="sr-power-sub">我方修为占 {{ row.share }}%</p>
          </div>

          <div class="sr-act">
            <span v-if="row.defeated" class="sr-done">已败</span>
            <button v-else-if="row.unlocked" class="sr-go" @click="openBattle(row.index)">
              挑战 →
            </button>
            <span v-else class="sr-lock">未解锁</span>
          </div>
        </li>
      </ol>

      <div class="sect-foot">
        <p>
          首通各得灵石之赏，重战只论高下不再给赏。防御能挡下任何来击，但唯有双方皆攻才分高下；
          修为愈高，对攻时一击愈重，宜于上方法门中勤修。
        </p>
        <button v-if="count > 0" class="sect-reset" @click="onReset">重设名册</button>
      </div>
    </template>

    <!-- ================= 斗法场 ================= -->
    <div v-else class="arena">
      <div class="ar-head">
        <button class="ar-back" @click="closeBattle">← 回名册</button>
        <p class="ar-title">
          云岚宗 · 第 {{ battle.state.index + 1 }} 位
          <span class="ar-title-name">{{ foe.name }}</span>
        </p>
        <span class="ar-tag">{{ foe.title }}</span>
        <span class="ar-round">第 {{ battle.state.round }} / {{ battle.state.maxRound }} 回合</span>
      </div>

      <!-- 修为对比：只在双方皆攻时起作用 -->
      <p class="ar-sup">
        <span :class="edgeClass">修为占比 我 {{ myShare }}% · 敌 {{ 100 - myShare }}%</span>
        <span class="ar-sup-note">{{ supNote }}</span>
        <span class="ar-first">对方出手随机</span>
      </p>

      <div class="ar-ring" :class="{ fighting: !battle.state.over && battle.state.round > 0 }">
        <!-- 我方 -->
        <div class="ar-side mine">
          <p class="ar-side-tag">我方 · {{ props.username }}</p>
          <p class="ar-realm">{{ myLabel }}</p>

          <div class="hp-bar">
            <i :style="{ width: myHpPct + '%' }" :class="{ low: myHpPct <= 30 }"></i>
          </div>
          <p class="hp-text">
            气血 <b>{{ me.hp }}</b> / {{ me.maxHp }}
          </p>

          <p class="ar-power">修为 {{ me.exp }}</p>
          <p class="ar-move">
            本回合出手 · <b :class="me.move">{{ moveName(me.move) }}</b>
          </p>

          <p v-if="lastHit.me" class="hit-num me-hit" :key="hitKey.me">-{{ lastHit.me }}</p>
        </div>

        <div class="ar-vs">
          <span class="vs-mark" :class="{ clash: !battle.state.over && battle.state.round > 0 }">
            {{ battle.state.over ? (battle.state.win ? '胜' : battle.state.draw ? '平' : '败') : 'VS' }}
          </span>
          <p class="vs-tip">
            {{ battle.state.over ? '斗法已毕' : battle.state.round ? roundTip : '待你出手' }}
          </p>
        </div>

        <!-- 对方 -->
        <div class="ar-side foe">
          <p class="ar-side-tag">{{ SECT_NAME }} · {{ foe.name }}</p>
          <p class="ar-realm">{{ realmLabel(foe.exp) }}</p>

          <div class="hp-bar foe-bar">
            <i :style="{ width: foeHpPct + '%' }" :class="{ low: foeHpPct <= 30 }"></i>
          </div>
          <p class="hp-text">
            气血 <b>{{ foeHp.hp }}</b> / {{ foeHp.maxHp }}
          </p>

          <p class="ar-power">修为 {{ foe.exp }}</p>
          <p class="ar-move">
            本回合出手 · <b :class="foe.move">{{ moveName(foe.move) }}</b>
          </p>

          <p v-if="lastHit.foe" class="hit-num foe-hit" :key="hitKey.foe">-{{ lastHit.foe }}</p>
        </div>
      </div>

      <!-- 招式：只两手 -->
      <div v-if="!battle.state.over" class="ar-tactics">
        <button
          v-for="t in MOVES"
          :key="t.key"
          class="tac"
          :class="t.key"
          @click="choose(t.key)"
        >
          <span class="tac-ico">{{ t.ico }}</span>
          <span class="tac-name">{{ t.name }}</span>
          <span class="tac-desc">{{ t.desc }}</span>
        </button>
        <button class="tac-auto" @click="autoFight">速战 · 自动出手到底</button>
      </div>

      <!-- 战报 -->
      <ul v-if="battle.state.log.length" class="duel-log">
        <li v-for="l in recentLog" :key="l.round" :class="{ win: l.win === true, lose: l.win === false }">
          <span class="dl-round">{{ l.round }}</span>
          <span class="dl-line">{{ l.line }}</span>
        </li>
      </ul>

      <!-- 结果 -->
      <div v-if="battle.state.over" class="ar-result" :class="resultClass">
        <p class="rs-title">{{ resultTitle }}</p>
        <p class="rs-reason">{{ resultReason }}</p>

        <div class="rs-compare">
          <span>回合 {{ battle.state.round }}</span>
          <span>残余气血 {{ me.hp }} : {{ foeHp.hp }}</span>
          <span>修为 {{ me.exp }} : {{ foe.exp }}</span>
        </div>

        <p v-if="battle.settle && battle.settle.reward" class="rs-reward">
          首通之赏 · 灵石 +{{ battle.settle.reward }}
        </p>
        <p v-else-if="battle.settle && battle.settle.win" class="rs-reward muted">
          首通之赏已领，此番只论高下
        </p>

        <div class="rs-acts">
          <button
            v-if="battle.settle && battle.settle.win && nextIndex >= 0"
            class="ar-btn"
            @click="openBattle(nextIndex)"
          >
            请下一位 · {{ SECT_MEMBERS[nextIndex].name }} →
          </button>
          <button
            v-if="battle.settle && !battle.settle.win"
            class="ar-btn"
            @click="openBattle(battle.state.index)"
          >
            再战一场
          </button>
          <button class="ar-btn ghost" @click="closeBattle">
            {{ battle.settle && battle.settle.win && nextIndex < 0 ? '名册已尽 · 回名册' : '回名册' }}
          </button>
        </div>

        <p v-if="battle.settle && !battle.settle.win" class="rs-advice">{{ advice }}</p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, reactive, onBeforeUnmount, watch } from 'vue'
import { cave } from '../composables/useCave.js'
import {
  SECT_NAME,
  SECT_SIZE,
  SECT_MEMBERS,
  MOVES,
  sect,
  initSect,
  resetSect,
  isDefeated,
  isUnlocked,
  clearedCount,
  realmLabel,
  memberHp,
  playerStats,
  startDuel,
  duelRound,
  settleDuel,
  expShare
} from '../composables/useSect.js'

const props = defineProps({
  username: { type: String, default: '道友' }
})
const emit = defineEmits(['toast'])

/* ---------- 名册 ---------- */
const rows = computed(() =>
  SECT_MEMBERS.map((m, i) => ({
    ...m,
    index: i,
    hp: memberHp(m),
    share: Math.round(expShare(cave.exp, m.exp) * 100),
    label: realmLabel(m.exp),
    defeated: isDefeated(m),
    unlocked: isUnlocked(i)
  }))
)

const count = computed(() => clearedCount())
const myLabel = computed(() => realmLabel(cave.exp))
const myHp = computed(() => playerStats().hp)

/** 战绩存档状态：云端 > 本地 */
const saveHint = computed(() => {
  if (sect.synced) return '战绩已存云端 · 换设备也在'
  if (sect.cleared.length) return '战绩已存本地 · 待同步云端'
  return '战绩随道号自动留存'
})

/* ---------- 斗法状态 ---------- */
const battle = reactive({
  open: false,
  state: null,
  settle: null
})

const me = computed(() => battle.state?.me || { hp: 0, maxHp: 1, exp: 0, move: '' })
const foe = computed(() => battle.state?.foe || SECT_MEMBERS[0])
const foeHp = computed(() => battle.state?.foe || { hp: 0, maxHp: 1 })

const myHpPct = computed(() => Math.max(0, Math.round((me.value.hp / me.value.maxHp) * 100)))
const foeHpPct = computed(() => Math.max(0, Math.round((foeHp.value.hp / foeHp.value.maxHp) * 100)))

/** 修为占比：只在双方皆攻时决定伤害轻重 */
const myShare = computed(() => Math.round(expShare(me.value.exp, foe.value.exp) * 100))
const edgeClass = computed(() =>
  myShare.value >= 55 ? 'sup-up' : myShare.value <= 45 ? 'sup-down' : 'sup-equal'
)
const supNote = computed(() => {
  if (myShare.value >= 55) return '对攻时我伤更重，放开手打'
  if (myShare.value <= 45) return '对攻吃亏，宜以防御相持、先积修为'
  return '修为相当，对攻两败俱伤'
})

/** 招式名 */
function moveName(k) {
  return k === 'attack' ? '攻击' : k === 'guard' ? '防御' : '—'
}

/** VS 处的交手提示 */
const roundTip = computed(() => {
  const s = battle.state
  if (!s || !s.last) return '法力相激'
  if (s.last.kind === 'clash') return '两攻相接'
  if (s.last.kind === 'calm') return '双守成平'
  return '一击被挡'
})

/* 最近一击的飘伤数字 */
const lastHit = reactive({ me: 0, foe: 0 })
const hitKey = reactive({ me: 0, foe: 0 })

const recentLog = computed(() => (battle.state?.log || []).slice(-4).reverse())

const resultClass = computed(() => {
  const s = battle.state
  if (!s) return ''
  return s.win ? 'win' : s.draw ? 'draw' : 'lose'
})
const resultTitle = computed(() => {
  const s = battle.state
  if (!s) return ''
  return s.win ? '胜' : s.draw ? '平手' : '败'
})
const resultReason = computed(() => {
  const s = battle.state
  if (!s) return ''
  if (s.win) {
    if (s.byTimeout) return `相持 ${s.round} 回合至时限，以残余气血胜之`
    return `对攻 ${s.clashes} 次，修为压过${foe.value.name}，破其气血`
  }
  if (s.draw) {
    if (s.mutual) return '最后一记对攻同归于尽，判为平手'
    if (s.byTimeout) return `相持 ${s.round} 回合未分高下，判为平手`
    return '双方皆守，久攻不下，判为平手'
  }
  return s.byTimeout ? '苦战至时限，残余气血不及对方' : '对攻之下修为不及，气血先竭'
})
const advice = computed(() => {
  const s = battle.state
  if (!s) return ''
  const diff = foe.value.exp - me.value.exp
  if (diff > 0) return `对方修为高出 ${diff} 点，对攻必吃亏——只守不攻只能拖成平手，先积修为再来。`
  if (diff < 0) return `修为压过对方 ${-diff} 点，尽管对攻——修为愈高，一击愈重。`
  return '修为相当，对攻两败俱伤；可多守待机，看谁先失手。'
})

const nextIndex = computed(() => {
  const s = battle.state
  if (!s) return -1
  const n = s.index + 1
  if (n >= SECT_SIZE) return -1
  return isUnlocked(n) ? n : -1
})

/* ---------- 操作 ---------- */
let hitTimer = null

function openBattle(i) {
  const r = startDuel(i)
  if (!r.ok) {
    emit('toast', r.msg)
    return
  }
  clearHitTimer()
  lastHit.me = 0
  lastHit.foe = 0
  battle.state = reactive(r.state)
  battle.settle = null
  battle.open = true
  if (typeof window !== 'undefined') window.scrollTo({ top: 0, behavior: 'smooth' })
}

function closeBattle() {
  clearHitTimer()
  battle.open = false
  battle.state = null
  battle.settle = null
}

/** 打一回合 */
function choose(key) {
  const s = battle.state
  if (!s || s.over) return

  const beforeMe = s.me.hp
  const beforeFoe = s.foe.hp
  const r = duelRound(s, key)

  // 飘伤
  if (beforeFoe - s.foe.hp > 0) {
    lastHit.foe = beforeFoe - s.foe.hp
    hitKey.foe += 1
  } else {
    lastHit.foe = 0
  }
  if (beforeMe - s.me.hp > 0) {
    lastHit.me = beforeMe - s.me.hp
    hitKey.me += 1
  } else {
    lastHit.me = 0
  }

  if (r.over) {
    battle.settle = settleDuel(s)
    emit('toast', battle.settle.msg)
    return
  }
  clearHitTimer()
  hitTimer = setTimeout(() => {
    lastHit.me = 0
    lastHit.foe = 0
  }, 1200)
}

/** 速战：自动随机出手，打到分出胜负 */
function autoFight() {
  const s = battle.state
  if (!s || s.over) return
  let guard = 0
  while (!s.over && guard < 100) {
    duelRound(s, Math.random() < 0.5 ? 'attack' : 'guard')
    guard += 1
  }
  if (s.over) {
    lastHit.me = 0
    lastHit.foe = 0
    battle.settle = settleDuel(s)
    emit('toast', battle.settle.msg)
  }
}

function clearHitTimer() {
  if (hitTimer) {
    clearTimeout(hitTimer)
    hitTimer = null
  }
}

function onReset() {
  resetSect()
  closeBattle()
  emit('toast', '云岚宗名册已重设，十三人复位')
}

watch(
  () => props.username,
  (n) => {
    initSect(n)
    closeBattle()
  },
  { immediate: true }
)

onBeforeUnmount(clearHitTimer)
</script>

<style scoped>
.sect {
  margin-top: 26px;
  padding: 22px 22px 16px;
  border: 1px solid var(--mist);
  border-radius: 16px;
  background: linear-gradient(165deg, rgba(30, 18, 48, 0.72), rgba(9, 10, 26, 0.88));
}

/* ---------- 头部 ---------- */
.sect-head {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
}
.sh-left { flex: 1; }
.sh-badge {
  display: inline-block;
  padding: 2px 10px;
  margin-bottom: 8px;
  border: 1px solid rgba(232, 198, 107, 0.45);
  border-radius: 999px;
  font-size: 11px;
  letter-spacing: 2px;
  color: var(--gold);
}
.sh-left h3 {
  font-family: var(--font-display);
  font-size: 24px;
  letter-spacing: 3px;
  color: #f2e6c8;
}
.sh-left p { margin-top: 6px; font-size: 12.5px; line-height: 1.75; color: #93a0c4; }
.sh-left b { color: #e0cf9f; }

.sh-right { text-align: right; min-width: 150px; }
.sh-progress { font-size: 13px; color: var(--cyan); }
.sh-progress b { font-size: 18px; }
.sh-mine { margin-top: 4px; font-size: 12px; color: #8b97b8; }
.sh-mine b { color: #f2e6c8; }
.sh-save { margin-top: 6px; font-size: 11px; letter-spacing: 1px; color: #6f7a9c; }

/* ---------- 名册 ---------- */
.sect-list {
  list-style: none;
  margin: 12px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sect-row {
  display: grid;
  grid-template-columns: 42px 1.6fr 1.4fr 1.2fr 92px;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  background: rgba(12, 14, 32, 0.55);
  transition: border-color 0.2s, background 0.2s, transform 0.2s;
}
.sect-row.current {
  border-color: rgba(232, 198, 107, 0.5);
  background: rgba(40, 32, 20, 0.38);
}
.sect-row.cleared { border-color: rgba(111, 233, 255, 0.28); }
.sect-row.locked { opacity: 0.42; }
.sect-row:not(.locked):hover { transform: translateX(3px); }

.sr-no { font-family: var(--font-display); font-size: 15px; color: #6f7a9c; }
.sect-row.current .sr-no { color: var(--gold); }

.sr-name {
  font-family: var(--font-display);
  font-size: 16px;
  letter-spacing: 1px;
  color: #f2e6c8;
}
.sr-title {
  margin-left: 8px;
  font-family: inherit;
  font-size: 11px;
  letter-spacing: 1px;
  color: #7d89ad;
}
.sr-line {
  margin-top: 2px;
  font-size: 11.5px;
  color: #6f7a9c;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sr-realm-name { font-size: 13px; color: var(--cyan); }
.sr-exp { margin-top: 2px; font-size: 11.5px; color: #7d89ad; }

.sr-power-num { font-size: 13px; color: #e0b0a0; }
.sr-power-sub { margin-top: 2px; font-size: 11px; color: #6f7a9c; }

.sr-act { text-align: right; }
.sr-go {
  padding: 6px 16px;
  border: 1px solid rgba(232, 198, 107, 0.6);
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(232, 198, 107, 0.22), rgba(232, 198, 107, 0.06));
  color: var(--gold);
  font-size: 13px;
  letter-spacing: 1px;
  cursor: pointer;
  transition: background 0.2s, transform 0.15s;
}
.sr-go:hover { background: rgba(232, 198, 107, 0.32); transform: scale(1.04); }
.sr-done { font-size: 12px; letter-spacing: 1px; color: var(--cyan); }
.sr-lock { font-size: 12px; color: #5f6a8c; }

/* ---------- 底部 ---------- */
.sect-foot {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 14px;
}
.sect-foot p { flex: 1; font-size: 11.5px; line-height: 1.8; color: #6f7a9c; }
.sect-reset {
  flex: none;
  padding: 5px 14px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 999px;
  background: transparent;
  color: #7d89ad;
  font-size: 12px;
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;
}
.sect-reset:hover { border-color: #e08c8c; color: #e08c8c; }

/* ---------- 斗法场 ---------- */
.arena { padding-top: 2px; }

.ar-head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 10px;
}
.ar-back {
  padding: 6px 14px;
  border: 1px solid var(--mist);
  border-radius: 999px;
  background: transparent;
  color: #cfd8f2;
  font-size: 12.5px;
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;
}
.ar-back:hover { border-color: var(--gold); color: var(--gold); }
.ar-title { flex: 1; font-size: 13px; color: #93a0c4; letter-spacing: 1px; }
.ar-title-name {
  margin-left: 8px;
  font-family: var(--font-display);
  font-size: 19px;
  letter-spacing: 2px;
  color: var(--gold);
}
.ar-tag {
  padding: 3px 12px;
  border: 1px solid rgba(111, 233, 255, 0.35);
  border-radius: 999px;
  font-size: 11.5px;
  color: var(--cyan);
}
.ar-round { font-size: 12px; color: #7d89ad; }

.ar-sup {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  font-size: 12px;
}
.sup-up { color: #ffb27a; }
.sup-down { color: #8fd0ff; }
.sup-equal { color: #b7c2e0; }
.ar-sup-note { color: #6f7a9c; font-size: 11.5px; }
.ar-first {
  margin-left: auto;
  padding: 1px 10px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 999px;
  font-size: 11px;
  color: #8b97b8;
}

.ar-ring {
  display: grid;
  grid-template-columns: 1fr 96px 1fr;
  align-items: stretch;
  gap: 10px;
  padding: 16px 14px;
  border: 1px solid rgba(255, 255, 255, 0.07);
  border-radius: 14px;
  background: radial-gradient(circle at 50% 40%, rgba(60, 40, 90, 0.45), rgba(8, 9, 22, 0.7));
}
.ar-ring.fighting { animation: ring-shake 0.9s ease-in-out infinite; }
@keyframes ring-shake {
  0%, 100% { transform: translateX(0); }
  30% { transform: translateX(-2px); }
  70% { transform: translateX(2px); }
}

.ar-side { position: relative; padding: 6px 10px; }
.ar-side.foe { text-align: right; }
.ar-side-tag { font-size: 11.5px; letter-spacing: 1px; color: #7d89ad; }
.ar-realm {
  margin-top: 4px;
  font-family: var(--font-display);
  font-size: 18px;
  letter-spacing: 1px;
  color: #f2e6c8;
}

.hp-bar {
  height: 9px;
  margin: 10px 0 5px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  overflow: hidden;
}
.hp-bar i {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(111, 233, 255, 0.6), var(--cyan));
  transition: width 0.35s ease;
}
.hp-bar i.low { background: linear-gradient(90deg, rgba(255, 138, 128, 0.7), #ff8a80); }
.foe-bar { direction: rtl; }
.foe-bar i { background: linear-gradient(90deg, rgba(224, 140, 140, 0.6), #e08c8c); }
.hp-text { font-size: 11.5px; color: #93a0c4; }
.hp-text b { color: #f2e6c8; font-size: 13px; }

.ar-power { margin-top: 6px; font-size: 12px; color: var(--gold); }
.ar-move {
  margin-top: 6px;
  font-size: 11.5px;
  color: #7d89ad;
}
.ar-move b {
  padding: 1px 9px;
  border-radius: 999px;
  font-weight: 400;
  font-size: 11.5px;
}
.ar-move b.attack { border: 1px solid rgba(232, 198, 107, 0.45); color: var(--gold); }
.ar-move b.guard { border: 1px solid rgba(111, 233, 255, 0.45); color: var(--cyan); }

.hit-num {
  position: absolute;
  top: 42%;
  font-family: var(--font-display);
  font-size: 26px;
  animation: hit-float 1s ease-out forwards;
  pointer-events: none;
}
.me-hit { right: 14px; color: #ff8a80; }
.foe-hit { left: 14px; color: #ffd479; }
@keyframes hit-float {
  0% { opacity: 0; transform: translateY(6px) scale(0.8); }
  30% { opacity: 1; transform: translateY(-4px) scale(1.15); }
  100% { opacity: 0; transform: translateY(-20px) scale(1); }
}

.ar-vs {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.vs-mark {
  font-family: var(--font-display);
  font-size: 28px;
  color: var(--gold);
  text-shadow: 0 0 18px rgba(232, 198, 107, 0.4);
}
.vs-mark.clash { animation: vs-pulse 1.1s ease-in-out infinite; }
@keyframes vs-pulse {
  0%, 100% { opacity: 0.75; }
  50% { opacity: 1; text-shadow: 0 0 24px rgba(232, 198, 107, 0.7); }
}
.vs-tip { font-size: 10.5px; color: #6f7a9c; text-align: center; }

/* ---------- 招式 ---------- */
.ar-tactics {
  display: grid;
  grid-template-columns: 1fr 1fr auto;
  gap: 10px;
  margin-top: 14px;
}
.tac {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  padding: 10px 14px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
  background: rgba(14, 16, 38, 0.7);
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s, transform 0.15s;
  text-align: left;
}
.tac:hover { transform: translateY(-2px); }
.tac-ico { font-size: 16px; color: #cfd8f2; }
.tac-name { font-family: var(--font-display); font-size: 15px; letter-spacing: 2px; color: #f2e6c8; }
.tac-desc { font-size: 10.5px; color: #7d89ad; }

.tac.attack { border-color: rgba(232, 198, 107, 0.28); }
.tac.attack:hover { border-color: rgba(232, 198, 107, 0.6); background: rgba(60, 48, 22, 0.6); }
.tac.guard { border-color: rgba(111, 233, 255, 0.28); }
.tac.guard:hover { border-color: rgba(111, 233, 255, 0.55); background: rgba(18, 44, 58, 0.6); }

.tac-auto {
  align-self: stretch;
  padding: 0 16px;
  border: 1px dashed rgba(255, 255, 255, 0.18);
  border-radius: 12px;
  background: transparent;
  color: #8b97b8;
  font-size: 11.5px;
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;
}
.tac-auto:hover { border-color: var(--gold); color: var(--gold); }

/* ---------- 战报 ---------- */
.duel-log {
  list-style: none;
  margin: 12px 0 0;
  padding: 10px 14px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  background: rgba(8, 9, 20, 0.6);
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.duel-log li {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #a7b2d4;
}
.duel-log li.win .dl-line { color: #ffd479; }
.duel-log li.lose .dl-line { color: #ff9c94; }
.dl-round {
  flex: none;
  width: 18px;
  text-align: right;
  color: #5f6a8c;
  font-family: var(--font-display);
}
.dl-line { line-height: 1.6; }

/* ---------- 结果 ---------- */
.ar-result {
  margin-top: 14px;
  padding: 16px 18px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  background: rgba(10, 12, 28, 0.65);
  text-align: center;
}
.ar-result.win { border-color: rgba(232, 198, 107, 0.55); box-shadow: 0 0 24px rgba(232, 198, 107, 0.1); }
.ar-result.lose { border-color: rgba(224, 140, 140, 0.45); }
.ar-result.draw { border-color: rgba(150, 160, 190, 0.4); }

.rs-title {
  font-family: var(--font-display);
  font-size: 30px;
  letter-spacing: 8px;
  color: var(--gold);
}
.ar-result.lose .rs-title { color: #e08c8c; }
.ar-result.draw .rs-title { color: #b7c2e0; }
.rs-reason { margin-top: 6px; font-size: 13px; color: #b7c2e0; }
.rs-compare {
  display: flex;
  justify-content: center;
  gap: 22px;
  margin-top: 12px;
  font-size: 12px;
  color: #8b97b8;
}
.rs-reward { margin-top: 10px; font-size: 13px; color: var(--cyan); }
.rs-reward.muted { color: #6f7a9c; }
.rs-acts { margin-top: 16px; display: flex; justify-content: center; gap: 10px; flex-wrap: wrap; }
.rs-advice { margin-top: 12px; font-size: 11.5px; color: #6f7a9c; }

.ar-btn {
  padding: 9px 28px;
  border: 1px solid rgba(232, 198, 107, 0.65);
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(232, 198, 107, 0.26), rgba(232, 198, 107, 0.08));
  color: var(--gold);
  font-size: 14px;
  letter-spacing: 2px;
  cursor: pointer;
  transition: background 0.2s, transform 0.15s;
}
.ar-btn:hover { background: rgba(232, 198, 107, 0.34); transform: scale(1.03); }
.ar-btn.ghost {
  border-color: rgba(255, 255, 255, 0.16);
  background: transparent;
  color: #b7c2e0;
  letter-spacing: 1px;
}
.ar-btn.ghost:hover { background: rgba(255, 255, 255, 0.06); }

@media (max-width: 860px) {
  .sect-row { grid-template-columns: 34px 1fr 92px; row-gap: 6px; }
  .sr-realm, .sr-power { display: none; }
  .ar-ring { grid-template-columns: 1fr; }
  .ar-side.foe { text-align: left; }
  .ar-vs { flex-direction: row; }
  .vs-tip { display: none; }
  .ar-tactics { grid-template-columns: 1fr 1fr; }
  .tac-auto { grid-column: 1 / -1; padding: 8px; }
}
</style>
