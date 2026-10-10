# إصلاح مشكلة إغلاق لعبة "ارتقي عالياً" بعد شاشة البداية

## وصف المشكلة
عند تشغيل اللعبة على أندرويد، تظهر شاشة بداية محرك Godot (شعار Godot) لثانية واحدة، ثم تُغلق اللعبة فورًا وتعود إلى الشاشة الرئيسية للهاتف.

## السبب الجذري (Root Cause)
المشكلة **ليست** في إعدادات البناء (workflow) ولا في توقيع الـ APK ولا في المعمارية (arm64). السبب الحقيقي هو **خطأ في تحليل سكربت GDScript (Parse Error)** يمنع المشهد الرئيسي `Main.tscn` من التحميل، فينهار التطبيق بعد شاشة البداية مباشرة.

### الخطأ الأول (السبب الرئيسي للانهيار)
في Godot 4.6 أضاف المحرك دالة أصلية جديدة باسم `draw_ellipse()` إلى الفئة الأساسية `CanvasItem`. المشروع يعرّف دالة مخصّصة بنفس الاسم:

```gdscript
func draw_ellipse(center: Vector2, radii: Vector2, color: Color) -> void:
```

لأن الاسم يطابق دالة أصلية لكن بتوقيع (signature) مختلف، يعتبره المحرك "تجاوزًا خاطئًا" ويرفع تحذيرًا يُعامل كخطأ:

```
SCRIPT ERROR: Parse Error: The function signature doesn't match the parent.
Parent signature is "draw_ellipse(Vector2, float, float, Color, bool, float, bool) -> void".
at: GDScript::reload (res://scripts/main.gd:189)

SCRIPT ERROR: Parse Error: The method "draw_ellipse()" overrides a method from native class "CanvasItem".
This won't be called by the engine and may not work as expected. (Warning treated as error.)

ERROR: Failed to load script "res://scripts/main.gd" with error "Parse error".
```

عند فشل تحميل السكربت، يفشل تحميل المشهد الرئيسي بالكامل → تُغلق اللعبة فورًا بعد شاشة البداية.

### الخطأ الثاني (خطأ وقت التشغيل)
في دالة `_draw_hud()` كان هناك خطأ في صيغة النص:

```gdscript
"⚔ %d   🛡 43   💥 12%" % player.attack_power
```

علامة `%` في نهاية النص تُفسَّر كمعرّف تنسيق غير مكتمل:

```
SCRIPT ERROR: incomplete format in operator '%'.
at: _draw_hud (res://scripts/main.gd:134)
```

## الحل المطبَّق

### 1) إعادة تسمية الدالة المخصّصة
تم تغيير اسم الدالة المخصّصة من `draw_ellipse` إلى `_draw_oval` في الملفين `main.gd` و `player.gd`، مع تحديث كل مواضع الاستدعاء:

```gdscript
# قبل
func draw_ellipse(center: Vector2, radii: Vector2, color: Color) -> void:
# بعد
func _draw_oval(center: Vector2, radii: Vector2, color: Color) -> void:
```

### 2) تصحيح صيغة النص
```gdscript
# قبل
"💥 12%" % player.attack_power
# بعد
"💥 12%%" % player.attack_power
```

## التحقق
تم تشغيل المشروع المصحّح بواسطة Godot 4.6.3 (نفس إصدار البناء) واختبار العرض الفعلي:
- لا توجد أخطاء Parse.
- لا توجد أخطاء وقت تشغيل.
- اللعبة تُعرض كاملة (الواجهة، الخريطة المصغّرة، المهام، الشخصية، المباني).

## الملفات
- `ArtaqiAlya_Godot_V1_Fixed_v4.zip` — المشروع الكامل بعد الإصلاح (استخدم هذا للبناء).
- تم إصلاح النسخ التشخيصية الخمسة ونسخ V1 أيضاً.
- تم تحديث `.github/workflows/godot-build-v3.yml` ليبني `ArtaqiAlya_Godot_V1_Fixed_v4.zip`.

## ملاحظة مهمة
جميع النسخ التشخيصية (Diagnostic) كانت تحتوي على نفس الخطأ `draw_ellipse`، ولهذا لم تنجح طريقة التشخيص في تحديد المشكلة — فالخطأ كان موجودًا في كل النسخ.
