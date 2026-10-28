<template>
  <div class="home">
    <!-- ================= 主页背景（等比铺满视口） ================= -->
    <div class="home-bg" aria-hidden="true">
      <img class="home-bg-img" src="../assets/hhh.jpg" alt="" draggable="false" />
      <div class="home-bg-veil"></div>
    </div>

    <!-- ================= 顶部导航栏 ================= -->
    <header class="nav">
      <div class="nav-inner">
        <!-- 品牌 -->
        <div class="logo" @click="backHome(); toast('已在仙门首页')">
          <span class="seal-mini">仙</span>
          <span class="logo-text">
            <b>仙逆</b>
            <i>XIAN&nbsp;NI</i>
          </span>
        </div>

        <!-- 主导航 -->
        <nav class="menu">
          <a
            v-for="m in menus"
            :key="m.key"
            class="menu-item"
            :class="{ active: activeMenu === m.key, external: m.href }"
            :title="m.href ? '前往腾讯视频观看《仙逆》动漫' : m.label"
            @click="goMenu(m)"
          >
            {{ m.label }}
            <svg v-if="m.href" class="ext" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M7 17 17 7" />
              <path d="M9 7h8v8" />
            </svg>
          </a>
        </nav>

        <!-- 用户区 -->
        <div class="nav-right">
          <!-- 后台管理系统入口 -->
          <button class="admin-entry" title="前往仙门执事堂（后台管理）" @click="emit('admin')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <rect x="3" y="3" width="7.5" height="9" rx="1.6" />
              <rect x="13.5" y="3" width="7.5" height="5.5" rx="1.6" />
              <rect x="13.5" y="11.5" width="7.5" height="9.5" rx="1.6" />
              <rect x="3" y="15" width="7.5" height="6" rx="1.6" />
            </svg>
            后台管理
          </button>

          <button class="icon-btn" title="搜索" @click="focusSearch">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7">
              <circle cx="11" cy="11" r="7" />
              <line x1="16.5" y1="16.5" x2="21" y2="21" />
            </svg>
          </button>

          <div class="user" :class="{ open: userOpen }">
            <button class="user-btn" @click="userOpen = !userOpen">
              <span class="avatar">{{ avatarText }}</span>
              <span class="uname">{{ username }}</span>
              <svg class="chev" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <polyline points="6 9 12 15 18 9" />
              </svg>
            </button>

            <transition name="drop">
              <div v-if="userOpen" class="dropdown">
                <div class="dd-head">
                  <span class="avatar big">{{ avatarText }}</span>
                  <div>
                    <p class="dd-name">{{ username }}</p>
                    <p class="dd-realm">{{ realmText }}</p>
                    <p class="dd-stone">◈ 灵石 {{ cave.stones }}</p>
                  </div>
                </div>
                <a class="dd-item" @click="enterCave()">我的洞府</a>
                <a class="dd-item" @click="soon('道号设置')">道号设置</a>
                <a class="dd-item" @click="enterCave()">灵石账册</a>
                <div class="dd-split"></div>
                <a class="dd-item danger" @click="onLogout">褪去此身 · 退出</a>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </header>

    <!-- ================= 仙门首页 ================= -->
    <template v-if="view === 'home'">
      <!-- 搜索栏 -->
      <section class="search-sec">
      <p class="slogan">逆天改命 · 问道长生</p>
      <h1 class="s-title">寻你所欲，仙途无阻</h1>

      <form class="search-bar" @submit.prevent="doSearch()">
        <div class="type-select">
          <select v-model="searchType" @change="onTypeChange">
            <option value="全部">全部</option>
            <option v-for="c in topCategories" :key="c.id" :value="c.name">{{ c.name }}</option>
          </select>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <polyline points="6 9 12 15 18 9" />
          </svg>
        </div>

        <input
          ref="searchInput"
          v-model="keyword"
          type="text"
          placeholder="检索功法、丹药、道友或洞天福地…"
          @keyup.enter="doSearch()"
        />

        <button type="submit" class="search-btn" :disabled="loading">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <circle cx="11" cy="11" r="7" />
            <line x1="16.5" y1="16.5" x2="21" y2="21" />
          </svg>
          {{ loading ? '检索中' : '搜 索' }}
        </button>
      </form>

      <div class="hots">
        <span class="hots-label">热搜</span>
        <button v-for="h in hots" :key="h" class="hot-chip" @click="quickSearch(h)">{{ h }}</button>
      </div>
    </section>

    <!-- ================= 分级分类导航 ================= -->
    <section class="cate-sec">
      <div class="cate-head">
        <span class="cate-label">分级分类</span>
        <button class="cate-reset" :class="{ on: !activeTopId && !activeCategoryId }" @click="resetCategory">
          全部
        </button>
      </div>

      <div class="cate-tops">
        <button
          v-for="c in topCategories"
          :key="c.id"
          class="cate-top"
          :class="{ on: activeTopId === c.id }"
          @click="pickTop(c)"
        >
          <span class="cate-ico">{{ c.icon || '·' }}</span>
          {{ c.name }}
          <em class="cate-count">{{ c.count }}</em>
        </button>
      </div>

      <transition name="fold">
        <div v-if="currentTop && currentTop.children && currentTop.children.length" class="cate-subs">
          <button
            v-for="s in currentTop.children"
            :key="s.id"
            class="cate-sub"
            :class="{ on: activeCategoryId === s.id }"
            @click="pickSub(s)"
          >
            {{ s.name }}
            <em class="cate-count">{{ s.count }}</em>
          </button>
        </div>
      </transition>
    </section>

    <!-- ================= 概览 ================= -->
    <section class="stats">
      <div class="stat" v-for="s in stats" :key="s.key || s.label">
        <span class="stat-ico">{{ s.ico }}</span>
        <div>
          <p class="stat-val">{{ s.value }}</p>
          <p class="stat-label">{{ s.label }}</p>
        </div>
      </div>
    </section>

    <!-- ================= 主体内容 ================= -->
    <section class="main-grid">
      <div class="col-left">
        <!-- 检索结果视图 -->
        <template v-if="searchResult">
          <div class="sec-head">
            <h2>检索结果</h2>
            <a @click="backToRecommend">← 返回荐读</a>
          </div>

          <div class="result-bar">
            <span class="rb-text">
              <b class="rb-kw">{{ searchResult.keyword || '全部分类' }}</b>
              <template v-if="searchResult.categoryPath"> · {{ searchResult.categoryPath }}</template>
              共 <b>{{ searchResult.total }}</b> 条
            </span>
            <div class="sorts">
              <button
                v-for="s in sorts"
                :key="s.key"
                class="sort-btn"
                :class="{ on: sortKey === s.key }"
                @click="changeSort(s.key)"
              >
                {{ s.label }}
              </button>
            </div>
          </div>

          <div v-if="searchResult.items.length" class="cards">
            <article
              v-for="c in searchResult.items"
              :key="c.id"
              class="card"
              @click="openDetail(c.id)"
            >
              <span class="card-tag">{{ c.tag || c.categoryName }}</span>
              <h3>{{ c.title }}</h3>
              <p class="card-desc">{{ c.summary }}</p>
              <div class="card-foot">
                <span class="author">{{ c.categoryPath }}</span>
                <span class="hot">✦ {{ fmtHot(c.hot) }}</span>
              </div>
            </article>
          </div>
          <p v-else class="empty">
            此界暂无相符之物，道友可换个说法，或将检索范围改为「全部」再寻。
          </p>

          <div v-if="searchResult.pages > 1" class="pager">
            <button class="pg" :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
            <span class="pg-info">第 {{ page }} / {{ searchResult.pages }} 页</span>
            <button class="pg" :disabled="page >= searchResult.pages" @click="goPage(page + 1)">下一页</button>
          </div>
        </template>

        <!-- 荐读视图 -->
        <template v-else>
          <div class="sec-head">
            <h2>荐读{{ currentTop ? ' · ' + currentTop.name : '' }}</h2>
            <a @click="browseCategory">按分类浏览 →</a>
          </div>
          <div class="cards">
            <article
              v-for="c in recommendList"
              :key="c.id"
              class="card"
              @click="openDetail(c.id)"
            >
              <span class="card-tag">{{ c.tag || c.categoryName }}</span>
              <h3>{{ c.title }}</h3>
              <p class="card-desc">{{ c.summary }}</p>
              <div class="card-foot">
                <span class="author">{{ c.author }}</span>
                <span class="hot">✦ {{ fmtHot(c.hot) }}</span>
              </div>
            </article>
          </div>
        </template>
      </div>

      <aside class="col-right">
        <div class="panel">
          <div class="panel-head"><h2>阅览榜</h2><span>今日</span></div>
          <ul class="rank">
            <li v-for="(r, i) in rank" :key="r.id" @click="openDetail(r.id)">
              <span class="rank-no" :class="{ top: i < 3 }">{{ i + 1 }}</span>
              <span class="rank-name">{{ r.name }}</span>
              <span class="rank-realm">{{ fmtHot(r.hot) }}</span>
            </li>
          </ul>
          <p v-if="!rank.length" class="empty small">榜上尚空</p>
        </div>

        <div class="panel">
          <div class="panel-head"><h2>宗门公告</h2><span>仙历</span></div>
          <ul class="notice">
            <li v-for="n in notices" :key="n.id">
              <span class="dot"></span>
              <p>{{ n.text }}</p>
              <span class="date">{{ n.date }}</span>
            </li>
          </ul>
          <p v-if="!notices.length" class="empty small">暂无公告</p>
        </div>
      </aside>
    </section>
    </template>

    <!-- ================= 我的洞府 / 修炼 ================= -->
    <CultivatePanel
      v-else-if="view === 'train'"
      :username="username"
      @back="backHome"
      @toast="toast"
    />

    <CavePanel
      v-else
      :username="username"
      @back="backHome"
      @open="openDetail"
      @toast="toast"
    />

    <!-- ================= 页脚 ================= -->
    <footer class="foot">
      <span class="foot-seal">仙逆</span>
      <p>仙门立于云端 · 道友共此长生</p>
      <p class="copy">© 2026 仙逆 · 问道长生　|　国风演示项目</p>
    </footer>

    <!-- ================= 详情弹窗 ================= -->
    <transition name="fade">
      <div v-if="detail" class="mask" @click.self="closeDetail">
        <div class="detail">
          <button class="dt-close" @click="closeDetail">✕</button>
          <p class="dt-path">{{ detail.categoryPath }}</p>
          <h3 class="dt-title">{{ detail.title }}</h3>
          <p class="dt-summary">{{ detail.summary }}</p>
          <div class="dt-tags">
            <span v-for="t in detail.tags" :key="t" class="dt-tag">{{ t }}</span>
          </div>
          <div class="dt-body">{{ detail.content }}</div>

          <div class="dt-acts">
            <button class="dt-act" :class="{ on: isFav(detail.id) }" @click="toggleFav(detail)">
              {{ isFav(detail.id) ? '★ 已藏于阁' : '☆ 收入藏书阁' }}
            </button>
            <button class="dt-act ghost" @click="closeDetail(); enterCave()">我的洞府 →</button>
          </div>

          <div class="dt-foot">
            <span>出处 · {{ detail.author }}</span>
            <span>热度 {{ fmtHot(detail.hot) }}</span>
            <span>查阅 {{ detail.views }}</span>
          </div>
        </div>
      </div>
    </transition>

    <!-- 气泡提示 -->
    <div class="toast" :class="{ show: toastShow }">{{ toastMsg }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import CavePanel from './CavePanel.vue'
import CultivatePanel from './CultivatePanel.vue'
import { cave, realm, initCave, clearCave, isFav, addFav, removeFav, pushHistory } from '../composables/useCave.js'

const props = defineProps({
  username: { type: String, default: '道友' }
})
const emit = defineEmits(['logout', 'admin'])

/* ---------------- 视图：仙门首页 / 修炼 / 我的洞府 ---------------- */
const view = ref('home')

function enterCave() {
  view.value = 'cave'
  activeMenu.value = 'mine'
  userOpen.value = false
  window.scrollTo({ top: 0 })
}

/** 修炼台：三课小游戏，所得修为直接入洞府账 */
function enterTrain() {
  view.value = 'train'
  activeMenu.value = 'train'
  userOpen.value = false
  window.scrollTo({ top: 0 })
}

function backHome() {
  view.value = 'home'
  activeMenu.value = 'home'
  window.scrollTo({ top: 0 })
}

/* ---------------- 导航 ---------------- */
// 腾讯视频《仙逆》动漫 · 专辑页（点「动漫」直接跳转这里）
const ANIME_URL = 'https://v.qq.com/x/cover/mzc00200aaogpgh.html'

const menus = [
  { key: 'home', label: '首页' },
  { key: 'method', label: '功法阁', type: '功法' },
  { key: 'pill', label: '丹阁', type: '丹药' },
  { key: 'cave', label: '洞天福地', type: '洞天' },
  { key: 'dao', label: '论道台', type: '论道' },
  { key: 'train', label: '修炼' },
  { key: 'mine', label: '我的洞府' },
  { key: 'anime', label: '动漫', href: ANIME_URL }
]
const activeMenu = ref('home')
const userOpen = ref(false)

function goMenu(m) {
  // 外链：新标签页打开腾讯视频《仙逆》动漫，当前仙门不受影响
  if (m.href) {
    window.open(m.href, '_blank', 'noopener,noreferrer')
    toast('正在开启腾讯视频 · 《仙逆》动漫')
    return
  }
  // 我的洞府：切到洞府视图
  if (m.key === 'mine') {
    enterCave()
    return
  }
  // 修炼：切到修炼台
  if (m.key === 'train') {
    enterTrain()
    toast('已入「修炼」 · 三课可修')
    return
  }
  if (m.key === 'home') {
    backHome()
    toast('已归仙门首页')
    return
  }
  // 功法阁 / 丹阁 / 洞天福地 / 论道台 → 回到首页视图并按对应顶级分类浏览
  if (m.type) {
    const top = topCategories.value.find((c) => c.name === m.type)
    if (top) {
      view.value = 'home'
      activeMenu.value = m.key
      searchResult.value = null
      pickTop(top, false)
      loadRecommend()
      toast(`已入「${m.label}」`)
      return
    }
  }
  // 其余世界尚未开辟
  toast(`「${m.label}」此界尚未开辟`)
}

/* ---------------- 后端数据 ---------------- */
const loading = ref(false)
const stats = ref([])
const categories = ref([])
const recommend = ref([])
const rank = ref([])
const notices = ref([])
const hots = ref([])

/** 顶级分类（含子分类与数量） */
const topCategories = computed(() => categories.value)
/** 当前选中的顶级分类 */
const currentTop = computed(() => categories.value.find((c) => c.id === activeTopId.value) || null)

/* ---------------- 分类导航状态 ---------------- */
const activeTopId = ref(null)
const activeCategoryId = ref(null)

/* ---------------- 搜索状态 ---------------- */
const searchInput = ref(null)
const keyword = ref('')
const searchType = ref('全部')
const searchResult = ref(null)
const page = ref(1)
const size = 9
const sortKey = ref('hot')
const sorts = [
  { key: 'hot', label: '热度' },
  { key: 'new', label: '最新' },
  { key: 'view', label: '查阅' }
]

/** 荐读视图：选中分类时展示该分类下的内容，否则展示后端荐读 */
const recommendList = computed(() => recommend.value)

/** 统一请求封装：返回 data 字段，失败时提示 */
async function api(path) {
  try {
    const res = await fetch(path)
    if (!res.ok) throw new Error('HTTP ' + res.status)
    const body = await res.json()
    if (!body.success) {
      toast(body.message || '仙门暂无回应')
      return null
    }
    return body.data
  } catch (e) {
    toast('仙门暂时无应，请稍后再试')
    return null
  }
}

/** 拉取首页聚合数据 */
async function loadHome() {
  const data = await api('/api/home')
  if (!data) return
  stats.value = data.stats || []
  categories.value = data.categories || []
  recommend.value = data.recommended || []
  rank.value = data.rank || []
  notices.value = data.notices || []
}

/** 拉取热搜词 */
async function loadHots() {
  const data = await api('/api/search/hots?limit=6')
  if (data) hots.value = data
}

/* ---------------- 分级分类 ---------------- */
function pickTop(c, runSearch = true) {
  activeTopId.value = c.id
  activeCategoryId.value = null
  searchType.value = c.name
  if (runSearch) doSearch(true)
}

function pickSub(s) {
  activeCategoryId.value = s.id
  doSearch(true)
}

function resetCategory() {
  activeTopId.value = null
  activeCategoryId.value = null
  searchType.value = '全部'
  searchResult.value = null
  keyword.value = ''
  page.value = 1
  activeMenu.value = 'home'
}

/** 搜索栏上的大类下拉变化：同步分类导航 */
function onTypeChange() {
  activeCategoryId.value = null
  const top = topCategories.value.find((c) => c.name === searchType.value)
  activeTopId.value = top ? top.id : null
}

/** 按当前选中分类浏览 */
function browseCategory() {
  if (!activeTopId.value && !activeCategoryId.value) {
    toast('请先在上方选择一个分类')
    return
  }
  doSearch(true)
}

/* ---------------- 检索 ---------------- */
async function doSearch(ignoreKeyword = false) {
  const kw = keyword.value.trim()
  const hasCategory = !!(activeCategoryId.value || activeTopId.value)
  if (!kw && !hasCategory && !ignoreKeyword) {
    toast('请先写下欲寻之物')
    searchInput.value?.focus()
    return
  }
  if (loading.value) return

  page.value = 1
  await fetchResults()
}

async function fetchResults() {
  loading.value = true
  const params = new URLSearchParams()
  if (keyword.value.trim()) params.set('keyword', keyword.value.trim())
  if (activeCategoryId.value) {
    params.set('categoryId', String(activeCategoryId.value))
  } else if (activeTopId.value) {
    params.set('categoryId', String(activeTopId.value))
  } else if (searchType.value && searchType.value !== '全部') {
    params.set('type', searchType.value)
  }
  params.set('page', String(page.value))
  params.set('size', String(size))
  params.set('sort', sortKey.value)

  const data = await api('/api/search?' + params.toString())
  loading.value = false
  if (!data) return
  searchResult.value = data
  if (data.total === 0) {
    toast('未寻得相符之物')
  } else {
    toast(`寻得 ${data.total} 条✓`)
  }
}

function goPage(p) {
  page.value = p
  fetchResults()
}

function changeSort(k) {
  sortKey.value = k
  if (searchResult.value) fetchResults()
}

function quickSearch(w) {
  keyword.value = w
  doSearch()
}

function focusSearch() {
  // 洞府视图里没有搜索框：先回首页再聚焦
  if (view.value !== 'home') {
    backHome()
    setTimeout(() => searchInput.value?.focus(), 60)
  } else {
    searchInput.value?.focus()
  }
  toast('请输入检索之词')
}

function backToRecommend() {
  searchResult.value = null
  keyword.value = ''
  page.value = 1
  loadRecommend()
}

/** 按当前分类重新取荐读（未选分类时回到后端荐读） */
async function loadRecommend() {
  const cid = activeCategoryId.value || activeTopId.value
  if (!cid) {
    const data = await api('/api/search?sort=hot&size=6')
    if (data) recommend.value = data.items
    return
  }
  const data = await api(`/api/search?categoryId=${cid}&sort=hot&size=6`)
  if (data) recommend.value = data.items
}

/* ---------------- 详情 ---------------- */
const detail = ref(null)

async function openDetail(id) {
  const data = await api('/api/content/' + id)
  if (!data) return
  detail.value = data
  // 记入洞府足迹：首次研读另有修为与灵石进账
  const r = pushHistory({ id: data.id, title: data.title, categoryPath: data.categoryPath })
  if (r.first) toast(`首次研读 · 修为 +${r.exp}`)
}

function closeDetail() {
  detail.value = null
}

/* ---------------- 展示辅助 ---------------- */
const avatarText = computed(() => (props.username || '道').charAt(0))
/** 境界取自洞府修为，随研读、签到、收录藏书同步增长 */
const realmText = computed(() => `${realm.value.name}境 · 第 ${realm.value.level} 重`)

/* ---------------- 洞府：藏书收录 ---------------- */
function toggleFav(item) {
  if (!item || item.id == null) return
  if (isFav(item.id)) {
    removeFav(item.id)
    toast('已移出藏书阁')
    return
  }
  const r = addFav(item)
  toast(r.ok ? `已收入藏书阁 · 修为 +${r.exp}` : r.msg)
}

/** 热度千分位化简写：98000 → 9.8万 */
function fmtHot(n) {
  const v = Number(n || 0)
  if (v >= 10000) return (v / 10000).toFixed(1).replace(/\.0$/, '') + '万'
  return String(v)
}

/* ---------------- 提示 ---------------- */
const toastMsg = ref('')
const toastShow = ref(false)
let toastTimer

function toast(msg) {
  toastMsg.value = msg
  toastShow.value = true
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (toastShow.value = false), 2000)
}

