import { useState, useMemo, useCallback } from 'react';
import { arrayMove } from '@dnd-kit/sortable';

export function useDragOrder(storageKey, items) {
  const [storedIds, setStoredIds] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem(storageKey)) || [];
    } catch {
      return [];
    }
  });

  const orderedItems = useMemo(() => {
    if (storedIds.length === 0) return items;
    const posMap = new Map(storedIds.map((id, i) => [id, i]));
    return [...items].sort((a, b) => {
      const ai = posMap.has(a.id) ? posMap.get(a.id) : Infinity;
      const bi = posMap.has(b.id) ? posMap.get(b.id) : Infinity;
      return ai - bi;
    });
  }, [items, storedIds]);

  const handleDragEnd = useCallback((event) => {
    const { active, over } = event;
    if (!over || active.id === over.id) return;

    const currentIds = items.map((i) => i.id);
    const orderedCurrentIds = storedIds.length === 0
      ? currentIds
      : [...currentIds].sort((a, b) => {
        const ai = storedIds.indexOf(a);
        const bi = storedIds.indexOf(b);
        return (ai === -1 ? Infinity : ai) - (bi === -1 ? Infinity : bi);
      });

    const activeId = typeof active.id === 'string' ? Number(active.id) : active.id;
    const overId = typeof over.id === 'string' ? Number(over.id) : over.id;
    const oldIdx = orderedCurrentIds.indexOf(activeId);
    const newIdx = orderedCurrentIds.indexOf(overId);
    if (oldIdx === -1 || newIdx === -1) return;

    const reordered = arrayMove(orderedCurrentIds, oldIdx, newIdx);

    setStoredIds((prev) => {
      const otherIds = prev.filter((id) => !currentIds.includes(id));
      let insertPos = 0;
      for (let i = 0; i < prev.length; i++) {
        if (currentIds.includes(prev[i])) break;
        insertPos++;
      }
      const merged = [...otherIds.slice(0, insertPos), ...reordered, ...otherIds.slice(insertPos)];
      localStorage.setItem(storageKey, JSON.stringify(merged));
      return merged;
    });
  }, [items, storedIds, storageKey]);

  return { orderedItems, handleDragEnd };
}