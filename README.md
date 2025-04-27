# FeedService

## About

Default configuration for this microservice:

```
ktor {
  database {
    mode = "LOCAL" # mode может быть "gateway" или "LOCAL"
    host = "localhost"
    port = 8080 # DB port
  }
}
```

Default port for this service is 8082 (notice, that application.conf port does not affect anything). [Application.kt](app/src/main/kotlin/com/mad/feed/Application.kt)

### Routes:

Post Routes:

- POST /posts – Create a new post
- GET /posts/{id} – Get a post by ID
- GET /posts?page={page_no}&page_size={page size, e.g. 20} – List all posts
- GET /posts/user/{userId}?page={page_no}&page_size={page size, e.g. 20} – List posts of a specific user

Comment Routes:

- POST /posts/{postId}/comments – Add a new comment to a post
- GET /posts/{postId}/comments – List comments for a post

Reaction Routes:

- POST /posts/{postId}/reactions – Add a reaction to a post
- DELETE /posts/{postId}/reactions – Remove a reaction from a post

--------------------------------------------------------------------------------

### Post query examples

**Create a post**

URL:

```
POST http://localhost:8082/api/posts
```

Body:

```json
{
  "post": {
    "userId": "user123",
    "content": "user123 first post",
    "attachments": [
      {
        "postId": "",
        "type": "ATTACHMENT_TYPE_IMAGE",
        "position": 0,
        "minioId": "bucket/img_001.png"
      }
    ]
  }
}
```

Response:

```json
{
  "id": "746f0482-b50f-4131-9c4c-9e1f0d30356a",
  "userId": "user123",
  "content": "user123 second post",
  "attachments": [
    {
      "id": "c0c9652a-21b2-4a3f-ac67-3917675cb49e",
      "postId": "746f0482-b50f-4131-9c4c-9e1f0d30356a",
      "type": "ATTACHMENT_TYPE_IMAGE",
      "position": 0,
      "minioId": "bucket/img_001.png"
    }
  ],
  "date": "2025-04-27T15:48:30.914Z"
}
```

**Get post by ID**

URL:

```
http://localhost:8082/posts/0d13c9c1-757a-4f66-88fb-e8988ca78de0
```

Response:

```json
{
  "id": "0d13c9c1-757a-4f66-88fb-e8988ca78de0",
  "userId": "user123",
  "content": "user123 first post",
  "attachments": [
    {
      "id": "092b1e71-ebca-4214-b17a-983a8588a445",
      "postId": "0d13c9c1-757a-4f66-88fb-e8988ca78de0",
      "type": "ATTACHMENT_TYPE_IMAGE",
      "position": 0,
      "minioId": "bucket/img_001.png"
    }
  ],
  "date": "2025-04-27T14:19:06.369Z",
  "reactions": [
    {
      "postId": "0d13c9c1-757a-4f66-88fb-e8988ca78de0",
      "userId": "user123",
      "reaction": "REACTION_WOW"
    }
  ],
  "comments": [
    {
      "id": "d2376782-38b3-4629-9014-9cd5f34c998f",
      "userId": "user789",
      "content": "comment to user123 post 1 by user789",
      "date": "2025-04-27T15:08:26.675Z"
    },
    {
      "id": "54ed2402-9fa0-4444-8c10-651df27da68e",
      "userId": "user456",
      "content": "👍",
      "date": "2025-04-27T15:08:54.799Z"
    }
  ]
}
```

**List posts**

URL:

```
http://localhost:8082/posts?page=1&page_size=20
```

Response:

```json
{
  "posts": [
    {
      "id": "746f0482-b50f-4131-9c4c-9e1f0d30356a",
      "userId": "user123",
      "content": "user123 second post",
      "attachments": [
        {
          "id": "c0c9652a-21b2-4a3f-ac67-3917675cb49e",
          "postId": "746f0482-b50f-4131-9c4c-9e1f0d30356a",
          "type": "ATTACHMENT_TYPE_IMAGE",
          "position": 0,
          "minioId": "bucket/img_001.png"
        }
      ],
      "date": "2025-04-27T15:48:30.914Z"
    },
    {
      "id": "de03c28c-f7a7-4581-b74f-f1973f97ae4c",
      "userId": "user456",
      "attachments": [
        {
          "id": "a0b8a56a-1065-417d-b053-3448adfc4e22",
          "postId": "de03c28c-f7a7-4581-b74f-f1973f97ae4c",
          "type": "ATTACHMENT_TYPE_VIDEO",
          "position": 0,
          "minioId": "bucket/video_123.mp4"
        }
      ],
      "date": "2025-04-27T14:20:20.457Z"
    },
    {
      "id": "0d13c9c1-757a-4f66-88fb-e8988ca78de0",
      "userId": "user123",
      "content": "user123 first post",
      "attachments": [
        {
          "id": "092b1e71-ebca-4214-b17a-983a8588a445",
          "postId": "0d13c9c1-757a-4f66-88fb-e8988ca78de0",
          "type": "ATTACHMENT_TYPE_IMAGE",
          "position": 0,
          "minioId": "bucket/img_001.png"
        }
      ],
      "date": "2025-04-27T14:19:06.369Z",
      "reactions": [
        {
          "postId": "0d13c9c1-757a-4f66-88fb-e8988ca78de0",
          "userId": "user123",
          "reaction": "REACTION_WOW"
        }
      ],
      "comments": [
        {
          "id": "d2376782-38b3-4629-9014-9cd5f34c998f",
          "userId": "user789",
          "content": "comment to user123 post 1 by user789",
          "date": "2025-04-27T15:08:26.675Z"
        },
        {
          "id": "54ed2402-9fa0-4444-8c10-651df27da68e",
          "userId": "user456",
          "content": "👍",
          "date": "2025-04-27T15:08:54.799Z"
        }
      ]
    }
  ]
}
```