function soon(name) {
  toast(`「${name}」正在开辟之中`)
}

/* ---------------- 退出 ---------------- */
function onLogout() {
  userOpen.value = false
  emit('logout')
}

/* ---------------- 全局点击收起下拉 / ESC 关弹窗 ---------------- */
function onDocClick(e) {
  if (!e.target.closest('.user')) userOpen.value = false
}

function onKeydown(e) {
  if (e.key === 'Escape') {
    if (detail.value) closeDetail()
    else userOpen.value = false
  }
}

onMounted(async () => {
  document.addEventListener('click', onDocClick)
  document.addEventListener('keydown', onKeydown)
  // 进入首页后允许纵向滚动
  document.body.classList.add('page-scroll')
  // 开辟（或载入）此道号的洞府
  initCave(props.username)
  // 开发态预览入口：?preview=cave 直达洞府、?preview=train 直达修炼台，仅开发环境生效
  if (import.meta.env.DEV) {
    const preview = new URLSearchParams(location.search).get('preview')
    if (preview === 'cave') view.value = 'cave'
    if (preview === 'train') {
      view.value = 'train'
      activeMenu.value = 'train'
    }
  }
  await Promise.all([loadHome(), loadHots()])
  toast(`✦ ${props.username} 道友，仙门已开 ✦`)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onDocClick)
  document.removeEventListener('keydown', onKeydown)
  clearTimeout(toastTimer)
  document.body.classList.remove('page-scroll')
  // 登出后仅清空内存态（数据库存档保留，换号登录不串数据）
  clearCave()
})
</script>

