import React, { useState } from 'react';
import {
  Radio,
  History,
  GitCompare,
  Settings,
  Smartphone,
  ShieldCheck,
  CheckCircle2,
  Copy,
  Check,
  Share2,
  BookmarkPlus,
  RefreshCw,
  Search,
  SlidersHorizontal,
  ChevronDown,
  ChevronUp,
  Cpu,
  Layers,
  FileText,
  AlertTriangle,
  Info,
  ExternalLink
} from 'lucide-react';

interface MockTag {
  id: string;
  name: string;
  uidColon: string;
  uidContinuous: string;
  uidDecimal: string;
  uidReversed: string;
  techMain: string;
  techList: string[];
  atqa: string;
  sak: string;
  standard: string;
  ndefMessage?: {
    recordType: string;
    payloadText: string;
    sizeBytes: number;
    isWritable: boolean;
  };
  mifareClassic?: {
    typeName: string;
    sizeBytes: number;
    sectorCount: number;
    blockCount: number;
    blockSizeBytes: number;
    authenticatedSectors: number;
    sectors: Array<{
      sectorIndex: number;
      blockCount: number;
      firstBlockIndex: number;
      status: 'Lido com Sucesso' | 'Autenticado Key A' | 'Falha de Autenticação';
      authKeyType?: string;
      authKeyHex?: string;
      blocks: Array<{
        blockIndex: number;
        type: 'Fabricante' | 'Dados' | 'Trailer de Setor';
        hex: string;
        ascii: string;
        isRead: boolean;
      }>;
      accessBits?: {
        rawHex: string;
        gpbHex: string;
        isValid: boolean;
        trailerPermissions: {
          keyAWrite: string;
          accessBitsRead: string;
          accessBitsWrite: string;
          keyBWrite: string;
        };
        dataPermissions: {
          read: string;
          write: string;
          inc: string;
          dec: string;
        };
      };
    }>;
  };
}

