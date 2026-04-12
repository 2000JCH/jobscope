import { useState, useCallback } from 'react';
import { fetchCalendar } from '../api/application';

export function useCalendarEvents() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(false);

  const load = useCallback((startDate, endDate) => {
    setLoading(true);
    fetchCalendar({ startDate, endDate })
      .then((res) => {
        const calendarDates = res.data.data;
        const mapped = calendarDates.flatMap((day) =>
          day.events.map((ev) => ({
            id: `${ev.type}-${ev.applicationId}-${day.date}-${ev.time}`,
            title: `${ev.companyName} ${ev.label}`,
            start: day.date,
            allDay: true,
            extendedProps: { ...ev, date: day.date },
            backgroundColor: ev.type === 'INTERVIEW' ? '#4dabf7' : '#ff6b6b',
            borderColor: ev.type === 'INTERVIEW' ? '#339af0' : '#fa5252',
          }))
        );
        setEvents(mapped);
      })
      .catch(() => setEvents([]))
      .finally(() => setLoading(false));
  }, []);

  return { events, loading, load };
}