<style scoped>
.home {
  position: relative;
  z-index: 3;
  min-height: 100%;
  padding-bottom: 40px;
}

/* ================= 主页背景 ================= */
/* 固定铺满视口：无论窗口多宽多窄，都按 16:9 原图等比覆盖、不拉伸变形 */
.home-bg {
  position: fixed;
  inset: 0;
  z-index: -1; /* 落在 .home 内容之下，但仍在本层叠上下文内，盖住旧的星野/远山 */
  overflow: hidden;
  pointer-events: none;
  background: #05060c;
}

.home-bg-img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover; /* 等比铺满，多余部分裁切 */
  object-position: 50% 36%; /* 主角二人居中偏上，裁切时优先保留 */
  user-select: none;
  animation: homeBgIn 1.2s ease both;
}

/* 暗色纱罩：压住亮部，保证卡片与文字可读 */
.home-bg-veil {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(
      180deg,
      rgba(5, 6, 12, 0.74) 0%,
      rgba(5, 6, 12, 0.34) 20%,
      rgba(5, 6, 12, 0.46) 56%,
      rgba(5, 6, 12, 0.88) 100%
    ),
    radial-gradient(98% 82% at 50% 38%, rgba(6, 10, 12, 0.04), rgba(5, 6, 12, 0.55));
}

