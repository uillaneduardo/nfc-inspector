import React, { useState } from 'react';
import { TagRecord } from '../types';
import { generateReport } from '../data/sampleTags';
import {
  ArrowLeft,
  Copy,
  Share2,
  BookmarkCheck,
  Check,
  FileCode,
} from 'lucide-react';

interface ReportViewProps {
  tag: TagRecord;
  onBack: () => void;
  onSaveToHistory: (tag: TagRecord) => void;
}

export const ReportView: React.FC<ReportViewProps> = ({
  tag,
  onBack,
  onSaveToHistory,
}) => {
  const [copied, setCopied] = useState(false);
  const [saved, setSaved] = useState(false);
  const reportText = tag.fullReport || generateReport(tag);

  const handleCopy = () => {
    navigator.clipboard.writeText(reportText);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: 'Relatório Técnico NFC - NFC Inspector',
          text: reportText,
        });
      } catch (_) {}
    } else {
      handleCopy();
    }
  };

  const handleSave = () => {
    onSaveToHistory(tag);
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  return (
    <div className="space-y-4 pb-12">
      {/* Top Bar */}
      <div className="flex items-center justify-between">
        <button
          onClick={onBack}
          className="inline-flex items-center gap-1.5 text-xs text-neutral-400 hover:text-white transition"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Voltar ao Leitor</span>
        </button>
        <span className="text-xs font-semibold text-neutral-400 uppercase tracking-wider">
          Relatório Completo
        </span>
      </div>

      {/* Action Buttons */}
      <div className="grid grid-cols-3 gap-2">
        <button
          onClick={handleCopy}
          className="flex items-center justify-center gap-1.5 bg-blue-600 hover:bg-blue-500 text-white py-2.5 px-3 rounded-xl text-xs font-semibold transition"
        >
          {copied ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
          <span>{copied ? 'Copiado!' : 'Copiar relatório'}</span>
        </button>

        <button
          onClick={handleShare}
          className="flex items-center justify-center gap-1.5 bg-neutral-800 hover:bg-neutral-700 text-neutral-200 py-2.5 px-3 rounded-xl text-xs font-semibold border border-neutral-700 transition"
        >
          <Share2 className="w-3.5 h-3.5" />
          <span>Compartilhar</span>
        </button>

        <button
          onClick={handleSave}
          className="flex items-center justify-center gap-1.5 bg-neutral-800 hover:bg-neutral-700 text-neutral-200 py-2.5 px-3 rounded-xl text-xs font-semibold border border-neutral-700 transition"
        >
          {saved ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <BookmarkCheck className="w-3.5 h-3.5" />}
          <span>{saved ? 'Salvo!' : 'Salvar leitura'}</span>
        </button>
      </div>

      {/* Formatted Monospace Report Box */}
      <div className="rounded-2xl bg-neutral-950 border border-neutral-800 p-4 sm:p-5">
        <pre className="font-mono text-xs text-neutral-300 whitespace-pre-wrap leading-relaxed select-all">
          {reportText}
        </pre>
      </div>
    </div>
  );
};
