/*
 * This file is part of the ONT API.
 * The contents of this file are subject to the LGPL License, Version 3.0.
 * Copyright (c) 2018, Avicomp Services, AO
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0 in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package ru.avicomp.ontapi;

import org.semanticweb.owlapi.model.HasOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyID;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A collection to store anything that has {@link OWLOntologyID Ontology ID}.
 * Implementation notes:
 * It was introduced to be sure that all members are in the consistent state.
 * Currently it is not possible to use directly different {@code Map}s with {@link OWLOntologyID Ontology ID} as keys
 * like in the original OWL-API implementation,
 * since anything, including that ID, can be changed externally (e.g. directly from the jena graph
 * using shadow {@link ru.avicomp.ontapi.jena.model.OntGraphModel} interface or something else).
 * On the other hand, it is not expected that this collection will hold a large number of elements,
 * so using reordering operation in every method is OK.
 * <p>
 * Created by @ssz on 08.12.2018.
 */
@SuppressWarnings("WeakerAccess")
public class OntologyCollectionImpl<O extends HasOntologyID> implements OntologyCollection<O>, Serializable {
    private static final long serialVersionUID = 3693502109998760296L;

    protected final Map<OWLOntologyID, O> map;
    protected final ReadWriteLock lock;

    public OntologyCollectionImpl() {
        this(NoOpReadWriteLock.NO_OP_RW_LOCK);
    }

    public OntologyCollectionImpl(ReadWriteLock lock) {
        this(lock, new HashMap<>());
    }

    /**
     * Creates an {@link OntologyCollection Ontology Collection} from the given {@link Collection Java Collection}.
     *
     * @param lock {@link ReadWriteLock}, not {@code null}
     * @param list {@link Collection} of element-containers with {@link OWLOntologyID key-id}s inside
     * @throws IllegalStateException in case the given collection contains duplicates
     * @throws NullPointerException  some {@code null} input parameters
     */
    public OntologyCollectionImpl(ReadWriteLock lock, Collection<O> list) throws IllegalStateException, NullPointerException {
        this(lock, list.stream().collect(Collectors.toMap(HasOntologyID::getOntologyID, Function.identity())));
    }

    /**
     * The main constructor.
     *
     * @param lock {@link ReadWriteLock}, not {@code null}
     * @param map  {@link Map}, not {@code null}
     */
    protected OntologyCollectionImpl(ReadWriteLock lock, Map<OWLOntologyID, O> map) {
        this.map = Objects.requireNonNull(map, "Null ontology map");
        this.lock = Objects.requireNonNull(lock, "Null lock");
    }

    /**
     * Answers {@code true} if this collection is concurrent.
     *
     * @return boolean
     * @see OntologyManagerImpl#isConcurrent()
     */
    protected boolean isConcurrent() {
        return NoOpReadWriteLock.NO_OP_RW_LOCK != lock;
    }

    @Override
    public long size() {
        lock.readLock().lock();
        try {
            return map.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return map.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public OntologyCollectionImpl<O> clear() {
        lock.writeLock().lock();
        try {
            map.clear();
            return this;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Stream<O> values() {
        lock.readLock().lock();
        try {
            if (isConcurrent()) {
                return new ArrayList<>(map.values()).stream();
            }
            return map.values().stream();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<O> get(OWLOntologyID key) {
        lock.readLock().lock();
        try {
            O res = map.get(key);
            if (res != null) {
                // this is fast as Map:
                if (key.equals(res.getOntologyID())) {
                    return Optional.of(res);
                }
                replace(key, res);
            }
            res = findValue(key).orElse(null);
            if (res != null) {
                replace(key, res);
                return Optional.of(res);
            }
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public OntologyCollectionImpl<O> add(O value) {
        lock.writeLock().lock();
        try {
            put(value);
            return this;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<O> remove(OWLOntologyID key) {
        lock.writeLock().lock();
        try {
            Optional<O> res = get(key);
            res.ifPresent(k -> map.remove(k.getOntologyID()));
            return res;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public OntologyCollectionImpl<O> delete(O value) {
        lock.writeLock().lock();
        try {
            findKey(value).ifPresent(map::remove);
            return this;
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected void replace(OWLOntologyID oldKey, O newValue) {
        map.remove(oldKey);
        put(newValue);
    }

    protected void put(O value) {
        findKey(value).ifPresent(map::remove);
        map.put(value.getOntologyID(), value);
    }

    protected Optional<O> findValue(OWLOntologyID key) {
        return map.values().stream()
                .filter(o -> o.getOntologyID().hashCode() == key.hashCode() && key.equals(o.getOntologyID()))
                .findFirst();
    }

    protected Optional<OWLOntologyID> findKey(O value) {
        return map.entrySet().stream().filter(x -> x.getValue().equals(value)).map(Map.Entry::getKey).findFirst();
    }

    @Override
    public String toString() {
        return values().map(String::valueOf).collect(Collectors.joining(", ", "[", "]"));
    }

}
