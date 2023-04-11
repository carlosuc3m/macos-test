/*-
 * #%L
 * Use deep learning frameworks from Java in an agnostic and isolated way.
 * %%
 * Copyright (C) 2022 - 2023 Institut Pasteur and BioImage.IO developers.
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the BioImage.io nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
/**
 * 
 */
package io.bioimage.modelrunner.engine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import io.bioimage.modelrunner.exceptions.LoadEngineException;

/**
 * @author Carlos Garcia Lopez de Haro
 */
public class EngineLoader extends ClassLoader
{

	/**
	 * Child ClassLoader of the System ClassLoader that adds the JARs needed to
	 * the classes
	 */
	private ClassLoader engineClassloader;

	/**
	 * Child ClassLoader of the System ClassLoader that adds the JARs needed to
	 * the classes
	 */
	private ClassLoader baseClassloader;

	/**
	 * Path to the folder containing all the jars needed to load the
	 * corresponding engine
	 */
	private String enginePath;

	/**
	 * Engine name of the Deep Learning framework.
	 */
	private String engine;

	/**
	 * Engine name plus major version of the Deep Learning framework.
	 *
	 * Key for the cache of loaded engines.
	 */
	private String versionedEngine;

	/**
	 * Instance of the class from the wanted Deep Learning engine that is used
	 * to call all the needed methods to execute a model
	 */
	private DeepLearningEngineInterface engineInstance;

	/**
	 * Name of the interface all the engines have to implement
	 */
	private static final String ENGINE_INTERFACE_NAME = 
			"io.bioimage.modelrunner.engine.DeepLearningEngineInterface";

	/**
	 * HashMap containing all the already loaded ClassLoader. This variables
	 * avoids reloading classes that have already been loaded. Reloading already
	 * existing ClassLaoders can be a problem if the loaded class opens an
	 * external native library because this library will not be freed until the
	 * Classes from the ClassLoader are Garbage Collected
	 */
	private static HashMap< String, ClassLoader > loadedEngines = new HashMap< String, ClassLoader >();

	/**
	 * Create a ClassLaoder that contains the classes of the parent ClassLoader
	 * given as an input and the classes found in the String path given
	 * 
	 * @param classloader
	 *            parent ClassLoader of the wanted ClassLoader
	 * @param engineInfo
	 *            object containing all the needed info to load a Deep LEarning
	 *            framework
	 * @throws LoadEngineException
	 *             if there are errors loading the DL framework
	 * @throws Exception
	 *             if the DL engine does not contain all the needed libraries
	 */
	private EngineLoader( ClassLoader classloader, EngineInfo engineInfo ) throws LoadEngineException, Exception
	{
		super();
		this.baseClassloader = classloader;
		this.enginePath = engineInfo.getDeepLearningVersionJarsDirectory();
		this.engine = engineInfo.getEngine();
		this.versionedEngine = this.engine + engineInfo.getMajorVersion();
		loadClasses();
		setEngineClassLoader();
		setEngineInstance();
		setBaseClassLoader();
	}

	/**
	 * Returns the ClassLoader of the corresponding Deep Learning framework
	 * (engine)
	 * 
	 * @param classloader
	 *            parent ClassLoader of the wanted ClassLoader
	 * @param engineInfo
	 *            the path to the directory where all the JARs needed to load
	 *            the corresponding Deep Learning framework (engine) are stored
	 * @return the ClassLoader corresponding to the wanted Deep Learning version
	 * @throws LoadEngineException
	 *             if there are errors loading the DL framework
	 * @throws Exception
	 *             if the DL engine does not contain all the needed libraries
	 */
	public static EngineLoader createEngine( ClassLoader classloader, EngineInfo engineInfo )
			throws LoadEngineException, Exception
	{
		return new EngineLoader( classloader, engineInfo );
	}

	/**
	 * Load the needed JAR files into a child ClassLoader of the
	 * ContextClassLoader.The JAR files needed are the JARs that contain the
	 * engine and the JAR containing this class
	 * 
	 * @throws URISyntaxException
	 *             if there is an error creating an URL
	 * @throws MalformedURLException
	 *             if theURL is incorrect
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
	private void loadClasses()
			throws URISyntaxException, MalformedURLException, ClassNotFoundException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		// If the ClassLoader was already created, use it
		if ( loadedEngines.get( versionedEngine ) != null )
		{
			this.engineClassloader = loadedEngines.get( versionedEngine );
			return;
		}
		ArrayList<URL> urlList = new ArrayList<URL>();
		// TODO  remove URL[] urls = new URL[ new File( this.enginePath ).listFiles().length ];
		// TODO remove int c = 0;
		System.out.println(enginePath);
		for ( File ff : new File( this.enginePath ).listFiles() )
		{
			if (!ff.getName().endsWith(".jar"))
					continue;
			urlList.add(ff.toURI().toURL());
		}
		URL[] urls = new URL[urlList.size()];
		urlList.toArray(urls);
		this.engineClassloader = new URLClassLoader( urls, baseClassloader );

		loadedEngines.put( this.versionedEngine, this.engineClassloader );
	}

	/**
	 * Set the ClassLoader containing the engines classes as the Thread
	 * classloader
	 */
	public void setEngineClassLoader()
	{
		Thread.currentThread().setContextClassLoader( this.engineClassloader );
	}

