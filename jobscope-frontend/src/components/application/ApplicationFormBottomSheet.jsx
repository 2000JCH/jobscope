import { useState, useEffect } from 'react';
import { createApplication, updateApplication } from '../../api/application';
import { RESULT_OPTIONS } from '../../utils/applicationEnum';
import BottomSheet from '../common/BottomSheet';
import styles from './ApplicationFormBottomSheet.module.css';

function ApplicationFormBottomSheet({ isOpen, onClose, onSuccess, initialData }) {
  const isEdit = !!initialData;

  const [companyName, setCompanyName] = useState('');
  const [jobPosition, setJobPosition] = useState('');
  const [appliedAt, setAppliedAt] = useState('');
  const [deadlineAt, setDeadlineAt] = useState('');
  const [alarmEnabled, setAlarmEnabled] = useState(true);
  const [jobPostingUrl, setJobPostingUrl] = useState('');
  const [memo, setMemo] = useState('');
  const [result, setResult] = useState('');
  const [retrospective, setRetrospective] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isOpen && initialData) {
      setCompanyName(initialData.companyName ?? '');
      setJobPosition(initialData.jobPosition ?? '');
      setAppliedAt(initialData.appliedAt ?? '');
      setDeadlineAt(initialData.deadlineAt ? initialData.deadlineAt.slice(0, 16) : '');
      setAlarmEnabled(initialData.alarmEnabled ?? true);
      setJobPostingUrl(initialData.jobPostingUrl ?? '');
      setMemo(initialData.memo ?? '');
      setResult(initialData.result ?? '');
      setRetrospective(initialData.retrospective ?? '');
    } else if (isOpen && !initialData) {
      setCompanyName('');
      setJobPosition('');
      setAppliedAt('');
      setDeadlineAt('');
      setAlarmEnabled(true);
      setJobPostingUrl('');
      setMemo('');
      setResult('');
      setRetrospective('');
    }
    setError('');
  }, [isOpen, initialData]);

  function handleSubmit(e) {
    e.preventDefault();
    if (!companyName.trim()) { setError('회사명을 입력해주세요.'); return; }
    if (!jobPosition.trim()) { setError('직군을 입력해주세요.'); return; }
    if (!appliedAt) { setError('지원 날짜를 입력해주세요.'); return; }

    const payload = {
      companyName: companyName.trim(),
      jobPosition: jobPosition.trim(),
      appliedAt,
      deadlineAt: deadlineAt || null,
      alarmEnabled,
      jobPostingUrl: jobPostingUrl.trim() || null,
      memo: memo.trim() || null,
    };

    if (isEdit) {
      payload.result = result || null;
      payload.retrospective = result === 'FAILED' ? retrospective.trim() || null : null;
    }

    setSubmitting(true);
    const request = isEdit
      ? updateApplication(initialData.id, payload)
      : createApplication(payload);

    request
      .then(() => { onSuccess(); onClose(); })
      .catch(() => setError('저장에 실패했습니다. 다시 시도해주세요.'))
      .finally(() => setSubmitting(false));
  }

  return (
    <BottomSheet isOpen={isOpen} onClose={onClose} title={isEdit ? '지원 수정' : '지원 등록'}>
      <form className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.field}>
          <label className={styles.label}>회사명 *</label>
          <input className={styles.input} value={companyName} onChange={(e) => setCompanyName(e.target.value)} placeholder="예) 네이버" />
        </div>
        <div className={styles.field}>
          <label className={styles.label}>직군 *</label>
          <input className={styles.input} value={jobPosition} onChange={(e) => setJobPosition(e.target.value)} placeholder="예) 백엔드" />
        </div>
        <div className={styles.field}>
          <label className={styles.label}>지원 날짜 *</label>
          <input className={styles.input} type="date" value={appliedAt} onChange={(e) => setAppliedAt(e.target.value)} />
        </div>
        <div className={styles.field}>
          <label className={styles.label}>서류 마감</label>
          <input className={styles.input} type="datetime-local" value={deadlineAt} onChange={(e) => setDeadlineAt(e.target.value)} />
        </div>
        <div className={styles.field}>
          <label className={styles.label}>채용 공고 URL</label>
          <input className={styles.input} type="url" value={jobPostingUrl} onChange={(e) => setJobPostingUrl(e.target.value)} placeholder="https://" />
        </div>
        <div className={styles.field}>
          <label className={styles.label}>메모</label>
          <textarea className={styles.textarea} value={memo} onChange={(e) => setMemo(e.target.value)} placeholder="준비 사항 등" rows={3} />
        </div>
        {isEdit && (
          <div className={styles.field}>
            <label className={styles.label}>결과</label>
            <select className={styles.input} value={result} onChange={(e) => setResult(e.target.value)}>
              <option value="">선택 안 함</option>
              {RESULT_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
          </div>
        )}
        {isEdit && result === 'FAILED' && (
          <div className={styles.field}>
            <label className={styles.label}>탈락 회고</label>
            <textarea className={styles.textarea} value={retrospective} onChange={(e) => setRetrospective(e.target.value)} placeholder="이번 지원에서 배운 점이나 느낀 점을 적어보세요." rows={4} />
          </div>
        )}
        <div className={styles.toggleRow}>
          <span className={styles.label}>알림</span>
          <button
            type="button"
            className={`${styles.toggle} ${alarmEnabled ? styles.toggleOn : ''}`}
            onClick={() => setAlarmEnabled(!alarmEnabled)}
          >
            {alarmEnabled ? 'ON' : 'OFF'}
          </button>
        </div>
        {error && <p className={styles.error}>{error}</p>}
        <button className={styles.submitBtn} type="submit" disabled={submitting}>
          {submitting ? '저장 중...' : '저장'}
        </button>
      </form>
    </BottomSheet>
  );
}

export default ApplicationFormBottomSheet;
