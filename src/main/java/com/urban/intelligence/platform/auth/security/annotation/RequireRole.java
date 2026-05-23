package com.urban.intelligence.platform.auth.security.annotation;

import com.urban.intelligence.platform.auth.domain.Role;

import java.lang.annotation.*;

/**
 * RequireRole - Method-level annotation for role-based access control.
 *
 * Usage:
 * @RequireRole(Role.ADMIN)
 * public void adminOnlyOperation() { ... }
 *
 * Can be stacked to allow multiple roles:
 * @RequireRole({Role.ADMIN, Role.OPERATOR})
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    Role[] value();
}