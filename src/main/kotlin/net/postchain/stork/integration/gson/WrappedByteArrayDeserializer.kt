package net.postchain.stork.integration.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import net.postchain.common.exception.ProgrammerMistake
import net.postchain.common.hexStringToWrappedByteArray
import net.postchain.common.types.WrappedByteArray
import java.lang.reflect.Type

class WrappedByteArrayDeserializer : JsonDeserializer<WrappedByteArray> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) =
            if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
                val hexString = trimZeroPrefix(json.asJsonPrimitive.asString)
                hexString.hexStringToWrappedByteArray()
            } else {
                throw ProgrammerMistake("Only JSON strings can be deserialized to WrappedByteArray")
            }
}

private fun trimZeroPrefix(hex: String) =
        if (hex.startsWith("0x")) {
            hex.substring(2)
        } else hex

