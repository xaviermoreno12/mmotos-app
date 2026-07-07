import { create } from 'zustand';
import type { CartItem, ProductoDTO } from '../types';

interface CartStore {
  items: CartItem[];
  tipoFactura: 'A' | 'B' | 'C';
  metodoPago: string;
  montoPago: number;
  addItem: (producto: ProductoDTO) => void;
  loadItems: (items: CartItem[]) => void;
  removeItem: (productoId: string) => void;
  updateCantidad: (productoId: string, cantidad: number) => void;
  setTipoFactura: (tipo: 'A' | 'B' | 'C') => void;
  setMetodoPago: (metodo: string) => void;
  setMontoPago: (monto: number) => void;
  clearCart: () => void;
  getTotal: () => number;
}

export const useCartStore = create<CartStore>((set, get) => ({
  items: [],
  tipoFactura: 'B',
  metodoPago: 'EFECTIVO',
  montoPago: 0,

  addItem: (producto) => {
    set((state) => {
      const existing = state.items.find((i) => i.productoId === producto.id);
      if (existing) {
        return {
          items: state.items.map((i) =>
            i.productoId === producto.id
              ? { ...i, cantidad: i.cantidad + 1 }
              : i,
          ),
        };
      }
      return {
        items: [
          ...state.items,
          {
            productoId: producto.id,
            sku: producto.sku,
            nombre: producto.nombre,
            cantidad: 1,
            precioUnitario: producto.precioEnPesos,
          },
        ],
      };
    });
  },

  loadItems: (items) => set({ items, montoPago: 0 }),

  removeItem: (productoId) =>
    set((state) => ({ items: state.items.filter((i) => i.productoId !== productoId) })),

  updateCantidad: (productoId, cantidad) => {
    if (cantidad <= 0) {
      get().removeItem(productoId);
      return;
    }
    set((state) => ({
      items: state.items.map((i) =>
        i.productoId === productoId ? { ...i, cantidad } : i,
      ),
    }));
  },

  setTipoFactura: (tipo) => set({ tipoFactura: tipo }),
  setMetodoPago: (metodo) => set({ metodoPago: metodo }),
  setMontoPago: (monto) => set({ montoPago: monto }),
  clearCart: () => set({ items: [], montoPago: 0 }),

  getTotal: () => {
    const { items } = get();
    return items.reduce((sum, i) => sum + i.cantidad * i.precioUnitario, 0);
  },
}));
