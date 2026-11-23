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
          <!-- 奇缘积淀：道心 / 古神底蕴 / 执念 / 妖元 -->
          <div class="st-extra">
            <span class="ex-chip">道心 {{ b.state.stats.daoxin }}</span>
            <span class="ex-chip gold">古神底蕴 {{ b.state.stats.guyuan }}</span>
            <span class="ex-chip danger">执念 {{ b.state.stats.zhonian }}</span>
            <span class="ex-chip">妖元 {{ b.state.stats.yaoyuan }}</span>
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

        <!-- ---------- 星域古图（点星球进入修炼） ---------- -->
        <div class="bz-panel bz-map-panel">
          <div class="panel-head">
            <h3>星域古图</h3>
            <span class="panel-count">点星球进入修炼 · 每星灵蕴一万 · 奇遇之星一探便定</span>
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

              <!-- 星球：贴图 + 光晕，可点入星修炼 -->
              <g
                v-for="p in PLANETS"
                :key="p.id"
                class="planet"
                :class="{ cultivating: activeId === p.id }"
                :title="p.name + ' · ' + TIER_NAME[p.tier]"
                @click="onPlanet(p)"
              >
                <defs>
                  <clipPath :id="'pc-' + p.id">
                    <circle :cx="p.x" :cy="p.y" :r="p.r * 1.52" />
                  </clipPath>
                </defs>
                <!-- 星球本色光晕（两层） -->
                <circle :cx="p.x" :cy="p.y" :r="p.r * 2.4" :fill="p.hue" opacity="0.14" />
                <circle :cx="p.x" :cy="p.y" :r="p.r * 1.85" :fill="p.hue" opacity="0.22" />
                <!-- 本色光核 + 轮廓亮边：暗调星球也始终明亮 -->
                <circle :cx="p.x" :cy="p.y" :r="p.r * 1.12" :fill="p.hue" opacity="0.34" />
                <circle
                  :cx="p.x"
                  :cy="p.y"
                  :r="p.r * 1.46"
                  fill="none"
                  :stroke="p.hue"
                  stroke-width="0.9"
                  opacity="0.42"
                />
                <!-- 修炼中的星球：金环流转 -->
                <circle
                  v-if="activeId === p.id"
                  class="p-ring"
                  :cx="p.x"
                  :cy="p.y"
                  :r="p.r * 1.9"
                  fill="none"
                  stroke="#ffe27a"
                  stroke-width="1.2"
                  stroke-dasharray="10 14"
                />
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
                <text
                  class="p-left"
                  :class="{ on: activeId === p.id }"
                  :x="p.x"
                  :y="p.y + p.r + 31"
                >
                  {{ activeId === p.id ? '修炼中' : isDrained(p) ? '无法修行' : `灵蕴 ${b.state.planets[p.id].left}` }}
                </text>
              </g>

              <!-- 奇遇之星：同样可入星修炼，亦可探查奇遇（任何状态都不置灰，始终可点） -->
              <g
                v-for="p in EVENT_PLANETS"
                :key="p.id"
                class="planet ep"
                :class="{ cultivating: activeId === p.id, god: p.id === 'guyuanxing' }"
                :title="p.name + ' · ' + p.tag"
                @click="onEventPlanet(p)"
              >
                <defs>
                  <clipPath :id="'pc-' + p.id">
                    <circle :cx="p.x" :cy="p.y" :r="p.r * 1.52" />
                  </clipPath>
                </defs>
                <!-- 奇遇星光晕（两层，加强以保证底部两星醒目） -->
                <circle :cx="p.x" :cy="p.y" :r="p.r * 2.4" :fill="p.hue" opacity="0.2" />
                <circle :cx="p.x" :cy="p.y" :r="p.r * 1.85" :fill="p.hue" opacity="0.28" />
                <!-- 本色光核：垫在贴图之下，借 screen 混合把星体暗部整体提亮 -->
                <circle :cx="p.x" :cy="p.y" :r="p.r * 1.15" :fill="p.hue" opacity="0.55" />
                <!-- 常态亮边：让星体始终有清晰的轮廓光，不显暗 -->
                <circle
                  :cx="p.x"
                  :cy="p.y"
                  :r="p.r * 1.48"
                  fill="none"
                  :stroke="p.hue"
                  stroke-width="1.1"
                  opacity="0.6"
                />
                <!-- 修炼中的奇遇星：金环流转 -->
                <circle
                  v-if="activeId === p.id"
                  class="p-ring"
                  :cx="p.x"
                  :cy="p.y"
                  :r="p.r * 1.9"
                  fill="none"
                  stroke="#ffe27a"
                  stroke-width="1.2"
                  stroke-dasharray="10 14"
                />
                <!-- 星球贴图 -->
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
                <text
                  class="p-left"
                  :class="activeId === p.id ? 'on' : 'ep-tag'"
                  :x="p.x"
                  :y="p.y + p.r + 31"
                >
                  {{ activeId === p.id ? '修炼中' : isDrained(p) ? '无法修行' : isEventDone(p) ? '已探明' : p.tag }}
                </text>
              </g>
            </svg>
          </div>

          <!-- ---------- 奇遇状态栏（当前触发的奇遇抉择结果） ---------- -->
          <transition name="fade">
            <div v-if="ev.planet && ev.chosen !== null" class="cult-bar ev-bar">
              <span class="cult-dot" aria-hidden="true"></span>
              <span class="cult-name">{{ ev.planet.name }} · 奇缘已了</span>
              <span class="cult-info">{{ ev.planet.options[ev.chosen].text }}</span>
              <button class="cult-exit" @click="closeEvent">领会离去</button>
            </div>
          </transition>

          <!-- ---------- 星中修炼状态栏 ---------- -->
          <transition name="fade">
            <div v-if="activePlanet" class="cult-bar">
              <span class="cult-dot" aria-hidden="true"></span>
              <span class="cult-name">{{ activePlanet.name }} · 修炼中</span>
              <span class="cult-info">
                三秒一息 · 修为 +{{ GAIN_PER_TICK }} · 本次入星已得 +{{ sessionGain }} · 此星灵蕴尚余
                {{ b.state.planets[activePlanet.id].left }}
              </span>
              <button class="cult-exit" @click="exitPlanet">离开星球</button>
            </div>
          </transition>
          <div class="map-legend">
            <span class="lg"><i style="background: #6fe9ff"></i>下界灵星</span>
            <span class="lg"><i style="background: #ffd76a"></i>中界灵星</span>
            <span class="lg"><i style="background: #ff9a4d"></i>上界灵星</span>
            <span class="lg"><i style="background: #ffe27a"></i>古神之地</span>
            <span class="lg"><i style="background: #8affc4"></i>奇遇之星 · 一探便定</span>
            <span class="lg-tip">每星灵蕴一万；身临星中，三秒一息修为自长；灵蕴耗尽则无法修行</span>
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

    <!-- ================= 星球抉择弹窗（在此修炼 / 前往探查 / 离去） ================= -->
    <transition name="fade">
      <div v-if="pc.planet" class="bz-mask" @click.self="closeChoice">
        <div class="bz-modal ev-modal">
          <p class="ev-tag">【{{ planetTag(pc.planet) }}】</p>
          <p class="modal-title">{{ pc.planet.name }}</p>
          <p class="ev-desc">{{ planetDesc(pc.planet) }}</p>
          <p class="pc-left">
            {{
              isDrained(pc.planet)
                ? '此星灵蕴已尽 · 无法修行'
                : `此星灵蕴尚余 ${planetLeft(pc.planet)} · 身临星中三秒一息，修为 +${GAIN_PER_TICK}`
            }}
          </p>
          <div class="ev-opts">
            <button class="ev-btn" :disabled="isDrained(pc.planet)" @click="pickCultivate">
              {{ activeId === pc.planet.id ? '继续修炼' : '在此修炼' }}
            </button>
            <button
              v-if="isEventPlanet(pc.planet) && !isEventDone(pc.planet)"
              class="ev-btn"
              @click="pickExplore"
            >
              前往探查 · {{ pc.planet.tag }}
            </button>
            <button class="ev-btn ev-btn-leave" @click="closeChoice">离去</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- ================= 奇遇抉择弹窗 ================= -->
    <transition name="fade">
      <div v-if="ev.planet" class="bz-mask" @click.self="closeEvent">
        <div class="bz-modal ev-modal">
          <p class="ev-tag">【{{ ev.planet.tag }}】</p>
          <p class="modal-title">{{ ev.planet.name }}</p>
          <p class="ev-desc">{{ ev.planet.desc }}</p>

          <!-- 未抉择：两个选项 -->
          <div v-if="ev.chosen === null" class="ev-opts">
            <button
              v-for="(o, i) in ev.planet.options"
              :key="i"
              class="ev-btn"
              @click="chooseEvent(i)"
            >
              {{ o.label }}
            </button>
          </div>

          <!-- 已抉择：结果提示 -->
          <template v-else>
            <p class="ev-result">{{ ev.planet.options[ev.chosen].text }}</p>
            <button class="modal-btn" @click="closeEvent">领 悟</button>
          </template>
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
 * 星域古图：仙逆诸星，点星球进入其中修炼
 * 每颗星球灵蕴固定一万，入星后每三秒修为 +50，
 * 灵蕴耗尽即「枯竭」，永久不可再修；余量随道号存入 localStorage
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

