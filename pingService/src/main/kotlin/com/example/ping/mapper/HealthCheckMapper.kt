package com.example.ping.mapper

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select

@Mapper
interface HealthCheckMapper {
    @Select("SELECT 1")
    fun ping(): Int
}
