# FeedService

```sql
CREATE TABLE posts (
    id        VARCHAR(36) PRIMARY KEY,
    userid    VARCHAR(255) NOT NULL,
    content   TEXT,
    date      VARCHAR(255) NOT NULL
);

CREATE TABLE post_attachments (
    id        VARCHAR(36) PRIMARY KEY,
    postid    VARCHAR(36) NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    type      VARCHAR(50) NOT NULL,
    position  INT         NOT NULL,
    minio_id  VARCHAR(255) NOT NULL
);

CREATE TABLE post_comments (
    id        VARCHAR(36) PRIMARY KEY,
    postid    VARCHAR(36) NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    userid    VARCHAR(255) NOT NULL,
    content   TEXT        NOT NULL,
    date      VARCHAR(255) NOT NULL
);

CREATE TABLE post_reactions (
    postid    VARCHAR(36) NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    userid    VARCHAR(255) NOT NULL,
    reaction  VARCHAR(50) NOT NULL,
    PRIMARY KEY (postid, userid)
);

CREATE TABLE comment_reactions (
    commentid VARCHAR(36) NOT NULL REFERENCES post_comments(id) ON DELETE CASCADE,
    userid    VARCHAR(255) NOT NULL,
    reaction  VARCHAR(50) NOT NULL,
    PRIMARY KEY (commentid, userid)
);

CREATE INDEX idx_posts_user ON posts(userid);
CREATE INDEX idx_comments_post ON post_comments(postid);
CREATE INDEX idx_attach_post  ON post_attachments(postid);
```