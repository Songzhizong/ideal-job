/*
 Navicat Premium Data Transfer

 Source Server         : local_mysql
 Source Server Type    : MySQL
 Source Server Version : 80021
 Source Host           : localhost:3306
 Source Schema         : ideal-job

 Target Server Type    : MySQL
 Target Server Version : 80021
 File Encoding         : 65001

 Date: 11/09/2020 10:02:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ideal_job_info
-- ----------------------------
DROP TABLE IF EXISTS `ideal_job_info`;
CREATE TABLE `ideal_job_info` (
  `job_id` bigint NOT NULL AUTO_INCREMENT,
  `alarm_email` varchar(200) NOT NULL,
  `application` varchar(64) NOT NULL,
  `biz_type` varchar(64) NOT NULL,
  `block_strategy` varchar(16) NOT NULL,
  `business_id` varchar(64) NOT NULL,
  `created_time` datetime NOT NULL,
  `cron` varchar(200) NOT NULL,
  `custom_tag` varchar(64) NOT NULL,
  `deleted` int NOT NULL,
  `execute_param` longtext NOT NULL,
  `execute_type` varchar(16) NOT NULL,
  `executor_handler` varchar(128) NOT NULL,
  `job_name` varchar(200) NOT NULL,
  `job_status` int NOT NULL,
  `last_trigger_time` bigint NOT NULL,
  `next_trigger_time` bigint NOT NULL,
  `retry_count` int NOT NULL,
  `route_strategy` varchar(20) NOT NULL,
  `tenant_id` varchar(64) NOT NULL,
  `update_time` datetime NOT NULL,
  `worker_id` bigint NOT NULL,
  PRIMARY KEY (`job_id`),
  KEY `application` (`application`),
  KEY `tenant_id` (`tenant_id`),
  KEY `worker_id` (`worker_id`),
  KEY `biz_type` (`biz_type`),
  KEY `custom_tag` (`custom_tag`),
  KEY `business_id` (`business_id`),
  KEY `job_name` (`job_name`),
  KEY `executor_handler` (`executor_handler`),
  KEY `next_trigger_time` (`next_trigger_time`)
) ENGINE=InnoDB AUTO_INCREMENT=134938329517916161 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务信息';

-- ----------------------------
-- Records of ideal_job_info
-- ----------------------------
BEGIN;
INSERT INTO `ideal_job_info` VALUES (134938329517916160, '', '', '', 'PARALLEL', '', '2020-09-09 18:23:23', '0 0/5 * * * ?', '', 0, '', 'JOB_HANDLER', 'demoJobHandler', '', 0, 0, 0, 0, 'WEIGHT_ROUND_ROBIN', '', '2020-09-09 18:31:41', 134930081742061568);
COMMIT;

-- ----------------------------
-- Table structure for ideal_job_instance
-- ----------------------------
DROP TABLE IF EXISTS `ideal_job_instance`;
CREATE TABLE `ideal_job_instance` (
  `instance_id` bigint NOT NULL AUTO_INCREMENT,
  `created_time` datetime NOT NULL,
  `dispatch_msg` varchar(200) NOT NULL,
  `dispatch_status` int NOT NULL,
  `execute_param` longtext NOT NULL,
  `executor_handler` varchar(128) NOT NULL,
  `executor_instance` varchar(128) NOT NULL,
  `finished_time` bigint NOT NULL,
  `handle_status` varchar(8) NOT NULL,
  `handle_time` bigint NOT NULL,
  `job_id` bigint NOT NULL,
  `parent_id` bigint NOT NULL,
  `result` longtext NOT NULL,
  `scheduler_instance` varchar(32) NOT NULL,
  `sequence` int NOT NULL,
  `trigger_type` varchar(8) NOT NULL,
  `update_time` datetime NOT NULL,
  `worker_id` bigint NOT NULL,
  PRIMARY KEY (`instance_id`),
  KEY `parent_id` (`parent_id`),
  KEY `job_id` (`job_id`),
  KEY `created_time` (`created_time`)
) ENGINE=InnoDB AUTO_INCREMENT=134941716263206913 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务实例';

-- ----------------------------
-- Records of ideal_job_instance
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for ideal_job_lock
-- ----------------------------
DROP TABLE IF EXISTS `ideal_job_lock`;
CREATE TABLE `ideal_job_lock` (
  `lock_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '锁名称',
  PRIMARY KEY (`lock_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='lock';

-- ----------------------------
-- Records of ideal_job_lock
-- ----------------------------
BEGIN;
INSERT INTO `ideal_job_lock` VALUES ('schedule_lock');
COMMIT;

-- ----------------------------
-- Table structure for ideal_job_worker
-- ----------------------------
DROP TABLE IF EXISTS `ideal_job_worker`;
CREATE TABLE `ideal_job_worker` (
  `worker_id` bigint NOT NULL AUTO_INCREMENT,
  `app_name` varchar(64) NOT NULL,
  `created_time` datetime NOT NULL,
  `deleted` int NOT NULL,
  `title` varchar(32) NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`worker_id`),
  UNIQUE KEY `uk_app_name` (`app_name`),
  KEY `title` (`title`)
) ENGINE=InnoDB AUTO_INCREMENT=134930081742061569 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='执行器';

-- ----------------------------
-- Records of ideal_job_worker
-- ----------------------------
BEGIN;
INSERT INTO `ideal_job_worker` VALUES (134930081742061568, 'SAMPLE-EXECUTOR', '2020-09-09 17:50:36', 0, '示例执行器', '2020-09-09 18:33:29');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
