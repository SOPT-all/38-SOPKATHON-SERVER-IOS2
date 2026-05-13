create table members (
    id bigint not null auto_increment,
    email varchar(100) not null,
    password varchar(100) not null,
    name varchar(50) not null,
    role varchar(30) not null,
    created_at timestamp(6) not null,
    updated_at timestamp(6) not null,
    primary key (id),
    unique key uk_members_email (email)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table refresh_tokens (
    id bigint not null auto_increment,
    member_id bigint not null,
    token_hash varchar(128) not null,
    expires_at timestamp(6) not null,
    created_at timestamp(6) not null,
    primary key (id),
    unique key uk_refresh_tokens_token_hash (token_hash),
    key idx_refresh_tokens_member_id (member_id),
    key idx_refresh_tokens_expires_at (expires_at)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;
