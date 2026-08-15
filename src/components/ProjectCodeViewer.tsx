import React, { useState } from 'react';
import JSZip from 'jszip';
import {
  Folder,
  FileCode,
  Download,
  Copy,
  Check,
  Code2,
  Layers,
  Terminal,
  ExternalLink,
} from 'lucide-react';

interface ProjectCodeViewerProps {
  onClose?: () => void;
}

export const ANDROID_FILES: Record<string, string> = {
  'build.gradle.kts (Project)': `// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}`,

  'settings.gradle.kts': `pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\\\.android.*")
                includeGroupByRegex("com\\\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NFC Inspector"
include(":app")`,

  'gradle/libs.versions.toml': `[versions]
agp = "8.3.2"
kotlin = "2.0.0"
coreKtx = "1.13.1"
lifecycleRuntimeKtx = "2.8.3"
activityCompose = "1.9.0"
composeBom = "2024.06.00"
navigationCompose = "2.7.7"
room = "2.6.1"
ksp = "2.0.0-1.0.21"
materialIconsExtended = "1.6.8"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended", version.ref = "materialIconsExtended" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }`,

  'app/build.gradle.kts': `plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.nfcinspector.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nfcinspector.app"
        minSdk = 26 // Moto G50 5G Android 12 compatible
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}`,

  'app/src/main/AndroidManifest.xml': `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.NFC" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <!-- Permite instalar em qualquer aparelho e lidar com NFC Ausente -->
    <uses-feature
        android:name="android.hardware.nfc"
        android:required="false" />

    <application
        android:name=".NfcInspectorApplication"
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.NFCInspector"
        tools:targetApi="34">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
            android:theme="@style/Theme.NFCInspector">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>

            <intent-filter>
                <action android:name="android.nfc.action.TECH_DISCOVERED" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.nfc.action.TAG_DISCOVERED" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </activity>

    </application>

</manifest>`,

  'MainActivity.kt': `package com.nfcinspector.app

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.nfcinspector.app.data.model.NfcStatus
import com.nfcinspector.app.nfc.NfcManager
import com.nfcinspector.app.ui.screens.MainAppScaffold
import com.nfcinspector.app.ui.theme.NFCInspectorTheme
import com.nfcinspector.app.ui.viewmodel.MainViewModel
import com.nfcinspector.app.ui.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as NfcInspectorApplication).repository)
    }

    private lateinit var nfcManager: NfcManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcManager = NfcManager(
            context = this,
            onTagScanned = { tagRecord ->
                runOnUiThread { viewModel.onTagScanned(tagRecord) }
            },
            onError = { errorMsg ->
                runOnUiThread { viewModel.onScanError(errorMsg) }
            }
        )

        handleNfcIntent(intent)

        setContent {
            NFCInspectorTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val status = nfcManager.checkNfcStatus()
        viewModel.updateNfcStatus(status)
        if (status is NfcStatus.ReadyWaiting || status is NfcStatus.TagDetected) {
            nfcManager.startReaderMode(this)
        }
    }

    override fun onPause() {
        super.onPause()
        nfcManager.stopReaderMode(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == action ||
            NfcAdapter.ACTION_TAG_DISCOVERED == action
        ) {
            @Suppress("DEPRECATION")
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                nfcManager.onTagDiscovered(tag)
            }
        }
    }
}`,

  'NfcManager.kt': `package com.nfcinspector.app.nfc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import com.nfcinspector.app.data.model.NfcStatus
import com.nfcinspector.app.data.model.TagRecord

class NfcManager(
    private val context: Context,
    private val onTagScanned: (TagRecord) -> Unit,
    private val onError: (String) -> Unit
) : NfcAdapter.ReaderCallback {

    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)

    fun checkNfcStatus(): NfcStatus {
        if (nfcAdapter == null) return NfcStatus.Unsupported
        if (!nfcAdapter.isEnabled) return NfcStatus.Disabled
        return NfcStatus.ReadyWaiting
    }

    fun startReaderMode(activity: Activity) {
        val adapter = nfcAdapter ?: return
        if (!adapter.isEnabled) return

        val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NFC_V or
                NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS

        val options = Bundle()
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 200)

        try {
            adapter.enableReaderMode(activity, this, flags, options)
        } catch (e: Exception) {
            onError("Erro ao iniciar modo leitor: \${e.localizedMessage}")
        }
    }

    fun stopReaderMode(activity: Activity) {
        nfcAdapter?.disableReaderMode(activity)
    }

    override fun onTagDiscovered(tag: Tag?) {
        if (tag == null) return
        triggerHapticFeedback()
        try {
            val record = NfcTagParser.parseTag(tag)
            onTagScanned(record)
        } catch (e: Exception) {
            onError("Erro de leitura: \${e.localizedMessage}")
        }
    }

    private fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                v?.vibrate(50)
            }
        } catch (_: Exception) {}
    }

    companion object {
        fun openNfcSettings(context: Context) {
            try {
                context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
            } catch (e: Exception) {
                context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
            }
        }
    }
}`,

  'NfcTagParser.kt': `package com.nfcinspector.app.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.*
import com.nfcinspector.app.data.model.*
import java.math.BigInteger
import java.util.Locale

object NfcTagParser {
    fun parseTag(tag: Tag): TagRecord {
        val rawId = tag.id ?: byteArrayOf()
        val uidColonHex = toColonHex(rawId)
        val uidContinuousHex = toContinuousHex(rawId)
        val uidDecimal = toDecimalString(rawId)
        val uidLengthBytes = rawId.size

        val techList = tag.techList.map { it.substringAfterLast(".") }
        val mainTech = determineMainTech(techList)

        val nfcAParams = parseNfcA(tag)
        val nfcBParams = parseNfcB(tag)
        val isoDepParams = parseIsoDep(tag)
        val mifareClassicParams = parseMifareClassic(tag)
        val mifareUltralightParams = parseMifareUltralight(tag)
        val nfcFParams = parseNfcF(tag)
        val nfcVParams = parseNfcV(tag)
        val ndefParams = parseNdef(tag)
        val isNdefFormatable = NdefFormatable.get(tag) != null

        return TagRecord(
            uidColonHex = uidColonHex,
            uidContinuousHex = uidContinuousHex,
            uidDecimal = uidDecimal,
            uidLengthBytes = uidLengthBytes,
            mainTechnology = mainTech,
            technologies = techList,
            nfcA = nfcAParams,
            nfcB = nfcBParams,
            isoDep = isoDepParams,
            mifareClassic = mifareClassicParams,
            mifareUltralight = mifareUltralightParams,
            nfcF = nfcFParams,
            nfcV = nfcVParams,
            ndef = ndefParams,
            isNdefFormatable = isNdefFormatable
        )
    }

    private fun determineMainTech(techList: List<String>): String {
        return when {
            techList.contains("IsoDep") && techList.contains("NfcA") -> "ISO 14443-4A (ISO-DEP / Smart Card)"
            techList.contains("MifareClassic") -> "NXP MIFARE Classic"
            techList.contains("MifareUltralight") -> "NXP MIFARE Ultralight / NTAG"
            techList.contains("NfcA") && techList.contains("Ndef") -> "NFC Forum Type 2 / Type 4"
            techList.contains("NfcA") -> "ISO 14443-3A (NFC-A)"
            techList.contains("NfcB") -> "ISO 14443-3B (NFC-B)"
            techList.contains("NfcF") -> "JIS 6319-4 (Sony FeliCa / NFC-F)"
            techList.contains("NfcV") -> "ISO 15693 (Vicinity / NFC-V)"
            else -> techList.firstOrNull() ?: "Tag NFC"
        }
    }

    fun toColonHex(bytes: ByteArray): String =
        bytes.joinToString(":") { String.format(Locale.US, "%02X", it) }

    fun toContinuousHex(bytes: ByteArray): String =
        bytes.joinToString("") { String.format(Locale.US, "%02X", it) }

    fun toDecimalString(bytes: ByteArray): String =
        try { BigInteger(1, bytes).toString(10) } catch (e: Exception) { "N/A" }
}`,
};

