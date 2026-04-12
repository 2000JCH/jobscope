import { useState } from 'react';
import { useApplicationList } from '../hooks/useApplicationList';
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

  const [selectedApplicationId, setSelectedApplicationId] = useState(null);
  const [showCreateForm, setShowCreateForm] = useState(false);

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1 className={styles.title}>지원현황</h1>
        <button className={styles.addBtn} onClick={() => setShowCreateForm(true)}>+ 등록</button>
      </div>

      <ApplicationListFilter
        search={search}
        onSearchChange={(v) => { setSearch(v); setPage(0); }}
        results={results}
        onResultsChange={(v) => { setResults(v); setPage(0); }}
        sort={sort}
        onSortChange={(v) => { setSort(v); setPage(0); }}
      />

      <div className={styles.count}>{totalElements}건</div>

      {loading && <p className={styles.status}>불러오는 중...</p>}
      {error && <p className={styles.statusError}>{error}</p>}

      {!loading && !error && (
        <div className={styles.list}>
          {applications.length === 0
            ? <p className={styles.empty}>지원 내역이 없습니다.</p>
            : applications.map((app) => (
              <ApplicationCard
                key={app.id}
                application={app}
                onClick={() => setSelectedApplicationId(app.id)}
              />
            ))
          }
        </div>
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