create schema if not exists app;

alter table app."user"
    add column if not exists gender_id smallint;

alter table app."user"
    add column if not exists age_bucket_id smallint;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'app' AND table_name = 'user' AND column_name = 'gender'
    ) THEN
        EXECUTE '
            update app."user" u
            set gender_id = g.id
            from app.gender g
            where u.gender_id is null
              and u.gender is not null
              and lower(trim(u.gender)) = lower(g.name)
              and g.delete_info is null';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'app' AND table_name = 'user' AND column_name = 'age'
    ) THEN
        EXECUTE '
            update app."user" u
            set age_bucket_id = ab.id
            from app.age_bucket ab
            where u.age_bucket_id is null
              and (
                    (ab.min_age is null or u.age >= ab.min_age)
                and (ab.max_age is null or u.age <= ab.max_age)
              )
              and ab.delete_info is null';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'app' AND table_name = 'user' AND column_name = 'gender_id'
        AND is_nullable = 'YES'
    ) THEN
        -- Set default for any remaining nulls before making NOT NULL
        UPDATE app."user" SET gender_id = 1 WHERE gender_id IS NULL;
        ALTER TABLE app."user" ALTER COLUMN gender_id SET NOT NULL;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'app' AND table_name = 'user' AND column_name = 'age_bucket_id'
        AND is_nullable = 'YES'
    ) THEN
        -- Set default for any remaining nulls before making NOT NULL
        UPDATE app."user" SET age_bucket_id = 1 WHERE age_bucket_id IS NULL;
        ALTER TABLE app."user" ALTER COLUMN age_bucket_id SET NOT NULL;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_user_gender' AND table_schema = 'app'
    ) THEN
        ALTER TABLE app."user"
            ADD CONSTRAINT fk_user_gender
            FOREIGN KEY (gender_id) REFERENCES app.gender(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_user_age_bucket' AND table_schema = 'app'
    ) THEN
        ALTER TABLE app."user"
            ADD CONSTRAINT fk_user_age_bucket
            FOREIGN KEY (age_bucket_id) REFERENCES app.age_bucket(id);
    END IF;
END $$;

create index if not exists ix_user_gender_id_active
    on app."user" (gender_id)
    where delete_info is null;

create index if not exists ix_user_age_bucket_id_active
    on app."user" (age_bucket_id)
    where delete_info is null;
