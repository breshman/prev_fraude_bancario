/**
 * Utilidad para exportación de datos de simulación hacia Microsoft Excel.
 * Soporta formato Excel XML Spreadsheet (.xls) con estilos, colores y anchos de columna,
 * así como formato CSV con BOM UTF-8 para compatibilidad universal con Excel en cualquier SO.
 */

export interface SimulationExportData {
  id: string;
  timestamp: string | Date;
  customerName: string;
  accountId: string;
  amount: number;
  channel: string;
  country: string;
  city: string;
  device: string;
  ipAddress: string;
  riskScore: number;
  riskLevel: string;
  threshold: number;
  exceedsThreshold: boolean;
  status: string;
  reasons: string[];
}

export interface ExcelExportFilterOptions {
  format?: 'xlsx' | 'xls' | 'csv';
  startDate?: string;
  endDate?: string;
  threshold?: number;
  statusFilter?: string;
  onlyAlerts?: boolean;
  selectedIds?: string[];
}

export class ExcelExportUtil {
  /**
   * Descarga en el navegador un archivo Blob generado por el backend (GET /api/v1/transacciones/exportar),
   * extrayendo el nombre del archivo desde el header Content-Disposition si está disponible.
   */
  static downloadBackendBlob(
    blob: Blob,
    contentDisposition?: string | null,
    fallbackPrefix: string = 'Analisis_Fraude_SecurityFlow',
    format: 'xlsx' | 'xls' | 'csv' = 'xlsx',
    startDate?: string,
    endDate?: string
  ): void {
    let filename = this.extractFilenameFromHeader(contentDisposition);
    if (!filename) {
      const timestampStr = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
      const rangePart = startDate && endDate
        ? `_${startDate}_a_${endDate}`
        : (startDate ? `_desde_${startDate}` : (endDate ? `_hasta_${endDate}` : ''));
      const ext = format === 'csv' ? 'csv' : 'xlsx';
      filename = `${fallbackPrefix}${rangePart}_${timestampStr}.${ext}`;
    }
    this.triggerDownload(blob, filename);
  }

  /**
   * Filtra una lista de transacciones por rango de fechas (YYYY-MM-DD) para respaldo local o análisis en vista.
   */
  static filterByDateRange<T extends { timestamp: string | Date }>(
    data: T[],
    startDate?: string,
    endDate?: string
  ): T[] {
    if (!startDate && !endDate) return data;

    const startMs = startDate ? new Date(`${startDate}T00:00:00`).getTime() : null;
    const endMs = endDate ? new Date(`${endDate}T23:59:59.999`).getTime() : null;

    return data.filter(item => {
      const itemTime = new Date(item.timestamp).getTime();
      if (isNaN(itemTime)) return true;
      if (startMs !== null && !isNaN(startMs) && itemTime < startMs) return false;
      if (endMs !== null && !isNaN(endMs) && itemTime > endMs) return false;
      return true;
    });
  }

