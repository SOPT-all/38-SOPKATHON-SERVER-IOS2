insert into members (email, password, name, role, created_at, updated_at)
values (
    'test@sopt.org',
    '$2a$10$Jn68maAQyKfp1M.bNg/JtOcbr12Uf5UqkThaDMRZcxvrW7PPviEVG',
    '테스트계정',
    'ROLE_USER',
    current_timestamp(6),
    current_timestamp(6)
)
on duplicate key update email = email;

insert into users (id, nickname)
values (1, '핀고')
on duplicate key update nickname = nickname;

insert into spots (id, name)
values (1, '성수동 카페거리')
on duplicate key update name = name;
