/*
 * This file is part of the ONT API.
 * The contents of this file are subject to the LGPL License, Version 3.0.
 * Copyright (c) 2021, owl.cs group.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0 in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.github.owlcs.ontapi.owlapi.axioms;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLNaryPropertyAxiom;
import org.semanticweb.owlapi.model.OWLPropertyExpression;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @param <P> the property expression
 */
@SuppressWarnings("WeakerAccess")
public abstract class NaryPropertyAxiomImpl<P extends OWLPropertyExpression>
        extends PropertyAxiomImpl implements OWLNaryPropertyAxiom<P> {

    protected final List<P> properties;

    /**
     * @param properties  a {@code Collection} of properties
     * @param annotations a {@code Collection} of annotations
     */
    public NaryPropertyAxiomImpl(Collection<? extends P> properties, Collection<OWLAnnotation> annotations) {
        super(annotations);
        this.properties = toContentList(properties, "properties cannot be null");
    }

    @Override
    public Stream<P> properties() {
        return properties.stream();
    }

    @Override
    public List<P> getOperandsAsList() {
        return properties;
    }

    @Override
    public Set<P> getPropertiesMinus(@Nonnull P property) {
        Set<P> props = new LinkedHashSet<>(properties);
        props.remove(property);
        return props;
    }
}
