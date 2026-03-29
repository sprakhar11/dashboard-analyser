package com.project.analytics.auth.mapper

import com.project.analytics.auth.model.Gender
import org.apache.ibatis.annotations.*

@Mapper
interface GenderMapper {

    @Select("""
        SELECT id, code, name, sort_order, delete_info
        FROM app.gender
        WHERE delete_info IS NULL
        ORDER BY sort_order ASC
    """)
    @Results(
        Result(property = "id", column = "id"),
        Result(property = "code", column = "code"),
        Result(property = "name", column = "name"),
        Result(property = "sortOrder", column = "sort_order"),
        Result(property = "deleteInfo", column = "delete_info")
    )
    fun findAllActive(): List<Gender>

    @Select("""
        SELECT id, code, name, sort_order, delete_info
        FROM app.gender
        WHERE id = #{id}
          AND delete_info IS NULL
    """)
    @Results(
        Result(property = "id", column = "id"),
        Result(property = "code", column = "code"),
        Result(property = "name", column = "name"),
        Result(property = "sortOrder", column = "sort_order"),
        Result(property = "deleteInfo", column = "delete_info")
    )
    fun findActiveById(@Param("id") id: Short): Gender?
}
