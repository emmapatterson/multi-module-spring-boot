# Multi-module Spring Boot project in Kotlin DSL

A common tool used for starting a new Spring Project is the [Spring Initializr](https://start.spring.io/ "https://start.spring.io/"). Using this, selecting Kotlin as your language, will create a simple Spring Boot project with a single `build.gradle.kts` file. 

However, as projects become bigger, we may want to split up our codebase into multiple build modules for better maintainability and understandability. But to understand why we would want a multi-module project, let’s first look at what a module actually is.

### What is a Module?
A module:
*   has a separate codebase to other modules’ code,
*   is transformed into a separate JAR file (artefact) during a build, and
*   can implement different dependencies to other modules.

[What is the Benefit to Modular Programming?](https://www.techopedia.com/definition/25972/modular-programming "https://www.techopedia.com/definition/25972/modular-programming")

_“The benefits of using modular programming include:_
*   _less code has to be written._
*   _a single procedure can be developed for reuse, eliminating the need to retype the code many times._
*   _the code is stored across multiple files._
*   _errors can easily be identified, as they are localised to a subroutine or function._
*   _the same code can be used in many applications._
*   _the scoping of variables can easily be controlled.”_

If we split up the codebase into multiple smaller modules that each has clearly defined dependencies to other modules, we take a big step towards an easily maintainable codebase.

### Example setup

Let’s now look at an example of how this can be implemented. For this we will imagine an example Spring Boot project with a `backend` module and a `test-utils` module. We want the `backend` to use the `test-utils` module. The folder structure would look as follows:
``` kotlin
├── backend 
| ├── src 
| └── build.gradle.kts 
├── test-utils 
| ├── src 
| └── build.gradle.kts 
├── build.gradle.kts 
└── settings.gradle.kts
```
Each module is in a separate folder with Kotlin sources, a `build.gradle.kts` file
The top-level `build.gradle.kts` file configures build behaviour that is shared between all sub-modules so that we don’t have to duplicate things in the sub-modules.

The `backend` module contains the actual Spring Boot application.
The `test-utils` module provides certain util classes that can be accessed by our `backend` module.

Let’s now take a look at how these files may look.

### Parent Build Gradle
``` kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    id("org.springframework.boot") version "2.2.0.RELEASE" apply false
    id("io.spring.dependency-management") version "1.0.8.RELEASE" apply false
    kotlin("jvm") version "1.3.50" apply false
    kotlin("plugin.spring") version "1.3.50" apply false
}

allprojects {
    group = "com.example"
    version = "1.0.0"

    tasks.withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }
}

subprojects {
    repositories {
        mavenCentral()
    }

    apply {
        plugin("io.spring.dependency-management")
    }
}
```

### Build gradle for a `test-utils` module:
``` kotlin
plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
}
```
Notice we don’t need to specify the version of the dependencies in this gradle file as they are stated in the parent gradle file.

### An example Spring Boot build gradle for module `backend`:
``` kotlin
plugins {
    id("org.springframework.boot")

    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":test-utils"))

    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```
Note the line `implementation(project(":test-utils"))`. This is very powerful as it allows for you to pull in this module as a dependency.

Just be aware of circular dependencies, i.e. modules that depend on each other. You **cannot** also have `implementation(project(":backend"))` in the `test-utils` module as this would create a circular dependency.

### Setting.gradle.kts
Lastly then you must tell the project that the modules exist and should be included, this is done in the `setting.gradle.kts`:
``` kotlin
rootProject.name = "example"
include("backend", "test-utils")
```

This example shows a basic set up of a multi-module project using the [Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html "https://docs.gradle.org/current/userguide/kotlin_dsl.html") for the build files.

To run gradle task on a specific module you can do so using the following idea:

`./gradlew :backend:clean :backend:build`

This can be useful for testing or running only one module at a time.
