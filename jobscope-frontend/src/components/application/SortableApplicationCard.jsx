import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import ApplicationCard from './ApplicationCard';

function SortableApplicationCard({ application, onClick }) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id: application.id });
  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
    zIndex: isDragging ? 10 : undefined,
  };

  return (
    <ApplicationCard
      application={application}
      onClick={onClick}
      dragRef={setNodeRef}
      dragStyle={style}
      dragHandleProps={{ ...attributes, ...listeners }}
    />
  );
}

export default SortableApplicationCard;