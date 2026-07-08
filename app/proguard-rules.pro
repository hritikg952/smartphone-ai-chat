# Proguard rules for Smartphone AI Chat
# Add project specific ProGuard rules here.

# Keep data classes used by the notification system
-keep class com.smartphoneaichat.notification.** { *; }

# Keep BuildConfig so HF_TOKEN is accessible at runtime
-keep class com.smartphoneaichat.BuildConfig { *; }