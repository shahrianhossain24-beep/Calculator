# Findings from read-only inspection of the supplied Samsung Calculator APK

Method: unzip listing, manifest string extraction, and class-name strings from classes.dex.
REA itself could not run (sandbox has no network/npm/JADX), and no decompilation or resource decoding was done.
Nothing from the APK (code, drawables, layouts, animation XML) was copied.

| # | Finding | Label | Evidence |
|---|---------|-------|----------|
| 1 | Original package is com.sec.android.app.popupcalculator | seen | manifest strings |
| 2 | APK ships lib/arm64-v8a, armeabi-v7a, x86, x86_64 (no plain armeabi) | seen | `unzip -l` |
| 3 | Resources are nodpi-split and name-obfuscated (res/-B.png, ...) | seen | `unzip -l`, file name |
| 4 | Samsung-only hooks: keyguard shortcut, multiwindow, flip-cover launcher, SecretCodeIME, backup/restore receiver, widgets (2x2 + Glance) | seen | manifest strings |
| 5 | Extra screens: unit converter, mortgage (base/detail/result) | seen | NewUnitConverterActivity, Mortgage* activities |
| 6 | Calculation engine classes: CalculatorLogic, CalculateTool, TextCore, SyntaxException | seen | dex class names |
| 7 | Animations exist for: normal<->scientific keypad, history open/close, clear-all text, text translation on enter, key recoil, suggestion chips | seen (names only) | ChangeKeypadAnimationUtils, ShowHideHistoryAnimationUtils, BaseAnimatedEditText.animateClearAll, TextController.onEnterAnimation, SeslRecoilAnimator, ChipItemAnimator |
| 8 | Haptics used | seen | Vibrator / VibrationEffect refs |
| 9 | Recoil = press-down squash with spring return; history slides keypad/display | inferred | class/method names only |
| 10 | Durations, easing curves, interpolators | unknown | not decoded |
| 11 | Key grid arrangement, portrait/landscape layouts, where history sits | unknown | layouts not decoded (obfuscated resources.arsc) |
| 12 | Whether the original uses a blue gradient accent | unknown | your requirement, not verified in the APK |

Consequences for the rebuild: layout is a conventional calculator grid, and animation timings are my own.
Suggestion chips, converter, mortgage, widgets and backup/restore were not reimplemented.
