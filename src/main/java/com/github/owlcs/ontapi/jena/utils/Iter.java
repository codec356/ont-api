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

package com.github.owlcs.ontapi.jena.utils;

import com.github.owlcs.ontapi.jena.OntJenaException;
import org.apache.jena.atlas.iterator.FilterUnique;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.StmtIteratorImpl;
import org.apache.jena.util.iterator.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Misc utils to work with Iterators, Streams, Collections, etc,
 * which are related somehow to Jena or/and used inside {@link com.github.owlcs.ontapi.jena} package.
 * Created by szuev on 11.04.2017.
 *
 * @see ExtendedIterator
 * @see ClosableIterator
 * @see org.apache.jena.atlas.iterator.Iter
 */
public class Iter {

    /**
     * Creates a new sequential {@code Stream} from the given {@code ExtendedIterator}.
     * Takes care about degenerate cases empty and single-element iterator.
     *
     * @param iterator {@link ExtendedIterator} of {@link X}-elements
     * @param <X>      anything
     * @return a {@code Stream} of {@link X}
     * @see #asStream(Iterator)
     */
    public static <X> Stream<X> asStream(ExtendedIterator<? extends X> iterator) {
        if (iterator instanceof NullIterator) {
            return Stream.empty();
        }
        if (iterator instanceof SingletonIterator) {
            return Stream.of(iterator.next());
        }
        return asStream((Iterator<? extends X>) iterator);
    }

    /**
     * Creates a new sequential {@code Stream} from the given {@code Iterator},
     * which is expected to deliver nonnull items:
     * it is required that the operation {@link Iterator#next()} must not return {@code null}
     * if the method {@link Iterator#hasNext()} answers {@code true}.
     * The method {@link Iter#asStream(Iterator, int)} with the second parameter equals {@code 0} can be used to
     * create a {@code Stream} for an iterator that may deliver {@code null}s.
     * <p>
     * If the given parameter is {@link ClosableIterator},
     * do not forget to call {@link Stream#close()} explicitly if the iterator is not exhausted
     * (i.e. in case {@link Iterator#hasNext()} is still {@code true}).
     * It should be done for all short-circuiting terminal operations such as {@link Stream#findFirst()},
     * {@link Stream#findAny()}, {@link Stream#anyMatch(Predicate)} etc.
     *
     * @param iterator {@link Iterator} that delivers nonnull elements, cannot be {@code null}
     * @param <X>      the type of iterator-items
     * @return {@code Stream}
     */
    public static <X> Stream<X> asStream(Iterator<? extends X> iterator) {
        return asStream(iterator, Spliterator.NONNULL);
    }

    /**
     * Constructs a new sequential {@code Stream} from the given {@code Iterator},
     * with the specified {@code characteristics}.
     *
     * If the given parameter is {@link ClosableIterator}, an explicit call to the {@link Stream#close()} method
     * is required for all short-circuiting terminal operations.
     *
     * @param iterator        {@link Iterator}, the {@code Spliterator}'s source, not {@code null}
     * @param characteristics {@code int}, characteristics of the {@code Spliterator}'s source
     * @param <X>             the type of iterator-items
     * @return a non-parallel {@code Stream}, that wraps the {@code iterator} with the given characteristics
     */
    public static <X> Stream<X> asStream(Iterator<? extends X> iterator, int characteristics) {
        return asStream(iterator, -1, characteristics);
    }

    /**
     * Constructs a new sequential {@code Stream} from the given {@code Iterator},
     * with the specified {@code characteristics} and estimated {@code size}.
     *
     * If the given parameter is {@link ClosableIterator}, an explicit call to the {@link Stream#close()} method
     * is required for all short-circuiting terminal operations.

     * @param iterator        {@link Iterator}, the {@code Spliterator}'s source, not {@code null}
     * @param size            {@code long}, a {@code Spliterator}'s estimates size, positive number or {@code -1}
     * @param characteristics {@code int}, characteristics of the {@code Spliterator}'s source
     * @param <X>             the type of iterator-items
     * @return a non-parallel {@code Stream}, that wraps the {@code iterator} with the given parameters
     */
    public static <X> Stream<X> asStream(Iterator<? extends X> iterator, long size, int characteristics) {
        Stream<X> res = StreamSupport.stream(asSpliterator(iterator, size, characteristics), false);
        return iterator instanceof ClosableIterator ? res.onClose(((ClosableIterator<?>) iterator)::close) : res;
    }

