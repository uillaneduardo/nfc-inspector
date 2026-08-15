import { TagRecord } from '../types';

export function generateReport(tag: Partial<TagRecord>): string {
  const dateStr = tag.formattedDateTime || new Date().toLocaleString('pt-BR');
  let r = `===============================\n        NFC INSPECTOR\n    Relatório Técnico Detalhado\n===============================\nData e Hora: ${dateStr}\nTecnologia Principal: ${tag.mainTechnology}\n\n--- IDENTIFICADOR (UID) ---\nHex com separadores: ${tag.uidColonHex}\nHex contínuo:        ${tag.uidContinuousHex}\nDecimal:             ${tag.uidDecimal}\nTamanho:             ${tag.uidLengthBytes} bytes (${(tag.uidLengthBytes || 0) * 8} bits)\n\n--- TECNOLOGIAS DETECTADAS ---\n`;
  
  tag.technologies?.forEach(t => {
    r += `• ${t}\n`;
  });
  r += '\n';

  if (tag.nfcA) {
    r += `--- NFC-A (ISO 14443-3A) ---\nATQA:                ${tag.nfcA.atqaHex}\nSAK:                 ${tag.nfcA.sakHex}\nTimeout:             ${tag.nfcA.timeoutMs} ms\nMax Transceive:      ${tag.nfcA.maxTransceiveBytes} bytes\n\n`;
  }
  if (tag.isoDep) {
    r += `--- ISO-DEP (ISO 14443-4) ---\nHistorical Bytes:    ${tag.isoDep.historicalBytesHex || 'N/A'}\nHiLayer Response:    ${tag.isoDep.hiLayerResponseHex || 'N/A'}\nExtended APDU:       ${tag.isoDep.isExtendedLengthApduSupported ? 'Suportado' : 'Não suportado'}\nTimeout:             ${tag.isoDep.timeoutMs} ms\nMax Transceive:      ${tag.isoDep.maxTransceiveBytes} bytes\n\n`;
  }
  if (tag.mifareClassic) {
    r += `--- MIFARE CLASSIC ---\nTipo:                ${tag.mifareClassic.typeName}\nTamanho:             ${tag.mifareClassic.sizeBytes} bytes\nSetores:             ${tag.mifareClassic.sectorCount}\nBlocos:              ${tag.mifareClassic.blockCount}\nNota:                ${tag.mifareClassic.note}\n\n`;
  }
  if (tag.mifareUltralight) {
    r += `--- MIFARE ULTRALIGHT ---\nTipo:                ${tag.mifareUltralight.typeName}\nMax Transceive:      ${tag.mifareUltralight.maxTransceiveBytes} bytes\nTimeout:             ${tag.mifareUltralight.timeoutMs} ms\n\n`;
  }
  if (tag.nfcF) {
    r += `--- NFC-F (JIS 6319-4 / FeliCa) ---\nSystem Code:         ${tag.nfcF.systemCodeHex || 'N/A'}\nMfr Response:        ${tag.nfcF.manufacturerResponseHex || 'N/A'}\nMax Transceive:      ${tag.nfcF.maxTransceiveBytes} bytes\n\n`;
  }
  if (tag.nfcV) {
    r += `--- NFC-V (ISO 15693 / Vicinity) ---\nDSFID:               ${tag.nfcV.dsfidHex || 'N/A'}\nResponse Flags:      ${tag.nfcV.responseFlagsHex || 'N/A'}\nMax Transceive:      ${tag.nfcV.maxTransceiveBytes} bytes\n\n`;
  }
  if (tag.ndef) {
    r += `--- NDEF (NFC Data Exchange Format) ---\nTipo de Armazenamento: ${tag.ndef.typeName}\nGravável:              ${tag.ndef.isWritable ? 'Sim' : 'Não'}\nBloqueável p/ Leitura: ${tag.ndef.canMakeReadOnly ? 'Sim' : 'Não'}\nTamanho Atual:         ${tag.ndef.currentSizeBytes} bytes\nCapacidade Máxima:     ${tag.ndef.maxSizeBytes} bytes\nTotal de Registros:    ${tag.ndef.recordCount}\n`;
    tag.ndef.records.forEach((rec, idx) => {
      r += `  [Registro #${idx + 1}]\n    TNF:             ${rec.tnfName}\n    Tipo:            ${rec.typeString}\n`;
      if (rec.isText) {
        r += `    Idioma:          ${rec.textLanguage || 'pt'}\n    Texto:           ${rec.textContent}\n`;
      } else if (rec.isUri) {
        r += `    URI:             ${rec.uriContent}\n`;
      } else if (rec.isMime) {
        r += `    MIME Type:       ${rec.mimeType}\n`;
      }
      r += `    Payload (Hex):   ${rec.rawPayloadHex}\n`;
    });
    r += '\n';
  }

  r += `===============================\nPrivacidade: 100% Offline e Seguro.\nGerado por NFC Inspector.`;
  return r;
}

