/*
 * This file is part of the ONT API.
 * The contents of this file are subject to the LGPL License, Version 3.0.
 * Copyright (c) 2019, The University of Manchester, owl.cs group.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0 in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.github.owlcs.ontapi.internal.axioms;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import com.github.owlcs.ontapi.internal.InternalConfig;
import com.github.owlcs.ontapi.internal.InternalObjectFactory;
import com.github.owlcs.ontapi.internal.ONTObject;
import com.github.owlcs.ontapi.jena.model.OntDisjoint;
import com.github.owlcs.ontapi.jena.model.OntOPE;
import com.github.owlcs.ontapi.jena.model.OntStatement;
import com.github.owlcs.ontapi.jena.vocabulary.OWL;

/**
 * see {@link AbstractTwoWayNaryTranslator}
 * examples:
 * <ul>
 * <li>{@code :dataProperty1 owl:propertyDisjointWith :dataProperty2}</li>
 * <li>{@code  [ rdf:type owl:AllDisjointProperties; owl:members ( :dataProperty1 :dataProperty2 :dataProperty3 ) ]}</li>
 * </ul>
 * <p>
 * Created by szuev on 12.10.2016.
 *
 * @see OWLDisjointObjectPropertiesAxiom
 */
public class DisjointObjectPropertiesTranslator extends AbstractTwoWayNaryTranslator<OWLDisjointObjectPropertiesAxiom, OWLObjectPropertyExpression, OntOPE> {
    @Override
    Property getPredicate() {
        return OWL.propertyDisjointWith;
    }

    @Override
    Class<OntOPE> getView() {
        return OntOPE.class;
    }

    @Override
    Resource getMembersType() {
        return OWL.AllDisjointProperties;
    }

    @Override
    Property getMembersPredicate() {
        return OWL.members;
    }

    @Override
    Class<OntDisjoint.ObjectProperties> getDisjointView() {
        return OntDisjoint.ObjectProperties.class;
    }

    @Override
    public ONTObject<OWLDisjointObjectPropertiesAxiom> toAxiom(OntStatement statement,
                                                               InternalObjectFactory reader,
                                                               InternalConfig config) {
        return makeAxiom(statement, reader.getAnnotations(statement, config),
                reader::getProperty,
                (members, annotations) -> reader.getOWLDataFactory()
                        .getOWLDisjointObjectPropertiesAxiom(ONTObject.toSet(members), ONTObject.toSet(annotations)));
    }
}