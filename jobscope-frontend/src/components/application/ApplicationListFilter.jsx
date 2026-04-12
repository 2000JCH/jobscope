import { SORT_OPTIONS, RESULT_OPTIONS } from '../../utils/applicationEnum';
import styles from './ApplicationListFilter.module.css';

function ApplicationListFilter({ search, onSearchChange, results, onResultsChange, sort, onSortChange }) {
  function toggleResult(value) {
    if (results.includes(value)) {
      onResultsChange(results.filter((r) => r !== value));
    } else {
      onResultsChange([...results, value]);
    }
  }

  return (
    <div className={styles.wrapper}>
      <input
        className={styles.searchInput}
        type="text"
        placeholder="회사명 검색"
        value={search}
        onChange={(e) => onSearchChange(e.target.value)}
      />
      <div className={styles.row}>
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
