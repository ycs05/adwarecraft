*** Begin Patch
*** Update File: src/main/java/dev/adwarecraft/AdwarecraftClient.java
@@
     private static void closeAd(MinecraftClient client) {
@@
     }
+
+    static void disableForSettings() {
+        adTicksRemaining = 0;
+    }
*** End Patch
