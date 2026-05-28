-- ========================================
-- V5__Initial_Data_Quartz.sql
-- ========================================
-- Description: Seed Quartz lock rows
-- Author: System
-- Date: 2025-10-29
-- ========================================

INSERT INTO qrtz_locks (sched_name, lock_name)
VALUES
	('SpringBootScheduler', 'TRIGGER_ACCESS'),
	('SpringBootScheduler', 'JOB_ACCESS'),
	('SpringBootScheduler', 'CALENDAR_ACCESS'),
	('SpringBootScheduler', 'STATE_ACCESS'),
	('SpringBootScheduler', 'MISFIRE_ACCESS')
ON CONFLICT DO NOTHING;

