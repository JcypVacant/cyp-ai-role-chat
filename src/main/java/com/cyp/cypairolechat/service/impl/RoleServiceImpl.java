package com.cyp.cypairolechat.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyp.cypairolechat.exception.BusinessException;
import com.cyp.cypairolechat.exception.ErrorCode;
import com.cyp.cypairolechat.mapper.RoleMapper;
import com.cyp.cypairolechat.model.dto.role.RoleQueryRequest;
import com.cyp.cypairolechat.model.entity.Role;
import com.cyp.cypairolechat.model.vo.RoleVO;
import com.cyp.cypairolechat.service.RoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author Jcyp
* @description 针对表【role(AI角色扮演信息表)】的数据库操作Service实现
* @createDate 2025-09-23 20:29:54
*/
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role>
    implements RoleService {

    @Override
    public QueryWrapper<Role> getQueryWrapper(RoleQueryRequest roleQueryRequest) {
        if (roleQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = roleQueryRequest.getId();
        String roleName = roleQueryRequest.getRoleName();
        String category = roleQueryRequest.getCategory();
        Long userId = roleQueryRequest.getUserId();
        String description = roleQueryRequest.getDescription();
        String sortField = roleQueryRequest.getSortField();
        String sortOrder = roleQueryRequest.getSortOrder();

        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.like(roleName != null, "roleName", roleName);
        queryWrapper.eq(category != null, "category", category);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.like(description != null, "description", description);
        queryWrapper.eq("isDelete", 0); // 只查未删除

        if (sortField != null) {
            queryWrapper.orderBy(true, "ascend".equals(sortOrder), sortField);
        } else {
            queryWrapper.orderByDesc("createTime");
        }

        return queryWrapper;
    }

    @Override
    public List<RoleVO> getRoleVOList(List<Role> roleList) {
        return roleList.stream().map(role -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<RoleVO> getRoleVOPage(Page<Role> rolePage) {
        Page<RoleVO> voPage = new Page<>(rolePage.getCurrent(), rolePage.getSize(), rolePage.getTotal());
        List<RoleVO> roleVOList = getRoleVOList(rolePage.getRecords());
        voPage.setRecords(roleVOList);
        return voPage;
    }

}




