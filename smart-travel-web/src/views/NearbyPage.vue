<template>
  <div class="nearby-page">
    <div class="page-header">
      <h2>附近景点</h2>
      <p>输入当前位置，经 Redis GEO 查询附近景点。</p>
    </div>

    <el-card class="section-card" shadow="never">
      <template #header>
        <span class="section-title">查询条件</span>
      </template>

      <el-form :model="form" label-width="92px" class="query-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="经度">
              <el-input
                v-model="form.longitude"
                placeholder="请输入经度"
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="纬度">
              <el-input
                v-model="form.latitude"
                placeholder="请输入纬度"
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="半径">
              <el-input-number
                v-model="form.radius"
                :min="1"
                :max="50000"
                :step="500"
                controls-position="right"
                class="number-input"
              />
              <span class="input-unit">米</span>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="返回数量">
              <el-input-number
                v-model="form.limit"
                :min="1"
                :max="50"
                :step="1"
                :precision="0"
                controls-position="right"
                class="number-input"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="form-actions">
          <el-button type="primary" :loading="loading" :disabled="loading" @click="handleSearch">
            查询
          </el-button>
          <el-button :disabled="loading" @click="handleReset">重置</el-button>
          <el-button :disabled="loading" @click="fillTestCoordinate">填入测试坐标</el-button>
        </div>
      </el-form>
    </el-card>

    <el-card class="section-card geo-card" shadow="never">
      <template #header>
        <span class="section-title">Redis GEO 演示说明</span>
      </template>
      <el-alert
        title="前端调用 /spot/nearby，后端使用 Redis GEO 查询附近景点"
        type="info"
        :closable="false"
        show-icon
      >
        <template #default>
          <ul class="geo-desc">
            <li>前端只负责提交 longitude、latitude、radius、limit 查询参数。</li>
            <li>后端根据经纬度和半径从 Redis GEO 中查询附近景点 ID 和距离。</li>
            <li>后端再补充景点名称、地址、封面、热度等基础信息后返回给前端。</li>
            <li>前端不直接访问 Redis，也不自己计算最终展示距离。</li>
          </ul>
        </template>
      </el-alert>
    </el-card>

    <el-card class="section-card" shadow="never">
      <template #header>
        <div class="result-header">
          <span class="section-title">查询结果</span>
          <span v-if="lastQueryTime" class="query-time">最后查询：{{ lastQueryTime }}</span>
        </div>
      </template>

      <el-empty
        v-if="!searched && !loading"
        description="请输入经纬度后查询附近景点"
      />

      <el-skeleton v-else-if="loading && nearbyList.length === 0" :rows="6" animated />

      <el-result
        v-else-if="errorMessage"
        icon="error"
        title="查询失败"
        :sub-title="errorMessage"
      >
        <template #extra>
          <el-button type="primary" :loading="loading" @click="handleSearch">重试</el-button>
        </template>
      </el-result>

      <el-empty
        v-else-if="searched && nearbyList.length === 0"
        description="当前范围内暂无景点"
      />

      <el-table
        v-else
        v-loading="loading"
        :data="nearbyList"
        :row-key="getRowKey"
        border
        class="nearby-table"
      >
        <el-table-column label="封面" width="112">
          <template #default="{ row }">
            <img
              v-if="hasValue(row.coverUrl) && !imageErrorMap[getImageKey(row)]"
              :src="formatText(row.coverUrl, '')"
              :alt="formatText(row.name, '景点封面')"
              class="cover-img"
              @error="handleImageError(row)"
            />
            <div v-else class="cover-placeholder">暂无图片</div>
          </template>
        </el-table-column>

        <el-table-column label="景点名称" min-width="150">
          <template #default="{ row }">
            <span class="spot-name">{{ formatText(row.name, '暂无名称') }}</span>
          </template>
        </el-table-column>

        <el-table-column label="地址" min-width="220">
          <template #default="{ row }">
            {{ formatText(row.address, '暂无地址') }}
          </template>
        </el-table-column>

        <el-table-column label="距离" prop="distance" width="120" sortable :sort-method="sortDistanceColumn">
          <template #default="{ row }">
            <el-tag type="success">{{ formatDistance(row.distance) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="经纬度" min-width="190">
          <template #default="{ row }">
            {{ formatCoordinatePair(row.longitude, row.latitude) }}
          </template>
        </el-table-column>

        <el-table-column label="热度" width="100">
          <template #default="{ row }">
            {{ formatText(row.heatScore) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              :disabled="!hasValue(row.id)"
              @click="handleViewDetail(row.id)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getNearbySpots } from '../api/spot'

const route = useRoute()
const router = useRouter()

const defaultForm = {
  longitude: '',
  latitude: '',
  radius: 3000,
  limit: 10
}

const testCoordinate = {
  longitude: '108.954239',
  latitude: '34.265472',
  radius: 3000,
  limit: 10
}

const form = reactive({ ...defaultForm })
const nearbyList = ref([])
const loading = ref(false)
const searched = ref(false)
const errorMessage = ref('')
const lastQueryTime = ref('')
const lastQueryParams = ref(null)
const imageErrorMap = ref({})

const hasValue = (value) => value !== undefined && value !== null && (typeof value !== 'string' || value.trim() !== '')
const formatText = (value, fallback = '暂无') => {
  if (!hasValue(value)) return fallback
  return typeof value === 'string' ? value.trim() : value
}
const toNumber = (value) => Number(typeof value === 'string' ? value.trim() : value)
const isFiniteNumber = (value) => Number.isFinite(toNumber(value))

const formatTime = (date) => date.toLocaleString('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
  hour12: false
})

const validateForm = () => {
  if (!hasValue(form.longitude)) {
    ElMessage.warning('请输入经度')
    return null
  }
  if (!isFiniteNumber(form.longitude)) {
    ElMessage.warning('经度必须是数字')
    return null
  }
  const longitude = toNumber(form.longitude)
  if (longitude < -180 || longitude > 180) {
    ElMessage.warning('经度范围应为 -180 到 180')
    return null
  }

  if (!hasValue(form.latitude)) {
    ElMessage.warning('请输入纬度')
    return null
  }
  if (!isFiniteNumber(form.latitude)) {
    ElMessage.warning('纬度必须是数字')
    return null
  }
  const latitude = toNumber(form.latitude)
  if (latitude < -90 || latitude > 90) {
    ElMessage.warning('纬度范围应为 -90 到 90')
    return null
  }

  if (!hasValue(form.radius)) {
    ElMessage.warning('请输入查询半径')
    return null
  }
  if (!isFiniteNumber(form.radius) || toNumber(form.radius) <= 0) {
    ElMessage.warning('半径必须大于 0')
    return null
  }
  const radius = toNumber(form.radius)
  if (radius > 50000) {
    ElMessage.warning('半径不建议超过 50000 米')
    return null
  }

  if (!hasValue(form.limit)) {
    ElMessage.warning('请输入返回数量')
    return null
  }
  if (!isFiniteNumber(form.limit) || toNumber(form.limit) <= 0) {
    ElMessage.warning('返回数量必须大于 0')
    return null
  }
  const limit = Math.floor(toNumber(form.limit))
  if (limit > 50) {
    ElMessage.warning('返回数量不建议超过 50')
    return null
  }

  return {
    longitude,
    latitude,
    radius,
    limit
  }
}

const normalizeNearbyList = (res) => {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.data)) return res.data
  if (Array.isArray(res?.records)) return res.records
  if (Array.isArray(res?.data?.records)) return res.data.records
  return []
}

const sortByDistance = (list) => {
  return [...list].sort((a, b) => {
    const distanceA = isFiniteNumber(a?.distance) ? toNumber(a.distance) : Number.POSITIVE_INFINITY
    const distanceB = isFiniteNumber(b?.distance) ? toNumber(b.distance) : Number.POSITIVE_INFINITY
    return distanceA - distanceB
  })
}

const sortDistanceColumn = (a, b) => {
  const distanceA = isFiniteNumber(a?.distance) ? toNumber(a.distance) : Number.POSITIVE_INFINITY
  const distanceB = isFiniteNumber(b?.distance) ? toNumber(b.distance) : Number.POSITIVE_INFINITY
  return distanceA - distanceB
}

const handleSearch = async () => {
  if (loading.value) return
  const params = validateForm()
  if (!params) return

  loading.value = true
  searched.value = true
  errorMessage.value = ''
  nearbyList.value = []
  imageErrorMap.value = {}

  try {
    const res = await getNearbySpots(params)
    nearbyList.value = sortByDistance(normalizeNearbyList(res))
    lastQueryParams.value = params
    lastQueryTime.value = formatTime(new Date())
  } catch (error) {
    errorMessage.value = '附近景点查询失败，请稍后重试'
    ElMessage.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  Object.assign(form, defaultForm)
  nearbyList.value = []
  searched.value = false
  errorMessage.value = ''
  lastQueryTime.value = ''
  lastQueryParams.value = null
  imageErrorMap.value = {}
}

const fillTestCoordinate = () => {
  Object.assign(form, testCoordinate)
  errorMessage.value = ''
}

const fillFormFromQuery = (query) => {
  let hasInvalidQuery = false

  if (hasValue(query.longitude)) {
    if (isFiniteNumber(query.longitude) && toNumber(query.longitude) >= -180 && toNumber(query.longitude) <= 180) {
      form.longitude = String(query.longitude)
    } else {
      hasInvalidQuery = true
    }
  }

  if (hasValue(query.latitude)) {
    if (isFiniteNumber(query.latitude) && toNumber(query.latitude) >= -90 && toNumber(query.latitude) <= 90) {
      form.latitude = String(query.latitude)
    } else {
      hasInvalidQuery = true
    }
  }

  if (hasValue(query.radius)) {
    if (isFiniteNumber(query.radius) && toNumber(query.radius) > 0 && toNumber(query.radius) <= 50000) {
      form.radius = toNumber(query.radius)
    } else {
      hasInvalidQuery = true
    }
  }

  if (hasValue(query.limit)) {
    if (isFiniteNumber(query.limit) && toNumber(query.limit) > 0 && toNumber(query.limit) <= 50) {
      form.limit = Math.floor(toNumber(query.limit))
    } else {
      hasInvalidQuery = true
    }
  }

  if (hasInvalidQuery) {
    ElMessage.warning('部分跳转参数非法，已保留默认查询条件')
  }
}

watch(() => route.query, (query) => {
  fillFormFromQuery(query)
}, { immediate: true })

const formatDistanceNumber = (value) => {
  if (Number.isInteger(value)) return String(value)
  return value.toFixed(1).replace(/\.0$/, '')
}

const formatDistance = (distance) => {
  if (!hasValue(distance) || !isFiniteNumber(distance)) return '暂无距离'
  const distanceValue = toNumber(distance)
  if (distanceValue < 1000) return `${formatDistanceNumber(distanceValue)} m`
  return `${(distanceValue / 1000).toFixed(2)} km`
}

const formatCoordinate = (value) => {
  if (!hasValue(value) || !isFiniteNumber(value)) return '暂无'
  return toNumber(value).toFixed(6).replace(/\.?0+$/, '')
}

const formatCoordinatePair = (longitude, latitude) => {
  if (!hasValue(longitude) || !hasValue(latitude) || !isFiniteNumber(longitude) || !isFiniteNumber(latitude)) return '暂无坐标'
  return `${formatCoordinate(longitude)}, ${formatCoordinate(latitude)}`
}

const getImageKey = (row) => formatText(row?.id, formatText(row?.coverUrl, 'unknown'))

const handleImageError = (row) => {
  imageErrorMap.value = {
    ...imageErrorMap.value,
    [getImageKey(row)]: true
  }
}

const getRowKey = (row) => formatText(row?.id, `${formatText(row?.name, 'spot')}-${formatText(row?.longitude, '')}-${formatText(row?.latitude, '')}`)

const handleViewDetail = (id) => {
  if (!hasValue(id)) {
    ElMessage.warning('景点 ID 不存在，无法查看详情')
    return
  }
  router.push(`/spots/${id}`)
}
</script>

<style scoped>
.nearby-page {
  padding: 10px;
}
.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0 0 6px 0;
  color: #303133;
  font-size: 22px;
}
.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}
.section-card {
  margin-bottom: 20px;
}
.section-title {
  font-weight: bold;
  font-size: 15px;
  color: #303133;
}
.query-form {
  max-width: 100%;
}
.number-input {
  width: 160px;
}
.input-unit {
  margin-left: 8px;
  color: #606266;
  font-size: 13px;
}
.form-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  padding-left: 92px;
}
.geo-card {
  border-left: 4px solid #409eff;
}
.geo-desc {
  margin: 8px 0 0 0;
  padding-left: 18px;
  color: #606266;
  font-size: 13px;
  line-height: 2;
}
.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.query-time {
  color: #909399;
  font-size: 13px;
}
.nearby-table {
  width: 100%;
}
.cover-img {
  width: 76px;
  height: 52px;
  object-fit: cover;
  border-radius: 4px;
  display: block;
}
.cover-placeholder {
  width: 76px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 12px;
}
.spot-name {
  color: #303133;
  font-weight: 600;
}
@media (max-width: 768px) {
  .form-actions {
    padding-left: 0;
  }
  .number-input {
    width: 100%;
  }
  .input-unit {
    display: inline-block;
    margin-top: 6px;
  }
  .result-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
