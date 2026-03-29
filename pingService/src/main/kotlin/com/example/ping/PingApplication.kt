package com.example.ping

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.example.ping", "com.project.analytics.auth"])
@MapperScan("com.example.ping.mapper", "com.project.analytics.auth.mapper")
class PingApplication

fun main(args: Array<String>) {
    runApplication<PingApplication>(*args)
}
