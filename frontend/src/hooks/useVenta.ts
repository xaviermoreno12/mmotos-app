import { useMutation } from '@tanstack/react-query';
import { realizarVenta } from '../api/ventas';
import type { VentaRequest, VentaResponse } from '../types';

export function useVenta(options?: {
  onSuccess?: (data: VentaResponse) => void;
  onError?: (error: Error) => void;
}) {
  return useMutation<VentaResponse, Error, VentaRequest>({
    mutationFn: realizarVenta,
    onSuccess: options?.onSuccess,
    onError: options?.onError,
  });
}
