package com.project.analytics.auth.mapper

import com.project.analytics.auth.model.User
import org.apache.ibatis.annotations.*

@Mapper
interface UserMapper {

    @Select("""
        SELECT id, name, email, password_hash, multiple_session_allowed, age, gender_id, age_bucket_id, add_date, delete_info
        FROM app.user
        WHERE email = #{email}
          AND delete_info IS NULL
    """)
    @Results(
        Result(property = "id", column = "id"),
        Result(property = "name", column = "name"),
        Result(property = "email", column = "email"),
        Result(property = "passwordHash", column = "password_hash"),
        Result(property = "multipleSessionAllowed", column = "multiple_session_allowed"),
        Result(property = "age", column = "age"),
        Result(property = "genderId", column = "gender_id"),
        Result(property = "ageBucketId", column = "age_bucket_id"),
        Result(property = "addDate", column = "add_date"),
        Result(property = "deleteInfo", column = "delete_info")
    )
    fun findActiveByEmail(@Param("email") email: String): User?

    @Select("""
        SELECT id, name, email, password_hash, multiple_session_allowed, age, gender_id, age_bucket_id, add_date, delete_info
        FROM app.user
        WHERE id = #{id}
          AND delete_info IS NULL
    """)
    @Results(
        Result(property = "id", column = "id"),
        Result(property = "name", column = "name"),
        Result(property = "email", column = "email"),
        Result(property = "passwordHash", column = "password_hash"),
        Result(property = "multipleSessionAllowed", column = "multiple_session_allowed"),
        Result(property = "age", column = "age"),
        Result(property = "genderId", column = "gender_id"),
        Result(property = "ageBucketId", column = "age_bucket_id"),
        Result(property = "addDate", column = "add_date"),
        Result(property = "deleteInfo", column = "delete_info")
    )
    fun findActiveById(@Param("id") id: Long): User?

    @Insert("""
        INSERT INTO app.user (name, email, password_hash, multiple_session_allowed, age, gender_id, age_bucket_id)
        VALUES (#{name}, #{email}, #{passwordHash}, #{multipleSessionAllowed}, #{age}, #{genderId}, #{ageBucketId})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun insert(user: User): Int

    @Select("""
        SELECT COUNT(*) > 0
        FROM app.user
        WHERE email = #{email}
          AND delete_info IS NULL
    """)
    fun existsActiveByEmail(@Param("email") email: String): Boolean
}
