// src/components/Loader.tsx
export default function Loader({ size = 12 }: { size?: number }) {
  const w = size; // width w rem lub px
  const h = size; // height

  return (
    <div className="flex items-center justify-center">
      <div
        className="animate-spin rounded-full border-4 border-blue-500 border-t-transparent"
        style={{ width: `${w}px`, height: `${h}px` }}
      />
    </div>
  );
}
