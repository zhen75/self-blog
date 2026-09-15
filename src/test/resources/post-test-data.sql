SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE posts;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO posts (title, summary, content, status)
VALUES
    ('Post 1', 'Summary 1', 'Content 1', 'published'),
    ('Post 2', 'Summary 2', 'Content 2', 'published'),
    ('Post 3', 'Summary 3', 'Content 3', 'draft'),
    ('Post 4', 'Summary 4', 'Content 4', 'draft'),
    ('Post 5', 'Summary 5', 'Content 5', 'published'),
    ('Post 6', 'Summary 6', 'Content 6', 'draft'),
    ('Post 7', 'Summary 7', 'Content 7', 'published'),
    ('Post 8', 'Summary 8', 'Content 8', 'draft'),
    ('Post 9', 'Summary 9', 'Content 9', 'published'),
    ('Post 10', 'Summary 10', 'Content 10', 'draft'),
    ('Post 11', 'Summary 11', 'Content 11', 'published'),
    ('Post 12', 'Summary 12', 'Content 12', 'draft');