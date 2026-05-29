-- ========================================
-- V19__Create_Blog_Schema.sql
-- ========================================
-- Blog & Workshop module: editorial posts authored by admins/teachers and
-- shown on the public marketing site + the in-app /blog page.
--
-- One post can be of two kinds:
--   * 'article'  — standalone reading content (parent tips, student stories,
--                  center news). Self-contained.
--   * 'workshop' — promotes a single Class instance the admin chose. We do NOT
--                  duplicate workshop logistics (date/time/location/capacity)
--                  here; instead `class_id` references the canonical Class row
--                  and the FE joins on render. Editing the class once updates
--                  every post that references it.
--
-- Note: `class_id` is intentionally NOT NULL only when type='workshop' (enforced
-- by chk_blog_posts_workshop_class). Articles always have class_id NULL.
-- ========================================

CREATE TABLE IF NOT EXISTS blog_posts (
    id              BIGSERIAL    PRIMARY KEY,
    slug            VARCHAR(160) NOT NULL UNIQUE,
    type            VARCHAR(20)  NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'draft',
    category        VARCHAR(40)  NOT NULL,
    title           VARCHAR(220) NOT NULL,
    excerpt         VARCHAR(400),
    -- Sanitized HTML emitted by the Tiptap editor on the FE. Stored as TEXT
    -- because rich-text bodies can run several KB once images + tables are in.
    body            TEXT,
    cover_url       VARCHAR(500),
    -- Visual hue for the placeholder gradient when cover_url is missing.
    -- 0–360 (HSL hue ring); FE accepts integers.
    hue             INTEGER      NOT NULL DEFAULT 200,
    -- Comma-separated tag slugs. Tags are display-only here; if filtering by
    -- tag becomes a feature, split into a separate blog_post_tags table.
    tags            VARCHAR(500),
    featured        BOOLEAN      NOT NULL DEFAULT FALSE,
    class_id        BIGINT,
    -- Optional. Reserved for when an author entity exists; meanwhile authored_by
    -- + audit columns identify the creator.
    author_id       BIGINT,
    -- Display strings written at publish time. We keep them denormalized so the
    -- list view doesn't have to compute Vietnamese-locale date strings every
    -- request. `published_at` (instant) is the canonical timestamp for ordering.
    published_at    TIMESTAMP,
    published_label VARCHAR(60),
    meta_label      VARCHAR(120),
    reading_minutes INTEGER,
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    version         BIGINT       DEFAULT 0,

    CONSTRAINT fk_blog_posts_class
        FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE SET NULL,
    CONSTRAINT chk_blog_posts_type
        CHECK (type IN ('article', 'workshop')),
    CONSTRAINT chk_blog_posts_status
        CHECK (status IN ('draft', 'published')),
    CONSTRAINT chk_blog_posts_workshop_class
        -- Workshop posts must always reference a class; articles must not.
        CHECK ((type = 'workshop' AND class_id IS NOT NULL)
            OR (type = 'article'  AND class_id IS NULL)),
    CONSTRAINT chk_blog_posts_hue
        CHECK (hue BETWEEN 0 AND 360)
);

CREATE INDEX IF NOT EXISTS idx_blog_posts_status        ON blog_posts(status);
CREATE INDEX IF NOT EXISTS idx_blog_posts_type          ON blog_posts(type);
CREATE INDEX IF NOT EXISTS idx_blog_posts_class_id      ON blog_posts(class_id);
CREATE INDEX IF NOT EXISTS idx_blog_posts_published_at  ON blog_posts(published_at DESC);
CREATE INDEX IF NOT EXISTS idx_blog_posts_featured      ON blog_posts(featured) WHERE featured = TRUE;