const SAMPLE_TAGS: MockTag[] = [
  {
    id: 'tag-mifare-1',
    name: 'Cartão de Transporte (MIFARE Classic 1K)',
    uidColon: '04:A2:8F:B1',
    uidContinuous: '04A28FB1',
    uidDecimal: '77762481',
    uidReversed: 'B18FA204',
    techMain: 'Tecnologia principal: MIFARE Classic',
    techList: ['NfcA', 'MifareClassic', 'NdefFormatable'],
    atqa: '00 04',
    sak: '08',
    standard: 'ISO 14443-3A (NXP MIFARE Classic 1K)',
    mifareClassic: {
      typeName: 'MIFARE Classic Standard',
      sizeBytes: 1024,
      sectorCount: 16,
      blockCount: 64,
      blockSizeBytes: 16,
      authenticatedSectors: 15,
      sectors: [
        {
          sectorIndex: 0,
          blockCount: 4,
          firstBlockIndex: 0,
          status: 'Lido com Sucesso',
          authKeyType: 'Padrão Transporte (FF..FF)',
          authKeyHex: 'FFFFFFFFFFFF',
          blocks: [
            {
              blockIndex: 0,
              type: 'Fabricante',
              hex: '04 A2 8F B1 38 08 04 00 62 63 64 65 66 67 68 69',
              ascii: '....8...bcdefghi',
              isRead: true
            },
            {
              blockIndex: 1,
              type: 'Dados',
              hex: '14 00 00 00 EB FF FF FF 14 00 00 00 01 FE 01 FE',
              ascii: '................',
              isRead: true
            },
            {
              blockIndex: 2,
              type: 'Dados',
              hex: '00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00',
              ascii: '................',
              isRead: true
            },
            {
              blockIndex: 3,
              type: 'Trailer de Setor',
              hex: 'FF FF FF FF FF FF FF 07 80 69 FF FF FF FF FF FF',
              ascii: '.......i........',
              isRead: true
            }
          ],
          accessBits: {
            rawHex: 'FF 07 80',
            gpbHex: '0x69',
            isValid: true,
            trailerPermissions: {
              keyAWrite: 'Key A',
              accessBitsRead: 'Key A',
              accessBitsWrite: 'Nunca (HW)',
              keyBWrite: 'Key A'
            },
            dataPermissions: {
              read: 'Key A|B',
              write: 'Key A|B',
              inc: 'Key A|B',
              dec: 'Key A|B'
            }
          }
        },
        {
          sectorIndex: 1,
          blockCount: 4,
          firstBlockIndex: 4,
          status: 'Lido com Sucesso',
          authKeyType: 'MAD / NXP (A0..A5)',
          authKeyHex: 'A0A1A2A3A4A5',
          blocks: [
            {
              blockIndex: 4,
              type: 'Dados',
              hex: '53 50 5F 54 52 41 4E 53 49 54 5F 56 41 4C 49 44',
              ascii: 'SP_TRANSIT_VALID',
              isRead: true
            },
            {
              blockIndex: 5,
              type: 'Dados',
              hex: '00 25 00 00 DA FF FF FF 00 25 00 00 05 FA 05 FA',
              ascii: '.%.......%......',
              isRead: true
            },
            {
              blockIndex: 6,
              type: 'Dados',
              hex: '20 26 08 16 11 30 00 00 00 00 00 00 00 00 00 00',
              ascii: ' 26...0..........',
              isRead: true
            },
            {
              blockIndex: 7,
              type: 'Trailer de Setor',
              hex: 'A0 A1 A2 A3 A4 A5 78 77 88 00 B0 B1 B2 B3 B4 B5',
              ascii: '......xw........',
              isRead: true
            }
          ],
          accessBits: {
            rawHex: '78 77 88',
            gpbHex: '0x00',
            isValid: true,
            trailerPermissions: {
              keyAWrite: 'Nunca',
              accessBitsRead: 'Key A|B',
              accessBitsWrite: 'Nunca',
              keyBWrite: 'Nunca'
            },
            dataPermissions: {
              read: 'Key A|B',
              write: 'Key B',
              inc: 'Nunca',
              dec: 'Key A|B'
            }
          }
        },
        {
          sectorIndex: 2,
          blockCount: 4,
          firstBlockIndex: 8,
          status: 'Falha de Autenticação',
          blocks: [
            {
              blockIndex: 8,
              type: 'Dados',
              hex: 'Não lido / Protegido',
              ascii: '—',
              isRead: false
            },
            {
              blockIndex: 9,
              type: 'Dados',
              hex: 'Não lido / Protegido',
              ascii: '—',
              isRead: false
            },
            {
              blockIndex: 10,
              type: 'Dados',
              hex: 'Não lido / Protegido',
              ascii: '—',
              isRead: false
            },
            {
              blockIndex: 11,
              type: 'Trailer de Setor',
              hex: 'Não lido / Protegido',
              ascii: '—',
              isRead: false
            }
          ]
        }
      ]
    }
  },
  {
    id: 'tag-ntag-2',
    name: 'Crachá de Visitante (NTAG213)',
    uidColon: '04:78:E1:92:4A:6C:80',
    uidContinuous: '0478E1924A6C80',
    uidDecimal: '1259182390192128',
    uidReversed: '806C4A92E17804',
    techMain: 'MIFARE Ultralight / NTAG',
    techList: ['NfcA', 'MifareUltralight', 'Ndef'],
    atqa: '00 44',
    sak: '00',
    standard: 'ISO 14443-3A (NFC Forum Type 2)',
    ndefMessage: {
      recordType: 'URI (https://nfcinspector.local/badge/4091)',
      payloadText: 'https://nfcinspector.local/badge/4091',
      sizeBytes: 42,
      isWritable: false
    }
  }
];

