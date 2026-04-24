import { Search } from 'lucide-react';
import { SORT_OPTIONS, RESULT_OPTIONS } from '../../utils/applicationEnum';
import styles from './ApplicationListFilter.module.css';

function ApplicationListFilter({ results, onResultsChange, sort, onSortChange, onSearchOpen }) {
  function toggleResult(value) {
    if (results.includes(value)) {
      onResultsChange(results.filter((r) => r !== value));
    } else {
      onResultsChange([...results, value]);
    }
  }

  return (
    <div className={styles.wrapper}>
      <div className={styles.chips}>
        {RESULT_OPTIONS.map((opt) => (
          <button
            key={opt.value}
            className={`${styles.chip} ${results.includes(opt.value) ? styles.chipActive : ''}`}
            onClick={() => toggleResult(opt.value)}
          >
            {opt.label}
          </button>
        ))}
      </div>
      <div className={styles.rightControls}>
        <button className={styles.searchIconBtn} onClick={onSearchOpen} aria-label="검색">
          <Search size={16} />
        </button>
        <select
          className={styles.sortSelect}
          value={sort}
          onChange={(e) => onSortChange(e.target.value)}
        >
          {SORT_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>
      </div>
    </div>
  );
}

export default ApplicationListFilter;
