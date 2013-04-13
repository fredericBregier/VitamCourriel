/**
 * This file is part of Vitam Project.
 * 
 * Copyright 2010, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of individual contributors.
 * 
 * All Vitam Project is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Vitam is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Vitam. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.gouv.culture.vitam.utils;

import java.util.Properties;

/**
 * A collection of utility methods to retrieve and parse the values of the Java system properties.
 * 
 * @author "Frederic Bregier"
 */
public final class SystemPropertyUtil {

	private static final Properties props = new Properties();

	// Retrieve all system properties at once so that there's no need to deal with
	// security exceptions from next time. Otherwise, we might end up with logging every
	// security exceptions on every system property access or introducing more complexity
	// just because of less verbose logging.
	static {
		refresh();
	}

	/**
	 * Re-retrieves all system properties so that any post-launch properties updates are retrieved.
	 */
	public static void refresh() {
		Properties newProps = null;
		try {
			newProps = System.getProperties();
		} catch (SecurityException e) {
			System.err
					.println("Unable to retrieve the system properties; default values will be used.");
			newProps = new Properties();
		}

		synchronized (props) {
			props.clear();
			props.putAll(newProps);
		}
	}

	/**
	 * Returns {@code true} if and only if the system property with the specified {@code key}
	 * exists.
	 */
	public static boolean contains(String key) {
		if (key == null) {
			throw new NullPointerException("key");
		}
		return props.containsKey(key);
	}

	/**
	 * Returns the value of the Java system property with the specified {@code key}, while falling
	 * back to {@code null} if the property access fails.
	 * 
	 * @return the property value or {@code null}
	 */
	public static String get(String key) {
		return get(key, null);
	}

	/**
	 * Returns the value of the Java system property with the specified {@code key}, while falling
	 * back to the specified default value if the property access fails.
	 * 
	 * @return the property value. {@code def} if there's no such property or if an access to the
	 *         specified property is not allowed.
	 */
	public static String get(String key, String def) {
		if (key == null) {
			throw new NullPointerException("key");
		}

		String value = props.getProperty(key);
		if (value == null) {
			return def;
		}

		return value;
	}

	/**
	 * Returns the value of the Java system property with the specified {@code key}, while falling
	 * back to the specified default value if the property access fails.
	 * 
	 * @return the property value. {@code def} if there's no such property or if an access to the
	 *         specified property is not allowed.
	 */
	public static boolean getBoolean(String key, boolean def) {
		if (key == null) {
			throw new NullPointerException("key");
		}

		String value = props.getProperty(key);
		if (value == null) {
			return def;
		}

		value = value.trim().toLowerCase();
		if (value.length() == 0) {
			return true;
		}

		if (value.equals("true") || value.equals("yes") || value.equals("1")) {
			return true;
		}

		if (value.equals("false") || value.equals("no") || value.equals("0")) {
			return false;
		}

		// "Unable to parse the boolean system property '" + key + "':" + value + " - "
		// "using the default value: " + def);
		return def;
	}

	/**
	 * Returns the value of the Java system property with the specified {@code key}, while falling
	 * back to the specified default value if the property access fails.
	 * 
	 * @return the property value. {@code def} if there's no such property or if an access to the
	 *         specified property is not allowed.
	 */
	public static int getInt(String key, int def) {
		if (key == null) {
			throw new NullPointerException("key");
		}

		String value = props.getProperty(key);
		if (value == null) {
			return def;
		}

		value = value.trim().toLowerCase();
		if (value.matches("-?[0-9]+")) {
			try {
				return Integer.parseInt(value);
			} catch (Exception e) {
				// Ignore
			}
		}

		// "Unable to parse the integer system property '" + key + "':" + value + " - " +
		// "using the default value: " + def);

		return def;
	}

	/**
	 * Returns the value of the Java system property with the specified {@code key}, while falling
	 * back to the specified default value if the property access fails.
	 * 
	 * @return the property value. {@code def} if there's no such property or if an access to the
	 *         specified property is not allowed.
	 */
	public static long getLong(String key, long def) {
		if (key == null) {
			throw new NullPointerException("key");
		}

		String value = props.getProperty(key);
		if (value == null) {
			return def;
		}

		value = value.trim().toLowerCase();
		if (value.matches("-?[0-9]+")) {
			try {
				return Long.parseLong(value);
			} catch (Exception e) {
				// Ignore
			}
		}

		// "Unable to parse the long integer system property '" + key + "':" + value + " - " +
		// "using the default value: " + def);

		return def;
	}