    /**
     * Creates a {@code Spliterator} using a given {@code Iterator} as the source of elements.
     * If the {@code size} is not {@code -1}, the returned {@code Spliterator} will report this number
     * as its initial {@link Spliterator#estimateSize() estimated size}.
     *
     * @param iterator        {@link Iterator}, not {@code null}
     * @param size            {@code long}, a positive number or {@code -1}
     * @param characteristics {@code int}, characteristics of the spliterator's source
     * @param <X>             the type of iterator-items
     * @return {@link Spliterator}
     * @throws NullPointerException if the given iterator is {@code null}
     */
    @SuppressWarnings("WeakerAccess")
    public static <X> Spliterator<X> asSpliterator(Iterator<? extends X> iterator, long size, int characteristics) {
        if (size < 0) {
            return Spliterators.spliteratorUnknownSize(iterator, characteristics);
        }
        return Spliterators.spliterator(iterator, size, characteristics);
    }

    /**
     * Creates a {@code Stream} for a future {@code Set}, which is produced by the factory-parameter {@code getAsSet}.
     * The returned {@code Stream} is based on a data-snapshot and it is therefore always safe to use.
     *
     * @param getAsSet {@code Supplier} that produces a {@code Set} of {@link X}
     * @param <X>      the type of items
     * @return <b>distinct</b> sequential {@code Stream}
     * @see Iter#create(Supplier)
     */
    public static <X> Stream<X> fromSet(Supplier<Set<X>> getAsSet) {
        int chs = Spliterator.NONNULL | Spliterator.DISTINCT | Spliterator.IMMUTABLE;
        return asStream(create(() -> getAsSet.get().iterator()), chs);
    }

    /**
     * Creates an iterator which returns RDF Statements based on the given extended iterator of triples.
     *
     * @param triples {@link ExtendedIterator} of {@link Triple}s
     * @param map     a Function to map {@link Triple} -&gt; {@link Statement}
     * @return {@link StmtIterator}
     * @see org.apache.jena.rdf.model.impl.IteratorFactory#asStmtIterator(Iterator, org.apache.jena.rdf.model.impl.ModelCom)
     */
    public static StmtIterator createStmtIterator(ExtendedIterator<Triple> triples, Function<Triple, Statement> map) {
        return new StmtIteratorImpl(triples.mapWith(map));
    }