@keyframes homeBgIn {
  from {
    opacity: 0;
    transform: scale(1.04);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* ================= 导航栏 ================= */
.nav {
  position: sticky;
  top: 0;
  z-index: 20;
  background: linear-gradient(180deg, rgba(8, 9, 22, 0.92), rgba(8, 9, 22, 0.62));
  border-bottom: 1px solid rgba(111, 233, 255, 0.16);
  backdrop-filter: blur(14px);
}

.nav-inner {
  max-width: 1240px;
  margin: 0 auto;
  height: 68px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 28px;
}

/* 品牌 */
.logo {
  display: flex;
  align-items: center;
  gap: 11px;
  cursor: pointer;
  user-select: none;
  flex-shrink: 0;
}

.seal-mini {
  width: 38px;
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  font-family: 'Ma Shan Zheng', serif;
  font-size: 22px;
  color: var(--gold);
  background: linear-gradient(160deg, rgba(232, 198, 107, 0.2), rgba(111, 233, 255, 0.08));
  border: 1px solid rgba(232, 198, 107, 0.45);
  box-shadow: 0 0 18px rgba(232, 198, 107, 0.25);
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}

.logo-text b {
  font-family: 'Ma Shan Zheng', serif;
  font-size: 24px;
  letter-spacing: 5px;
  color: #fff;
  font-weight: 400;
  text-shadow: 0 0 16px rgba(111, 233, 255, 0.5);
}

.logo-text i {
  font-style: normal;
  font-size: 9px;
  letter-spacing: 3px;
  color: rgba(233, 238, 251, 0.42);
}

/* 菜单 */
.menu {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  min-width: 0;
  overflow-x: auto;
  scrollbar-width: none;
}

.menu::-webkit-scrollbar {
  display: none;
}

.menu-item {
  position: relative;
  padding: 8px 14px;
  font-size: 15px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.72);
  border-radius: 9px;
  cursor: pointer;
  white-space: nowrap;
  transition: 0.22s;
}

.menu-item:hover {
  color: var(--cyan);
  background: rgba(111, 233, 255, 0.07);
}

.menu-item.active {
  color: var(--gold);
}

.menu-item.active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 1px;
  width: 18px;
  height: 2px;
  border-radius: 2px;
  transform: translateX(-50%);
  background: linear-gradient(90deg, var(--gold), var(--cyan));
  box-shadow: 0 0 10px rgba(232, 198, 107, 0.8);
}

/* 外链菜单项（动漫 → 腾讯视频） */
.menu-item.external {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: rgba(232, 198, 107, 0.9);
  border: 1px solid rgba(232, 198, 107, 0.28);
  background: rgba(232, 198, 107, 0.06);
  padding: 7px 12px;
}

