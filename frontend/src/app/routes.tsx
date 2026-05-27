import { Routes, Route } from "react-router-dom";
import { ProtectedRoute } from "@components/ProtectedRoute";
import { MainLayout } from "@layouts/MainLayout";
import { LoginPage } from "@pages/LoginPage";
import { DashboardPage } from "@features/dashboard/pages/DashboardPage";
import { IncidentsPage } from "@features/incidents/pages/IncidentsPage";
import { AnalyticsPage } from "@features/analytics/pages/AnalyticsPage";
import { SpatialPage } from "@features/spatial/pages/SpatialPage";
import { RecommendationsPage } from "@features/recommendations/pages/RecommendationsPage";
import { DistrictsPage } from "@features/districts/pages/DistrictsPage";

export const AppRoutes = () => {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/unauthorized" element={<UnauthorizedPage />} />

      {/* Protected routes */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <MainLayout>
              <DashboardPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/incidents"
        element={
          <ProtectedRoute>
            <MainLayout>
              <IncidentsPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/analytics"
        element={
          <ProtectedRoute>
            <MainLayout>
              <AnalyticsPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/spatial"
        element={
          <ProtectedRoute>
            <MainLayout>
              <SpatialPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/recommendations"
        element={
          <ProtectedRoute requiredRole={["ADMIN", "OPERATOR", "ANALYST"]}>
            <MainLayout>
              <RecommendationsPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/districts"
        element={
          <ProtectedRoute requiredRole={["ADMIN", "OPERATOR"]}>
            <MainLayout>
              <DistrictsPage />
            </MainLayout>
          </ProtectedRoute>
        }
      />

      {/* Catch all */}
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};

import { Navigate } from "react-router-dom";

const UnauthorizedPage = () => (
  <div className="flex items-center justify-center min-h-screen">
    <div className="text-center">
      <h1 className="text-4xl font-bold mb-2">403</h1>
      <p className="text-muted-foreground mb-4">
        You don't have permission to access this resource
      </p>
      <a href="/dashboard" className="text-primary hover:underline">
        Go back to dashboard
      </a>
    </div>
  </div>
);

const NotFoundPage = () => (
  <div className="flex items-center justify-center min-h-screen">
    <div className="text-center">
      <h1 className="text-4xl font-bold mb-2">404</h1>
      <p className="text-muted-foreground mb-4">Page not found</p>
      <a href="/dashboard" className="text-primary hover:underline">
        Go back to dashboard
      </a>
    </div>
  </div>
);
