import { useEffect } from 'react';
import styles from './BottomSheet.module.css';

let lockCount = 0;

function BottomSheet({ isOpen, onClose, title, children }) {
  useEffect(() => {
    if (isOpen) {
      lockCount += 1;
      document.body.style.overflow = 'hidden';
      return () => {
        lockCount -= 1;
        if (lockCount === 0) {
          document.body.style.overflow = '';
        }
      };
    }
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.sheet} onClick={(e) => e.stopPropagation()}>
        <div className={styles.handle} />
        {title && <h2 className={styles.title}>{title}</h2>}
        <div className={styles.content}>{children}</div>
      </div>
    </div>
  );
}

export default BottomSheet;
