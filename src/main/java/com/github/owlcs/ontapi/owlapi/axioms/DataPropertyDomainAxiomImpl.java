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

import com.github.owlcs.ontapi.owlapi.InternalizedEntities;
import com.github.owlcs.ontapi.owlapi.objects.ce.DataSomeValuesFromImpl;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.stream.Stream;


public class DataPropertyDomainAxiomImpl
        extends PropertyDomainAxiomImpl<OWLDataPropertyExpression> implements OWLDataPropertyDomainAxiom {

    /**
     * @param property    {@link OWLDataPropertyExpression}, the property
     * @param domain      {@link OWLClassExpression}, the domain
     * @param annotations a {@code Collection} of annotations on the axiom
     */
    public DataPropertyDomainAxiomImpl(OWLDataPropertyExpression property,
                                       OWLClassExpression domain,
                                       Collection<OWLAnnotation> annotations) {
        super(property, domain, annotations);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataPropertyDomainAxiomImpl getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return new DataPropertyDomainAxiomImpl(getProperty(), getDomain(), NO_ANNOTATIONS);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends OWLAxiom> T getAnnotatedAxiom(@Nonnull Stream<OWLAnnotation> anns) {
        return (T) new DataPropertyDomainAxiomImpl(getProperty(), getDomain(), mergeAnnotations(this, anns));
    }

    @Override
    public OWLSubClassOfAxiom asOWLSubClassOfAxiom() {
        OWLClassExpression sub = new DataSomeValuesFromImpl(getProperty(), InternalizedEntities.RDFS_LITERAL);
        return new SubClassOfAxiomImpl(sub, getDomain(), NO_ANNOTATIONS);
    }
}
