/**
 * Infrastructure module - Gateway filters, HTTP clients, and external integrations.
 * <p>
 * This module contains technical implementations:
 * <ul>
 *     <li>{@code auth.client} - Auth service HTTP client</li>
 *     <li>{@code filter} - Gateway filter implementations (Authentication, OperatorIdInjection, AccessLog)</li>
 *     <li>{@code config} - Infrastructure configurations (WebClient, GlobalExceptionHandler)</li>
 * </ul>
 * <p>
 * This module depends on application, domain, and common modules.
 */
package com.catface996.gateway.infrastructure;
