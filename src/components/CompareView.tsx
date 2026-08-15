import React, { useState } from 'react';
import { TagRecord } from '../types';
import { ArrowRightLeft, CheckCircle2, AlertCircle, Sparkles } from 'lucide-react';

interface CompareViewProps {
  compareTag1: TagRecord | null;
  compareTag2: TagRecord | null;
  history: TagRecord[];
  onSelectForCompare: (slot: 1 | 2, tag: TagRecord) => void;
}

export const CompareView: React.FC<CompareViewProps> = ({
  compareTag1,
  compareTag2,
  history,
  onSelectForCompare,
}) => {
  const [modalSlot, setModalSlot] = useState<1 | 2 | null>(null);

  interface DiffRow {
    label: string;
    valA: string;
    valB: string;
  }

  const renderDiffSection = (title: string, rows: DiffRow[]) => {
    return (
      <div className="rounded-xl bg-neutral-900 border border-neutral-800 p-4 space-y-2">
        <h4 className="text-xs font-bold text-blue-400 uppercase tracking-wider mb-2">{title}</h4>
        <div className="space-y-1.5 text-xs">
          {rows.map((r, idx) => {
            const isMatch = r.valA.trim() === r.valB.trim();
            return (
              <div
                key={idx}
                className="grid grid-cols-12 gap-2 py-1.5 border-b border-neutral-800/60 last:border-0 items-center"
              >
                <div className="col-span-4 text-neutral-400 font-medium text-[11px]">{r.label}</div>
                <div
                  className={`col-span-4 font-mono text-[11px] break-all ${
                    isMatch ? 'text-neutral-300' : 'text-amber-400 font-semibold'
                  }`}
                >
                  {r.valA}
                </div>
                <div
                  className={`col-span-4 font-mono text-[11px] break-all ${
                    isMatch ? 'text-neutral-300' : 'text-emerald-400 font-semibold'
                  }`}
                >
                  {r.valB}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  return (
    <div className="space-y-4 pb-12">
      <div>
        <h2 className="text-xl font-bold text-white">Comparar Leituras NFC</h2>
        <p className="text-xs text-neutral-400">
          Selecione duas leituras para identificar diferenças estruturais e parâmetros de RF.
        </p>
      </div>

      {/* Selectors */}
      <div className="grid grid-cols-2 gap-3">
        {/* Slot A */}
        <div
          onClick={() => setModalSlot(1)}
          className={`rounded-xl border p-3.5 cursor-pointer transition ${
            compareTag1
              ? 'bg-neutral-900 border-blue-500/50 hover:border-blue-400'
              : 'bg-neutral-900/50 border-dashed border-neutral-700 hover:border-neutral-500'
          }`}
        >
          <div className="flex items-center justify-between text-xs font-bold text-blue-400 mb-1">
            <span>Leitura A</span>
            <span className="text-[10px] text-neutral-500 underline">Alterar</span>
          </div>
          {compareTag1 ? (
            <div>
              <div className="font-mono text-xs font-bold text-neutral-200 truncate">
                {compareTag1.uidColonHex}
              </div>
              <div className="text-[11px] text-neutral-400 truncate mt-0.5">
                {compareTag1.mainTechnology}
              </div>
            </div>
          ) : (
            <div className="text-xs text-neutral-500 py-1">Toque para escolher</div>
          )}
        </div>

        {/* Slot B */}
        <div
          onClick={() => setModalSlot(2)}
          className={`rounded-xl border p-3.5 cursor-pointer transition ${
            compareTag2
              ? 'bg-neutral-900 border-emerald-500/50 hover:border-emerald-400'
              : 'bg-neutral-900/50 border-dashed border-neutral-700 hover:border-neutral-500'
          }`}
        >
          <div className="flex items-center justify-between text-xs font-bold text-emerald-400 mb-1">
            <span>Leitura B</span>
            <span className="text-[10px] text-neutral-500 underline">Alterar</span>
          </div>
          {compareTag2 ? (
            <div>
              <div className="font-mono text-xs font-bold text-neutral-200 truncate">
                {compareTag2.uidColonHex}
              </div>
              <div className="text-[11px] text-neutral-400 truncate mt-0.5">
                {compareTag2.mainTechnology}
              </div>
            </div>
          ) : (
            <div className="text-xs text-neutral-500 py-1">Toque para escolher</div>
          )}
        </div>
      </div>

      {/* Comparison Results */}
      {compareTag1 && compareTag2 ? (
        <div className="space-y-3 pt-2">
          <div className="flex items-center justify-between px-2 text-xs text-neutral-400">
            <span className="font-bold uppercase tracking-wider text-[10px]">Tabela de Diferenças</span>
            <div className="flex items-center gap-3 text-[10px]">
              <span className="flex items-center gap-1 text-neutral-400">
                <span className="w-2 h-2 rounded-full bg-neutral-500"></span> Igual
              </span>
              <span className="flex items-center gap-1 text-amber-400">
                <span className="w-2 h-2 rounded-full bg-amber-400"></span> Diferente
              </span>
            </div>
          </div>

          {/* 1. Identification Diff */}
          {renderDiffSection('1. Identificação (UID)', [
            { label: 'UID Hex', valA: compareTag1.uidColonHex, valB: compareTag2.uidColonHex },
            {
              label: 'Comprimento',
              valA: `${compareTag1.uidLengthBytes} bytes`,
              valB: `${compareTag2.uidLengthBytes} bytes`,
            },
            { label: 'Decimal', valA: compareTag1.uidDecimal, valB: compareTag2.uidDecimal },
          ])}

          {/* 2. Technologies Diff */}
          {renderDiffSection('2. Tecnologias', [
            {
              label: 'Principal',
              valA: compareTag1.mainTechnology,
              valB: compareTag2.mainTechnology,
            },
            {
              label: 'Tech List',
              valA: compareTag1.technologies.join(', '),
              valB: compareTag2.technologies.join(', '),
            },
          ])}

          {/* 3. NFC-A Diff */}
          {(compareTag1.nfcA || compareTag2.nfcA) &&
            renderDiffSection('3. Parâmetros NFC-A', [
              {
                label: 'ATQA',
                valA: compareTag1.nfcA?.atqaHex || 'N/A',
                valB: compareTag2.nfcA?.atqaHex || 'N/A',
              },
              {
                label: 'SAK',
                valA: compareTag1.nfcA?.sakHex || 'N/A',
                valB: compareTag2.nfcA?.sakHex || 'N/A',
              },
              {
                label: 'Max Transceive',
                valA: compareTag1.nfcA ? `${compareTag1.nfcA.maxTransceiveBytes} B` : 'N/A',
                valB: compareTag2.nfcA ? `${compareTag2.nfcA.maxTransceiveBytes} B` : 'N/A',
              },
            ])}

          {/* 4. ISO-DEP Diff */}
          {(compareTag1.isoDep || compareTag2.isoDep) &&
            renderDiffSection('4. Parâmetros ISO-DEP', [
              {
                label: 'Historical Bytes',
                valA: compareTag1.isoDep?.historicalBytesHex || 'N/A',
                valB: compareTag2.isoDep?.historicalBytesHex || 'N/A',
              },
              {
                label: 'Extended APDU',
                valA: compareTag1.isoDep ? (compareTag1.isoDep.isExtendedLengthApduSupported ? 'Sim' : 'Não') : 'N/A',
                valB: compareTag2.isoDep ? (compareTag2.isoDep.isExtendedLengthApduSupported ? 'Sim' : 'Não') : 'N/A',
              },
            ])}

          {/* 5. NDEF Diff */}
          {(compareTag1.ndef || compareTag2.ndef) &&
            renderDiffSection('5. Parâmetros NDEF', [
              {
                label: 'Formato',
                valA: compareTag1.ndef?.typeName || 'N/A',
                valB: compareTag2.ndef?.typeName || 'N/A',
              },
              {
                label: 'Gravável',
                valA: compareTag1.ndef ? (compareTag1.ndef.isWritable ? 'Sim' : 'Não') : 'N/A',
                valB: compareTag2.ndef ? (compareTag2.ndef.isWritable ? 'Sim' : 'Não') : 'N/A',
              },
              {
                label: 'Tamanho Atual',
                valA: compareTag1.ndef ? `${compareTag1.ndef.currentSizeBytes} B` : 'N/A',
                valB: compareTag2.ndef ? `${compareTag2.ndef.currentSizeBytes} B` : 'N/A',
              },
              {
                label: 'Capacidade Máx',
                valA: compareTag1.ndef ? `${compareTag1.ndef.maxSizeBytes} B` : 'N/A',
                valB: compareTag2.ndef ? `${compareTag2.ndef.maxSizeBytes} B` : 'N/A',
              },
              {
                label: 'Qtd Registros',
                valA: compareTag1.ndef ? `${compareTag1.ndef.recordCount}` : 'N/A',
                valB: compareTag2.ndef ? `${compareTag2.ndef.recordCount}` : 'N/A',
              },
            ])}
        </div>
      ) : (
        <div className="rounded-2xl bg-neutral-900 border border-neutral-800 p-8 text-center my-4">
          <ArrowRightLeft className="w-10 h-10 text-neutral-600 mx-auto mb-3" />
          <h3 className="font-semibold text-neutral-300 text-sm mb-1">Selecione duas leituras acima</h3>
          <p className="text-xs text-neutral-500 max-w-xs mx-auto">
            Escolha as tags A e B do seu histórico para gerar a comparação completa lado a lado.
          </p>
        </div>
      )}

      {/* Modal Picker */}
      {modalSlot !== null && (
        <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/70 backdrop-blur-xs p-4">
          <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-5 max-w-md w-full max-h-[80vh] flex flex-col space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-sm text-white">
                Selecionar para Leitura {modalSlot === 1 ? 'A' : 'B'}
              </h3>
              <button
                onClick={() => setModalSlot(null)}
                className="text-xs text-neutral-400 hover:text-white"
              >
                Fechar
              </button>
            </div>

            <div className="overflow-y-auto space-y-2 pr-1">
              {history.length === 0 ? (
                <div className="text-xs text-neutral-500 py-6 text-center">
                  Nenhuma leitura disponível no histórico.
                </div>
              ) : (
                history.map(scan => (
                  <div
                    key={scan.id}
                    onClick={() => {
                      onSelectForCompare(modalSlot!, scan);
                      setModalSlot(null);
                    }}
                    className="p-3 rounded-xl bg-neutral-950 border border-neutral-800 hover:border-blue-500/60 cursor-pointer transition text-xs space-y-1"
                  >
                    <div className="font-mono font-bold text-neutral-100 flex justify-between">
                      <span>{scan.uidColonHex}</span>
                      <span className="text-[10px] text-neutral-500 font-sans">{scan.formattedDateTime}</span>
                    </div>
                    <div className="text-blue-400 text-[11px]">{scan.mainTechnology}</div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
