# Build Guide

This guide explains how to run and verify the Groovlets project on any machine (Linux, macOS or Windows, x86_64 or ARM64).

## TL;DR

```bash
# 1. Install JDK 8 and Groovy 2.4.21 (see sections 3 and 4)
java -version          # 1.8.x
groovy --version       # Groovy Version: 2.4.21

# 2. Start the server from the project root
groovy server.groovy

# 3. Try it
curl "http://localhost:8888/codes?code=200"      # -> OK
```

## 1. There is no build step

This project has **no build tool** (no Gradle, no Maven, no wrapper) and produces **no jar**:

- The web services are **Groovlets**: plain Groovy scripts in `src/main/groovy/` (files without extension, e.g. `info`, `services`, `calc`). Each one is served at `/<file-name>` and is compiled **at runtime**, on its first request, by Groovy's `GroovyServlet`.
- Shared classes live in `src/main/library/` (package `src.main.library`). Groovlets import them as `src.main.library.*`, and Groovy compiles them from source when they are first used. This is why the server must be started from the project root.
- `server.groovy` is an executable Groovy script that starts an embedded **Jetty 6** server on port **8888**. Its dependencies (Jetty, logback, slf4j) are downloaded automatically through `@Grab` on first run.
- The `Makefile` is only used to recompile the stylesheet (`main.less` to `src/main/style/main.css`) with `lessc`. It is not needed to run the project, because the compiled `main.css` is committed.

"Building" this project therefore means: install JDK 8 and Groovy, and (optionally) check that every script compiles (section 6).

## 2. Requirements

| Component  | Version                 | Notes                                                                 |
|------------|-------------------------|-----------------------------------------------------------------------|
| Java (JDK) | **8**                   | Verified with JDK 8. A full JDK is required                           |
| Groovy     | **2.4.21** (2.4.x line) | Must be installed; it provides `GroovyServlet` and the servlet API    |
| Jetty      | 6.1.26                  | Downloaded automatically by `@Grab` in `server.groovy`                |
| Network    | Maven Central / JCenter | Needed on first run, so `@Grab` can download dependencies             |

**About the Groovy version:** the project is from 2017 and was verified with **Groovy 2.4.21 on JDK 8**. Newer Groovy lines have not been verified. Groovy 5 in particular will not work, because its servlet support uses the `jakarta.servlet` API, while Jetty 6 uses `javax.servlet`.

## 3. Installing JDK 8

Pick the option for your platform. After installing, open a **new** terminal and check with `java -version` (it should report `1.8.0_xxx`).

### Any platform: SDKMAN! (Linux, macOS, WSL)

```bash
curl -s "https://get.sdkman.io" | bash
sdk list java | grep -E "8\.0\.[0-9]+"     # pick an available JDK 8 identifier
sdk install java <identifier>               # e.g. 8.0.xxx-tem or 8.0.xxx-zulu
sdk use java <identifier>
```

### Linux

- **Debian / Ubuntu:** `sudo apt install openjdk-8-jdk` (on newer releases where it is not packaged, use SDKMAN! or a Temurin / Zulu tarball).
- **Fedora / RHEL / CentOS:** `sudo dnf install java-1.8.0-openjdk-devel`
- **Arch:** `sudo pacman -S jdk8-openjdk`
- **Manual tarball:** download a JDK 8 `.tar.gz` for your architecture (x64 or aarch64) from <https://adoptium.net> or <https://www.azul.com/downloads/>, extract it anywhere and point `JAVA_HOME` to it.

### macOS

- **Intel (x86_64):** `brew install --cask temurin@8`
- **Apple Silicon (arm64):** Temurin 8 does not ship a native arm64 macOS build. Use Azul Zulu 8: `brew install --cask zulu@8`, or download it from <https://www.azul.com/downloads/>.

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
```

### Windows

- **winget:** `winget install EclipseAdoptium.Temurin.8.JDK`
- **Chocolatey:** `choco install temurin8`
- **Manual:** download the JDK 8 `.msi` from <https://adoptium.net> and enable the "Set JAVA_HOME" option in the installer.

## 4. Installing Groovy 2.4.21

### SDKMAN! (Linux, macOS, WSL)

```bash
sdk install groovy 2.4.21
sdk use groovy 2.4.21
```

### Manual install (any OS)

1. Download <https://archive.apache.org/dist/groovy/2.4.21/distribution/apache-groovy-binary-2.4.21.zip>.
2. Extract it anywhere.
3. Add its `bin/` folder to your `PATH` (or call `bin/groovy` / `bin\groovy.bat` directly).

```bash
groovy --version
# Groovy Version: 2.4.21 JVM: 1.8.0_xxx ...
```

Groovy is pure Java, so the same zip works on every OS and CPU architecture. Package managers (Homebrew, apt, Chocolatey, Scoop) usually install a much newer Groovy; use SDKMAN! or the manual install to get 2.4.21.

## 5. Running the server

From the **project root** (required: the server serves `src/main/groovy` and `src/main/style` relative to the current directory, and groovlets load `src.main.library.*` classes from it):

```bash
groovy server.groovy
```

```bat
:: Windows
groovy server.groovy
```

The first start downloads Jetty and the logging libraries through `@Grab` (into `~/.groovy/grapes`), which can take a minute. Then it prints `Starting Jetty, press Ctrl+C to stop.` and listens on <http://localhost:8888>. Stop it with `Ctrl+C`.

Notes:

- Requesting `/` (exactly) redirects to `http://api.lappsgrid.org/info`; that is intended behavior of `server.groovy`.
- Some groovlets write a log file; by default it goes to `/tmp/groovlets.log` (set `LOG_PATH` to change it).
- The `database`, `test` scripts in the project root are optional groovlets that are not served unless copied into `src/main/groovy/` (see their header comments).

