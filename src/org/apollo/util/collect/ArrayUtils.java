package org.apollo.util.collect;

import static java.lang.String.valueOf;
import static java.lang.reflect.Array.newInstance;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A class that provides various static utility methods for arrays. This class is supposed to
 * function as an addition to the {@link java.util.Arrays} class, not as a replacement.
 * 
 * @author Chris Fletcher
 */
public final class ArrayUtils {

    /**
     * Empirically determined point at which the average cost of a JNI call exceeds the expense
     * of an element-by-element copy. This number may change over time.
     */
    private static final int JNI_COPY_ARRAY_THRESHOLD = 6;

    /**
     * Short-hand method for deciding whether or not to use the native
     * {@link System#arraycopy(Object, int, Object, int, int)} method or an element-by-element
     * copy, based on the {@link #JNI_COPY_ARRAY_THRESHOLD}.
     * 
     * @param src
     *            The source array.
     * @param srcPos
     *            The source starting position.
     * @param dest
     *            The destination array.
     * @param destPos
     *            The destination starting position.
     * @param length
     *            The amount of bytes to copy.
     */
    private static <T> void arraycopy(T[] src, int srcPos, T[] dest, int destPos, int length) {
	if (length >= JNI_COPY_ARRAY_THRESHOLD) {
	    System.arraycopy(src, srcPos, dest, destPos, length);
	} else {
	    for (int i = 0; i < length; i++) {
		dest[destPos + i] = src[srcPos + i];
	    }
	}
    }

    /**
     * Invokes, over each element in the specified array, the specified action.
     * 
     * @param action
     *            The action that is to be invoked. This {@link Consumer} accepts one argument,
     *            which is the current element.
     * @param array
     *            The array over which the action is invoked.
     * @throws NullPointerException
     *             if action is {@code null}.
     */
    @SafeVarargs
    public static <T> void forEach(Consumer<? super T> action, T... array) {
	Objects.requireNonNull(action);
	for (T t : array)
	    action.accept(t);
    }

    /**
     * Invokes, over each element in the specified array, the specified action.
     * 
     * @param action
     *            The action that is to be invoked. This {@link BiConsumer} accepts two
     *            arguments: the array index and the element at the index.
     * @param array
     *            The array over which the action is invoked.
     * @throws NullPointerException
     *             if action is {@code null}.
     */
    @SafeVarargs
    public static <T> void forEach(BiConsumer<Integer, ? super T> action, T... array) {
	Objects.requireNonNull(action);

	int length = array.length;
	for (int i = 0; i < length; i++)
	    action.accept(i, array[i]);
    }

    /**
     * Creates a new array with the specified length. Each element in the returned array will
     * have the specified default value.
     * 
     * @param length
     *            The length of the new array.
     * @param defaultValue
     *            The default element value at the index of each array.
     * @return The newly created array.
     * @throws NullPointerException
     *             if the default value is {@code null}.
     */
    public static <T> T[] newArray(final int length, final T defaultValue) {
	Objects.requireNonNull(defaultValue);

	@SuppressWarnings("unchecked")
	T[] array = (T[]) newInstance(defaultValue.getClass(), length);
	for (int i = 0; i < length; i++)
	    array[i] = defaultValue;

	return array;
    }

    /**
     * Creates a new array with the specified length. Each element in the returned array will
     * have the specified default value.
     * 
     * @param length
     *            The length of the new array.
     * @param defaultValue
     *            The default element value at the index of each array.
     * @return The newly created array.
     * @throws NullPointerException
     *             if the default value is {@code null}.
     */
    public static int[] newArray(final int length, final int defaultValue) {
	switch (length) {
	case 0:
	    return new int[0];
	case 1:
	    return new int[] { defaultValue };
	case 2:
	    return new int[] { defaultValue, defaultValue };
	case 3:
	    return new int[] { defaultValue, defaultValue, defaultValue };
	default:
	    final int[] array = new int[length];
	    for (int i = 0; i < length; i++)
		array[i] = defaultValue;

	    return array;
	}
    }

    /**
     * Replaces a specific index within an array with the specified replacement.
     * 
     * @param array
     *            The array to be altering.
     * @param index
     *            The index within the array.
     * @param replacement
     *            The object that will be replacing the object currently found at the specified
     *            index.
     * @return The object that was previously found at the specified index ({@code null} if
     *         none was).
     * @throws ArrayIndexOutOfBoundsException
     *             if the specified index is out of bounds.
     */
    public static <T> T replace(T[] array, int index, T replacement) {
	T old = array[index];
	array[index] = replacement;
	return old;
    }

    /**
     * Returns a randomly selected element from the specified {@code int} array, as defined by
     * {@link Math#random()}.
     * 
     * @param array
     *            The array to be selecting a random element from.
     * @return The randomly selected element.
     */
    public static int random(int... array) {
	switch (array.length) {
	case 0:
	    return 0;
	case 1:
	    return array[0];
	default:
	    int selection = (int) (Math.random() * array.length);
	    return array[selection];
	}
    }

