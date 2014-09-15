/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.process.impl;

import java.util.List;

import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ComplexInputDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.RuleDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.UserTaskDefinitionImpl;

/**
 * @author Matthieu Chaffotte
 * @author Laurent Leseigneur
 */
public class ContractDefinitionBuilder extends FlowElementContainerBuilder {

    private final ContractDefinitionImpl contract;

    public ContractDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final FlowElementContainerDefinitionImpl container,
            final UserTaskDefinitionImpl activity) {
        super(container, processDefinitionBuilder);
        contract = new ContractDefinitionImpl();
        activity.setContract(contract);
    }

    public ContractDefinitionBuilder addInput(final String name, final Type type, final String description) {
        final InputDefinitionImpl input = new InputDefinitionImpl(name, type, description);
        contract.addInput(input);
        return this;
    }
    public ContractDefinitionBuilder addComplexInput(final String name, final String description, final List<InputDefinition> inputs) {
        final ComplexInputDefinitionImpl input = new ComplexInputDefinitionImpl(name, description, inputs);
        contract.addInput(input);
        return this;
    }

    public ContractDefinitionBuilder addRule(final String name, final String expression, final String explanation, final String... inputNames) {
        final RuleDefinitionImpl rule = new RuleDefinitionImpl(name, expression, explanation);
        for (final String inputName : inputNames) {
            rule.addInputName(inputName);
        }
        contract.addRule(rule);
        return this;
    }

}