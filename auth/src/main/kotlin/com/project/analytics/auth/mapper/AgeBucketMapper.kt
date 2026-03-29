package com.project.analytics.auth.mapper

import com.project.analytics.auth.model.AgeBucket
import org.apache.ibatis.annotations.*

@Mapper
interface AgeBucketMapper {

    @Select("""
        SELECT id, code, name, min_age, max_age, sort_order, delete_info
        FROM app.age_bucket
        WHERE delete_info IS NULL
        ORDER BY sort_order ASC
    """)
    @Results(
        Result(property = "id", column = "id"),
        Result(property = "code", column = "code"),
        Result(property = "name", column = "name"),
        Result(property = "minAge", column = "min_age"),
        Result(property = "maxAge", column = "max_age"),
        Result(property = "sortOrder", column = "sort_order"),
        Result(property = "deleteInfo", column = "delete_info")
    )
    fun findAllActive(): List<AgeBucket>

    @Select("""
        SELECT id, code, name, min_age, max_age, sort_order, delete_info
        FROM app.age_bucket
        WHERE id = #{id}
          AND delete_info IS NULL
    """)
    @Results(
        Result(property = "id", column = "id"),
        Result(property = "code", column = "code"),
        Result(property = "name", column = "name"),
        Result(property = "minAge", column = "min_age"),
        Result(property = "maxAge", column = "max_age"),
        Result(property = "sortOrder", column = "sort_order"),
        Result(property = "deleteInfo", column = "delete_info")
    )
    fun findActiveById(@Param("id") id: Short): AgeBucket?

    @Select("""
        SELECT id, code, name, min_age, max_age, sort_order, delete_info
        FROM app.age_bucket
        WHERE delete_info IS NULL
          AND (min_age IS NULL OR min_age <= #{age})
          AND (max_age IS NULL OR max_age >= #{age})
        LIMIT 1
    """)
    @Results(
        Result(property = "id", column = "id"),
        Result(property = "code", column = "code"),
        Result(property = "name", column = "name"),
        Result(property = "minAge", column = "min_age"),
        Result(property = "maxAge", column = "max_age"),
        Result(property = "sortOrder", column = "sort_order"),
        Result(property = "deleteInfo", column = "delete_info")
    )
    fun findByAge(@Param("age") age: Int): AgeBucket?
}
