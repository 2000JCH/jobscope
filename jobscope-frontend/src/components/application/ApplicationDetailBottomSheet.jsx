import { useEffect, useState } from 'react';
import { deleteApplication, deleteHistory } from '../../api/application';
import { useApplicationDetail } from '../../hooks/useApplicationDetail';
import { formatDate, formatDateTime } from '../../utils/dateFormat';
import BottomSheet from '../common/BottomSheet';
import ConfirmDialog from '../common/ConfirmDialog';
import ResultBadge from './ResultBadge';
import HistoryTimeline from './HistoryTimeline';
import ApplicationFormBottomSheet from './ApplicationFormBottomSheet';
import HistoryFormBottomSheet from './HistoryFormBottomSheet';
import styles from './ApplicationDetailBottomSheet.module.css';

function ApplicationDetailBottomSheet({ isOpen, applicationId, onClose, onDeleted, onUpdated }) {
  const { detail, loading, error, load, reload } = useApplicationDetail();

  const [showEditForm, setShowEditForm] = useState(false);
  const [showHistoryForm, setShowHistoryForm] = useState(false);
  const [editingHistory, setEditingHistory] = useState(null);
  const [confirmDeleteApp, setConfirmDeleteApp] = useState(false);
  const [confirmDeleteHistoryId, setConfirmDeleteHistoryId] = useState(null);

  useEffect(() => {
    if (isOpen && applicationId) {
      load(applicationId);
    }
  }, [isOpen, applicationId, load]);

  function handleDeleteApp() {
    deleteApplication(applicationId)
      .then(() => { setConfirmDeleteApp(false); onDeleted(); onClose(); })
      .catch(() => { setConfirmDeleteApp(false); alert('삭제에 실패했습니다.'); });
  }

  function handleDeleteHistory(historyId) {
    setConfirmDeleteHistoryId(historyId);
  }

  function confirmHistoryDelete() {
    deleteHistory(applicationId, confirmDeleteHistoryId)
      .then(() => { setConfirmDeleteHistoryId(null); reload(); })
      .catch(() => { setConfirmDeleteHistoryId(null); alert('히스토리 삭제에 실패했습니다.'); });
  }

  function handleEditHistory(history) {
    setEditingHistory(history);
    setShowHistoryForm(true);
  }

  function handleAddHistory() {
    setEditingHistory(null);
    setShowHistoryForm(true);
  }

  return (
    <>
      <BottomSheet isOpen={isOpen && !showEditForm && !showHistoryForm} onClose={onClose}>
        {loading && <p className={styles.loading}>불러오는 중...</p>}
        {error && <p className={styles.error}>{error}</p>}
        {detail && (
          <div className={styles.wrapper}>
            <div className={styles.header}>
              <div>
                <h2 className={styles.company}>{detail.companyName}</h2>
                <p className={styles.position}>{detail.jobPosition}</p>
              </div>
              <ResultBadge result={detail.result} />
            </div>

            <div className={styles.metaList}>
              <div className={styles.metaRow}>
                <span className={styles.metaKey}>지원일</span>
                <span>{formatDate(detail.appliedAt)}</span>
              </div>
              {detail.deadlineAt && (
                <div className={styles.metaRow}>
                  <span className={styles.metaKey}>마감일</span>
                  <span>{formatDateTime(detail.deadlineAt)}</span>
                </div>
              )}
              {detail.jobPostingUrl && (
                <div className={styles.metaRow}>
                  <span className={styles.metaKey}>공고</span>
                  <a className={styles.link} href={detail.jobPostingUrl} target="_blank" rel="noreferrer">링크 열기</a>
                </div>
              )}
              {detail.memo && (
                <div className={styles.metaRow}>
                  <span className={styles.metaKey}>메모</span>
                  <span className={styles.metaValue}>{detail.memo}</span>
                </div>
              )}
              {detail.retrospective && (
                <div className={styles.metaRow}>
                  <span className={styles.metaKey}>탈락 회고</span>
                  <span className={styles.metaValue}>{detail.retrospective}</span>
                </div>
              )}
            </div>

            <div className={styles.section}>
              <div className={styles.sectionHeader}>
                <h3 className={styles.sectionTitle}>전형 단계</h3>
                <button className={styles.addBtn} onClick={handleAddHistory}>+ 추가</button>
              </div>
              <HistoryTimeline
                histories={detail.histories}
                onEdit={handleEditHistory}
                onDelete={handleDeleteHistory}
              />
            </div>

            <div className={styles.footerActions}>
              <button className={styles.editBtn} onClick={() => setShowEditForm(true)}>수정</button>
              <button className={styles.deleteBtn} onClick={() => setConfirmDeleteApp(true)}>삭제</button>
            </div>
          </div>
        )}
      </BottomSheet>

      <ApplicationFormBottomSheet
        isOpen={showEditForm}
        onClose={() => setShowEditForm(false)}
        initialData={detail}
        onSuccess={() => { reload(); onUpdated(); }}
      />

      <HistoryFormBottomSheet
        isOpen={showHistoryForm}
        onClose={() => { setShowHistoryForm(false); setEditingHistory(null); }}
        applicationId={applicationId}
        initialData={editingHistory}
        onSuccess={() => reload()}
      />

      <ConfirmDialog
        isOpen={confirmDeleteApp}
        message="지원 내역을 삭제하시겠습니까?"
        onConfirm={handleDeleteApp}
        onCancel={() => setConfirmDeleteApp(false)}
      />

      <ConfirmDialog
        isOpen={!!confirmDeleteHistoryId}
        message="전형 단계를 삭제하시겠습니까?"
        onConfirm={confirmHistoryDelete}
        onCancel={() => setConfirmDeleteHistoryId(null)}
      />
    </>
  );
}

export default ApplicationDetailBottomSheet;