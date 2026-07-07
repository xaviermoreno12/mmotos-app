import { useQuery } from '@tanstack/react-query';
import { resumenCaja } from '../api/ventas';

export function useCajaResumen(fecha?: string) {
  return useQuery({
    queryKey: ['caja', 'resumen', fecha],
    queryFn: () => resumenCaja(fecha),
    staleTime: 30 * 1000,
  });
}
