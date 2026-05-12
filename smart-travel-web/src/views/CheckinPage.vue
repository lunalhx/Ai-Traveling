<template>
  <div class="checkin-page">
    <div class="page-header">
      <h2>用户打卡</h2>
      <p>输入当前位置，后端判断是否进入景点打卡范围。</p>
    </div>

    <el-card class="section-card" shadow="never">
      <template #header>
        <span class="section-title">打卡信息</span>
      </template>

      <el-form :model="form" label-width="92px" class="checkin-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="景点 ID">
              <el-input
                v-model="form.spotId"
                placeholder="请输入景点 ID"
                clearable
                @keyup.enter="handleSubmitCheckin"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="用户经度">
              <el-input
                v-model="form.longitude"
                placeholder="请输入经度"
                clearable
                @keyup.enter="handleSubmitCheckin"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="用户纬度">
              <el-input
                v-model="form.latitude"
                placeholder="请输入纬度"
                clearable
                @keyup.enter="handleSubmitCheckin"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="form-actions">
          <el-button :disabled="pageBusy" @click="fillTestCoordinate">填入测试坐标</el-button>
          <el-button :loading="todayLoading" :disabled="pageBusy" @click="handleQueryTodayStatus">
            查询今日状态
          </el-button>
          <el-button type="primary" :loading="submitLoading" :disabled="pageBusy" @click="handleSubmitCheckin">
            提交打卡
          </el-button>
        </div>
      </el-form>
    </el-card>

    <el-card class="section-card" shadow="never">
      <template #header>
        <span class="section-title">今日状态</span>
      </template>

      <el-skeleton v-if="todayLoading" :rows="2" animated />
      <el-empty
        v-else-if="!todaySearched"
        description="输入景点 ID 后可查询今日是否已打卡"
        :image-size="80"
      />
      <el-result
        v-else-if="todayError"
        icon="error"
        title="今日状态查询失败"
        :sub-title="todayError"
      >
        <template #extra>
          <el-button type="primary" :loading="todayLoading" @click="handleQueryTodayStatus">重试</el-button>
        </template>
      </el-result>
      <el-alert
        v-else-if="todayStatus?.checkedIn"
        type="success"
        title="今日已打卡"
        :description="`打卡时间：${formatText(todayStatus.checkinTime, '暂无时间')}`"
        :closable="false"
        show-icon
      />
      <el-alert
        v-else
        type="info"
        title="今日未打卡"
        description="当前位置进入景点打卡范围后，可以提交打卡。"
        :closable="false"
        show-icon
      />
    </el-card>

    <el-card class="section-card" shadow="never">
      <template #header>
        <span class="section-title">打卡结果</span>
      </template>

      <el-skeleton v-if="submitLoading" :rows="4" animated />
      <el-empty
        v-else-if="!checkinResult && !businessMessage && !systemError"
        description="提交打卡后，这里会展示后端返回的打卡结果"
        :image-size="80"
      />
      <el-alert
        v-else-if="businessMessage"
        type="warning"
        title="业务提示"
        :description="businessMessage"
        :closable="false"
        show-icon
      />
      <el-alert
        v-else-if="systemError"
        type="error"
        title="打卡失败"
        :description="systemError"
        :closable="false"
        show-icon
      />
      <div v-else class="result-content">
        <el-result
          icon="success"
          :title="formatText(checkinResult.message, '打卡成功')"
          sub-title="后端已完成位置范围校验和重复打卡判断"
        />
        <el-descriptions :column="1" border>
          <el-descriptions-item label="景点名称">
            {{ formatText(checkinResult.spotName, '暂无名称') }}
          </el-descriptions-item>
          <el-descriptions-item label="景点 ID">
            {{ formatText(checkinResult.spotId, '暂无') }}
          </el-descriptions-item>
          <el-descriptions-item label="打卡日期">
            {{ formatText(checkinResult.checkinDate, '暂无日期') }}
          </el-descriptions-item>
          <el-descriptions-item label="打卡距离">
            {{ formatDistance(checkinResult.distanceMeter ?? checkinResult.distance) }}
          </el-descriptions-item>
          <el-descriptions-item label="奖励积分">
            {{ formatText(checkinResult.rewardPoints) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="hasValue(checkinResult.recordId)" label="记录 ID">
            {{ checkinResult.recordId }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <el-card class="section-card explain-card" shadow="never">
      <template #header>
        <span class="section-title">打卡幂等与位置判断说明</span>
      </template>
      <el-alert
        title="前端提交位置，后端判断范围并处理重复打卡"
        type="info"
        :closable="false"
        show-icon
      >
        <template #default>
          <ul class="explain-desc">
            <li>本页只提交 spotId、用户当前经度和纬度，不直接判断是否能打卡。</li>
            <li>后端根据景点坐标和用户坐标计算距离，并判断是否进入打卡范围。</li>
            <li>后端使用 Redis Set 做快速防重复打卡。</li>
            <li>MySQL 使用 user_id + spot_id + checkin_date 唯一索引做最终兜底。</li>
            <li>项目不是后台持续监听用户位置，只处理用户主动上报的本次位置。</li>
          </ul>
        </template>
      </el-alert>
    </el-card>

    <el-card class="section-card" shadow="never">
      <template #header>
        <span class="section-title">页面入口</span>
      </template>
      <div class="link-actions">
        <el-button :disabled="!isPositiveInteger(form.spotId)" @click="handleBackToSpotDetail">
          返回景点详情
        </el-button>
        <el-button type="primary" @click="handleViewRecords">查看我的打卡记录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { checkin, getTodayCheckinStatus } from '../api/checkin'

const route = useRoute()
const router = useRouter()

const defaultForm = {
  spotId: '',
  longitude: '',
  latitude: ''
}

const testCoordinate = {
  longitude: '108.954239',
  latitude: '34.265472'
}

const form = reactive({ ...defaultForm })
const submitLoading = ref(false)
const todayLoading = ref(false)
const todaySearched = ref(false)
const todayStatus = ref(null)
const todayError = ref('')
const checkinResult = ref(null)
const businessMessage = ref('')
const systemError = ref('')

const pageBusy = computed(() => submitLoading.value || todayLoading.value)

const hasValue = (value) => value !== undefined && value !== null && (typeof value !== 'string' || value.trim() !== '')
const formatText = (value, fallback = '暂无') => {
  if (!hasValue(value)) return fallback
  return typeof value === 'string' ? value.trim() : value
}
const toNumber = (value) => Number(typeof value === 'string' ? value.trim() : value)
const isFiniteNumber = (value) => Number.isFinite(toNumber(value))
const isPositiveInteger = (value) => {
  if (!hasValue(value)) return false
  const num = Number(value)
  return Number.isInteger(num) && num > 0
}

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

const validateSpotId = () => {
  if (!hasValue(form.spotId)) {
    ElMessage.warning('请输入景点 ID')
    return null
  }
  if (!isPositiveInteger(form.spotId)) {
    ElMessage.warning('景点 ID 必须是正整数')
    return null
  }
  return Number(form.spotId)
}

const validateCoordinate = () => {
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

  return {
    longitude,
    latitude
  }
}

const validateCheckinForm = () => {
  const spotId = validateSpotId()
  if (!spotId) return null

  const coordinate = validateCoordinate()
  if (!coordinate) return null

  return {
    spotId,
    longitude: coordinate.longitude,
    latitude: coordinate.latitude
  }
}

const normalizeTodayStatus = (res) => {
  if (res?.data && typeof res.data === 'object') return res.data
  if (res && typeof res === 'object') return res
  return {
    checkedIn: false
  }
}

const normalizeCheckinResult = (res) => {
  if (res?.data && typeof res.data === 'object') return res.data
  if (res && typeof res === 'object') return res
  return null
}

const getErrorMessage = (error, fallback) => {
  return error?.response?.data?.message || error?.response?.data?.msg || error?.message || fallback
}

const isBusinessCheckinMessage = (message) => {
  return /今日已打卡|重复打卡|未进入打卡范围|暂不支持打卡|景点不存在|已下架|参数错误/.test(message)
}

const clearCheckinFeedback = () => {
  checkinResult.value = null
  businessMessage.value = ''
  systemError.value = ''
}

const fillFormFromQuery = (query) => {
  let hasInvalidQuery = false

  if (hasValue(query.spotId)) {
    if (isPositiveInteger(query.spotId)) {
      form.spotId = String(query.spotId)
    } else {
      hasInvalidQuery = true
    }
  }

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

  if (hasInvalidQuery) {
    ElMessage.warning('部分跳转参数非法，已保留当前打卡表单')
  }
}

watch(() => route.query, (query) => {
  fillFormFromQuery(query)
}, { immediate: true })

const fillTestCoordinate = () => {
  Object.assign(form, testCoordinate)
  clearCheckinFeedback()
}

const handleQueryTodayStatus = async () => {
  if (todayLoading.value) return
  const spotId = validateSpotId()
  if (!spotId) return

  todayLoading.value = true
  todaySearched.value = true
  todayError.value = ''
  todayStatus.value = null

  try {
    const res = await getTodayCheckinStatus({ spotId })
    todayStatus.value = normalizeTodayStatus(res)
  } catch (error) {
    todayError.value = getErrorMessage(error, '今日打卡状态查询失败，请稍后重试')
    ElMessage.error(todayError.value)
  } finally {
    todayLoading.value = false
  }
}

const handleSubmitCheckin = async () => {
  if (submitLoading.value) return
  const params = validateCheckinForm()
  if (!params) return

  submitLoading.value = true
  clearCheckinFeedback()

  try {
    const res = await checkin(params)
    const result = normalizeCheckinResult(res)
    checkinResult.value = result || {
      spotId: params.spotId,
      message: '打卡成功'
    }
    ElMessage.success(formatText(checkinResult.value.message, '打卡成功'))
    handleQueryTodayStatus()
  } catch (error) {
    const message = getErrorMessage(error, '打卡失败，请稍后重试')
    if (isBusinessCheckinMessage(message)) {
      businessMessage.value = message
      ElMessage.warning(message)
    } else {
      systemError.value = message
      ElMessage.error(message)
    }
  } finally {
    submitLoading.value = false
  }
}

const handleBackToSpotDetail = () => {
  const spotId = validateSpotId()
  if (!spotId) return
  router.push(`/spots/${spotId}`)
}

const handleViewRecords = () => {
  router.push('/checkin-records')
}
</script>

<style scoped>
.checkin-page {
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
.checkin-form {
  max-width: 100%;
}
.form-actions,
.link-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  padding-left: 92px;
}
.result-content :deep(.el-result) {
  padding-top: 8px;
  padding-bottom: 16px;
}
.explain-card {
  border-left: 4px solid #409eff;
}
.explain-desc {
  margin: 8px 0 0 0;
  padding-left: 18px;
  color: #606266;
  font-size: 13px;
  line-height: 2;
}
@media (max-width: 768px) {
  .form-actions,
  .link-actions {
    padding-left: 0;
  }
}
</style>