export const SAMPLE_TAGS: TagRecord[] = [
  {
    id: 'sample_ntag213',
    timestamp: Date.now() - 3600000,
    formattedDateTime: '15/08/2026 14:15:32',
    uidColonHex: '04:A7:31:92:6C:18:80',
    uidContinuousHex: '04A731926C1880',
    uidDecimal: '1309859218765440',
    uidLengthBytes: 7,
    mainTechnology: 'NFC Forum Type 2 (NfcA + NDEF / NTAG213)',
    technologies: ['NfcA', 'MifareUltralight', 'Ndef'],
    nfcA: {
      atqaHex: '0x0044',
      sakHex: '0x00',
      timeoutMs: 618,
      maxTransceiveBytes: 253,
    },
    mifareUltralight: {
      typeName: 'NTAG213 (144 bytes user memory)',
      maxTransceiveBytes: 253,
      timeoutMs: 618,
    },
    ndef: {
      isWritable: true,
      canMakeReadOnly: true,
      typeName: 'NFC Forum Type 2',
      currentSizeBytes: 42,
      maxSizeBytes: 144,
      recordCount: 1,
      records: [
        {
          id: 'rec_1',
          tnfName: 'TNF_WELL_KNOWN (0x01)',
          typeString: 'U',
          isText: false,
          isUri: true,
          isMime: false,
          isExternal: false,
          uriContent: 'https://nfcinspector.app',
          rawPayloadHex: '046E6663696E73706563746F722E617070',
        },
      ],
    },
    scanNotes: 'UID é um identificador de camada física e não deve ser considerado automaticamente uma credencial segura.',
    fullReport: '',
  },
  {
    id: 'sample_mifare1k',
    timestamp: Date.now() - 7200000,
    formattedDateTime: '15/08/2026 13:10:05',
    uidColonHex: '8A:3F:E2:01',
    uidContinuousHex: '8A3FE201',
    uidDecimal: '2319442433',
    uidLengthBytes: 4,
    mainTechnology: 'NXP MIFARE Classic 1K',
    technologies: ['NfcA', 'MifareClassic'],
    nfcA: {
      atqaHex: '0x0004',
      sakHex: '0x08',
      timeoutMs: 618,
      maxTransceiveBytes: 253,
    },
    mifareClassic: {
      typeName: 'MIFARE Classic Standard (1K)',
      sizeBytes: 1024,
      sectorCount: 16,
      blockCount: 64,
      note: 'Suporte ao MIFARE Classic depende do chipset NFC do aparelho (NXP). A ausência dessa tecnologia na API não descarta que o cartão seja MIFARE.',
    },
    isNdefFormatable: true,
    scanNotes: 'UID de 4 bytes clássico (single size). Cartão amplamente utilizado em controle de acesso.',
    fullReport: '',
  },
  {
    id: 'sample_isodep_emv',
    timestamp: Date.now() - 10800000,
    formattedDateTime: '15/08/2026 12:05:44',
    uidColonHex: '1B:90:4D:2F',
    uidContinuousHex: '1B904D2F',
    uidDecimal: '462441775',
    uidLengthBytes: 4,
    mainTechnology: 'ISO 14443-4A (ISO-DEP / Smart Card)',
    technologies: ['NfcA', 'IsoDep'],
    nfcA: {
      atqaHex: '0x0040',
      sakHex: '0x20',
      timeoutMs: 618,
      maxTransceiveBytes: 261,
    },
    isoDep: {
      historicalBytesHex: '0x804F534543555245',
      hiLayerResponseHex: 'Não disponível neste cartão',
      isExtendedLengthApduSupported: true,
      timeoutMs: 1200,
      maxTransceiveBytes: 261,
    },
    scanNotes: 'Identificado smart card / cartão sem contato ISO 14443-4. Apenas diagnóstico de RF realizado, sem extração de dados confidenciais.',
    fullReport: '',
  },
  {
    id: 'sample_felica',
    timestamp: Date.now() - 14400000,
    formattedDateTime: '15/08/2026 11:00:19',
    uidColonHex: '01:2E:3F:8A:9B:CD:EF:10',
    uidContinuousHex: '012E3F8A9BCDEF10',
    uidDecimal: '85072049581729552',
    uidLengthBytes: 8,
    mainTechnology: 'JIS 6319-4 (Sony FeliCa / NFC-F)',
    technologies: ['NfcF', 'Ndef'],
    nfcF: {
      systemCodeHex: '0x12FC',
      manufacturerResponseHex: '0x012E3F8A',
      timeoutMs: 250,
      maxTransceiveBytes: 254,
    },
    ndef: {
      isWritable: false,
      canMakeReadOnly: true,
      typeName: 'NFC Forum Type 3',
      currentSizeBytes: 64,
      maxSizeBytes: 240,
      recordCount: 1,
      records: [
        {
          id: 'rec_felica_1',
          tnfName: 'TNF_WELL_KNOWN (0x01)',
          typeString: 'T',
          isText: true,
          isUri: false,
          isMime: false,
          isExternal: false,
          textLanguage: 'ja',
          textContent: '交通乗車券 FeliCa Lite-S',
          rawPayloadHex: '026A61E4BAA4E9809AE4B997E8BB8AE588B8',
        },
      ],
    },
    scanNotes: 'Padrão NFC-F utilizado com frequência no Japão e Ásia (FeliCa).',
    fullReport: '',
  },
  {
    id: 'sample_vicinity',
    timestamp: Date.now() - 18000000,
    formattedDateTime: '15/08/2026 09:40:50',
    uidColonHex: 'E0:04:01:00:8F:22:91:3C',
    uidContinuousHex: 'E00401008F22913C',
    uidDecimal: '16142109847192837436',
    uidLengthBytes: 8,
    mainTechnology: 'ISO 15693 (Vicinity Card / NFC-V)',
    technologies: ['NfcV', 'Ndef'],
    nfcV: {
      dsfidHex: '0x00',
      responseFlagsHex: '0x00',
      maxTransceiveBytes: 253,
    },
    ndef: {
      isWritable: true,
      canMakeReadOnly: false,
      typeName: 'NFC Forum Type 5',
      currentSizeBytes: 38,
      maxSizeBytes: 1024,
      recordCount: 1,
      records: [
        {
          id: 'rec_vicinity_1',
          tnfName: 'TNF_WELL_KNOWN (0x01)',
          typeString: 'T',
          isText: true,
          isUri: false,
          isMime: false,
          isExternal: false,
          textLanguage: 'pt',
          textContent: 'Etiqueta de Logística ICODE SLIX',
          rawPayloadHex: '0270744574697175657461206C6F67697374696361',
        },
      ],
    },
    scanNotes: 'Cartão Vicinity para longo alcance de leitura de estoque e logística.',
    fullReport: '',
  },
];

// Initialize full report strings
SAMPLE_TAGS.forEach(tag => {
  tag.fullReport = generateReport(tag);
});
