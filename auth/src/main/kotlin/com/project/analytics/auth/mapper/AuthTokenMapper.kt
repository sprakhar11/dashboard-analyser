package com.project.analytics.auth.mapper

import com.project.analytics.auth.model.AuthToken
import org.apache.ibatis.annotations.*
import java.time.LocalDateTime

@Mapper
interface AuthTokenMapper {

    @Select("""
        SELECT id, user_id, token, browser_id, add_date, log_out_date, expiry_date, delete_info
        FROM app.auth_token
        WHERE token = #{token}
          AND (delete_info IS NULL OR delete_info->>'deleted' != 'true')
          AND log_out_date IS NULL
          AND expiry_date > now()
    """)
    @Results(
        Result(property = "id", column = "id"),
        Result(property = "userId", column = "user_id"),
        Result(property = "token", column = "token"),
        Result(property = "browserId", column = "browser_id"),
        Result(property = "addDate", column = "add_date"),
        Result(property = "logOutDate", column = "log_out_date"),
        Result(property = "expiryDate", column = "expiry_date"),
        Result(property = "deleteInfo", column = "delete_info")
    )
    fun findActiveByToken(@Param("token") token: String): AuthToken?

    @Select("""
        SELECT id, user_id, token, browser_id, add_date, log_out_date, expiry_date, delete_info
        FROM app.auth_token
        WHERE user_id = #{userId}
          AND browser_id = #{browserId}
          AND (delete_info IS NULL OR delete_info->>'deleted' != 'true')
          AND log_out_date IS NULL
          AND expiry_date > now()
    """)
    @Results(
        Result(property = "id", column = "id"),
        Result(property = "userId", column = "user_id"),
        Result(property = "token", column = "token"),
        Result(property = "browserId", column = "browser_id"),
        Result(property = "addDate", column = "add_date"),
        Result(property = "logOutDate", column = "log_out_date"),
        Result(property = "expiryDate", column = "expiry_date"),
        Result(property = "deleteInfo", column = "delete_info")
    )
    fun findActiveByUserIdAndBrowserId(@Param("userId") userId: Long, @Param("browserId") browserId: String): AuthToken?

    @Select("""
        SELECT id, user_id, token, browser_id, add_date, log_out_date, expiry_date, delete_info
        FROM app.auth_token
        WHERE user_id = #{userId}
          AND (delete_info IS NULL OR delete_info->>'deleted' != 'true')
          AND log_out_date IS NULL
          AND expiry_date > now()
        LIMIT 1
    """)
    @Results(
        Result(property = "id", column = "id"),
        Result(property = "userId", column = "user_id"),
        Result(property = "token", column = "token"),
        Result(property = "browserId", column = "browser_id"),
        Result(property = "addDate", column = "add_date"),
        Result(property = "logOutDate", column = "log_out_date"),
        Result(property = "expiryDate", column = "expiry_date"),
        Result(property = "deleteInfo", column = "delete_info")
    )
    fun findAnyActiveByUserId(@Param("userId") userId: Long): AuthToken?

    @Insert("""
        INSERT INTO app.auth_token (user_id, token, browser_id, expiry_date)
        VALUES (#{userId}, #{token}, #{browserId}, #{expiryDate})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun insert(authToken: AuthToken): Int

    @Update("""
        UPDATE app.auth_token
        SET log_out_date = #{logOutDate}
        WHERE id = #{tokenId}
    """)
    fun setLogOutDate(@Param("tokenId") tokenId: Long, @Param("logOutDate") logOutDate: LocalDateTime): Int
}
