package com.stoktakip.repository;

/**
 * LIKE desenleri. Kullanıcı metnindeki {@code %}, {@code _} ve {@code \} joker değil düz
 * karakter sayılır; desen {@link #ESCAPE} kaçış karakteriyle kullanılmalıdır.
 */
public final class LikePatterns {
    public static final char ESCAPE = '\\';

    private LikePatterns() {
    }

    /** Kırpılmış metni içeren her değerle eşleşen desen: {@code %metin%}. */
    public static String contains(String value) {
        return "%" + escape(value.trim()) + "%";
    }

    static String escape(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
