# FeedService

```sql
CREATE TABLE IF NOT EXISTS posts (
    id varchar(36) PRIMARY KEY,
    userid varchar(255) NOT NULL,
    content    TEXT,
    date varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS post_attachments (
    id varchar(36) PRIMARY KEY,
    postid varchar(36) NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    type      VARCHAR(20) NOT NULL,
    position  INT          NOT NULL,
    url       VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS post_comments (
    id       varchar(36) PRIMARY KEY,
    postid   varchar(36) NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    userid   varchar(255) NOT NULL,
    content  TEXT NOT NULL,
    date     varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS post_reactions (
    postid   varchar(36) NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    userid   varchar(255) NOT NULL,
    reaction VARCHAR(20) NOT NULL,    -- LIKE | LOVE | …
    PRIMARY KEY (postid, userid)
);

CREATE TABLE IF NOT EXISTS comment_reactions (
    commentid varchar(36) NOT NULL REFERENCES post_comments(id) ON DELETE CASCADE,
    userid    varchar(255) NOT NULL,
    reaction  VARCHAR(20) NOT NULL,
    PRIMARY KEY (commentid, userid)
);

CREATE INDEX IF NOT EXISTS idx_posts_date              ON posts(date DESC);
CREATE INDEX IF NOT EXISTS idx_post_comments_postid    ON post_comments(postid);
CREATE INDEX IF NOT EXISTS idx_post_reactions_postid   ON post_reactions(postid);
CREATE INDEX IF NOT EXISTS idx_comment_reactions_cid   ON comment_reactions(commentid);

```