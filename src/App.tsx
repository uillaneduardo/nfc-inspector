import React, { useState } from 'react';
import { 
  Smartphone, 
  Radio, 
  Layers, 
  ShieldCheck, 
  Terminal, 
  CheckCircle2, 
  FolderTree, 
  FileCode2, 
  Database,
  SlidersHorizontal,
  Info,
  Copy,
  Check
} from 'lucide-react';

export default function App() {
  const [activeTab, setActiveTab] = useState<'overview' | 'structure' | 'architecture' | 'commands'>('overview');
  const [copiedText, setCopiedText] = useState<string | null>(null);

  const copyToClipboard = (text: string, id: string) => {
    navigator.clipboard.writeText(text);
    setCopiedText(id);
    setTimeout(() => setCopiedText(null), 2000);
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col">
      {/* Top Header */}
      <header className="border-b border-slate-800/80 bg-slate-900/60 backdrop-blur-md px-6 py-4 sticky top-0 z-30">
        <div className="max-w-6xl mx-auto flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-xl bg-cyan-500/10 border border-cyan-500/30 flex items-center justify-center text-cyan-400">
              <Radio className="h-5 w-5 animate-pulse" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-lg font-bold text-slate-100 tracking-tight">NFC Inspector</h1>
                <span className="px-2 py-0.5 text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded-full">
                  Android Nativo (Kotlin)
                </span>
              </div>
              <p className="text-xs text-slate-400">Projeto oficial em <code className="text-cyan-300 font-mono">android/</code> com Jetpack Compose & Material 3</p>
            </div>
          </div>

          <div className="flex items-center gap-2 text-xs">
            <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-800 text-slate-300 border border-slate-700">
              <Smartphone className="h-3.5 w-3.5 text-cyan-400" /> Target: Moto G50 5G (Android 12)
            </span>
            <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-800 text-slate-300 border border-slate-700">
              <ShieldCheck className="h-3.5 w-3.5 text-emerald-400" /> 100% Offline (Sem INTERNET)
            </span>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 max-w-6xl w-full mx-auto p-6 flex flex-col gap-6">
        {/* Navigation Tabs */}
        <div className="flex items-center gap-2 border-b border-slate-800 pb-2">
          <button
            onClick={() => setActiveTab('overview')}
            className={`px-4 py-2 text-sm font-medium rounded-lg transition-colors flex items-center gap-2 ${
              activeTab === 'overview'
                ? 'bg-cyan-500/10 text-cyan-400 border border-cyan-500/30'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900'
            }`}
          >
            <CheckCircle2 className="h-4 w-4" /> Status & Correções
          </button>
          <button
            onClick={() => setActiveTab('structure')}
            className={`px-4 py-2 text-sm font-medium rounded-lg transition-colors flex items-center gap-2 ${
              activeTab === 'structure'
                ? 'bg-cyan-500/10 text-cyan-400 border border-cyan-500/30'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900'
            }`}
          >
            <FolderTree className="h-4 w-4" /> Estrutura do App (`android/`)
          </button>
          <button
            onClick={() => setActiveTab('architecture')}
            className={`px-4 py-2 text-sm font-medium rounded-lg transition-colors flex items-center gap-2 ${
              activeTab === 'architecture'
                ? 'bg-cyan-500/10 text-cyan-400 border border-cyan-500/30'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900'
            }`}
          >
            <Layers className="h-4 w-4" /> Fluxo de Leitura & NFC
          </button>
          <button
            onClick={() => setActiveTab('commands')}
            className={`px-4 py-2 text-sm font-medium rounded-lg transition-colors flex items-center gap-2 ${
              activeTab === 'commands'
                ? 'bg-cyan-500/10 text-cyan-400 border border-cyan-500/30'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900'
            }`}
          >
            <Terminal className="h-4 w-4" /> Guia de Build & APK
          </button>
        </div>

        {/* Tab 1: Overview & Corrections */}
        {activeTab === 'overview' && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 flex flex-col gap-4">
              <h2 className="text-base font-semibold text-slate-100 flex items-center gap-2">
                <CheckCircle2 className="h-5 w-5 text-emerald-400" />
                Validações e Correções Aplicadas
              </h2>
              <ul className="space-y-3 text-sm text-slate-300">
                <li className="flex items-start gap-2.5">
                  <span className="h-5 w-5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 flex items-center justify-center text-xs shrink-0 mt-0.5">✓</span>
                  <div>
                    <strong className="text-slate-100">Estado inicial `NfcStatus.Checking`:</strong> O app inicia verificando o hardware antes de declarar o leitor pronto.
                  </div>
                </li>
                <li className="flex items-start gap-2.5">
                  <span className="h-5 w-5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 flex items-center justify-center text-xs shrink-0 mt-0.5">✓</span>
                  <div>
                    <strong className="text-slate-100">Sem auto-save / Salvamento manual:</strong> Tags não poluem o banco automaticamente. Botão "Salvar leitura" com debounce e bloqueio de duplicatas.
                  </div>
                </li>
                <li className="flex items-start gap-2.5">
                  <span className="h-5 w-5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 flex items-center justify-center text-xs shrink-0 mt-0.5">✓</span>
                  <div>
                    <strong className="text-slate-100">Reader Mode exclusivo:</strong> Removidos intent-filters (`TECH_DISCOVERED`, `TAG_DISCOVERED`) do Manifest para evitar aberturas indesejadas com o app fechado.
                  </div>
                </li>
                <li className="flex items-start gap-2.5">
                  <span className="h-5 w-5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 flex items-center justify-center text-xs shrink-0 mt-0.5">✓</span>
                  <div>
                    <strong className="text-slate-100">Tratamento de remoção de tag:</strong> Captura segura de `TagLostException` e `IOException` com mensagem amigável sem fechar o app.
                  </div>
                </li>
                <li className="flex items-start gap-2.5">
                  <span className="h-5 w-5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 flex items-center justify-center text-xs shrink-0 mt-0.5">✓</span>
                  <div>
                    <strong className="text-slate-100">Privacidade 100% offline:</strong> Sem permissão `INTERNET`, sem cloud, sem telemetria e sem dependências de rede.
                  </div>
                </li>
              </ul>
            </div>

            <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 flex flex-col gap-4">
              <h2 className="text-base font-semibold text-slate-100 flex items-center gap-2">
                <SlidersHorizontal className="h-5 w-5 text-cyan-400" />
                Compatibilidade e Hardware
              </h2>
              <div className="space-y-3 text-sm">
                <div className="p-3 bg-slate-950/60 border border-slate-800 rounded-xl flex justify-between items-center">
                  <span className="text-slate-400">Dispositivo Teste:</span>
                  <span className="font-medium text-slate-200">Motorola Moto G50 5G</span>
                </div>
                <div className="p-3 bg-slate-950/60 border border-slate-800 rounded-xl flex justify-between items-center">
                  <span className="text-slate-400">Sistema Operacional:</span>
                  <span className="font-medium text-slate-200">Android 12 (API 31/32)</span>
                </div>
                <div className="p-3 bg-slate-950/60 border border-slate-800 rounded-xl flex justify-between items-center">
                  <span className="text-slate-400">minSdk / targetSdk:</span>
                  <span className="font-mono text-cyan-300">minSdk 26 / targetSdk 34</span>
                </div>
                <div className="p-3 bg-slate-950/60 border border-slate-800 rounded-xl flex justify-between items-center">
                  <span className="text-slate-400">Framework UI:</span>
                  <span className="font-medium text-slate-200">Jetpack Compose + Material 3</span>
                </div>
                <div className="p-3 bg-slate-950/60 border border-slate-800 rounded-xl flex justify-between items-center">
                  <span className="text-slate-400">Banco de Dados:</span>
                  <span className="font-medium text-slate-200">Room (SQLite local offline)</span>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Tab 2: Structure */}
        {activeTab === 'structure' && (
          <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 flex flex-col gap-4">
            <h2 className="text-base font-semibold text-slate-100 flex items-center gap-2">
              <FileCode2 className="h-5 w-5 text-cyan-400" />
              Mapeamento de Fontes em <code className="text-cyan-300 font-mono text-sm">android/</code>
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 font-mono text-xs text-slate-300">
              <div className="p-4 bg-slate-950 border border-slate-800/80 rounded-xl space-y-2">
                <div className="text-cyan-400 font-bold flex items-center gap-1.5 pb-1 border-b border-slate-800">
                  <FolderTree className="h-3.5 w-3.5" /> nfc/ & parser/
                </div>
                <p><strong>NfcManager.kt:</strong> Ciclo de vida Reader Mode, checagem e vibração.</p>
                <p><strong>NfcTagParser.kt:</strong> Parser seguro para NfcA, NfcB, IsoDep, NDEF, Mifare, NfcF, NfcV.</p>
                <p><strong>HceLabPlaceholder.kt:</strong> Esqueleto seguro para estudos de Host Card Emulation.</p>
              </div>

              <div className="p-4 bg-slate-950 border border-slate-800/80 rounded-xl space-y-2">
                <div className="text-cyan-400 font-bold flex items-center gap-1.5 pb-1 border-b border-slate-800">
                  <Database className="h-3.5 w-3.5" /> data/local/ & model/
                </div>
                <p><strong>NfcState.kt:</strong> Estados Checking, Unsupported, Disabled, ReadyWaiting.</p>
                <p><strong>TagRecord.kt:</strong> Modelagem de UID (Hex/Dec), tecnologias e relatório textual.</p>
                <p><strong>AppDatabase.kt / TagDao.kt:</strong> Persistência Room 100% offline.</p>
              </div>

              <div className="p-4 bg-slate-950 border border-slate-800/80 rounded-xl space-y-2">
                <div className="text-cyan-400 font-bold flex items-center gap-1.5 pb-1 border-b border-slate-800">
                  <Smartphone className="h-3.5 w-3.5" /> ui/screens/
                </div>
                <p><strong>ReaderScreen.kt:</strong> Radar de leitura, status dinâmico e salvamento manual.</p>
                <p><strong>HistoryScreen.kt:</strong> Histórico de tags salvas com busca e exclusão.</p>
                <p><strong>CompareScreen.kt:</strong> Comparador diferencial de 2 tags lado a lado.</p>
                <p><strong>ReportScreen.kt:</strong> Relatório técnico com compartilhamento local.</p>
                <p><strong>AboutScreen.kt:</strong> Guia educacional sobre frequências e tecnologias NFC.</p>
              </div>

              <div className="p-4 bg-slate-950 border border-slate-800/80 rounded-xl space-y-2">
                <div className="text-cyan-400 font-bold flex items-center gap-1.5 pb-1 border-b border-slate-800">
                  <Layers className="h-3.5 w-3.5" /> ui/viewmodel/ & root
                </div>
                <p><strong>MainViewModel.kt:</strong> StateFlow reativo, fluxo de leitura e persistência.</p>
                <p><strong>MainActivity.kt:</strong> Gerenciador de navegação e ciclo de vida.</p>
                <p><strong>AndroidManifest.xml:</strong> Permissões mínimas (NFC + VIBRATE).</p>
              </div>
            </div>
          </div>
        )}

        {/* Tab 3: Architecture */}
        {activeTab === 'architecture' && (
          <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 flex flex-col gap-6">
            <h2 className="text-base font-semibold text-slate-100 flex items-center gap-2">
              <Radio className="h-5 w-5 text-cyan-400" />
              Ciclo de Vida do NFC Reader Mode
            </h2>
            <div className="flex flex-col gap-3 text-sm">
              <div className="p-4 bg-slate-950 border border-slate-800 rounded-xl flex items-start gap-3">
                <div className="h-7 w-7 rounded-lg bg-cyan-500/10 text-cyan-400 flex items-center justify-center font-bold text-xs shrink-0">1</div>
                <div>
                  <h3 className="font-semibold text-slate-200">App Abre / `onResume`</h3>
                  <p className="text-slate-400 text-xs mt-1">
                    O ViewModel inicia em <code className="text-cyan-300 font-mono">NfcStatus.Checking</code>. O `NfcManager` consulta o `NfcAdapter`. Se o NFC estiver desligado, exibe botão para abrir as Configurações do Sistema.
                  </p>
                </div>
              </div>

              <div className="p-4 bg-slate-950 border border-slate-800 rounded-xl flex items-start gap-3">
                <div className="h-7 w-7 rounded-lg bg-cyan-500/10 text-cyan-400 flex items-center justify-center font-bold text-xs shrink-0">2</div>
                <div>
                  <h3 className="font-semibold text-slate-200">Ativação do Reader Mode</h3>
                  <p className="text-slate-400 text-xs mt-1">
                    Com o NFC ativo, é chamado <code className="text-cyan-300 font-mono">NfcAdapter.enableReaderMode</code> com flags <code className="text-slate-300 font-mono">FLAG_READER_NFC_A | B | F | V | SKIP_NDEF_CHECK</code>.
                  </p>
                </div>
              </div>

              <div className="p-4 bg-slate-950 border border-slate-800 rounded-xl flex items-start gap-3">
                <div className="h-7 w-7 rounded-lg bg-cyan-500/10 text-cyan-400 flex items-center justify-center font-bold text-xs shrink-0">3</div>
                <div>
                  <h3 className="font-semibold text-slate-200">Aproximação e Parse do Cartão</h3>
                  <p className="text-slate-400 text-xs mt-1">
                    O callback <code className="text-cyan-300 font-mono">onTagDiscovered(Tag)</code> dispara vibração háptica curta de 50ms, processa os parâmetros via <code className="text-cyan-300 font-mono">NfcTagParser</code> e entrega os dados para a UI. <strong>Não salva no banco automaticamente</strong>.
                  </p>
                </div>
              </div>

              <div className="p-4 bg-slate-950 border border-slate-800 rounded-xl flex items-start gap-3">
                <div className="h-7 w-7 rounded-lg bg-cyan-500/10 text-cyan-400 flex items-center justify-center font-bold text-xs shrink-0">4</div>
                <div>
                  <h3 className="font-semibold text-slate-200">Pausa / Segundo Plano (`onPause`)</h3>
                  <p className="text-slate-400 text-xs mt-1">
                    Chama imediatamente <code className="text-cyan-300 font-mono">NfcAdapter.disableReaderMode</code> liberando o hardware do aparelho para outros processos do sistema.
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Tab 4: Commands & Build */}
        {activeTab === 'commands' && (
          <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 flex flex-col gap-6">
            <div>
              <h2 className="text-base font-semibold text-slate-100 flex items-center gap-2">
                <Terminal className="h-5 w-5 text-cyan-400" />
                Como Compilar e Gerar o APK Debug
              </h2>
              <p className="text-xs text-slate-400 mt-1">O projeto pode ser aberto diretamente no Android Studio na pasta <code className="text-cyan-300 font-mono">android/</code>.</p>
            </div>

            <div className="space-y-4">
              <div className="p-4 bg-slate-950 border border-slate-800 rounded-xl space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-semibold text-slate-400">Comando para compilar APK Debug:</span>
                  <button
                    onClick={() => copyToClipboard('cd android && ./gradlew assembleDebug', 'build-cmd')}
                    className="text-xs text-cyan-400 hover:text-cyan-300 flex items-center gap-1"
                  >
                    {copiedText === 'build-cmd' ? <Check className="h-3.5 w-3.5" /> : <Copy className="h-3.5 w-3.5" />}
                    {copiedText === 'build-cmd' ? 'Copiado!' : 'Copiar'}
                  </button>
                </div>
                <pre className="p-3 bg-slate-900 text-cyan-300 font-mono text-xs rounded-lg overflow-x-auto">
                  cd android && ./gradlew assembleDebug
                </pre>
              </div>

              <div className="p-4 bg-slate-950 border border-slate-800 rounded-xl space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-semibold text-slate-400">Localização do APK gerado:</span>
                </div>
                <pre className="p-3 bg-slate-900 text-emerald-400 font-mono text-xs rounded-lg overflow-x-auto">
                  android/app/build/outputs/apk/debug/app-debug.apk
                </pre>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-800/80 bg-slate-950 px-6 py-4 text-center text-xs text-slate-500">
        NFC Inspector • Projeto Android Nativo em Kotlin & Jetpack Compose • 100% Offline & Seguro
      </footer>
    </div>
  );
}
