// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /chat/voice */
export async function chatByVoice(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.chatByVoiceParams,
  body: {},
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseString>('/chat/voice', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: {
      ...params,
    },
    data: body,
    ...(options || {}),
  })
}