/** 每颗星球可修炼的灵蕴总量（固定一万） */
const PLANET_TOTAL = 10000

const TIER_NAME = { 1: '下界灵星', 2: '中界灵星', 3: '上界灵星', 4: '古神之地' }

/* =============================================================
 * 奇遇之星：五颗特异星辰，点击触发「一探便定」的奇遇抉择
 * 每星只能抉择一次，选定即「已探明」；效果随存档一并保存
 * fx 字段：exp 本尊修为 / daoxin 道心 / guyuan 古神底蕴 / zhonian 执念 / yaoyuan 妖元
 * ============================================================= */
const EVENT_PLANETS = [
  {
    id: 'qinglan',
    name: '青岚星',
    tag: '凡星',
    x: 95,
    y: 190,
    r: 10,
    hue: '#8affc4',
    img: imgQinggang,
    desc: '灵气平和的凡界星辰。',
    options: [
      { label: '静心修炼', text: '吸纳星辰灵气，修为微涨。修为 + 8', fx: { exp: 8 } },
      { label: '稍作停留便离去', text: '此地灵气浅薄，你选择继续远行。无增减', fx: {} }
    ]
  },
  {
    id: 'duanxu',
    name: '断墟星',
    tag: '古遗迹',
    x: 485,
    y: 352,
    r: 10,
    hue: '#c9a46a',
    img: imgLuotian,
    desc: '远古修士陨落之地，残阵隐现。',
    options: [
      { label: '深入探寻', text: '寻得一缕残道印记，道心 + 5，却触动禁制。', fx: { daoxin: 5 } },
      { label: '在外观望', text: '风险难测，你不愿贸然涉险，转身离开。', fx: {} }
    ]
  },
  {
    id: 'guyuanxing',
    name: '古神残星',
    tag: '本尊专属',
    x: 585,
    y: 38,
    r: 11,
    hue: '#ffe27a',
    img: imgGushen,
    desc: '古神尸骸所化，弥漫原始神息。',
    options: [
      { label: '本尊凝神感悟', text: '神源入体，古神底蕴大增。古神底蕴 + 12', fx: { guyuan: 12 } },
      { label: '仅仅旁观，不强行汲取', text: '神息狂暴，不宜久留，你默默记下此地位置。', fx: {} }
    ]
  },
  {
    id: 'manhuang',
    name: '蛮荒妖星',
    tag: '妖星',
    x: 180,
    y: 348,
    r: 10,
    hue: '#ff4d6a',
    img: imgYouming,
    desc: '凶妖盘踞，嘶吼自大地传来。',
    options: [
      {
        label: '出手镇压妖兽',
        text: '激战取胜，得妖元，修为 + 15；道心小幅动荡。',
        fx: { exp: 15, yaoyuan: 10, daoxin: -2 }
      },
      { label: '绕道避开', text: '不愿无谓厮杀，你悄然掠过这颗星辰。', fx: {} }
    ]
  },
  {
    id: 'jimie',
    name: '域外寂灭星',
    tag: '高危',
    x: 748,
    y: 292,
    r: 11,
    hue: '#8a7dff',
    img: imgXiaoyue,
    desc: '死气弥漫，容易滋生心魔。',
    options: [
      {
        label: '逆流而入夺造化',
        text: '险死还生，收获丰厚修为，执念 + 10。',
        fx: { exp: 50, zhonian: 10 }
      },
      { label: '立刻远遁', text: '寂灭之力可怖，你果断抽身离去，保全自身。', fx: {} }
    ]
  }
]

