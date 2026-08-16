# NFC Inspector — Aplicativo Android Nativo

**NFC Inspector** é um aplicativo Android nativo completo desenvolvido em **Kotlin** com **Jetpack Compose** e **Material 3**, voltado para diagnóstico técnico aprofundado, identificação e inspeção de tags e cartões NFC pertencentes ao usuário ou sob autorização.

100% gratuito, offline-first, sem anúncios, sem rastreadores, sem telemetria e sem coleta de dados.

---

## 1. Arquitetura do Projeto

O projeto é estruturado seguindo rigorosamente **Clean Architecture**, **MVVM** e **Unidirectional Data Flow**:

- **`com.nfcinspector.app.domain`**:
  - `model.ReaderSource.kt`: Identificação e metadados da origem de leitura (`ANDROID_NFC`, `USB`, `BLUETOOTH`, `REMOTE`, `IMPORTED`).
  - `model.ReaderCapabilities.kt`: Consulta declarativa de capacidades do leitor ativo (`READ`, `WRITE`, `ISO_DEP`, `MIFARE_CLASSIC`, `NDEF`, `APDU`, `HCE`).
  - `transport.NfcTransport.kt`: Contrato de abstração da camada de transporte desacoplada da API do Android.
  - `operation.NfcOperationResult.kt`: Tratamento estruturado de resultados e erros de domínio (`TagLost`, `AuthenticationFailed`, `UnsupportedTechnology`, etc.).
- **`com.nfcinspector.app.data.model`**:
  - `TagRecord.kt`: Modelo central de agregação da leitura contendo `scanId` (UUID v4), `readerSource`, formatações de UID, metadados de tecnologia e mapa de memória.
  - `TechDataModels.kt`: Modelos para cada tecnologia de RF (`NfcAParams`, `NfcBParams`, `IsoDepParams`, `MifareClassicParams`, `MifareUltralightParams`, `NfcFParams`, `NfcVParams`, `NdefParams`, `NdefRecordItem`).
  - `NfcState.kt`: Modelagem dos estados reativos do hardware.
- **`com.nfcinspector.app.data.local` & `repository`**:
  - `AppDatabase.kt`, `TagDao.kt`, `TagEntity.kt`: Persistência local utilizando **Room Database (v2)** com migração segura `MIGRATION_1_2` (preserva dados e adiciona `scanUuid` e `readerSource`).
  - `TechSerializer.kt`: Serialização e desserialização isolada dos parâmetros de RF e mapas de memória para persistência em SQLite.
  - `HistoryRepository.kt`: Repositório para o fluxo de leituras salvas offline, busca por ID, exclusão e deduplicação estável.
- **`com.nfcinspector.app.nfc`**:
  - `NfcManager.kt`: Gerenciamento do ciclo de vida com `NfcAdapter.enableReaderMode` e `disableReaderMode`, verificação de estado em `onResume`, feedback háptico (`VibrationEffect`) e intents com fallback.
  - `NfcTagParser.kt`: Parser seguro com tratamento de desconexão e fechamento seguro de conexões para `NfcA`, `NfcB`, `IsoDep`, `Ndef`, `MifareClassic`, `MifareUltralight`, `NfcF` e `NfcV`.
  - `mifare.MifareClassicInspector.kt`: Motor de autenticação por chaves padrão e inspeção estrutural de setores/blocos com sanitização criptográfica.
  - `android.AndroidNfcTransport.kt`: Adaptador de transporte concreto para o leitor interno Android.
- **`com.nfcinspector.app.report`**:
  - `ReportFormatter.kt`: Gerador de relatórios técnicos em texto puro e DTOs estruturados no padrão **JSON Schema v1**.
- **`com.nfcinspector.app.ui`**:
  - `ReaderScreen.kt`: Tela principal com estados de hardware, diagnóstico e cartões de parâmetros técnicos.
  - `HistoryScreen.kt`: Histórico local de leituras salvas offline com pesquisa e atalhos de comparação.
  - `CompareScreen.kt`: Comparador diferencial lado a lado entre duas leituras com destaque de equivalências e divergências.
  - `ReportScreen.kt`: Tela de relatório estruturada com abas distintas: **Visual (Compose)**, **Texto Técnico (TXT)** e **Interoperabilidade (JSON)**.
  - `AboutScreen.kt`: Guia educacional técnico sobre NFC, segurança, privacidade e boas práticas.
  - `MainActivity.kt` & `Theme.kt`: Gerenciamento do ciclo de vida, navegação Material 3 (`NavigationBar`), suporte a tema dinâmico e tema escuro.

