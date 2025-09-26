package com.cyp.cypairolechat.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cyp.cypairolechat.model.dto.role.RoleQueryRequest;
import com.cyp.cypairolechat.model.entity.Role;
import com.cyp.cypairolechat.model.vo.RoleVO;

import java.util.List;

/**
* @author Jcyp
* @description 针对表【role(AI角色扮演信息表)】的数据库操作Service
* @createDate 2025-09-23 20:29:54
*/
public interface RoleService extends IService<Role> {
    /**
     * 获取查询条件
     */
    QueryWrapper<Role> getQueryWrapper(RoleQueryRequest roleQueryRequest);

    /**
     * 封装角色列表为 VO
     */
    List<RoleVO> getRoleVOList(List<Role> roleList);

    /**
     * 分页查询角色 VO
     */
    Page<RoleVO> getRoleVOPage(Page<Role> rolePage);

}
