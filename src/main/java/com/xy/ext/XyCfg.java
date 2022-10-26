package com.xy.ext;

public interface XyCfg {
    default String getStr(Class<?> interfaceClass, String mname) {
        try {
            DefaultStrValue annotation = interfaceClass
                    .getMethod(mname)
                    .getAnnotation(DefaultStrValue.class);

            if (null == annotation) return "";

            return annotation.value();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return "";
        }
    }

    default int getInt(Class<?> interfaceClass, String mname) {
        try {
            DefaultIntValue annotation = interfaceClass
                    .getMethod(mname)
                    .getAnnotation(DefaultIntValue.class);

            if (null == annotation) return 0;

            return annotation.value();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return 0;
        }
    }

    default boolean getBool(Class<?> interfaceClass, String mname) {
        try {
            DefaultBoolValue annotation = interfaceClass
                    .getMethod(mname)
                    .getAnnotation(DefaultBoolValue.class);

            if (null == annotation) return false;

            return annotation.value();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
    }
}
