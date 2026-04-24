import { useState, useCallback } from 'react';
import { DndContext, closestCenter, MouseSensor, TouchSensor, useSensor, useSensors } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { ArrowLeft, X } from 'lucide-react';
import { useApplicationList } from '../hooks/useApplicationList';
import { useDragOrder } from '../hooks/useDragOrder';
import { deleteApplication } from '../api/application';
import SortableApplicationCard from '../components/application/SortableApplicationCard';
import ApplicationCard from '../components/application/ApplicationCard';
import ApplicationListFilter from '../components/application/ApplicationListFilter';
import ApplicationDetailBottomSheet from '../components/application/ApplicationDetailBottomSheet';
import ApplicationFormBottomSheet from '../components/application/ApplicationFormBottomSheet';
import styles from './ApplicationPage.module.css';

function ApplicationPage() {
  const {
    applications, totalElements, loading, error,
    search, setSearch,
    results, setResults,
    sort, setSort,
    page, setPage,
    totalPages,
    reload,
  } = useApplicationList();

  const { orderedItems, handleDragEnd } = useDragOrder('app-order', applications);

  const [isSearchMode, setIsSearchMode] = useState(false);
  const [inputValue, setInputValue] = useState('');
  const [selectedApplicationId, setSelectedApplicationId] = useState(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [selectedIds, setSelectedIds] = useState(new Set());
  const [deleting, setDeleting] = useState(false);

  const sensors = useSensors(
    useSensor(MouseSensor),
    useSensor(TouchSensor, { activationConstraint: { delay: 200, tolerance: 5 } }),
  );

  const toggleItem = useCallback((id) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }, []);

  const toggleSelectAll = () => {
    setSelectedIds(selectedIds.size === orderedItems.length ? new Set() : new Set(orderedItems.map((a) => a.id)));
  };

  const handleDeleteSelected = async () => {
    if (selectedIds.size === 0) return;
    if (!window.confirm(`선택한 ${selectedIds.size}건을 삭제하시겠습니까?`)) return;
    setDeleting(true);
    try {
      await Promise.all([...selectedIds].map((id) => deleteApplication(id)));
      setIsEditMode(false);
      setSelectedIds(new Set());
      reload();
    } finally {
      setDeleting(false);
    }
  };

  const allSelected = orderedItems.length > 0 && selectedIds.size === orderedItems.length;

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        {isSearchMode ? (
          <>
            <button
              className={styles.backBtn}
              onClick={() => { setIsSearchMode(false); setInputValue(''); setSearch(''); setPage(0); }}
              aria-label="검색 닫기"
            >
              <ArrowLeft size={20} />
            </button>
            <input
              className={styles.searchHeaderInput}
              type="text"
              placeholder="회사명 검색 후 엔터"
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') { setSearch(inputValue); setPage(0); }
              }}
              autoFocus
            />
            {inputValue && (
              <button
                className={styles.clearBtn}
                onClick={() => { setInputValue(''); setSearch(''); setPage(0); }}
                aria-label="검색어 지우기"
              >
                <X size={16} />
              </button>
            )}
          </>
        ) : (
          <>
            <h1 className={styles.title}>지원현황</h1>
            <div className={styles.headerActions}>
              {!isEditMode && (
                <>
                  {applications.length > 0 && (
                    <button className={styles.editBtn} onClick={() => { setIsEditMode(true); setSelectedIds(new Set()); }}>
                      편집
                    </button>
                  )}
                  <button className={styles.addBtn} onClick={() => setShowCreateForm(true)}>+ 등록</button>
                </>
              )}
              {isEditMode && (
                <>
                  <button className={styles.editActionBtn} onClick={toggleSelectAll}>
                    {allSelected ? '전체 해제' : '전체 선택'}
                  </button>
                  <button className={styles.editActionBtn} onClick={() => { setIsEditMode(false); setSelectedIds(new Set()); }}>
                    취소
                  </button>
                  <button
                    className={styles.deleteBtn}
                    disabled={selectedIds.size === 0 || deleting}
                    onClick={handleDeleteSelected}
                  >
                    삭제 ({selectedIds.size})
                  </button>
                </>
              )}
            </div>
          </>
        )}
      </div>

      <ApplicationListFilter
        results={results}
        onResultsChange={(v) => { setResults(v); setPage(0); }}
        sort={sort}
        onSortChange={(v) => { setSort(v); setPage(0); }}
        onSearchOpen={() => setIsSearchMode(true)}
      />

      <div className={styles.count}>{totalElements}건</div>

      {loading && <p className={styles.status}>불러오는 중...</p>}
      {error && <p className={styles.statusError}>{error}</p>}

      {!loading && !error && (
        isEditMode ? (
          <div className={styles.list}>
            {orderedItems.length === 0
              ? <p className={styles.empty}>지원 내역이 없습니다.</p>
              : orderedItems.map((app) => (
                <ApplicationCard
                  key={app.id}
                  application={app}
                  isEditMode={true}
                  isSelected={selectedIds.has(app.id)}
                  onToggle={() => toggleItem(app.id)}
                />
              ))
            }
          </div>
        ) : (
          <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
            <SortableContext items={orderedItems.map((a) => a.id)} strategy={verticalListSortingStrategy}>
              <div className={styles.list}>
                {orderedItems.length === 0
                  ? <p className={styles.empty}>지원 내역이 없습니다.</p>
                  : orderedItems.map((app) => (
                    <SortableApplicationCard
                      key={app.id}
                      application={app}
                      onClick={() => setSelectedApplicationId(app.id)}
                    />
                  ))
                }
              </div>
            </SortableContext>
          </DndContext>
        )
      )}

      {totalPages > 1 && (
        <div className={styles.pagination}>
          <button disabled={page === 0} onClick={() => setPage(page - 1)}>이전</button>
          <span>{page + 1} / {totalPages}</span>
          <button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>다음</button>
        </div>
      )}

      <ApplicationDetailBottomSheet
        isOpen={!!selectedApplicationId}
        applicationId={selectedApplicationId}
        onClose={() => setSelectedApplicationId(null)}
        onDeleted={() => { setSelectedApplicationId(null); reload(); }}
        onUpdated={reload}
      />

      <ApplicationFormBottomSheet
        isOpen={showCreateForm}
        onClose={() => setShowCreateForm(false)}
        onSuccess={reload}
      />
    </div>
  );
}

export default ApplicationPage;