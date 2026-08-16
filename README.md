# NFC Inspector - Aplicativo Android Nativo

**NFC Inspector** é um aplicativo Android nativo completo desenvolvido em **Kotlin** com **Jetpack Compose** e **Material 3**, voltado para diagnóstico técnico aprofundado, identificação e inspeção de tags e cartões NFC pertencentes ao usuário ou sob autorização.

100% gratuito, offline, sem anúncios, sem rastreadores, sem telemetria e sem coleta de dados.

---

## 1. Arquitetura do Projeto

O projeto é estruturado seguindo rigorosamente a arquitetura recomendada pelo Google Android (**MVVM** + **Unidirectional Data Flow**):

- **`com.nfcinspector.app.data.model`**:
  - `NfcState.kt`: Modelagem dos estados reativos do hardware (`Checking`, `Unsupported`, `Disabled`, `ReadyWaiting`, `TagDetected`, `ScanError`).
  - `TechDataModels.kt`: Modelos para cada tecnologia de RF (`NfcAParams`, `NfcBParams`, `IsoDepParams`, `MifareClassicParams`, `MifareUltralightParams`, `NfcFParams`, `NfcVParams`, `NdefParams`, `NdefRecordItem`).
  - `TagRecord.kt`: Agregação dos dados da tag, formatações de UID (Hex com dois pontos, Hex contínuo, Decimal) e gerador de relatório técnico em texto puro.
- **`com.nfcinspector.app.data.local` & `repository`**:
  - `AppDatabase.kt`, `TagDao.kt`, `TagEntity.kt`: Persistência local utilizando **Room Database** (SQLite) para armazenamento 100% offline.
  - `HistoryRepository.kt`: Repositório para o fluxo de leituras salvas manualmente, exclusão individual e limpeza total.
- **`com.nfcinspector.app.nfc`**:
  - `NfcManager.kt`: Gerenciamento do ciclo de vida com `NfcAdapter.enableReaderMode` e `disableReaderMode`, verificação de estado em `onResume`, feedback háptico (`VibrationEffect`) e intents de configurações do sistema com fallback.
  - `NfcTagParser.kt`: Parser seguro com tratamento de desconexão e fechamento seguro de conexões para `NfcA`, `NfcB`, `IsoDep`, `Ndef`, `MifareClassic`, `MifareUltralight`, `NfcF` e `NfcV`.
  - `nfc.lab.HceLabPlaceholder.kt`: Base arquitetural isolada para futuros experimentos de Host Card Emulation (HCE) com protocolo proprietário de laboratório.
- **`com.nfcinspector.app.ui`**:
  - `ReaderScreen.kt`: Tela principal com estados (Verificando, Não suportado, Desativado, Pronto para leitura, Cartão detectado) e cartões técnicos detalhados com valores em monospace. Contém ação explícita de "Salvar leitura" com prevenção de duplicidade.
  - `HistoryScreen.kt`: Histórico local de leituras salvas offline, exclusão individual, limpeza total e atalhos para comparação.
  - `CompareScreen.kt`: Comparador diferencial lado a lado entre duas leituras com destaque de equivalências e divergências.
  - `ReportScreen.kt`: Relatório completo com ações de copiar, compartilhar e salvar no histórico local.
  - `AboutScreen.kt`: Guia educacional técnico sobre NFC, UID, NDEF, ISO-DEP, MIFARE e compromisso de privacidade.
  - `MainActivity.kt` & `Theme.kt`: Gerenciamento do ciclo de vida, navegação Material 3 (`NavigationBar`), suporte a tema dinâmico e tema escuro.

---

## 2. Fluxo de Leitura NFC (Reader Mode)

O aplicativo utiliza exclusivamente o **Android NFC Reader Mode** (`enableReaderMode` / `disableReaderMode`):

1. **Abertura do App**: O estado inicial é `NfcStatus.Checking` enquanto o adaptador de hardware é consultado.
2. **NFC Desativado**: Exibe aviso com botão direto para as configurações do sistema. Ao reativar e retornar ao app, o `onResume` reavalia e entra imediatamente em `ReadyWaiting`.
3. **Aproximação da Tag**: Callback `onTagDiscovered(Tag)` é disparado, aciona feedback tátil e extrai os parâmetros via `NfcTagParser`.
4. **Remoção da Tag**: Caso o cartão seja afastado antes do término da leitura, uma mensagem amigável é exibida e o app continua estável.
5. **Salvamento Manual**: As leituras **não** são salvas automaticamente no banco de dados, evitando poluição e duplicações. O usuário toca no botão "Salvar leitura" quando desejar guardar a medição no histórico.

---

## 3. Especificações e Ambiente de Build

