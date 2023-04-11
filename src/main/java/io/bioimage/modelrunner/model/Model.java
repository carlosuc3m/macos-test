/*-
 * #%L
 * Use deep learning frameworks from Java in an agnostic and isolated way.
 * %%
 * Copyright (C) 2022 - 2023 Institut Pasteur and BioImage.IO developers.
 * %%
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */
/**
 * 
 */
package io.bioimage.modelrunner.model;

import java.util.List;

import io.bioimage.modelrunner.engine.DeepLearningEngineInterface;
import io.bioimage.modelrunner.engine.EngineInfo;
import io.bioimage.modelrunner.engine.EngineLoader;
import io.bioimage.modelrunner.exceptions.LoadEngineException;
import io.bioimage.modelrunner.exceptions.LoadModelException;
import io.bioimage.modelrunner.exceptions.RunModelException;
import io.bioimage.modelrunner.tensor.Tensor;

import net.imglib2.type.numeric.real.FloatType;

/**
 * Class that manages a Deep Learning model to load it and run it.
 * 
 * @author Carlos Garcia Lopez de Haro
 */
public class Model
{
	/**
	 * ClassLoader containing all the classes needed to use the corresponding
	 * Deep Learning framework (engine).
	 */
	private EngineLoader engineClassLoader;

	/**
	 * All the information needed to load the engine corresponding to the model
	 * and the model itself.
	 */
	private EngineInfo engineInfo;

	/**
	 * Path to the folder containing the Bioimage.io model
	 */
	private String modelFolder;

	/**
	 * Source file of the Deep Learning model as defined in the yaml file
	 */
	private String modelSource;

	/**
	 * Model name as defined in the yaml file. For identification purposes
	 */
	private String modelName;

	/**
	 * Construct the object model with all the needed information to load a
	 * model and make inference
	 * 
	 * @param engineInfo
	 *            informaton needed about the model
	 * @param modelFolder
	 *            directory where of the model folder
	 * @param modelSource
	 *            name of the actual model file (.pt for torchscript)
	 * @param classLoader
	 *            parent ClassLoader (can be null)
	 * @throws LoadEngineException
	 *             if there is an error finding the Deep LEarningn interface
	 *             that connects with the DL libraries
	 * @throws Exception
	 *             if the directory is not found
	 */
	private Model( EngineInfo engineInfo, String modelFolder, String modelSource, ClassLoader classLoader )
			throws LoadEngineException, Exception
	{
		this.engineInfo = engineInfo;
		this.modelFolder = modelFolder;
		this.modelSource = modelSource;
		setEngineClassLoader( classLoader );
	}

	/**
	 * Creates a DeepLearning model {@link Model} from the wanted Deep Learning
	 * framework (engine)
	 * 
	 * @param modelFolder
	 *            String path to the folder where all the components of the
	 *            model are stored
	 * @param modelSource
	 *            String path to the actual model file. In Pytorch is the path
	 *            to a .pt file and for Tf it is the same as the modelFolder
	 * @param engineInfo
	 *            all the information needed to load the classes of a Deep
	 *            Learning framework (engine)
	 * @return the Model that is going to be used to make inference
	 * @throws LoadEngineException
	 *             if there is an error finding the Deep LEarningn interface
	 *             that connects with the DL libraries
	 * @throws Exception
	 *             if the directory is not found
	 */
	public static Model createDeepLearningModel( String modelFolder, String modelSource, EngineInfo engineInfo )
			throws LoadEngineException, Exception
	{
		return new Model( engineInfo, modelFolder, modelSource, null );
	}

	/**
	 * Creates a DeepLearning model {@link Model} from the wanted Deep Learning
	 * framework (engine)
	 * 
	 * @param modelFolder
	 *            String path to the folder where all the components of the
	 *            model are stored
	 * @param modelSource
	 *            String path to the actual model file. In Pytorch is the path
	 *            to a .pt file and for Tf it is the same as the modelFolder
	 * @param engineInfo
	 *            all the information needed to load the classes of a Deep
	 *            Learning framework (engine)
	 * @param classLoader
	 *            parent ClassLoader (can be null)
	 * @return the Model that is going to be used to make inference
	 * @throws LoadEngineException
	 *             if there is an error finding the Deep LEarningn interface
	 *             that connects with the DL libraries
	 * @throws Exception
	 *             if the directory is not found
	 */
	public static Model createDeepLearningModel( String modelFolder, String modelSource, EngineInfo engineInfo,
			ClassLoader classLoader ) throws LoadEngineException, Exception
	{
		return new Model( engineInfo, modelFolder, modelSource, classLoader );
	}

