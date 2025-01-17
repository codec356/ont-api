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

package com.github.owlcs.ontapi.jena.impl.conf;

import com.github.owlcs.ontapi.jena.OntJenaException;
import com.github.owlcs.ontapi.jena.OntVocabulary;
import com.github.owlcs.ontapi.jena.impl.*;
import com.github.owlcs.ontapi.jena.model.*;
import com.github.owlcs.ontapi.jena.utils.Iter;
import com.github.owlcs.ontapi.jena.vocabulary.OWL;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.*;
import org.apache.jena.vocabulary.RDFS;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Settings and personalities that are used for constructing {@link OntModel}.
 * An access point to several predefined {@link OntPersonality Ontology Personality} constants.
 * <p>
 * Created by @szuev on 04.11.2016.
 */
@SuppressWarnings("WeakerAccess")
public class OntModelConfig {

    /**
     * A system-wide vocabulary.
     */
    private static final OntVocabulary VOCABULARY = OntVocabulary.Factory.get();
    /**
     * Default builtins vocabulary.
     */
    private static final OntPersonality.Builtins BUILTINS = createBuiltinsVocabulary(VOCABULARY);
    /**
     * Default reserved vocabulary.
     */
    private static final OntPersonality.Reserved RESERVED = createReservedVocabulary(VOCABULARY);

    /**
     * Standard resources. Private access since this constant is mutable.
     *
     * @see org.apache.jena.enhanced.BuiltinPersonalities#model
     */
    private static final Personality<RDFNode> STANDARD_PERSONALITY = new Personality<RDFNode>()
            .add(Resource.class, ResourceImpl.factory)
            .add(Property.class, PropertyImpl.factory)
            .add(Literal.class, LiteralImpl.factory)
            .add(Container.class, ResourceImpl.factory)
            .add(Alt.class, AltImpl.factory)
            .add(Bag.class, BagImpl.factory)
            .add(Seq.class, SeqImpl.factory)
            .add(ReifiedStatement.class, ReifiedStatementImpl.reifiedStatementFactory)
            .add(RDFList.class, RDFListImpl.factory)
            .add(RDFNode.class, ResourceImpl.rdfNodeFactory);

    /**
     * Default personality builder. Private access since this constant is mutable.
     */
    private static final PersonalityBuilder ONT_PERSONALITY_BUILDER = new PersonalityBuilder()
            .addPersonality(STANDARD_PERSONALITY)
            // the base ontology object:
            .add(OntObject.class, OntObjectImpl.objectFactory)

            // ont-id:
            .add(OntID.class, OntIDImpl.idFactory)

            // annotation object:
            .add(OntAnnotation.class, OntAnnotationImpl.annotationFactory)

            // entities:
            .add(OntClass.Named.class, Entities.CLASS.createFactory())
            .add(OntDataRange.Named.class, Entities.DATATYPE.createFactory())
            .add(OntIndividual.Named.class, Entities.INDIVIDUAL.createFactory())
            .add(OntObjectProperty.Named.class, Entities.OBJECT_PROPERTY.createFactory())
            .add(OntDataProperty.class, Entities.DATA_PROPERTY.createFactory())
            .add(OntAnnotationProperty.class, Entities.ANNOTATION_PROPERTY.createFactory())
            .add(OntEntity.class, Entities.ALL)

            // individuals:
            .add(OntIndividual.Anonymous.class, OntIndividualImpl.anonymousIndividualFactory)
            .add(OntIndividual.class, OntIndividualImpl.abstractIndividualFactory)

            // property expressions:
            .add(OntObjectProperty.Inverse.class, OntPEImpl.inversePropertyFactory)
            .add(OntObjectProperty.class, OntPEImpl.abstractOPEFactory)
            .add(OntRealProperty.class, OntPEImpl.abstractDOPFactory)
            .add(OntNamedProperty.class, OntPEImpl.abstractNamedPropertyFactory)
            .add(OntProperty.class, OntPEImpl.abstractPEFactory)

