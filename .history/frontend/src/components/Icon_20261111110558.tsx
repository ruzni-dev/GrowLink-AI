type IconProps = {
  name: string;
  size?: number;
  strokeWidth?: number;
};

const paths: Record<string, string> = {
  arrow: 'M5 12h14M13 6l6 6-6 6',
  search: 'm21 21-4.35-4.35M10.5 18a7.5 7.5 0 1 1 0-15 7.5 7.5 0 0 1 0 15Z',
  menu: 'M4 6h16M4 12h16M4 18h16',
  close: 'M6 6l12 12M18 6 6 18',
  check: 'm5 12 4 4L19 6',
  map: 'M20 10c0 5-8 11-8 11S4 15 4 10a8 8 0 1 1 16 0Z M12 12.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Z',
  chevron: 'm6 9 6 6 6-6',
  users: 'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2 M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z M22 21v-2a4 4 0 0 0-3-3.87 M16 3.13a4 4 0 0 1 0 7.75',
  globe: 'circle cx="12" cy="12" r="9 M3 12h18 M12 3a14 14 0 0 1 0 18 M12 3a14 14 0 0 0 0 18',
  spark: 'm12 3-1.6 5.4L5 10l5.4 1.6L12 17l1.6-5.4L19 10l-5.4-1.6L12 3Z M19 16l-.7 2.3L16 19l2.3.7L19 22l.7-2.3L22 19l-2.3-.7L19 16Z',
};

export function Icon({ name, size = 18, strokeWidth = 1.8 }: IconProps) {
  const path = paths[name];
  if (!path) return null;

  const isGlobe = name === 'globe';
  return (
    <svg
      aria-hidden="true"
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={strokeWidth}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      {isGlobe ? (
        <>
          <circle cx="12" cy="12" r="9" />
          <path d="M3 12h18M12 3a14 14 0 0 1 0 18M12 3a14 14 0 0 0 0 18" />
        </>
      ) : (
        <path d={path} />
      )}
    </svg>
  );
}
