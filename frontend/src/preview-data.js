export const PREVIEW_FILES = [
  {
    id: 'preview-1',
    filename: 'investor-update-q2.pdf',
    fileSize: 2_480_112,
    mimeType: 'application/pdf',
    uploadTime: '2026-04-12T09:30:00.000Z'
  },
  {
    id: 'preview-2',
    filename: 'field-audit-photos.zip',
    fileSize: 18_912_443,
    mimeType: 'application/zip',
    uploadTime: '2026-04-14T15:12:00.000Z'
  },
  {
    id: 'preview-3',
    filename: 'contracts-signed.docx',
    fileSize: 832_114,
    mimeType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    uploadTime: '2026-04-15T07:48:00.000Z'
  },
  {
    id: 'preview-4',
    filename: 'press-kit-assets.mp4',
    fileSize: 84_441_203,
    mimeType: 'video/mp4',
    uploadTime: '2026-04-16T13:20:00.000Z'
  }
]

export const PREVIEW_ACTIVITY = [
  {
    id: 'activity-1',
    action: 'UPLOAD',
    filename: 'press-kit-assets.mp4',
    createdAt: '2026-04-16T13:20:00.000Z',
    detail: 'A new media bundle landed in the workspace.'
  },
  {
    id: 'activity-2',
    action: 'SHARE_LINK_CREATED',
    filename: 'contracts-signed.docx',
    createdAt: '2026-04-15T12:05:00.000Z',
    detail: 'A 24-hour download link was issued for external review.'
  },
  {
    id: 'activity-3',
    action: 'RENAME',
    filename: 'investor-update-q2.pdf',
    createdAt: '2026-04-14T10:18:00.000Z',
    detail: 'The file name was normalized before sending to stakeholders.'
  },
  {
    id: 'activity-4',
    action: 'LOGIN',
    filename: '',
    createdAt: '2026-04-13T08:30:00.000Z',
    detail: 'A verified session was opened from the web workspace.'
  }
]

export const PREVIEW_TEAM = [
  {
    id: 'member-1',
    name: 'Aarav Singh',
    role: 'Operations Lead',
    zone: 'Delhi',
    focus: 'Document intake and external approvals'
  },
  {
    id: 'member-2',
    name: 'Naina Rao',
    role: 'Security Reviewer',
    zone: 'Bengaluru',
    focus: 'Session policy and share-link audits'
  },
  {
    id: 'member-3',
    name: 'Ishaan Mehta',
    role: 'Records Manager',
    zone: 'Mumbai',
    focus: 'Retention windows and download tracing'
  }
]

export const PREVIEW_LEADERBOARD = [
  { id: 'rank-1', name: 'Ops Desk', metric: '142 verified actions', secondary: '12.8 GB protected' },
  { id: 'rank-2', name: 'Contracts Cell', metric: '109 verified actions', secondary: '84 signed files shared' },
  { id: 'rank-3', name: 'Media Vault', metric: '93 verified actions', secondary: '31 large bundles uploaded' }
]

export const PREVIEW_METRICS = [
  { label: 'Protected Sessions', value: 'HttpOnly' },
  { label: 'Share Windows', value: '24h' },
  { label: 'Preview Files', value: `${PREVIEW_FILES.length}` }
]