.menu-item.external:hover {
  color: #1a1023;
  background: linear-gradient(100deg, var(--gold), #ffe9a8 60%, var(--cyan));
  border-color: transparent;
  box-shadow:
    0 8px 22px rgba(232, 198, 107, 0.35),
    0 0 16px rgba(111, 233, 255, 0.25);
}

.menu-item .ext {
  width: 12px;
  height: 12px;
  opacity: 0.85;
  transition: 0.22s;
}

.menu-item.external:hover .ext {
  transform: translate(1px, -1px);
}

/* 用户区 */
.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

/* 后台管理入口 */
.admin-entry {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 13px;
  border-radius: 10px;
  border: 1px solid rgba(232, 198, 107, 0.32);
  background: rgba(232, 198, 107, 0.07);
  color: rgba(232, 198, 107, 0.92);
  font-family: inherit;
  font-size: 14px;
  letter-spacing: 2px;
  white-space: nowrap;
  cursor: pointer;
  transition: 0.22s;
}

.admin-entry svg {
  width: 15px;
  height: 15px;
}

.admin-entry:hover {
  color: #1a1023;
  background: linear-gradient(100deg, var(--gold), #ffe9a8 60%, var(--cyan));
  border-color: transparent;
  box-shadow:
    0 8px 22px rgba(232, 198, 107, 0.32),
    0 0 16px rgba(111, 233, 255, 0.25);
}


.icon-btn {
  width: 38px;
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  border: 1px solid rgba(160, 190, 230, 0.22);
  background: rgba(255, 255, 255, 0.04);
  color: rgba(233, 238, 251, 0.7);
  cursor: pointer;
  transition: 0.22s;
}

.icon-btn svg {
  width: 19px;
  height: 19px;
}

.icon-btn:hover {
  color: var(--cyan);
  border-color: rgba(111, 233, 255, 0.5);
  box-shadow: 0 0 16px rgba(111, 233, 255, 0.25);
}

.user {
  position: relative;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 5px 12px 5px 5px;
  border-radius: 30px;
  border: 1px solid rgba(160, 190, 230, 0.22);
  background: rgba(255, 255, 255, 0.04);
  color: #e9eefb;
  font-family: inherit;
  font-size: 14px;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.22s;
}

.user-btn:hover,
.user.open .user-btn {
  border-color: rgba(232, 198, 107, 0.6);
  box-shadow: 0 0 18px rgba(232, 198, 107, 0.22);
}

.avatar {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-family: 'Ma Shan Zheng', serif;
  font-size: 15px;
  color: #1a1023;
  background: linear-gradient(140deg, var(--gold), #ffe9a8 60%, var(--cyan));
  box-shadow: 0 0 12px rgba(232, 198, 107, 0.45);
}

.avatar.big {
  width: 40px;
  height: 40px;
  font-size: 20px;
}

.uname {
  max-width: 110px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chev {
  width: 14px;
  height: 14px;
  color: rgba(233, 238, 251, 0.5);
}

/* 下拉 */
.dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 10px);
  width: 210px;
  padding: 10px;
  border-radius: 14px;
  background: linear-gradient(160deg, rgba(20, 22, 46, 0.97), rgba(8, 9, 22, 0.98));
  border: 1px solid rgba(111, 233, 255, 0.25);
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(14px);
}

.dd-head {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 6px 6px 12px;
  border-bottom: 1px solid rgba(160, 190, 230, 0.14);
  margin-bottom: 6px;
}

.dd-name {
  font-size: 15px;
  color: #fff;
  letter-spacing: 1px;
}

.dd-realm {
  margin-top: 3px;
  font-size: 12px;
  color: rgba(233, 238, 251, 0.5);
}

.dd-stone {
  margin-top: 3px;
  font-size: 12px;
  letter-spacing: 1px;
  color: var(--gold);
}

.dd-item {
  display: block;
  padding: 9px 10px;
  border-radius: 9px;
  font-size: 14px;
  letter-spacing: 1px;
  color: rgba(233, 238, 251, 0.78);
  cursor: pointer;
  transition: 0.2s;
}

.dd-item:hover {
  color: var(--cyan);
  background: rgba(111, 233, 255, 0.08);
}

.dd-item.danger {
  color: rgba(255, 150, 150, 0.9);
}

.dd-item.danger:hover {
  background: rgba(255, 120, 120, 0.1);
  color: #ff9b9b;
}

.dd-split {
  height: 1px;
  margin: 6px 4px;
  background: rgba(160, 190, 230, 0.14);
}

.drop-enter-active,
.drop-leave-active {
  transition: 0.22s;
}

.drop-enter-from,
.drop-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* ================= 搜索区 ================= */
.search-sec {
  max-width: 1240px;
  margin: 0 auto;
  padding: 74px 24px 34px;
  text-align: center;
}

.slogan {
  font-size: 14px;
  letter-spacing: 10px;
  color: rgba(232, 198, 107, 0.85);
  margin-bottom: 16px;
}

.s-title {
  font-family: 'Ma Shan Zheng', serif;
  font-size: 46px;
  font-weight: 400;
  letter-spacing: 8px;
  color: #fff;
  text-shadow:
    0 0 26px rgba(111, 233, 255, 0.45),
    0 0 60px rgba(232, 198, 107, 0.3);
  margin-bottom: 34px;
}

.search-bar {
  max-width: 740px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-radius: 16px;
  background: linear-gradient(160deg, rgba(20, 22, 46, 0.72), rgba(8, 9, 22, 0.78));
  border: 1px solid rgba(111, 233, 255, 0.28);
  box-shadow:
    0 18px 50px rgba(0, 0, 0, 0.5),
    inset 0 0 26px rgba(58, 42, 107, 0.28);
  backdrop-filter: blur(14px);
  transition: 0.28s;
}

.search-bar:focus-within {
  border-color: var(--cyan);
  box-shadow:
    0 0 0 3px rgba(111, 233, 255, 0.14),
    0 18px 60px rgba(0, 0, 0, 0.55),
    0 0 34px rgba(111, 233, 255, 0.25);
}

.type-select {
  position: relative;
  flex-shrink: 0;
}

.type-select select {
  appearance: none;
  padding: 12px 34px 12px 16px;
  border: none;
  border-radius: 11px;
  background: rgba(255, 255, 255, 0.05);
  color: rgba(232, 198, 107, 0.95);
  font-family: inherit;
  font-size: 15px;
  letter-spacing: 2px;
  cursor: pointer;
  outline: none;
}

.type-select select option {
  background: #0b0c1c;
  color: #e9eefb;
}

.type-select svg {
  position: absolute;
  right: 12px;
  top: 50%;
  width: 14px;
  height: 14px;
  transform: translateY(-50%);
  color: rgba(232, 198, 107, 0.75);
  pointer-events: none;
}

.search-bar input {
  flex: 1;
  min-width: 0;
  padding: 12px 6px;
  border: none;
  background: transparent;
  color: #fff;
  font-family: inherit;
  font-size: 16px;
  letter-spacing: 1px;
  outline: none;
}

.search-bar input::placeholder {
  color: rgba(233, 238, 251, 0.4);
}

.search-btn {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 26px;
  border: none;
  border-radius: 11px;
  font-family: inherit;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 4px;
  color: #1a1023;
  background: linear-gradient(100deg, var(--gold), #ffe9a8 55%, var(--cyan));
  box-shadow:
    0 8px 26px rgba(232, 198, 107, 0.32),
    0 0 16px rgba(111, 233, 255, 0.28);
  cursor: pointer;
  transition: 0.25s;
}

.search-btn svg {
  width: 18px;
  height: 18px;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow:
    0 14px 36px rgba(232, 198, 107, 0.45),
    0 0 24px rgba(111, 233, 255, 0.42);
}

.search-btn:active {
  transform: translateY(0);
}

.search-btn:disabled {
  opacity: 0.72;
  cursor: wait;
  transform: none;
}

.hots {
  max-width: 740px;
  margin: 20px auto 0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
}

.hots-label {
  font-size: 13px;
  letter-spacing: 3px;
  color: rgba(232, 198, 107, 0.7);
}

.hot-chip {
  padding: 6px 14px;
  border-radius: 20px;
  border: 1px solid rgba(160, 190, 230, 0.22);
  background: rgba(255, 255, 255, 0.035);
  color: rgba(233, 238, 251, 0.7);
  font-family: inherit;
  font-size: 13px;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.22s;
}

.hot-chip:hover {
  color: var(--cyan);
  border-color: rgba(111, 233, 255, 0.55);
  background: rgba(111, 233, 255, 0.08);
  box-shadow: 0 0 16px rgba(111, 233, 255, 0.22);
}

/* ================= 分级分类导航 ================= */
.cate-sec {
  max-width: 1240px;
  margin: 0 auto 46px;
  padding: 18px 22px 20px;
  border-radius: 16px;
  background: linear-gradient(160deg, rgba(20, 22, 46, 0.6), rgba(8, 9, 22, 0.66));
  border: 1px solid rgba(111, 233, 255, 0.18);
  box-shadow: inset 0 0 26px rgba(58, 42, 107, 0.22);
  backdrop-filter: blur(10px);
}

.cate-head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
}

.cate-label {
  font-size: 13px;
  letter-spacing: 4px;
  color: rgba(232, 198, 107, 0.8);
}

.cate-reset {
  padding: 3px 12px;
  border-radius: 20px;
  border: 1px solid rgba(160, 190, 230, 0.22);
  background: rgba(255, 255, 255, 0.035);
  color: rgba(233, 238, 251, 0.68);
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.22s;
}

.cate-reset:hover,
.cate-reset.on {
  color: #1a1023;
  background: linear-gradient(100deg, var(--gold), #ffe9a8 60%, var(--cyan));
  border-color: transparent;
}

.cate-tops {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.cate-top {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 8px 16px;
  border-radius: 11px;
  border: 1px solid rgba(160, 190, 230, 0.2);
  background: rgba(255, 255, 255, 0.035);
  color: rgba(233, 238, 251, 0.82);
  font-family: inherit;
  font-size: 14px;
  letter-spacing: 2px;
  cursor: pointer;
  transition: 0.24s;
}

.cate-top:hover {
  border-color: rgba(111, 233, 255, 0.5);
  color: var(--cyan);
  box-shadow: 0 0 16px rgba(111, 233, 255, 0.2);
}

.cate-top.on {
  color: #1a1023;
  background: linear-gradient(100deg, var(--gold), #ffe9a8 60%, var(--cyan));
  border-color: transparent;
  box-shadow:
    0 8px 22px rgba(232, 198, 107, 0.32),
    0 0 16px rgba(111, 233, 255, 0.25);
}

.cate-ico {
  font-size: 15px;
}

.cate-count {
  font-style: normal;
  font-size: 11px;
  padding: 1px 7px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.1);
  color: inherit;
  opacity: 0.85;
}

.cate-top.on .cate-count {
  background: rgba(26, 16, 35, 0.18);
  color: #1a1023;
  opacity: 1;
}

/* 子分类 */
.cate-subs {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed rgba(160, 190, 230, 0.16);
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.cate-sub {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 13px;
  border-radius: 20px;
  border: 1px solid rgba(160, 190, 230, 0.18);
  background: rgba(255, 255, 255, 0.03);
  color: rgba(233, 238, 251, 0.7);
  font-family: inherit;
  font-size: 13px;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.22s;
}

.cate-sub:hover {
  color: var(--cyan);
  border-color: rgba(111, 233, 255, 0.5);
  background: rgba(111, 233, 255, 0.08);
}

.cate-sub.on {
  color: #1a1023;
  background: linear-gradient(100deg, var(--cyan), #b9f4ff 70%);
  border-color: transparent;
  box-shadow: 0 0 16px rgba(111, 233, 255, 0.3);
}

.fold-enter-active,
.fold-leave-active {
  transition: 0.24s;
}

.fold-enter-from,
.fold-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* ================= 概览 ================= */
.stats {
  max-width: 1240px;
  margin: 0 auto 56px;
  padding: 0 24px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border-radius: 14px;
  background: linear-gradient(160deg, rgba(20, 22, 46, 0.6), rgba(8, 9, 22, 0.66));
  border: 1px solid rgba(111, 233, 255, 0.18);
  box-shadow: inset 0 0 24px rgba(58, 42, 107, 0.22);
  backdrop-filter: blur(10px);
  transition: 0.25s;
}

.stat:hover {
  border-color: rgba(232, 198, 107, 0.45);
  transform: translateY(-2px);
}

.stat-ico {
  font-size: 20px;
  color: var(--gold);
  text-shadow: 0 0 14px rgba(232, 198, 107, 0.6);
}

.stat-val {
  font-family: 'Ma Shan Zheng', serif;
  font-size: 22px;
  color: #fff;
  letter-spacing: 2px;
}

.stat-label {
  margin-top: 3px;
  font-size: 12px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.5);
}

/* ================= 主体 ================= */
.main-grid {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 24px;
  align-items: start;
}

.sec-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 18px;
}

.sec-head h2 {
  position: relative;
  padding-left: 14px;
  font-size: 20px;
  font-weight: 400;
  letter-spacing: 5px;
  color: var(--gold);
}

.sec-head h2::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 3px;
  height: 18px;
  border-radius: 2px;
  transform: translateY(-50%);
  background: linear-gradient(180deg, var(--gold), var(--cyan));
  box-shadow: 0 0 10px rgba(111, 233, 255, 0.7);
}

.sec-head a {
  font-size: 13px;
  letter-spacing: 1px;
  color: rgba(111, 233, 255, 0.8);
  cursor: pointer;
}

.sec-head a:hover {
  text-decoration: underline;
}

/* 结果信息条 */
.result-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin: -6px 0 16px;
  padding: 10px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.035);
  border: 1px solid rgba(160, 190, 230, 0.14);
  font-size: 13px;
  color: rgba(233, 238, 251, 0.66);
}

.rb-kw {
  color: var(--cyan);
}

.rb-text b {
  color: var(--gold);
}

.sorts {
  display: flex;
  gap: 6px;
}

.sort-btn {
  padding: 4px 12px;
  border-radius: 20px;
  border: 1px solid rgba(160, 190, 230, 0.2);
  background: transparent;
  color: rgba(233, 238, 251, 0.65);
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.22s;
}

.sort-btn:hover {
  color: var(--cyan);
  border-color: rgba(111, 233, 255, 0.5);
}

.sort-btn.on {
  color: #1a1023;
  background: linear-gradient(100deg, var(--gold), #ffe9a8 60%, var(--cyan));
  border-color: transparent;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.card {
  position: relative;
  padding: 22px 20px 18px;
  border-radius: 16px;
  background: linear-gradient(160deg, rgba(20, 22, 46, 0.62), rgba(8, 9, 22, 0.7));
  border: 1px solid rgba(111, 233, 255, 0.18);
  box-shadow: inset 0 0 26px rgba(58, 42, 107, 0.24);
  backdrop-filter: blur(10px);
  cursor: pointer;
  overflow: hidden;
  transition: 0.28s;
}

.card::after {
  content: '';
  position: absolute;
  top: -60%;
  right: -40%;
  width: 130px;
  height: 130px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(232, 198, 107, 0.14), transparent 70%);
  opacity: 0;
  transition: 0.35s;
}

.card:hover {
  transform: translateY(-4px);
  border-color: rgba(232, 198, 107, 0.5);
  box-shadow:
    0 18px 44px rgba(0, 0, 0, 0.5),
    inset 0 0 30px rgba(58, 42, 107, 0.3);
}

.card:hover::after {
  opacity: 1;
}

.card-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  letter-spacing: 2px;
  color: var(--cyan);
  background: rgba(111, 233, 255, 0.1);
  border: 1px solid rgba(111, 233, 255, 0.3);
}

.card h3 {
  margin: 14px 0 10px;
  font-family: 'Ma Shan Zheng', serif;
  font-size: 21px;
  font-weight: 400;
  letter-spacing: 3px;
  color: #fff;
}

.card-desc {
  font-size: 13px;
  line-height: 1.75;
  color: rgba(233, 238, 251, 0.6);
  min-height: 44px;
}

.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid rgba(160, 190, 230, 0.12);
  font-size: 12px;
  letter-spacing: 1px;
}

