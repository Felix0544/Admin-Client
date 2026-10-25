# AdminClient (Fabric, Minecraft 1.21.4)

Cheat-Mod mit Menue. Alles funktioniert NUR im Singleplayer oder mit OP-Rechten (Level 2+).

## Bedienung
- **G** oeffnet/schliesst das Menue (Taste aenderbar unter Optionen > Steuerung > AdminClient)
- Reiter oben wechseln, Linksklick auf ein Modul = an/aus
- [-] [+] neben einem Modul aendern dessen Einstellungen

## Module
**PvP**
- Auto Anchor: Respawn-Anker neben den naechsten Spieler setzen, mit Glowstone laden, zuenden
  (Anker + Glowstone in die Hotbar; explodiert nur in Overworld/End)
- Kill Aura: greift Ziele in Reichweite an (Range, Ziele: Spieler/Monster/Alle)
- Auto Bow Aim: zielt mit gespanntem Bogen inkl. Fallkurve und Vorhalt; Loslassen machst du selbst

**Bases**
- X-Ray: nur Erze, Spawner, Kisten, antiker Schutt, Lava sichtbar (ohne Sodium; schaltet Fullbright mit ein)
- Chest-ESP: Kisten, Trapped Chests, Enderkisten, Fass, Shulker als Rahmen durch Waende
- Fullbright: immer hell

**Movement**
- Fly (Tempo), Speed (Faktor)
- Freecam: Kamera loest sich von der Figur, WASD + Leertaste/Shift, Maus dreht

## Bauen
1. JDK 21 installieren
2. Gradle Wrapper erzeugen: `gradle wrapper --gradle-version 8.12` (oder in IntelliJ als Gradle-Projekt oeffnen)
3. `./gradlew build`
4. `build/libs/adminclient-1.0.0.jar` in den `mods`-Ordner (Fabric Loader + Fabric API noetig)

## Hinweise
- Auf Servern mit OP brauchst du fuer Fly `allow-flight=true`, sonst kickt dich der Server.
- X-Ray funktioniert nicht zusammen mit Sodium/Iris.
