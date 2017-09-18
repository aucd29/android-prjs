package net.sarangnamu.eternium.commons

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * Created by <a href="mailto:aucd29@hanwha.com">Burke Choi</a> on 2017. 9. 18.. <p/>
 */

class Json private constructor() {
    private object Holder { val INSTANCE = Json() }
    val mapper = jacksonObjectMapper()

    companion object {
        val instance: Json by lazy { Holder.INSTANCE }
    }

    init {
        with (mapper) {
            configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        }
    }

    fun string(obj: Any): String {
        return mapper.writeValueAsString(obj)
    }
}
