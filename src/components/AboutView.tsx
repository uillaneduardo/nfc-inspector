import React from 'react';
import {
  ShieldCheck,
  Radio,
  Fingerprint,
  FileCode,
  CreditCard,
  Cpu,
  Lock,
  Smartphone,
  Info,
} from 'lucide-react';

export const AboutView: React.FC = () => {
  return (
    <div className="space-y-4 pb-12">
      {/* App Header */}
      <div className="rounded-2xl bg-neutral-900 border border-neutral-800 p-5">
        <div className="flex items-center gap-3 mb-2">
          <div className="w-10 h-10 rounded-xl bg-blue-600/20 border border-blue-500/40 flex items-center justify-center text-blue-400">
            <Radio className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-lg font-bold text-white">NFC Inspector</h2>
            <p className="text-xs text-neutral-400">Versão 1.0.0 • Nativo Android (Kotlin + Jetpack Compose)</p>
          </div>
        </div>
        <p className="text-xs text-neutral-300 leading-relaxed mt-2">
          Aplicativo de diagnóstico técnico, inspeção e aprendizado sobre cartões e tags NFC pertencentes ao usuário ou sob autorização expressa para testes de laboratório.
        </p>
      </div>

      {/* Privacy Guarantee */}
      <div className="rounded-2xl bg-emerald-950/30 border border-emerald-800/40 p-4">
        <div className="flex items-start gap-3">
          <ShieldCheck className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5" />
          <div>
            <h3 className="text-sm font-bold text-emerald-400">100% Offline & Privacidade Absoluta</h3>
            <p className="text-xs text-neutral-300 mt-1 leading-relaxed">
              Sem conexão com a internet, sem anúncios, sem trackers, sem telemetria e sem cadastro. Todas as leituras e relatórios permanecem exclusivamente na memória local deste aparelho.
            </p>
          </div>
        </div>
      </div>

      {/* Educational Guide */}
      <h3 className="text-xs font-bold uppercase tracking-wider text-neutral-400 pt-2 px-1">
        Guia Conceitual de Tecnologias NFC
      </h3>

      <div className="space-y-3">
        {/* NFC */}
        <div className="rounded-xl bg-neutral-900 border border-neutral-800 p-4 space-y-1.5">
          <div className="flex items-center gap-2 text-blue-400 font-semibold text-xs">
            <Radio className="w-4 h-4" />
            <span>NFC (Near Field Communication)</span>
          </div>
          <p className="text-xs text-neutral-300 leading-relaxed">
            Tecnologia de comunicação sem fio por acoplamento indutivo na faixa de 13,56 MHz, operando em distâncias curtas (&lt; 4 cm) segundo os padrões ISO/IEC 18092, ISO/IEC 14443 e ISO/IEC 15693.
          </p>
        </div>

        {/* UID */}
        <div className="rounded-xl bg-neutral-900 border border-neutral-800 p-4 space-y-1.5">
          <div className="flex items-center gap-2 text-blue-400 font-semibold text-xs">
            <Fingerprint className="w-4 h-4" />
            <span>UID (Unique Identifier)</span>
          </div>
          <p className="text-xs text-neutral-300 leading-relaxed">
            Identificador de hardware transmitido durante o processo de anticolisão de RF (4, 7 ou 10 bytes). Um UID nunca deve ser considerado automaticamente como uma credencial ou chave de autenticação segura.
          </p>
        </div>

        {/* NDEF */}
        <div className="rounded-xl bg-neutral-900 border border-neutral-800 p-4 space-y-1.5">
          <div className="flex items-center gap-2 text-blue-400 font-semibold text-xs">
            <FileCode className="w-4 h-4" />
            <span>NDEF (NFC Data Exchange Format)</span>
          </div>
          <p className="text-xs text-neutral-300 leading-relaxed">
            Estrutura de dados padronizada pelo NFC Forum composta por Registros NDEF (com campos TNF, Type e Payload) para troca universal de URLs, textos puros, contatos e payloads MIME.
          </p>
        </div>

        {/* ISO-DEP */}
        <div className="rounded-xl bg-neutral-900 border border-neutral-800 p-4 space-y-1.5">
          <div className="flex items-center gap-2 text-blue-400 font-semibold text-xs">
            <CreditCard className="w-4 h-4" />
            <span>ISO-DEP (ISO 14443-4)</span>
          </div>
          <p className="text-xs text-neutral-300 leading-relaxed">
            Camada de transmissão por protocolo de blocos APDU (Application Protocol Data Unit) empregada em smart cards de transporte, cartões bancários e passaportes eletrônicos (eMRTD).
          </p>
        </div>

        {/* MIFARE */}
        <div className="rounded-xl bg-neutral-900 border border-neutral-800 p-4 space-y-1.5">
          <div className="flex items-center gap-2 text-blue-400 font-semibold text-xs">
            <Cpu className="w-4 h-4" />
            <span>MIFARE® (Classic & Ultralight)</span>
          </div>
          <p className="text-xs text-neutral-300 leading-relaxed">
            Família de circuitos integrados proprietários da NXP Semiconductors. O suporte à leitura de MIFARE Classic depende de hardware de RF compatível com modulação específica no chipset NFC do smartphone.
          </p>
        </div>

        {/* Cartões Bancários */}
        <div className="rounded-xl bg-neutral-900 border border-neutral-800 p-4 space-y-1.5">
          <div className="flex items-center gap-2 text-amber-400 font-semibold text-xs">
            <Lock className="w-4 h-4" />
            <span>Cartões Bancários & Diagnóstico de RF</span>
          </div>
          <p className="text-xs text-neutral-300 leading-relaxed">
            O NFC Inspector diagnostica a presença de interface sem contato e parâmetros técnicos básicos de RF. O aplicativo <strong>não extrai CVV, PIN, chaves criptográficas ou transações</strong>.
          </p>
        </div>
      </div>

      {/* Target Device Notice */}
      <div className="rounded-xl bg-neutral-900 border border-neutral-800 p-4 text-xs text-neutral-400 space-y-1">
        <div className="flex items-center gap-1.5 text-neutral-300 font-semibold mb-1">
          <Smartphone className="w-4 h-4 text-blue-400" />
          <span>Dispositivo Alvo: Motorola Moto G50 5G</span>
        </div>
        <p>Compatível com Android 12 (minSdk 26, targetSdk 34).</p>
      </div>
    </div>
  );
};
