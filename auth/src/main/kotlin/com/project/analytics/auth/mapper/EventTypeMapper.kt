package com.project.analytics.auth.mapper

import com.project.analytics.auth.model.EventType
import org.apache.ibatis.annotations.*

@Mapper
interface EventTypeMapper {

    @Select("""
        SELECT id, name, delete_info
        FROM app.event_type
        WHERE delete_info IS NULL
        ORDER BY id ASC
    """)
    @Results(
        Result(property = "id", column = "id"),
        Result(property = "name", column = "name"),
        Result(property = "deleteInfo", column = "delete_info")
    )
    fun findAllActive(): List<EventType>

    @Select("""
        SELECT id, name, delete_info
        FROM app.event_type
        WHERE id = #{id}
          AND delete_info IS NULL
    """)
    @Results(
        Result(property = "id", column = "id"),
        Result(property = "name", column = "name"),
        Result(property = "deleteInfo", column = "delete_info")
    )
    fun findActiveById(@Param("id") id: Short): EventType?
}