            // class expressions:
            .add(OntClass.ObjectSomeValuesFrom.class, OntCEImpl.objectSomeValuesOfCEFactory)
            .add(OntClass.DataSomeValuesFrom.class, OntCEImpl.dataSomeValuesOfCEFactory)
            .add(OntClass.ObjectAllValuesFrom.class, OntCEImpl.objectAllValuesOfCEFactory)
            .add(OntClass.DataAllValuesFrom.class, OntCEImpl.dataAllValuesOfCEFactory)
            .add(OntClass.ObjectHasValue.class, OntCEImpl.objectHasValueCEFactory)
            .add(OntClass.DataHasValue.class, OntCEImpl.dataHasValueCEFactory)
            .add(OntClass.ObjectMinCardinality.class, OntCEImpl.objectMinCardinalityCEFactory)
            .add(OntClass.DataMinCardinality.class, OntCEImpl.dataMinCardinalityCEFactory)
            .add(OntClass.ObjectMaxCardinality.class, OntCEImpl.objectMaxCardinalityCEFactory)
            .add(OntClass.DataMaxCardinality.class, OntCEImpl.dataMaxCardinalityCEFactory)
            .add(OntClass.ObjectCardinality.class, OntCEImpl.objectCardinalityCEFactory)
            .add(OntClass.DataCardinality.class, OntCEImpl.dataCardinalityCEFactory)
            .add(OntClass.HasSelf.class, OntCEImpl.hasSelfCEFactory)
            .add(OntClass.UnionOf.class, OntCEImpl.unionOfCEFactory)
            .add(OntClass.OneOf.class, OntCEImpl.oneOfCEFactory)
            .add(OntClass.IntersectionOf.class, OntCEImpl.intersectionOfCEFactory)
            .add(OntClass.ComplementOf.class, OntCEImpl.complementOfCEFactory)
            .add(OntClass.NaryDataAllValuesFrom.class, OntCEImpl.naryDataAllValuesFromCEFactory)
            .add(OntClass.NaryDataSomeValuesFrom.class, OntCEImpl.naryDataSomeValuesFromCEFactory)
            .add(OntClass.ComponentsCE.class, OntCEImpl.abstractComponentsCEFactory)
            .add(OntClass.CardinalityRestrictionCE.class, OntCEImpl.abstractCardinalityRestrictionCEFactory)
            .add(OntClass.ComponentRestrictionCE.class, OntCEImpl.abstractComponentRestrictionCEFactory)
            .add(OntClass.UnaryRestrictionCE.class, OntCEImpl.abstractPropertyRestrictionCEFactory)
            .add(OntClass.RestrictionCE.class, OntCEImpl.abstractRestrictionCEFactory)
            .add(OntClass.class, OntCEImpl.abstractCEFactory)

            // data ranges:
            .add(OntDataRange.OneOf.class, OntDRImpl.oneOfDRFactory)
            .add(OntDataRange.Restriction.class, OntDRImpl.restrictionDRFactory)
            .add(OntDataRange.ComplementOf.class, OntDRImpl.complementOfDRFactory)
            .add(OntDataRange.UnionOf.class, OntDRImpl.unionOfDRFactory)
            .add(OntDataRange.IntersectionOf.class, OntDRImpl.intersectionOfDRFactory)
            .add(OntDataRange.ComponentsDR.class, OntDRImpl.abstractComponentsDRFactory)
            .add(OntDataRange.class, OntDRImpl.abstractDRFactory)

            // facet restrictions:
            .add(OntFacetRestriction.Length.class, OntFRImpl.lengthFRFactory)
            .add(OntFacetRestriction.MinLength.class, OntFRImpl.minLengthFRFactory)
            .add(OntFacetRestriction.MaxLength.class, OntFRImpl.maxLengthFRFactory)
            .add(OntFacetRestriction.MinInclusive.class, OntFRImpl.minInclusiveFRFactory)
            .add(OntFacetRestriction.MaxInclusive.class, OntFRImpl.maxInclusiveFRFactory)
            .add(OntFacetRestriction.MinExclusive.class, OntFRImpl.minExclusiveFRFactory)
            .add(OntFacetRestriction.MaxExclusive.class, OntFRImpl.maxExclusiveFRFactory)
            .add(OntFacetRestriction.Pattern.class, OntFRImpl.patternFRFactory)
            .add(OntFacetRestriction.TotalDigits.class, OntFRImpl.totalDigitsFRFactory)
            .add(OntFacetRestriction.FractionDigits.class, OntFRImpl.fractionDigitsFRFactory)
            .add(OntFacetRestriction.LangRange.class, OntFRImpl.langRangeFRFactory)
            .add(OntFacetRestriction.class, OntFRImpl.abstractFRFactory)

