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