--------------------------------------------------------------------------------

**List user posts**

URL:

```
http://localhost:8082/posts/user/user123?page=1&page_size=10
```

Response:

```json
{
  "posts": [
    {
      "id": "746f0482-b50f-4131-9c4c-9e1f0d30356a",
      "userId": "user123",
      "content": "user123 second post",
      "attachments": [
        {
          "id": "c0c9652a-21b2-4a3f-ac67-3917675cb49e",
          "postId": "746f0482-b50f-4131-9c4c-9e1f0d30356a",
          "type": "ATTACHMENT_TYPE_IMAGE",
          "position": 0,
          "minioId": "bucket/img_001.png"
        }
      ],
      "date": "2025-04-27T15:48:30.914Z"
    },
    {
      "id": "0d13c9c1-757a-4f66-88fb-e8988ca78de0",
      "userId": "user123",
      "content": "user123 first post",
      "attachments": [
        {
          "id": "092b1e71-ebca-4214-b17a-983a8588a445",
          "postId": "0d13c9c1-757a-4f66-88fb-e8988ca78de0",
          "type": "ATTACHMENT_TYPE_IMAGE",
          "position": 0,
          "minioId": "bucket/img_001.png"
        }
      ],
      "date": "2025-04-27T14:19:06.369Z",
      "reactions": [
        {
          "postId": "0d13c9c1-757a-4f66-88fb-e8988ca78de0",
          "userId": "user123",
          "reaction": "REACTION_WOW"
        }
      ],
      "comments": [
        {
          "id": "d2376782-38b3-4629-9014-9cd5f34c998f",
          "userId": "user789",
          "content": "comment to user123 post 1 by user789",
          "date": "2025-04-27T15:08:26.675Z"
        },
        {
          "id": "54ed2402-9fa0-4444-8c10-651df27da68e",
          "userId": "user456",
          "content": "👍",
          "date": "2025-04-27T15:08:54.799Z"
        }
      ]
    }
  ]
}
```

### Comment query examples

**Add comment**

URL:

```
http://localhost:8082/posts/0d13c9c1-757a-4f66-88fb-e8988ca78de0/comments
```

Body:

```json
{
  "userId": "user789",
  "content": "comment2 to user123 post 1 by user789"
}
```

Response:

```json
{
  "id": "945ac5d5-8d48-4d80-b06f-12fc8d0c4d36",
  "userId": "user789",
  "content": "comment2 to user123 post 1 by user789",
  "date": "2025-04-27T15:51:11.863Z"
}
```

**List comments**

URL:

```
http://localhost:8082/posts/0d13c9c1-757a-4f66-88fb-e8988ca78de0/comments
```

Body:

```json
{
  "page": 1,
  "pageSize": 50
}
```

Response:

```json
{
  "comments": [
    {
      "id": "945ac5d5-8d48-4d80-b06f-12fc8d0c4d36",
      "userId": "user789",
      "content": "comment2 to user123 post 1 by user789",
      "date": "2025-04-27T15:51:11.863Z"
    },
    {
      "id": "54ed2402-9fa0-4444-8c10-651df27da68e",
      "userId": "user456",
      "content": "👍",
      "date": "2025-04-27T15:08:54.799Z"
    },
    {
      "id": "d2376782-38b3-4629-9014-9cd5f34c998f",
      "userId": "user789",
      "content": "comment to user123 post 1 by user789",
      "date": "2025-04-27T15:08:26.675Z"
    }
  ]
}
```

### Reaction query examples

**Add reaction**

URL:

```
http://localhost:8082/posts/0d13c9c1-757a-4f66-88fb-e8988ca78de0/reactions
```

Body:

```json
{
  "postId": "0d13c9c1-757a-4f66-88fb-e8988ca78de0",
  "userId": "user-999",
  "reaction": "REACTION_LIKE"
}
```

Response:

```json
{
  "postId": "0d13c9c1-757a-4f66-88fb-e8988ca78de0",
  "userId": "user-999",
  "reaction": "REACTION_LIKE"
}
```

--------------------------------------------------------------------------------

**Remove reaction**

URL:

```
http://localhost:8082/posts/0d13c9c1-757a-4f66-88fb-e8988ca78de0/reactions
```

