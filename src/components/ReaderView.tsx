import React, { useState } from 'react';
import {
  Nfc,
  CheckCircle2,
  AlertTriangle,
  XCircle,
  Copy,
  Check,
  ChevronDown,
  ChevronUp,
  FileText,
  Radio,
  Settings,
  ShieldAlert,
  Info,
  Sparkles,
} from 'lucide-react';
import { TagRecord, NfcStateEnum } from '../types';

interface ReaderViewProps {
  nfcState: NfcStateEnum;
  setNfcState: (st: NfcStateEnum) => void;
  currentTag: TagRecord | null;
  onScanAnother: () => void;
  onOpenReport: (tag: TagRecord) => void;
  onSimulateTap: (tag: TagRecord) => void;
  sampleTags: TagRecord[];
}

export const ReaderView: React.FC<ReaderViewProps> = ({
  nfcState,
  setNfcState,
  currentTag,
  onScanAnother,
  onOpenReport,
  onSimulateTap,
  sampleTags,
}) => {
  const [copiedField, setCopiedField] = useState<string | null>(null);
  const [openSections, setOpenSections] = useState<Record<string, boolean>>({
    nfcA: true,
    isoDep: true,
    ndef: true,
    mifare: true,
  });

  const toggleSection = (sec: string) => {
    setOpenSections(prev => ({ ...prev, [sec]: !prev[sec] }));
  };

  const handleCopy = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    setCopiedField(label);
    setTimeout(() => setCopiedField(null), 2000);
  };

  return (
    <div className="space-y-4 pb-12">
      {/* State A: NFC Unsupported */}
      {nfcState === 'unsupported' && (
        <div id="unsupported-card" className="rounded-2xl bg-red-950/20 border border-red-800/40 p-5 text-neutral-200">
          <div className="flex items-center gap-2.5 text-red-400 font-semibold text-base mb-2">
            <XCircle className="w-5 h-5" />
            <span>✕ NFC não suportado</span>
          </div>
          <h3 className="font-bold text-lg text-white mb-1">NFC não disponível</h3>
          <p className="text-sm text-neutral-300 leading-relaxed mb-3">
            Este dispositivo não possui suporte a NFC ou o Android não disponibiliza um adaptador NFC compatível.
          </p>
          <div className="text-xs text-neutral-400 bg-neutral-900/60 p-3 rounded-lg border border-neutral-800">
            Você ainda pode consultar o histórico de leituras anteriores e navegar pelas explicações técnicas na aba Sobre.
          </div>
        </div>
      )}

      {/* State B: NFC Disabled */}
      {nfcState === 'disabled' && (
        <div id="disabled-card" className="rounded-2xl bg-amber-950/20 border border-amber-800/40 p-5 text-neutral-200">
          <div className="flex items-center gap-2.5 text-amber-400 font-semibold text-base mb-2">
            <AlertTriangle className="w-5 h-5" />
            <span>⚠ NFC desativado</span>
          </div>
          <h3 className="font-bold text-lg text-white mb-1">NFC desativado</h3>
          <p className="text-sm text-neutral-300 leading-relaxed mb-4">
            Para identificar cartões e tags, é necessário ativar o NFC do aparelho.
          </p>
          <button
            id="btn-enable-nfc"
            onClick={() => setNfcState('ready')}
            className="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-500 text-white font-medium text-sm px-5 py-2.5 rounded-xl transition shadow-sm"
          >
            <Settings className="w-4 h-4" />
            <span>Ativar NFC</span>
          </button>
        </div>
      )}

      {/* State C: Ready / Scanning */}
      {nfcState === 'ready' && !currentTag && (
        <div id="ready-scanning-card" className="rounded-2xl bg-neutral-900 border border-neutral-800 p-8 text-center">
          <div className="inline-flex items-center gap-2 bg-emerald-950/60 border border-emerald-800/50 text-emerald-400 px-3 py-1 rounded-full text-xs font-semibold mb-6">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
            <span>✓ NFC ativo</span>
          </div>

          <div className="relative mx-auto w-24 h-24 mb-5 flex items-center justify-center">
            <div className="absolute inset-0 rounded-full border border-blue-500/20 animate-ping"></div>
            <div className="absolute inset-2 rounded-full border border-blue-500/40"></div>
            <div className="w-16 h-16 rounded-full bg-blue-600/10 border border-blue-500/50 flex items-center justify-center text-blue-400 shadow-[0_0_20px_rgba(37,99,235,0.2)]">
              <Nfc className="w-8 h-8" />
            </div>
          </div>

          <h3 className="text-lg font-bold text-white mb-1">Aguardando cartão ou tag NFC...</h3>
          <p className="text-sm text-neutral-400 max-w-xs mx-auto mb-6">
            Aproxime a tag da antena de RF na traseira do smartphone (Moto G50 5G) para iniciar a leitura.
          </p>

          <div className="pt-4 border-t border-neutral-800/80">
            <span className="text-xs text-neutral-400 font-medium block mb-3 uppercase tracking-wider">
              Simular Toque de Tag (Teste Rápido)
            </span>
            <div className="flex flex-wrap gap-2 justify-center">
              {sampleTags.map(tag => (
                <button
                  key={tag.id}
                  onClick={() => onSimulateTap(tag)}
                  className="text-xs px-3 py-1.5 rounded-lg bg-neutral-800 hover:bg-neutral-700 text-neutral-200 border border-neutral-700/60 transition flex items-center gap-1.5"
                >
                  <Sparkles className="w-3 h-3 text-blue-400" />
                  {tag.mainTechnology.split('(')[0].trim()}
                </button>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* State Tag Detected / Active Inspection */}
      {currentTag && (
        <div id="detected-tag-container" className="space-y-4">
          {/* Header Banner */}
          <div className="flex items-center justify-between bg-emerald-950/40 border border-emerald-800/60 px-4 py-3 rounded-xl">
            <div className="flex items-center gap-2 text-emerald-400 text-sm font-semibold">
              <CheckCircle2 className="w-4 h-4" />
              <span>Cartão detectado com sucesso</span>
            </div>
            <button
              onClick={onScanAnother}
              className="text-xs font-semibold text-blue-400 hover:text-blue-300 px-2 py-1 rounded transition"
            >
              Ler Outro
            </button>
          </div>

          {/* Main ID Card */}
          <div className="rounded-2xl bg-neutral-900 border border-neutral-800 p-5 shadow-sm">
            <div className="flex items-center justify-between text-xs text-neutral-400 mb-2">
              <span className="font-semibold uppercase tracking-wider">Cartão detectado</span>
              <span>{currentTag.formattedDateTime}</span>
            </div>

            <div className="mb-4">
              <span className="text-xs font-semibold text-neutral-500 uppercase block mb-1">Tecnologia Principal</span>
              <div className="text-base font-bold text-blue-400">{currentTag.mainTechnology}</div>
            </div>

            {/* UID Highlight */}
            <div className="bg-neutral-950 rounded-xl p-3.5 border border-neutral-800/80 mb-3">
              <div className="flex items-center justify-between mb-1">
                <span className="text-xs text-neutral-400 font-medium">UID (Identificador Único)</span>
                <button
                  onClick={() => handleCopy(currentTag.uidColonHex, 'uid')}
                  className="inline-flex items-center gap-1 text-xs text-blue-400 hover:text-blue-300 bg-blue-950/40 px-2 py-0.5 rounded border border-blue-900/60"
                >
                  {copiedField === 'uid' ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
                  <span>{copiedField === 'uid' ? 'Copiado' : 'Copiar UID'}</span>
                </button>
              </div>
              <div className="font-mono text-lg sm:text-xl font-bold text-neutral-100 tracking-wide">
                {currentTag.uidColonHex}
              </div>
            </div>

            {/* Formatted Identifiers Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs text-neutral-300 mb-4 bg-neutral-950/50 p-3 rounded-lg">
              <div className="flex justify-between sm:block">
                <span className="text-neutral-500 block">Hex contínuo:</span>
                <span className="font-mono font-medium">{currentTag.uidContinuousHex}</span>
              </div>
              <div className="flex justify-between sm:block">
                <span className="text-neutral-500 block">Decimal:</span>
                <span className="font-mono font-medium">{currentTag.uidDecimal}</span>
              </div>
              <div className="flex justify-between sm:block">
                <span className="text-neutral-500 block">Comprimento:</span>
                <span>{currentTag.uidLengthBytes} bytes ({currentTag.uidLengthBytes * 8} bits)</span>
              </div>
            </div>

            {/* UID Security Notice */}
            <div className="flex items-start gap-2 bg-neutral-950/80 p-2.5 rounded-lg border border-neutral-800 text-xs text-neutral-400">
              <Info className="w-4 h-4 text-blue-400 shrink-0 mt-0.5" />
              <span>{currentTag.scanNotes}</span>
            </div>
          </div>

          {/* Detected Tech List Badges */}
          <div className="rounded-xl bg-neutral-900 border border-neutral-800 p-4">
            <h4 className="text-xs font-bold uppercase tracking-wider text-neutral-400 mb-2.5">
              Tecnologias Detectadas (Tag.getTechList())
            </h4>
            <div className="flex flex-wrap gap-2">
              {currentTag.technologies.map(tech => (
                <div
                  key={tech}
                  className="inline-flex items-center gap-1.5 bg-neutral-800 border border-neutral-700/80 text-emerald-400 text-xs px-3 py-1.5 rounded-lg font-medium"
                >
                  <CheckCircle2 className="w-3.5 h-3.5" />
                  <span>✓ {tech}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Expandable Technical Deep-Dives */}

          {/* 1. NFC-A */}
          {currentTag.nfcA && (
            <div className="rounded-xl bg-neutral-900 border border-neutral-800 overflow-hidden">
              <button
                onClick={() => toggleSection('nfcA')}
                className="w-full flex items-center justify-between p-4 text-left font-semibold text-sm text-neutral-200 hover:bg-neutral-800/50 transition"
              >
                <div className="flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-blue-500"></span>
                  <span>ISO 14443-3A (NfcA)</span>
                </div>
                {openSections.nfcA ? <ChevronUp className="w-4 h-4 text-neutral-400" /> : <ChevronDown className="w-4 h-4 text-neutral-400" />}
              </button>
              {openSections.nfcA && (
                <div className="p-4 pt-0 text-xs space-y-2 border-t border-neutral-800/80">
                  <div className="flex justify-between py-1 border-b border-neutral-800/50">
                    <span className="text-neutral-400">ATQA (Answer To Request A):</span>
                    <span className="font-mono font-bold text-neutral-200">{currentTag.nfcA.atqaHex}</span>
                  </div>
                  <div className="flex justify-between py-1 border-b border-neutral-800/50">
                    <span className="text-neutral-400">SAK (Select Acknowledge):</span>
                    <span className="font-mono font-bold text-neutral-200">{currentTag.nfcA.sakHex}</span>
                  </div>
                  <div className="flex justify-between py-1 border-b border-neutral-800/50">
                    <span className="text-neutral-400">Timeout:</span>
                    <span className="font-mono text-neutral-200">{currentTag.nfcA.timeoutMs} ms</span>
                  </div>
                  <div className="flex justify-between py-1">
                    <span className="text-neutral-400">Tamanho Máx. Transceive:</span>
                    <span className="font-mono text-neutral-200">{currentTag.nfcA.maxTransceiveBytes} bytes</span>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* 2. ISO-DEP */}
          {currentTag.isoDep && (
            <div className="rounded-xl bg-neutral-900 border border-neutral-800 overflow-hidden">
              <button
                onClick={() => toggleSection('isoDep')}
                className="w-full flex items-center justify-between p-4 text-left font-semibold text-sm text-neutral-200 hover:bg-neutral-800/50 transition"
              >
                <div className="flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-indigo-500"></span>
                  <span>ISO 14443-4 (IsoDep / Smart Card)</span>
                </div>
                {openSections.isoDep ? <ChevronUp className="w-4 h-4 text-neutral-400" /> : <ChevronDown className="w-4 h-4 text-neutral-400" />}
              </button>
              {openSections.isoDep && (
                <div className="p-4 pt-0 text-xs space-y-2 border-t border-neutral-800/80">
                  <div className="flex justify-between py-1 border-b border-neutral-800/50">
                    <span className="text-neutral-400">Historical Bytes:</span>
                    <span className="font-mono font-bold text-neutral-200">{currentTag.isoDep.historicalBytesHex || 'N/A'}</span>
                  </div>
                  <div className="flex justify-between py-1 border-b border-neutral-800/50">
                    <span className="text-neutral-400">HiLayer Response:</span>
                    <span className="font-mono text-neutral-200">{currentTag.isoDep.hiLayerResponseHex || 'N/A'}</span>
                  </div>
                  <div className="flex justify-between py-1 border-b border-neutral-800/50">
                    <span className="text-neutral-400">Extended Length APDU:</span>
                    <span className="text-neutral-200 font-medium">
                      {currentTag.isoDep.isExtendedLengthApduSupported ? '✓ Suportado' : 'Não suportado'}
                    </span>
                  </div>
                  <div className="flex justify-between py-1 border-b border-neutral-800/50">
                    <span className="text-neutral-400">Timeout:</span>
                    <span className="font-mono text-neutral-200">{currentTag.isoDep.timeoutMs} ms</span>
                  </div>
                  <div className="flex justify-between py-1">
                    <span className="text-neutral-400">Tamanho Máx. Transceive:</span>
                    <span className="font-mono text-neutral-200">{currentTag.isoDep.maxTransceiveBytes} bytes</span>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* 3. NDEF */}
          {currentTag.ndef && (
            <div className="rounded-xl bg-neutral-900 border border-neutral-800 overflow-hidden">
              <button
                onClick={() => toggleSection('ndef')}
                className="w-full flex items-center justify-between p-4 text-left font-semibold text-sm text-neutral-200 hover:bg-neutral-800/50 transition"
              >
                <div className="flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-cyan-500"></span>
                  <span>NDEF (NFC Forum Data)</span>
                </div>
                {openSections.ndef ? <ChevronUp className="w-4 h-4 text-neutral-400" /> : <ChevronDown className="w-4 h-4 text-neutral-400" />}
              </button>
              {openSections.ndef && (
                <div className="p-4 pt-0 text-xs space-y-3 border-t border-neutral-800/80">
                  <div className="grid grid-cols-2 gap-2 bg-neutral-950/60 p-2.5 rounded-lg">
                    <div>
                      <span className="text-neutral-500 block">Tipo:</span>
                      <span className="font-medium text-neutral-200">{currentTag.ndef.typeName}</span>
                    </div>
                    <div>
                      <span className="text-neutral-500 block">Gravável:</span>
                      <span className="font-medium text-neutral-200">{currentTag.ndef.isWritable ? 'Sim' : 'Não'}</span>
                    </div>
                    <div>
                      <span className="text-neutral-500 block">Tamanho Atual:</span>
                      <span className="font-mono text-neutral-200">{currentTag.ndef.currentSizeBytes} bytes</span>
                    </div>
                    <div>
                      <span className="text-neutral-500 block">Capacidade Máxima:</span>
                      <span className="font-mono text-neutral-200">{currentTag.ndef.maxSizeBytes} bytes</span>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <span className="text-neutral-400 font-bold uppercase tracking-wider text-[11px] block">
                      Registros NDEF ({currentTag.ndef.records.length})
                    </span>
                    {currentTag.ndef.records.map((rec, i) => (
                      <div key={rec.id} className="bg-neutral-950 p-3 rounded-lg border border-neutral-800/80 space-y-1.5">
                        <div className="flex justify-between text-neutral-400">
                          <span className="font-semibold text-blue-400">Registro #{i + 1} ({rec.tnfName.split(' ')[0]})</span>
                          <span className="font-mono text-[11px]">Tipo: {rec.typeString}</span>
                        </div>
                        {rec.isText && (
                          <div className="text-neutral-200">
                            <span className="text-neutral-500 block text-[11px]">Texto ({rec.textLanguage}):</span>
                            <div className="bg-neutral-900 p-2 rounded text-neutral-100 font-mono text-xs">{rec.textContent}</div>
                          </div>
                        )}
                        {rec.isUri && (
                          <div className="text-neutral-200">
                            <span className="text-neutral-500 block text-[11px]">URI Decodificada:</span>
                            <div className="bg-neutral-900 p-2 rounded text-blue-400 font-mono text-xs break-all">{rec.uriContent}</div>
                          </div>
                        )}
                        {rec.isMime && (
                          <div className="text-neutral-200">
                            <span className="text-neutral-500 block text-[11px]">MIME ({rec.mimeType}):</span>
                            <div className="bg-neutral-900 p-2 rounded font-mono text-xs break-all">{rec.rawPayloadHex}</div>
                          </div>
                        )}
                        <div className="text-[10px] text-neutral-500 font-mono pt-1">
                          Payload Hex: {rec.rawPayloadHex}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* 4. MIFARE Classic */}
          {currentTag.mifareClassic && (
            <div className="rounded-xl bg-neutral-900 border border-neutral-800 overflow-hidden">
              <button
                onClick={() => toggleSection('mifare')}
                className="w-full flex items-center justify-between p-4 text-left font-semibold text-sm text-neutral-200 hover:bg-neutral-800/50 transition"
              >
                <div className="flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-orange-500"></span>
                  <span>MIFARE Classic</span>
                </div>
                {openSections.mifare ? <ChevronUp className="w-4 h-4 text-neutral-400" /> : <ChevronDown className="w-4 h-4 text-neutral-400" />}
              </button>
              {openSections.mifare && (
                <div className="p-4 pt-0 text-xs space-y-2 border-t border-neutral-800/80">
                  <div className="flex justify-between py-1 border-b border-neutral-800/50">
                    <span className="text-neutral-400">Tipo Detectado:</span>
                    <span className="font-medium text-neutral-200">{currentTag.mifareClassic.typeName}</span>
                  </div>
                  <div className="flex justify-between py-1 border-b border-neutral-800/50">
                    <span className="text-neutral-400">Tamanho da Memória:</span>
                    <span className="font-mono text-neutral-200">{currentTag.mifareClassic.sizeBytes} bytes</span>
                  </div>
                  <div className="flex justify-between py-1 border-b border-neutral-800/50">
                    <span className="text-neutral-400">Setores:</span>
                    <span className="font-mono text-neutral-200">{currentTag.mifareClassic.sectorCount}</span>
                  </div>
                  <div className="flex justify-between py-1 border-b border-neutral-800/50">
                    <span className="text-neutral-400">Blocos:</span>
                    <span className="font-mono text-neutral-200">{currentTag.mifareClassic.blockCount}</span>
                  </div>
                  <div className="p-2.5 bg-neutral-950 rounded text-neutral-400 text-[11px] leading-relaxed">
                    ℹ {currentTag.mifareClassic.note}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* 5. MIFARE Ultralight */}
          {currentTag.mifareUltralight && (
            <div className="rounded-xl bg-neutral-900 border border-neutral-800 p-4 text-xs space-y-2">
              <h4 className="font-semibold text-sm text-neutral-200 flex items-center gap-2 mb-2">
                <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
                <span>MIFARE Ultralight / NTAG</span>
              </h4>
              <div className="flex justify-between py-1 border-b border-neutral-800/50">
                <span className="text-neutral-400">Tipo:</span>
                <span className="font-medium text-neutral-200">{currentTag.mifareUltralight.typeName}</span>
              </div>
              <div className="flex justify-between py-1">
                <span className="text-neutral-400">Tamanho Máx. Transceive:</span>
                <span className="font-mono text-neutral-200">{currentTag.mifareUltralight.maxTransceiveBytes} bytes</span>
              </div>
            </div>
          )}

          {/* 6. NFC-F (Sony FeliCa) */}
          {currentTag.nfcF && (
            <div className="rounded-xl bg-neutral-900 border border-neutral-800 p-4 text-xs space-y-2">
              <h4 className="font-semibold text-sm text-neutral-200 flex items-center gap-2 mb-2">
                <span className="w-2 h-2 rounded-full bg-purple-500"></span>
                <span>JIS 6319-4 (Sony FeliCa / NFC-F)</span>
              </h4>
              <div className="flex justify-between py-1 border-b border-neutral-800/50">
                <span className="text-neutral-400">System Code:</span>
                <span className="font-mono font-bold text-neutral-200">{currentTag.nfcF.systemCodeHex || 'N/A'}</span>
              </div>
              <div className="flex justify-between py-1">
                <span className="text-neutral-400">Manufacturer Response:</span>
                <span className="font-mono text-neutral-200">{currentTag.nfcF.manufacturerResponseHex || 'N/A'}</span>
              </div>
            </div>
          )}

          {/* 7. NFC-V (ISO 15693) */}
          {currentTag.nfcV && (
            <div className="rounded-xl bg-neutral-900 border border-neutral-800 p-4 text-xs space-y-2">
              <h4 className="font-semibold text-sm text-neutral-200 flex items-center gap-2 mb-2">
                <span className="w-2 h-2 rounded-full bg-teal-500"></span>
                <span>ISO 15693 (Vicinity / NFC-V)</span>
              </h4>
              <div className="flex justify-between py-1 border-b border-neutral-800/50">
                <span className="text-neutral-400">DSFID:</span>
                <span className="font-mono font-bold text-neutral-200">{currentTag.nfcV.dsfidHex || 'N/A'}</span>
              </div>
              <div className="flex justify-between py-1">
                <span className="text-neutral-400">Response Flags:</span>
                <span className="font-mono text-neutral-200">{currentTag.nfcV.responseFlagsHex || 'N/A'}</span>
              </div>
            </div>
          )}

          {/* Bottom Action: View Full Report */}
          <button
            onClick={() => onOpenReport(currentTag)}
            className="w-full flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-500 text-white font-semibold text-sm py-3.5 px-4 rounded-xl transition shadow-sm"
          >
            <FileText className="w-4 h-4" />
            <span>Ver Relatório Técnico Completo</span>
          </button>
        </div>
      )}
    </div>
  );
};
