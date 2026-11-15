// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}

// O repositório fica em uma pasta do OneDrive com acentos ("Instituição", "Programação").
// No Windows, caminhos não-ASCII quebram a execução dos testes unitários (ClassNotFoundException).
// Nesse caso, os arquivos de build são gerados fora do projeto, em ~/.todolist-build.
val hasNonAsciiPath = rootDir.absolutePath.any { it.code > 127 }
if (hasNonAsciiPath) {
    val externalBuildRoot = File(System.getProperty("user.home"), ".todolist-build")
    allprojects {
        layout.buildDirectory.set(File(externalBuildRoot, path.replace(":", "_").ifEmpty { "root" }))
    }
}
