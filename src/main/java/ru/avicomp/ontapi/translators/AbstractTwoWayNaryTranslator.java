package ru.avicomp.ontapi.translators;

import java.util.stream.Stream;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.semanticweb.owlapi.model.IsAnonymous;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNaryAxiom;
import org.semanticweb.owlapi.model.OWLObject;

import ru.avicomp.ontapi.OntApiException;
import ru.avicomp.ontapi.jena.model.OntDisjoint;
import ru.avicomp.ontapi.jena.model.OntGraphModel;
import ru.avicomp.ontapi.jena.model.OntObject;
import ru.avicomp.ontapi.jena.model.OntStatement;

/**
 * This is for following axioms with two or more than two entities:
 *
 * DisjointClasses ({@link DisjointClassesTranslator}),
 * DisjointObjectProperties ({@link DisjointObjectPropertiesTranslator}),
 * DisjointDataProperties ({@link DisjointDataPropertiesTranslator}),
 * DifferentIndividuals ({@link DifferentIndividualsTranslator}).
 *
 * Each of these axioms could be written in two ways: as single triple (or sequence of single triples) or as special anonymous node with rdf:List inside.
 * <p>
 * Created by szuev on 12.10.2016.
 */
abstract class AbstractTwoWayNaryTranslator<Axiom extends OWLAxiom & OWLNaryAxiom<OWL>, OWL extends OWLObject & IsAnonymous, ONT extends OntObject> extends AbstractNaryTranslator<Axiom, OWL, ONT> {
    @Override
    public void write(Axiom axiom, OntGraphModel model) {
        long count = axiom.operands().count();
        if (count < 2) throw new OntApiException("Should be at least two entities " + axiom);
        if (count == 2) { // single triple classic way
            OWLObject entity = axiom.operands().filter(e -> !e.isAnonymous()).findFirst().orElse(null);
            if (entity == null)
                throw new OntApiException("Can't find a single non-anonymous entity expression inside " + axiom);
            OWLObject rest = axiom.operands().filter((obj) -> !entity.equals(obj)).findFirst().orElse(null);
            OWL2RDFHelper.writeTriple(model, entity, getPredicate(), rest, axiom.annotations(), true);
        } else { // OWL2 anonymous node
            Resource root = model.createResource();
            model.add(root, RDF.type, getMembersType());
            model.add(root, getMembersPredicate(), OWL2RDFHelper.addRDFList(model, axiom.operands()));
            OWL2RDFHelper.addAnnotations(root.as(OntDisjoint.class), axiom.annotations());
        }
    }

    @Override
    Stream<OntStatement> statements(OntGraphModel model) {
        return Stream.concat(super.statements(model),
                model.ontObjects(getDisjointView()).filter(OntObject::isLocal).map(OntObject::getRoot));
    }

    @Override
    Stream<ONT> components(OntStatement statement) {
        if (statement.getSubject().canAs(getDisjointView())) {
            return statement.getSubject().as(getDisjointView()).members();
        }
        return super.components(statement);
    }

    abstract Resource getMembersType();

    abstract Property getMembersPredicate();

    abstract Class<? extends OntDisjoint<ONT>> getDisjointView();
}