-- ========================================
-- V17__Class_Add_Room_Column_And_Seed_Master_Data.sql
-- ========================================
-- Split the classroom location concept into two fields:
--  - `location` (existing): campus / branch name, populated from master_data type='location'
--  - `room`     (new):      physical room within the campus, optional (null for online),
--                           populated from master_data type='room'
-- Seed master_data with a baseline set of room codes the admin can extend later.
-- ========================================

ALTER TABLE classes ADD COLUMN IF NOT EXISTS room VARCHAR(50);

INSERT INTO master_data (type, code, name, position, active) VALUES
    ('room', 'Room 101',   'Room 101',   1, TRUE),
    ('room', 'Room 102',   'Room 102',   2, TRUE),
    ('room', 'Room 201',   'Room 201',   3, TRUE),
    ('room', 'Room 204',   'Room 204',   4, TRUE),
    ('room', 'Phòng A1',   'Phòng A1',   5, TRUE),
    ('room', 'Phòng A2',   'Phòng A2',   6, TRUE),
    ('room', 'Lab 1',      'Lab 1',      7, TRUE),
    ('room', 'Lab 2',      'Lab 2',      8, TRUE),
    ('room', 'Online',     'Online',     9, TRUE)
ON CONFLICT (type, code) DO NOTHING;
