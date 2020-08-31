/*
 * This file is part of the ONT API.
 * The contents of this file are subject to the LGPL License, Version 3.0.
 * Copyright (c) 2020, owl.cs group.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0 in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.github.owlcs.ontapi.internal.searchers.objects;

import com.github.owlcs.ontapi.OntApiException;
import com.github.owlcs.ontapi.config.AxiomsSettings;
import com.github.owlcs.ontapi.internal.AxiomTranslator;
import com.github.owlcs.ontapi.internal.ModelObjectFactory;
import com.github.owlcs.ontapi.internal.ONTObject;
import com.github.owlcs.ontapi.internal.ONTObjectFactory;
import com.github.owlcs.ontapi.jena.model.OntModel;
import com.github.owlcs.ontapi.jena.model.OntObject;
import com.github.owlcs.ontapi.jena.model.OntStatement;
import com.github.owlcs.ontapi.jena.utils.Iter;
import com.github.owlcs.ontapi.jena.utils.OntModels;
import com.github.owlcs.ontapi.jena.vocabulary.OWL;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by @ssz on 26.07.2020.
 */
public class AnnotationPropertySearcher extends PropertySearcher<OWLAnnotationProperty> {
    /**
     * All translators, since any axiom can be annotated
     */
    private static final Set<AxiomTranslator<OWLAxiom>> TRANSLATORS = selectTranslators(null);

    /**
     * Recursively lists all annotations for the ontology header in the form of a flat stream.
     *
     * @param m {@link OntModel}, not {@code null}
     * @return a {@code Stream} of all header annotations including sub-annotations
     */
    public static Stream<OntStatement> headerAnnotations(OntModel m) {
        return m.id().map(OntObject::getMainStatement).map(OntModels::annotations).orElse(Stream.empty());
    }

    @Override
    protected ExtendedIterator<? extends AxiomTranslator<OWLAxiom>> listTranslators() {
        return Iter.create(TRANSLATORS);
    }

    @Override
    protected ONTObject<OWLAnnotationProperty> createEntity(String uri, OntModel model, ONTObjectFactory factory) {
        return factory.getProperty(OntApiException.mustNotBeNull(model.getAnnotationProperty(uri)));
    }

    @Override
    protected ONTObject<OWLAnnotationProperty> createEntity(String uri, ModelObjectFactory factory) {
        return factory.getAnnotationProperty(uri);
    }

    @Override
    protected Resource getEntityType() {
        return OWL.AnnotationProperty;
    }

    @Override
    protected Set<Node> getBuiltinsSpec(OntModel m) {
        return getBuiltinsVocabulary(m).getAnnotationProperties();
    }

    @Override
    protected boolean containsInOntology(Resource uri, OntModel m, AxiomsSettings conf) {
        if (headerAnnotations(m).map(Statement::getPredicate).anyMatch(uri::equals)) { // in header
            return true;
        }
        // in axioms:
        return super.containsInOntology(uri, m, conf);
    }
}
