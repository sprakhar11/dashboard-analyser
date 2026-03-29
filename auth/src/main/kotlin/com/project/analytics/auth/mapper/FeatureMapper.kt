package com.project.analytics.auth.mapper

import com.project.analytics.auth.model.Feature
import org.apache.ibatis.annotations.*

@Mapper
interface FeatureMapper {

    @Select("""
        SELECT id, name, delete_info
        FROM app.feature
        WHERE delete_info IS NULL
        ORDER BY id ASC
    """)
    @Results(
        Result(property = "id", column = "id"),
        Result(property = "name", column = "name"),
        Result(property = "deleteInfo", column = "delete_info")
    )
    fun findAllActive(): List<Feature>

    @Select("""
        SELECT id, name, delete_info
        FROM app.feature
        WHERE id = #{id}
          AND delete_info IS NULL
    """)
    @Results(
        Result(property = "id", column = "id"),
        Result(property = "name", column = "name"),
        Result(property = "deleteInfo", column = "delete_info")
    )
    fun findActiveById(@Param("id") id: Short): Feature?
}