            // negative property assertions:
            .add(OntNegativeAssertion.WithObjectProperty.class, OntNPAImpl.objectNPAFactory)
            .add(OntNegativeAssertion.WithDataProperty.class, OntNPAImpl.dataNPAFactory)
            .add(OntNegativeAssertion.class, OntNPAImpl.abstractNPAFactory)

            // disjoint anonymous collections:
            .add(OntDisjoint.Classes.class, OntDisjointImpl.disjointClassesFactory)
            .add(OntDisjoint.Individuals.class, OntDisjointImpl.differentIndividualsFactory)
            .add(OntDisjoint.ObjectProperties.class, OntDisjointImpl.objectPropertiesFactory)
            .add(OntDisjoint.DataProperties.class, OntDisjointImpl.dataPropertiesFactory)
            .add(OntDisjoint.Properties.class, OntDisjointImpl.abstractPropertiesFactory)
            .add(OntDisjoint.class, OntDisjointImpl.abstractDisjointFactory)

            // SWRL objects:
            .add(OntSWRL.Variable.class, OntSWRLImpl.variableSWRLFactory)
            .add(OntSWRL.Builtin.class, OntSWRLImpl.builtinWRLFactory)
            .add(OntSWRL.IArg.class, OntSWRLImpl.iArgSWRLFactory)
            .add(OntSWRL.DArg.class, OntSWRLImpl.dArgSWRLFactory)
            .add(OntSWRL.Arg.class, OntSWRLImpl.abstractArgSWRLFactory)
            .add(OntSWRL.Atom.WithBuiltin.class, OntSWRLImpl.builtInAtomSWRLFactory)
            .add(OntSWRL.Atom.WithClass.class, OntSWRLImpl.classAtomSWRLFactory)
            .add(OntSWRL.Atom.WithDataRange.class, OntSWRLImpl.dataRangeAtomSWRLFactory)
            .add(OntSWRL.Atom.WithObjectProperty.class, OntSWRLImpl.individualAtomSWRLFactory)
            .add(OntSWRL.Atom.WithDataProperty.class, OntSWRLImpl.dataValuedAtomSWRLFactory)
            .add(OntSWRL.Atom.WithDifferentIndividuals.class, OntSWRLImpl.differentIndividualsAtomSWRLFactory)
            .add(OntSWRL.Atom.WithSameIndividuals.class, OntSWRLImpl.sameIndividualsAtomSWRLFactory)
            .add(OntSWRL.Atom.Unary.class, OntSWRLImpl.abstractUnarySWRLFactory)
            .add(OntSWRL.Atom.Binary.class, OntSWRLImpl.abstractBinarySWRLFactory)
            .add(OntSWRL.Atom.class, OntSWRLImpl.abstractAtomSWRLFactory)
            .add(OntSWRL.class, OntSWRLImpl.abstractSWRLFactory)
            .add(OntSWRL.Imp.class, OntSWRLImpl.impSWRLFactory);

    /**
     * Returns the standard jena {@link Personality} as modifiable copy.
     * It contains {@code 10} standard resource factories which are used by RDFS model
     * ({@link Model}, the default model implementation).
     *
     * @return {@link Personality} of {@link RDFNode}s
     * @see org.apache.jena.enhanced.BuiltinPersonalities#model
     */
    public static Personality<RDFNode> getStandardPersonality() {
        return STANDARD_PERSONALITY.copy();
    }

    /**
     * Returns a fresh copy of {@link PersonalityBuilder} with {@code 93} resource factories inside
     * ({@code 10} standard + {@code 87} ontological).
     * The returned instance contains everything needed, and can be modified to build a new {@link OntPersonality}.
     *
     * @return {@link PersonalityBuilder}
     */
    public static PersonalityBuilder getPersonalityBuilder() {
        return ONT_PERSONALITY_BUILDER.copy();
    }

