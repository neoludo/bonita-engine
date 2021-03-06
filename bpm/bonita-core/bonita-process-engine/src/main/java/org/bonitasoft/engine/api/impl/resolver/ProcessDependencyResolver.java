/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.api.impl.resolver;

import java.util.List;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface ProcessDependencyResolver {

    /**
     * resolve a dedicated part of the process
     * e.g. load connectors
     * Must throw an exception is something is not resolved in the process
     * 
     * @param tenantAccessor
     * @param businessArchive
     * @param sDefinition
     * @throws BonitaException
     */
    boolean resolve(TenantServiceAccessor tenantAccessor, BusinessArchive businessArchive, SProcessDefinition sDefinition) throws BonitaException;

    /**
     * @param tenantAccessor
     * @param processDefinition
     * @return
     */
    List<Problem> checkResolution(TenantServiceAccessor tenantAccessor, final SProcessDefinition processDefinition);

}
