package net.postchain.stork.integration.gson

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import net.postchain.common.types.WrappedByteArray
import org.http4k.format.ConfigurableGson

object StorkGsonConfig : ConfigurableGson(
        GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(WrappedByteArray::class.java, WrappedByteArrayDeserializer())
)