	/**
	 * Set the parent ClassLoader as the Thread classloader
	 */
	public void setBaseClassLoader()
	{
		Thread.currentThread().setContextClassLoader( this.baseClassloader );
	}

	/**
	 * Find the wanted interface {@link DeepLearningEngineInterface} from the entries
	 * of a JAR file. REturns null if the interface is not in the entries
	 * 
	 * @param entries
	 *            entries of a JAR executable file
	 * @return the wanted class implementing the interface or null if it is not
	 *         there
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	private static DeepLearningEngineInterface getEngineClassFromEntries( Enumeration< ? extends ZipEntry > entries,
			ClassLoader engineClassloader )
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
	{
		while ( entries.hasMoreElements() )
		{
			ZipEntry entry = ( ZipEntry ) entries.nextElement();
			String file = entry.getName();
			if ( file.endsWith( ".class" ) && !file.contains( "$" ) && !file.contains( "-" ) )
			{
				String className = getClassNameInJAR( file );
				Class< ? > c = engineClassloader.loadClass( className );
				Class[] intf = c.getInterfaces();
				for ( int j = 0; j < intf.length; j++ )
				{
					if ( intf[ j ].getName().equals( ENGINE_INTERFACE_NAME ) )
					{
						// Assume that DeepLearningInterface has no arguments
						// for the constructor
						return ( DeepLearningEngineInterface ) c.newInstance();
					}
				}
				// REmove references
				intf = null;
				c = null;
			}
		}
		return null;
	}

	/**
	 * Return the name of the class as seen by the ClassLoader from the name of
	 * the file entry in the JAR file. Basically removes the .class suffix and
	 * substitutes "/" by ".".
	 * 
	 * @param entryName
	 *            String containing the name of the file compressed inside the
	 *            JAR file
	 * @return the Class name as seen by the ClassLoader
	 */
	public static String getClassNameInJAR( String entryName )
	{
		String className = entryName.substring( 0, entryName.indexOf( "." ) );
		className = className.replace( "/", "." );
		return className;
	}

	/**
	 * Finds the class that implements the interface that connects the java
	 * framework with the deep learning libraries
	 * 
	 * @throws LoadEngineException
	 *             if there is any error finding and loading the DL libraries
	 */
	private void setEngineInstance() throws LoadEngineException
	{
		// Load all the classes in the engine folder and select the wanted
		// interface
		ZipFile jarFile;
		String jarPrefix = "dl-modelrunner-" + this.engine;
		String errMsg = "Missing " + jarPrefix + " jar file that implements the 'DeepLearningInterface";
		try
		{
			for ( File ff : new File( this.enginePath ).listFiles() )
			{
				// Find the correct dl-modelrunner-<engine> JAR file.
				if (!ff.getName().endsWith(".jar") ||
						!ff.getName().startsWith(jarPrefix + "-"))
				{
					continue;
				}
				jarFile = new ZipFile( ff );
				Enumeration< ? extends ZipEntry > entries = jarFile.entries();
				this.engineInstance = getEngineClassFromEntries( entries, engineClassloader );
				if ( this.engineInstance != null )
				{
					jarFile.close();
					return;
				}
				jarFile.close();
			}
		}
		catch ( IOException | ClassNotFoundException | InstantiationException
				| IllegalAccessException | IllegalArgumentException 
				| InvocationTargetException | NoSuchMethodException | SecurityException e )
		{
			errMsg = e.getCause().toString();
		}
		// As no interface has been found create an exception
		throw new LoadEngineException( new File( this.enginePath ), errMsg );
	}

	/**
	 * Return the engine instance from where to call the corresponging engine
	 * 
	 * @return engine instance
	 */
	public DeepLearningEngineInterface getEngineInstance()
	{
		return this.engineInstance;
	}

	/**
	 * Close the created ClassLoader
	 */
	// TODO is it necessary??
	public void close()
	{
		engineInstance.closeModel();
		setBaseClassLoader();
		System.out.println( "Exited engine ClassLoader" );
	}
}
