import { createContext, useState, type ReactNode, useContext, useEffect } from 'react';

interface SidebarContextType {
  isSidebarOpen: boolean;
  toggleSidebar: () => void;
  screenSize: 'mobile' | 'tablet' | 'desktop';
}

export const SidebarContext = createContext<SidebarContextType | null>(null);

export function useSidebar() {
  const context = useContext(SidebarContext);
  if (!context) {
    throw new Error('useSidebar must be used within a SidebarProvider');
  }
  return context;
}

function getScreenSize(): 'mobile' | 'tablet' | 'desktop' {
  if (typeof window === 'undefined') return 'desktop';
  
  const width = window.innerWidth;
  if (width < 768) return 'mobile';
  if (width < 1024) return 'tablet';
  return 'desktop';
}

function getInitialSidebarState(screenSize: 'mobile' | 'tablet' | 'desktop'): boolean {
  switch (screenSize) {
    case 'mobile':
      return false;
    case 'tablet':
      return false;
    case 'desktop':
      return true;
    default:
      return true;
  }
}

export function SidebarProvider({ children }: { children: ReactNode }) {
  const [screenSize, setScreenSize] = useState<'mobile' | 'tablet' | 'desktop'>(() => getScreenSize());
  const [isSidebarOpen, setSidebarOpen] = useState(() => getInitialSidebarState(screenSize));

  useEffect(() => {
    function handleResize() {
      const newScreenSize = getScreenSize();
      const prevScreenSize = screenSize;
      
      setScreenSize(newScreenSize);
      
      if (prevScreenSize !== newScreenSize) {
        setSidebarOpen(getInitialSidebarState(newScreenSize));
      }
    }

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [screenSize]);

  const toggleSidebar = () => {
    setSidebarOpen(prev => !prev);
  };

  const value = { 
    isSidebarOpen, 
    toggleSidebar,
    screenSize
  };

  return (
    <SidebarContext.Provider value={value}>
      {children}
    </SidebarContext.Provider>
  );
}