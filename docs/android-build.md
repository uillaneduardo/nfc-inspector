# Build do APK Android no Windows

Este documento registra o procedimento validado para preparar o ambiente de desenvolvimento e gerar uma build Debug do **NFC Inspector** no Windows.

## Ambiente validado

O build foi validado com:

- Windows 10 x64
- JDK 21.0.12 (Microsoft Build of OpenJDK)
- Gradle 8.7
- Gradle Wrapper 8.7
- Android SDK Platform 34
- Android SDK Build-Tools 34.0.0
- Android Gradle Plugin 8.3.2
- Kotlin 2.0.0
- Java/Kotlin bytecode target 17

O comando validado foi:

```powershell
.\gradlew.bat clean assembleDebug
```

Resultado esperado:

```text
BUILD SUCCESSFUL
```

O APK é gerado em:

```text
android\app\build\outputs\apk\debug\app-debug.apk
```

## 1. Instalar o JDK 21

O Gradle 8.7 deve ser executado com uma JVM compatível. Neste projeto, o ambiente foi padronizado em **JDK 21**.

No Windows, uma opção validada é o Microsoft Build of OpenJDK 21.

Após a instalação, confirme:

```powershell
java -version
where.exe java
```

A saída deve indicar Java 21.

Exemplo do ambiente validado:

```text
openjdk version "21.0.12"
C:\Program Files\Microsoft\jdk-21.0.12.8-hotspot\bin\java.exe
```

Se necessário, configure `JAVA_HOME`:

```powershell
$env:JAVA_HOME="C:\Program Files\Microsoft\jdk-21.0.12.8-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

Em seguida:

```powershell
java -version
```

## 2. Gradle 8.7

O projeto possui Gradle Wrapper e, depois de configurado corretamente, **não exige uma instalação global de Gradle para builds normais**.

Durante a recuperação inicial do projeto foi necessário instalar Gradle 8.7 localmente para regenerar o wrapper. Caso seja necessário repetir essa recuperação, uma instalação persistente pode ser mantida, por exemplo, em:

```text
C:\Gradle\gradle-8.7
```

Valide com:

```powershell
gradle --version
```

O resultado esperado deve combinar Gradle 8.7 com JVM 21.

## 3. Android SDK

No Android Studio, verifique o SDK em:

```text
File > Settings > Languages & Frameworks > Android SDK
```

O ambiente validado utilizou:

```text
Android SDK Platform 34
Android SDK Build-Tools 34.0.0
```

O projeto utiliza `compileSdk = 34` e `targetSdk = 34`.

## 4. Configurar local.properties

O Gradle precisa conhecer o caminho do Android SDK.

Crie o arquivo:

```text
android/local.properties
```

Exemplo para Windows:

```properties
sdk.dir=C:\\Users\\USUARIO\\AppData\\Local\\Android\\Sdk
```

No ambiente em que o build foi validado, o caminho era:

```text
C:\Users\USE\AppData\Local\Android\Sdk
```

**Não versione `local.properties` no Git.** O caminho é específico de cada computador e o arquivo deve permanecer ignorado pelo `.gitignore`.

## 5. Validar o Gradle Wrapper

Entre no diretório Android:

```powershell
cd android
```

Execute:

```powershell
.\gradlew.bat --version
```

O resultado esperado deve indicar:

```text
Gradle 8.7
JVM: 21.x
```

O wrapper é composto por:

```text
android/gradlew
android/gradlew.bat
android/gradle/wrapper/gradle-wrapper.properties
android/gradle/wrapper/gradle-wrapper.jar
```

Esses arquivos fazem parte do projeto e devem ser versionados.

## 6. Gerar o APK Debug

No diretório `android/`:

```powershell
.\gradlew.bat clean assembleDebug
```

No primeiro build o Gradle pode baixar dependências e instalar componentes faltantes do Android SDK, portanto a execução pode demorar alguns minutos.

Ao final:

```text
BUILD SUCCESSFUL
```

APK:

```text
android\app\build\outputs\apk\debug\app-debug.apk
```

## 7. Warnings atuais

O build validado concluiu com sucesso, porém apresentou warnings de APIs Compose depreciadas, incluindo:

- `Icons.Outlined.CompareArrows`
- `Icons.Filled.CompareArrows`
- `Icons.Outlined.FormatListBulleted`
- `Icons.Filled.ArrowBack`
- `Divider()` renomeado para `HorizontalDivider()`

Esses warnings **não impedem a geração do APK**, mas podem ser corrigidos futuramente para manter o projeto alinhado às APIs atuais do Jetpack Compose.

## 8. Fluxo recomendado para novos computadores

```text
Instalar Android Studio
        ↓
Instalar/configurar JDK 21
        ↓
Clonar o repositório
        ↓
Abrir a pasta android/ no Android Studio
        ↓
Instalar Android SDK 34 / Build Tools 34
        ↓
Criar local.properties
        ↓
.\gradlew.bat --version
        ↓
.\gradlew.bat clean assembleDebug
        ↓
app-debug.apk
```