  private static extractFilenameFromHeader(contentDisposition?: string | null): string | null {
    if (!contentDisposition) return null;
    const match = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/.exec(contentDisposition);
    if (match && match[1]) {
      return match[1].replace(/['"]/g, '').trim();
    }
    return null;
  }

  /**
   * Genera y descarga un archivo Excel (.xls) con estilos visuales y formato bancario (respaldo local).
   */
  static exportToExcel(
    data: SimulationExportData[],
    filename: string = 'Analisis_Fraude_SecurityFlow',
    startDate?: string,
    endDate?: string
  ): void {
    const filtered = this.filterByDateRange(data, startDate, endDate);
    const timestampStr = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
    const rangePart = startDate && endDate
      ? `_${startDate}_a_${endDate}`
      : (startDate ? `_desde_${startDate}` : (endDate ? `_hasta_${endDate}` : ''));
    const fullFilename = `${filename}${rangePart}_${timestampStr}.xls`;

    const xmlContent = this.buildExcelXml(filtered);
    const blob = new Blob([xmlContent], { type: 'application/vnd.ms-excel;charset=utf-8' });
    this.triggerDownload(blob, fullFilename);
  }

  /**
   * Genera y descarga un archivo CSV compatible con Excel (BOM UTF-8, separador punto y coma) (respaldo local).
   */
  static exportToCsv(
    data: SimulationExportData[],
    filename: string = 'Analisis_Fraude_SecurityFlow',
    startDate?: string,
    endDate?: string
  ): void {
    const filtered = this.filterByDateRange(data, startDate, endDate);
    const timestampStr = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
    const rangePart = startDate && endDate
      ? `_${startDate}_a_${endDate}`
      : (startDate ? `_desde_${startDate}` : (endDate ? `_hasta_${endDate}` : ''));
    const fullFilename = `${filename}${rangePart}_${timestampStr}.csv`;

    const headers = [
      'ID Transacción',
      'Fecha y Hora',
      'Cliente',
      'Cuenta Origen',
      'Monto (S/)',
      'Canal',
      'País',
      'Ciudad',
      'Dispositivo',
      'Dirección IP',
      'Score de Riesgo',
      'Nivel de Riesgo',
      'Umbral Aplicado',
      '¿Supera Umbral?',
      'Estado Decisión',
      'Reglas Disparadas'
    ];

    const rows = filtered.map(item => [
      item.id,
      this.formatDate(item.timestamp),
      `"${item.customerName.replace(/"/g, '""')}"`,
      item.accountId,
      item.amount.toFixed(2),
      item.channel,
      item.country,
      item.city,
      `"${item.device}"`,
      item.ipAddress,
      item.riskScore,
      item.riskLevel,
      item.threshold,
      item.exceedsThreshold ? 'SÍ (ALERTA)' : 'NO',
      item.status,
      `"${item.reasons.join('; ').replace(/"/g, '""')}"`
    ]);

    const csvContent = '\uFEFF' + [headers.join(';'), ...rows.map(r => r.join(';'))].join('\r\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    this.triggerDownload(blob, fullFilename);
  }

  private static triggerDownload(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    setTimeout(() => {
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    }, 100);
  }

  private static formatDate(date: string | Date): string {
    const d = new Date(date);
    return isNaN(d.getTime()) ? String(date) : d.toLocaleString('es-PE', { hour12: false });
  }

  private static escapeXml(text: string | number): string {
    if (text === null || text === undefined) return '';
    return String(text)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&apos;');
  }

  private static buildExcelXml(data: SimulationExportData[]): string {
    const rowsXml = data.map(item => {
      const isExceeded = item.exceedsThreshold;
      const exceedsStyle = isExceeded ? 'sAlertYes' : 'sAlertNo';
      let statusStyle = 'sText';
      if (item.status === 'BLOQUEADA') statusStyle = 'sStatusBlocked';
      else if (item.status === 'RETENIDA') statusStyle = 'sStatusHeld';
      else if (item.status === 'APROBADA') statusStyle = 'sStatusApproved';

      return `
      <Row>
        <Cell ss:StyleID="sTextBold"><Data ss:Type="String">${this.escapeXml(item.id)}</Data></Cell>
        <Cell ss:StyleID="sText"><Data ss:Type="String">${this.escapeXml(this.formatDate(item.timestamp))}</Data></Cell>
        <Cell ss:StyleID="sText"><Data ss:Type="String">${this.escapeXml(item.customerName)}</Data></Cell>
        <Cell ss:StyleID="sText"><Data ss:Type="String">${this.escapeXml(item.accountId)}</Data></Cell>
        <Cell ss:StyleID="sCurrency"><Data ss:Type="Number">${item.amount}</Data></Cell>
        <Cell ss:StyleID="sCentered"><Data ss:Type="String">${this.escapeXml(item.channel)}</Data></Cell>
        <Cell ss:StyleID="sText"><Data ss:Type="String">${this.escapeXml(item.country)}</Data></Cell>
        <Cell ss:StyleID="sText"><Data ss:Type="String">${this.escapeXml(item.city)}</Data></Cell>
        <Cell ss:StyleID="sText"><Data ss:Type="String">${this.escapeXml(item.device)}</Data></Cell>
        <Cell ss:StyleID="sText"><Data ss:Type="String">${this.escapeXml(item.ipAddress)}</Data></Cell>
        <Cell ss:StyleID="sNumber"><Data ss:Type="Number">${item.riskScore}</Data></Cell>
        <Cell ss:StyleID="sCentered"><Data ss:Type="String">${this.escapeXml(item.riskLevel)}</Data></Cell>
        <Cell ss:StyleID="sNumber"><Data ss:Type="Number">${item.threshold}</Data></Cell>
        <Cell ss:StyleID="${exceedsStyle}"><Data ss:Type="String">${isExceeded ? '⚠️ SÍ (ALERTA)' : 'NO'}</Data></Cell>
        <Cell ss:StyleID="${statusStyle}"><Data ss:Type="String">${this.escapeXml(item.status)}</Data></Cell>
        <Cell ss:StyleID="sText"><Data ss:Type="String">${this.escapeXml(item.reasons.join(', '))}</Data></Cell>
      </Row>`;
    }).join('\n');

    return `<?xml version="1.0" encoding="UTF-8"?>
<?mso-application progid="Excel.Sheet"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:o="urn:schemas-microsoft-com:office:office"
 xmlns:x="urn:schemas-microsoft-com:office:excel"
 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:html="http://www.w3.org/TR/REC-html40">
 <Styles>
  <Style ss:ID="Default" ss:Name="Normal">
   <Alignment ss:Vertical="Center"/>
   <Borders/>
   <Font ss:FontName="Calibri" x:Family="Swiss" ss:Size="11" ss:Color="#000000"/>
   <Interior/>
   <NumberFormat/>
   <Protection/>
  </Style>
  <Style ss:ID="sHeader">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="2" ss:Color="#0D6EFD"/>
   </Borders>
   <Font ss:FontName="Calibri" x:Family="Swiss" ss:Size="11" ss:Color="#FFFFFF" ss:Bold="1"/>
   <Interior ss:Color="#1E3A8A" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="sText">
   <Alignment ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Size="10" ss:Color="#333333"/>
  </Style>
  <Style ss:ID="sTextBold">
   <Alignment ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Size="10" ss:Color="#0F172A" ss:Bold="1"/>
  </Style>
  <Style ss:ID="sCentered">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Size="10" ss:Color="#333333"/>
  </Style>
  <Style ss:ID="sNumber">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Size="10" ss:Color="#333333"/>
   <NumberFormat ss:Format="#,##0"/>
  </Style>
  <Style ss:ID="sCurrency">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Size="10" ss:Color="#0F172A" ss:Bold="1"/>
   <NumberFormat ss:Format="&quot;S/ &quot;#,##0.00"/>
  </Style>
  <Style ss:ID="sAlertYes">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Size="10" ss:Color="#DC2626" ss:Bold="1"/>
   <Interior ss:Color="#FEE2E2" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="sAlertNo">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Size="10" ss:Color="#16A34A"/>
   <Interior ss:Color="#DCFCE7" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="sStatusApproved">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Size="10" ss:Color="#15803D" ss:Bold="1"/>
   <Interior ss:Color="#DCFCE7" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="sStatusHeld">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Size="10" ss:Color="#B45309" ss:Bold="1"/>
   <Interior ss:Color="#FEF3C7" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="sStatusBlocked">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Font ss:FontName="Calibri" ss:Size="10" ss:Color="#B91C1C" ss:Bold="1"/>
   <Interior ss:Color="#FEE2E2" ss:Pattern="Solid"/>
  </Style>
 </Styles>
 <Worksheet ss:Name="Simulaciones Fraude Security Flow">
  <Table ss:DefaultRowHeight="20">
   <Column ss:Width="110"/>
   <Column ss:Width="130"/>
   <Column ss:Width="130"/>
   <Column ss:Width="100"/>
   <Column ss:Width="90"/>
   <Column ss:Width="100"/>
   <Column ss:Width="90"/>
   <Column ss:Width="90"/>
   <Column ss:Width="110"/>
   <Column ss:Width="100"/>
   <Column ss:Width="80"/>
   <Column ss:Width="85"/>
   <Column ss:Width="85"/>
   <Column ss:Width="110"/>
   <Column ss:Width="95"/>
   <Column ss:Width="250"/>
   <Row ss:Height="28">
    <Cell ss:StyleID="sHeader"><Data ss:Type="String">ID Transacción</Data></Cell>
    <Cell ss:StyleID="sHeader"><Data ss:Type="String">Fecha y Hora</Data></Cell>
    <Cell ss:StyleID="sHeader"><Data ss:Type="String">Cliente</Data></Cell>
    <Cell ss:StyleID="sHeader"><Data ss:Type="String">Cuenta Origen</Data></Cell>
    <Cell ss:StyleID="sHeader"><Data ss:Type="String">Monto (S/)</Data></Cell>
    <Cell ss:StyleID="sHeader"><Data ss:Type="String">Canal</Data></Cell>
    <Cell ss:StyleID="sHeader"><Data ss:Type="String">País</Data></Cell>
    <Cell ss:StyleID="sHeader"><Data ss:Type="String">Ciudad</Data></Cell>
    <Cell ss:StyleID="sHeader"><Data ss:Type="String">Dispositivo</Data></Cell>
    <Cell ss:StyleID="sHeader"><Data ss:Type="String">Dirección IP</Data></Cell>
    <Cell ss:StyleID="sHeader"><Data ss:Type="String">Score Riesgo</Data></Cell>
    <Cell ss:StyleID="sHeader"><Data ss:Type="String">Nivel Riesgo</Data></Cell>
    <Cell ss:StyleID="sHeader"><Data ss:Type="String">Umbral</Data></Cell>
    <Cell ss:StyleID="sHeader"><Data ss:Type="String">¿Supera Umbral?</Data></Cell>
    <Cell ss:StyleID="sHeader"><Data ss:Type="String">Estado</Data></Cell>
    <Cell ss:StyleID="sHeader"><Data ss:Type="String">Reglas Disparadas</Data></Cell>
   </Row>
   ${rowsXml}
  </Table>
 </Worksheet>
</Workbook>`;
  }
}
