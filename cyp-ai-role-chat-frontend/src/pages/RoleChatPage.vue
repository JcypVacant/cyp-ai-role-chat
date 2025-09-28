<template>
  <div class="role-chat-container">
    <!-- 角色信息头部 -->
    <div class="role-header">
      <a-avatar :src="roleInfo?.cover || defaultAvatar" size="large" />
      <div class="role-info">
        <h2>{{ roleInfo?.roleName|| 'AI角色' }}</h2>
        <p>{{ roleInfo?.description || '正在进行语音对话...' }}</p>
      </div>
    </div>

    <!-- 对话消息区域 -->
    <div class="chat-messages" ref="chatContainer" @scroll="handleScroll">
      <!-- 加载更多提示（顶部） -->
      <div v-if="isLoadingMore" class="loading-tip">加载更多历史消息...</div>
      <!-- 加载历史提示 -->
      <div v-if="isLoadingHistory && chatHistory.length === 0" class="loading-tip">加载历史消息中...</div>

      <!-- 空状态 -->
      <div v-if="!isLoadingHistory && chatHistory.length === 0" class="empty-tip">
        开始与{{ roleInfo?.roleName || 'AI角色' }}的对话吧~
      </div>

      <!-- 消息列表 -->
      <div
        v-for="msg in chatHistory"
        :key="msg.id"
        :class="['message-item', msg.messageType === 'user' ? 'user-message' : 'ai-message']"
      >
        <a-avatar
          :src="msg.messageType === 'user' ? userAvatar : (userAvatar || roleInfo?.cover)"
          size="small"
        />
        <div class="message-content">
          <p>{{ msg.message }}</p>
          <audio
            v-if="msg.messageFormat === 'voice' && msg.voiceUrl"
            controls
            :src="fixVoiceUrl(msg.voiceUrl)"
            class="audio-player"
          >
          </audio>
        </div>
      </div>
    </div>

    <!-- 录音控制区域 -->
    <div class="audio-control">
        <template >
          <AudioTwoTone />
        </template>
    </div>
    <!-- 底部加载提示（首次加载） -->
    <div v-if="isLoadingHistory && chatHistory.length > 0" class="loading-tip">加载中...</div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed, nextTick, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router';
import { AudioTwoTone } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { chatByVoice, getChatHistory } from '@/api/chatController';
import { getRoleById } from '@/api/roleController'; // 需要实现该接口
import { useLoginUserStore } from '@/stores/loginUser';
// 状态定义
const isRecording = ref(false);
const isLoadingHistory = ref(true);
const mediaRecorder = ref<MediaRecorder | null>(null);
const audioChunks = ref<Blob[]>([]);
const chatHistory = ref<API.ChatHistory[]>([]);
const roleInfo = ref<API.Role | null>(null);
const userAvatar = ref('');
const defaultAvatar = 'https://api.dicebear.com/6.x/identicon/svg?seed=ai';
const chatContainer = ref<HTMLDivElement>(null);

// 路由与用户状态
const route = useRoute();
const router = useRouter();
const loginUserStore = useLoginUserStore();
// 在函数组件中
const roleId = route.params.id as string  // 类型断言为string
// 新增分页相关状态
const currentPage = ref(1); // 当前页码
const totalCount = ref(0); // 总条数
const hasMore = ref(true); // 是否有更多数据
const isLoadingMore = ref(false); // 加载更多中
// 初始化
onMounted(async () => {
  // 检查登录状态
  await loginUserStore.fetchLoginUser();
  userAvatar.value = loginUserStore.loginUser.userAvatar || '';
  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录');
    router.push(`/user/login?redirect=/role/chat/${roleId}`);
    return;
  }
  // 加载角色信息和历史消息
  loadRoleInfo();
  loadChatHistory();
  // 滚动到底部
  scrollToBottom();
})
// 处理滚动加载更多（监听顶部滚动）
const handleScroll = () => {
  const container = chatContainer.value;
  if (!container) return;

  // 滚动到顶部且有更多数据时加载上一页
  if (container.scrollTop <= 10 && hasMore.value && !isLoadingMore.value) {
    loadChatHistory(true);
  }
};
const loadRoleInfo = async () => {
  const res = await getRoleById({roleId})
  if (res.data.code === 0) {
    roleInfo.value = res.data.data || null
    console.log(res.data.data)
  }
}
// 修改加载历史消息方法（支持分页）
const loadChatHistory = async (isLoadMore = false) => {
  // 没有更多数据时停止请求
  if (isLoadMore && (!hasMore.value || isLoadingMore.value)) return;

  // 设置加载状态
  if (isLoadMore) {
    isLoadingMore.value = true;
  } else {
    isLoadingHistory.value = true;
  }

  try {
    const res = await getChatHistory({
      roleId: roleId,
      userId: loginUserStore.loginUser.id,
      current: isLoadMore ? currentPage.value + 1 : 1,
      pageSize: 6
    });

    if (res.data.code === 0) {
      console.log(res.data.data)
      const pageData = res.data.data;
      totalCount.value = pageData.total || 0;
      console.log(pageData.records)

      if (isLoadMore) {
        // 加载更多：往前添加历史消息
        chatHistory.value = [...(pageData.records || []), ...chatHistory.value];
        currentPage.value++;
      } else {
        // 首次加载：直接替换
        chatHistory.value = pageData.records || [];
        currentPage.value = 1;
      }

      // 判断是否还有更多数据
      hasMore.value = (currentPage.value * 10) < totalCount.value;
    } else {
      message.error('历史消息加载失败');
    }
  } catch (e) {
    message.error('历史消息加载失败');
    console.error(e);
  } finally {
    isLoadingHistory.value = false;
    isLoadingMore.value = false;
  }
};
// 格式化时间
const formatTime = (time: string) => {
  if (!time) return '';
  const date = new Date(time);
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

// 切换录音状态
const toggleRecording = async () => {
  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录');
    return;
  }

  if (isRecording.value) {
    stopRecording();
  } else {
    startRecording();
  }
};

