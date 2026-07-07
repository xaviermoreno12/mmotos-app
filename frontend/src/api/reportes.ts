import { client } from './client';

function descargar(blob: Blob, nombre: string) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = nombre;
  a.click();
  URL.revokeObjectURL(url);
}

export async function descargarReporteVentas(desde: string, hasta: string, formato: string) {
  const res = await client.get('/api/reportes/ventas', {
    params: { desde, hasta, formato },
    responseType: 'blob',
  });
  const ext = formato === 'pdf' ? '.pdf' : '.xlsx';
  descargar(res.data as Blob, `ventas-${desde}-${hasta}${ext}`);
}

export async function descargarReporteStockBajo(formato: string) {
  const res = await client.get('/api/reportes/stock-bajo', {
    params: { formato },
    responseType: 'blob',
  });
  const ext = formato === 'pdf' ? '.pdf' : '.xlsx';
  descargar(res.data as Blob, `stock-bajo${ext}`);
}

export async function descargarReporteCaja(fecha: string, formato: string) {
  const res = await client.get('/api/reportes/caja', {
    params: { fecha, formato },
    responseType: 'blob',
  });
  const ext = formato === 'pdf' ? '.pdf' : '.xlsx';
  descargar(res.data as Blob, `caja-${fecha}${ext}`);
}

export async function descargarReporteListaProductos(formato: string) {
  const res = await client.get('/api/reportes/lista-productos', {
    params: { formato },
    responseType: 'blob',
  });
  const ext = formato === 'pdf' ? '.pdf' : '.xlsx';
  descargar(res.data as Blob, `lista-productos${ext}`);
}
