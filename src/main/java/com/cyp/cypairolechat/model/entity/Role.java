package com.cyp.cypairolechat.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * AI角色扮演信息表
 * @TableName role
 */
@TableName(value ="role")
@Data
public class Role implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 角色名称（如哈利波特、苏格拉底）
     */
    private String roleName;

    /**
     * 角色封面URL
     */
    private String cover;

    /**
     * 角色简介（背景、性格等）
     */
    private String description;

    /**
     * 角色系统提示词（定义角色行为、语气、知识范围）
     */
    private String systemPrompt;

    /**
     * 角色分类（如文学、历史、影视、虚拟等）
     */
    private String category;

    /**
     * 阿里云TTS发音人（用于语音合成，如"zhitian"、"siqi"）
     */
    private String ttsVoice;

    /**
     * 角色优先级（用于排序，数值越高越靠前）
     */
    private Integer priority;

    /**
     * 创建者id（0表示系统预设角色）
     */
    private Long userId;

    /**
     * 最后编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}