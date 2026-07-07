import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { obtenerCajaActiva, abrirCaja, cerrarCaja } from '../api/ventas';
import type { AbrirCajaRequest, CerrarCajaRequest } from '../types';

export function useCajaActiva() {
  return useQuery({
    queryKey: ['caja', 'activa'],
    queryFn: obtenerCajaActiva,
    refetchInterval: 30_000, // Refrescar cada 30s para KPIs en tiempo real
  });
}

export function useAbrirCaja() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: AbrirCajaRequest) => abrirCaja(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['caja'] });
    },
  });
}

export function useCerrarCaja() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: CerrarCajaRequest) => cerrarCaja(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['caja'] });
    },
  });
}
