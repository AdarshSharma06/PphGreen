-- PPH Green — Community Feed + Private Messaging Migration
-- Production ddl-auto=none — execute manually via Supabase SQL editor after backup

-- 1. Community posts
CREATE TABLE IF NOT EXISTS community_posts (
  id BIGSERIAL PRIMARY KEY,
  author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  content VARCHAR(5000) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_community_posts_author ON community_posts(author_id);
CREATE INDEX IF NOT EXISTS idx_community_posts_created ON community_posts(created_at, id);

-- 2. Community post images
CREATE TABLE IF NOT EXISTS community_post_images (
  id BIGSERIAL PRIMARY KEY,
  post_id BIGINT NOT NULL REFERENCES community_posts(id) ON DELETE CASCADE,
  storage_path VARCHAR(500) NOT NULL,
  file_url VARCHAR(1000) NOT NULL,
  display_order INTEGER NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_community_post_images_post ON community_post_images(post_id);

-- 3. Community post comments
CREATE TABLE IF NOT EXISTS community_post_comments (
  id BIGSERIAL PRIMARY KEY,
  post_id BIGINT NOT NULL REFERENCES community_posts(id) ON DELETE CASCADE,
  author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  content VARCHAR(2000) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_community_post_comments_post ON community_post_comments(post_id, created_at, id);
CREATE INDEX IF NOT EXISTS idx_community_post_comments_author ON community_post_comments(author_id);

-- 4. Conversations — canonical pair (participant_one_id < participant_two_id)
CREATE TABLE IF NOT EXISTS conversations (
  id BIGSERIAL PRIMARY KEY,
  participant_one_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  participant_two_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uk_conversation_participants UNIQUE (participant_one_id, participant_two_id),
  CONSTRAINT chk_conversation_not_self CHECK (participant_one_id <> participant_two_id)
);
CREATE INDEX IF NOT EXISTS idx_conversations_participant_one ON conversations(participant_one_id);
CREATE INDEX IF NOT EXISTS idx_conversations_participant_two ON conversations(participant_two_id);
CREATE INDEX IF NOT EXISTS idx_conversations_updated ON conversations(updated_at DESC);

-- 5. Direct messages
CREATE TABLE IF NOT EXISTS direct_messages (
  id BIGSERIAL PRIMARY KEY,
  conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
  sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  content VARCHAR(2000) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  read_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_direct_messages_conversation ON direct_messages(conversation_id, created_at, id);
CREATE INDEX IF NOT EXISTS idx_direct_messages_sender ON direct_messages(sender_id);

-- 6. RLS hardening — new tables only, backend via postgres BYPASSRLS (verified)
-- Frontend has no Supabase key; backend uses JDBC postgres (bypass). No anon policies = deny via REST.
ALTER TABLE community_posts ENABLE ROW LEVEL SECURITY;
ALTER TABLE community_post_images ENABLE ROW LEVEL SECURITY;
ALTER TABLE community_post_comments ENABLE ROW LEVEL SECURITY;
ALTER TABLE conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE direct_messages ENABLE ROW LEVEL SECURITY;
-- No CREATE POLICY → Supabase REST (anon/authenticated) denied; JDBC postgres bypasses.
-- If Render DB_USERNAME is non-bypass, add: CREATE POLICY allow_backend_all ON <table> FOR ALL TO "<render_db_username>" USING (true) WITH CHECK (true);

-- Rollback
-- DROP TABLE IF EXISTS direct_messages;
-- DROP TABLE IF EXISTS conversations;
-- DROP TABLE IF EXISTS community_post_comments;
-- DROP TABLE IF EXISTS community_post_images;
-- DROP TABLE IF EXISTS community_posts;
