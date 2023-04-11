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
package io.bioimage.modelrunner.versionmanagement;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import io.bioimage.modelrunner.engine.EngineInfo;
import io.bioimage.modelrunner.system.PlatformDetection;

import com.google.gson.Gson;

/**
 * TODO remove unused methods
 * Holds the list of available TensorFlow versions read from the JSON file.
 * 
 * @author Daniel Felipe Gonzalez Obando and Carlos Garcia Lopez de Haro
 */
public class AvailableEngines
{
	/**
	 * HashMap that translates the keys used to name Deep Learning engines
	 * in the rdf.yaml to the ones used by the Deep Learning manager
	 * 
	 */
	private static HashMap<String, String> BIOIMAGEIO_TO_MODELRUNNER_KEYS_MAP;
	static {
		BIOIMAGEIO_TO_MODELRUNNER_KEYS_MAP = new HashMap<String, String>();
		BIOIMAGEIO_TO_MODELRUNNER_KEYS_MAP.put(EngineInfo.getBioimageioPytorchKey(), EngineInfo.getPytorchKey());
		BIOIMAGEIO_TO_MODELRUNNER_KEYS_MAP.put(EngineInfo.getBioimageioTfKey(), EngineInfo.getTensorflowKey());
		BIOIMAGEIO_TO_MODELRUNNER_KEYS_MAP.put(EngineInfo.getBioimageioOnnxKey(), EngineInfo.getOnnxKey());
		BIOIMAGEIO_TO_MODELRUNNER_KEYS_MAP.put(EngineInfo.getBioimageioKerasKey(), EngineInfo.getKerasKey());
	}
	/**
	 * HashMap that translates the keys used by the Deep Learning manager to the ones
	 *  used to name Deep Learning engines in the rdf.yaml
	 */
	private static HashMap<String, String> MODELRUNNER_TO_BIOIMAGEIO_KEYS_MAP;
	static {
		MODELRUNNER_TO_BIOIMAGEIO_KEYS_MAP = new HashMap<String, String>();
		MODELRUNNER_TO_BIOIMAGEIO_KEYS_MAP.put(EngineInfo.getPytorchKey(), EngineInfo.getBioimageioPytorchKey());
		MODELRUNNER_TO_BIOIMAGEIO_KEYS_MAP.put(EngineInfo.getTensorflowKey(), EngineInfo.getBioimageioTfKey());
		MODELRUNNER_TO_BIOIMAGEIO_KEYS_MAP.put(EngineInfo.getOnnxKey(), EngineInfo.getBioimageioOnnxKey());
		MODELRUNNER_TO_BIOIMAGEIO_KEYS_MAP.put(EngineInfo.getKerasKey(), EngineInfo.getBioimageioKerasKey());
	}
	
	/**
	 * 
	 * @return that translates the keys used to name Deep Learning engines in the rdf.yaml to
	 * the model runner keys defined in the resource files
	 * https://raw.githubusercontent.com/bioimage-io/model-runner-java/main/src/main/resources/availableDLVersions.json
	 * https://github.com/bioimage-io/model-runner-java/blob/main/src/main/resources/availableDLVersions.json
	 */
	public static HashMap<String, String> bioimageioToModelRunnerKeysMap(){
		return BIOIMAGEIO_TO_MODELRUNNER_KEYS_MAP;
	}

	/**
	 * 
	 * @return the map that translates the keys used by the model runner library to the ones
	 *  used to name Deep Learning engines in the rdf.yaml
	 *  https://github.com/bioimage-io/model-runner-java/blob/main/src/main/resources/availableDLVersions.json
	 * https://raw.githubusercontent.com/bioimage-io/model-runner-java/main/src/main/resources/availableDLVersions.json
	 */
	public static HashMap<String, String> modelRunnerToBioimageioKeysMap(){
		return MODELRUNNER_TO_BIOIMAGEIO_KEYS_MAP;
	}
	
    /**
     * Creates an instance containing only Deep Learning versions compatible with the current system.
     * 
     * @return The available versions instance.
     */
    public static AvailableEngines loadCompatibleOnly()
    {
        AvailableEngines availableVersions = load();
        String currentPlatform = new PlatformDetection().toString();
        availableVersions.setVersions(availableVersions.getVersions().stream()
                .filter(v -> v.getOs().equals(currentPlatform)).collect(Collectors.toList()));
        availableVersions.getVersions().stream().forEach(x -> x.setEnginesDir());
        return availableVersions;
    }
    
    /**
     * Remove the python repeated versions from the list of versions. There
     * can be several JAva versions that reproduce the same Python version
     * @param versions
     * 	original list of versions that might contain repeated Python versions
     * @return list of versions without repeated Python versions
     */
    public static List<DeepLearningVersion> removeRepeatedPythonVersions(List<DeepLearningVersion> versions) {
    	List<DeepLearningVersion> nVersions = new ArrayList<DeepLearningVersion>();
    	for (DeepLearningVersion vv : versions) {
    		List<DeepLearningVersion> coinc = nVersions.stream()
    				.filter(v -> vv.getPythonVersion().equals(v.getPythonVersion()) 
    						&& vv.getCPU() == v.getCPU() && vv.getGPU() == v.getGPU())
    				.collect(Collectors.toList());
    		if (coinc.size() != 0 && coinc.get(0).isJavaVersionBigger(vv))
    			continue;
    		else if (coinc.size() != 0)
    			nVersions.remove(coinc.get(0));
    		nVersions.add(vv);
    	}
    	return nVersions;
    }
    
