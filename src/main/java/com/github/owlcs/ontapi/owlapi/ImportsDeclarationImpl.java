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
package com.github.owlcs.ontapi.owlapi;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;


public class ImportsDeclarationImpl implements OWLImportsDeclaration, Serializable {

    private final IRI iri;

    /**
     * @param iri iri to import
     */
    public ImportsDeclarationImpl(IRI iri) {
        this.iri = Objects.requireNonNull(iri, "iri cannot be null");
    }

    @Override
    public IRI getIRI() {
        return iri;
    }

    @Override
    public int hashCode() {
        return iri.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof OWLImportsDeclaration)) {
            return false;
        }
        OWLImportsDeclaration other = (OWLImportsDeclaration) obj;
        return iri.equals(other.getIRI());
    }

    @Override
    public int compareTo(@Nullable OWLImportsDeclaration o) {
        return iri.compareTo(Objects.requireNonNull(o).getIRI());
    }

    @Override
    public String toString() {
        return String.format("Import(%s)", iri.toQuotedString());
    }
}
