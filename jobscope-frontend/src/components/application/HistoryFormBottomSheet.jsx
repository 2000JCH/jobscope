import { useState, useEffect } from 'react';
import { createHistory, updateHistory } from '../../api/application';
import { STAGE_RESULT_OPTIONS } from '../../utils/applicationEnum';
import BottomSheet from '../common/BottomSheet';
import styles from './HistoryFormBottomSheet.module.css';

function HistoryFormBottomSheet({ isOpen, onClose, onSuccess, applicationId, initialData }) {
  const isEdit = !!initialData;

  const [stage, setStage] = useState('');
  const [stageResult, setStageResult] = useState('PENDING');
  const [scheduledAt, setScheduledAt] = useState('');
  const [completedAt, setCompletedAt] = useState('');
  const [memo, setMemo] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isOpen && initialData) {
      setStage(initialData.stage ?? '');
      setStageResult(initialData.stageResult ?? 'PENDING');
      setScheduledAt(initialData.scheduledAt ? initialData.scheduledAt.slice(0, 16) : '');
      setCompletedAt(initialData.completedAt ? initialData.completedAt.slice(0, 16) : '');
      setMemo(initialData.memo ?? '');
    } else if (isOpen && !initialData) {
      setStage('');
      setStageResult('PENDING');
      setScheduledAt('');
      setCompletedAt('');
      setMemo('');
    }
    setError('');
  }, [isOpen, initialData]);

  function handleSubmit(e) {
    e.preventDefault();
    if (!stage.trim()) { setError('단계명을 입력해주세요.'); return; }

    const payload = {
      stage: stage.trim(),
      stageResult,
      scheduledAt: scheduledAt || null,
      completedAt: completedAt || null,
      memo: memo.trim() || null,
    };

    setSubmitting(true);
    const request = isEdit
      ? updateHistory(applicationId, initialData.id, payload)
      : createHistory(applicationId, payload);

    request
      .then(() => { onSuccess(); onClose(); })
      .catch(() => setError('저장에 실패했습니다. 다시 시도해주세요.'))
      .finally(() => setSubmitting(false));
  }

  return (
    <BottomSheet isOpen={isOpen} onClose={onClose} title={isEdit ? '단계 수정' : '단계 추가'}>
      <form className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.field}>
          <label className={styles.label}>단계명 *</label>
          <input className={styles.input} value={stage} onChange={(e) => setStage(e.target.value)} placeholder="예) 1차 면접" />
        </div>
        <div className={styles.field}>
          <label className={styles.label}>결과</label>
          <div className={styles.chips}>
            {STAGE_RESULT_OPTIONS.map((opt) => (
              <button
                key={opt.value}
                type="button"
                className={`${styles.chip} ${stageResult === opt.value ? styles.chipActive : ''}`}
                onClick={() => setStageResult(opt.value)}
              >
                {opt.label}
              </button>
            ))}
          </div>
        </div>
        <div className={styles.field}>
          <label className={styles.label}>예정 일시</label>
          <input className={styles.input} type="datetime-local" value={scheduledAt} onChange={(e) => setScheduledAt(e.target.value)} />
        </div>
        <div className={styles.field}>
          <label className={styles.label}>완료 일시</label>
          <input className={styles.input} type="datetime-local" value={completedAt} onChange={(e) => setCompletedAt(e.target.value)} />
        </div>
        <div className={styles.field}>
          <label className={styles.label}>메모</label>
          <textarea className={styles.textarea} value={memo} onChange={(e) => setMemo(e.target.value)} placeholder="면접 질문, 준비 내용 등" rows={3} />
        </div>
        {error && <p className={styles.error}>{error}</p>}
        <button className={styles.submitBtn} type="submit" disabled={submitting}>
          {submitting ? '저장 중...' : '저장'}
        </button>
      </form>
    </BottomSheet>
  );
}

export default HistoryFormBottomSheet;