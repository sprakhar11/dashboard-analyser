package com.example.ping

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@MapperScan("com.example.ping.mapper")
class PingApplication

fun main(args: Array<String>) {
    runApplication<PingApplication>(*args)
}
