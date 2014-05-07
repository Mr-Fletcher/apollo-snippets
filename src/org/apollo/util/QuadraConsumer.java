package org.apollo.util;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an operation that accepts four input arguments and returns no result. This is the
 * four-arity specialization of {@link Consumer}. Unlike most other functional interfaces,
 * {@code QuadraConsumer} is expected to operate via side-effects.
 * <p>
 * This is a <a href="package-summary.html">functional interface</a> whose functional method is
 * {@link #accept(Object, Object, Object)}.
 * </p>
 * 
 * @author Chris Fletcher
 *
 * @param <T>
 *            The type of the first argument to the operation.
 * @param <U>
 *            The type of the second argument to the operation.
 * @param <V>
 *            The type of the third argument to the operation.
 * @param <W>
 *            The type of the fourth argument to the operation.
 *
 * @see Consumer
 */
@FunctionalInterface
public interface QuadraConsumer<T, U, V, W> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t
     *            The first input argument.
     * @param u
     *            The second input argument.
     * @param v
     *            The third input argument.
     * @param w
     *            The fourth input argument.
     */
    void accept(T t, U u, V v, W w);

    /**
     * Returns a composed {@code QuadraConsumer} that performs, in sequence, this operation
     * followed by the {@code after} operation. If performing either operation throws an
     * exception, it is relayed to the caller of the composed operation. If performing this
     * operation throws an exception, the {@code after} operation will not be performed.
     *
     * @param after
     *            The operation to perform after this operation.
     * @return A composed {@code QuadraConsumer} that performs in sequence this operation
     *         followed by the {@code after} operation.
     * @throws NullPointerException
     *             if {@code after} is {@code null}.
     */
    default QuadraConsumer<T, U, V, W> andThen(QuadraConsumer<? super T, ? super U, ? super V, ? super W> after) {
	Objects.requireNonNull(after);
	return (t, r, v, w) -> {
	    accept(t, r, v, w);
	    after.accept(t, r, v, w);
	};
    }
}