.author {
  color: rgba(233, 238, 251, 0.5);
  max-width: 62%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot {
  color: rgba(232, 198, 107, 0.9);
}

/* 空态与分页 */
.empty {
  padding: 40px 0;
  text-align: center;
  font-size: 14px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.45);
}

.empty.small {
  padding: 14px 0;
  font-size: 12px;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-top: 24px;
}

.pg {
  padding: 7px 18px;
  border-radius: 10px;
  border: 1px solid rgba(160, 190, 230, 0.24);
  background: rgba(255, 255, 255, 0.04);
  color: rgba(233, 238, 251, 0.78);
  font-family: inherit;
  font-size: 13px;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.22s;
}

.pg:hover:not(:disabled) {
  color: var(--cyan);
  border-color: rgba(111, 233, 255, 0.5);
  box-shadow: 0 0 14px rgba(111, 233, 255, 0.22);
}

.pg:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.pg-info {
  font-size: 13px;
  color: rgba(233, 238, 251, 0.55);
  letter-spacing: 1px;
}

/* 侧栏 */
.col-right {
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: sticky;
  top: 88px;
}

.panel {
  padding: 20px;
  border-radius: 16px;
  background: linear-gradient(160deg, rgba(20, 22, 46, 0.62), rgba(8, 9, 22, 0.7));
  border: 1px solid rgba(111, 233, 255, 0.18);
  box-shadow: inset 0 0 26px rgba(58, 42, 107, 0.24);
  backdrop-filter: blur(10px);
}

