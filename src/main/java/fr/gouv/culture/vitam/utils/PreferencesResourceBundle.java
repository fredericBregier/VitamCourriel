/**
 * This file is part of Vitam Project.
 * 
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the
 * COPYRIGHT.txt in the distribution for a full listing of individual contributors.
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

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Text label handler
 * 
 * @author "Frederic Bregier"
 * 
 */
public class PreferencesResourceBundle {
	private ResourceBundle labels;

	/**
	 * Use the local to choose the correct resource
	 * 
	 * @param locale
	 */
	public PreferencesResourceBundle(String path, Locale locale) {
		this.labels = ResourceBundle.getBundle(path, locale);
	}

	/**
	 * Use the local to choose the correct resource
	 * 
	 * @param locale
	 */
	public PreferencesResourceBundle(Locale locale) {
		this.labels = ResourceBundle.getBundle("resources/Vitam", locale);
	}

	/**
	 * Return the label for the corresponding local
	 * 
	 * @param key
	 *            Label
	 * @return the label
	 **/
	public String get(String key) {
		if (labels != null) {
			String value = null;
			try {
				value = labels.getString(key);
			} catch (NullPointerException e) {
				return key;
			} catch (MissingResourceException e) {
				return key;
			} catch (ClassCastException e) {
				return key;
			}
			if (value == null) {
				value = key;
			}
        	return value;
		} else {
			return key;
		}
	}

};