Body:

```json
{
  "postId": "0d13c9c1-757a-4f66-88fb-e8988ca78de0",
  "userId": "user-999"
}
```

Response:

```
HTTP 204 No Content
```

## Posts

Posts section of this service has the following actions:

- Create a post
- Get a post by ID
- List all posts (with pagination)
- List all posts for a specific user (with pagination)

Post format:

```proto
enum AttachmentType {
  ATTACHMENT_TYPE_UNSPECIFIED = 0;
  ATTACHMENT_TYPE_IMAGE = 1;
  ATTACHMENT_TYPE_VIDEO = 2;
}

message PostAttachment {
  string id = 1;
  string post_id = 2;
  AttachmentType type = 3;
  int32 position = 4;
  string minio_id = 5;
}

message Post {
  string id = 1;
  string user_id = 2;
  optional string content = 3;
  repeated PostAttachment attachments = 4;
  google.protobuf.Timestamp date = 5;
  repeated PostReaction reactions = 6;
  repeated PostComment comments = 7;
}
```

Actions for posts:

```proto
service FeedService {
  rpc CreatePost(CreatePostRequest) returns (Post);
  rpc GetPost(GetPostRequest) returns (Post);
  rpc ListUserPosts(ListUserPostsRequest) returns (ListPostsResponse);
  rpc ListPosts(ListPostsRequest) returns (ListPostsResponse);
}

message CreatePostRequest {
  Post post = 1;
}

message GetPostRequest {
  string id = 1;
}

message ListUserPostsRequest {
  string user_id = 1;
  string viewer_id = 2;
  int32 page = 3;
  int32 page_size = 4;
}

message ListPostsRequest {
  string viewer_id = 1;
  int32 page = 2;
  int32 page_size = 3;
}

message ListPostsResponse {
  repeated Post posts = 1;
}
```

## Comments

Comments section of this service has the following actions:

- Add a comment to a post
- List all comments for a post (with pagination)

Comment format:

```proto
message PostComment {
  string id = 1;
  string user_id = 2;
  string content = 3;
  google.protobuf.Timestamp date = 4;
  repeated PostReaction reactions = 5;
}
```

Comment actions:

```proto
service FeedService {
  rpc CreateComment(CreateCommentRequest) returns (PostComment);
  rpc ListComments(ListCommentsRequest) returns (ListCommentsResponse);
}

message CreateCommentRequest {
  string post_id = 1;
  PostComment comment = 2;
}

message ListCommentsRequest {
  string post_id = 1;
  int32 page = 2;
  int32 page_size = 3;
}

message ListCommentsResponse {
  repeated PostComment comments = 1;
}
```

## Reactions

Reactions section of this service has the following actions:

- Add a reaction to a post
- Remove a reaction from a post

Reaction format:

```proto
enum Reaction {
  REACTION_UNSPECIFIED = 0;
  REACTION_LIKE = 1;
  REACTION_LOVE = 2;
  REACTION_HAHA = 3;
  REACTION_WOW = 4;
  REACTION_SAD = 5;
  REACTION_ANGRY = 6;
}

message PostReaction {
  string post_id = 1;
  string user_id = 2;
  Reaction reaction = 3;
}
```

Reaction actions:

```proto
service FeedService {
  rpc AddReaction(AddReactionRequest) returns (PostReaction);
  rpc RemoveReaction(RemoveReactionRequest) returns (google.protobuf.Empty);
}
message AddReactionRequest {
  string post_id = 1;
  string user_id = 2;
  Reaction reaction = 3;
}

message RemoveReactionRequest {
  string post_id = 1;
  string user_id = 2;
}
```

## SQL

```sql
CREATE TABLE posts (
    id VARCHAR(36) PRIMARY KEY,
    userid VARCHAR(255) NOT NULL,
    content TEXT,
    date VARCHAR(255) NOT NULL
);

CREATE TABLE post_attachments (
    id VARCHAR(36) PRIMARY KEY,
    postid VARCHAR(36) NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    position INT NOT NULL,
    minio_id VARCHAR(255) NOT NULL
);

CREATE TABLE post_comments (
    id VARCHAR(36) PRIMARY KEY,
    postid VARCHAR(36) NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    userid VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    date VARCHAR(255) NOT NULL
);

CREATE TABLE post_reactions (
    postid VARCHAR(36) NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    userid VARCHAR(255) NOT NULL,
    reaction VARCHAR(50) NOT NULL,
    PRIMARY KEY (postid, userid)
);

CREATE TABLE comment_reactions (
    commentid VARCHAR(36) NOT NULL REFERENCES post_comments(id) ON DELETE CASCADE,
    userid VARCHAR(255) NOT NULL,
    reaction VARCHAR(50) NOT NULL,
    PRIMARY KEY (commentid, userid)
);

CREATE INDEX idx_posts_user ON posts(userid);
CREATE INDEX idx_comments_post ON post_comments(postid);
CREATE INDEX idx_attach_post  ON post_attachments(postid);
```
