/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80028
 Source Host           : localhost:3306
 Source Schema         : saas_message

 Target Server Type    : MySQL
 Target Server Version : 80028
 File Encoding         : 65001

 Date: 05/03/2023 10:41:16
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_batch_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_batch_message`;
CREATE TABLE `tb_batch_message` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `execute_status` tinyint NOT NULL COMMENT '状态:0.执行中、1.执行成功，99.执行失败',
  `count` smallint NOT NULL COMMENT '总数',
  `success_number` smallint DEFAULT NULL COMMENT '成功发送数量',
  `fail_number` smallint DEFAULT NULL COMMENT '失败发送数量',
  `type` smallint NOT NULL COMMENT '类型:10.站内信,20.邮件,30.短信',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='批量消息';

-- ----------------------------
-- Table structure for tb_comment_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_comment_message`;
CREATE TABLE `tb_comment_message` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `target_id` int NOT NULL COMMENT '目标 id',
  `target_name` varchar(128) NOT NULL COMMENT '目标名称',
  `target_type` varchar(64) NOT NULL COMMENT '目标类型',
  `user_id` int NOT NULL COMMENT '用户id',
  `username` varchar(32) NOT NULL COMMENT '用户名称',
  `user_type` varchar(32) NOT NULL COMMENT '用户类型',
  `anonymous` tinyint NOT NULL DEFAULT '0' COMMENT '是否匿名',
  `content` text NOT NULL COMMENT '内容',
  `meta` json DEFAULT NULL COMMENT '元数据信息',
  `reply` tinyint NOT NULL DEFAULT '0' COMMENT '是否存在回复:0.否,1.是',
  `parent_id` int DEFAULT NULL COMMENT '父类 id',
  `reply_count` int NOT NULL DEFAULT '0' COMMENT '回复数量',
  `like_count` int DEFAULT '0' COMMENT '点赞数量',
  `unlike_count` int DEFAULT '0' COMMENT '不喜欢数量',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `ix_target_id_target_type` (`target_id`,`target_type`)
) ENGINE=InnoDB COMMENT='评论';

-- ----------------------------
-- Table structure for tb_email_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_email_message`;
CREATE TABLE `tb_email_message` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `type` varchar(64) NOT NULL COMMENT '类型',
  `from_email` varchar(128) NOT NULL COMMENT '发送邮件',
  `to_email` varchar(128) NOT NULL COMMENT '收取邮件',
  `title` varchar(64) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容',
  `retry_count` tinyint NOT NULL DEFAULT '0' COMMENT '重试次数',
  `max_retry_count` tinyint NOT NULL DEFAULT '0' COMMENT '最大重试次数',
  `last_send_time` datetime(3) DEFAULT NULL COMMENT '最后发送时间',
  `execute_status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0.执行中、1.执行成功，99.执行失败',
  `success_time` datetime(3) DEFAULT NULL COMMENT '发送成功时间',
  `exception` text COMMENT '异常信息',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  `has_attachment` tinyint DEFAULT NULL COMMENT '是否存在附件:0.否,1.是',
  `batch_id` int DEFAULT NULL COMMENT '批量消息 id',
  `attachment_list` json DEFAULT NULL COMMENT '附件集合',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `ix_from_user` (`from_email`) USING BTREE,
  KEY `ix_to_user` (`to_email`) USING BTREE
) ENGINE=InnoDB COMMENT='邮件消息';

-- ----------------------------
-- Table structure for tb_evaluate_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_evaluate_message`;
CREATE TABLE `tb_evaluate_message` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `target_id` int NOT NULL COMMENT '目标 id',
  `target_name` varchar(128) NOT NULL COMMENT '目标名称',
  `target_type` smallint NOT NULL COMMENT '目标类型',
  `user_id` int NOT NULL COMMENT '用户id',
  `username` varchar(32) NOT NULL COMMENT '用户名称',
  `user_type` varchar(32) NOT NULL COMMENT '用户类型',
  `anonymous` tinyint NOT NULL DEFAULT '0' COMMENT '是否匿名',
  `rate` double(4,2) NOT NULL DEFAULT '0.00' COMMENT '星级',
  `content` text NOT NULL COMMENT '内容',
  `meta` json DEFAULT NULL COMMENT '元数据信息',
  `append` json DEFAULT NULL COMMENT '追加内容',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_user_id_target_id_target_type` (`target_id`,`target_type`,`user_id`)
) ENGINE=InnoDB COMMENT='评价消息';