	/**
	 * Sets the classloader containing the Deep Learning engine
	 * 
	 * @param classLoader
	 *            parent ClassLoader (can be null)
	 * @throws LoadEngineException
	 *             if there is an error finding the Deep LEarningn interface
	 *             that connects with the DL libraries
	 * @throws Exception
	 *             if the directory is not found
	 */
	public void setEngineClassLoader( ClassLoader classLoader ) throws LoadEngineException, Exception
	{
		this.engineClassLoader = EngineLoader.createEngine(
				( classLoader == null ) ? Thread.currentThread().getContextClassLoader() : classLoader, engineInfo );
	}

	/**
	 * Load the model wanted to make inference into the particular ClassLoader
	 * created to run a specific Deep Learning framework (engine)
	 * 
	 * @throws LoadModelException
	 *             if the model was not loaded
	 */
	public void loadModel() throws LoadModelException
	{
		DeepLearningEngineInterface engineInstance = engineClassLoader.getEngineInstance();
		engineClassLoader.setEngineClassLoader();
		engineInstance.loadModel( modelFolder, modelSource );
		engineClassLoader.setBaseClassLoader();
	}

	/**
	 * Close the Deep LEarning model in the ClassLoader where the Deep Learning
	 * framework has been called and instantiated
	 */
	public void closeModel()
	{
		DeepLearningEngineInterface engineInstance = getEngineClassLoader().getEngineInstance();
		engineClassLoader.setEngineClassLoader();
		engineInstance.closeModel();
		getEngineClassLoader().close();
		engineInstance = null;
		engineClassLoader.setBaseClassLoader();
		engineClassLoader = null;
	}

	/**
	 * Method that calls the ClassLoader with the corresponding JARs of the Deep
	 * Learning framework (engine) loaded to run inference on the tensors. The
	 * method returns the corresponding output tensors
	 * 
	 * @param inTensors
	 *            input tensors containing all the tensor data
	 * @param outTensors
	 *            expected output tensors. Their backend data will be rewritten with the result of the inference
	 * @throws RunModelException
	 *             if the is any problem running the model
	 * @throws RunModelException
	 *             if there is any problem closing the tensors
	 */
	public void runModel( List< Tensor < ? > > inTensors, List< Tensor < ? > > outTensors ) throws RunModelException, Exception
	{
		DeepLearningEngineInterface engineInstance = engineClassLoader.getEngineInstance();
		engineClassLoader.setEngineClassLoader();
		inTensors.stream().forEach( tt -> tt = Tensor.createCopyOfTensorInWantedDataType( tt, new FloatType() ) );
		engineInstance.run( inTensors, outTensors );
		engineClassLoader.setBaseClassLoader();
	}

	/**
	 * Get the EngineClassLoader created by the DeepLearning Model
	 * {@link Model}. The EngineClassLoader loads the JAR files needed to use
	 * the corresponding Deep Learning framework (engine)
	 * 
	 * @return the Model corresponding EngineClassLoader
	 */
	public EngineLoader getEngineClassLoader()
	{
		return this.engineClassLoader;
	}

	/**
	 * Get the folder where this model is located
	 * 
	 * @return the folder where this model is located
	 */
	public String getModelFolder()
	{
		return this.modelFolder;
	}

	/**
	 * Get the source of this model as specified in the yaml file
	 * 
	 * @return the source of this model from the yaml file
	 */
	public String getModelSource()
	{
		return this.modelSource;
	}

	/**
	 * Sets the model name
	 * 
	 * @param modelName
	 *            the name of the model
	 */
	public void setModelName( String modelName )
	{
		this.modelName = modelName;
	}

	/**
	 * Gets the name of the model
	 * 
	 * @return the name of the model
	 */
	public String getModelName()
	{
		return this.modelName;
	}
}
