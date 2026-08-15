import React, { useState } from 'react';
import { TagRecord } from '../types';
import {
  History,
  Trash2,
  FileText,
  Copy,
  Check,
  Search,
  ArrowRightLeft,
  Calendar,
} from 'lucide-react';

interface HistoryViewProps {
  history: TagRecord[];
  onOpenReport: (tag: TagRecord) => void;
  onDeleteScan: (id: string) => void;
  onClearAll: () => void;
  onSelectForCompare: (slot: 1 | 2, tag: TagRecord) => void;
}

export const HistoryView: React.FC<HistoryViewProps> = ({
  history,
  onOpenReport,
  onDeleteScan,
  onClearAll,
  onSelectForCompare,
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [copiedId, setCopiedId] = useState<string | null>(null);
  const [showConfirmClear, setShowConfirmClear] = useState(false);

  const filteredHistory = history.filter(
    h =>
      h.uidColonHex.toLowerCase().includes(searchTerm.toLowerCase()) ||
      h.mainTechnology.toLowerCase().includes(searchTerm.toLowerCase()) ||
      h.technologies.some(t => t.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  const handleCopy = (uid: string, id: string) => {
    navigator.clipboard.writeText(uid);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  return (
    <div className="space-y-4 pb-12">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-white">Histórico Local</h2>
          <p className="text-xs text-neutral-400">
            {history.length} leituras armazenadas 100% offline
          </p>
        </div>
        {history.length > 0 && (
          <button
            onClick={() => setShowConfirmClear(true)}
            className="p-2 text-red-400 hover:text-red-300 hover:bg-red-950/40 rounded-lg transition"
            title="Limpar Todo o Histórico"
          >
            <Trash2 className="w-4 h-4" />
          </button>
        )}
      </div>

      {/* Search Input */}
      {history.length > 0 && (
        <div className="relative">
          <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-neutral-500" />
          <input
            type="text"
            placeholder="Buscar por UID ou tecnologia..."
            value={searchTerm}
            onChange={e => setSearchTerm(e.target.value)}
            className="w-full pl-9 pr-4 py-2 text-xs bg-neutral-900 border border-neutral-800 rounded-xl text-neutral-200 placeholder-neutral-500 focus:outline-none focus:border-blue-500"
          />
        </div>
      )}

      {/* Empty State */}
      {history.length === 0 ? (
        <div className="rounded-2xl bg-neutral-900 border border-neutral-800 p-8 text-center my-8">
          <History className="w-12 h-12 text-neutral-600 mx-auto mb-3" />
          <h3 className="font-semibold text-neutral-300 mb-1">Nenhuma leitura salva ainda</h3>
          <p className="text-xs text-neutral-500 max-w-xs mx-auto">
            Aproxime um cartão ou tag na aba 'Leitor' para registrar automaticamente o diagnóstico.
          </p>
        </div>
      ) : filteredHistory.length === 0 ? (
        <div className="text-center py-8 text-neutral-500 text-xs">
          Nenhuma leitura encontrada para "{searchTerm}".
        </div>
      ) : (
        <div className="space-y-3">
          {filteredHistory.map(scan => (
            <div
              key={scan.id}
              className="rounded-xl bg-neutral-900 border border-neutral-800 p-4 hover:border-neutral-700/80 transition"
            >
              <div className="flex items-start justify-between mb-1.5">
                <div className="font-mono text-base font-bold text-neutral-100 flex items-center gap-2">
                  <span>{scan.uidColonHex}</span>
                  <button
                    onClick={() => handleCopy(scan.uidColonHex, scan.id)}
                    className="text-neutral-500 hover:text-neutral-300 p-1"
                    title="Copiar UID"
                  >
                    {copiedId === scan.id ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                  </button>
                </div>
                <div className="flex items-center gap-1 text-[11px] text-neutral-500">
                  <Calendar className="w-3 h-3" />
                  <span>{scan.formattedDateTime}</span>
                </div>
              </div>

              <div className="text-xs font-medium text-blue-400 mb-2">
                {scan.mainTechnology}
              </div>

              <div className="flex flex-wrap gap-1.5 mb-3">
                {scan.technologies.map(t => (
                  <span
                    key={t}
                    className="text-[10px] bg-neutral-800 text-neutral-300 px-2 py-0.5 rounded border border-neutral-700/50"
                  >
                    {t}
                  </span>
                ))}
              </div>

              <div className="flex items-center justify-between pt-2 border-t border-neutral-800/60 text-xs">
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => onSelectForCompare(1, scan)}
                    className="px-2 py-1 rounded bg-neutral-800 text-neutral-300 hover:bg-neutral-700 hover:text-white transition text-[11px] font-medium"
                  >
                    + Comp A
                  </button>
                  <button
                    onClick={() => onSelectForCompare(2, scan)}
                    className="px-2 py-1 rounded bg-neutral-800 text-neutral-300 hover:bg-neutral-700 hover:text-white transition text-[11px] font-medium"
                  >
                    + Comp B
                  </button>
                </div>

                <div className="flex items-center gap-1">
                  <button
                    onClick={() => onOpenReport(scan)}
                    className="flex items-center gap-1 px-2.5 py-1 rounded bg-blue-950/40 text-blue-400 border border-blue-900/60 hover:bg-blue-900/40 transition text-xs font-semibold"
                  >
                    <FileText className="w-3.5 h-3.5" />
                    <span>Relatório</span>
                  </button>
                  <button
                    onClick={() => onDeleteScan(scan.id)}
                    className="p-1 text-neutral-500 hover:text-red-400 transition"
                    title="Excluir leitura"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Confirmation Modal */}
      {showConfirmClear && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-xs p-4">
          <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-5 max-w-sm w-full space-y-4">
            <h3 className="font-bold text-base text-white">Excluir Todo o Histórico?</h3>
            <p className="text-xs text-neutral-400 leading-relaxed">
              Todas as leituras locais serão apagadas permanentemente do armazenamento deste dispositivo. Esta ação é irreversível.
            </p>
            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => setShowConfirmClear(false)}
                className="px-3.5 py-2 rounded-xl text-xs font-semibold text-neutral-300 hover:bg-neutral-800 transition"
              >
                Cancelar
              </button>
              <button
                onClick={() => {
                  onClearAll();
                  setShowConfirmClear(false);
                }}
                className="px-4 py-2 rounded-xl text-xs font-semibold bg-red-600 hover:bg-red-500 text-white transition"
              >
                Excluir Tudo
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
