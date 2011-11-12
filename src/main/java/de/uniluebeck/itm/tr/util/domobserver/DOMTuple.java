package de.uniluebeck.itm.tr.util.domobserver;

import de.uniluebeck.itm.tr.util.Tuple;

public class DOMTuple extends Tuple<Object, Object> {

	/**
	 * Constructs a new immutable tuple.
	 *
	 * @param first
	 * 		the first element of the tuple
	 * @param second
	 * 		the second element of the tuple
	 */
	public DOMTuple(Object first, Object second) {
		super();
		this.first = first;
		this.second = second;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final DOMTuple tuple = (DOMTuple) o;

		return !(first != null ? !first.equals(tuple.first) : tuple.first != null) && !(second != null ?
				!second.equals(tuple.second) : tuple.second != null);

	}

	@Override
	public int hashCode() {
		int result = first != null ? first.hashCode() : 0;
		result = 31 * result + (second != null ? second.hashCode() : 0);
		return result;
	}
}