    /**
     * Returns a randomly selected element from the specified array, as defined by
     * {@link Math#random()}.
     * 
     * @param array
     *            The array to be selecting a random element from.
     * @return The randomly selected element.
     */
    @SafeVarargs
    public static <T> T random(T... array) {
	switch (array.length) {
	case 0:
	    return null;
	case 1:
	    return array[0];
	default:
	    int selection = (int) (Math.random() * array.length);
	    return array[selection];
	}
    }

    /**
     * Concatenates all arrays of type {@code T} to a single one, using the system's array
     * copying functionality. Changes made to any of the source arrays will never be reflected
     * in the returned one, or vice versa.
     * 
     * @param arrays
     *            The arrays that are to be concatenated.
     * @return A concatenation of all arrays.
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T> T[] concat(T[]... arrays) {
	int length = arrays.length;
	if (length == 0)
	    return (T[]) new Object[0];
	else if (length == 1)
	    return arrays[0].clone();

	length = 0;
	for (T[] array : arrays)
	    length += array.length;

	Class<?> type = arrays.getClass().getComponentType().getComponentType();
	final T[] result = (T[]) newInstance(type, length);
	int offset = 0;
	for (T[] array : arrays) {
	    int len = array.length;

	    if (len == 0)
		continue;
	    if (len == 1) {
		result[offset++] = array[0];
		continue;
	    }

	    arraycopy(array, 0, result, offset, len);
	    offset += len;
	}
	return result;
    }

    /**
     * Uses the specified {@link Function} to convert the specified array of type {@code S} to
     * a primitive-{@code int} array.
     * 
     * @param converter
     *            The function that converts the element types.
     * @param array
     *            The array that needs to be converted.
     * @return The converted array.
     */
    @SafeVarargs
    public static <S> int[] convert(Function<S, Integer> converter, S... array) {
	int length = array.length;
	int[] result = new int[length];

	if (length == 0)
	    return result;
	if (length == 1) {
	    result[0] = converter.apply(array[0]);
	    return result;
	}

	for (int i = 0; i < length; i++)
	    result[i] = converter.apply(array[i]);

	return result;
    }

    /**
     * Uses the specified {@link Function} to convert the specified array of type {@code S} to
     * an array of type {@code D}.
     * 
     * @param type
     *            The class type of the destination element type.
     * @param converter
     *            The function that converts the element types.
     * @param array
     *            The array that needs to be converted.
     * @return The converted array.
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <S, D> D[] convert(Class<D> type, Function<S, D> converter, S... array) {
	int length = array.length;
	D[] result = (D[]) newInstance(type, length);

	if (length == 0)
	    return result;
	if (length == 1) {
	    result[0] = converter.apply(array[0]);
	    return result;
	}

	for (int i = 0; i < length; i++)
	    result[i] = converter.apply(array[i]);

	return result;
    }

    /**
     * Uses the specified {@link Function} to convert the specified arrays of type {@code S} to
     * a single array of type {@code D}.
     * 
     * @param type
     *            The class type of the destination element type.
     * @param converter
     *            The function that converts the element types.
     * @param arrays
     *            The array that needs to be converted.
     * @return The converted array.
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <S, D> D[] convert(Class<D> type, Function<S, D> converter, S[]... arrays) {
	int length = arrays.length;
	if (length == 1)
	    return convert(type, converter, arrays[0]);

	D[] result = (D[]) newInstance(type, length);
	if (length == 0)
	    return result;

	length = 0;
	for (S[] array : arrays)
	    length += array.length;

	int offset = 0;
	for (S[] array : arrays) {
	    length = array.length;
	    if (length == 0)
		continue;

	    for (int i = offset; i < length; i++)
		result[offset + 1] = converter.apply(array[i]);

	    offset += length;
	}
	return result;
    }

    /**
     * Concatenates the specified array into a single string, using the specified string as
     * delimiter. The values of the array as obtained as specified by
     * {@link String#valueOf(Object)}.
     * 
     * @param delimiter
     *            The delimiter of the array (may be {@code null} for no delimiter).
     * @param array
     *            The array that is to be concatenated.
     * @return The resulting string.
     */
    public static <T> String toString(String delimiter, T[] array) {
	int length = array.length;
	switch (length) {
	case 0:
	    return "";
	case 1:
	    return valueOf(array[0]);
	case 2:
	    return valueOf(array[0]) + ((delimiter != null) ? delimiter : "") + valueOf(array[1]);
	default:
	    String first = valueOf(array[0]);

	    int last = (length - 1);
	    StringBuilder sb = new StringBuilder((first.length() + delimiter.length()) * length);

	    sb.append(first).append(delimiter);
	    for (int i = 1; i < length; i++) {
		sb.append(array[i]);
		if (i == last)
		    return sb.toString();

		if (delimiter != null)
		    sb.append(delimiter);
	    }
	}
	throw new IllegalStateException();
    }

