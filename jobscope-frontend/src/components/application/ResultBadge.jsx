import { RESULT_LABEL } from '../../utils/applicationEnum';
import styles from './ResultBadge.module.css';

function ResultBadge({ result }) {
  return (
    <span className={`${styles.badge} ${styles[result]}`}>
      {RESULT_LABEL[result] ?? result}
    </span>
  );
}

export default ResultBadge;