Documentação detalhada da arquitetura: consulte [`docs/architecture.md`](docs/architecture.md).

---

## 2. Abstrações de Transporte & Extensibilidade

O NFC Inspector separa rigorosamente **Camada de Transporte** de **Camada de Protocolo**:

1. **`NfcTransport`**: Interface responsável pela entrega bruta de comandos e recepção de respostas.
2. **`ReaderSource`**: Identifica a origem do leitor utilizado em cada diagnóstico (`NFC Interno Android`, `Leitor USB Externo`, `Leitor Bluetooth`, `Remoto`).
3. **`ReaderCapabilities`**: Informa dinamicamente as funcionalidades suportadas pelo hardware ativo sem suposições estáticas.
4. **`scanId` (UUID)**: Todo diagnóstico possui um identificador global único que viabiliza exportação padronizada e futura sincronização sem colisão.

---

## 3. Fontes de Leitura

| Fonte de Leitura | Estado | Descrição |
| :--- | :--- | :--- |
| **NFC Interno Android** | ✅ **Implementado** | Leitura direta pelo sensor NFC integrado do smartphone (Reader Mode). |
| **Leitor USB Externo (CCID)** | ⏳ *Planejado* | Suporte a leitores USB plug-and-play via USB Host (ex.: ACR122U). |
| **Leitor Bluetooth / BLE** | ⏳ *Planejado* | Conexão com leitores portáteis sem fio. |
| **Importação de Arquivo** | ⏳ *Planejado* | Carregamento de diagnósticos pré-gravados em JSON Schema v1. |

---

## 4. Visão de Futuro e Roadmap

### NFC Inspector Core
* Motor de análise aprofundada de protocolos (ISO 14443-4 ISO-DEP, NDEF, MIFARE Classic/Ultralight, FeliCa, ISO 15693).
* Diagnósticos de segurança e verificação de consistência de bits de acesso.

### NFC Inspector Lab (Planejado)
* Emulação de cartões de teste via Host Card Emulation (HCE) para perfis ISO-DEP / NDEF.
* **Nota Técnica de Engenharia**: Cartões MIFARE Classic operam com modulação de camada física proprietária e enquadramento incompatível com a pilha HCE padrão do Android (que atua exclusivamente em ISO 14443-4 / ISO-DEP). O Lab detalhará de forma clara as limitações de emulação da plataforma.

### NFC Inspector Sync (Planejado)
* API REST leve e pareamento ponta-a-ponta via QR Code.
* Sincronização segura descentralizada e 100% offline entre dispositivos móveis e desktops de auditoria.

---

## 5. Especificações e Ambiente de Build

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
  - `android.hardware.nfc` (`android:required="false"` para permitir instalação e aviso informativo em qualquer aparelho).
  - **Zero permissões de rede (`android.permission.INTERNET` NÃO solicitada)**.

---

## 6. Como Compilar e Executar

### 6.1. Configuração do JDK 21 no Android Studio
1. Abra o projeto pela pasta `android/`.
2. Acesse: **File > Settings > Build, Execution, Deployment > Build Tools > Gradle**.
3. No campo **Gradle JDK**, selecione **Embedded JDK (JDK 21)** ou **jbr-21**.
4. Clique em **Sync Project with Gradle Files**.

### 6.2. Gerando o APK Debug via Terminal
No diretório `android`:

```bash
./gradlew clean assembleDebug
```

*(No Windows: `.\gradlew.bat clean assembleDebug`)*

O APK gerado estará em:
```
android/app/build/outputs/apk/debug/app-debug.apk
```

---

## 7. Privacidade e Segurança

1. **Operação 100% Offline**: O aplicativo não possui permissão de internet e não se conecta a nenhum servidor.
2. **Sem Coleta de Dados**: Nenhum identificador intrusivo do dispositivo (IMEI, Android ID, número de série) é lido ou armazenado.
3. **Proteção de Chaves**: Chaves criptográficas não são exportadas ou gravadas em texto puro nos relatórios compartilháveis.
