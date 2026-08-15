import React, { useState, useEffect } from 'react';
import {
  Nfc,
  History as HistoryIcon,
  ArrowRightLeft,
  Info,
  Code2,
  Smartphone,
  Sparkles,
  RefreshCw,
  Power,
  ShieldCheck,
} from 'lucide-react';
import { TagRecord, NfcStateEnum } from './types';
import { SAMPLE_TAGS, generateReport } from './data/sampleTags';
import { ReaderView } from './components/ReaderView';
import { HistoryView } from './components/HistoryView';
import { CompareView } from './components/CompareView';
import { ReportView } from './components/ReportView';
import { AboutView } from './components/AboutView';
import { ProjectCodeViewer } from './components/ProjectCodeViewer';

export default function App() {
  const [activeTab, setActiveTab] = useState<'reader' | 'history' | 'compare' | 'about' | 'code'>('reader');
  const [nfcState, setNfcState] = useState<NfcStateEnum>('ready');
  const [currentTag, setCurrentTag] = useState<TagRecord | null>(null);
  const [reportTag, setReportTag] = useState<TagRecord | null>(null);

  // Local storage history persistence
  const [history, setHistory] = useState<TagRecord[]>(() => {
    const saved = localStorage.getItem('nfc_inspector_history');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch (_) {}
    }
    return SAMPLE_TAGS.slice(0, 3);
  });

  // Compare slots
  const [compareTag1, setCompareTag1] = useState<TagRecord | null>(() => history[0] || null);
  const [compareTag2, setCompareTag2] = useState<TagRecord | null>(() => history[1] || null);

  useEffect(() => {
    localStorage.setItem('nfc_inspector_history', JSON.stringify(history));
  }, [history]);

  const handleSimulateTap = (tag: TagRecord) => {
    // Generate fresh timestamp
    const now = new Date();
    const formatted = now.toLocaleDateString('pt-BR') + ' ' + now.toLocaleTimeString('pt-BR');
    const newRecord: TagRecord = {
      ...tag,
      id: 'scan_' + Date.now(),
      timestamp: Date.now(),
      formattedDateTime: formatted,
    };
    newRecord.fullReport = generateReport(newRecord);

    setCurrentTag(newRecord);
    setNfcState('detected');

    // Automatically save to offline history
    setHistory(prev => [newRecord, ...prev.filter(p => p.id !== newRecord.id)]);
  };

  const handleScanAnother = () => {
    setCurrentTag(null);
    setNfcState('ready');
  };

  const handleDeleteScan = (id: string) => {
    setHistory(prev => prev.filter(item => item.id !== id));
    if (compareTag1?.id === id) setCompareTag1(null);
    if (compareTag2?.id === id) setCompareTag2(null);
  };

  const handleClearAll = () => {
    setHistory([]);
    setCompareTag1(null);
    setCompareTag2(null);
  };

  const handleSelectForCompare = (slot: 1 | 2, tag: TagRecord) => {
    if (slot === 1) {
      setCompareTag1(tag);
    } else {
      setCompareTag2(tag);
    }
    setActiveTab('compare');
  };

  const handleSaveToHistory = (tag: TagRecord) => {
    setHistory(prev => [tag, ...prev.filter(p => p.id !== tag.id)]);
  };

  return (
    <div className="min-h-screen bg-neutral-950 text-neutral-100 flex flex-col font-sans selection:bg-blue-500 selection:text-white">
      {/* Top App Bar */}
      <header className="sticky top-0 z-40 bg-neutral-900/90 backdrop-blur-md border-b border-neutral-800">
        <div className="max-w-4xl mx-auto px-4 h-14 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center text-white shadow-sm shadow-blue-500/30">
              <Nfc className="w-4 h-4" />
            </div>
            <div>
              <h1 className="font-bold text-sm text-white tracking-tight flex items-center gap-1.5">
                <span>NFC Inspector</span>
                <span className="text-[10px] bg-neutral-800 text-blue-400 font-mono px-1.5 py-0.5 rounded border border-neutral-700">
                  Android 12
                </span>
              </h1>
            </div>
          </div>

          {/* Quick Simulation & State Switchers */}
          <div className="flex items-center gap-1.5">
            <button
              onClick={() => setNfcState(s => (s === 'ready' ? 'disabled' : s === 'disabled' ? 'unsupported' : 'ready'))}
              className={`text-xs px-2.5 py-1.5 rounded-lg border flex items-center gap-1.5 transition ${
                nfcState === 'ready' || nfcState === 'detected'
                  ? 'bg-emerald-950/40 border-emerald-800/60 text-emerald-400 hover:bg-emerald-900/50'
                  : nfcState === 'disabled'
                  ? 'bg-amber-950/40 border-amber-800/60 text-amber-400 hover:bg-amber-900/50'
                  : 'bg-red-950/40 border-red-800/60 text-red-400 hover:bg-red-900/50'
              }`}
              title="Alternar estado do adaptador NFC (Ativo / Desativado / Não Suportado)"
            >
              <Power className="w-3 h-3" />
              <span className="hidden sm:inline font-medium">
                {nfcState === 'ready' || nfcState === 'detected'
                  ? 'NFC Ativo'
                  : nfcState === 'disabled'
                  ? 'NFC Desativado'
                  : 'NFC Incompatível'}
              </span>
            </button>

            <button
              onClick={() => setActiveTab('code')}
              className={`text-xs px-2.5 py-1.5 rounded-lg border flex items-center gap-1.5 transition ${
                activeTab === 'code'
                  ? 'bg-blue-600 text-white border-blue-500'
                  : 'bg-neutral-800 border-neutral-700 text-neutral-300 hover:bg-neutral-700'
              }`}
            >
              <Code2 className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">Código Android</span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-4xl w-full mx-auto p-4 sm:p-6">
        {reportTag ? (
          <ReportView
            tag={reportTag}
            onBack={() => setReportTag(null)}
            onSaveToHistory={handleSaveToHistory}
          />
        ) : activeTab === 'reader' ? (
          <ReaderView
            nfcState={nfcState}
            setNfcState={setNfcState}
            currentTag={currentTag}
            onScanAnother={handleScanAnother}
            onOpenReport={tag => setReportTag(tag)}
            onSimulateTap={handleSimulateTap}
            sampleTags={SAMPLE_TAGS}
          />
        ) : activeTab === 'history' ? (
          <HistoryView
            history={history}
            onOpenReport={tag => setReportTag(tag)}
            onDeleteScan={handleDeleteScan}
            onClearAll={handleClearAll}
            onSelectForCompare={handleSelectForCompare}
          />
        ) : activeTab === 'compare' ? (
          <CompareView
            compareTag1={compareTag1}
            compareTag2={compareTag2}
            history={history}
            onSelectForCompare={(slot, tag) => {
              if (slot === 1) setCompareTag1(tag);
              else setCompareTag2(tag);
            }}
          />
        ) : activeTab === 'about' ? (
          <AboutView />
        ) : (
          <ProjectCodeViewer />
        )}
      </main>

      {/* Material 3 Bottom Navigation Bar */}
      {!reportTag && (
        <nav className="sticky bottom-0 z-40 bg-neutral-900/95 backdrop-blur-md border-t border-neutral-800">
          <div className="max-w-md mx-auto grid grid-cols-4 h-16 px-2">
            <button
              onClick={() => setActiveTab('reader')}
              className={`flex flex-col items-center justify-center gap-1 transition ${
                activeTab === 'reader' ? 'text-blue-400 font-semibold' : 'text-neutral-400 hover:text-neutral-200'
              }`}
            >
              <Nfc className="w-5 h-5" />
              <span className="text-[11px]">Leitor</span>
            </button>

            <button
              onClick={() => setActiveTab('history')}
              className={`flex flex-col items-center justify-center gap-1 transition relative ${
                activeTab === 'history' ? 'text-blue-400 font-semibold' : 'text-neutral-400 hover:text-neutral-200'
              }`}
            >
              <HistoryIcon className="w-5 h-5" />
              <span className="text-[11px]">Histórico</span>
              {history.length > 0 && (
                <span className="absolute top-2 right-6 w-4 h-4 rounded-full bg-blue-600 text-white text-[9px] flex items-center justify-center font-bold">
                  {history.length}
                </span>
              )}
            </button>

            <button
              onClick={() => setActiveTab('compare')}
              className={`flex flex-col items-center justify-center gap-1 transition ${
                activeTab === 'compare' ? 'text-blue-400 font-semibold' : 'text-neutral-400 hover:text-neutral-200'
              }`}
            >
              <ArrowRightLeft className="w-5 h-5" />
              <span className="text-[11px]">Comparar</span>
            </button>

            <button
              onClick={() => setActiveTab('about')}
              className={`flex flex-col items-center justify-center gap-1 transition ${
                activeTab === 'about' ? 'text-blue-400 font-semibold' : 'text-neutral-400 hover:text-neutral-200'
              }`}
            >
              <Info className="w-5 h-5" />
              <span className="text-[11px]">Sobre</span>
            </button>
          </div>
        </nav>
      )}
    </div>
  );
}
