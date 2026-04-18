import { create } from 'zustand';

export const useApplicationStore = create((set) => ({
  selectedApplicationId: null,
  setSelectedApplicationId: (id) => set({ selectedApplicationId: id }),
  clearSelectedApplication: () => set({ selectedApplicationId: null }),
}));
