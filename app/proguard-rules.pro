# قواعد ProGuard الافتراضية لتجنب تشويه أسماء كلاسات النظام وواجهات العرض

-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*

# الحفاظ على كلاسات الـ Activity لتعمل بدون مشاكل
-keep public class * extends android.app.Activity

# الحفاظ على كلاسات الدعم وواجهات العرض الأساسية
-keepclassmembers class * {
    public void *(android.view.View);
}