    /**
     * Personalities which don't care about the owl-entities "punnings" (no restriction on the type declarations).
     *
     * @see <a href='https://www.w3.org/TR/owl2-new-features/#F12:_Punning'>2.4.1 F12: Punning</a>
     * @see StdMode#LAX
     */
    public static final OntPersonality ONT_PERSONALITY_LAX = getPersonalityBuilder()
            .setBuiltins(BUILTINS)
            .setReserved(RESERVED)
            .setPunnings(StdMode.LAX.getVocabulary())
            .build();

    /**
     * Personality with four kinds of restriction on a {@code rdf:type} intersection (i.e. "illegal punnings"):
     * <ul>
     * <li>{@link OntDataRange.Named}  &lt;-&gt; {@link OntClass.Named}</li>
     * <li>{@link OntAnnotationProperty} &lt;-&gt; {@link OntObjectProperty.Named}</li>
     * <li>{@link OntObjectProperty.Named} &lt;-&gt; {@link OntDataProperty}</li>
     * <li>{@link OntDataProperty} &lt;-&gt; {@link OntAnnotationProperty}</li>
     * </ul>
     * each of the pairs above can't exist in the form of OWL-Entity in the same model at the same time.
     * From specification: "OWL 2 DL imposes certain restrictions:
     * it requires that a name cannot be used for both a class and a datatype and
     * that a name can only be used for one kind of property."
     *
     * @see <a href='https://www.w3.org/TR/owl2-new-features/#F12:_Punning'>2.4.1 F12: Punning</a>
     * @see StdMode#STRICT
     */
    public static final OntPersonality ONT_PERSONALITY_STRICT = getPersonalityBuilder()
            .setBuiltins(BUILTINS)
            .setReserved(RESERVED)
            .setPunnings(StdMode.STRICT.getVocabulary())
            .build();

    /**
     * The week variant of previous constant: there are two forbidden intersections:
     * <ul>
     * <li>{@link OntDataRange.Named}  &lt;-&gt; {@link OntClass.Named}</li>
     * <li>{@link OntObjectProperty.Named} &lt;-&gt; {@link OntDataProperty}</li>
     * </ul>
     *
     * @see <a href='https://www.w3.org/TR/owl2-new-features/#F12:_Punning'>2.4.1 F12: Punning</a>
     * @see StdMode#MEDIUM
     */
    public static final OntPersonality ONT_PERSONALITY_MEDIUM = getPersonalityBuilder()
            .setBuiltins(BUILTINS)
            .setReserved(RESERVED)
            .setPunnings(StdMode.MEDIUM.getVocabulary())
            .build();


    /**
     * Use {@link StdMode#MEDIUM} by default as a trade-off between the specification and the number of checks,
     * which are usually not necessary and only load the system.
     */
    private static OntPersonality personality = ONT_PERSONALITY_MEDIUM;

    /**
     * Gets a system-wide personalities.
     *
     * @return {@link OntPersonality}
     * @see com.github.owlcs.ontapi.jena.OntModelFactory
     */
    public static OntPersonality getPersonality() {
        return personality;
    }

    /**
     * Sets a system-wide personalities.
     *
     * @param other {@link OntPersonality}, not {@code null}
     * @return {@link OntPersonality}, a previous associated system-wide personalities
     */
    public static OntPersonality setPersonality(OntPersonality other) {
        OntPersonality res = personality;
        personality = OntJenaException.notNull(other, "Null personality specified.");
        return res;
    }

    /**
     * Creates a {@link OntPersonality.Builtins builtins personality vocabulary}
     * from the given {@link OntVocabulary system vocabulary}.
     *
     * @param voc {@link OntVocabulary}, not {@code null}
     * @return {@link OntPersonality.Builtins}
     */
    public static OntPersonality.Builtins createBuiltinsVocabulary(OntVocabulary voc) {
        Objects.requireNonNull(voc);
        Map<Class<? extends OntObject>, Set<Node>> res = new HashMap<>();
        res.put(OntAnnotationProperty.class, Iter.asUnmodifiableNodeSet(voc.getBuiltinAnnotationProperties()));
        res.put(OntDataProperty.class, Iter.asUnmodifiableNodeSet(voc.getBuiltinDatatypeProperties()));
        res.put(OntObjectProperty.Named.class, Iter.asUnmodifiableNodeSet(voc.getBuiltinObjectProperties()));
        res.put(OntDataRange.Named.class, Iter.asUnmodifiableNodeSet(voc.getBuiltinDatatypes()));
        res.put(OntClass.Named.class, Iter.asUnmodifiableNodeSet(voc.getBuiltinClasses()));
        res.put(OntSWRL.Builtin.class, Iter.asUnmodifiableNodeSet(voc.getBuiltinSWRLs()));
        res.put(OntIndividual.Named.class, Collections.emptySet());
        return new VocabularyImpl.EntitiesImpl(res);
    }

