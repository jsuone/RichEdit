package com.example.sample.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ByteArrayMapConverter {

    public static Gson createGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(byte[].class, new ByteArraySerializer());
        gsonBuilder.registerTypeAdapter(byte[].class, new ByteArrayDeserializer());
        return gsonBuilder.create();
    }

    private static class ByteArraySerializer implements JsonSerializer<byte[]> {
        @Override
        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(Base64.getEncoder().encodeToString(src));
        }
    }

    private static class ByteArrayDeserializer implements JsonDeserializer<byte[]> {
        @Override
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return Base64.getDecoder().decode(json.getAsString());
        }
    }
/*
    public static void main(String[] args) {
        // 创建一个包含 byte[] 数据的 Map
        Map<String, byte[]> map = new HashMap<>();
        map.put("key1", "value1".getBytes());
        map.put("key2", "value2".getBytes());

        // 使用定制的 Gson 实例将 Map 转换为字符串
        Gson gson = createGson();
        String jsonString = gson.toJson(map);
        System.out.println("Serialized JSON: " + jsonString);

        // 使用定制的 Gson 实例将字符串转换回 Map 对象
        Map<String, byte[]> deserializedMap = gson.fromJson(jsonString, new TypeToken<Map<String, byte[]>>(){}.getType());
        System.out.println("Deserialized Map: " + deserializedMap);
    }*/
}
