/*
 * Created 21.10.2009
 *
 * Copyright (c) 2009 SLF4J.ORG
 *
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.slf4j.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.Util;

import android.os.Build;
import android.util.Log;

/**
 * An implementation of {@link ILoggerFactory} which always returns
 * {@link AndroidLogger} instances.
 * 
 * @author Thorsten M&ouml;ler
 * @version $Rev:$; $Author:$; $Date:$
 */
public class AndroidLoggerFactory implements ILoggerFactory
{
	private final Map<String, AndroidLogger> loggerMap;

	// tag names cannot be longer on Android platform <= 1.6 / API level <= 4
	// see also android/system/core/include/cutils/property.h and
	// android/frameworks/base/core/jni/android_util_Log.cpp
	static final int TAG_MAX_LENGTH = 23;
	private static final int MIN_API_LEVEL = 7;
	private static final String LEVEL_CONFIG_FILE = "assets/slf4j.properties";
	private static final String CONFIG_KEY_ROOT_LEVEL = "root.level";


	private final Map<String, Integer> levelConf;
	private int rootLevel = Integer.MAX_VALUE;

	public AndroidLoggerFactory()
	{
		loggerMap = new HashMap<String, AndroidLogger>();
		levelConf = new HashMap<String, Integer>();
		loadLevels();
	}

	private void loadLevels()
	{
		// open and load file
		final Properties conf = new Properties();
		try {
			final InputStream in = LoggerFactory.class.getClassLoader()
					.getResourceAsStream(LEVEL_CONFIG_FILE);
			if (null != in) {
				conf.load(in);
			} else {
				Util.report("Can't find logger configuration: please set a "
						+ LEVEL_CONFIG_FILE + " file");
			}
		} catch (final IOException e) {
			Util.report("Can't load logger configuration: please set a "
					+ LEVEL_CONFIG_FILE + " file");
		}
		// retrieve root level
		if (!conf.containsKey(CONFIG_KEY_ROOT_LEVEL)) {
			Util.report("Root level not defined");
		} else {
			rootLevel = getLevelFromLabel(conf
					.getProperty(CONFIG_KEY_ROOT_LEVEL));
		}
		// read logger level configuration
		for (final Entry<Object, Object> e : conf.entrySet()) {
			final String key = (String) e.getKey();
			final String value = (String) e.getValue();
			// ignore "root.level" key
			if (!CONFIG_KEY_ROOT_LEVEL.equals(key)) {
				final Integer level = getLevelFromLabel(value);
				if (null != level) {
					levelConf.put(key, level);
				} else {
					Util.report("Wrong level value for " + key);
				}
			}
		}
	}

	private Integer getLevelFromLabel(final String levelLabel)
	{

		if ("trace".equalsIgnoreCase(levelLabel)) {
			return Log.VERBOSE;
		} else if ("debug".equalsIgnoreCase(levelLabel)) {
			return Log.DEBUG;
		} else if ("info".equalsIgnoreCase(levelLabel)) {
			return Log.INFO;
		} else if ("warn".equalsIgnoreCase(levelLabel)) {
			return Log.WARN;
		} else if ("error".equalsIgnoreCase(levelLabel)) {
			return Log.ERROR;
		} else {
			return null;
		}
	}

	/* @see org.slf4j.ILoggerFactory#getLogger(java.lang.String) */
	public AndroidLogger getLogger(final String name)
	{
		final String actualName;
		if (Build.VERSION.SDK_INT < MIN_API_LEVEL) {
		    actualName = forceValidName(name); // fix for bug #173
		} else {
		    actualName = name;
		}

		AndroidLogger slogger = null;
		// protect against concurrent access of the loggerMap
		synchronized (this)
		{
			slogger = loggerMap.get(actualName);
			if (slogger == null)
			{
				if (!actualName.equals(name)) Log.i(AndroidLoggerFactory.class.getSimpleName(),
					"Logger name '" + name + "' exceeds maximum length of " + TAG_MAX_LENGTH +
					" characters, using '" + actualName + "' instead.");

				slogger = new AndroidLogger(actualName, findLevel(name));
				loggerMap.put(actualName, slogger);
			}
		}
		return slogger;
	}

	/**
	 * Trim name in case it exceeds maximum length of {@value #TAG_MAX_LENGTH} characters.
	 */
	private final String forceValidName(String name)
	{
		if (name != null && name.length() > TAG_MAX_LENGTH)
		{
			final StringTokenizer st = new StringTokenizer(name, ".");
			if (st.hasMoreTokens()) // note that empty tokens are skipped, i.e., "aa..bb" has tokens "aa", "bb"
			{
				final StringBuilder sb = new StringBuilder();
				String token;
				do
				{
					token = st.nextToken();
					if (token.length() == 1) // token of one character appended as is
					{
						sb.append(token);
						sb.append('.');
					}
					else if (st.hasMoreTokens()) // truncate all but the last token
					{
						sb.append(token.charAt(0));
						sb.append("*.");
					}
					else // last token (usually class name) appended as is
					{
						sb.append(token);
					}
				} while (st.hasMoreTokens());

				name = sb.toString();
			}

			// Either we had no useful dot location at all or name still too long.
			// Take leading part and append '*' to indicate that it was truncated
			if (name.length() > TAG_MAX_LENGTH)
			{
				name = name.substring(0, TAG_MAX_LENGTH - 1) + '*';
			}
		}
		return name;
	}

	private Integer findLevel(String name)
	{
		String configuredLoggerName = name;
		Integer level = null;
		while (null != configuredLoggerName
				&& null == (level = levelConf.get(configuredLoggerName))) {
			int i = configuredLoggerName.lastIndexOf(".");
			if (i < 0) {
				// break out
				configuredLoggerName = null;
			} else {
				configuredLoggerName = configuredLoggerName.substring(0, i);
			}
		}
		if (null == level) {
			level = rootLevel;
		}
		return level;
	}
}
