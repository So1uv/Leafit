"""Offline checks for resources, Room SQL and static palette contrast.
Run: python3 tools/verify_sources.py
This does not compile Kotlin or render Compose.
"""
from pathlib import Path
import re
import sqlite3
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
MAIN = ROOT / "app/src/main"
JAVA = MAIN / "java/com/example/fitnesstracker"

# All locales must expose the same keys and format placeholders.
locales = {}
for path in MAIN.glob("res/values*/strings.xml"):
    items = ET.parse(path).getroot().findall("string")
    names = [item.attrib["name"] for item in items]
    assert len(set(names)) == len(names), f"Duplicate strings in {path}"
    locales[path.parent.name] = {item.attrib["name"]: "".join(item.itertext()) for item in items}
base = locales["values"]
for locale, values in locales.items():
    assert values.keys() == base.keys(), f"Locale keys differ: {locale}"
    for key, value in values.items():
        assert sorted(re.findall(r"%\d+\$[dsf]", value)) == sorted(re.findall(r"%\d+\$[dsf]", base[key])), (locale, key)
for path in MAIN.rglob("*.xml"):
    ET.parse(path)
for path in MAIN.rglob("*.kt"):
    source = path.read_text()
    missing = set(re.findall(r"R\.string\.(\w+)", source)) - base.keys()
    assert not missing, (path, missing)
    imports = re.findall(r"^import .+$", source, re.M)
    assert len(imports) == len(set(imports)), f"Duplicate imports: {path}"
    assert "@Composable\n@Composable" not in source, path
print(f"PASS: XML, {len(base)} string keys in {len(locales)} locales, placeholders and resource references")

# Execute the actual migration and the actual SQL annotations against an existing v5 database.
database_source = (JAVA / "data/database/AppDatabase.kt").read_text()
migration = re.search(r'db.execSQL\("(ALTER TABLE day_notes[^"\n]+)"\)', database_source).group(1)
dao = (JAVA / "data/dao/ExtrasDao.kt").read_text()
queries = dict((name, sql) for sql, name in re.findall(
    r'@Query\("([^"\n]+)"\)\s*(?:suspend )?fun (\w+)', dao))
connection = sqlite3.connect(":memory:")
connection.executescript("""
    CREATE TABLE day_notes (dayStart INTEGER NOT NULL PRIMARY KEY, text TEXT NOT NULL);
    CREATE TABLE weight_records (id INTEGER PRIMARY KEY, date INTEGER, weightKg REAL);
    INSERT INTO day_notes VALUES (100, 'Original note'), (200, 'Another note');
    INSERT INTO weight_records VALUES (1, 100, 72.5);
""")
connection.execute(migration)
assert connection.execute("SELECT * FROM day_notes ORDER BY dayStart").fetchall() == [
    (100, "Original note", 0), (200, "Another note", 0)]
connection.execute(queries["toggleNotePin"], {"dayStart": 100})
connection.execute(queries["updateNoteText"], {"dayStart": 100, "text": "Edited note"})
assert connection.execute("SELECT text, isPinned FROM day_notes WHERE dayStart=100").fetchone() == ("Edited note", 1)
connection.execute(queries["toggleNotePin"], {"dayStart": 100})
assert connection.execute("SELECT isPinned FROM day_notes WHERE dayStart=100").fetchone() == (0,)
connection.execute(queries["deleteNote"], {"dayStart": 100})
assert connection.execute("SELECT * FROM day_notes").fetchall() == [(200, "Another note", 0)]
assert connection.execute("SELECT weightKg FROM weight_records").fetchone() == (72.5,)
print("PASS: v5→v6 migration preserves notes and weight; pin, edit, unpin and targeted deletion")

# Check all opaque text/container pairs introduced by the fixed light/dark themes.
def luminance(argb):
    value = int(argb, 16)
    rgb = [(value >> shift & 255) / 255 for shift in (16, 8, 0)]
    linear = [channel / 12.92 if channel <= .04045 else ((channel + .055) / 1.055) ** 2.4 for channel in rgb]
    return sum(c * k for c, k in zip(linear, (.2126, .7152, .0722)))

def contrast(a, b):
    x, y = sorted((luminance(a), luminance(b)))
    return (y + .05) / (x + .05)

theme = (JAVA / "ui/theme/Theme.kt").read_text()
ratios = []
for name in ("MintLight", "MintDark"):
    section = theme.split(f"private val {name} = ")[1].split("\n)\n")[0]
    colors = dict(re.findall(r"(\w+)\s*= Color\(0x([A-Fa-f0-9]+)\)", section))
    for background, foreground in [("primary", "onPrimary"), ("primaryContainer", "onPrimaryContainer"),
                                   ("secondaryContainer", "onSecondaryContainer"), ("tertiaryContainer", "onTertiaryContainer"),
                                   ("surfaceContainerLow", "onSurfaceVariant"), ("surfaceContainerHigh", "onSurfaceVariant"),
                                   ("surfaceContainerHighest", "onSurfaceVariant")]:
        ratio = contrast(colors[background], colors[foreground])
        assert ratio >= 4.5, (name, background, foreground, ratio)
        ratios.append(ratio)
sleep = (JAVA / "ui/screens/sleep/SleepScreen.kt").read_text()
for bg, fg in re.findall(r"0x([A-Fa-f0-9]+) to 0x([A-Fa-f0-9]+)", sleep):
    ratio = contrast(bg, fg)
    assert ratio >= 4.5, (bg, fg, ratio)
    ratios.append(ratio)
print(f"PASS: {len(ratios)} fixed text/container pairs, minimum contrast {min(ratios):.2f}:1")
print("NOT CHECKED: Kotlin compilation, generated Room schema validation, device rendering or animation behavior")
