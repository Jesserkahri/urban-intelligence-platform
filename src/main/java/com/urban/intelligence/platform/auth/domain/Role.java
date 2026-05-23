package com.urban.intelligence.platform.auth.domain;

/**
 * Role - Defines access levels for the Urban Intelligence Platform.
 *
 * Hierarchical permission model:
 * - ADMIN: Full system access, user management, configuration
 * - OPERATOR: Operational management, incident handling
 * - ANALYST: Read access + analytics, reports, insights
 * - VIEWER: Read-only access to dashboards and data
 */
public enum Role {
    ADMIN,
    OPERATOR,
    ANALYST,
    VIEWER
}