import { useState, useCallback } from 'react';
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import { useCalendarEvents } from '../hooks/useCalendarEvents';
import ApplicationDetailBottomSheet from '../components/application/ApplicationDetailBottomSheet';
import styles from './CalendarPage.module.css';

function CalendarPage() {
  const { events, loading, load } = useCalendarEvents();
  const [selectedApplicationId, setSelectedApplicationId] = useState(null);

  const handleDatesSet = useCallback((info) => {
    const startDate = info.startStr.slice(0, 10);
    const exclusiveEnd = new Date(info.end);
    exclusiveEnd.setDate(exclusiveEnd.getDate() - 1);
    const endDate = exclusiveEnd.toISOString().slice(0, 10);
    load(startDate, endDate);
  }, [load]);

  function handleEventClick(info) {
    const applicationId = info.event.extendedProps.applicationId;
    setSelectedApplicationId(applicationId);
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>캘린더</h1>
      {loading && <p className={styles.loading}>불러오는 중...</p>}
      <div className={styles.calendar}>
        <FullCalendar
          plugins={[dayGridPlugin, interactionPlugin]}
          initialView="dayGridMonth"
          locale="ko"
          headerToolbar={{ left: 'prev', center: 'title', right: 'next' }}
          events={events}
          datesSet={handleDatesSet}
          eventClick={handleEventClick}
          height="auto"
          eventDisplay="block"
          dayMaxEvents={3}
        />
      </div>

      <div className={styles.legend}>
        <span className={styles.legendItem}>
          <span className={`${styles.dot} ${styles.interview}`} />면접/코테
        </span>
        <span className={styles.legendItem}>
          <span className={`${styles.dot} ${styles.deadline}`} />서류 마감
        </span>
      </div>

      <ApplicationDetailBottomSheet
        isOpen={!!selectedApplicationId}
        applicationId={selectedApplicationId}
        onClose={() => setSelectedApplicationId(null)}
        onDeleted={() => setSelectedApplicationId(null)}
        onUpdated={() => {}}
      />
    </div>
  );
}

export default CalendarPage;