/** 全部星球：灵蕴星 + 奇遇星（奇遇星同样可入星修炼，灵蕴亦为一万） */
const ALL_PLANETS = [...PLANETS, ...EVENT_PLANETS]

/** 开辟诸星：每星灵蕴皆为固定一万 */
function genPlanets() {
  const o = {}
  for (const p of ALL_PLANETS) {
    o[p.id] = { total: PLANET_TOTAL, left: PLANET_TOTAL }
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

/* ---------------- 存档（数据库权威 + 本地缓存） ---------------- */
/* 数据分层与洞府同款：
 *   - 数据库 t_benzun 为权威数据源（后端 /api/benzun），换浏览器、清缓存都不丢
 *   - localStorage 仅为离线缓存与首屏秒开，断网也能继续修炼，联网后自动补传
 */
const KEY_PREFIX = 'xianni_benzun_'
const API_BENZUN = '/api/benzun'
/** 写入数据库的防抖间隔（毫秒），修炼三秒一息，不宜过于频繁 */
const PUSH_DELAY = 800

function storageKey() {
  return KEY_PREFIX + (props.username || 'guest')
}

/** 奇缘积淀（奇遇抉择所得）：道心 / 古神底蕴 / 执念 / 妖元 */
function genStats() {
  return { daoxin: 0, guyuan: 0, zhonian: 0, yaoyuan: 0 }
}

/** 存档字段安全取整 */
function safeNum(v) {
  return Math.floor(Number(v) || 0)
}

const b = reactive({
  // exp 本尊修为 / level 当前第几重（0 起）/ planets 各星灵蕴余量
  // stats 奇缘积淀 / events 已探明的奇遇之星
  state: { exp: 0, level: 0, planets: genPlanets(), stats: genStats(), events: {} }
})

/** 最后修改时间戳（毫秒），用于与数据库比对新旧 */
let savedAt = 0
/** 是否已与数据库同步过 */
let synced = false

/** 把一份存档（本地缓存或云端返回）写进内存状态，星球灵蕴只认「一万制」 */
function applyState(s) {
  if (!s) return
  b.state.exp = Math.max(0, Math.floor(Number(s.exp) || 0))
  b.state.level = Math.max(0, Math.min(GOD_REALMS.length - 1, Math.floor(Number(s.level) || 0)))
  // 星球灵蕴：仅保留同为「一万制」的余量，其余（含旧随机档）一律重开为一万
  const saved = s.planets && typeof s.planets === 'object' ? s.planets : {}
  const fresh = genPlanets()
  const merged = {}
  for (const p of ALL_PLANETS) {
    const sv = saved[p.id]
    if (sv && Number(sv.total) === PLANET_TOTAL && Number(sv.left) >= 0) {
      merged[p.id] = { total: PLANET_TOTAL, left: Math.min(Number(sv.left), PLANET_TOTAL) }
    } else {
      merged[p.id] = fresh[p.id]
    }
  }
  b.state.planets = merged
  // 奇缘积淀与奇遇探明记录（缺省则从零再积）
  const svStats = s.stats && typeof s.stats === 'object' ? s.stats : {}
  b.state.stats = {
    daoxin: safeNum(svStats.daoxin),
    guyuan: safeNum(svStats.guyuan),
    zhonian: safeNum(svStats.zhonian),
    yaoyuan: safeNum(svStats.yaoyuan)
  }
  b.state.events = s.events && typeof s.events === 'object' ? s.events : {}
  savedAt = Math.max(savedAt, Math.floor(Number(s.savedAt) || 0))
}

/** 读本地缓存（首屏秒开），有档返回 true */
function loadLocal() {
  try {
    const raw = localStorage.getItem(storageKey())
    if (raw) {
      applyState(JSON.parse(raw) || {})
      return true
    }
  } catch {
    /* 档案损坏则从零再修 */
  }
  return false
}

/** 页面挂载时的载入入口：先读本地缓存避免白屏 */
function loadState() {
  loadLocal()
}

/** 本地是否有值得保留的数据（用于旧档迁移进数据库） */
function hasLocalData() {
  if (b.state.exp > 0) return true
  for (const id in b.state.planets) {
    const st = b.state.planets[id]
    if (st && Number(st.left) < PLANET_TOTAL) return true
  }
  for (const k in b.state.stats) {
    if (safeNum(b.state.stats[k]) > 0) return true
  }
  return Object.keys(b.state.events).length > 0
}

/** 当前存档快照（用于本地缓存与提交数据库） */
function snapshot() {
  return {
    username: props.username || 'guest',
    exp: b.state.exp,
    level: b.state.level,
    planets: b.state.planets,
    stats: b.state.stats,
    events: b.state.events,
    savedAt
  }
}

/** 写本地缓存（离线可用） */
function writeLocal() {
  try {
    localStorage.setItem(storageKey(), JSON.stringify(snapshot()))
  } catch {
    /* 隐私模式忽略 */
  }
}

/* ==================== 数据库同步 ==================== */

let pushTimer = null
let pushing = false
let pushAgain = false

/** 防抖推送：本地改完稍等片刻再落库，避免修炼计时刷库 */
function schedulePush() {
  if (typeof fetch !== 'function') return
  clearTimeout(pushTimer)
  pushTimer = setTimeout(() => pushToServer(), PUSH_DELAY)
}

/** 立即把当前存档写入数据库；失败则静默，待下次改动重试 */
async function pushToServer(force = false) {
  if (typeof fetch !== 'function') return
  if (pushing) {
    pushAgain = true
    return
  }
  // 未产生过任何进度的空档不落库，防止把云端好档覆盖成零
  if (savedAt === 0 && !hasLocalData()) return
  if (force) savedAt = Date.now()
  const at = savedAt
  const name = props.username || 'guest'
  pushing = true
  try {
    const res = await fetch(`${API_BENZUN}/save`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(snapshot()),
      // keepalive：关闭/刷新页面的瞬间也能把请求发完，避免最后一次操作丢失
      keepalive: true
    })
    const json = await res.json().catch(() => null)
    if (json?.success) {
      synced = true
      const r = json.data
      // 云端记录更新（例如另开过页面）：以云端为准，避免互相覆盖
      if (r?.newer && r.state && Number(r.state.savedAt || 0) > at && name === props.username) {
        applyState(r.state)
        writeLocal()
      }
    } else {
      synced = false
    }
  } catch {
    synced = false
  } finally {
    pushing = false
    if (pushAgain) {
      pushAgain = false
      schedulePush()
    }
  }
}

/** 从数据库拉取存档并按新旧合并 */
async function pullFromServer() {
  if (typeof fetch !== 'function') return
  const name = props.username || 'guest'
  try {
    const res = await fetch(`${API_BENZUN}?username=${encodeURIComponent(name)}`)
    const json = await res.json().catch(() => null)
    if (!json?.success || name !== props.username) return
    const remote = json.data?.state || null
    const remoteTime = Number(remote?.savedAt || 0)
    const localTime = savedAt

    if (!remote) {
      // 库里没有：把本地已有数据（旧版本存档）迁进去，下次打开直接读库
      if (hasLocalData()) await pushToServer(true)
      return
    }
    if (remoteTime > localTime) {
      // 云端更新：采用云端
      applyState(remote)
      writeLocal()
    } else if (localTime > remoteTime) {
      // 本地更新：上传覆盖
      await pushToServer()
    } else if (hasLocalData() && b.state.exp > (Number(remote.exp) || 0)) {
      // 时间戳相同（旧档无时间戳）：修为更高者为准
      await pushToServer(true)
    } else {
      applyState(remote)
      writeLocal()
    }
    synced = true
  } catch {
    synced = false
  }
}

/** 离开页面 / 切后台时，把没来得及推送的改动立即落库 */
function flushOnLeave() {
  if (!(props.username || 'guest')) return
  if (pushTimer) {
    clearTimeout(pushTimer)
    pushTimer = null
  }
  pushToServer(true)
}

function saveState() {
  savedAt = Date.now()
  writeLocal()
  schedulePush()
}

onMounted(() => {
  loadState()
  // 与数据库对齐：云端有档读云端，旧本地档自动迁移入库
  pullFromServer()
  // 关闭 / 刷新 / 切走页面时立即落库，避免最后几息的修为丢失
  window.addEventListener('pagehide', flushOnLeave)
  window.addEventListener('beforeunload', flushOnLeave)
  // 进入本尊页后允许纵向滚动（与首页同款 page-scroll，星图内容超出视口可上下滑动）
  document.body.classList.add('page-scroll')
  window.scrollTo({ top: 0 })
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
  stopCultivate(false)
  // 离开页面前把未落库的改动推上去
  flushOnLeave()
  window.removeEventListener('pagehide', flushOnLeave)
  window.removeEventListener('beforeunload', flushOnLeave)
  // 离开本尊页即恢复登录页的固定不滚动（首页挂载时会重新挂上 page-scroll）
  document.body.classList.remove('page-scroll')
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

/* ---------------- 星域古图 · 进入星球修炼 ---------------- */
/** 每三秒增长的本尊修为 */
const GAIN_PER_TICK = 50
/** 修炼节拍：三秒一息 */
const TICK_MS = 3000

/** 当前正在其中修炼的星球 id（同一时间本尊只能身在一星） */
const activeId = ref(null)
const activePlanet = computed(() => PLANETS.find(p => p.id === activeId.value) || null)
/** 本次入星修炼累计所得修为 */
const sessionGain = ref(0)
let cultTimer = null

/**
 * 点星球：弹出「在此修炼 / 离去」抉择（灵蕴星与奇遇星同款交互）
 */
function onPlanet(p) {
  pc.planet = p
}

/** 进入星球：开启三秒一息的修炼节拍 */
function enterPlanet(p) {
  stopCultivate(false)
  activeId.value = p.id
  sessionGain.value = 0
  emit('toast', `本尊已入「${p.name}」· 开始苦修`)
  cultTimer = setInterval(() => {
    const st = b.state.planets[p.id]
    if (!st || st.left <= 0) {
      exitPlanet()
      return
    }
    const gain = Math.min(st.left, GAIN_PER_TICK)
    st.left -= gain
    b.state.exp += gain
    sessionGain.value += gain
    saveState()
    if (st.left <= 0) {
      // 灵蕴取尽即枯竭：本尊出星，此星永久不可再修
      st.left = 0
      emit('toast', `「${p.name}」灵蕴已枯竭 · 本次入星共得修为 +${sessionGain.value}`)
      stopCultivate(false)
    }
  }, TICK_MS)
}

/** 离开星球：结算本次入星所得 */
function exitPlanet() {
  stopCultivate(true)
}

/** 停止修炼（toast=true 时结算提示） */
function stopCultivate(toast) {
  if (cultTimer) {
    clearInterval(cultTimer)
    cultTimer = null
  }
  const p = activePlanet.value
  if (p && toast) {
    emit('toast', `离开「${p.name}」· 本次入星共得修为 +${sessionGain.value}`)
  }
  activeId.value = null
  sessionGain.value = 0
}

/* ---------------- 奇遇之星 · 一探便定 ---------------- */
/** 奇缘积淀各项名称（用于提示） */
const FX_NAME = {
  exp: '本尊修为',
  daoxin: '道心',
  guyuan: '古神底蕴',
  zhonian: '执念',
  yaoyuan: '妖元'
}

const ev = reactive({ planet: null, chosen: null })

/* ---------------- 点星球抉择弹窗（在此修炼 / 前往探查 / 离去） ---------------- */
const pc = reactive({ planet: null })

/** 某星是否为奇遇之星 */
function isEventPlanet(p) {
  return EVENT_PLANETS.some(e => e.id === p.id)
}

/** 弹窗标签：奇遇星用 tag，灵蕴星用品阶名 */
function planetTag(p) {
  const e = EVENT_PLANETS.find(e => e.id === p.id)
  return e ? e.tag : TIER_NAME[p.tier]
}

/** 弹窗描述：奇遇星用 desc，灵蕴星用品阶说明 */
function planetDesc(p) {
  const e = EVENT_PLANETS.find(e => e.id === p.id)
  return e ? e.desc : `${TIER_NAME[p.tier]} · 星蕴浩瀚，身临星中三秒一息，修为 +${GAIN_PER_TICK}。`
}

/** 此星灵蕴余量 */
function planetLeft(p) {
  const st = b.state.planets[p.id]
  return st ? st.left : 0
}

/** 选「在此修炼」：入星开启三秒一息；已在其中则仅关闭弹窗（修炼不断） */
function pickCultivate() {
  const p = pc.planet
  closeChoice()
  if (!p || isDrained(p) || activeId.value === p.id) return
  enterPlanet(p)
}

/** 选「前往探查」：仅未探明的奇遇星可触发奇遇抉择 */
function pickExplore() {
  const p = pc.planet
  closeChoice()
  if (!p || !isEventPlanet(p) || isEventDone(p)) return
  ev.planet = p
  ev.chosen = null
}

function closeChoice() {
  pc.planet = null
}

/** 点奇遇之星：同款先弹抉择，探查入口收在弹窗内 */
function onEventPlanet(p) {
  pc.planet = p
}

/** 某奇遇之星是否已探明（缘法已了，不可再探） */
function isEventDone(p) {
  return !!b.state.events[p.id]
}

/** 抉择：生效奇缘积淀，标记已探明并保存存档 */
function chooseEvent(i) {
  const p = ev.planet
  if (!p || ev.chosen !== null) return
  ev.chosen = i
  const fx = p.options[i].fx || {}
  const parts = []
  for (const k of Object.keys(fx)) {
    const v = safeNum(fx[k])
    if (!v) continue
    if (k === 'exp') {
      b.state.exp = Math.max(0, b.state.exp + v)
    } else {
      b.state.stats[k] = (b.state.stats[k] || 0) + v
    }
    parts.push(`${FX_NAME[k]} ${v > 0 ? '+' + v : v}`)
  }
  b.state.events[p.id] = true
  saveState()
  if (parts.length) emit('toast', parts.join(' · '))
}

function closeEvent() {
  ev.planet = null
  ev.chosen = null
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

/* ---------- 奇缘积淀 ---------- */
.st-extra {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

.ex-chip {
  padding: 5px 12px;
  border-radius: 999px;
  font-size: 12px;
  letter-spacing: 1px;
  color: rgba(111, 233, 255, 0.92);
  background: rgba(111, 233, 255, 0.08);
  border: 1px solid rgba(111, 233, 255, 0.28);
}

.ex-chip.gold {
  color: #ffe9a8;
  background: rgba(232, 198, 107, 0.1);
  border-color: rgba(232, 198, 107, 0.4);
}

.ex-chip.danger {
  color: #ff9ab0;
  background: rgba(255, 106, 138, 0.08);
  border-color: rgba(255, 106, 138, 0.35);
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
  filter: saturate(1.08) brightness(1.12);
}

.planet:hover .p-img {
  filter: brightness(1.3) saturate(1.15);
}

/* 星球不再有任何置灰状态：灵蕴耗尽 / 奇遇已探明都不影响亮度，始终明亮可点 */

/* 修炼中的星球：星体泛光、金环流转 */
.planet.cultivating .p-img {
  filter: brightness(1.4) saturate(1.25);
}

.planet.cultivating .p-name {
  fill: #ffe9a8;
}

.p-ring {
  transform-box: fill-box;
  transform-origin: center;
  animation: ringSpin 6s linear infinite;
}

@keyframes ringSpin {
  to {
    transform: rotate(360deg);
  }
}

/* ---------- 星中修炼状态栏 ---------- */
.cult-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 12px;
  padding: 12px 16px;
  border-radius: 12px;
  border: 1px solid rgba(232, 198, 107, 0.5);
  background: linear-gradient(100deg, rgba(232, 198, 107, 0.14), rgba(58, 42, 107, 0.3));
  box-shadow: 0 0 18px rgba(232, 198, 107, 0.18);
}

.cult-dot {
  flex-shrink: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ffe27a;
  box-shadow: 0 0 10px rgba(232, 198, 107, 0.9);
  animation: cultPulse 1.2s ease-in-out infinite;
}

@keyframes cultPulse {
  0%,
  100% {
    opacity: 0.45;
    transform: scale(0.8);
  }

  50% {
    opacity: 1;
    transform: scale(1.15);
  }
}

.cult-name {
  font-family: var(--font-display);
  font-size: 15px;
  letter-spacing: 2px;
  color: var(--gold);
  text-shadow: 0 0 12px rgba(232, 198, 107, 0.5);
}

.cult-info {
  font-size: 12px;
  letter-spacing: 1px;
  color: rgba(233, 238, 251, 0.75);
}

.cult-exit {
  margin-left: auto;
  padding: 7px 18px;
  border-radius: 9px;
  border: 1px solid rgba(232, 198, 107, 0.45);
  background: rgba(232, 198, 107, 0.12);
  color: var(--gold);
  font-family: inherit;
  font-size: 13px;
  letter-spacing: 2px;
  cursor: pointer;
  transition: 0.22s;
}

.cult-exit:hover {
  color: #1a1023;
  background: linear-gradient(100deg, var(--gold), #ffe9a8 60%, var(--cyan));
  border-color: transparent;
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

/* 修炼中：星球下方文字金光提示 */
.p-left.on {
  fill: #ffe9a8;
}

/* 奇遇之星：标签用青色区分，古神残星带金色呼吸 */
.ep-tag {
  fill: rgba(111, 233, 255, 0.85);
}

/* 奇遇之星基础提亮：底部两星贴近星图暗角，避免被背景薄纱压得过暗 */
.planet.ep .p-img {
  filter: brightness(1.9) saturate(1.3);
}

.planet.ep:hover .p-img {
  filter: brightness(2.1) saturate(1.4);
}

/* 古神残星：在本就明亮的基础上做金色呼吸 */
.planet.ep.god .p-img {
  animation: godBreath 3.2s ease-in-out infinite;
}

@keyframes godBreath {
  0%,
  100% {
    filter: brightness(1.75) saturate(1.25);
  }

  50% {
    filter: brightness(2.15) saturate(1.4);
  }
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

/* ---------- 奇遇抉择弹窗 ---------- */
.ev-modal {
  width: min(480px, calc(100vw - 48px));
}

.ev-tag {
  font-size: 12px;
  letter-spacing: 3px;
  color: rgba(111, 233, 255, 0.85);
}

.ev-desc {
  margin-top: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  background: rgba(8, 9, 22, 0.55);
  border: 1px solid rgba(111, 233, 255, 0.14);
  font-size: 13px;
  letter-spacing: 1.5px;
  line-height: 1.9;
  color: rgba(233, 238, 251, 0.75);
}

.ev-opts {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 16px;
}

.ev-btn {
  padding: 12px 16px;
  border-radius: 11px;
  border: 1px solid rgba(111, 233, 255, 0.3);
  background: linear-gradient(160deg, rgba(58, 42, 107, 0.32), rgba(8, 9, 22, 0.6));
  color: #e9eefb;
  font-family: inherit;
  font-size: 14px;
  letter-spacing: 2px;
  cursor: pointer;
  transition: 0.22s;
}

.ev-btn:hover {
  transform: translateY(-1px);
  border-color: rgba(232, 198, 107, 0.6);
  color: var(--gold);
  box-shadow: 0 0 20px rgba(232, 198, 107, 0.18);
}

.ev-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
  border-color: rgba(111, 233, 255, 0.18);
  color: rgba(233, 238, 251, 0.5);
}

.ev-btn-leave {
  border-color: rgba(111, 233, 255, 0.22);
  color: rgba(233, 238, 251, 0.7);
}

.ev-btn-leave:hover {
  border-color: rgba(111, 233, 255, 0.5);
  color: rgba(111, 233, 255, 0.95);
  box-shadow: 0 0 16px rgba(111, 233, 255, 0.15);
}

.pc-left {
  margin-top: 12px;
  font-size: 12px;
  letter-spacing: 1px;
  color: rgba(232, 198, 107, 0.85);
}

.ev-result {
  margin-top: 16px;
  font-size: 14px;
  letter-spacing: 1.5px;
  line-height: 1.9;
  color: var(--gold);
}

.ev-bar {
  border-color: rgba(111, 233, 255, 0.42);
  background: linear-gradient(100deg, rgba(111, 233, 255, 0.12), rgba(58, 42, 107, 0.3));
  box-shadow: 0 0 18px rgba(111, 233, 255, 0.15);
}

.ev-bar .cult-dot {
  background: #8affc4;
  box-shadow: 0 0 10px rgba(138, 255, 196, 0.9);
}

.ev-bar .cult-name {
  color: rgba(111, 233, 255, 0.95);
  text-shadow: 0 0 12px rgba(111, 233, 255, 0.45);
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