    /**
     * Creates a {@link OntPersonality.Reserved reserved personality vocabulary}
     * from the given {@link OntVocabulary system vocabulary}.
     *
     * @param voc {@link OntVocabulary}, not {@code null}
     * @return {@link OntPersonality.Reserved}
     */
    public static OntPersonality.Reserved createReservedVocabulary(OntVocabulary voc) {
        Objects.requireNonNull(voc);
        Map<Class<? extends Resource>, Set<Node>> res = new HashMap<>();
        res.put(Resource.class, Iter.asUnmodifiableNodeSet(voc.getSystemResources()));
        res.put(Property.class, Iter.asUnmodifiableNodeSet(voc.getSystemProperties()));
        return new VocabularyImpl.ReservedIml(res);
    }

    /**
     * Creates a {@link OntPersonality.Punnings punnings personality vocabulary} according to {@link StdMode}.
     *
     * @param mode {@link StdMode}, not {@code null}
     * @return {@link OntPersonality.Punnings}
     */
    private static OntPersonality.Punnings createPunningsVocabulary(OntModelConfig.StdMode mode) {
        Map<Class<? extends OntObject>, Set<Node>> res = new HashMap<>();
        if (!StdMode.LAX.equals(mode)) {
            toMap(res, OntClass.Named.class, RDFS.Datatype);
            toMap(res, OntDataRange.Named.class, OWL.Class);
        }
        if (StdMode.STRICT.equals(mode)) {
            toMap(res, OntAnnotationProperty.class, OWL.ObjectProperty, OWL.DatatypeProperty);
            toMap(res, OntDataProperty.class, OWL.ObjectProperty, OWL.AnnotationProperty);
            toMap(res, OntObjectProperty.Named.class, OWL.DatatypeProperty, OWL.AnnotationProperty);
        }
        if (StdMode.MEDIUM.equals(mode)) {
            toMap(res, OntDataProperty.class, OWL.ObjectProperty);
            toMap(res, OntObjectProperty.Named.class, OWL.DatatypeProperty);
        }
        OntEntity.listEntityTypes().forEachRemaining(t -> res.computeIfAbsent(t, k -> Collections.emptySet()));
        //return type -> fromMap(res, type);
        return new VocabularyImpl.EntitiesImpl(res);
    }

    @SafeVarargs
    private static <K, V extends RDFNode> void toMap(Map<K, Set<Node>> map, K key, V... values) {
        map.put(key, Arrays.stream(values).map(FrontsNode::asNode).collect(Collectors.toUnmodifiableSet()));
    }

    /**
     * A standard personality mode to manage punnings.
     */
    public enum StdMode {
        /**
         * The following punnings are considered as illegal and are excluded:
         * <ul>
         * <li>owl:Class &lt;-&gt; rdfs:Datatype</li>
         * <li>owl:ObjectProperty &lt;-&gt; owl:DatatypeProperty</li>
         * <li>owl:ObjectProperty &lt;-&gt; owl:AnnotationProperty</li>
         * <li>owl:AnnotationProperty &lt;-&gt; owl:DatatypeProperty</li>
         * </ul>
         */
        STRICT,
        /**
         * Forbidden intersections of rdf-declarations:
         * <ul>
         * <li>Class &lt;-&gt; Datatype</li>
         * <li>ObjectProperty &lt;-&gt; DataProperty</li>
         * </ul>
         */
        MEDIUM,
        /**
         * Allow everything.
         */
        LAX,
        ;

        private OntPersonality.Punnings punnings;

        public OntPersonality.Punnings getVocabulary() {
            return punnings == null ? punnings = createPunningsVocabulary(this) : punnings;
        }
    }

}
