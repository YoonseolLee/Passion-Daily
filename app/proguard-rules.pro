# Firebase
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Firestore
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.firestore.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
   *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
   kotlinx.serialization.KSerializer serializer(...);
}

-keep class com.example.passionDaily.domain.model.** { *; }
-keep class com.example.passionDaily.data.model.** { *; }
-keep class com.example.passionDaily.data.entity.** { *; }
-keep class com.example.passionDaily.data.dto.** { *; }

# Kotlin Serialization Specific Rules
-keep,includedescriptorclasses class com.example.passionDaily.**.*$Companion { *; }
-keep class com.example.passionDaily.**.*$Creator { *; }
-keep class com.example.passionDaily.** { *; }
-keepclassmembers @kotlinx.serialization.Serializable class com.example.passionDaily.** {
   *** Companion;
   *** INSTANCE;
   kotlinx.serialization.KSerializer serializer(...);
}

# 익명 내부 클래스 보존
-keepattributes InnerClasses,EnclosingMethod
-keepattributes Signature,Exceptions,*Annotation*

# Gson Specific Rules
-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# Gson TypeToken specific rules
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class com.google.gson.** {*;}

# Keep generic signature of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keepclassmembers class ** {
    java.lang.reflect.Type type;
    com.google.gson.reflect.TypeToken *;
}

# Gson 관련 규칙
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Exceptions

# Gson 클래스 보존
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.** { *; }
-keep class com.google.gson.internal.** { *; }

# 타입 인자를 유지
-keep,allowobfuscation class * extends com.google.gson.reflect.TypeToken
-keep,allowobfuscation class * extends com.google.gson.TypeAdapter
-keep,allowobfuscation class * implements com.google.gson.TypeAdapterFactory
-keep,allowobfuscation class * implements com.google.gson.JsonSerializer
-keep,allowobfuscation class * implements com.google.gson.JsonDeserializer

# R8 강력 최적화 방지
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# 모든 Gson 내부 클래스 보존
-keep class com.google.gson.internal.$Gson$Types { *; }

# Firebase Cloud Messaging 관련 클래스 보존
-keep class com.google.firebase.messaging.** { *; }
-keep class com.google.firebase.iid.** { *; }

# JSON 처리 관련 클래스 보존
-keep class org.json.** { *; }
-keep class kotlinx.serialization.** { *; }

# Coroutines 관련 클래스 보존
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# OkHttp 관련 클래스 보존
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Firestore Model 클래스 보존
-keep class com.example.passionDaily.user.data.remote.model.** { *; }
-keepclassmembers class com.example.passionDaily.user.data.remote.model.** {
  <init>();
}
