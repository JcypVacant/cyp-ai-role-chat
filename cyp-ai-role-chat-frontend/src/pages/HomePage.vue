<template>
  <!-- 搜索和操作区 -->
  <div class="search-and-action-bar">
    <a-row :gutter="16" justify-center="justify-center">
      <a-col :span="16">
        <a-space>
          <a-input
            placeholder="请输入角色名称"
            allow-clear
            style="width: 300px"
            class="search-input"
            v-model:value="searchKeyword"
            @pressEnter="handleSearch"
          />
          <a-button type="primary" class="search-button" @click="handleSearch">搜索</a-button>
        </a-space>
      </a-col>
    </a-row>
  </div>

  <!-- 角色列表卡片区域 -->
  <div class="role-list-container">
    <a-card
      v-for="role in roleList"
      :key="role.id"
      hoverable
      class="role-card"
      @click="goToRoleChat(role.id)"
    >
      <template #cover>
        <img
          :alt="role.cover"
          :src="role.cover || '../assets/default-cover.jpg'"
          class="role-image"
        />
      </template>
      <template #actions>
        <!-- 使用 a-tooltip 包裹图标 -->
        <a-tooltip title="开始对话">
          <AudioTwoTone class="role-action-icon" />
        </a-tooltip>
      </template>
      <a-card-meta
        :title="role.roleName"
        :description="role.description"
        class="role-meta"
      />
    </a-card>
  </div>

  <!-- 分页区域 -->
  <div class="pagination-wrapper">
    <a-pagination
      v-model:current="rolePages.current"
      v-model:page-size="rolePages.pageSize"
      :total="rolePages.total"
      :show-size-changer="false"
      :show-total="(total: number) => `共 ${total} 个角色`"
      @change="loadRoles"
      class="custom-pagination"
    />
  </div>
</template>

<script lang="ts" setup>
import { AudioTwoTone} from '@ant-design/icons-vue';
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { listRoleVoByPage } from '@/api/roleController.ts'
import { ref, reactive, onMounted } from 'vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { Tooltip as ATooltip } from 'ant-design-vue'; // 导入 Tooltip 组件
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户角色名称搜索词
const searchKeyword = ref('')
// 角色数据
const  roleList = ref<API.RoleVO[]>([])
const rolePages = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 加载角色
const loadRoles = async () => {
  try {
    const res = await listRoleVoByPage({
      current: rolePages.current,
      pageSize: rolePages.pageSize,
      roleName: searchKeyword.value,
    })

    if (res.data.code === 0 && res.data.data) {
      roleList.value = res.data.data.records || []
      rolePages.total = res.data.data.total || 0
    }
  } catch (error) {
    console.error('加载角色失败：', error)
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadRoles()

  // 鼠标跟随光效
  const handleMouseMove = (e: MouseEvent) => {
    const { clientX, clientY } = e
    const { innerWidth, innerHeight } = window
    const x = (clientX / innerWidth) * 100
    const y = (clientY / innerHeight) * 100

    document.documentElement.style.setProperty('--mouse-x', `${x}%`)
    document.documentElement.style.setProperty('--mouse-y', `${y}%`)
  }

  document.addEventListener('mousemove', handleMouseMove)

  // 清理事件监听器
  return () => {
    document.removeEventListener('mousemove', handleMouseMove)
  }
})
// 3. 新增一个搜索处理函数
const handleSearch = () => {
  // 每次搜索都从第一页开始
  rolePages.current = 1
  loadRoles()
}
const goToRoleChat = (roleId: string) => {
  router.push(`/role/chat/${roleId}`)
}

</script>

<style scoped>
/* 全局样式变量 */
:root {
  --primary-color: #1677ff;
  --secondary-color: #0F52BA;
  --text-color: #333;
  --light-text-color: #666;
  --background-color: #f5f7fa;
  --card-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  --card-hover-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  --transition-speed: 0.3s;
}

/* 搜索和操作区 */
.search-and-action-bar {
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all var(--transition-speed);
}

.search-and-action-bar:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.search-input {
  border-radius: 8px !important;
  transition: all var(--transition-speed);
}

.search-input:focus {
  box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.2) !important;
}

.search-button {
  border-radius: 8px !important;
  transition: all var(--transition-speed);
}

.search-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

/* 角色列表卡片区域 */
.role-list-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  padding: 16px;
  margin-bottom: 32px;
  background: radial-gradient(circle at var(--mouse-x) var(--mouse-y), rgba(22, 119, 255, 0.05) 0%, transparent 60%);
  transition: background 0.5s ease;
}

.role-card {
  width: 100%;
  height: 100%;
  border-radius: 12px !important;
  overflow: hidden;
  transition: all var(--transition-speed);
  box-shadow: var(--card-shadow);
}

.role-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--card-hover-shadow);
}

.role-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  transition: transform var(--transition-speed);
}

.role-card:hover .role-image {
  transform: scale(1.05);
}

.role-meta {
  padding: 16px;
}

.role-action-icon {
  color: var(--primary-color);
  transition: transform var(--transition-speed);
}

.role-card:hover .role-action-icon {
  transform: scale(1.2);
}

/* 分页区域 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px;
}

.custom-pagination {
  transition: all var(--transition-speed);
}

.custom-pagination .ant-pagination-item-active {
  border-color: var(--primary-color) !important;
}

.custom-pagination .ant-pagination-item-active a {
  color: var(--primary-color) !important;
}

.custom-pagination .ant-pagination-item:hover {
  border-color: var(--primary-color) !important;
}

.custom-pagination .ant-pagination-item:hover a {
  color: var(--primary-color) !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .role-list-container {
    grid-template-columns: 1fr;
    padding: 12px;
  }

  .search-and-action-bar {
    padding: 12px;
  }

  .search-input {
    width: 100% !important;
  }

  .pagination-wrapper {
    padding: 16px 8px;
  }
}

@media (min-width: 769px) and (max-width: 1199px) {
  .role-list-container {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1200px) {
  .role-list-container {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
