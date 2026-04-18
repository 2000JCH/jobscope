import { STAGE_RESULT_LABEL } from '../../utils/applicationEnum';
import styles from './StageResultBadge.module.css';

function StageResultBadge({ stageResult }) {
  return (
    <span className={`${styles.badge} ${styles[stageResult]}`}>
      {STAGE_RESULT_LABEL[stageResult] ?? stageResult}
    </span>
  );
}

export default StageResultBadge;
