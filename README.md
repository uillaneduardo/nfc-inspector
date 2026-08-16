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
  - `nfc.lab.HceLabPlaceholder.kt`: Base arquitetural isolada para futuros experimentos de Host Card Emulation (HCE) com protocolo próprio de laboratório.
- **`com.nfcinspector.app.ui`**:
  - `ReaderScreen.kt`: Tela principal com estados de verificação, indisponibilidade, desativado, espera e tag detectada.
  - `HistoryScreen.kt`: Histórico local de leituras salvas offline.
  - `CompareScreen.kt`: Comparação lado a lado entre leituras.
  - `ReportScreen.kt`: Relatório técnico completo com ações de copiar, compartilhar e salvar.
  - `AboutScreen.kt`: Guia educacional sobre NFC, UID, NDEF, ISO-DEP e MIFARE.

---

## 2. Fluxo de Leitura NFC

O aplicativo utiliza o **Android NFC Reader Mode**:

1. verifica o hardware ao abrir;
2. informa quando NFC não é suportado;
3. oferece acesso às configurações quando NFC está desligado;
4. entra em modo de espera quando NFC está ativo;
5. processa a tag em `onTagDiscovered(Tag)`;
6. exibe os dados técnicos;
7. salva a leitura apenas mediante ação explícita do usuário.

---

## 3. Ambiente de Build

- **Dispositivo alvo inicial:** Motorola Moto G50 5G (Android 12)
- **minSdk:** 26
- **targetSdk:** 34
- **compileSdk:** 34
- **Gradle JVM:** JDK 21
- **Gradle Wrapper:** 8.7
- **Android Gradle Plugin:** 8.3.2
- **Kotlin:** 2.0.0
- **Java source/target:** 17
- **Kotlin jvmTarget:** 17
- **Android SDK Platform:** 34
- **Build Tools:** 34.0.0

A primeira build real em Windows foi validada com sucesso através de:

```powershell
.\gradlew.bat clean assembleDebug
```

---

## 4. Documentação de desenvolvimento

Os procedimentos detalhados foram separados do README:

- [Build do APK Android no Windows](docs/android-build.md)
- [Troubleshooting de build Android](docs/troubleshooting.md)

Esses documentos registram o ambiente validado e os problemas encontrados durante a primeira compilação real, incluindo JDK, Gradle Wrapper, Android SDK e erros Kotlin/Compose.

---

## 5. Gerar o APK Debug

No diretório `android/`:

```powershell
.\gradlew.bat clean assembleDebug
```

O APK será gerado em:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

---

## 6. Instalação no Android

O APK Debug pode ser instalado manualmente no dispositivo após autorizar a origem utilizada para abrir o arquivo.

Para desenvolvimento via USB, também é possível ativar a Depuração USB no Android e executar o módulo `app` diretamente pelo Android Studio.

---

## 7. Estrutura do repositório

O aplicativo real está em:

```text
android/
```

Arquivos React/Vite existentes na raiz são utilizados somente pelo preview visual do AI Studio e não fazem parte do runtime nem do APK Android.
