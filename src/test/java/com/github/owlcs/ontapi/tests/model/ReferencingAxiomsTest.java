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

package com.github.owlcs.ontapi.tests.model;

import com.github.owlcs.ontapi.OntManagers;
import com.github.owlcs.ontapi.OwlObjects;
import com.github.owlcs.ontapi.jena.vocabulary.OWL;
import com.github.owlcs.ontapi.jena.vocabulary.RDF;
import com.github.owlcs.ontapi.tests.ModelData;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by @ssz on 08.03.2020.
 */
@RunWith(Parameterized.class)
public class ReferencingAxiomsTest {
    private final TestData data;

    public ReferencingAxiomsTest(TestData data) {
        this.data = data;
    }

    @Parameterized.Parameters(name = "{0}")
    public static TestData[] getData() {
        return TestData.values();
    }

    private static OWLOntologyManager newManager() {
        return OntManagers.createManager();
    }

    @Test
    public void testSearchByClass() {
        data.doTest(T.CLASS, HasClassesInSignature::classesInSignature);
    }

    @Test
    public void testSearchByLiteral() {
        OWLOntology ont = data.load(newManager());
        OWLDataFactory df = ont.getOWLOntologyManager().getOWLDataFactory();
        Set<OWLLiteral> literals = ont.axioms().flatMap(x -> OwlObjects.objects(OWLLiteral.class, x))
                .collect(Collectors.toSet());
        literals.add(df.getOWLLiteral(true));
        for (int i = 0; i < 4; i++)
            literals.add(df.getOWLLiteral(String.valueOf(i), OWL2Datatype.XSD_NON_NEGATIVE_INTEGER));
        data.getTester(T.LITERAL).testAxiomsCounts(ont, x -> literals.stream());
    }

    @Test
    public void testSearchByIRI() {
        OWLOntology ont = data.load(newManager());
        Set<IRI> iris = ont.signature().map(HasIRI::getIRI).collect(Collectors.toSet());
        iris.add(IRI.create(OWL.intersectionOf.getURI()));
        iris.add(IRI.create(RDF.langString.getURI()));
        iris.add(IRI.create("http://" + RandomStringUtils.randomAlphabetic(12)));
        data.getTester(T.IRI).testAxiomsCounts(ont, x -> iris.stream());
    }

    @Test
    public void testSearchByAnonymousIndividuals() {
        data.doTest(T.ANONYMOUS_INDIVIDUAL, HasAnonymousIndividuals::anonymousIndividuals);
    }

    @Test
    public void testSearchByNamedIndividuals() {
        data.doTest(T.NAMED_INDIVIDUAL, HasIndividualsInSignature::individualsInSignature);
    }

    @Test
    public void testSearchByDatatypes() {
        data.doTest(T.DATATYPE, HasDatatypesInSignature::datatypesInSignature);
    }

    @Test
    public void testSearchByObjectProperty() {
        data.doTest(T.OBJECT_PROPERTY, HasObjectPropertiesInSignature::objectPropertiesInSignature);
    }

    @Test
    public void testSearchByDatatypeProperty() {
        data.doTest(T.DATA_PROPERTY, HasDataPropertiesInSignature::dataPropertiesInSignature);
    }

    @Test
    public void testSearchByAnnotationProperty() {
        data.doTest(T.ANNOTATION_PROPERTY, HasAnnotationPropertiesInSignature::annotationPropertiesInSignature);
    }

