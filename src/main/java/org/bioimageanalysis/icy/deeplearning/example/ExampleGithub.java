package org.bioimageanalysis.icy.deeplearning.example;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

/**
 * This is an example of the library that runs a Deep Learning model on a supported engine locally
 * on your computer.
 * REgard that in order to this example to work, a Deep Learning model needs to be downloaded from the
 * Bioimage.io repo and a Java Deep Learning framework needs to be installed too.
 * 
 * The example model for this example is: https://bioimage.io/#/?tags=10.5281%2Fzenodo.6406756&id=10.5281%2Fzenodo.6406756
 * 
 * 
 * @author Carlos Garcia Lopez de Haro
 *
 */
public class ExampleGithub {
	
	public static void main(String[] args){
		System.out.println(new File().getAbsolutePath());
		for (File ff : new File().listFiles()){
			System.out.println(ff.getAbsolutePath());
		}
	}
}
