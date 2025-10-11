package me.vaan.bibleread.api.data;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FieldValueExtractor {
    // Cache to store MethodHandles for each class
    private static final Map<Class<?>, Map<String, MethodHandle>> GETTER_CACHE = new ConcurrentHashMap<>();

    // Lookup object (can be trusted because we use publicLookup and access private via setAccessible)
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * Main method to extract field values as a map: fieldName -> toString(value)
     *
     * @param obj       The object to extract values from
     * @param blacklist Optional varargs list of field names to exclude
     * @return Map of field names to their string representations
     */
    public static Map<String, String> getFieldStringValues(Object obj, String... blacklist) {
        if (obj == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }

        Class<?> clazz = obj.getClass();

        Set<String> blacklistSet;
        if (blacklist == null) {
            blacklistSet = Collections.emptySet();
        } else {
            blacklistSet = new HashSet<>(Arrays.asList(blacklist));
        }

        Map<String, MethodHandle> fieldGetters = GETTER_CACHE.computeIfAbsent(clazz, FieldValueExtractor::findFieldGetters);

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, MethodHandle> entry : fieldGetters.entrySet()) {
            String fieldName = entry.getKey();
            if (blacklistSet.contains(fieldName)) continue;

            try {
                Object value = entry.getValue().invoke(obj);
                if (value instanceof Collection<?>) {
                    Collection<?> collection = (Collection<?>) value;
                    List<String> totalList = new ArrayList<>(collection.size());
                    for (Object it : collection) {
                        totalList.add(it.toString());
                    }

                    result.put(fieldName, "[" + String.join(", ", totalList) + "]");
                }

                result.put(fieldName, value != null ? value.toString() : "null");
            } catch (Throwable e) {
                result.put(fieldName, "<error: " + e.getMessage() + ">");
            }
        }

        return result;
    }

    /**
     * Uses reflection and MethodHandles to find non-static field getters
     *
     * @param clazz The class to introspect
     * @return Map of field name -> MethodHandle
     */
    private static Map<String, MethodHandle> findFieldGetters(Class<?> clazz) {
        Map<String, MethodHandle> fieldGetterMap = new HashMap<>();

        for (Field field : getAllFields(clazz)) {
            if (Modifier.isStatic(field.getModifiers())) continue;

            try {
                field.setAccessible(true);
                MethodHandle getter = LOOKUP.unreflectGetter(field);
                fieldGetterMap.put(field.getName(), getter);
            } catch (IllegalAccessException e) {
                // Skip inaccessible fields
            }
        }

        return fieldGetterMap;
    }

    /**
     * Recursively collect all fields from the class hierarchy
     *
     * @param clazz The class to introspect
     * @return List of Fields
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}
