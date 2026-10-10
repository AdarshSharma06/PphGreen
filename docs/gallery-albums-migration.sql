-- PPH Green — Event-Based Gallery Albums Migration
-- Production ddl-auto=none — execute manually via Supabase SQL editor
-- Order: run after backing up

-- 1. Add event association and highlight columns to existing gallery table
-- Use ON DELETE CASCADE so event deletion removes its media (and via FK, its likes/comments)
-- instead of converting them to standalone Community items (SET NULL would promote them).
ALTER TABLE gallery
  ADD COLUMN IF NOT EXISTS event_id BIGINT REFERENCES events(id) ON DELETE CASCADE,
  ADD COLUMN IF NOT EXISTS is_highlight BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS highlight_order INTEGER;

CREATE INDEX IF NOT EXISTS idx_gallery_event_id ON gallery(event_id);
CREATE INDEX IF NOT EXISTS idx_gallery_highlight ON gallery(event_id, is_highlight, highlight_order);

-- 2. Gallery likes — one per user per photo
CREATE TABLE IF NOT EXISTS gallery_likes (
  id BIGSERIAL PRIMARY KEY,
  gallery_id BIGINT NOT NULL REFERENCES gallery(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uk_gallery_likes_gallery_user UNIQUE (gallery_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_gallery_likes_gallery ON gallery_likes(gallery_id);
CREATE INDEX IF NOT EXISTS idx_gallery_likes_user ON gallery_likes(user_id);

-- 3. Gallery comments — per-photo, not per-event
CREATE TABLE IF NOT EXISTS gallery_comments (
  id BIGSERIAL PRIMARY KEY,
  gallery_id BIGINT NOT NULL REFERENCES gallery(id) ON DELETE CASCADE,
  author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  content VARCHAR(2000) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_gallery_comments_gallery ON gallery_comments(gallery_id);
CREATE INDEX IF NOT EXISTS idx_gallery_comments_author ON gallery_comments(author_id);

-- 4. Backfill: existing standalone media remain with event_id=NULL, is_highlight=FALSE
-- No data migration needed; existing file_url / storage objects preserved.

-- 5. RLS hardening — new gallery tables only (do NOT enable on events without review)
-- Frontend has NO Supabase key and never uses PostgREST; backend uses JDBC via DB_URL.
-- Verified SQL Editor role: postgres rolbypassrls=true → backend with same role bypasses RLS.
-- BEFORE relying on BYPASSRLS, verify Render JDBC role has same bypass:
--   In Supabase SQL Editor: SELECT rolname, rolbypassrls FROM pg_roles WHERE rolname = current_user;
--   In Render Dashboard: check DB_USERNAME (redacted) and compare. If DB_USERNAME != postgres
--   and rolbypassrls=false, then backend will be blocked by ENABLE RLS with no policies.
-- For now, enable RLS with no policies = deny all via anon/authenticated REST (secure default).
-- Backend via postgres (bypass) remains unaffected. If Render uses non-bypass role, add explicit allow policy for that role.
ALTER TABLE gallery_likes ENABLE ROW LEVEL SECURITY;
ALTER TABLE gallery_comments ENABLE ROW LEVEL SECURITY;
-- No CREATE POLICY → Supabase REST (anon/authenticated) denied; JDBC postgres bypasses.
-- If Render DB_USERNAME is non-bypass (e.g., app_user), uncomment and set actual name:
-- CREATE POLICY allow_backend_all ON gallery_likes FOR ALL TO "<render_db_username>" USING (true) WITH CHECK (true);
-- CREATE POLICY allow_backend_all ON gallery_comments FOR ALL TO "<render_db_username>" USING (true) WITH CHECK (true);

-- Rollback (if needed):
-- ALTER TABLE gallery DROP COLUMN IF EXISTS highlight_order, DROP COLUMN IF EXISTS is_highlight, DROP COLUMN IF EXISTS event_id;
-- DROP TABLE IF EXISTS gallery_comments;
-- DROP TABLE IF EXISTS gallery_likes;
