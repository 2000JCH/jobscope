import { DndContext, closestCenter, MouseSensor, TouchSensor, useSensor, useSensors } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy, useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { GripVertical } from 'lucide-react';
import { useDragOrder } from '../../hooks/useDragOrder';
import { formatDateTime, formatDDay } from '../../utils/dateFormat';
import styles from './ImminentDeadlineList.module.css';

function SortableDeadlineItem({ d, onClickApplication }) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id: d.applicationId });
  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  return (
    <li ref={setNodeRef} style={style} className={styles.item}>
      <span {...attributes} {...listeners} className={styles.dragHandle} onClick={(e) => e.stopPropagation()}>
        <GripVertical size={14} />
      </span>
      <div
        className={styles.info}
        role="button"
        tabIndex={0}
        onClick={() => onClickApplication(d.applicationId)}
        onKeyDown={(e) => e.key === 'Enter' && onClickApplication(d.applicationId)}
      >
        <span className={styles.company}>{d.companyName}</span>
        <span className={styles.deadline}>{formatDateTime(d.deadlineAt)}</span>
      </div>
      {d.dDay !== null && d.dDay !== undefined && (
        <span className={`${styles.dday} ${d.dDay <= 1 ? styles.urgent : ''}`}>
          {formatDDay(d.dDay)}
        </span>
      )}
    </li>
  );
}

function ImminentDeadlineList({ deadlines, onClickApplication }) {
  const { orderedItems, handleDragEnd } = useDragOrder('deadline-order', (deadlines || []).map((d) => ({ ...d, id: d.applicationId })));

  const sensors = useSensors(
    useSensor(MouseSensor),
    useSensor(TouchSensor, { activationConstraint: { delay: 200, tolerance: 5 } }),
  );

  if (!deadlines || deadlines.length === 0) {
    return <p className={styles.empty}>마감 임박 공고가 없습니다.</p>;
  }

  return (
    <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
      <SortableContext items={orderedItems.map((d) => d.id)} strategy={verticalListSortingStrategy}>
        <ul className={styles.list}>
          {orderedItems.map((d) => (
            <SortableDeadlineItem key={d.applicationId} d={d} onClickApplication={onClickApplication} />
          ))}
        </ul>
      </SortableContext>
    </DndContext>
  );
}

export default ImminentDeadlineList;
