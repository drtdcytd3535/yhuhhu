<template>
  <section class="market">
    <!-- ================= 头部：返回 + 标题 + 灵石 ================= -->
    <div class="market-head">
      <button class="market-back" @click="emit('back')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
          <path d="M15 5 8 12l7 7" />
        </svg>
        返回仙门
      </button>
      <div class="market-title">
        <h2>天宝坊市</h2>
        <p>功法 · 丹药 · 法宝，一市尽藏 · 灵石为聘</p>
      </div>
      <div class="market-stones">
        <p class="stones-num">◈ {{ cave.stones }}</p>
        <p class="stones-label">灵石</p>
      </div>
    </div>

    <!-- ================= 坊市三阁：功法阁 / 丹阁 / 万宝阁 ================= -->
    <div class="market-tabs">
      <button
        v-for="t in MARKET_TABS"
        :key="t.key"
        class="market-tab"
        :class="{ on: tab === t.key }"
        @click="switchTab(t.key)"
      >
        {{ t.label }}
        <em>{{ t.desc }}</em>
      </button>
    </div>

    <!-- 功法阁 / 丹阁：以灵石购入功法与丹药（点开详情后照旧购买） -->
    <template v-if="tab !== 'wanbao'">
      <div v-if="loading" class="market-empty">坊市清点货品中…</div>

      <div v-else-if="items.length" class="market-grid">
        <article
          v-for="c in items"
          :key="c.id"
          class="ware-card"
          @click="emit('open', c.id)"
        >
          <span class="ware-tag">{{ c.tag || c.categoryName }}</span>
          <span v-if="c.price > 0" class="ware-price">◈ {{ c.price }}</span>
          <h3 class="ware-name">{{ c.title }}</h3>
          <p class="ware-desc">{{ c.summary }}</p>
          <div class="ware-foot">
            <span>{{ c.categoryPath }}</span>
            <span>✦ {{ fmtHot(c.hot) }}</span>
          </div>
        </article>
      </div>
      <p v-else class="market-empty">此阁货架空空，道友稍后再来。</p>

      <div v-if="pages > 1" class="market-pager">
        <button :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
        <span>第 {{ page }} / {{ pages }} 页</span>
        <button :disabled="page >= pages" @click="goPage(page + 1)">下一页</button>
      </div>
    </template>

    <!-- 万宝阁：原商店整章搬入（购入 / 佩戴 / 卸下，功能不变） -->
    <ShopPanel
      v-else
      :username="username"
      embedded
      @toast="forwardToast"
    />
  </section>
</template>

<script setup>
import { ref, watch } from 'vue'
import ShopPanel from './ShopPanel.vue'
import { cave } from '../composables/useCave.js'

const props = defineProps({
  username: { type: String, default: '道友' },
  /** 顶级分类（含子分类），来自 /api/home，用于定位「功法」「丹药」两阁 */
  categories: { type: Array, default: () => [] }
})
const emit = defineEmits(['back', 'open', 'toast'])

/** 坊市三阁：功法阁与丹阁按顶级分类取货，万宝阁沿用原商店 */
const MARKET_TABS = [
  { key: 'method', label: '功法阁', desc: '以灵石购入功法', category: '功法' },
  { key: 'pill', label: '丹阁', desc: '以灵石购入丹药', category: '丹药' },
  { key: 'wanbao', label: '万宝阁', desc: '法宝 · 修炼加成', category: '' }
]

const tab = ref('method')
const items = ref([])
const pages = ref(1)
const page = ref(1)
const loading = ref(false)
const PAGE_SIZE = 9

/** 热度千分位化简写：98000 → 9.8万（与首页展示一致） */
function fmtHot(n) {
  const v = Number(n || 0)
  if (v >= 10000) return (v / 10000).toFixed(1).replace(/\.0$/, '') + '万'
  return String(v)
}

function forwardToast(msg) {
  emit('toast', msg)
}

function switchTab(key) {
  if (tab.value === key) return
  tab.value = key
  page.value = 1
  loadGoods()
}

function goPage(p) {
  page.value = p
  loadGoods()
}

