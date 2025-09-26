// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /role/list/page/vo */
export async function listRoleVoByPage(
  body: API.RoleQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageRoleVO>('/role/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