export default function App() {
  const [currentScreen, setCurrentScreen] = useState<'reader' | 'history' | 'compare' | 'settings'>('reader');
  const [scannedTag, setScannedTag] = useState<MockTag | null>(SAMPLE_TAGS[0]);
  const [copiedKey, setCopiedKey] = useState<string | null>(null);
  const [filterSector, setFilterSector] = useState<'all' | 'auth' | 'fail'>('all');
  const [historySearch, setHistorySearch] = useState('');
  const [expandedSectors, setExpandedSectors] = useState<Record<number, boolean>>({ 0: true, 1: true });

  const handleCopy = (text: string, id: string) => {
    navigator.clipboard.writeText(text);
    setCopiedKey(id);
    setTimeout(() => setCopiedKey(null), 2000);
  };

  const toggleSector = (idx: number) => {
    setExpandedSectors(prev => ({ ...prev, [idx]: !prev[idx] }));
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col items-center justify-start p-4 sm:p-6 font-sans">
      {/* Scope Disclaimer Banner */}
      <div className="w-full max-w-4xl bg-slate-900 border border-slate-800 rounded-xl p-3 mb-4 flex items-center justify-between gap-3 text-xs text-slate-400">
        <div className="flex items-center gap-2">
          <span className="px-2 py-0.5 rounded bg-cyan-500/10 text-cyan-400 font-mono font-bold">AI Studio Preview</span>
          <span>Este preview em <code className="text-cyan-300">ai-preview/</code> é puramente visual. A aplicação oficial está localizada em <code className="text-emerald-400">android/</code>.</span>
        </div>
        <div className="flex items-center gap-2">
          <span className="inline-flex items-center gap-1 text-emerald-400 font-semibold">
            <ShieldCheck className="w-3.5 h-3.5" /> 100% Offline
          </span>
        </div>
      </div>

      {/* Simulator Device Frame */}
      <div className="w-full max-w-md bg-slate-900 border-4 border-slate-800 rounded-[2.5rem] shadow-2xl overflow-hidden flex flex-col min-h-[760px] relative">
        {/* Android Status Bar */}
        <div className="bg-slate-950 px-6 py-2 flex items-center justify-between text-xs text-slate-400 select-none">
          <span className="font-semibold text-slate-200">12:30</span>
          <div className="flex items-center gap-1.5">
            <Radio className="w-3.5 h-3.5 text-cyan-400 animate-pulse" />
            <span className="text-[10px] font-bold text-cyan-400">NFC</span>
            <div className="w-3.5 h-2.5 border border-slate-400 rounded-sm relative">
              <div className="bg-slate-300 w-2.5 h-1.5 m-0.5 rounded-[1px]" />
            </div>
          </div>
        </div>

        {/* Top App Bar */}
        <div className="bg-slate-900/90 border-b border-slate-800 px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-cyan-500/10 border border-cyan-500/30 flex items-center justify-center text-cyan-400">
              <Radio className="w-4 h-4" />
            </div>
            <div>
              <h1 className="text-sm font-bold text-slate-100">NFC Inspector</h1>
              <p className="text-[10px] text-emerald-400">Pronto para leitura • Offline</p>
            </div>
          </div>

          <button
            onClick={() => setScannedTag(scannedTag?.id === SAMPLE_TAGS[0].id ? SAMPLE_TAGS[1] : SAMPLE_TAGS[0])}
            className="flex items-center gap-1 px-2.5 py-1 text-xs rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition"
            title="Alternar Tag Simulada"
          >
            <RefreshCw className="w-3 h-3 text-cyan-400" />
            <span>Simular Tag</span>
          </button>
        </div>

        {/* Screen Content */}
        <div className="flex-1 overflow-y-auto p-4 space-y-4 text-slate-200">
          {currentScreen === 'reader' && (
            <div className="space-y-4">
              {/* Ready Radar Box */}
              <div className="bg-slate-950 border border-slate-800/80 rounded-2xl p-4 flex flex-col items-center text-center relative overflow-hidden">
                <div className="w-12 h-12 rounded-full bg-cyan-500/10 border border-cyan-500/30 flex items-center justify-center text-cyan-400 mb-2">
                  <Radio className="w-6 h-6 animate-pulse" />
                </div>
                <h3 className="text-sm font-bold text-slate-100">Leitor NFC Ativo</h3>
                <p className="text-xs text-slate-400 mt-0.5">Aproxime uma tag do verso do smartphone</p>
              </div>

              {scannedTag && (
                <div className="space-y-3">
                  {/* Identification Card */}
                  <div className="bg-slate-950 border border-slate-800 rounded-xl p-3.5 space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-bold text-cyan-400 uppercase tracking-wider">Identificação Geral</span>
                      <span className="text-[10px] px-2 py-0.5 bg-cyan-500/10 text-cyan-400 rounded-full border border-cyan-500/20 font-mono">
                        {scannedTag.techList.join(', ')}
                      </span>
                    </div>

                    <div className="space-y-1.5 text-xs">
                      <div className="flex justify-between items-center bg-slate-900/60 px-2 py-1.5 rounded-lg border border-slate-800/50">
                        <span className="text-slate-400 font-mono">UID (Hex):</span>
                        <div className="flex items-center gap-1.5">
                          <span className="font-mono font-bold text-slate-200">{scannedTag.uidColon}</span>
                          <button
                            onClick={() => handleCopy(scannedTag.uidColon, 'uid-colon')}
                            className="text-slate-400 hover:text-slate-200 p-0.5"
                          >
                            {copiedKey === 'uid-colon' ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                          </button>
                        </div>
                      </div>

                      <div className="flex justify-between items-center bg-slate-900/60 px-2 py-1.5 rounded-lg border border-slate-800/50">
                        <span className="text-slate-400 font-mono">UID (Contínuo):</span>
                        <span className="font-mono text-slate-200">{scannedTag.uidContinuous}</span>
                      </div>

                      <div className="flex justify-between items-center bg-slate-900/60 px-2 py-1.5 rounded-lg border border-slate-800/50">
                        <span className="text-slate-400">Tecnologia:</span>
                        <span className="text-slate-200 font-medium">{scannedTag.techMain}</span>
                      </div>

                      <div className="flex justify-between items-center bg-slate-900/60 px-2 py-1.5 rounded-lg border border-slate-800/50">
                        <span className="text-slate-400">Padrão / Camada RF:</span>
                        <span className="text-slate-300 font-mono text-[11px]">{scannedTag.standard}</span>
                      </div>

                      <div className="grid grid-cols-2 gap-2 pt-1">
                        <div className="bg-slate-900/80 p-2 rounded-lg border border-slate-800 text-center">
                          <div className="text-[10px] text-slate-400">ATQA</div>
                          <div className="font-mono font-bold text-cyan-400">{scannedTag.atqa}</div>
                        </div>
                        <div className="bg-slate-900/80 p-2 rounded-lg border border-slate-800 text-center">
                          <div className="text-[10px] text-slate-400">SAK</div>
                          <div className="font-mono font-bold text-cyan-400">{scannedTag.sak}</div>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* MIFARE Classic Inspector */}
                  {scannedTag.mifareClassic && (
                    <div className="bg-slate-950 border border-slate-800 rounded-xl p-3.5 space-y-3">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-1.5">
                          <Cpu className="w-4 h-4 text-cyan-400" />
                          <span className="text-xs font-bold text-cyan-400">MIFARE Classic</span>
                        </div>
                        <span className="text-[10px] px-2 py-0.5 bg-cyan-500/10 text-cyan-400 font-bold rounded-md">
                          {scannedTag.mifareClassic.sizeBytes / 1024} KB ({scannedTag.mifareClassic.sectorCount} Setores)
                        </span>
                      </div>

                      <div className="grid grid-cols-3 gap-1.5 text-center text-xs">
                        <div className="bg-slate-900/80 p-1.5 rounded border border-slate-800">
                          <div className="text-[9px] text-slate-400">Capacidade</div>
                          <div className="font-bold text-slate-200">{scannedTag.mifareClassic.sizeBytes} B</div>
                        </div>
                        <div className="bg-slate-900/80 p-1.5 rounded border border-slate-800">
                          <div className="text-[9px] text-slate-400">Setores</div>
                          <div className="font-bold text-slate-200">{scannedTag.mifareClassic.sectorCount}</div>
                        </div>
                        <div className="bg-slate-900/80 p-1.5 rounded border border-slate-800">
                          <div className="text-[9px] text-slate-400">Blocos</div>
                          <div className="font-bold text-slate-200">{scannedTag.mifareClassic.blockCount}</div>
                        </div>
                      </div>

                      {/* Filter Chips */}
                      <div className="flex items-center gap-1.5 pt-1">
                        <button
                          onClick={() => setFilterSector('all')}
                          className={`px-2 py-1 text-[11px] rounded-lg border transition ${
                            filterSector === 'all'
                              ? 'bg-cyan-500/20 text-cyan-300 border-cyan-500/40'
                              : 'bg-slate-900 text-slate-400 border-slate-800'
                          }`}
                        >
                          Todos ({scannedTag.mifareClassic.sectors.length})
                        </button>
                        <button
                          onClick={() => setFilterSector('auth')}
                          className={`px-2 py-1 text-[11px] rounded-lg border transition ${
                            filterSector === 'auth'
                              ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40'
                              : 'bg-slate-900 text-slate-400 border-slate-800'
                          }`}
                        >
                          Autenticados ({scannedTag.mifareClassic.authenticatedSectors})
                        </button>
                        <button
                          onClick={() => setFilterSector('fail')}
                          className={`px-2 py-1 text-[11px] rounded-lg border transition ${
                            filterSector === 'fail'
                              ? 'bg-rose-500/20 text-rose-300 border-rose-500/40'
                              : 'bg-slate-900 text-slate-400 border-slate-800'
                          }`}
                        >
                          Falhas ({scannedTag.mifareClassic.sectorCount - scannedTag.mifareClassic.authenticatedSectors})
                        </button>
                      </div>

                      {/* Sector List */}
                      <div className="space-y-2 pt-1">
                        {scannedTag.mifareClassic.sectors
                          .filter(sec => {
                            if (filterSector === 'auth') return sec.status !== 'Falha de Autenticação';
                            if (filterSector === 'fail') return sec.status === 'Falha de Autenticação';
                            return true;
                          })
                          .map(sec => (
                            <div key={sec.sectorIndex} className="bg-slate-900/70 border border-slate-800 rounded-lg p-2.5 space-y-2">
                              <div
                                onClick={() => toggleSector(sec.sectorIndex)}
                                className="flex items-center justify-between cursor-pointer"
                              >
                                <div className="flex items-center gap-2">
                                  <span className="text-xs font-bold text-slate-200">
                                    Setor {String(sec.sectorIndex).padStart(2, '0')}
                                  </span>
                                  <span className="text-[10px] text-slate-400">
                                    (Blocos #{sec.firstBlockIndex}..#{sec.firstBlockIndex + sec.blockCount - 1})
                                  </span>
                                </div>
                                <div className="flex items-center gap-2">
                                  <span
                                    className={`text-[10px] font-bold px-1.5 py-0.5 rounded ${
                                      sec.status === 'Lido com Sucesso'
                                        ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                                        : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                                    }`}
                                  >
                                    {sec.status}
                                  </span>
                                  {expandedSectors[sec.sectorIndex] ? (
                                    <ChevronUp className="w-3.5 h-3.5 text-slate-400" />
                                  ) : (
                                    <ChevronDown className="w-3.5 h-3.5 text-slate-400" />
                                  )}
                                </div>
                              </div>

                              {sec.authKeyType && (
                                <div className="text-[11px] text-emerald-400">
                                  Autenticação: {sec.authKeyType}
                                  {sec.authKeyHex && <span className="font-mono text-slate-400 text-[10px]"> ({sec.authKeyHex})</span>}
                                </div>
                              )}

                              {expandedSectors[sec.sectorIndex] && (
                                <div className="space-y-1.5 pt-1">
                                  {sec.blocks.map(blk => (
                                    <div key={blk.blockIndex} className="bg-slate-950 p-2 rounded border border-slate-800/80 text-xs">
                                      <div className="flex items-center justify-between">
                                        <div className="flex items-center gap-1.5">
                                          <span className="font-mono font-bold text-slate-200">
                                            Bloco {String(blk.blockIndex).padStart(2, '0')}
                                          </span>
                                          <span
                                            className={`text-[9px] px-1 rounded font-bold ${
                                              blk.type === 'Fabricante'
                                                ? 'bg-amber-500/20 text-amber-300'
                                                : blk.type === 'Trailer de Setor'
                                                ? 'bg-purple-500/20 text-purple-300'
                                                : 'bg-cyan-500/20 text-cyan-300'
                                            }`}
                                          >
                                            {blk.type}
                                          </span>
                                        </div>
                                        {blk.isRead && (
                                          <button
                                            onClick={() => handleCopy(blk.hex, `blk-${blk.blockIndex}`)}
                                            className="text-slate-400 hover:text-slate-200"
                                          >
                                            {copiedKey === `blk-${blk.blockIndex}` ? (
                                              <Check className="w-3 h-3 text-emerald-400" />
                                            ) : (
                                              <Copy className="w-3 h-3" />
                                            )}
                                          </button>
                                        )}
                                      </div>
                                      <div className="mt-1 font-mono text-[10px] text-slate-300 break-all leading-tight">
                                        HEX: {blk.hex}
                                      </div>
                                      {blk.isRead && (
                                        <div className="font-mono text-[10px] text-slate-400">
                                          ASCII: {blk.ascii}
                                        </div>
                                      )}
                                    </div>
                                  ))}

                                  {sec.accessBits && (
                                    <div className="bg-slate-950 p-2 rounded border border-slate-800 text-[10px] space-y-1">
                                      <div className="flex justify-between font-bold text-cyan-400">
                                        <span>Interpretação dos Access Bits</span>
                                        <span className="text-emerald-400 font-mono">{sec.accessBits.rawHex}</span>
                                      </div>
                                      <div className="text-slate-400">
                                        • Sector Trailer: Key A Write ({sec.accessBits.trailerPermissions.keyAWrite}) | Access Bits Read ({sec.accessBits.trailerPermissions.accessBitsRead})
                                      </div>
                                      <div className="text-slate-400">
                                        • Blocos de Dados: Read ({sec.accessBits.dataPermissions.read}) | Write ({sec.accessBits.dataPermissions.write})
                                      </div>
                                    </div>
                                  )}
                                </div>
                              )}
                            </div>
                          ))}
                      </div>
                    </div>
                  )}

                  {/* NDEF Message Card */}
                  {scannedTag.ndefMessage && (
                    <div className="bg-slate-950 border border-slate-800 rounded-xl p-3.5 space-y-2">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-bold text-cyan-400 uppercase tracking-wider">Mensagem NDEF</span>
                        <span className="text-[10px] px-2 py-0.5 bg-emerald-500/10 text-emerald-400 font-bold rounded-md">
                          {scannedTag.ndefMessage.sizeBytes} bytes
                        </span>
                      </div>
                      <div className="bg-slate-900/80 p-2.5 rounded-lg border border-slate-800 text-xs space-y-1">
                        <div className="text-[10px] text-slate-400 font-medium">Tipo de Registro: {scannedTag.ndefMessage.recordType}</div>
                        <div className="font-mono text-cyan-300 break-all">{scannedTag.ndefMessage.payloadText}</div>
                      </div>
                    </div>
                  )}

                  {/* Action Buttons */}
                  <div className="grid grid-cols-2 gap-2 pt-2">
                    <button
                      onClick={() => handleCopy(`UID: ${scannedTag.uidColon}\nTech: ${scannedTag.techMain}\nATQA: ${scannedTag.atqa}\nSAK: ${scannedTag.sak}`, 'full-report')}
                      className="flex items-center justify-center gap-1.5 py-2.5 rounded-xl bg-cyan-600 hover:bg-cyan-500 text-white font-medium text-xs shadow-lg shadow-cyan-600/20 transition"
                    >
                      {copiedKey === 'full-report' ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                      <span>Copiar Relatório</span>
                    </button>
                    <button
                      onClick={() => alert('Tag salva no banco local Room (100% Offline)!')}
                      className="flex items-center justify-center gap-1.5 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-xs transition"
                    >
                      <BookmarkPlus className="w-3.5 h-3.5 text-cyan-400" />
                      <span>Salvar no Histórico</span>
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}

          {currentScreen === 'history' && (
            <div className="space-y-3">
              <div className="relative">
                <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
                <input
                  type="text"
                  placeholder="Buscar por UID ou nome..."
                  value={historySearch}
                  onChange={e => setHistorySearch(e.target.value)}
                  className="w-full pl-9 pr-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
                />
              </div>

              <div className="space-y-2">
                {SAMPLE_TAGS.map(tag => (
                  <div
                    key={tag.id}
                    onClick={() => {
                      setScannedTag(tag);
                      setCurrentScreen('reader');
                    }}
                    className="bg-slate-950 border border-slate-800 hover:border-cyan-500/50 p-3 rounded-xl cursor-pointer transition space-y-1.5"
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-bold text-slate-200">{tag.name}</span>
                      <span className="text-[10px] font-mono text-cyan-400">{tag.uidColon}</span>
                    </div>
                    <div className="flex items-center justify-between text-[10px] text-slate-400">
                      <span>{tag.techMain}</span>
                      <span>{tag.standard}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {currentScreen === 'compare' && (
            <div className="space-y-3 text-xs">
              <div className="bg-slate-950 p-3 rounded-xl border border-slate-800 text-center">
                <h3 className="font-bold text-slate-200 mb-1">Comparação Lado a Lado</h3>
                <p className="text-[11px] text-slate-400">Compare especificações técnicas, ATQA, SAK e setores.</p>
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div className="bg-slate-950 p-2.5 rounded-lg border border-slate-800 space-y-1">
                  <div className="font-bold text-cyan-400 text-[11px]">Tag 1</div>
                  <div className="font-mono text-[10px]">{SAMPLE_TAGS[0].uidColon}</div>
                  <div className="text-[10px] text-slate-400">ATQA: {SAMPLE_TAGS[0].atqa}</div>
                  <div className="text-[10px] text-slate-400">SAK: {SAMPLE_TAGS[0].sak}</div>
                </div>
                <div className="bg-slate-950 p-2.5 rounded-lg border border-slate-800 space-y-1">
                  <div className="font-bold text-cyan-400 text-[11px]">Tag 2</div>
                  <div className="font-mono text-[10px]">{SAMPLE_TAGS[1].uidColon}</div>
                  <div className="text-[10px] text-slate-400">ATQA: {SAMPLE_TAGS[1].atqa}</div>
                  <div className="text-[10px] text-slate-400">SAK: {SAMPLE_TAGS[1].sak}</div>
                </div>
              </div>
            </div>
          )}

          {currentScreen === 'settings' && (
            <div className="space-y-3 text-xs">
              <div className="bg-slate-950 p-3.5 rounded-xl border border-slate-800 space-y-2">
                <h3 className="font-bold text-slate-200">Chaves de Diagnóstico MIFARE</h3>
                <p className="text-[11px] text-slate-400 leading-relaxed">
                  Defina chaves personalizadas de 6 bytes (HEX) para tentar autenticação em setores protegidos.
                </p>
                <div className="space-y-1.5 pt-1">
                  <div>
                    <label className="text-[10px] text-slate-400 block mb-1">Key A Personalizada (12 dígitos HEX):</label>
                    <input
                      type="text"
                      placeholder="Ex: A0A1A2A3A4A5"
                      defaultValue="FFFFFFFFFFFF"
                      className="w-full px-3 py-1.5 bg-slate-900 border border-slate-800 rounded-lg font-mono text-xs text-slate-200"
                    />
                  </div>
                  <div>
                    <label className="text-[10px] text-slate-400 block mb-1">Key B Personalizada (12 dígitos HEX):</label>
                    <input
                      type="text"
                      placeholder="Ex: B0B1B2B3B4B5"
                      defaultValue="B0B1B2B3B4B5"
                      className="w-full px-3 py-1.5 bg-slate-900 border border-slate-800 rounded-lg font-mono text-xs text-slate-200"
                    />
                  </div>
                </div>
              </div>

              <div className="bg-slate-950 p-3.5 rounded-xl border border-slate-800 space-y-1.5 text-[11px]">
                <div className="flex justify-between items-center py-1">
                  <span>Feedback Tátil (Vibração)</span>
                  <span className="text-emerald-400 font-bold">Ativado</span>
                </div>
                <div className="flex justify-between items-center py-1">
                  <span>Modo 100% Offline</span>
                  <span className="text-emerald-400 font-bold">Garantido</span>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Bottom Navigation Bar */}
        <div className="bg-slate-950/95 border-t border-slate-800 px-2 py-2 flex items-center justify-around">
          <button
            onClick={() => setCurrentScreen('reader')}
            className={`flex flex-col items-center gap-1 px-3 py-1 rounded-lg transition ${
              currentScreen === 'reader' ? 'text-cyan-400' : 'text-slate-500 hover:text-slate-300'
            }`}
          >
            <Radio className="w-4 h-4" />
            <span className="text-[10px] font-medium">Leitor</span>
          </button>
          <button
            onClick={() => setCurrentScreen('history')}
            className={`flex flex-col items-center gap-1 px-3 py-1 rounded-lg transition ${
              currentScreen === 'history' ? 'text-cyan-400' : 'text-slate-500 hover:text-slate-300'
            }`}
          >
            <History className="w-4 h-4" />
            <span className="text-[10px] font-medium">Histórico</span>
          </button>
          <button
            onClick={() => setCurrentScreen('compare')}
            className={`flex flex-col items-center gap-1 px-3 py-1 rounded-lg transition ${
              currentScreen === 'compare' ? 'text-cyan-400' : 'text-slate-500 hover:text-slate-300'
            }`}
          >
            <GitCompare className="w-4 h-4" />
            <span className="text-[10px] font-medium">Comparar</span>
          </button>
          <button
            onClick={() => setCurrentScreen('settings')}
            className={`flex flex-col items-center gap-1 px-3 py-1 rounded-lg transition ${
              currentScreen === 'settings' ? 'text-cyan-400' : 'text-slate-500 hover:text-slate-300'
            }`}
          >
            <Settings className="w-4 h-4" />
            <span className="text-[10px] font-medium">Ajustes</span>
          </button>
        </div>
      </div>
    </div>
  );
}
