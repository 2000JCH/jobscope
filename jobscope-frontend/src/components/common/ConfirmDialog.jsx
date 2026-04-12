import styles from './ConfirmDialog.module.css';

function ConfirmDialog({ isOpen, message, onConfirm, onCancel }) {
  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onCancel}>
      <div className={styles.dialog} onClick={(e) => e.stopPropagation()}>
        <p className={styles.message}>{message}</p>
        <div className={styles.actions}>
          <button className={styles.cancelBtn} onClick={onCancel}>취소</button>
          <button className={styles.confirmBtn} onClick={onConfirm}>확인</button>
        </div>
      </div>
    </div>
  );
}

export default ConfirmDialog;
