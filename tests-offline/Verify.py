"""Run SharingApp model regression tests without Gradle, a device, or SDK downloads."""
from pathlib import Path
import argparse
import hashlib
import json
import os
import re
import shutil
import subprocess
import urllib.request
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "build" / "offline-ci"
DEPENDENCIES = (
    ("junit/junit/4.13.2/junit-4.13.2.jar", "8e495b634469d64fb8acfa3495a065cbacc8a0fff55ce1e31007be4c16dc57d3"),
    ("org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar", "66fdef91e9739348df7a096aa384a5685f4e875584cce89386a7a47251c4d8e9"),
    ("com/google/code/gson/gson/2.10.1/gson-2.10.1.jar", "4241c14a7727c34feea6507ec801318a3d4a90f070e4525681079fb94ee4c593"),
)


def dependencies(prepare):
    directory = OUTPUT / "deps"
    directory.mkdir(parents=True, exist_ok=True)
    paths = []
    for coordinate, checksum in DEPENDENCIES:
        path = directory / Path(coordinate).name
        if not path.exists():
            if not prepare:
                raise RuntimeError("Missing pinned dependency; run --prepare-dependencies --prepare-only first: " + path.name)
            url = "https://repo.maven.apache.org/maven2/" + coordinate
            with urllib.request.urlopen(url, timeout=30) as response:
                data = response.read(10 * 1024 * 1024 + 1)
            if len(data) > 10 * 1024 * 1024 or hashlib.sha256(data).hexdigest() != checksum:
                raise RuntimeError("Downloaded dependency checksum failed: " + path.name)
            path.write_bytes(data)
        if hashlib.sha256(path.read_bytes()).hexdigest() != checksum:
            raise RuntimeError("Cached dependency checksum failed: " + path.name)
        paths.append(path)
    return paths


def installed_android_jar(explicit):
    if explicit:
        candidates = [Path(explicit)]
    else:
        sdk = os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
        if not sdk and os.environ.get("LOCALAPPDATA"):
            sdk = str(Path(os.environ["LOCALAPPDATA"]) / "Android" / "Sdk")
        if not sdk:
            raise RuntimeError("Set ANDROID_HOME to an existing SDK or pass --android-jar. This script does not download SDKs or accept licenses.")
        candidates = list((Path(sdk) / "platforms").glob("android-*/android.jar"))
        candidates = [p for p in candidates if re.fullmatch(r"android-\d+", p.parent.name)]
        candidates.sort(key=lambda p: int(p.parent.name.split("-")[1]), reverse=True)
    for path in candidates:
        if path.is_file():
            return path
    raise RuntimeError("No preinstalled Android platform jar found. No SDK installation or license acceptance was attempted.")


def run(command):
    result = subprocess.run([str(x) for x in command], cwd=ROOT, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=90)
    print(result.stdout, end="")
    if result.returncode:
        raise RuntimeError("Verification command failed with exit code " + str(result.returncode))
    return result.stdout


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--android-jar", help="Existing SDK android.jar; otherwise select highest installed platform")
    parser.add_argument("--prepare-dependencies", action="store_true", help="Allow pinned Maven Central dependency downloads")
    parser.add_argument("--prepare-only", action="store_true", help="Verify/cache dependencies without compiling or testing")
    args = parser.parse_args()
    jars = dependencies(args.prepare_dependencies)
    if args.prepare_only:
        print("PASS: 3 Maven Central dependencies verified against pinned SHA256")
        return
    android_jar = installed_android_jar(args.android_jar)
    java, javac = shutil.which("java"), shutil.which("javac")
    if not java or not javac:
        raise RuntimeError("A JDK with java and javac on PATH is required.")
    classes = OUTPUT / "classes"
    classes.mkdir(parents=True, exist_ok=True)
    model_dir = ROOT / "app/src/main/java/com/example/sharingapp"
    models = [model_dir / (name + ".java") for name in ("Contact", "ContactList", "Dimensions", "Item", "ItemList")]
    test_root = ROOT / "app/src/test/java"
    tests = sorted(test_root.rglob("*.java"))
    if not tests:
        raise RuntimeError("No JUnit test sources found.")
    xml_files = sorted((ROOT / "app/src/main").rglob("*.xml"))
    if not xml_files:
        raise RuntimeError("No Android XML resources found.")
    for path in xml_files:
        ET.parse(path)
    classpath = os.pathsep.join(str(p) for p in [android_jar, *jars])
    # Compile the checked-in models/tests, preserving Android Java 8 compatibility.
    run([javac, "--release", "8", "-encoding", "UTF-8", "-cp", classpath, "-d", classes, *models, *tests, ROOT / "tests-offline/ContactChecks.java"])
    runtime_cp = str(classes) + os.pathsep + classpath
    junit_classes = [p.relative_to(test_root).with_suffix("").as_posix().replace("/", ".") for p in tests]
    junit = run([java, "-cp", runtime_cp, "org.junit.runner.JUnitCore", *junit_classes])
    junit_count = re.search(r"OK \((\d+) tests?\)", junit)
    if not junit_count:
        raise RuntimeError("JUnit successful test count missing.")
    model_output = run([java, "-cp", runtime_cp, "ContactChecks"])
    model_count = re.search(r"PASS: (\d+) offline model checks", model_output)
    if not model_count:
        raise RuntimeError("Standalone model check count missing.")
    result = {
        "junit_tests": int(junit_count.group(1)),
        "model_checks": int(model_count.group(1)),
        "xml_parsed": len(xml_files),
        "android_compile_platform": android_jar.parent.name,
        "java_release": 8,
        "junit_classes": junit_classes,
        "network_during_verification": False,
        "scope": "Model JVM tests and XML parsing; no APK build, Android runtime, instrumentation or device UI verification",
    }
    (OUTPUT / "results.json").write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, indent=2))


if __name__ == "__main__":
    main()
