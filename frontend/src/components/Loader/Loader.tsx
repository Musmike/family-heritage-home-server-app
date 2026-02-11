export default function Loader({ size = 12 }: { size?: number }) {

  return (
    <div className="flex items-center justify-center">
      <div
        className="animate-spin rounded-full border-4 border-blue-500 border-t-transparent"
        style={{ width: size, height: size }}
      />
    </div>
  );
}