-- ----------------------------
-- Table structure for tb_like_or_unlike
-- ----------------------------
DROP TABLE IF EXISTS `tb_like_or_unlike`;
CREATE TABLE `tb_like_or_unlike` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `user_id` int NOT NULL COMMENT '用户 id',
  `username` varchar(32) NOT NULL COMMENT '用户名称',
  `user_type` varchar(32) NOT NULL COMMENT '用户类型',
  `target_id` int NOT NULL COMMENT '目标 id',
  `target_type` smallint NOT NULL COMMENT '目标名称',
  `is_like` tinyint NOT NULL COMMENT '是否点赞:0.否,1.是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='点赞或非点赞记录';

-- ----------------------------
-- Table structure for tb_notice_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_notice_message`;
CREATE TABLE `tb_notice_message` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `type` varchar(64) NOT NULL COMMENT '类型',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `title` varchar(128) NOT NULL COMMENT '标题',
  `sub_title` varchar(512) NOT NULL COMMENT '副标题',
  `content` text NOT NULL COMMENT '内容',
  `status` smallint NOT NULL COMMENT '状态:10.新创建,15.更新,20.已发布',
  `username` varchar(32) NOT NULL COMMENT '创建用户名称',
  `user_id` int NOT NULL COMMENT '创建用户 id',
  `user_type` varchar(32) NOT NULL COMMENT '创建用户类型',
  `attachment_list` json DEFAULT NULL COMMENT '附件信息',
  `hot` tinyint NOT NULL COMMENT '是否热门:0.否,1.是',
  `cover` json DEFAULT NULL COMMENT '封面图片',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='公告消息';

-- ----------------------------
-- Table structure for tb_site_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_site_message`;
CREATE TABLE `tb_site_message` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `channel` json DEFAULT NULL COMMENT '渠道名称',
  `type` varchar(64) NOT NULL COMMENT '类型',
  `user_id` int NOT NULL COMMENT '收信的用户 id',
  `user_type` varchar(64) NOT NULL COMMENT '收信的用户类型',
  `username` varchar(32) DEFAULT NULL COMMENT '收信的用户名',
  `title` varchar(64) DEFAULT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容',
  `pushable` tinyint NOT NULL COMMENT '是否推送消息：0.否，1.是',
  `readable` tinyint NOT NULL COMMENT '是否已读：0.否，1.是',
  `read_time` datetime(3) DEFAULT NULL COMMENT '读取时间',
  `meta` json DEFAULT NULL COMMENT '元数据信息',
  `retry_count` tinyint NOT NULL DEFAULT '0' COMMENT '重试次数',
  `max_retry_count` tinyint NOT NULL DEFAULT '0' COMMENT '最大重试次数',
  `last_send_time` datetime(3) DEFAULT NULL COMMENT '最后发送时间',
  `exception` text COMMENT '异常信息',
  `execute_status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0.执行中、1.执行成功，99.执行失败',
  `success_time` datetime(3) DEFAULT NULL COMMENT '发送成功时间',
  `attachment_list` json DEFAULT NULL COMMENT '附件信息',
  `batch_id` int DEFAULT NULL COMMENT '批量消息 id',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `ix_to_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB COMMENT='站内信消息';

-- ----------------------------
-- Table structure for tb_sms_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_sms_message`;
CREATE TABLE `tb_sms_message` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `type` varchar(64) NOT NULL COMMENT '类型',
  `channel` varchar(32) DEFAULT NULL COMMENT '渠道名称',
  `phone_number` varchar(24) NOT NULL COMMENT '手机号码',
  `content` varchar(128) DEFAULT NULL COMMENT '内容',
  `ali_yun_meta` json DEFAULT NULL COMMENT '阿里云元数据',
  `retry_count` tinyint NOT NULL DEFAULT '0' COMMENT '重试次数',
  `max_retry_count` tinyint NOT NULL DEFAULT '0' COMMENT '最大重试次数',
  `last_send_time` datetime DEFAULT NULL COMMENT '最后发送时间',
  `execute_status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0.执行中、1.执行成功，99.执行失败',
  `success_time` datetime DEFAULT NULL COMMENT '成功时间',
  `exception` text COMMENT '异常信息',
  `batch_id` int DEFAULT NULL COMMENT '批量消息 id',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `ix_phone_number` (`phone_number`) USING BTREE
) ENGINE=InnoDB COMMENT='短信消息';

SET FOREIGN_KEY_CHECKS = 1;