/** 按当前页签对应的顶级分类拉取货品列表 */
async function loadGoods() {
  const t = MARKET_TABS.find((x) => x.key === tab.value)
  if (!t || !t.category) return
  const top = (props.categories || []).find((c) => c.name === t.category)
  if (!top) {
    items.value = []
    pages.value = 1
    return
  }
  loading.value = true
  try {
    const res = await fetch(`/api/search?categoryId=${top.id}&sort=hot&page=${page.value}&size=${PAGE_SIZE}`)
    const body = await res.json().catch(() => null)
    if (body?.success) {
      items.value = body.data?.items || []
      pages.value = body.data?.pages || 1
    } else {
      items.value = []
      pages.value = 1
      if (body?.message) emit('toast', body.message)
    }
  } catch {
    items.value = []
    pages.value = 1
    emit('toast', '仙门暂时无应，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 首页分类异步到位后再取货（含首次进入坊市的场景）
watch(
  () => props.categories,
  (list) => {
    if (list && list.length && tab.value !== 'wanbao') loadGoods()
  },
  { immediate: true }
)
</script>

<style scoped>
.market {
  width: min(1080px, 94vw);
  margin: 0 auto;
  padding: 28px 0 60px;
}

/* ---------- 头部 ---------- */
.market-head {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 18px;
}

.market-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid var(--mist);
  border-radius: 999px;
  background: rgba(10, 12, 30, 0.55);
  color: #cfd8f2;
  font-size: 14px;
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;
}
.market-back svg { width: 16px; height: 16px; }
.market-back:hover { border-color: var(--gold); color: var(--gold); }

.market-title { flex: 1; }
.market-title h2 {
  font-family: var(--font-display);
  font-size: 30px;
  letter-spacing: 4px;
  color: var(--gold);
}
.market-title p { margin-top: 4px; font-size: 13px; color: #93a0c4; }

.market-stones { text-align: right; }
.stones-num {
  font-size: 22px;
  color: var(--cyan);
  text-shadow: 0 0 12px rgba(111, 233, 255, 0.45);
}
.stones-label { font-size: 12px; color: #93a0c4; }

/* ---------- 三阁页签 ---------- */
.market-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 22px;
}

.market-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 14px 10px;
  border: 1px solid var(--mist);
  border-radius: 14px;
  background: linear-gradient(160deg, rgba(24, 20, 56, 0.72), rgba(9, 10, 26, 0.85));
  color: #cfd8f2;
  font-family: inherit;
  font-size: 17px;
  letter-spacing: 3px;
  cursor: pointer;
  transition: border-color 0.22s, color 0.22s, box-shadow 0.22s;
}
.market-tab em {
  font-style: normal;
  font-size: 12px;
  letter-spacing: 1px;
  color: #7d89ad;
}
.market-tab:hover { border-color: rgba(111, 233, 255, 0.5); color: var(--cyan); }
.market-tab.on {
  border-color: rgba(232, 198, 107, 0.75);
  color: var(--gold);
  box-shadow: 0 0 18px rgba(232, 198, 107, 0.14);
}
.market-tab.on em { color: rgba(232, 198, 107, 0.7); }

/* ---------- 货品网格 ---------- */
.market-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.ware-card {
  position: relative;
  padding: 20px 18px 14px;
  border: 1px solid var(--mist);
  border-radius: 14px;
  background: linear-gradient(160deg, rgba(24, 20, 56, 0.72), rgba(9, 10, 26, 0.85));
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.25s, transform 0.25s, box-shadow 0.25s;
}
.ware-card:hover {
  border-color: rgba(232, 198, 107, 0.55);
  transform: translateY(-3px);
  box-shadow: 0 14px 34px rgba(0, 0, 0, 0.45);
}

.ware-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  letter-spacing: 2px;
  color: var(--cyan);
  background: rgba(111, 233, 255, 0.1);
  border: 1px solid rgba(111, 233, 255, 0.3);
}

.ware-price {
  position: absolute;
  top: 16px;
  right: 16px;
  font-size: 14px;
  color: var(--cyan);
  text-shadow: 0 0 10px rgba(111, 233, 255, 0.35);
}

.ware-name {
  margin-top: 10px;
  font-family: var(--font-display);
  font-size: 19px;
  letter-spacing: 2px;
  color: #f2e6c8;
  font-weight: 400;
}
.ware-desc {
  margin-top: 8px;
  min-height: 40px;
  font-size: 13px;
  line-height: 1.7;
  color: #a7b2d4;
}
.ware-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed rgba(160, 190, 230, 0.14);
  font-size: 12px;
  color: #7d89ad;
}

/* ---------- 空态 / 翻页 ---------- */
.market-empty {
  padding: 46px 0;
  text-align: center;
  font-size: 14px;
  letter-spacing: 2px;
  color: #6f7a9c;
}

.market-pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 22px;
  font-size: 13px;
  color: #93a0c4;
}
.market-pager button {
  padding: 6px 16px;
  border: 1px solid var(--mist);
  border-radius: 999px;
  background: rgba(10, 12, 30, 0.55);
  color: #cfd8f2;
  font-family: inherit;
  font-size: 13px;
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;
}
.market-pager button:hover:not(:disabled) { border-color: var(--gold); color: var(--gold); }
.market-pager button:disabled { opacity: 0.35; cursor: not-allowed; }
</style>
