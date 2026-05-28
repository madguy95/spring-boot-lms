-- ========================================
-- V15__Course_Backfill_Legacy_Category_Values.sql
-- ========================================
-- V14 renamed courses.category -> courses.tool but kept the existing string
-- values (coding/design/robotics/stem/language/game). Those codes don't exist
-- in master_data(type='tool'), so any UI that resolves tool labels via the
-- translation table or master_data lookup will miss. Backfill the rows here
-- using a best-effort mapping to the new fixed tool list.
-- ========================================

UPDATE courses
SET tool = CASE tool
    WHEN 'coding'   THEN 'scratch'
    WHEN 'design'   THEN 'scratch'
    WHEN 'game'     THEN 'scratch'
    WHEN 'robotics' THEN 'mbot2'
    WHEN 'stem'     THEN 'techai'
    WHEN 'language' THEN 'violin'
    ELSE tool
END
WHERE tool IN ('coding', 'design', 'robotics', 'stem', 'language', 'game');