    /**
     * Return a list of all the engine versions in Python compatible to the host system
     * 
     * @return the list of deep learning versions for the given engine
     */
    public static List<String> getAvailableCompatiblePythonVersions() {
        AvailableEngines availableVersions = load();
        String currentPlatform = new PlatformDetection().toString();
        List<String> availablePythonVersions = availableVersions.getVersions().stream()
				                .filter(v -> v.getOs().equals(currentPlatform))
				                .map(DeepLearningVersion::getPythonVersion)
				                .collect(Collectors.toList());
        return availablePythonVersions;
    }
    
    /**
     * Creates an instance containing only Deep Learning versions compatible with
     * the current system and corresponding to the version of interest
     * 
     * @param engine
     * 	engine name as specified by the bioimage.io, defined at
     * 	https://github.com/bioimage-io/spec-bioimage-io/blob/gh-pages/weight_formats_spec_0_4.md
     * @return The available versions instance.
     */
    public static AvailableEngines getAvailableVersionsForEngine(String engine) {
    	AvailableEngines availableVersions = new AvailableEngines();
    	String searchEngine = AvailableEngines.getSupportedVersionsEngineTag(engine);
    	if (searchEngine == null) {
    		availableVersions.setVersions(new ArrayList<DeepLearningVersion>());
    		return availableVersions;
    	}
        availableVersions = load();
        String currentPlatform = new PlatformDetection().toString();
        availableVersions.setVersions(availableVersions.getVersions().stream()
                .filter(v -> v.getOs().equals(currentPlatform) 
                		&& searchEngine.equals(v.getEngine())
                		)
                .collect(Collectors.toList()));
        return availableVersions;
    }
    
    /**
     * Return a list of all the Python versions of the corresponding engine
     * are installed in the local machine
     * 
     * @param engine
     * 	the engine of interest
     * @return the list of deep learning versions for the given engine
     */
    public static List<String> getAvailableCompatiblePythonVersionsForEngine(String engine) {
    	String searchEngine = AvailableEngines.getSupportedVersionsEngineTag(engine);
    	if (searchEngine == null)
    		return new ArrayList<String>();
    	AvailableEngines availableVersions = load();
        String currentPlatform = new PlatformDetection().toString();
        List<String> availablePythonVersions = availableVersions.getVersions().stream()
                .filter(v -> v.getOs().equals(currentPlatform) && searchEngine.equals(v.getEngine()))
                .map(DeepLearningVersion::getPythonVersion)
                .collect(Collectors.toList());
        return availablePythonVersions;
    }

    /**
     * Loads all available versions from {@code availableTFVersion.json} file.
     * 
     * @return The instance of all available versions.
     */
    public static AvailableEngines load()
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(
                AvailableEngines.class.getClassLoader().getResourceAsStream("availableDLVersions.json")));
        Gson g = new Gson();
        AvailableEngines availableVersions = g.fromJson(br, AvailableEngines.class);
        return availableVersions;
    }

    private List<DeepLearningVersion> versions;

    /**
     * Retrieves the list of available TF versions.
     * 
     * @return The list of TF versions in this instance.
     */
    public List<DeepLearningVersion> getVersions()
    {
        return versions;
    }

    /**
     * Sets the list of versions available in this instance.
     * 
     * @param versions
     *        The versions to be available in this instance.
     */
    public void setVersions(List<DeepLearningVersion> versions)
    {
        this.versions = versions;
    }
    
    /**
     * Check if an engine is supported by the dl-modelrunner or not
     * @param framework
	 * 	DL framework as specified by the Bioimage.io model zoo ()https://github.com/bioimage-io/spec-bioimage-io/blob/gh-pages/weight_formats_spec_0_4.md)
	 * @param version
	 * 	the version of the framework
	 * @param cpu
	 * 	whether the engine supports cpu or not
	 * @param gpu
	 * 	whether the engine supports gpu or not
     * @return true if the engine exists and false otherwise
     */
    public static boolean isEngineSupported(String framework, String version, boolean cpu, boolean gpu) {
    	String searchEngine = AvailableEngines.getSupportedVersionsEngineTag(framework);
    	if (searchEngine == null)
    		return false;
    	DeepLearningVersion engine = AvailableEngines.getAvailableVersionsForEngine(searchEngine).getVersions()
				.stream().filter(v -> v.getPythonVersion().equals(version) 
						&& v.getOs().equals(new PlatformDetection().toString())
						&& v.getCPU() == cpu
						&& v.getGPU() == gpu).findFirst().orElse(null);
		if (engine == null) 
			return false;
		return true;
    }
    
    /**
     * MEthod to get the correct engine tag to parse the engine files.
     * If it receives the engine name given by the BioImage.io (torchscript, tensorflow_saved_model...)
     * it produces the names specified in the resources files (pytorch, tensorflow...).
     * If it receives the later it does nothing 
     * @param engine
     * 	an engine tag
     * @return the correct engine tag format to parse the files at resources
     */
    public static String getSupportedVersionsEngineTag(String engine) {
    	boolean engineExists = AvailableEngines.bioimageioToModelRunnerKeysMap().keySet().stream().anyMatch(i -> i.equals(engine));
    	boolean engineExists2 = AvailableEngines.bioimageioToModelRunnerKeysMap().entrySet()
    			.stream().anyMatch(i -> i.getValue().equals(engine));
    	final String searchEngine;
    	if (!engineExists && !engineExists2) 
    		return null;
    	else if (!engineExists2)
    		searchEngine = AvailableEngines.bioimageioToModelRunnerKeysMap().get(engine).toLowerCase();
    	else 
    		searchEngine = engine;
    	return searchEngine;
    }

}
