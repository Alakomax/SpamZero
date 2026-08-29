# Room Database R8 / Proguard Rules
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.Query *;
    @androidx.room.Insert *;
    @androidx.room.Update *;
    @androidx.room.Delete *;
}
-keep class com.alakomax.spamzero.data.model.** { *; }
-keep class com.alakomax.spamzero.data.db.** { *; }
