import { useQuery } from '@tanstack/react-query';
import { listarVentas } from '../api/ventas';

export function useVentasList(desde?: string, hasta?: string) {
  return useQuery({
    queryKey: ['ventas', 'lista', desde, hasta],
    queryFn: () => listarVentas(desde, hasta),
    staleTime: 30 * 1000,
  });
}