export const ProjectCodeViewer: React.FC<ProjectCodeViewerProps> = () => {
  const [selectedFile, setSelectedFile] = useState<string>('MainActivity.kt');
  const [copied, setCopied] = useState(false);
  const [isExporting, setIsExporting] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(ANDROID_FILES[selectedFile]);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDownloadZip = async () => {
    setIsExporting(true);
    try {
      const zip = new JSZip();

      // Root Gradle
      zip.file('build.gradle.kts', ANDROID_FILES['build.gradle.kts (Project)']);
      zip.file('settings.gradle.kts', ANDROID_FILES['settings.gradle.kts']);
      zip.file('gradle.properties', 'org.gradle.jvmargs=-Xmx2048m\nandroid.useAndroidX=true\n');
      
      const gradleWrapper = zip.folder('gradle/wrapper');
      gradleWrapper?.file('gradle-wrapper.properties', 'distributionUrl=https\\://services.gradle.org/distributions/gradle-8.7-bin.zip\n');
      
      const gradleFolder = zip.folder('gradle');
      gradleFolder?.file('libs.versions.toml', ANDROID_FILES['gradle/libs.versions.toml']);

      // App folder
      const app = zip.folder('app');
      app?.file('build.gradle.kts', ANDROID_FILES['app/build.gradle.kts']);
      app?.file('proguard-rules.pro', '-keep class com.nfcinspector.app.data.model.** { *; }\n');

      const main = app?.folder('src/main');
      main?.file('AndroidManifest.xml', ANDROID_FILES['app/src/main/AndroidManifest.xml']);

      const java = main?.folder('java/com/nfcinspector/app');
      java?.file('MainActivity.kt', ANDROID_FILES['MainActivity.kt']);
      
      const nfcFolder = java?.folder('nfc');
      nfcFolder?.file('NfcManager.kt', ANDROID_FILES['NfcManager.kt']);
      nfcFolder?.file('NfcTagParser.kt', ANDROID_FILES['NfcTagParser.kt']);

      // Generate blob
      const content = await zip.generateAsync({ type: 'blob' });
      const url = URL.createObjectURL(content);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'NFC_Inspector_Android_Project.zip';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (e) {
      console.error(e);
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <div className="space-y-4 pb-12">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-neutral-900 border border-neutral-800 p-4 rounded-2xl">
        <div>
          <h2 className="text-base font-bold text-white flex items-center gap-2">
            <Code2 className="w-5 h-5 text-blue-400" />
            <span>Código Nativo Android (Kotlin + Compose)</span>
          </h2>
          <p className="text-xs text-neutral-400">
            Estrutura 100% nativa pronta para importar e compilar no Android Studio.
          </p>
        </div>

        <button
          onClick={handleDownloadZip}
          disabled={isExporting}
          className="inline-flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold px-4 py-2.5 rounded-xl transition shadow-sm"
        >
          <Download className="w-4 h-4" />
          <span>{isExporting ? 'Compactando...' : 'Baixar Projeto Android Studio (.ZIP)'}</span>
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-12 gap-3">
        {/* File Tree List */}
        <div className="md:col-span-4 bg-neutral-900 border border-neutral-800 rounded-xl p-2 space-y-1">
          <div className="text-[11px] font-bold text-neutral-500 uppercase px-2 py-1 tracking-wider">
            Arquivos do Projeto
          </div>
          {Object.keys(ANDROID_FILES).map(fileName => (
            <button
              key={fileName}
              onClick={() => setSelectedFile(fileName)}
              className={`w-full text-left px-2.5 py-1.5 rounded-lg text-xs font-mono flex items-center gap-2 transition ${
                selectedFile === fileName
                  ? 'bg-blue-600/20 text-blue-400 font-semibold border border-blue-500/30'
                  : 'text-neutral-400 hover:bg-neutral-800/60 hover:text-neutral-200'
              }`}
            >
              <FileCode className="w-3.5 h-3.5 shrink-0" />
              <span className="truncate">{fileName}</span>
            </button>
          ))}
        </div>

        {/* Code Content */}
        <div className="md:col-span-8 bg-neutral-950 border border-neutral-800 rounded-xl overflow-hidden flex flex-col">
          <div className="flex items-center justify-between px-4 py-2.5 bg-neutral-900 border-b border-neutral-800 text-xs">
            <span className="font-mono text-neutral-300 font-medium">{selectedFile}</span>
            <button
              onClick={handleCopy}
              className="inline-flex items-center gap-1 text-neutral-400 hover:text-white text-xs"
            >
              {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
              <span>{copied ? 'Copiado' : 'Copiar'}</span>
            </button>
          </div>
          <div className="p-4 overflow-x-auto max-h-[480px]">
            <pre className="font-mono text-xs text-neutral-300 whitespace-pre leading-relaxed">
              {ANDROID_FILES[selectedFile]}
            </pre>
          </div>
        </div>
      </div>
    </div>
  );
};
