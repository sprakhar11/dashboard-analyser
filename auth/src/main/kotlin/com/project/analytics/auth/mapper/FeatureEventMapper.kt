package com.project.analytics.auth.mapper

import com.project.analytics.auth.dto.FeatureTotalItem
import com.project.analytics.auth.dto.TrendPoint
import com.project.analytics.auth.model.FeatureEvent
import org.apache.ibatis.annotations.*
import java.time.LocalDate

@Mapper
interface FeatureEventMapper {

    @Insert("""
        INSERT INTO app.feature_event (user_id, auth_token_id, feature_id, event_type_id, browser_id, gender_id, age_bucket_id, event_time, meta_info)
        VALUES (#{userId}, #{authTokenId}, #{featureId}, #{eventTypeId}, #{browserId}, #{genderId}, #{ageBucketId}, #{eventTime}, #{metaInfo}::jsonb)
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun insert(featureEvent: FeatureEvent): Int

    @Select("""
        <script>
        SELECT
            f.id AS feature_id,
            f.name AS feature_name,
            COUNT(e.id) AS total_count
        FROM app.feature_event e
        JOIN app.feature f
            ON f.id = e.feature_id
           AND f.delete_info IS NULL
        WHERE e.delete_info IS NULL
          AND e.event_date BETWEEN #{fromDate} AND #{toDate}
          <if test="ageBucketId != null">AND e.age_bucket_id = #{ageBucketId}</if>
          <if test="genderId != null">AND e.gender_id = #{genderId}</if>
        GROUP BY f.id, f.name
        ORDER BY total_count DESC, f.id ASC
        </script>
    """)
    @Results(
        Result(property = "featureId", column = "feature_id"),
        Result(property = "featureName", column = "feature_name"),
        Result(property = "totalCount", column = "total_count")
    )
    fun getFeatureTotals(
        @Param("fromDate") fromDate: LocalDate,
        @Param("toDate") toDate: LocalDate,
        @Param("ageBucketId") ageBucketId: Short?,
        @Param("genderId") genderId: Short?
    ): List<FeatureTotalItem>

    @Select("""
        <script>
        SELECT
            e.event_date::text AS time,
            COUNT(e.id) AS count
        FROM app.feature_event e
        WHERE e.delete_info IS NULL
          AND e.feature_id = #{featureId}
          AND e.event_date BETWEEN #{fromDate} AND #{toDate}
          <if test="ageBucketId != null">AND e.age_bucket_id = #{ageBucketId}</if>
          <if test="genderId != null">AND e.gender_id = #{genderId}</if>
        GROUP BY e.event_date
        ORDER BY e.event_date ASC
        </script>
    """)
    @Results(
        Result(property = "time", column = "time"),
        Result(property = "count", column = "count")
    )
    fun getDailyTrend(
        @Param("featureId") featureId: Short,
        @Param("fromDate") fromDate: LocalDate,
        @Param("toDate") toDate: LocalDate,
        @Param("ageBucketId") ageBucketId: Short?,
        @Param("genderId") genderId: Short?
    ): List<TrendPoint>

    @Select("""
        <script>
        SELECT
            make_timestamp(
                extract(year from e.event_date)::int,
                extract(month from e.event_date)::int,
                extract(day from e.event_date)::int,
                e.event_hour::int, 0, 0
            ) AT TIME ZONE 'UTC' AS bucket_time,
            COUNT(e.id) AS total_count
        FROM app.feature_event e
        WHERE e.delete_info IS NULL
          AND e.feature_id = #{featureId}
          AND e.event_date BETWEEN #{fromDate} AND #{toDate}
          <if test="ageBucketId != null">AND e.age_bucket_id = #{ageBucketId}</if>
          <if test="genderId != null">AND e.gender_id = #{genderId}</if>
        GROUP BY e.event_date, e.event_hour
        ORDER BY bucket_time ASC
        </script>
    """)
    @Results(
        Result(property = "time", column = "bucket_time"),
        Result(property = "count", column = "total_count")
    )
    fun getHourlyTrend(
        @Param("featureId") featureId: Short,
        @Param("fromDate") fromDate: LocalDate,
        @Param("toDate") toDate: LocalDate,
        @Param("ageBucketId") ageBucketId: Short?,
        @Param("genderId") genderId: Short?
    ): List<TrendPoint>
}