	/**
	 * Returns the value of the Java system property with the specified {@code key}, while falling
	 * back to the specified default value if the property access fails.
	 * 
	 * @return the property value. {@code def} if there's no such property or if an access to the
	 *         specified property is not allowed.
	 */
	public static String getAndSet(String key, String def) {
		if (key == null) {
			throw new NullPointerException("key");
		}
		if (!props.containsKey(key)) {
			System.setProperty(key, def);
			refresh();
			return def;
		}
		return props.getProperty(key);
	}

	/**
	 * Returns the value of the Java system property with the specified {@code key}, while falling
	 * back to the specified default value if the property access fails.
	 * 
	 * @return the property value. {@code def} if there's no such property or if an access to the
	 *         specified property is not allowed.
	 */
	public static boolean getAndSetBoolean(String key, boolean def) {
		if (key == null) {
			throw new NullPointerException("key");
		}
		if (!props.containsKey(key)) {
			System.setProperty(key, Boolean.toString(def));
			refresh();
			return def;
		}
		return getBoolean(key, def);
	}

	/**
	 * Returns the value of the Java system property with the specified {@code key}, while falling
	 * back to the specified default value if the property access fails.
	 * 
	 * @return the property value. {@code def} if there's no such property or if an access to the
	 *         specified property is not allowed.
	 */
	public static int getAndSetInt(String key, int def) {
		if (key == null) {
			throw new NullPointerException("key");
		}
		if (!props.containsKey(key)) {
			System.setProperty(key, Integer.toString(def));
			refresh();
			return def;
		}
		return getInt(key, def);
	}

	/**
	 * Returns the value of the Java system property with the specified {@code key}, while falling
	 * back to the specified default value if the property access fails.
	 * 
	 * @return the property value. {@code def} if there's no such property or if an access to the
	 *         specified property is not allowed.
	 */
	public static long getAndSetLong(String key, long def) {
		if (key == null) {
			throw new NullPointerException("key");
		}
		if (!props.containsKey(key)) {
			System.setProperty(key, Long.toString(def));
			refresh();
			return def;
		}
		return getLong(key, def);
	}

	/**
	 * Set the value of the Java system property with the specified {@code key} to the specified
	 * default value.
	 * 
	 * @return the ancient value.
	 */
	public static String set(String key, String def) {
		if (key == null) {
			throw new NullPointerException("key");
		}
		String old = null;
		if (props.containsKey(key)) {
			old = props.getProperty(key);
		}
		System.setProperty(key, def);
		refresh();
		return old;
	}

	/**
	 * Set the value of the Java system property with the specified {@code key} to the specified
	 * default value.
	 * 
	 * @return the ancient value.
	 */
	public static boolean setBoolean(String key, boolean def) {
		if (key == null) {
			throw new NullPointerException("key");
		}
		boolean old = false;
		if (props.containsKey(key)) {
			old = getBoolean(key, def);
		}
		System.setProperty(key, Boolean.toString(def));
		refresh();
		return old;
	}

	/**
	 * Set the value of the Java system property with the specified {@code key} to the specified
	 * default value.
	 * 
	 * @return the ancient value.
	 */
	public static int setInt(String key, int def) {
		if (key == null) {
			throw new NullPointerException("key");
		}
		int old = 0;
		if (props.containsKey(key)) {
			old = getInt(key, def);
		}
		System.setProperty(key, Integer.toString(def));
		refresh();
		return old;
	}

	/**
	 * Set the value of the Java system property with the specified {@code key} to the specified
	 * default value.
	 * 
	 * @return the ancient value.
	 */
	public static long setLong(String key, long def) {
		if (key == null) {
			throw new NullPointerException("key");
		}
		long old = 0;
		if (props.containsKey(key)) {
			old = getLong(key, def);
		}
		System.setProperty(key, Long.toString(def));
		refresh();
		return old;
	}

	private SystemPropertyUtil() {
		// Unused
	}
}
