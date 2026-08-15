export type NfcStateEnum = 'unsupported' | 'disabled' | 'ready' | 'detected' | 'error';

export interface NfcAParams {
  atqaHex: string;
  sakHex: string;
  timeoutMs: number;
  maxTransceiveBytes: number;
}

export interface NfcBParams {
  appDataHex: string;
  protocolInfoHex: string;
  timeoutMs: number;
  maxTransceiveBytes: number;
}

export interface IsoDepParams {
  historicalBytesHex?: string;
  hiLayerResponseHex?: string;
  isExtendedLengthApduSupported: boolean;
  timeoutMs: number;
  maxTransceiveBytes: number;
}

export interface MifareClassicParams {
  typeName: string;
  sizeBytes: number;
  sectorCount: number;
  blockCount: number;
  note: string;
}

export interface MifareUltralightParams {
  typeName: string;
  maxTransceiveBytes: number;
  timeoutMs: number;
}

export interface NfcFParams {
  systemCodeHex?: string;
  manufacturerResponseHex?: string;
  timeoutMs: number;
  maxTransceiveBytes: number;
}

export interface NfcVParams {
  dsfidHex?: string;
  responseFlagsHex?: string;
  maxTransceiveBytes: number;
}

export interface NdefRecordItem {
  id: string;
  tnfName: string;
  typeString: string;
  isText: boolean;
  isUri: boolean;
  isMime: boolean;
  isExternal: boolean;
  textLanguage?: string;
  textContent?: string;
  uriContent?: string;
  mimeType?: string;
  rawPayloadHex: string;
}

export interface NdefParams {
  isWritable: boolean;
  canMakeReadOnly: boolean;
  typeName: string;
  currentSizeBytes: number;
  maxSizeBytes: number;
  recordCount: number;
  records: NdefRecordItem[];
}

export interface TagRecord {
  id: string;
  timestamp: number;
  formattedDateTime: string;
  uidColonHex: string;
  uidContinuousHex: string;
  uidDecimal: string;
  uidLengthBytes: number;
  mainTechnology: string;
  technologies: string[];
  nfcA?: NfcAParams;
  nfcB?: NfcBParams;
  isoDep?: IsoDepParams;
  mifareClassic?: MifareClassicParams;
  mifareUltralight?: MifareUltralightParams;
  nfcF?: NfcFParams;
  nfcV?: NfcVParams;
  ndef?: NdefParams;
  isNdefFormatable?: boolean;
  scanNotes: string;
  fullReport: string;
}
