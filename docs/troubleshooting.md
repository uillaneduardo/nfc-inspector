# Troubleshooting de Build Android

Este documento registra problemas encontrados durante a primeira build real do **NFC Inspector** no Android Studio/Windows e as correções aplicadas.

## 1. `JAVA_HOME is not set`

### Sintoma

```text
ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
```

### Causa

O terminal não conseguia localizar uma instalação Java válida.

### Correção

Instalar/configurar JDK 21 e confirmar:

```powershell
java -version
where.exe java
```

Quando necessário na sessão atual:

```powershell
$env:JAVA_HOME="C:\Program Files\Microsoft\jdk-21.0.12.8-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

## 2. Gradle 8.7 executando com Java 25

### Sintoma

O Gradle 8.7 falhava ao iniciar quando a JVM ativa era Java 25.0.2.

### Causa

Incompatibilidade entre a JVM usada para executar o Gradle e a versão do Gradle definida pelo projeto.

### Correção

Padronizar o runtime do Gradle em **JDK 21**.

Importante: o runtime do Gradle é Java 21, enquanto o código continua configurado para bytecode Java/Kotlin 17.

## 3. Falha do WinGet ao instalar OpenJDK 21

### Sintoma

```text
O hash do instalador não corresponde
```

### Causa

O WinGet encontrou divergência entre o hash esperado no manifesto e o instalador recebido.

### Correção utilizada

Atualizar/redefinir as fontes do WinGet ou utilizar o instalador oficial do JDK 21 por outro meio confiável.

Após a instalação, sempre validar:

```powershell
java -version
where.exe java
```

## 4. `ClassNotFoundException: org.gradle.wrapper.GradleWrapperMain`

### Sintoma

```text
Erro: Não foi possível localizar nem carregar a classe principal org.gradle.wrapper.GradleWrapperMain
Causada por: java.lang.ClassNotFoundException: org.gradle.wrapper.GradleWrapperMain
```

### Causa encontrada

O arquivo:

```text
android/gradle/wrapper/gradle-wrapper.jar
```

estava ausente inicialmente e depois foi adicionado ao repositório em formato corrompido por uma ferramenta que tratou o binário de forma inadequada.

### Correção

Instalar Gradle 8.7 real e regenerar o wrapper:

```powershell
gradle wrapper --gradle-version 8.7
```

Depois validar:

```powershell
.\gradlew.bat --version
```

O `gradle-wrapper.jar` válido deve ser versionado no Git junto com os demais arquivos do wrapper.

## 5. `SDK location not found`

### Sintoma

```text
SDK location not found. Define a valid SDK location with an ANDROID_HOME environment variable or by setting the sdk.dir path in your project's local properties file
```

### Causa

O projeto local não tinha o caminho do Android SDK configurado.

### Correção

Criar:

```text
android/local.properties
```

com:

```properties
sdk.dir=C:\\Users\\USUARIO\\AppData\\Local\\Android\\Sdk
```

O arquivo deve ficar somente na máquina local e não deve ser commitado.

## 6. Android SDK Platform 34 / Build Tools ausentes

### Sintoma

Durante a primeira build, o Gradle identificou componentes ausentes da API 34.

### Correção

Instalar:

```text
Android SDK Platform 34
Android SDK Build-Tools 34.0.0
```

No ambiente utilizado, o Gradle conseguiu instalar automaticamente os componentes após a licença ser aceita.

## 7. Aviso de versão XML do Android SDK

### Sintoma

```text
This version only understands SDK XML versions up to 3 but an SDK XML file of version 4 was encountered.
```

### Observação

Esse aviso apareceu devido à diferença de geração entre ferramentas do Android SDK/Android Studio, mas **não impediu o build**. O processo continuou, instalou a Platform 34 e Build Tools 34.0.0 e chegou à compilação Kotlin.

Se futuramente virar erro, revisar as versões do Android Studio, Command-line Tools e AGP antes de alterar o `compileSdk` do projeto.

## 8. Erros Kotlin em `NfcTagParser.kt`

### `Unresolved reference 'timeout'` em NfcB

O código tentava utilizar:

```kotlin
nfcB.timeout
```

A propriedade não estava disponível na API utilizada.

#### Correção

Remover o timeout do modelo `NfcBParams`, parser, relatório e interface.

### `Unresolved reference 'dsfid'` em NfcV

O acesso estava escrito como:

```kotlin
nfcV.dsfid
```

#### Correção

Utilizar a propriedade correta:

```kotlin
nfcV.dsfId
```

## 9. Erros Compose em `ReportScreen.kt`

### Sintomas

```text
Unresolved reference 'collectAsState'
Unresolved reference 'not' for operator '!'
Unresolved reference 'Check'
Overload resolution ambiguity em Icon(...)
```

### Causa

Imports ausentes faziam com que estados booleanos e o ícone `Check` não fossem resolvidos corretamente.

### Correção

Adicionar:

```kotlin
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.filled.Check
```

## 10. Build finalmente validado

Com JDK 21, Gradle 8.7, wrapper válido, SDK 34 e as correções Kotlin aplicadas, o comando:

```powershell
.\gradlew.bat clean assembleDebug
```

concluiu em aproximadamente 2 minutos com:

```text
BUILD SUCCESSFUL
```

Foram executadas 36 tasks.

## 11. Warnings que ainda podem ser tratados

A build atual ainda mostra apenas warnings de depreciação do Compose, como:

- usar `Icons.AutoMirrored` para alguns ícones direcionais;
- substituir `Divider()` por `HorizontalDivider()`.

Esses pontos não bloqueiam o APK e podem ser corrigidos separadamente sem urgência.

## 12. Princípio para troubleshooting futuro

Antes de alterar código ou versões do projeto, isolar a camada do problema:

```text
Java/JDK
  ↓
Gradle
  ↓
Gradle Wrapper
  ↓
Android SDK
  ↓
AGP/Kotlin
  ↓
Código do aplicativo
```

A primeira build mostrou a importância de validar cada camada nessa ordem. Isso evita tentar corrigir código Kotlin quando o problema real ainda está no Java, Gradle ou SDK.
