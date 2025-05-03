# chms-android

## CHMS(社区健康管理系统)Android仓库

### 配置信息
* Gradle Version: 8.9
* Gradle JDK: 21.0.3

### 注意事项
* Windows系统需要在运行项目时保证本地下载了相应的Java版本
* Windows系统在build项目时，需要以管理员权限打开控制台，移动到项目的本地目录下，运行以下命令：

```
C:\Windows\System32>cd /d D:\repository\CHMS\chms-android
D:\repository\CHMS\chms-android>gradlew assembleDebug --stacktrace
```

* 在Android Studio中直接rebuild项目的话会出现以下报错

  ```
  Execution failed for task ':app:kaptDebugKotlin'.
  > A failure occurred while executing org.jetbrains.kotlin.gradle.internal.KaptWithoutKotlincTask$KaptExecutionWorkAction
  * Try:
  > Run with --stacktrace option to get the stack trace.
  > Run with --info or --debug option to get more log output.
  > Run with --scan to get full insights.
  > Get more help at https://help.gradle.org.
  Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0.
  You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
  For more on this, please refer to https://docs.gradle.org/8.9/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
  BUILD FAILED in 29s
  108 actionable tasks: 101 executed, 7 up-to-date
  ```

   * 具体错误原因：

  ```
  jdk.compiler/com.sun.tools.javac.processing.JavacProcessingEnvironment.callProcessor(JavacProcessingEnvironment.java:1023)
  ... 39 more
  Caused by: java.lang.Exception: No native library found for os.name=Windows, os.arch=x86_64, paths=[/org/sqlite/native/Windows/x86_64;C:\Program Files\Java\jdk-17.0.12\bin;C:\WINDOWS\Sun\Java\bin;C:\WINDOWS\system32;C:\WINDOWS;.]
  at org.sqlite.SQLiteJDBCLoader.loadSQLiteNativeLibrary(SQLiteJDBCLoader.java:363)
  at org.sqlite.SQLiteJDBCLoader.initialize(SQLiteJDBCLoader.java:72)
  at androidx.room.verifier.DatabaseVerifier.(DatabaseVerifier.kt:79)
  ... 50 more
  Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0.
  You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.
  For more on this, please refer to https://docs.gradle.org/8.9/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
  BUILD FAILED in 13s
  60 actionable tasks: 1 executed, 59 up-to-date
  ```

> **搜索得到的问题原因：**
> 根据你提供的错误信息，构建过程中遭遇了 `AccessDeniedException`，这意味着程序试图访问一个未授予权限或被锁定的文件，并且该文件涉及到 `sqlitejdbc.dll.lck`，是SQLite JDBC在Windows系统上的一个锁文件。这种问题通常与文件权限、文件锁定或权限不足有关。以下是一些解决方案：
>
> 1. **以管理员身份运行**：
     >     - 以管理员身份打开命令提示符，然后在管理员权限下重新运行构建命令。这可以解决由于权限不足导致的访问被拒问题。
>
> 2. **检查锁文件**：
     >     - 此问题可能是由于文件被其他进程锁定。可以尝试重新启动计算机以解除所有文件锁，然后再次运行构建。
>
> 3. **手动删除 `.lck` 文件**：
     >     - 访问 `C:\WINDOWS\` 目录并手动找到并删除 `sqlite-3.41.2.2-*.dll.lck` 文件。请确保没有其他程序正在使用这个文件。
>
> 4. **检查防病毒/安全软件**：
     >     - 某些防病毒或安全软件可能会暂时锁定或防止程序访问某些文件，尝试禁用这些软件看看问题是否能解决。
>
> 5. **设置不同的临时目录**：
     >     - 将SQLite JDBC的临时目录更改为开发者拥有完全控制权限的目录。可以通过环境变量来指定，例如 `java.io.tmpdir`。
>
> 6. **更新或替换SQLite JDBC版本**：
     >     - 确保你使用的SQLite JDBC库是最新版本。可以尝试替换成其他版本，以看问题是否可以消失。参见 [SQLite JDBC GitHub](https://github.com/xerial/sqlite-jdbc) 寻找最新版本。
>
> 7. **检查 Gradle 配置**：
     >     - 确保没有并行构建、以及设置合适的 `KAPT` 配置，以避免并发访问。
>
> 8. **清理构建目录**：
     >     - 运行 `./gradlew clean` 以清理临时构建文件，然后再执行构建命令。
>
> 9. **关闭所有不必要的软件**：
     >     - 关闭所有不必要的软件或进程，以确保没有其他后台进程正在干扰构建过程。
>
> 通过这些步骤，通常可以解决 `AccessDeniedException` 相关的问题。如果问题依旧存在，建议检查相关依赖配置和系统环境变量，找出可能导致问题的其他原因。