// 开始录音
const startRecording = async () => {
  try {
    // 检查浏览器支持
    if (!navigator.mediaDevices || !MediaRecorder) {
      message.error('您的浏览器不支持录音功能');
      return;
    }

    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    mediaRecorder.value = new MediaRecorder(stream);
    audioChunks.value = [];
    mediaRecorder.value.ondataavailable = (e) => {
      audioChunks.value.push(e.data);
    };
    mediaRecorder.value.start();
    isRecording.value = true;
    message.success('开始录音');
  } catch (e) {
    message.error('录音权限获取失败，请检查设备或权限设置');
    console.error(e);
  }
};

// 停止录音并处理
const stopRecording = () => {
  if (!mediaRecorder.value) return;

  mediaRecorder.value.stop();
  mediaRecorder.value.onstop = async () => {
    const audioBlob = new Blob(audioChunks.value, { type: 'audio/wav' });
    const formData = new FormData();
    formData.append('file', audioBlob, `recording-${Date.now()}.wav`);
    formData.append('roleId', roleId);
    formData.append('userId', loginUserStore.loginUser.id);

    try {
      // 显示加载状态
      message.loading('处理中...', 0);

      // 调用后端接口
      const res = await chatByVoice(formData);
      if (res.data.code === 0) {
        // 重新加载历史消息（确保数据一致性）
        await loadChatHistory();
        scrollToBottom();
        message.success('发送成功');
      } else {
        message.error(res.data.message || '处理失败');
      }
    } catch (e) {
      message.error('语音处理失败');
      console.error(e);
    } finally {
      message.destroy();
      isRecording.value = false;
    }
  };
};

// 滚动到最新消息
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    }
  });
};
// 修复音频URL格式：在域名.com后补充缺失的斜杠
const fixVoiceUrl = (url: string) => {
  if (!url) return '';
  // 匹配".com"后面没有斜杠的情况，例如"myqcloud.comvoice-user"
  const regex = /(\.com)(?!\/)/i;
  if (regex.test(url)) {
    return url.replace(regex, '$1/');  // 替换为".com/"
  }
  return url;  // 格式正确则直接返回
};
</script>

<style scoped>
/* 新增加载更多样式 */
.loading-tip {
  text-align: center;
  padding: 10px;
  color: #666;
  font-size: 14px;
}
.role-chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

.role-header {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
  margin-bottom: 16px;
}

.role-info {
  margin-left: 16px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 20px;
}

.loading-tip, .empty-tip {
  text-align: center;
  padding: 20px;
  color: #666;
}


/* 提升选择器优先级，避免被其他样式覆盖 */
.chat-messages .message-item {
  display: flex;
  margin-bottom: 24px; /* 增大间距，让每个对话框更独立 */
  width: 100%; /* 占满父容器宽度 */
  min-height: 140px; /* 即使内容少，也给足高度空间 */
}

/* 对话框强制大尺寸（核心样式） */
.chat-messages .message-item .message-content {
  margin: 0 16px; /* 加大边距，避免贴边 */
  padding: 30px; /* 内部留白足够多，视觉上更"大" */
  border-radius: 16px; /* 圆角适中，保持对话框感 */
  width: 85%; /* 固定宽度（父容器的85%） */
  min-height: 140px; /* 强制最小高度，确保大框 */
  box-sizing: border-box; /* 保证padding不影响整体宽高 */
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1); /* 阴影加深，突出大框 */
}

/* 用户消息（靠右的蓝色框） */
.chat-messages .message-item.user-message .message-content {
  background-color: #1890ff;
  color: white;
  /* 确保靠右时也占满设定宽度 */
  margin-left: auto;
}

/* AI消息（靠左的白色框） */
.chat-messages .message-item.ai-message .message-content {
  background-color: #fff;
  border: 1px solid #eee; /* 加边框，增强白色框的存在感 */
}

/* 语音进度条强制固定长度 */
.chat-messages .message-item .message-content .audio-player {
  width: 200px; /* 固定进度条长度（可根据需求调整） */
  height: 30px; /* 进度条高度加大，更显眼 */
  margin-top: 15px; /* 与顶部保持距离 */
  /* 确保进度条在框内居中 */
  display: block;
}

/* 确保滚动容器有足够宽度供大框使用 */
.chat-messages {
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
}

.user-message .message-content {
  background-color: #1890ff;
  color: white;
}

.audio-player {
  margin-top: 6px;
  width: 100%;
}

.message-time {
  display: block;
  font-size: 12px;
  color: #888;
  margin-top: 4px;
  text-align: right;
}

.user-message .message-time {
  color: #ddd;
}

.audio-control {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
}

.recording-tip {
  color: #f5222d;
  margin-top: 8px;
  font-size: 14px;
}
</style>