### Smoke test

With the server running:

```bash
curl "http://localhost:8888/codes?code=200"                 # OK
curl "http://localhost:8888/calc?a=2&op=add&b=3"            # 5
curl "http://localhost:8888/macro?name=greet&who=bob"       # Hello bob
curl "http://localhost:8888/page?page=about"
curl "http://localhost:8888/plugin?id=trim&input=%20hi%20"  # hi
curl "http://localhost:8888/rules?fn=upper&input=hi"        # HI
curl "http://localhost:8888/transform?input=hello"          # HELLO
curl "http://localhost:8888/view?name=about.tpl"
curl "http://localhost:8888/theme?theme=dark"
curl "http://localhost:8888/report?days=7"
curl "http://localhost:8888/services"                       # JSON list
curl "http://localhost:8888/info"                           # HTML page
```

Groovlets that are called without the parameters they need answer with a 4xx status and a short usage message (e.g. `Expected name or id (greet|stamp).`). `codes` without `?code=` answers `500 Encountered a Server Error` on purpose. These are not errors. A real compilation error shows up as HTTP 500 with a `startup failed` message.

## 6. Checking that every script compiles (optional)

Because groovlets are compiled only when requested, a syntax error in a groovlet that is never called goes unnoticed. To check all scripts at once, compile them with `groovyc` into a throw-away folder. Groovlet files have no extension, so they are copied with a `.groovy` suffix first.

Linux / macOS / WSL / Git Bash, from the project root:

```bash
CHECK=$(mktemp -d)
mkdir -p "$CHECK/src/src/main/library" "$CHECK/out"
cp src/main/library/*.groovy "$CHECK/src/src/main/library/"
for f in src/main/groovy/*; do
  [ "$(basename "$f")" = logback.xml ] && continue
  cp "$f" "$CHECK/src/gl_$(basename "$f").groovy"
done
cp src/main/macros/*.groovy src/main/plugins/*.groovy "$CHECK/src/"
cp server.groovy "$CHECK/src/root_server.groovy"
(cd "$CHECK" && groovyc -j -d out $(find src -name '*.groovy')) && echo "ALL SCRIPTS COMPILE"
rm -rf "$CHECK"
```

`ALL SCRIPTS COMPILE` and no error output mean every groovlet, library class, macro, plugin and `server.groovy` compiles.

## 7. Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `groovy: command not found` | Groovy is not installed or not on `PATH` | Install Groovy 2.4.21 (section 4). |
| `unable to resolve class src.main.library.XXX` | Server (or a script) started from another directory | Start `groovy server.groovy` from the project root. |
| `General error during conversion: Error grabbing Grapes -- [unresolved dependency: ...]` | No access to Maven Central / JCenter, or a proxy | Check internet / firewall. Behind a proxy, set `JAVA_OPTS="-Dhttps.proxyHost=... -Dhttps.proxyPort=... -Dhttp.proxyHost=... -Dhttp.proxyPort=..."` before running `groovy`. |
| `java.lang.NoClassDefFoundError: javax/servlet/...` or `groovy.servlet.GroovyServlet` not found | Groovy 5 (Jakarta servlet API) or a Groovy install without the servlet module | Use Groovy 2.4.21. |
| `Unsupported major.minor version` / other startup errors with Groovy 2.4 | Groovy 2.4 running on a JDK newer than 8 | Set `JAVA_HOME` to JDK 8. |
| HTTP 500 with `startup failed` | A groovlet does not compile | The response and the server console show the file and line; see section 6. |
| `FileNotFoundException ... src/main/plugins/<name>.groovy` from `/plugin?name=...` | `name=` loads the plugin file with that exact (case-sensitive) name | Use the registered ids (`/plugin?id=trim`, `/plugin?id=reverse`) or the exact file name (`/plugin?name=Trim`). |
| `Address already in use` | Port 8888 is taken | Stop the other process (the port is hard-coded in `server.groovy`). |
| `lessc: command not found` when running `make less` | Less compiler not installed | Only needed to change the stylesheet: `npm install -g less`. |

## 8. Changes made to this repository

None. The project runs as-is with Groovy 2.4.21 and JDK 8; the only addition is this guide.
