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
package com.github.owlcs.ontapi.owlapi.objects;

import com.github.owlcs.ontapi.owlapi.OWLObjectImpl;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLLiteral;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class AnnotationImplNotAnnotated extends OWLObjectImpl implements OWLAnnotation {

    private final OWLAnnotationProperty property;
    private final OWLAnnotationValue value;

    /**
     * @param property {@link OWLAnnotationProperty}, the annotation property
     * @param value    {@link OWLAnnotationValue}, the annotation value
     */
    public AnnotationImplNotAnnotated(OWLAnnotationProperty property, OWLAnnotationValue value) {
        this.property = Objects.requireNonNull(property, "property cannot be null");
        this.value = Objects.requireNonNull(value, "value cannot be null");
    }

    @Override
    public List<OWLAnnotation> annotationsAsList() {
        return NO_ANNOTATIONS;
    }

    @Override
    public OWLAnnotationProperty getProperty() {
        return property;
    }

    @Override
    public OWLAnnotationValue getValue() {
        return value;
    }

    @Override
    public OWLAnnotation getAnnotatedAnnotation(@Nonnull Collection<OWLAnnotation> annotations) {
        if (annotations.isEmpty()) {
            return this;
        }
        return new AnnotationImpl(property, value, annotations);
    }

    @Override
    public OWLAnnotation getAnnotatedAnnotation(@Nonnull Stream<OWLAnnotation> annotations) {
        return getAnnotatedAnnotation(annotations.collect(Collectors.toList()));
    }

    @Override
    public boolean isDeprecatedIRIAnnotation() {
        return property.isDeprecated() && value instanceof OWLLiteral
                && ((OWLLiteral) value).isBoolean() && ((OWLLiteral) value).parseBoolean();
    }
}
