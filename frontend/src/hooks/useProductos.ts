import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { buscarProductos, crearProducto } from '../api/productos';

export function useBuscarProductos(termino: string) {
  return useQuery({
    queryKey: ['productos', 'buscar', termino],
    queryFn: () => buscarProductos(termino),
    enabled: termino.trim().length >= 2,
    staleTime: 30 * 1000,
  });
}

export function useCrearProducto() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: crearProducto,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['productos'] });
    },
  });
}
