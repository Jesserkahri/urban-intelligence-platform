import React from "react";
import { LogOut, Menu } from "lucide-react";
import { useAuth } from "@context/auth";
import { Button } from "@components/ui/Button";
import { useState } from "react";

interface HeaderProps {
  onMenuClick: () => void;
}

export const Header: React.FC<HeaderProps> = ({ onMenuClick }) => {
  const { user, logout } = useAuth();
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try {
      await logout();
    } catch (error) {
      console.error("Logout error:", error);
    } finally {
      setIsLoggingOut(false);
    }
  };

  return (
    <header className="sticky top-0 z-40 border-b border-border bg-card">
      <div className="flex items-center justify-between px-4 py-4 sm:px-6">
        <div className="flex items-center gap-4">
          <button
            onClick={onMenuClick}
            className="lg:hidden inline-flex items-center justify-center rounded-md p-2 hover:bg-accent"
          >
            <Menu className="h-6 w-6" />
          </button>
          <h1 className="text-xl font-bold text-foreground hidden sm:block">
            Urban Flagship
          </h1>
        </div>

        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <div className="text-right text-sm hidden sm:block">
              <p className="font-medium text-foreground">{user?.displayName}</p>
              <p className="text-xs text-muted-foreground">{user?.role}</p>
            </div>
          </div>

          <Button
            variant="ghost"
            size="icon"
            onClick={handleLogout}
            disabled={isLoggingOut}
            title="Logout"
          >
            <LogOut className="h-5 w-5" />
          </Button>
        </div>
      </div>
    </header>
  );
};
