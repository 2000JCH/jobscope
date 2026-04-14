const ALARM_TYPE_LABEL = {
  D7: 'D-7',
  D3: 'D-3',
  D1: 'D-1',
  DEADLINE: 'D-Day',
};

function AlarmLogItem({ log, isEditMode, isSelected, onToggle }) {
  const typeLabel = ALARM_TYPE_LABEL[log.alarmType] ?? log.alarmType;
  const sentDate = log.sentAt ? log.sentAt.slice(0, 10) : '';

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '0.75rem 0',
        borderBottom: '1px solid #eee',
        cursor: isEditMode ? 'pointer' : 'default',
        opacity: isEditMode && !isSelected ? 0.5 : 1,
      }}
      onClick={isEditMode ? onToggle : undefined}
    >
      {isEditMode && (
        <input
          type="checkbox"
          checked={isSelected}
          onChange={onToggle}
          onClick={(e) => e.stopPropagation()}
          style={{ marginRight: '0.75rem', flexShrink: 0 }}
        />
      )}
      <div style={{ flex: 1 }}>
        <div style={{ fontWeight: 600 }}>{log.companyName}</div>
        <div style={{ fontSize: '0.875rem', color: '#555', marginTop: '0.25rem' }}>
          {typeLabel} · {log.label}
        </div>
        <div style={{ fontSize: '0.75rem', color: '#999', marginTop: '0.125rem' }}>{sentDate}</div>
      </div>
      {!isEditMode && (
        <span style={{ fontSize: '0.75rem', color: log.isSuccess ? '#22c55e' : '#ef4444' }}>
          {log.isSuccess ? '발송 완료' : '발송 실패'}
        </span>
      )}
    </div>
  );
}

export default AlarmLogItem;