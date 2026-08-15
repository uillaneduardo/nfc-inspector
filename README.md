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

## 3. Especificações e Versões de SDK

- **Dispositivo Alvo Inicial**: Motorola Moto G50 5G (Android 12)
- **`minSdk`**: `26` (Android 8.0 Oreo ou superior - compatibilidade total com Android 12)
- **`targetSdk`**: `34` (Android 14)
- **`compileSdk`**: `34`
- **Linguagem**: Kotlin 2.0.0
- **UI Toolkit**: Jetpack Compose com Material 3 (BOM 2024.06.00)
- **Permissões Declaradas no `AndroidManifest.xml`**:
  - `android.permission.NFC`
  - `android.permission.VIBRATE`
  - `android.hardware.nfc` (configurado como `android:required="false"` para permitir instalação e aviso informativo em qualquer aparelho).
  - **Zero permissões de rede (`android.permission.INTERNET` NÃO solicitada)**.

---

## 4. Como Abrir e Compilar no Android Studio

### Passo 1: Abrir o Projeto
1. Abra o **Android Studio** (versão Iguana, Jellyfish, Koala ou superior).
2. Na tela inicial, clique em **Open** (ou **File > Open**).
3. Selecione o diretório `android` do projeto.
4. Aguarde a sincronização inicial do Gradle (**Gradle Sync**).

### Passo 2: Gerar APK Debug
1. No menu superior do Android Studio, clique em **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. Ou execute no terminal integrado no diretório `android`:
   ```bash
   ./gradlew assembleDebug
   ```

### Passo 3: Localização do APK Gerado
O arquivo APK compilado estará localizado em:
```
android/app/build/outputs/apk/debug/app-debug.apk
```

---

## 5. Como Instalar no Motorola Moto G50 5G

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
