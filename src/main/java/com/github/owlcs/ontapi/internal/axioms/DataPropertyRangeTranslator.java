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

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import com.github.owlcs.ontapi.internal.InternalConfig;
import com.github.owlcs.ontapi.internal.InternalObjectFactory;
import com.github.owlcs.ontapi.internal.ONTObject;
import com.github.owlcs.ontapi.internal.ONTWrapperImpl;
import com.github.owlcs.ontapi.jena.model.OntDR;
import com.github.owlcs.ontapi.jena.model.OntNDP;
import com.github.owlcs.ontapi.jena.model.OntStatement;

import java.util.Collection;

/**
 * See {@link AbstractPropertyRangeTranslator}.
 * <p>
 * Created by @szuev on 28.09.2016.
 */
public class DataPropertyRangeTranslator extends AbstractPropertyRangeTranslator<OWLDataPropertyRangeAxiom, OntNDP> {

    @Override
    Class<OntNDP> getView() {
        return OntNDP.class;
    }

    protected boolean filter(OntStatement statement, InternalConfig config) {
        return super.filter(statement, config) && statement.getObject().canAs(OntDR.class);
    }

    @Override
    public ONTObject<OWLDataPropertyRangeAxiom> toAxiom(OntStatement statement,
                                                        InternalObjectFactory reader,
                                                        InternalConfig config) {
        ONTObject<OWLDataProperty> p = reader.getProperty(statement.getSubject(getView()));
        ONTObject<? extends OWLDataRange> d = reader.getDatatype(statement.getObject().as(OntDR.class));
        Collection<ONTObject<OWLAnnotation>> annotations = reader.getAnnotations(statement, config);
        OWLDataPropertyRangeAxiom res = reader.getOWLDataFactory()
                .getOWLDataPropertyRangeAxiom(p.getOWLObject(), d.getOWLObject(), ONTObject.toSet(annotations));
        return ONTWrapperImpl.create(res, statement).append(annotations).append(p).append(d);
    }

}