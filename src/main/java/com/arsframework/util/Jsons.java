package com.arsframework.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Collection;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import com.arsframework.annotation.Min;
import com.arsframework.annotation.Nonnull;

/**
 * JSON处理工具类
 *
 * @author yongqiang.wu
 * @version 2019-03-22 09:38
 */
public abstract class Jsons {
    /**
     * 默认gson
     */
    private static Gson gson = new GsonBuilder().create();

    /**
     * Gson通用类型适配器
     */
    private static class CommonTypeAdapter extends TypeAdapter<Object> {
        protected final int depth; // 对象属性下钻深度（小于1表示不限制深度）

        public CommonTypeAdapter(int depth) {
            this.depth = depth;
        }

        /**
         * 将对象写入json
         *
         * @param writer json写操作对象
         * @param object 对象实例
         * @param level  当前层级
         * @throws IOException IO操作异常
         */
        protected void write(@Nonnull JsonWriter writer, Object object, @Min(1) int level) throws IOException {
            if (object == null || (this.depth > 0 && level > this.depth)) {
                writer.nullValue();
            } else if (object instanceof CharSequence) {
                writer.value(((CharSequence) object).toString());
            } else if (object instanceof Number) {
                writer.value((Number) object);
            } else if (object instanceof Boolean) {
                writer.value((Boolean) object);
            } else if (Objects.isMetaClass(object.getClass())) {
                writer.value(Strings.toString(object));
            } else if (object instanceof Map) {
                writer.beginObject();
                for (Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                    writer.name(Strings.toString(entry.getKey()));
                    write(writer, entry.getValue(), 1);
                }
                writer.endObject();
            } else if (object instanceof Collection) {
                writer.beginArray();
                for (Object value : ((Collection<?>) object)) {
                    write(writer, value, 1);
                }
                writer.endArray();
            } else if (object instanceof Object[]) {
                writer.beginArray();
                for (Object value : ((Object[]) object)) {
                    write(writer, value, 1);
                }
                writer.endArray();
            } else if (object instanceof int[]) {
                writer.beginArray();
                for (int value : ((int[]) object)) {
                    writer.value(value);
                }
                writer.endArray();
            } else if (object instanceof long[]) {
                writer.beginArray();
                for (long value : ((long[]) object)) {
                    writer.value(value);
                }
                writer.endArray();
            } else if (object instanceof short[]) {
                writer.beginArray();
                for (short value : ((short[]) object)) {
                    writer.value(value);
                }
                writer.endArray();
            } else if (object instanceof float[]) {
                writer.beginArray();
                for (float value : ((float[]) object)) {
                    writer.value(value);
                }
                writer.endArray();
            } else if (object instanceof double[]) {
                writer.beginArray();
                for (double value : ((double[]) object)) {
                    writer.value(value);
                }
                writer.endArray();
            } else if (object instanceof char[]) {
                writer.beginArray();
                for (char value : ((char[]) object)) {
                    writer.value(String.valueOf(value));
                }
                writer.endArray();
            } else if (object instanceof byte[]) {
                writer.beginArray();
                for (byte value : ((byte[]) object)) {
                    writer.value(value);
                }
                writer.endArray();
            } else if (object instanceof boolean[]) {
                writer.beginArray();
                for (boolean value : ((boolean[]) object)) {
                    writer.value(value);
                }
                writer.endArray();
            } else {
                writer.beginObject();
                Class<?> type = object.getClass();
                while (type != Object.class) {
                    for (Field field : type.getDeclaredFields()) {
                        if (Modifier.isStatic(field.getModifiers())) {
                            continue;
                        }
                        Object value;
                        field.setAccessible(true);
                        try {
                            value = field.get(object);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                        writer.name(field.getName());
                        if (value instanceof CharSequence) {
                            value = Strings.escape((CharSequence) value);
                        }
                        if (value == null || value instanceof CharSequence || value instanceof Number
                                || value instanceof Boolean || Objects.isMetaClass(value.getClass())) {
                            write(writer, value, level);
                        } else {
                            write(writer, value, level + 1);
                        }
                    }
                    type = type.getSuperclass();
                }
                writer.endObject();
            }
        }

        @Override
        public Object read(JsonReader reader) throws IOException {
            return null;
        }

        @Override
        public void write(JsonWriter writer, Object object) throws IOException {
            this.write(writer, object, 1);
        }

    }

    /**
     * 构建json处理对象
     *
     * @param depth 对象属性下钻深度
     * @return json处理对象
     */
    public static Gson buildGson(int depth) {
        return new GsonBuilder().registerTypeAdapterFactory(new TypeAdapterFactory() {

            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> token) {
                return (TypeAdapter<T>) new CommonTypeAdapter(depth);
            }

        }).disableHtmlEscaping().create();
    }

    /**
     * 将对象转换成JSON字符串
     *
     * @param object 被转换对象
     * @return JSON字符串
     */
    public static String format(Object object) {
        return format(object, -1);
    }

    /**
     * 将对象转换成JSON字符串
     *
     * @param object 被转换对象
     * @param min    是否最小化转换
     * @return JSON字符串
     */
    public static String format(Object object, boolean min) {
        return format(object, min ? 2 : -1);
    }

    /**
     * 将对象转换成JSON字符串
     * <p>
     * 对象向下关联一级
     *
     * @param object 被转换对象
     * @param depth  对象属性关联深度
     * @return JSON字符串
     */
    public static String format(Object object, int depth) {
        return object == null ? null :
                object instanceof CharSequence ? ((CharSequence) object).toString() : buildGson(depth).toJson(object);
    }

    /**
     * 将JSON字符串反转成对象
     *
     * @param json JSON字符串
     * @return 对象实例
     */
    public static Object parse(String json) {
        return parse(Object.class, json);
    }

    /**
     * 将JSON字符串反转成对象
     *
     * @param <T>  数据类型
     * @param type 目标对象类型
     * @param json JSON字符串
     * @return 对象实例
     */
    public static <T> T parse(Class<T> type, String json) {
        return Strings.isEmpty(json) ? null : gson.fromJson(json, type);
    }

}