    /**
     * Creates an unmodifiable Set of {@link Node}s from the collection of {@link RDFNode RDF Node}s.
     * Placed here as it is widely used.
     *
     * @param nodes Collection of {@link RDFNode}s
     * @return Set of {@link Node}
     */
    public static Set<Node> asUnmodifiableNodeSet(Collection<? extends RDFNode> nodes) {
        return nodes.stream().map(FrontsNode::asNode).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns an {@link ExtendedIterator Extended Iterator} consisting of the results of replacing each element of
     * the given {@code base} iterator with the contents of a mapped iterator produced
     * by applying the provided mapping function ({@code map}) to each element.
     * A functional equivalent of {@link Stream#flatMap(Function)}, but for {@link ExtendedIterator}s.
     *
     * @param base   {@link ExtendedIterator} with elements of type {@link F}
     * @param mapper {@link Function} map-function with Object of type of {@link F} (or any super type) as an input,
     *               and an {@link Iterator} of type {@link T} (or any extended type) as an output
     * @param <F>    the element type of the base iterator (from)
     * @param <T>    the element type of the new iterator (to)
     * @return new {@link ExtendedIterator} of type {@link F}
     */
    @SuppressWarnings("unchecked")
    public static <T, F> ExtendedIterator<T> flatMap(ExtendedIterator<F> base,
                                                     Function<? super F, ? extends Iterator<? extends T>> mapper) {
        return WrappedIterator.createIteratorIterator(base.mapWith((Function<F, Iterator<T>>) mapper));
    }

    /**
     * Creates a lazily concatenated {@link ExtendedIterator Extended Iterator} whose elements are all the
     * elements of the first iterator followed by all the elements of the second iterator.
     * A functional equivalent of {@link Stream#concat(Stream, Stream)}, but for {@link ExtendedIterator}s.
     *
     * @param a   the first iterator
     * @param b   the second iterator
     * @param <X> the type of iterator elements
     * @return the concatenation of the two input iterators
     * @see Iter#concat(ExtendedIterator[])
     */
    @SuppressWarnings("unchecked")
    public static <X> ExtendedIterator<X> concat(ExtendedIterator<? extends X> a, ExtendedIterator<? extends X> b) {
        return ((ExtendedIterator<X>) a).andThen(b);
    }

    /**
     * Creates a lazily concatenated {@link ExtendedIterator Extended Iterator} whose elements are all the
     * elements of the given iterators.
     * If the specified array has length equals {@code 2},
     * than this method is equivalent to the method {@link #concat(ExtendedIterator, ExtendedIterator)}).
     * An {@link ExtendedIterator}-based functional equivalent of
     * the expression {@code Stream#of(Stream, ..., Stream).flatMap(Function.identity())}.
     *
     * @param iterators Array of iterators
     * @param <X>       the type of iterator elements
     * @return all input elements as a single {@link ExtendedIterator} of type {@link X}
     * @see Iter#concat(ExtendedIterator, ExtendedIterator)
     */
    @SafeVarargs
    public static <X> ExtendedIterator<X> concat(ExtendedIterator<? extends X>... iterators) {
        ExtendedIterator<X> res = NullIterator.instance();
        for (ExtendedIterator<? extends X> i : iterators) {
            res = res.andThen(i);
        }
        return res;
    }

    /**
     * Returns an extended iterator consisting of the elements of the specified extended iterator
     * that match the given predicate.
     * A functional equivalent of {@link Stream#filter(Predicate)}, but for {@link ExtendedIterator}s.
     *
     * @param iterator  {@link ExtendedIterator} with elements of type {@link X}
     * @param predicate {@link Predicate} to apply to elements of the iterator
     * @param <X>       the element type of the input and output iterators
     * @return a new iterator
     */
    @SuppressWarnings("unchecked")
    public static <X> ExtendedIterator<X> filter(ExtendedIterator<X> iterator, Predicate<? super X> predicate) {
        return iterator.filterKeep((Predicate<X>) predicate);
    }

    /**
     * Returns an {@link ExtendedIterator Extended Iterator} consisting of the elements
     * of the given {@code base} iterator, additionally performing the provided {@code action}
     * on each element as elements are consumed from the resulting iterator.
     * A functional equivalent of {@link Stream#peek(Consumer)}, but for {@link ExtendedIterator}s.
     *
     * @param base   {@link ExtendedIterator} with elements of type {@link X}
     * @param action {@link Consumer} action
     * @param <X>    the element type of the input and output iterators
     * @return new {@link ExtendedIterator} of type {@link X}
     */
    public static <X> ExtendedIterator<X> peek(ExtendedIterator<X> base, Consumer<? super X> action) {
        return base.mapWith(x -> {
            action.accept(x);
            return x;
        });
    }

    /**
     * Returns an {@link ExtendedIterator Extended Iterator} consisting of the distinct elements
     * (according to {@link Object#equals(Object)}) of the given iterator.
     * A functional equivalent of {@link Stream#distinct()}, but for {@link ExtendedIterator}s.
     * Warning: the result is temporary stored in memory!
     *
     * @param base {@link ExtendedIterator} with elements of type {@link X}
     * @param <X>  the element type of the input and output iterators
     * @return new {@link ExtendedIterator} of type {@link X} without duplicates
     */
    public static <X> ExtendedIterator<X> distinct(ExtendedIterator<X> base) {
        return base.filterKeep(new FilterUnique<>());
    }

    /**
     * Returns whether any elements of the given iterator match the provided predicate.
     * A functional equivalent of {@link Stream#anyMatch(Predicate)}, but for {@link Iterator}s.
     *
     * @param iterator  {@link Iterator} with elements of type {@link X}
     * @param predicate {@link Predicate} to apply to elements of the iterator
     * @param <X>       the element type of the iterator
     * @return {@code true} if any elements of the stream match the provided predicate, otherwise {@code false}
     * @see Iter#allMatch(Iterator, Predicate)
     * @see Iter#noneMatch(Iterator, Predicate)
     */
    public static <X> boolean anyMatch(Iterator<X> iterator, Predicate<? super X> predicate) {
        if (iterator instanceof NullIterator) return false;
        try {
            while (iterator.hasNext()) {
                if (predicate.test(iterator.next())) return true;
            }
        } finally {
            close(iterator);
        }
        return false;
    }

    /**
     * Returns whether all elements of the given iterator match the provided predicate.
     * A functional equivalent of {@link Stream#allMatch(Predicate)}, but for {@link Iterator}s.
     *
     * @param iterator  {@link Iterator} with elements of type {@link X}
     * @param predicate {@link Predicate} to apply to elements of the iterator
     * @param <X>       the element type of the iterator
     * @return {@code true} if either all elements of the iterator match the provided predicate
     * or the iterator is empty, otherwise {@code false}
     * @see Iter#anyMatch(Iterator, Predicate)
     * @see Iter#noneMatch(Iterator, Predicate)
     */
    public static <X> boolean allMatch(Iterator<X> iterator, Predicate<? super X> predicate) {
        if (iterator instanceof NullIterator) return true;
        try {
            while (iterator.hasNext()) {
                if (!predicate.test(iterator.next())) return false;
            }
        } finally {
            close(iterator);
        }
        return true;
    }

    /**
     * Returns whether no elements of the given iterator match the provided predicate.
     * A functional equivalent of {@link Stream#noneMatch(Predicate)}, but for {@link Iterator}s.
     *
     * @param iterator  {@link Iterator} with elements of type {@link X}
     * @param predicate {@link Predicate} to apply to elements of the iterator
     * @param <X>       the element type of the iterator
     * @return {@code true} if either no elements of the iterator match the provided predicate
     * or the iterator is empty, otherwise {@code false}
     * @see Iter#anyMatch(Iterator, Predicate)
     * @see Iter#allMatch(Iterator, Predicate)
     */
    public static <X> boolean noneMatch(Iterator<X> iterator, Predicate<? super X> predicate) {
        return allMatch(iterator, predicate.negate());
    }

    /**
     * Returns an {@link Optional} describing the first element of the iterator,
     * or an empty {@code Optional} if the iterator is empty.
     * A functional equivalent of {@link Stream#findFirst()}, but for {@link Iterator}s.
     * Warning: the method closes the specified iterator, so it is no possible to reuse it after calling this method.
     *
     * @param iterator {@link Iterator}, not {@code null}
     * @param <X>      the element type of the iterator
     * @return {@link Optional} of {@link X}
     * @throws NullPointerException if the element selected is {@code null}
     */
    public static <X> Optional<X> findFirst(Iterator<X> iterator) {
        if (iterator instanceof NullIterator) return Optional.empty();
        try {
            return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.empty();
        } finally {
            close(iterator);
        }
    }

    /**
     * Returns the count of elements in the given iterator.
     * A functional equivalent of {@link Stream#count()}, but for {@link Iterator}s.
     * Warning: the method closes the specified iterator, so it is no possible to reuse it after.
     *
     * @param iterator {@link Iterator}, not {@code null}
     * @return long, the count of elements in the given {@code iterator}
     */
    public static long count(Iterator<?> iterator) {
        long res = 0;
        while (iterator.hasNext()) {
            iterator.next();
            res++;
        }
        return res;
    }

    /**
     * Puts all of the remaining items of the given iterator into the {@code collection},
     * and returns this collection itself.
     * This is a terminal operation.
     *
     * @param <X>        the element type of the iterator, not {@code null}
     * @param <C>        the {@code Collection} type, not {@code null}
     * @param iterator   the {@code Iterator} with elements of type {@link X}
     * @param collection the collection of type {@link C}
     * @return {@link C}, the same instance as specified
     */
    public static <X, C extends Collection<X>> C addAll(Iterator<? extends X> iterator, C collection) {
        iterator.forEachRemaining(collection::add);
        return collection;
    }

    /**
     * Returns a {@code Map} (of the type of {@link M})
     * whose keys and values are the result of applying the provided mapping functions to the input elements.
     * A functional equivalent of {@code stream.collect(Collectors.toMap(...))}, but for plain {@link Iterator}s.
     * This method makes no guarantees about synchronization or atomicity properties of it.
     *
     * @param iterator      input elements in the form of {@link Iterator}
     * @param keyMapper     a mapping function to produce keys
     * @param valueMapper   a mapping function to produce values
     * @param mergeFunction a merge function, used to resolve collisions between values associated with the same key,
     *                      as supplied to {@link Map#merge(Object, Object, BiFunction)}
     * @param mapSupplier   a function which returns a new, empty {@code Map} into which the results will be inserted
     * @param <X>           the type of the input elements
     * @param <K>           the output type of the key mapping function
     * @param <V>           the output type of the value mapping function
     * @param <M>           the type of the resulting {@code Map}
     * @return a {@code Map} whose keys are the result of applying a key mapping function to the input elements,
     * and whose values are the result of applying a value mapping function to all input elements
     * equal to the key and combining them using the merge function
     * @see Collectors#toMap(Function, Function, BinaryOperator, Supplier)
     */
    public static <X, K, V, M extends Map<K, V>> M toMap(Iterator<X> iterator,
                                                         Function<? super X, ? extends K> keyMapper,
                                                         Function<? super X, ? extends V> valueMapper,
                                                         BinaryOperator<V> mergeFunction,
                                                         Supplier<M> mapSupplier) {
        M res = mapSupplier.get();
        while (iterator.hasNext()) {
            X x = iterator.next();
            K k = keyMapper.apply(x);
            V v = valueMapper.apply(x);
            res.merge(k, v, mergeFunction);
        }
        return res;
    }

    /**
     * Closes iterator if it is {@link ClosableIterator CloseableIterator}.
     *
     * @param iterator {@link Iterator}
     */
    public static void close(Iterator<?> iterator) {
        if (iterator instanceof ClosableIterator) {
            ((ClosableIterator<?>) iterator).close();
        }
    }

    /**
     * Creates a new {@link ExtendedIterator Extended Iterator}} containing the specified elements.
     *
     * @param members Array of elements of the type {@link X}
     * @param <X>     the element type of the new iterator
     * @return a fresh {@link ExtendedIterator} instance
     */
    @SafeVarargs // Creating an iterator from an array is safe
    public static <X> ExtendedIterator<X> of(X... members) {
        return create(Arrays.asList(members));
    }

    /**
     * Creates a new {@link ExtendedIterator Extended Iterator}} containing nothing.
     *
     * @param <X> the element type of the new iterator
     * @return a fresh {@link ExtendedIterator} instance
     */
    public static <X> ExtendedIterator<X> of() {
        return NullIterator.instance();
    }

    /**
     * Creates a new {@link ExtendedIterator Extended Iterator}} containing single specified element.
     *
     * @param item - an object of type {@link X}
     * @param <X>  the element type of the new iterator
     * @return a fresh {@link ExtendedIterator} instance
     */
    public static <X> ExtendedIterator<X> of(X item) {
        return new SingletonIterator<>(item);
    }

    /**
     * Creates a new {@link ExtendedIterator Extended Iterator}} over all elements of the specified collection.
     *
     * @param members {@code Collection} of elements of the type {@link X}
     * @param <X>     the element type of the new iterator
     * @return a fresh {@link ExtendedIterator} instance
     */
    public static <X> ExtendedIterator<X> create(Collection<? extends X> members) {
        return members.isEmpty() ? NullIterator.instance() : create(members.iterator());
    }

    /**
     * Answers an {@code ExtendedIterator} returning the elements of the specified {@code iterator}.
     * If the given {@code iterator} is itself an {@code ExtendedIterator}, return that;
     * otherwise wrap {@code iterator}.
     *
     * @param iterator {@link Iterator}, not {@code null}
     * @param <X>      the element type of the iterator
     * @return {@link ExtendedIterator} instance
     */
    @SuppressWarnings("unchecked")
    public static <X> ExtendedIterator<X> create(Iterator<? extends X> iterator) {
        return (ExtendedIterator<X>) WrappedIterator.create(iterator);
    }

    /**
     * Creates a new {@link ExtendedIterator Extended Iterator}} over all elements of an iterator
     * which will be created by the {@code provider} on first iteration.
     * The returned iterator does not contains any elements,
     * but they will be derived at once when calling any of the {@code ExtendedIterator} methods.
     * <p>
     * The idea is to provide a truly lazy iterator
     * and, subsequently, a stream (through the {@link #asStream(Iterator)} method).
     * When any distinct operation (i.e. {@link #distinct(ExtendedIterator)} or {@link Stream#distinct()}) is used,
     * it, in fact, collects on demand an in-memory {@code Set} containing all elements,
     * but it will be appeared in process and an iterator or a stream initially weighs nothing.
     * This method allows to achieve a similar behaviour:
     * when creating an {@code ExtendedIterator} does not weight anything,
     * but it materializes itself when processing.
     * <p>
     * The returned iterator is not thread-safe, just as like any other RDF extended iterator, with whom we work.
     *
     * @param provider {@link Supplier} deriving nonnull {@link Iterator}, cannot be {@code null}
     * @param <X>      the element type of the new iterator
     * @return a fresh {@link ExtendedIterator} instance wrapping a feature iterator
     */
    public static <X> ExtendedIterator<X> create(Supplier<Iterator<? extends X>> provider) {
        Objects.requireNonNull(provider);
        return new NiceIterator<X>() {
            private Iterator<? extends X> base;

            Iterator<? extends X> base() {
                return base == null ? base = OntJenaException.notNull(provider.get()) : base;
            }

            @Override
            public boolean hasNext() {
                return base().hasNext();
            }

            @Override
            public X next() {
                return base().next();
            }

            @Override
            public void remove() {
                base().remove();
            }

            @Override
            public void close() {
                if (base != null) {
                    close(base);
                }
            }
        };
    }

}