- **Dispositivo Alvo Inicial**: Motorola Moto G50 5G (Android 12)
- **`minSdk`**: `26` (Android 8.0 Oreo ou superior - compatibilidade total com Android 12)
- **`targetSdk`**: `34` (Android 14)
- **`compileSdk`**: `34`
- **Gradle Runtime / Gradle JVM**: **JDK 21** (Java 21)
- **Gradle Wrapper**: `8.7`
- **Android Gradle Plugin (AGP)**: `8.3.2`
- **Linguagem**: Kotlin `2.0.0`
- **Compatibilidade de Bytecode (`sourceCompatibility` / `targetCompatibility`)**: Java 17 (`JavaVersion.VERSION_17`)
- **Kotlin `jvmTarget`**: `17`
- **UI Toolkit**: Jetpack Compose com Material 3 (BOM 2024.06.00)
- **Permissões Declaradas no `AndroidManifest.xml`**:
  - `android.permission.NFC`
  - `android.permission.VIBRATE`
  - `android.hardware.nfc` (configurado como `android:required="false"` para permitir instalação e aviso informativo em qualquer aparelho).
  - **Zero permissões de rede (`android.permission.INTERNET` NÃO solicitada)**.

---

## 4. Configuração do Ambiente JDK 21 e Android Studio

> ⚠️ **Importante sobre a versão do Java:** O Gradle 8.7 deve ser executado obrigatoriamente com o **JDK 21**. Se o seu sistema tiver outra versão padrão no PATH (como Java 25), o Gradle falhará com erro de versão incompatível. O código-fonte continua compilado para **Java 17**, mantendo total compatibilidade com o Android.

### 4.1. Configuração no Android Studio (IDE)

1. Abra o projeto pela pasta `android/`.
2. Acesse: **File > Settings** (ou `Ctrl + Alt + S` no Windows / `Cmd + ,` no macOS).
3. Navegue até: **Build, Execution, Deployment > Build Tools > Gradle**.
4. No campo **Gradle JDK**, selecione:
   - **Embedded JDK (JDK 21)** ou **jbr-21** (JetBrains Runtime fornecido pelo próprio Android Studio).
5. Clique em **Apply** e em seguida em **Sync Project with Gradle Files**.

---

### 4.2. Configuração no Terminal Windows (PowerShell)

Caso vá executar o build via linha de comando no terminal do Windows, garanta que a sessão use o JDK 21 (você pode usar o próprio JDK embutido no Android Studio sem precisar instalar nada externo):

```powershell
# 1. Definir JAVA_HOME apontando para o JDK 21 embutido do Android Studio
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
$env:Path="$env:JAVA_HOME\bin;$env:Path"

# 2. Confirmar que a versão ativa é o Java 21
java -version
```

A saída esperada deve indicar **Java 21** (por exemplo: `openjdk version "21.0.x"` ou `JBR-21...`).

---

### 4.3. Validação do Gradle Wrapper

No diretório `android/`, execute a verificação de versão:

```powershell
.\gradlew.bat --version
```

A saída esperada exibirá o Gradle 8.7 executando sob a JVM 21:

```text
------------------------------------------------------------
Gradle 8.7
------------------------------------------------------------

Build time:   2024-03-22 15:52:46 UTC
Revision:     650af95ecd9f9be97a5c0d983a14c33b8e886d6b

Kotlin:       1.9.22
Groovy:       3.0.17
Ant:          Apache Ant(TM) version 1.10.13 compiled on January 4 2023
JVM:          21.0.x (JetBrains s.r.o. / Oracle Corporation ...)
OS:           Windows 11 / 10
```

---

## 5. Como Compilar e Gerar o APK Debug

### Opção 1: Pela Interface do Android Studio
1. No menu superior, clique em **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. O arquivo gerado estará em:
   ```
   android/app/build/outputs/apk/debug/app-debug.apk
   ```

### Opção 2: Pelo Terminal (PowerShell / CMD no Windows)
Navegue até a pasta `android` e execute:

```powershell
.\gradlew.bat clean assembleDebug
```

*(No Linux ou macOS: `./gradlew clean assembleDebug`)*

---

## 6. Como Instalar no Motorola Moto G50 5G

### Opção A: Instalação direta via USB (Recomendado)
1. No Moto G50 5G, ative a **Depuração USB** em **Configurações > Opções do desenvolvedor > Depuração USB**.
2. Conecte o aparelho ao computador via cabo USB.
3. No Android Studio, selecione o **Motorola Moto G50 5G** na lista de dispositivos de destino no topo.
4. Pressione o botão **Run 'app'** (`Shift + F10` ou ícone de Play verde).
5. O Android Studio compilará e instalará o aplicativo diretamente no aparelho.

### Opção B: Instalação manual do APK
1. Transfira o arquivo `app-debug.apk` para o Moto G50 5G (via cabo USB, compartilhamento local ou cartão SD).
2. No aparelho, abra o gerenciador de arquivos (ex.: Files do Google) e toque em `app-debug.apk`.
3. Se solicitado, permita a instalação de apps de fontes desconhecidas para o gerenciador de arquivos.
4. Toque em **Instalar** e em seguida em **Abrir**.
