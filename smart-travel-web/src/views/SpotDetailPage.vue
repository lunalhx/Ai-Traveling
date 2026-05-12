<template>
  <div class="spot-detail-page">

    <!-- 顶部操作栏 -->
    <div class="top-bar">
      <el-button :icon="ArrowLeft" @click="handleBackToList">返回列表</el-button>
      <h2 class="page-title">景点详情</h2>
      <el-button :icon="Refresh" :loading="loading" :disabled="!isValidId" @click="handleRefresh">
        刷新详情
      </el-button>
    </div>
    <div v-if="lastRefreshTime" class="refresh-time">
      最后刷新：{{ lastRefreshTime }}
    </div>

    <!-- id 参数校验区域 -->
    <el-result
      v-if="!rawId"
      icon="warning"
      title="景点 ID 不存在"
      sub-title="无法识别景点，请从列表页正确跳转"
    >
      <template #extra>
        <el-button type="primary" @click="handleBackToList">返回列表</el-button>
      </template>
    </el-result>

    <el-result
      v-else-if="!isValidId"
      icon="error"
      title="景点 ID 参数错误"
      :sub-title="`参数「${rawId}」不是有效的整数 ID，请检查跳转来源`"
    >
      <template #extra>
        <el-button type="primary" @click="handleBackToList">返回列表</el-button>
      </template>
    </el-result>

    <!-- 主内容：id 校验通过时展示 -->
    <template v-else>

    <!-- 主信息卡片 -->
    <el-card class="main-card" shadow="never">
      <el-skeleton v-if="loading" animated>
        <template #template>
          <div class="skeleton-content">
            <el-skeleton-item variant="image" class="skeleton-cover" />
            <div class="skeleton-info">
              <el-skeleton-item variant="h3" class="skeleton-title" />
              <el-skeleton-item v-for="item in 7" :key="item" variant="text" />
            </div>
          </div>
        </template>
      </el-skeleton>

      <el-result
        v-else-if="loadError"
        icon="error"
        title="详情加载失败"
        :sub-title="loadError"
      >
        <template #extra>
          <el-button type="primary" :icon="Refresh" @click="handleRefresh">重试</el-button>
          <el-button @click="handleBackToList">返回列表</el-button>
        </template>
      </el-result>

      <el-empty v-else-if="!detail" description="景点不存在或已下线">
        <template #extra>
          <el-button type="primary" @click="handleBackToList">返回列表</el-button>
        </template>
      </el-empty>

      <div class="main-content" v-else>

        <!-- 左：封面图区域 -->
        <div class="cover-area">
          <div class="cover-frame">
            <img
              v-if="hasValue(detail.coverUrl) && !coverLoadFailed"
              :src="formatText(detail.coverUrl, '')"
              class="cover-img"
              alt="景点封面"
              @load="handleCoverLoad"
              @error="handleCoverError"
            />
            <div v-else class="cover-placeholder">
              {{ coverPlaceholderText }}
            </div>
          </div>
        </div>

        <!-- 右：基础信息区域 -->
        <div class="info-area">
          <h3 class="spot-name">{{ formatText(detail.name, '暂无名称') }}</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="景点名称">
              {{ formatText(detail.name, '暂无名称') }}
            </el-descriptions-item>
            <el-descriptions-item label="地址">
              {{ formatText(detail.address, '暂无地址') }}
            </el-descriptions-item>
            <el-descriptions-item label="开放时间">
              {{ formatText(detail.openTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="门票价格">
              {{ formatText(detail.ticketPrice) }}
            </el-descriptions-item>
            <el-descriptions-item label="热度分">
              {{ formatText(detail.heatScore) }}
            </el-descriptions-item>
            <el-descriptions-item label="经度">
              {{ formatCoordinate(detail.longitude) }}
            </el-descriptions-item>
            <el-descriptions-item label="纬度">
              {{ formatCoordinate(detail.latitude) }}
            </el-descriptions-item>
            <el-descriptions-item v-if="hasValue(detail.categoryId)" label="分类 ID">
              {{ detail.categoryId }}
            </el-descriptions-item>
          </el-descriptions>
          <div class="detail-actions">
            <el-button type="primary" :icon="Position" @click="handleGoCheckin">
              去打卡
            </el-button>
            <el-button :icon="MapLocation" @click="handleGoNearby">
              查附近景点
            </el-button>
          </div>
        </div>

      </div>
    </el-card>

    <!-- 景点简介区域 -->
    <el-card class="section-card" shadow="never" v-if="detail">
      <template #header>
        <span class="section-title">景点简介</span>
      </template>
      <p class="brief-text">{{ formatText(detail.brief, '暂无简介') }}</p>
    </el-card>

    <!-- 详情内容区域 -->
    <el-card class="section-card" shadow="never" v-if="detail">
      <template #header>
        <span class="section-title">详细介绍</span>
      </template>
      <div class="detail-content">
        <p>{{ formatText(detail.detail, '暂无详细介绍') }}</p>
      </div>
    </el-card>

    <!-- Redis 缓存演示说明区域 -->
    <el-card class="section-card cache-card" shadow="never">
      <template #header>
        <span class="section-title">Redis 缓存机制说明（演示用）</span>
      </template>
      <el-alert
        title="详情接口的 Redis 缓存逻辑由后端完成"
        type="info"
        :closable="false"
        show-icon
      >
        <template #default>
          <ul class="cache-desc">
            <li>前端只调用景点详情 HTTP 接口，不直接访问 Redis。</li>
            <li>第一次访问某个景点时，如果后端缓存未命中，可能查询 MySQL 后再写入 Redis。</li>
            <li>后续访问同一景点时，后端可能直接命中 Redis，减少 MySQL 查询压力。</li>
            <li>后端使用空值缓存防穿透、互斥锁防击穿、随机 TTL 防雪崩。</li>
            <li>本页不展示当前请求是否命中 Redis，除非后端接口返回明确的缓存命中字段。</li>
          </ul>
        </template>
      </el-alert>
    </el-card>

    </template><!-- end v-else -->

  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, MapLocation, Position, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getSpotDetail } from '../api/spot'

const route = useRoute()
const router = useRouter()

// 读取原始路由参数
const rawId = computed(() => route.params.id)

// 校验是否为有效正整数
const isValidId = computed(() => {
  if (!rawId.value) return false
  const num = Number(rawId.value)
  return Number.isInteger(num) && num > 0
})

// 转换为数字类型的 id
const spotId = computed(() => isValidId.value ? Number(rawId.value) : null)

// 详情数据和加载状态
const detail = ref(null)
const loading = ref(false)
const loadError = ref('')
const coverLoadFailed = ref(false)
const lastRefreshTime = ref('')
const detailRequestSeq = ref(0)

const hasValue = (value) => value !== undefined && value !== null && (typeof value !== 'string' || value.trim() !== '')
const formatText = (value, fallback = '暂无') => {
  if (!hasValue(value)) return fallback
  return typeof value === 'string' ? value.trim() : value
}
const formatCoordinate = (value) => formatText(value, '暂无坐标')
const coverPlaceholderText = computed(() => {
  if (hasValue(detail.value?.coverUrl) && coverLoadFailed.value) return '封面图片加载失败'
  return '暂无封面图片'
})
const formatRefreshTime = (date) => date.toLocaleString('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
  hour12: false
})

const handleBackToList = () => {
  router.push('/spots')
}

const handleGoCheckin = () => {
  const query = {
    spotId: spotId.value
  }

  if (hasValue(detail.value?.longitude) && hasValue(detail.value?.latitude)) {
    query.longitude = formatText(detail.value.longitude, '')
    query.latitude = formatText(detail.value.latitude, '')
  }

  router.push({
    path: '/checkin',
    query
  })
}

const handleGoNearby = () => {
  const query = {}

  if (hasValue(detail.value?.longitude) && hasValue(detail.value?.latitude)) {
    query.longitude = formatText(detail.value.longitude, '')
    query.latitude = formatText(detail.value.latitude, '')
  }

  router.push({
    path: '/nearby',
    query
  })
}

// 加载景点详情
const loadDetail = async (id = spotId.value) => {
  if (!id) return
  const requestSeq = detailRequestSeq.value + 1
  detailRequestSeq.value = requestSeq
  loading.value = true
  loadError.value = ''
  coverLoadFailed.value = false
  try {
    const res = await getSpotDetail(id)
    if (requestSeq !== detailRequestSeq.value || id !== spotId.value) return
    detail.value = res.data || null
    lastRefreshTime.value = formatRefreshTime(new Date())
  } catch (err) {
    if (requestSeq !== detailRequestSeq.value || id !== spotId.value) return
    detail.value = null
    loadError.value = '接口异常，请稍后重试'
    ElMessage.error(loadError.value)
  } finally {
    if (requestSeq === detailRequestSeq.value) {
      loading.value = false
    }
  }
}

watch(spotId, (newId, oldId) => {
  if (newId === oldId) return
  detail.value = null
  loadError.value = ''
  coverLoadFailed.value = false
  lastRefreshTime.value = ''

  if (!newId) {
    detailRequestSeq.value += 1
    loading.value = false
    return
  }

  loadDetail(newId)
}, { immediate: true })

watch(rawId, () => {
  if (!isValidId.value) {
    detail.value = null
    loadError.value = ''
    coverLoadFailed.value = false
    lastRefreshTime.value = ''
  }
})

// 刷新按钮
const handleRefresh = async () => {
  coverLoadFailed.value = false
  await loadDetail()
}

const handleCoverLoad = () => {
  coverLoadFailed.value = false
}

const handleCoverError = () => {
  coverLoadFailed.value = true
}
</script>

<style scoped>
.spot-detail-page {
  padding: 10px;
}
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.page-title {
  margin: 0;
  font-size: 20px;
  color: #303133;
}
.refresh-time {
  margin-bottom: 16px;
  color: #909399;
  font-size: 13px;
  text-align: right;
}
.main-card {
  margin-bottom: 20px;
}
.skeleton-content {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}
.skeleton-cover {
  flex: 0 0 340px;
  width: 340px;
  max-width: 100%;
  height: 240px;
  border-radius: 4px;
}
.skeleton-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-width: 0;
  padding-top: 4px;
}
.skeleton-title {
  width: 40%;
  height: 24px;
}
.main-content {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}
.cover-area {
  flex: 0 0 340px;
  width: 340px;
  max-width: 100%;
}
.cover-frame {
  width: 100%;
  aspect-ratio: 17 / 12;
  overflow: hidden;
  border-radius: 4px;
  background-color: #f5f7fa;
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
}
.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.no-data {
  color: #909399;
  font-size: 14px;
}
.info-area {
  flex: 1;
  min-width: 0;
}
.spot-name {
  margin: 0 0 16px 0;
  font-size: 22px;
  color: #303133;
}
.detail-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 16px;
}
.section-card {
  margin-bottom: 20px;
}
.section-title {
  font-weight: bold;
  font-size: 15px;
  color: #303133;
}
.brief-text {
  margin: 0;
  color: #606266;
  line-height: 1.8;
  font-size: 14px;
  white-space: pre-line;
}
.detail-content p {
  color: #606266;
  line-height: 1.8;
  font-size: 14px;
  margin: 0;
  white-space: pre-line;
}
.cache-card {
  border-left: 4px solid #409eff;
}
.cache-desc {
  margin: 8px 0 0 0;
  padding-left: 18px;
  color: #606266;
  font-size: 13px;
  line-height: 2;
}
@media (max-width: 768px) {
  .main-content,
  .skeleton-content {
    flex-direction: column;
  }
  .cover-area,
  .skeleton-cover {
    flex-basis: auto;
    width: 100%;
  }
  .skeleton-cover {
    height: auto;
    aspect-ratio: 17 / 12;
  }
}
</style>
