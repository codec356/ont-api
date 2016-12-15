package ru.avicomp.ontapi.translators;

import java.util.Set;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL2;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;

import ru.avicomp.ontapi.jena.model.OntOPE;
import ru.avicomp.ontapi.jena.model.OntStatement;
import uk.ac.manchester.cs.owl.owlapi.OWLFunctionalObjectPropertyAxiomImpl;

/**
 * example:
 * pizza:isBaseOf rdf:type owl:ObjectProperty , owl:FunctionalProperty .
 * <p>
 * Created by @szuev on 29.09.2016.
 */
class FunctionalObjectPropertyTranslator extends AbstractPropertyTypeTranslator<OWLFunctionalObjectPropertyAxiom, OntOPE> {
    @Override
    Resource getType() {
        return OWL2.FunctionalProperty;
    }

    @Override
    Class<OntOPE> getView() {
        return OntOPE.class;
    }

    @Override
    OWLFunctionalObjectPropertyAxiom create(OntStatement statement, Set<OWLAnnotation> annotations) {
        return new OWLFunctionalObjectPropertyAxiomImpl(RDF2OWLHelper.getObjectProperty(getSubject(statement)), annotations);
    }
}