.panel-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 14px;
}

.panel-head h2 {
  position: relative;
  padding-left: 12px;
  font-size: 17px;
  font-weight: 400;
  letter-spacing: 4px;
  color: var(--gold);
}

.panel-head h2::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 3px;
  height: 15px;
  border-radius: 2px;
  transform: translateY(-50%);
  background: linear-gradient(180deg, var(--gold), var(--cyan));
}

.panel-head span {
  font-size: 11px;
  letter-spacing: 2px;
  color: rgba(233, 238, 251, 0.4);
}

.rank {
  list-style: none;
}

.rank li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 6px;
  border-radius: 9px;
  font-size: 14px;
  transition: 0.2s;
  cursor: pointer;
}

.rank li:hover {
  background: rgba(111, 233, 255, 0.07);
}

.rank li + li {
  border-top: 1px solid rgba(160, 190, 230, 0.09);
}

.rank-no {
  width: 22px;
  height: 22px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 12px;
  color: rgba(233, 238, 251, 0.55);
  background: rgba(255, 255, 255, 0.06);
}

.rank-no.top {
  color: #1a1023;
  background: linear-gradient(140deg, var(--gold), #ffe9a8);
  box-shadow: 0 0 12px rgba(232, 198, 107, 0.5);
}

.rank-name {
  flex: 1;
  color: #e9eefb;
  letter-spacing: 1px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-realm {
  font-size: 12px;
  color: rgba(111, 233, 255, 0.85);
}

.notice {
  list-style: none;
}

.notice li {
  display: flex;
  align-items: flex-start;
  gap: 9px;
  padding: 10px 4px;
  font-size: 13px;
  line-height: 1.7;
  color: rgba(233, 238, 251, 0.68);
  cursor: pointer;
  transition: 0.2s;
}

.notice li:hover {
  color: rgba(111, 233, 255, 0.95);
}

.notice li + li {
  border-top: 1px solid rgba(160, 190, 230, 0.09);
}

.notice .dot {
  flex-shrink: 0;
  width: 5px;
  height: 5px;
  margin-top: 8px;
  border-radius: 50%;
  background: var(--gold);
  box-shadow: 0 0 8px rgba(232, 198, 107, 0.8);
}

.notice p {
  flex: 1;
}

.notice .date {
  flex-shrink: 0;
  font-size: 11px;
  color: rgba(233, 238, 251, 0.38);
}

/* ================= 详情弹窗 ================= */
.mask {
  position: fixed;
  inset: 0;
  z-index: 40;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(3, 4, 10, 0.72);
  backdrop-filter: blur(6px);
}

.detail {
  position: relative;
  width: min(620px, 100%);
  max-height: 82vh;
  overflow-y: auto;
  padding: 34px 32px 26px;
  border-radius: 20px;
  background: linear-gradient(160deg, rgba(20, 22, 46, 0.98), rgba(8, 9, 22, 0.99));
  border: 1px solid rgba(232, 198, 107, 0.35);
  box-shadow:
    0 30px 80px rgba(0, 0, 0, 0.7),
    inset 0 0 40px rgba(58, 42, 107, 0.3);
}

.dt-close {
  position: absolute;
  right: 16px;
  top: 14px;
  width: 32px;
  height: 32px;
  border-radius: 9px;
  border: 1px solid rgba(160, 190, 230, 0.22);
  background: rgba(255, 255, 255, 0.04);
  color: rgba(233, 238, 251, 0.7);
  font-size: 14px;
  cursor: pointer;
  transition: 0.2s;
}

.dt-close:hover {
  color: #ff9b9b;
  border-color: rgba(255, 150, 150, 0.5);
}

.dt-path {
  font-size: 12px;
  letter-spacing: 3px;
  color: rgba(111, 233, 255, 0.85);
  margin-bottom: 12px;
}

.dt-title {
  font-family: 'Ma Shan Zheng', serif;
  font-size: 32px;
  font-weight: 400;
  letter-spacing: 4px;
  color: #fff;
  text-shadow: 0 0 24px rgba(232, 198, 107, 0.35);
}

.dt-summary {
  margin-top: 12px;
  font-size: 14px;
  line-height: 1.9;
  color: rgba(233, 238, 251, 0.72);
}

.dt-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 16px 0 18px;
}

.dt-tag {
  padding: 3px 11px;
  border-radius: 20px;
  font-size: 11px;
  letter-spacing: 1px;
  color: var(--cyan);
  background: rgba(111, 233, 255, 0.09);
  border: 1px solid rgba(111, 233, 255, 0.3);
}

.dt-body {
  padding: 16px 18px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.035);
  border: 1px solid rgba(160, 190, 230, 0.14);
  font-size: 14px;
  line-height: 2;
  color: rgba(233, 238, 251, 0.86);
  white-space: pre-wrap;
}

