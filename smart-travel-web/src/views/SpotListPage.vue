<template>
  <div class="spot-list-page">
    <div class="page-header">
      <h2>景点浏览</h2>
      <p class="desc">查看景点分类、热门景点和景点列表</p>
    </div>

    <!-- 搜索筛选区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入景点名称"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="searchForm.categoryId"
            placeholder="请选择分类"
            clearable
            style="width: 150px"
            @change="handleSearch"
          >
            <el-option label="全部分类" value="" />
            <el-option 
              v-for="item in categories" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :disabled="listLoading" :loading="listLoading">搜索</el-button>
          <el-button @click="handleReset" :disabled="listLoading">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 热门景点区域 -->
    <div class="hot-spots-section">
      <h3>热门推荐</h3>
      <el-empty v-if="hotSpots.length === 0" description="暂无热门景点" :image-size="60" />
      <el-row :gutter="20" v-else>
        <el-col :span="6" v-for="spot in hotSpots" :key="'hot-' + spot.id">
          <el-card
            shadow="hover"
            class="hot-spot-card"
            :body-style="{ padding: '0px' }"
            @click="goToDetail(spot.id)"
          >
            <img v-if="spot.coverUrl" :src="spot.coverUrl" class="hot-cover" alt="封面"
              @error="(e) => e.target.style.display='none'"
            />
            <div v-else class="img-placeholder">暂无图片</div>
            <div class="card-info">
              <h4 :title="spot.name">{{ spot.name }}</h4>
              <p>🔥 热度: {{ spot.heatScore || 0 }}</p>
              <p class="address" :title="spot.address">📍 {{ spot.address || '暂无地址' }}</p>
              <div class="card-footer">
                <el-button type="primary" size="small" @click.stop="goToDetail(spot.id)">查看详情</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 景点列表区域 -->
    <div class="list-section" v-loading="listLoading">
      <h3>全部景点</h3>

      <el-empty v-if="!listLoading && spots.length === 0" description="暂无景点数据" />

      <el-row :gutter="20" v-else>
        <el-col :span="8" v-for="spot in spots" :key="spot.id" style="margin-bottom: 20px;">
          <el-card shadow="hover" class="spot-card" :body-style="{ padding: '0px' }">
            <img v-if="spot.coverUrl" :src="spot.coverUrl" class="spot-cover" alt="封面"
              @error="(e) => e.target.style.display='none'"
            />
            <div v-else class="img-placeholder">暂无图片</div>
            <div class="card-info">
              <h4>{{ spot.name }}</h4>
              <p class="address">📍 {{ spot.address || '暂无地址' }}</p>
              <p class="heat">🔥 热度: {{ spot.heatScore || 0 }}</p>
              <p class="brief" :title="spot.brief">{{ spot.brief || '暂无简介' }}</p>
              <div class="card-footer">
                <el-button type="primary" size="small" @click.stop="goToDetail(spot.id)">查看详情</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 分页区域 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="page"
          :page-sizes="[10, 20, 30]"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories, getSpotList, getHotSpots } from '../api/spot'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 响应式状慴
const categories = ref([])
const hotSpots = ref([])
const spots = ref([])
const total = ref(0)
const listLoading = ref(false)
const page = ref(1)
const pageSize = ref(10)

// 表单数据
const searchForm = reactive({
  keyword: '',
  categoryId: ''
})

// 加载分类数据
const loadCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data || []
  } catch (error) {
    ElMessage.error('加载景点分类失败，请稍后重试')
  }
}

// 加载景点列表数据：统一读取当前 page/pageSize/keyword/categoryId
const loadSpotList = async () => {
  listLoading.value = true
  const params = {
    page: page.value,
    pageSize: pageSize.value
  }
  if (searchForm.keyword) params.keyword = searchForm.keyword
  if (searchForm.categoryId) params.categoryId = searchForm.categoryId
  try {
    const res = await getSpotList(params)
    spots.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载景点列表失败，请稍后重试')
  } finally {
    listLoading.value = false
  }
}

// 加载热门景点：失败用 warning 提示，不阻断主列表
const loadHotSpots = async () => {
  try {
    const res = await getHotSpots({ limit: 10 })
    hotSpots.value = res.data || []
  } catch (error) {
    console.warn('热门景点加载失败', error)
    ElMessage.warning('热门景点加载失败，主列表不受影响')
  }
}

// 生命周期钩子
onMounted(() => {
  loadCategories()
  loadHotSpots()
  loadSpotList()
})

// 跳转详情页
const goToDetail = (id) => {
  if (id === undefined || id === null || id === '') {
    ElMessage.warning('景点 ID 不存在，无法查看详情')
    return
  }
  router.push(`/spots/${id}`)
}

// 搜索：page 重置为 1
const handleSearch = () => {
  page.value = 1
  loadSpotList()
}

// 重置：清空所有条件，page/pageSize 都恢复默认
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.categoryId = ''
  page.value = 1
  pageSize.value = 10
  loadSpotList()
}

// 分页：切换页码
const handlePageChange = (newPage) => {
  page.value = newPage
  loadSpotList()
}

// 分页：切换每页条数，page 重置为 1
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  page.value = 1
  loadSpotList()
}
</script>

<style scoped>
.spot-list-page {
  padding: 10px;
}
.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0 0 5px 0;
  color: #303133;
}
.desc {
  margin: 0;
  color: #909399;
  font-size: 14px;
}
.search-card {
  margin-bottom: 20px;
}
.hot-spots-section {
  margin-bottom: 30px;
}
.hot-spots-section h3, .list-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #303133;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}
.img-placeholder {
  height: 160px;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
}
.spot-cover {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
}
.hot-spot-card {
  cursor: pointer;
}
.hot-cover {
  width: 100%;
  height: 120px;
  object-fit: cover;
  display: block;
}
.hot-spot-card .img-placeholder {

  height: 120px;
}
.card-info {
  padding: 14px;
}
.card-info h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-info p {
  margin: 0;
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
}
.address {
  margin-bottom: 5px !important;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.heat {
  color: #ff9800 !important;
  margin-bottom: 5px !important;
}
.card-footer {
  margin-top: 10px;
}
.brief {
  color: #909399 !important;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
  -webkit-line-clamp: 2; /* 限制显示两行 */
}
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
