import { useState } from 'react';
import styles from './AdminNoticeForm.module.css';

function AdminNoticeForm({ onSubmit, initial = null, onCancel }) {
  const [title, setTitle] = useState(initial?.title ?? '');
  const [content, setContent] = useState(initial?.content ?? '');
  const [active, setActive] = useState(initial?.isActive ?? true);
  const [loading, setLoading] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!title.trim() || !content.trim()) return;
    setLoading(true);
    onSubmit({ title, content, active })
      .then(() => {
        if (!initial) { setTitle(''); setContent(''); setActive(true); }
      })
      .finally(() => setLoading(false));
  };

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      <input
        className={styles.input}
        placeholder="제목"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        maxLength={200}
        required
      />
      <textarea
        className={styles.textarea}
        placeholder="내용"
        value={content}
        onChange={(e) => setContent(e.target.value)}
        rows={4}
        required
      />
      <label className={styles.toggle}>
        <input
          type="checkbox"
          checked={active}
          onChange={(e) => setActive(e.target.checked)}
        />
        <span>활성화</span>
      </label>
      <div className={styles.actions}>
        <button type="submit" className={styles.submitBtn} disabled={loading}>
          {loading ? '저장 중...' : initial ? '수정' : '등록'}
        </button>
        {onCancel && (
          <button type="button" className={styles.cancelBtn} onClick={onCancel}>취소</button>
        )}
      </div>
    </form>
  );
}

export default AdminNoticeForm;