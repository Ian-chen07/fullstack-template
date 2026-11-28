-- 演示用种子数据（重置数据库后执行一次）
-- 用法：全选本文件内容 → 复制 → 粘贴到 H2 控制台 → 点一次 Run
-- 注意：只能执行一次，重复执行会报主键冲突 23505

INSERT INTO project (id, name, total_amount, available_amount, frozen_amount, used_amount)
VALUES (1, '实验室经费', 1000.00, 1000.00, 0.00, 0.00);

INSERT INTO users (username, password, role, project_id) VALUES ('member1',  '123456', 'member',  1);
INSERT INTO users (username, password, role, project_id) VALUES ('teacher1', '123456', 'teacher', 1);
INSERT INTO users (username, password, role, project_id) VALUES ('finance1', '123456', 'finance', 1);

INSERT INTO application (project_id, applicant_id, purpose, amount, status)
VALUES (1, 1, '购买传感器', 600.00, 'DRAFT');
