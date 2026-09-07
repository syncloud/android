-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*, RuntimeVisibleAnnotations

-keep class org.syncloud.android.core.redirect.model.** { *; }
-keep class org.syncloud.android.core.platform.model.** { *; }
-keep class org.syncloud.android.core.common.BaseResult { *; }
-keep class org.syncloud.android.core.common.Result { *; }
-keep class org.syncloud.android.core.common.ParameterMessages { *; }

-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata { public <methods>; }

-dontwarn org.slf4j.**
-dontwarn javax.xml.**
-dontwarn java.beans.**