    enum TestData {
        PIZZA(ModelData.PIZZA,
                T.IRI.of(92474991110L),
                T.LITERAL.of(5847447319L),
                T.ANONYMOUS_INDIVIDUAL.of(),
                T.NAMED_INDIVIDUAL.of(4858697351L),
                T.CLASS.of(75129089786L),
                T.DATATYPE.of(5847447319L),
                T.OBJECT_PROPERTY.of(-5055137984L),
                T.DATA_PROPERTY.of(),
                T.ANNOTATION_PROPERTY.of(5847447319L)
        ),
        FAMILY(ModelData.FAMILY,
                T.IRI.of(-7556502204L),
                T.LITERAL.of(-45686983406L),
                T.ANONYMOUS_INDIVIDUAL.of(),
                T.NAMED_INDIVIDUAL.of(123428810197L),
                T.CLASS.of(-7698608481L),
                T.DATATYPE.of(-41855234952L),
                T.OBJECT_PROPERTY.of(-40985808704L),
                T.DATA_PROPERTY.of(-40904863514L),
                T.ANNOTATION_PROPERTY.of(83282971L)
        ),
        PEOPLE(ModelData.PEOPLE,
                T.IRI.of(-2052328542L),
                T.LITERAL.of(14670462876L),
                T.ANONYMOUS_INDIVIDUAL.of(7259412151L),
                T.NAMED_INDIVIDUAL.of(10749291192L),
                T.CLASS.of(-36032894329L),
                T.DATATYPE.of(12391681305L),
                T.OBJECT_PROPERTY.of(-16161584024L),
                T.DATA_PROPERTY.of(),
                T.ANNOTATION_PROPERTY.of(16238983895L)
        ),
        CAMERA(ModelData.CAMERA,
                T.IRI.of(34678315922L),
                T.LITERAL.of(),
                T.ANONYMOUS_INDIVIDUAL.of(),
                T.NAMED_INDIVIDUAL.of(129741169L),
                T.CLASS.of(16668417184L),
                T.DATATYPE.of(6740474844L),
                T.OBJECT_PROPERTY.of(-1389590202L),
                T.DATA_PROPERTY.of(12529272927L),
                T.ANNOTATION_PROPERTY.of()
        ),
        KOALA(ModelData.KOALA,
                T.IRI.of(4895124448L),
                T.LITERAL.of(152056289L),
                T.ANONYMOUS_INDIVIDUAL.of(),
                T.NAMED_INDIVIDUAL.of(356839630L),
                T.CLASS.of(-3512598891L),
                T.DATATYPE.of(1252041014L),
                T.OBJECT_PROPERTY.of(5550952321L),
                T.DATA_PROPERTY.of(-3263365120L),
                T.ANNOTATION_PROPERTY.of(2255627747L)
        ),
        TRAVEL(ModelData.TRAVEL,
                T.IRI.of(-10914071789L),
                T.LITERAL.of(-3973926788L),
                T.ANONYMOUS_INDIVIDUAL.of(1033568903L),
                T.NAMED_INDIVIDUAL.of(-1214294187L),
                T.CLASS.of(9634300081L),
                T.DATATYPE.of(-5564259158L),
                T.OBJECT_PROPERTY.of(-1128108216L),
                T.DATA_PROPERTY.of(-8004661834L),
                T.ANNOTATION_PROPERTY.of(-3973926788L)
        ),
        WINE(ModelData.WINE,
                T.IRI.of(141385965517L),
                T.LITERAL.of(3321372063L),
                T.ANONYMOUS_INDIVIDUAL.of(),
                T.NAMED_INDIVIDUAL.of(43657818377L),
                T.CLASS.of(33981903885L),
                T.DATATYPE.of(2653283947L),
                T.OBJECT_PROPERTY.of(59036259728L),
                T.DATA_PROPERTY.of(-507343578L),
                T.ANNOTATION_PROPERTY.of(1282021579L)
        ),
        FOOD(ModelData.FOOD,
                T.IRI.of(112980744315L),
                T.LITERAL.of(),
                T.ANONYMOUS_INDIVIDUAL.of(),
                T.NAMED_INDIVIDUAL.of(12008713509L),
                T.CLASS.of(78674545819L),
                T.DATATYPE.of(),
                T.OBJECT_PROPERTY.of(22297484987L),
                T.DATA_PROPERTY.of(),
                T.ANNOTATION_PROPERTY.of()
        ),
        NCBITAXON_CUT(ModelData.NCBITAXON_CUT,
                T.IRI.of(501742172985L),
                T.LITERAL.of(103003236956L),
                T.ANONYMOUS_INDIVIDUAL.of(),
                T.NAMED_INDIVIDUAL.of(99706485721L),
                T.CLASS.of(103056848991L),
                T.DATATYPE.of(103003236956L),
                T.OBJECT_PROPERTY.of(-11121488227L),
                T.DATA_PROPERTY.of(-22057951323L),
                T.ANNOTATION_PROPERTY.of(110212215610L)
        ),
        HP_CUT(ModelData.HP_CUT,
                T.IRI.of(-25728375951L),
                T.LITERAL.of(4459800725L),
                T.ANONYMOUS_INDIVIDUAL.of(),
                T.NAMED_INDIVIDUAL.of(),
                T.CLASS.of(-20822811452L),
                T.DATATYPE.of(6174427199L),
                T.OBJECT_PROPERTY.of(-9359730838L),
                T.DATA_PROPERTY.of(),
                T.ANNOTATION_PROPERTY.of(-418190290L)
        ),

        FAMILY_PEOPLE_UNION(ModelData.FAMILY_PEOPLE_UNION,
                T.IRI.of(-6160557032L),
                T.LITERAL.of(-2908650024L),
                T.ANONYMOUS_INDIVIDUAL.of(-281525676L),
                T.NAMED_INDIVIDUAL.of(-3721888116L),
                T.CLASS.of(2270078579L),
                T.DATATYPE.of(-2908650024L),
                T.OBJECT_PROPERTY.of(1108552553L),
                T.DATA_PROPERTY.of(-2596921457L),
                T.ANNOTATION_PROPERTY.of(-311728567L)
        ),
        ;
        private final ModelData resource;
        private final ByPrimitiveTester[] expectations;

        TestData(ModelData data, ByPrimitiveTester... expectations) {
            this.resource = data;
            this.expectations = expectations;
        }

        public ByPrimitiveTester getTester(T type) {
            return Arrays.stream(expectations)
                    .filter(x -> x.type.equals(type.name()))
                    .findFirst().orElseThrow(IllegalArgumentException::new);
        }

        void doTest(T type, Function<OWLOntology, Stream<? extends OWLPrimitive>> getPrimitives) {
            OWLOntology ont = load(newManager());
            getTester(type).testAxiomsCounts(ont, getPrimitives::apply);
        }

        public OWLOntology load(OWLOntologyManager manager) {
            return resource.fetch(manager);
        }
    }

    enum T {
        IRI, LITERAL, ANONYMOUS_INDIVIDUAL, NAMED_INDIVIDUAL, CLASS, DATATYPE, OBJECT_PROPERTY, DATA_PROPERTY, ANNOTATION_PROPERTY,
        ;

        private ByPrimitiveTester of() {
            return of(0);
        }

        private ByPrimitiveTester of(long count) {
            return new ByPrimitiveTester(name(), count, (o, x) -> o.referencingAxioms((OWLPrimitive) x));
        }
    }

}