    /**
     * Concatenates the specified array into a single string, using the specified string as
     * delimiter. The values of the array as obtained as specified by
     * {@link String#valueOf(Object)}.
     * 
     * @param delimiter
     *            The delimiter of the array (may be {@code null} for no delimiter).
     * @param array
     *            The array that is to be concatenated.
     * @return The resulting string.
     */
    public static String toString(String delimiter, int[] array) {
	int length = array.length;
	switch (length) {
	case 0:
	    return "";
	case 1:
	    return valueOf(array[0]);
	case 2:
	    return array[0] + ((delimiter != null) ? delimiter : "") + array[1];
	default:
	    String first = valueOf(array[0]);

	    int last = (length - 1);
	    StringBuilder sb = new StringBuilder((first.length() + delimiter.length()) * length);

	    sb.append(first).append(delimiter);
	    for (int i = 1; i < length; i++) {
		sb.append(array[i]);
		if (i == last)
		    return sb.toString();

		if (delimiter != null)
		    sb.append(delimiter);
	    }
	}
	throw new IllegalStateException();
    }

    /**
     * Concatenates the specified array into a single string, using the specified string as
     * delimiter. The values of the array as obtained as specified by
     * {@link String#valueOf(Object)}.
     * 
     * @param delimiter
     *            The delimiter of the array (may be {@code null} for no delimiter).
     * @param array
     *            The array that is to be concatenated.
     * @return The resulting string.
     */
    public static String toString(String delimiter, long[] array) {
	int length = array.length;
	switch (length) {
	case 0:
	    return "";
	case 1:
	    return valueOf(array[0]);
	case 2:
	    return array[0] + ((delimiter != null) ? delimiter : "") + array[1];
	default:
	    String first = valueOf(array[0]);

	    final int last = (length - 1);
	    final StringBuilder sb = new StringBuilder((first.length() + delimiter.length()) * length);

	    sb.append(first).append(delimiter);
	    for (int i = 1; i < length; i++) {
		sb.append(array[i]);
		if (i == last)
		    return sb.toString();

		if (delimiter != null)
		    sb.append(delimiter);
	    }
	}
	throw new IllegalStateException();
    }

    /**
     * Performs a simple for-each loop on the specified array, returning {@code true} if the
     * specified value was found using the {@link Object#equals(Object) equality method}.
     * 
     * @param value
     *            The value to be searching for.
     * @param array
     *            The array to be searching.
     * @return {@code true} if the value was found, {@code false} otherwise.
     */
    @SafeVarargs
    public static <T> boolean search(final T value, final T... array) {
	if (array.length == 0)
	    return false;

	if (value == null)
	    return Stream.of(array).anyMatch(e -> e == null);

	return Stream.of(array).anyMatch(e -> e != null && e.equals(value));
    }

    /**
     * Counts all entries in the specified array that aren't {@code null}.
     * 
     * @param array
     *            The array to be counting the non-{@code null} entries of.
     * @return The amount of array entries that aren't {@code null}.
     */
    @SafeVarargs
    public static <T> int countNonNull(T... array) {
	return (array.length - countNull(array));
    }

    /**
     * Counts all entries in the specified array that are {@code null}.
     * 
     * @param array
     *            The array to be counting the {@code null} entries of.
     * @return The amount of array entries that are {@code null}.
     */
    @SafeVarargs
    public static <T> int countNull(T... array) {
	switch (array.length) {
	case 0:
	    return 0;
	case 1:
	    return (array[0] == null) ? 1 : 0;
	default:
	    int count = 0;
	    for (T t : array) {
		if (t == null)
		    count++;
	    }
	    return count;
	}
    }

    /**
     * Counts, in parallel, all entries in the specified array that aren't {@code null}.
     * Parellel array operations are useful when working with larger arrays on a multi-core
     * machine.
     * 
     * @param array
     *            The array to be counting the non-{@code null} entries of.
     * @return The amount of array entries that aren't {@code null}.
     */
    @SafeVarargs
    public static <T> int parallelCountNonNull(T... array) {
	return (array.length - parallelCountNull(array));
    }

    /**
     * Counts, in parallel, all entries in the specified array that are {@code null}. Parellel
     * array operations are useful when working with larger arrays on a multi-core machine.
     * 
     * @param array
     *            The array to be counting the {@code null} entries of.
     * @return The amount of array entries that are {@code null}.
     */
    @SafeVarargs
    public static <T> int parallelCountNull(T... array) {
	switch (array.length) {
	case 0:
	    return 0;
	case 1:
	    return (array[0] == null) ? 1 : 0;
	default:
	    AtomicInteger count = new AtomicInteger();
	    Arrays.stream(array).parallel().forEach(element -> {
		if (element == null)
		    count.incrementAndGet();
	    });
	    return count.intValue();
	}
    }

    /**
     * Default private constructor to prevent external instantiation.
     */
    private ArrayUtils() {
    }

}
