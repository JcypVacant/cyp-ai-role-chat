package com.cyp.cypairolechat.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 角色对话历史表（支持多用户与单角色聊天，适配文本/语音消息）
 * @TableName chat_history
 */
@TableName(value ="chat_history")
@Data
public class ChatHistory implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 消息内容（文本/语音转文本等）
     */
    private String message;

    /**
     * 消息来源类型：user/ai
     */
    private String messageType;

    /**
     * 消息格式：text/voice
     */
    private String messageFormat;

    /**
     * 语音消息的云端存储URL（如果消息格式为voice）
     */
    private String voiceUrl;

    /**
     * 所属角色id（多用户→单角色的核心关联）
     */
    private Long roleId;

    /**
     * 创建用户id（区分同一角色下的不同用户）
     */
    private Long userId;

    /**
     * 创建时间（排序核心字段）
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除（0-未删，1-已删）
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}