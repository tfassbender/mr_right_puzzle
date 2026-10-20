package com.tfassbender.mrright.storage

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder

/**
 * The [ObjectMapper] for stored files. Field-based visibility writes only backing fields, so derived
 * getters never end up in a file; instants are written as ISO-8601 strings.
 */
object JsonMapper {
    fun create(): ObjectMapper = jacksonMapperBuilder()
        .visibility(PropertyAccessor.ALL, Visibility.NONE)
        .visibility(PropertyAccessor.FIELD, Visibility.ANY)
        .visibility(PropertyAccessor.CREATOR, Visibility.ANY)
        .addModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build()
}