/* 详情弹窗操作：收入藏书阁 / 前往洞府 */
.dt-acts {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.dt-act {
  padding: 9px 18px;
  border-radius: 11px;
  border: 1px solid rgba(232, 198, 107, 0.4);
  background: rgba(232, 198, 107, 0.1);
  color: var(--gold);
  font-family: inherit;
  font-size: 13px;
  letter-spacing: 2px;
  cursor: pointer;
  transition: 0.22s;
}

.dt-act:hover {
  background: rgba(232, 198, 107, 0.2);
  box-shadow: 0 0 16px rgba(232, 198, 107, 0.3);
}

.dt-act.on {
  color: #1a1023;
  background: linear-gradient(100deg, var(--gold), #ffe9a8 60%, var(--cyan));
  border-color: transparent;
}

.dt-act.ghost {
  border-color: rgba(111, 233, 255, 0.4);
  background: rgba(111, 233, 255, 0.08);
  color: var(--cyan);
}

.dt-act.ghost:hover {
  background: rgba(111, 233, 255, 0.16);
  box-shadow: 0 0 16px rgba(111, 233, 255, 0.28);
}

.dt-foot {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid rgba(160, 190, 230, 0.14);
  font-size: 12px;
  letter-spacing: 1px;
  color: rgba(233, 238, 251, 0.5);
}

.fade-enter-active,
.fade-leave-active {
  transition: 0.24s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ================= 页脚 ================= */
.foot {
  max-width: 1240px;
  margin: 64px auto 0;
  padding: 34px 24px 0;
  border-top: 1px solid rgba(160, 190, 230, 0.13);
  text-align: center;
}

.foot-seal {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  margin-bottom: 14px;
  border-radius: 9px;
  font-family: 'Ma Shan Zheng', serif;
  font-size: 19px;
  color: var(--gold);
  background: rgba(232, 198, 107, 0.1);
  border: 1px solid rgba(232, 198, 107, 0.4);
}

.foot p {
  font-size: 13px;
  letter-spacing: 3px;
  color: rgba(233, 238, 251, 0.5);
}

.copy {
  margin-top: 8px;
  font-size: 12px !important;
  letter-spacing: 1px !important;
  color: rgba(233, 238, 251, 0.3) !important;
}

/* ================= 提示 ================= */
.toast {
  position: fixed;
  top: 84px;
  left: 50%;
  z-index: 60;
  padding: 12px 22px;
  border-radius: 10px;
  background: rgba(8, 9, 22, 0.94);
  border: 1px solid var(--cyan);
  color: var(--cyan);
  letter-spacing: 2px;
  box-shadow: 0 0 24px rgba(111, 233, 255, 0.3);
  opacity: 0;
  transform: translateX(-50%) translateY(-14px);
  transition: 0.32s;
  pointer-events: none;
}

.toast.show {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

/* ================= 响应式 ================= */
@media (max-width: 1080px) {
  .main-grid {
    grid-template-columns: minmax(0, 1fr);
  }
  .col-right {
    position: static;
  }
  .stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 720px) {
  .nav-inner {
    height: auto;
    padding: 12px 16px;
    flex-wrap: wrap;
    gap: 12px;
  }
  .menu {
    order: 3;
    width: 100%;
    flex: none;
  }
  .search-sec {
    padding: 44px 16px 24px;
  }
  .s-title {
    font-size: 30px;
    letter-spacing: 5px;
  }
  .search-bar {
    flex-wrap: wrap;
  }
  .search-bar input {
    order: 3;
    width: 100%;
    flex: none;
    padding: 10px 8px 4px;
  }
  .search-btn {
    flex: 1;
    justify-content: center;
  }
  .cate-sec {
    margin: 0 16px 34px;
    padding: 14px 16px 16px;
  }
  .stats {
    padding: 0 16px;
    gap: 12px;
  }
  .main-grid {
    padding: 0 16px;
  }
  .dt-title {
    font-size: 24px;
  }
  /* 窄屏竖向裁切较多，主体再往上提一点 */
  .home-bg-img {
    object-position: 50% 30%;
  }
}

@media (prefers-reduced-motion: reduce) {
  .home-bg-img {
    animation: none;
  }
}
</style>
