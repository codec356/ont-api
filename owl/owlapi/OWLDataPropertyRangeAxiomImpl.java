package uk.ac.manchester.cs.owl.owlapi;

import org.semanticweb.owlapi.model.*;

import java.util.Set;
/*
 * Copyright (C) 2006, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 26-Oct-2006<br><br>
 */
public class OWLDataPropertyRangeAxiomImpl extends OWLPropertyRangeAxiomImpl<OWLDataPropertyExpression, OWLDataRange> implements OWLDataPropertyRangeAxiom {

    public OWLDataPropertyRangeAxiomImpl(OWLDataFactory dataFactory, OWLDataPropertyExpression property, OWLDataRange range, Set<? extends OWLAnnotation> annotations) {
        super(dataFactory, property, range, annotations);
    }

    public OWLDataPropertyRangeAxiom getAxiomWithoutAnnotations() {
        if(!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLDataPropertyRangeAxiom(getProperty(), getRange());
    }

    public OWLAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLDataPropertyRangeAxiom(getProperty(), getRange(), mergeAnnos(annotations));
    }

    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLDataPropertyRangeAxiom;
        }
        return false;
    }

    public void accept(OWLAxiomVisitor visitor) {
        visitor.visit(this);
    }

    public void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLAxiomVisitorEx<O> visitor) {
        return visitor.visit(this);
    }


    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    public AxiomType getAxiomType() {
        return AxiomType.DATA_PROPERTY_RANGE;
    }


    public OWLSubClassOfAxiom asSubClassOfAxiom() {
        OWLDataFactory df = getOWLDataFactory();
        OWLClassExpression sup = df.getOWLDataAllValuesFrom(getProperty(), getRange());
        return df.getOWLSubClassOfAxiom(df.getOWLThing(), sup);
    }
}
