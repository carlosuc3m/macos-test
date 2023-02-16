package org.bioimageanalysis.icy.deeplearning.versionmanagement;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.deeplearning.system.PlatformDetection;

import com.google.gson.Gson;

/**
 * TODO remove unused methods
 * Holds the list of available TensorFlow versions read from the JSON file.
 * 
 * @author Daniel Felipe Gonzalez Obando and Carlos Garcia Lopez de Haro
 */
public class AvailableDeepLearningVersions
{
	/**
	 * HashMap that translates the keys used to name Deep Learning engines
	 * in the rdf.yaml to the ones used by the Deep Learning manager
	 */
	private static HashMap<String, String> engineKeys;
	static {
		engineKeys = new HashMap<String, String>();
		engineKeys.put("torchscript", "pytorch");
		engineKeys.put("tensorflow_saved_model_bundle", "tensorflow");
		engineKeys.put("onnx", "onnx");
		engineKeys.put("keras_hdf5", "keras");
	}
	
	/**
	 * MEthod that returns all the possible names for each the DL engines existing at the moment
	 * @return
	 */
	public static HashMap<String, String> getEngineKeys(){
		return engineKeys;
	}
	
    /**
     * Creates an instance containing only Deep Learning versions compatible with the current system.
     * 
     * @return The available versions instance.
     */
    public static AvailableDeepLearningVersions loadCompatibleOnly()
    {
        AvailableDeepLearningVersions availableVersions = load();
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
     * Return a list of all the Python versions compatible to the host system
     * 
     * @return the list of deep learning versions for the given engine
     */
    public static List<String> getAvailableCompatiblePythonVersions() {
        AvailableDeepLearningVersions availableVersions = load();
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
     * @return The available versions instance.
     */
    public static AvailableDeepLearningVersions getAvailableVersionsForEngine(String engine) {
    	boolean engineExists = engineKeys.keySet().stream().anyMatch(i -> i.equals(engine));
    	AvailableDeepLearningVersions availableVersions = new AvailableDeepLearningVersions();
    	if (!engineExists) {
    		availableVersions.setVersions(new ArrayList<DeepLearningVersion>());
    		return availableVersions;
    	}
        availableVersions = load();
        String currentPlatform = new PlatformDetection().toString();
        availableVersions.setVersions(availableVersions.getVersions().stream()
                .filter(v -> v.getOs().equals(currentPlatform) 
                		&& engineKeys.get(engine).toLowerCase().contains(v.getEngine().toLowerCase())
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
    	boolean engineExists = engineKeys.keySet().stream().anyMatch(i -> i.equals(engine));
    	if (!engineExists) {
    		return new ArrayList<String>();
    	}
    	AvailableDeepLearningVersions availableVersions = load();
        String currentPlatform = new PlatformDetection().toString();
        List<String> availablePythonVersions = availableVersions.getVersions().stream()
                .filter(v -> v.getOs().equals(currentPlatform) && engineKeys.get(engine).toLowerCase().contains(v.getEngine().toLowerCase()))
                .map(DeepLearningVersion::getPythonVersion)
                .collect(Collectors.toList());
        return availablePythonVersions;
    }

    /**
     * Loads all available versions from {@code availableTFVersion.json} file.
     * 
     * @return The instance of all available versions.
     */
    public static AvailableDeepLearningVersions load()
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(
                AvailableDeepLearningVersions.class.getClassLoader().getResourceAsStream("availableDLVersions.json")));
        Gson g = new Gson();
        AvailableDeepLearningVersions availableVersions = g.fromJson(br, AvailableDeepLearningVersions.class);
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

}
